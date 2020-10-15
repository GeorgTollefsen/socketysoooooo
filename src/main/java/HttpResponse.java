import java.util.HashMap;

public class HttpResponse {
    private String statusCode;
    public HashMap<String, String> responseHeaders;
    public String body;

    public HttpResponse(String statusCode, HashMap<String, String> responseHeaders, String body) {
        this.statusCode = statusCode;
        this.responseHeaders = responseHeaders;
        this.body = body;
    }

    public String getResponseCode() {
        return statusCode;
    }

    public String getHeader(String headerName){
        String headerValue = responseHeaders.get(headerName);
        return headerValue;
    }

    public String getBody(){
        return body;
    }
}
