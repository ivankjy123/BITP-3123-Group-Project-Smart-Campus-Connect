package my.smartcampusconnect67.reporting_service.error;

public class ErrorResponse {

    private String error;
    private int status;
    private String message;
    private String path;

    public ErrorResponse() {
    }

    public ErrorResponse(String error,
                         int status,
                         String message,
                         String path) {
        this.error = error;
        this.status = status;
        this.message = message;
        this.path = path;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}