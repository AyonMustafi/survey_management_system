package exception;

/**
 * Exception thrown when attempting to create a survey that already exists
 */
public class DuplicateSurveyException extends SurveyException {
    public DuplicateSurveyException(int surveyId) {
        super("Survey with ID " + surveyId + " already exists");
    }
}

