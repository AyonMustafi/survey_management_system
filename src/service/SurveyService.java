package service;

import model.Form;
import model.Submission;
import exception.SurveyException;
import exception.SurveyNotFoundException;
import exception.DuplicateSurveyException;
import exception.InvalidSubmissionException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe survey management service
 */
public class SurveyService {
    private final Map<Integer, Form> surveys = new ConcurrentHashMap<>();
    private final Map<Integer, AtomicInteger> submissionCounts = new ConcurrentHashMap<>();
    private final Map<Integer, AtomicLong> totalRatings = new ConcurrentHashMap<>();
    private final Map<Integer, ReadWriteLock> surveyLocks = new ConcurrentHashMap<>();
    
    /**
     * Creates a new survey
     * @param surveyId Unique survey identifier
     * @param questions Map of question ID to question text
     * @param weights Map of question ID to weight
     * @throws DuplicateSurveyException if survey already exists
     * @throws SurveyException if survey data is invalid
     */
    public void createSurvey(int surveyId, Map<Integer, String> questions, Map<Integer, Integer> weights) 
            throws SurveyException {
        if (surveys.containsKey(surveyId)) {
            throw new DuplicateSurveyException(surveyId);
        }
        
        try {
            Form form = new Form(surveyId, questions, weights);
            surveys.put(surveyId, form);
            submissionCounts.put(surveyId, new AtomicInteger(0));
            totalRatings.put(surveyId, new AtomicLong(0));
            surveyLocks.put(surveyId, new ReentrantReadWriteLock());
        } catch (IllegalArgumentException e) {
            throw new SurveyException("Invalid survey data: " + e.getMessage(), e);
        }
    }
    
    /**
     * Submits a response to a survey
     * @param surveyId Survey identifier
     * @param userId User identifier
     * @param answers Map of question ID to answer
     * @return Calculated rating for this submission
     * @throws SurveyNotFoundException if survey doesn't exist
     * @throws InvalidSubmissionException if submission is invalid
     */
    public int submitSurvey(int surveyId, String userId, Map<Integer, Integer> answers) 
            throws SurveyException {
        Form form = surveys.get(surveyId);
        if (form == null) {
            throw new SurveyNotFoundException(surveyId);
        }
        
        try {
            Submission submission = new Submission(surveyId, userId, answers);
            int rating = calculateRating(form, submission);
            
            // Thread-safe update of survey statistics
            ReadWriteLock lock = surveyLocks.get(surveyId);
            lock.writeLock().lock();
            try {
                submissionCounts.get(surveyId).incrementAndGet();
                totalRatings.get(surveyId).addAndGet(rating);
            } finally {
                lock.writeLock().unlock();
            }
            
            return rating;
        } catch (IllegalArgumentException e) {
            throw new InvalidSubmissionException("Invalid submission: " + e.getMessage());
        }
    }
    
    /**
     * Gets the average rating for a survey
     * @param surveyId Survey identifier
     * @return Average rating
     * @throws SurveyNotFoundException if survey doesn't exist
     */
    public double getAverageRating(int surveyId) throws SurveyException {
        Form form = surveys.get(surveyId);
        if (form == null) {
            throw new SurveyNotFoundException(surveyId);
        }
        
        ReadWriteLock lock = surveyLocks.get(surveyId);
        lock.readLock().lock();
        try {
            int count = submissionCounts.get(surveyId).get();
            if (count == 0) {
                return 0.0;
            }
            long total = totalRatings.get(surveyId).get();
            return (double) total / count;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Gets the number of submissions for a survey
     * @param surveyId Survey identifier
     * @return Submission count
     * @throws SurveyNotFoundException if survey doesn't exist
     */
    public int getSubmissionCount(int surveyId) throws SurveyException {
        if (!surveys.containsKey(surveyId)) {
            throw new SurveyNotFoundException(surveyId);
        }
        return submissionCounts.get(surveyId).get();
    }
    
    /**
     * Checks if a survey exists
     * @param surveyId Survey identifier
     * @return true if survey exists
     */
    public boolean surveyExists(int surveyId) {
        return surveys.containsKey(surveyId);
    }
    
    /**
     * Gets survey information
     * @param surveyId Survey identifier
     * @return Survey form
     * @throws SurveyNotFoundException if survey doesn't exist
     */
    public Form getSurvey(int surveyId) throws SurveyException {
        Form form = surveys.get(surveyId);
        if (form == null) {
            throw new SurveyNotFoundException(surveyId);
        }
        return form;
    }
    
    /**
     * Calculates rating for a submission based on survey weights
     * @param form Survey form with weights
     * @param submission User submission
     * @return Calculated rating
     * @throws InvalidSubmissionException if submission doesn't match survey format
     */
    private int calculateRating(Form form, Submission submission) throws InvalidSubmissionException {
        Map<Integer, Integer> surveyWeights = form.getWeights();
        Map<Integer, Integer> userAnswers = submission.getAnswers();
        
        // Validate that all required questions are answered
        if (!surveyWeights.keySet().equals(userAnswers.keySet())) {
            throw new InvalidSubmissionException(
                "Submission must answer all questions. Expected: " + 
                surveyWeights.keySet() + ", Got: " + userAnswers.keySet()
            );
        }
        
        int totalRating = 0;
        for (Map.Entry<Integer, Integer> entry : userAnswers.entrySet()) {
            int questionId = entry.getKey();
            int answer = entry.getValue();
            int weight = surveyWeights.get(questionId);
            totalRating += answer * weight;
        }
        
        return totalRating;
    }
}

