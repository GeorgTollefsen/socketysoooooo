import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

public class HttpMessage {

    private final String startLine;
    private final HashMap<String, String> headers;
    private final String body;

    public HttpMessage(Socket Socket) throws IOException {
        startLine = readLine(Socket);
        headers = readHeaders(Socket);
        String contentLength = headers.get("Content-Length");
        if(contentLength != null){
            body = readBody(Socket, Integer.parseInt(contentLength)+1);
        }else{
            body = null;
        }


    }

    public static String readLine(Socket socketInternalInThisMethod) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line="";
        int cha;
        while ((cha = socketInternalInThisMethod.getInputStream().read()) !=13){
            sb.append((char)cha);
        }
        line = sb.toString();

        return line;
    }

    static String readBody(Socket newSocket, int contentLength) throws IOException {
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<contentLength; i++){
            int c = newSocket.getInputStream().read();
            sb.append((char)c);
        }
        return sb.toString().trim();
    }

    static HashMap<String, String> readHeaders(Socket newSocket) throws IOException {
        String lineFromServer;
        HashMap<String, String> headers = new HashMap<>();
        while (true){

            lineFromServer = readLine(newSocket);
            if(lineFromServer.equals("\n")||lineFromServer.equals("\r")){
                break;
            }
            System.out.println("Client får fra server: "+lineFromServer);
            String responseName = lineFromServer.split(" ")[0].trim();
            responseName = responseName.substring(0, responseName.length()-1).trim();
            String responseValue = lineFromServer.split(" ")[1];
            headers.put(responseName, responseValue);
        }
        return headers;
    }

    public String getStartLine() {
        return startLine;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }
}
