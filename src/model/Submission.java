package model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable submission representing a user's response to a survey
 */
public class Submission {
    private final int surveyId;
    private final String userId;
    private final Map<Integer, Integer> answers;
    private final long timestamp;
    
    public Submission(int surveyId, String userId, Map<Integer, Integer> answers) {
        this.surveyId = surveyId;
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.answers = Collections.unmodifiableMap(new HashMap<>(answers));
        this.timestamp = System.currentTimeMillis();
        
        // Validation
        if (userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }
        if (answers.isEmpty()) {
            throw new IllegalArgumentException("Submission must have at least one answer");
        }
    }
    
    public int getSurveyId() {
        return surveyId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public Map<Integer, Integer> getAnswers() {
        return answers;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Submission that = (Submission) o;
        return surveyId == that.surveyId &&
               timestamp == that.timestamp &&
               Objects.equals(userId, that.userId) &&
               Objects.equals(answers, that.answers);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(surveyId, userId, answers, timestamp);
    }
    
    @Override
    public String toString() {
        return "Submission{" +
               "surveyId=" + surveyId +
               ", userId='" + userId + '\'' +
               ", answerCount=" + answers.size() +
               ", timestamp=" + timestamp +
               '}';
    }
}