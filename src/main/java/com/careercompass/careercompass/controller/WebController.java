package com.careercompass.careercompass.controller;

import com.careercompass.careercompass.dto.StudentProfileDTO;
import com.careercompass.careercompass.entity.User;
import com.careercompass.careercompass.repository.UserRepository;
import com.careercompass.careercompass.security.UserDetailsImpl;
import com.careercompass.careercompass.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @ModelAttribute("isAuthenticated")
    public boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);
    }

    @ModelAttribute("currentUserName")
    public String currentUserName(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetailsImpl userDetails) {
                return userDetails.getUser().getName();
            }
            return authentication.getName();
        }
        return null;
    }

    @ModelAttribute("currentUserEmail")
    public String currentUserEmail(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            return authentication.getName();
        }
        return null;
    }

    @ModelAttribute("studentProfile")
    public StudentProfileDTO studentProfile(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            try {
                return userService.getStudentProfile();
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    @GetMapping("/")
    public String landing(Model model) {
        model.addAttribute("activePage", "landing");
        return "student/landing";
    }

    @GetMapping("/login")
    public String login(Model model, Authentication authentication) {
        if (isAuthenticated(authentication)) {
            return "redirect:/dashboard";
        }
        model.addAttribute("activePage", "login");
        return "student/login";
    }

    @GetMapping("/register")
    public String register(Model model, Authentication authentication) {
        if (isAuthenticated(authentication)) {
            return "redirect:/dashboard";
        }
        model.addAttribute("activePage", "register");
        return "student/register";
    }

    @GetMapping("/onboarding")
    public String onboarding(Model model) {
        model.addAttribute("activePage", "onboarding");
        return "student/onboarding";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("activePage", "dashboard");
        return "student/dashboard";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        model.addAttribute("activePage", "profile");
        return "student/profile";
    }

    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("activePage", "settings");
        return "student/settings";
    }

    @GetMapping("/careers")
    public String careers(Model model) {
        model.addAttribute("activePage", "careers");
        return "student/careers";
    }

    @GetMapping("/career-detail")
    public String careerDetailByParam(@RequestParam(value = "role", required = false, defaultValue = "Software Development Engineer (SDE)") String role, Model model) {
        model.addAttribute("careerName", role);
        model.addAttribute("activePage", "careers");
        return "student/career-detail";
    }

    @GetMapping("/careers/{name}")
    public String careerDetail(@PathVariable String name, Model model) {
        model.addAttribute("careerName", name);
        model.addAttribute("activePage", "careers");
        return "student/career-detail";
    }

    @GetMapping("/skill-assessment")
    public String skillAssessment(Model model) {
        model.addAttribute("activePage", "skill-assessment");
        return "student/skill-assessment";
    }

    @GetMapping("/roadmap")
    public String roadmap(Model model) {
        model.addAttribute("activePage", "roadmap");
        return "student/roadmap";
    }

    @GetMapping("/daily-tasks")
    public String dailyTasks(Model model) {
        model.addAttribute("activePage", "daily-tasks");
        return "student/daily-tasks";
    }

    @GetMapping("/job-description")
    public String jobDescription(Model model) {
        model.addAttribute("activePage", "job-description");
        return "student/job-description";
    }

    @GetMapping("/resources")
    public String resources(Model model) {
        model.addAttribute("activePage", "resources");
        return "student/resources";
    }

    @GetMapping("/projects")
    public String projects(Model model) {
        model.addAttribute("activePage", "projects");
        return "student/projects";
    }

    @GetMapping("/question-sets")
    public String questionSets(Model model) {
        model.addAttribute("activePage", "question-sets");
        return "student/question-sets";
    }

    @GetMapping("/companies")
    public String companies(Model model) {
        model.addAttribute("activePage", "companies");
        return "student/companies";
    }

    @GetMapping("/interview-experiences")
    public String interviewExperiences(Model model) {
        model.addAttribute("activePage", "interview-experiences");
        return "student/interview-experiences";
    }

    @GetMapping("/progress")
    public String progress(Model model) {
        model.addAttribute("activePage", "progress");
        return "student/progress";
    }
}
