import service.SurveyService;
import exception.SurveyException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Main class demonstrating the Survey Management System
 */
public class Main {
    private static final int SURVEY_ID = 1;
    private static final int THREAD_COUNT = 10;
    private static final int SUBMISSIONS_PER_THREAD = 5;
    
    public static void main(String[] args) {
        SurveyService surveyService = new SurveyService();
        
        try {
            // Create survey
            createSampleSurvey(surveyService);
            
            // Test concurrent submissions
            testConcurrentSubmissions(surveyService);
            
            // Display results
            displayResults(surveyService);
            
        } catch (SurveyException e) {
            System.err.println("Survey error: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("Thread interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
    
    private static void createSampleSurvey(SurveyService surveyService) throws SurveyException {
        Map<Integer, String> questions = new HashMap<>();
        questions.put(1, "How is the service quality?");
        questions.put(2, "How is the product quality?");
        questions.put(3, "How is the staff behavior?");
        
        Map<Integer, Integer> weights = new HashMap<>();
        weights.put(1, 2);
        weights.put(2, 4);
        weights.put(3, 5);
        
        surveyService.createSurvey(SURVEY_ID, questions, weights);
        System.out.println("Survey created successfully with ID: " + SURVEY_ID);
    }
    
    private static void testConcurrentSubmissions(SurveyService surveyService) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        
        System.out.println("Starting concurrent submissions...");
        
        for (int i = 0; i < THREAD_COUNT; i++) {
            final int threadId = i;
            executor.submit(() -> {
                for (int j = 0; j < SUBMISSIONS_PER_THREAD; j++) {
                    try {
                        Map<Integer, Integer> answers = generateRandomAnswers();
                        String userId = "user_" + threadId + "_" + j;
                        int rating = surveyService.submitSurvey(SURVEY_ID, userId, answers);
                        
                        System.out.println("Thread " + threadId + 
                                         ", User " + userId + 
                                         ", Rating: " + rating);
                    } catch (SurveyException e) {
                        System.err.println("Submission failed in thread " + threadId + ": " + e.getMessage());
                    }
                }
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);
        System.out.println("All submissions completed.");
    }
    
    private static Map<Integer, Integer> generateRandomAnswers() {
        Map<Integer, Integer> answers = new HashMap<>();
        answers.put(1, (int) (Math.random() * 10) + 1); // 1-10
        answers.put(2, (int) (Math.random() * 10) + 1); // 1-10
        answers.put(3, (int) (Math.random() * 10) + 1); // 1-10
        return answers;
    }
    
    private static void displayResults(SurveyService surveyService) throws SurveyException {
        System.out.println("\n=== SURVEY RESULTS ===");
        System.out.println("Survey ID: " + SURVEY_ID);
        System.out.println("Total Submissions: " + surveyService.getSubmissionCount(SURVEY_ID));
        System.out.println("Average Rating: " + String.format("%.2f", surveyService.getAverageRating(SURVEY_ID)));
        
        // Display survey questions
        var survey = surveyService.getSurvey(SURVEY_ID);
        System.out.println("\nSurvey Questions:");
        survey.getQuestions().forEach((id, question) -> 
            System.out.println("Q" + id + ": " + question + " (Weight: " + survey.getWeights().get(id) + ")")
        );
    }
}