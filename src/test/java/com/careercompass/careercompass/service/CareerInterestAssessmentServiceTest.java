
package com.careercompass.careercompass.service;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class CareerInterestAssessmentServiceTest {

    private final CareerInterestAssessmentService service =
            new CareerInterestAssessmentService();

    // Creates eight answers, with option A selected for every question.
    private Map<String, String> allAAnswers() {
        Map<String, String> answers = new LinkedHashMap<>();

        for (int i = 1; i <= 8; i++) {
            answers.put("Q" + i, "A");
        }

        return answers;
    }

    @Test
    void shouldReturnEightQuestionsWithFourOptionsEach() {

        var questions = service.getQuestions();

        assertEquals(8, questions.size());

        questions.forEach(question ->
                assertEquals(4, question.options().size())
        );
    }

    @Test
    void shouldCalculateCareerInterestScoresCorrectly() {

        var result = service.evaluate(allAAnswers(), null);

        assertEquals(
                100,
                result.scores().get("Software Development Engineer (SDE)")
        );

        assertEquals(0, result.scores().get("Data Analyst"));
        assertEquals(21, result.scores().get("AI/ML Engineer"));
        assertEquals(13, result.scores().get("Cybersecurity Engineer"));
    }

    @Test
    void shouldReturnTopThreeRecommendations() {

        var result = service.evaluate(allAAnswers(), null);

        assertEquals(3, result.recommendations().size());

        assertEquals(
                "Software Development Engineer (SDE)",
                result.recommendedCareerName()
        );
    }

    @Test
    void shouldAllowManualCareerSelection() {

        var result = service.evaluate(
                allAAnswers(),
                "Data Analyst"
        );

        assertEquals("Data Analyst", result.selectedCareerName());
    }

    @Test
    void shouldRejectIncompleteAnswers() {

        Map<String, String> answers = Map.of("Q1", "A");

        assertThrows(
                IllegalArgumentException.class,
                () -> service.evaluate(answers, null)
        );
    }

    @Test
    void shouldRejectInvalidAnswerOption() {

        Map<String, String> answers = allAAnswers();
        answers.put("Q3", "Z");

        assertThrows(
                IllegalArgumentException.class,
                () -> service.evaluate(answers, null)
        );
    }
}
