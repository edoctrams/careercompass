package com.careercompass.careercompass.service;

import com.careercompass.careercompass.entity.*;
import com.careercompass.careercompass.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PersonalizedRoadmapService {

    @Autowired
    private RoadmapRepository roadmapRepository;

    @Autowired
    private RoadmapTaskRepository roadmapTaskRepository;

    @Autowired
    private CareerSkillRepository careerSkillRepository;

    @Autowired
    private CompanyCareerSkillRepository companyCareerSkillRepository;

    @Autowired
    private UserSkillRepository userSkillRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private LearningResourceRepository learningResourceRepository;

    private static class SkillGapNode {
        final CareerSkill careerSkill;
        final Skill skill;
        final int currentLevel;
        final int requiredLevel;
        final int gap;
        final int importance;
        final int priorityScore;
        final boolean companyTargeted;

        SkillGapNode(CareerSkill careerSkill, int currentLevel, int requiredLevel, int importance, boolean companyTargeted) {
            this.careerSkill = careerSkill;
            this.skill = careerSkill.getSkill();
            this.currentLevel = currentLevel;
            this.requiredLevel = requiredLevel;
            this.gap = Math.max(0, requiredLevel - currentLevel);
            this.importance = importance;
            this.companyTargeted = companyTargeted;
            this.priorityScore = (this.gap * 3) + this.importance + (companyTargeted ? 1 : 0);
        }
    }

    @Transactional
    public Roadmap generateOrUpdateRoadmap(User user, CareerPath career, Company company, LocalDate requestedDeadline) {
        Long targetCompanyId = company != null ? company.getId() : null;
        List<Roadmap> existingRoadmaps = roadmapRepository.findByUserId(user.getId());

        Roadmap activeMatch = null;
        for (Roadmap r : existingRoadmaps) {
            if (r.getStatus() == RoadmapStatus.ACTIVE) {
                Long rCareerId = r.getCareerPath() != null ? r.getCareerPath().getId() : null;
                Long rCompanyId = r.getCompany() != null ? r.getCompany().getId() : null;
                if (Objects.equals(rCareerId, career.getId()) && Objects.equals(rCompanyId, targetCompanyId) && activeMatch == null) {
                    activeMatch = r;
                } else {
                    r.setStatus(RoadmapStatus.ARCHIVED);
                    roadmapRepository.save(r);
                }
            }
        }

        LocalDate defaultDeadline = LocalDate.now().plusMonths(
                career.getAveragePreparationMonths() != null ? career.getAveragePreparationMonths() : 6);

        Roadmap roadmap;
        if (activeMatch != null) {
            roadmap = activeMatch;
            if (requestedDeadline != null) {
                roadmap.setTargetDeadline(requestedDeadline);
                roadmap = roadmapRepository.save(roadmap);
            } else if (roadmap.getTargetDeadline() == null) {
                roadmap.setTargetDeadline(defaultDeadline);
                roadmap = roadmapRepository.save(roadmap);
            }
        } else {
            roadmap = new Roadmap();
            roadmap.setUser(user);
            roadmap.setCareerPath(career);
            roadmap.setCompany(company);
            roadmap.setTitle((company != null ? company.getName() + " - " : "") + career.getName() + " Personalized Roadmap");
            roadmap.setTargetDeadline(requestedDeadline != null ? requestedDeadline : defaultDeadline);
            roadmap.setStatus(RoadmapStatus.ACTIVE);
            roadmap = roadmapRepository.save(roadmap);
        }

        List<RoadmapTask> existingTasks = roadmapTaskRepository.findByRoadmapId(roadmap.getId());
        Set<Long> completedSkillIds = new HashSet<>();
        int maxCompletedWeek = 0;
        List<RoadmapTask> tasksToDelete = new ArrayList<>();

        for (RoadmapTask task : existingTasks) {
            if (task.getStatus() == RoadmapTaskStatus.COMPLETED) {
                if (task.getSkill() != null) {
                    completedSkillIds.add(task.getSkill().getId());
                }
                if (task.getWeekNumber() != null && task.getWeekNumber() > maxCompletedWeek) {
                    maxCompletedWeek = task.getWeekNumber();
                }
            } else {
                tasksToDelete.add(task);
            }
        }
        if (!tasksToDelete.isEmpty()) {
            roadmapTaskRepository.deleteAll(tasksToDelete);
        }

        StudentProfile profile = studentProfileRepository.findByUserId(user.getId()).orElse(null);
        int dailyHours = (profile != null && profile.getDailyAvailableHours() != null && profile.getDailyAvailableHours() > 0)
                ? profile.getDailyAvailableHours() : 2;
        int weeklyCapacityHours = Math.max(4, dailyHours * 5);

        Map<Long, Integer> userProficiencies = userSkillRepository.findByUserId(user.getId()).stream()
                .collect(Collectors.toMap(us -> us.getSkill().getId(), UserSkill::getProficiency, (a, b) -> b));

        Map<Long, CompanyCareerSkill> companyOverrides = new HashMap<>();
        if (company != null) {
            for (CompanyCareerSkill ccs : companyCareerSkillRepository.findByCompanyIdAndCareerPathId(company.getId(), career.getId())) {
                companyOverrides.put(ccs.getSkill().getId(), ccs);
            }
        }

        List<CareerSkill> careerSkills = careerSkillRepository.findByCareerPathId(career.getId());
        List<SkillGapNode> allNodes = new ArrayList<>();
        for (CareerSkill cs : careerSkills) {
            Long skillId = cs.getSkill().getId();
            if (completedSkillIds.contains(skillId)) {
                continue;
            }
            CompanyCareerSkill ccs = companyOverrides.get(skillId);
            int requiredLevel = Math.max(cs.getRequiredLevel(), ccs != null ? ccs.getTargetLevel() : 0);
            int importance = Math.max(cs.getImportance(), ccs != null ? ccs.getImportance() : 0);
            int currentLevel = userProficiencies.getOrDefault(skillId, 0);
            allNodes.add(new SkillGapNode(cs, currentLevel, requiredLevel, importance, ccs != null));
        }

        List<SkillGapNode> prioritized = allNodes.stream()
                .filter(n -> n.gap > 0)
                .collect(Collectors.toList());

        if (prioritized.isEmpty()) {
            prioritized = new ArrayList<>(allNodes);
        }

        prioritized.sort(Comparator
                .comparingInt((SkillGapNode n) -> n.priorityScore).reversed()
                .thenComparingInt(n -> n.gap).reversed()
                .thenComparing(n -> n.skill.getId()));

        List<SkillGapNode> ordered = orderByPrerequisites(prioritized);

        long weeksUntilDeadline = roadmap.getTargetDeadline() != null
                ? ChronoUnit.WEEKS.between(LocalDate.now(), roadmap.getTargetDeadline())
                : 12L;
        int maxWeeks = (int) Math.max(4L, Math.min(52L, weeksUntilDeadline));

        Map<Long, List<LearningResource>> resourcesBySkillId = learningResourceRepository.findAll().stream()
                .collect(Collectors.groupingBy(lr -> lr.getSkill().getId()));

        int currentWeek = Math.max(1, maxCompletedWeek + (maxCompletedWeek > 0 ? 1 : 0));
        int hoursUsedInWeek = 0;
        List<RoadmapTask> tasksToSave = new ArrayList<>();

        for (SkillGapNode node : ordered) {
            int estimatedHours = Math.max(2, node.gap > 0 ? node.gap * 3 : 4);
            if (hoursUsedInWeek > 0 && hoursUsedInWeek + estimatedHours > weeklyCapacityHours) {
                currentWeek = Math.min(maxWeeks, currentWeek + 1);
                hoursUsedInWeek = 0;
            }

            List<LearningResource> resources = resourcesBySkillId.getOrDefault(node.skill.getId(), Collections.emptyList());
            LearningResource primaryResource = resources.isEmpty() ? null : resources.get(0);

            RoadmapTask task = new RoadmapTask();
            task.setRoadmap(roadmap);
            task.setSkill(node.skill);
            task.setTitle(node.skill.getName() + " (Level " + node.currentLevel + " -> " + node.requiredLevel + ")");

            StringBuilder desc = new StringBuilder();
            desc.append("Focus on ").append(node.skill.getName())
                .append(" (gap: ").append(node.gap)
                .append(", importance: ").append(node.importance).append("/5).");
            if (company != null && node.companyTargeted) {
                desc.append(" Aligned with ").append(company.getName()).append(" target proficiency.");
            }
            if (node.careerSkill.getPrerequisiteSkill() != null) {
                desc.append(" Builds on prerequisite: ").append(node.careerSkill.getPrerequisiteSkill().getName()).append(".");
            }

            task.setDescription(desc.toString());
            task.setWeekNumber(currentWeek);
            task.setEstimatedHours(estimatedHours);
            task.setLearningResource(primaryResource);
            task.setResourceUrl(primaryResource != null ? primaryResource.getResourceUrl() : null);
            task.setStatus(RoadmapTaskStatus.NOT_STARTED);
            tasksToSave.add(task);

            hoursUsedInWeek += estimatedHours;
        }

        if (!tasksToSave.isEmpty()) {
            roadmapTaskRepository.saveAll(tasksToSave);
        }

        return roadmap;
    }

    private List<SkillGapNode> orderByPrerequisites(List<SkillGapNode> prioritized) {
        Map<Long, SkillGapNode> bySkillId = new LinkedHashMap<>();
        for (SkillGapNode node : prioritized) {
            bySkillId.put(node.skill.getId(), node);
        }

        List<SkillGapNode> result = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        Set<Long> visiting = new HashSet<>();

        for (SkillGapNode node : prioritized) {
            visitSkill(node, bySkillId, visited, visiting, result);
        }
        return result;
    }

    private void visitSkill(
            SkillGapNode node,
            Map<Long, SkillGapNode> bySkillId,
            Set<Long> visited,
            Set<Long> visiting,
            List<SkillGapNode> result) {
        Long skillId = node.skill.getId();
        if (visited.contains(skillId) || visiting.contains(skillId)) {
            return;
        }
        visiting.add(skillId);

        Skill prereq = node.careerSkill.getPrerequisiteSkill();
        if (prereq != null && bySkillId.containsKey(prereq.getId())) {
            visitSkill(bySkillId.get(prereq.getId()), bySkillId, visited, visiting, result);
        }

        visiting.remove(skillId);
        visited.add(skillId);
        result.add(node);
    }
}
