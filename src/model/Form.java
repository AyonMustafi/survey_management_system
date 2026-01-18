package model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable form representing a survey with questions and their weights
 */
public class Form {
    private final int surveyId;
    private final Map<Integer, String> questions;
    private final Map<Integer, Integer> weights;
    
    public Form(int surveyId, Map<Integer, String> questions, Map<Integer, Integer> weights) {
        this.surveyId = surveyId;
        this.questions = Collections.unmodifiableMap(new HashMap<>(questions));
        this.weights = Collections.unmodifiableMap(new HashMap<>(weights));
        
        // Validation
        if (questions.size() != weights.size()) {
            throw new IllegalArgumentException("Questions and weights must have the same size");
        }
        if (questions.isEmpty()) {
            throw new IllegalArgumentException("Survey must have at least one question");
        }
    }
    
    public int getSurveyId() {
        return surveyId;
    }
    
    public Map<Integer, String> getQuestions() {
        return questions;
    }
    
    public Map<Integer, Integer> getWeights() {
        return weights;
    }
    
    public int getQuestionCount() {
        return questions.size();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Form form = (Form) o;
        return surveyId == form.surveyId &&
               Objects.equals(questions, form.questions) &&
               Objects.equals(weights, form.weights);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(surveyId, questions, weights);
    }
    
    @Override
    public String toString() {
        return "Form{" +
               "surveyId=" + surveyId +
               ", questionCount=" + questions.size() +
               '}';
    }
}