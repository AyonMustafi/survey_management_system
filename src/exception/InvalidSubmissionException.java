package exception;

/**
 * Exception thrown when a submission is invalid
 */
public class InvalidSubmissionException extends SurveyException {
    public InvalidSubmissionException(String message) {
        super(message);
    }
}

