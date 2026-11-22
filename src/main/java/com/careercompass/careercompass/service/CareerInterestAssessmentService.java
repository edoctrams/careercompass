
package com.careercompass.careercompass.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class CareerInterestAssessmentService {

    private static final String SDE =
            "Software Development Engineer (SDE)";
    private static final String DATA =
            "Data Analyst";
    private static final String AI =
            "AI/ML Engineer";
    private static final String CYBER =
            "Cybersecurity Engineer";

    private static final List<String> CAREERS =
            List.of(SDE, DATA, AI, CYBER);

    private final List<Question> questions = List.of(

        new Question("Q1",
            "Which activity would you enjoy most?",
            List.of(
                option("A", "Designing and building software applications", SDE, AI),
                option("B", "Finding patterns and insights in datasets", DATA, AI),
                option("C", "Building intelligent prediction models", AI, DATA),
                option("D", "Investigating vulnerabilities and security threats", CYBER, SDE)
            )
        ),

        new Question("Q2",
            "How would you approach an unfamiliar problem?",
            List.of(
                option("A", "Break it into logical steps and algorithms", SDE, CYBER),
                option("B", "Collect evidence and analyze relevant data", DATA, AI),
                option("C", "Experiment with models and predictions", AI, SDE),
                option("D", "Investigate possible risks and weaknesses", CYBER, DATA)
            )
        ),

        new Question("Q3",
            "Which result would make you most satisfied?",
            List.of(
                option("A", "A reliable and efficient software product", SDE, AI),
                option("B", "A report that supports better decisions", DATA, AI),
                option("C", "An intelligent system that learns from data", AI, DATA),
                option("D", "A system protected against security attacks", CYBER, SDE)
            )
        ),

        new Question("Q4",
            "Which subject interests you the most?",
            List.of(
                option("A", "Data Structures and Algorithms", SDE, CYBER),
                option("B", "Statistics and Data Visualization", DATA, AI),
                option("C", "Machine Learning and Deep Learning", AI, DATA),
                option("D", "Network Security and Cryptography", CYBER, SDE)
            )
        ),

        new Question("Q5",
            "When a system behaves unexpectedly, what interests you?",
            List.of(
                option("A", "Debugging the software logic", SDE, AI),
                option("B", "Analyzing logs and identifying patterns", DATA, CYBER),
                option("C", "Investigating model predictions and errors", AI, DATA),
                option("D", "Checking for suspicious activity or breaches", CYBER, SDE)
            )
        ),

        new Question("Q6",
            "Which project would you choose?",
            List.of(
                option("A", "Developing a scalable web application", SDE, AI),
                option("B", "Building an interactive analytics dashboard", DATA, AI),
                option("C", "Creating an image or text recognition model", AI, DATA),
                option("D", "Developing a network intrusion detection system", CYBER, SDE)
            )
        ),

        new Question("Q7",
            "Which challenge motivates you most?",
            List.of(
                option("A", "Optimizing code and system performance", SDE, AI),
                option("B", "Converting raw data into useful insights", DATA, AI),
                option("C", "Improving model accuracy and intelligence", AI, DATA),
                option("D", "Identifying and preventing security threats", CYBER, SDE)
            )
        ),

        new Question("Q8",
            "What kind of professional impact would you prefer?",
            List.of(
                option("A", "Creating dependable software products", SDE, CYBER),
                option("B", "Helping organizations make informed decisions", DATA, AI),
                option("C", "Developing intelligent technology solutions", AI, SDE),
                option("D", "Protecting information and digital infrastructure", CYBER, SDE)
            )
        )
    );

    private static Option option(
            String id,
            String text,
            String primary,
            String secondary) {

        return new Option(id, text, primary, secondary);
    }

    public List<QuestionView> getQuestions() {
        return questions.stream()
                .map(q -> new QuestionView(
                        q.id(),
                        q.prompt(),
                        q.options().stream()
                                .map(o -> new OptionView(o.id(), o.text()))
                                .toList()
                ))
                .toList();
    }

    public AssessmentResult evaluate(
            Map<String, String> answers,
            String selectedCareerName) {

        if (answers == null || answers.size() != questions.size()) {
            throw new IllegalArgumentException(
                    "Please answer all 8 assessment questions."
            );
        }

        Map<String, Integer> rawScores = new LinkedHashMap<>();

        CAREERS.forEach(career -> rawScores.put(career, 0));

        for (Question question : questions) {

            String answerId = answers.get(question.id());

            Option selectedOption = question.options().stream()
                    .filter(o -> o.id().equals(answerId))
                    .findFirst()
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Invalid answer for " + question.id()
                            )
                    );

            rawScores.merge(
                    selectedOption.primary(), 3, Integer::sum
            );

            rawScores.merge(
                    selectedOption.secondary(), 1, Integer::sum
            );
        }

        // Every career has a theoretical maximum of 24 points.
        Map<String, Integer> normalizedScores = new LinkedHashMap<>();

        rawScores.forEach((career, score) ->
                normalizedScores.put(
                        career,
                        Math.round(score * 100.0f / 24)
                )
        );

        List<Recommendation> recommendations = normalizedScores
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue()
                        .reversed())
                .limit(3)
                .map(entry -> new Recommendation(
                        entry.getKey(),
                        entry.getValue(),
                        getReason(entry.getKey())
                ))
                .toList();

        String recommendedCareer = recommendations.get(0).careerName();

        String selectedCareer = selectedCareerName == null
                || selectedCareerName.isBlank()
                ? recommendedCareer
                : selectedCareerName;

        if (!CAREERS.contains(selectedCareer)) {
            throw new IllegalArgumentException(
                    "Please select one of the four supported career streams."
            );
        }

        return new AssessmentResult(
                normalizedScores,
                recommendations,
                recommendedCareer,
                selectedCareer
        );
    }

    private String getReason(String career) {

        return switch (career) {
            case SDE ->
                    "Your responses indicate an interest in logical problem-solving and software development.";

            case DATA ->
                    "Your responses indicate an interest in data analysis, statistics and evidence-based decisions.";

            case AI ->
                    "Your responses indicate an interest in machine learning, experimentation and intelligent systems.";

            case CYBER ->
                    "Your responses indicate an interest in security, risk investigation and digital protection.";

            default -> "Interest match based on your assessment responses.";
        };
    }

    private record Question(
            String id,
            String prompt,
            List<Option> options) {}

    private record Option(
            String id,
            String text,
            String primary,
            String secondary) {}

    public record QuestionView(
            String id,
            String prompt,
            List<OptionView> options) {}

    public record OptionView(
            String id,
            String text) {}

    public record Recommendation(
            String careerName,
            int score,
            String reason) {}

    public record AssessmentResult(
            Map<String, Integer> scores,
            List<Recommendation> recommendations,
            String recommendedCareerName,
            String selectedCareerName) {}
}
