package exception;

/**
 * Base exception for survey-related errors
 */
public class SurveyException extends Exception {
    public SurveyException(String message) {
        super(message);
    }
    
    public SurveyException(String message, Throwable cause) {
        super(message, cause);
    }
}

