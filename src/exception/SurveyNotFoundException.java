package exception;

/**
 * Exception thrown when a survey is not found
 */
public class SurveyNotFoundException extends SurveyException {
    public SurveyNotFoundException(int surveyId) {
        super("Survey with ID " + surveyId + " not found");
    }
}

