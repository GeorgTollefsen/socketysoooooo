import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

public class HttpClient {
    private String siTilServer;
    private int port;
    private String url;
    private String queryString;
    private String mainString;
    private HashMap<String, String> queryMap = new HashMap<String, String>();
    private String httpMethod ="GET";
    private String requestBody;
    private HashMap<String, String> responseHeaders = new HashMap<String, String>();

    public String getBody() {
        return body;
    }

    private String body;
    private String status;

    public HttpClient(String url, int port, String requestTarget) throws IOException {
        this(url,port,requestTarget, "GET", null);
    }

    //Stringen med request er det du skriver etter nettsiden
    public HttpClient( String url, int port, String requestTarget, final String httpMethod, String requestBody) throws IOException {
        this.port = port;
        this.httpMethod = httpMethod;
        this.url = url;
        this.queryString = requestTarget;
        this.requestBody = requestBody;
        queryString(queryString, queryMap);
    }

    private void queryString(String queryString, HashMap<String, String> queryMap) {
        if(queryString.contains("&")){
            String[] delerOpp = queryString.split("&");
            for(String s: delerOpp){
                String parameterName = s.split("=")[0];
                String parameterValue = s.split("=")[1];
                queryMap.put(parameterName, parameterValue);
            }
        }else if(queryString.contains("=")){
            String parameterName = queryString.split("=")[0];
            String parameterValue = queryString.split("=")[1];
            queryMap.put(parameterName, parameterValue);
        }
    }

    public String statusCode() {
        return status;
    }

    //for testing
    public HttpResponse executeRequest() throws IOException {
        Socket newSocket = new Socket(url, port);

        String contentLengthHeader = requestBody != null ? "Content-Length: "+requestBody.length()+"\r\n":"";
        String hvaViSenderTilServer = httpMethod + " " +queryString+" HTTP/1.1\r\n" +
                "Host: "+url+"\r\n" +
                contentLengthHeader+
                "\r\n";
        newSocket.getOutputStream().write(hvaViSenderTilServer.getBytes());
        if(requestBody!=null){
            newSocket.getOutputStream().write(requestBody.getBytes());
        }

        status = "200";
        body = "Not Set";

        HttpMessage response = new HttpMessage(newSocket);
        String responseLine = response.getStartLine();
        responseHeaders = response.getHeaders();
        body = response.getBody();
        System.out.println("Client får fra server: "+responseLine);
        status = responseLine.split(" ")[1];

        System.out.println("Body: "+body);
        return new HttpResponse(status, responseHeaders, body);
    }

}
