package puzzle.message;

import java.io.Serializable;

public class ErrorMessage implements Serializable {
    private String error;
    //Custom error for connection refused
    private int statusCode;

    public ErrorMessage(String error, int statusCode) {
        this.error = error;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
