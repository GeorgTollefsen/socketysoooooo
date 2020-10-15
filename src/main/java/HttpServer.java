import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HttpServer {
    private List<String> ProductNames = new ArrayList<String>();
    private int port;
    private HashMap<String, String> queryStringFromClient = new HashMap<String, String>();
    private HashMap<String, String> queryStringPost = new HashMap<String, String>();
    private ServerSocket serverSocket;
    String body;
    private File contentRoot;
    private List<String> productNames = new ArrayList<String>();

    public HttpServer(int port) throws IOException {
        this.port = port;
        serverSocket = new ServerSocket(port);
    }

    public void start() throws IOException {
        new Thread(() -> {
            while (true){
                try {
                    Socket clientSocket = serverSocket.accept();
                    handleRequest(clientSocket);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }).start(); //starter tråd
    }

    private void handleRequest(Socket klienten) throws IOException {
        HttpMessage request = new HttpMessage(klienten);
        String requestLine = request.getStartLine();
        System.out.println("Server får fra Client: "+requestLine);
        String statusCode = "200";
        String location = "No location is set";
        String whatClientWants = requestLine.split(" ")[1];
        body = "Hello World!";
        String contentType = "text/plain";
        String response = "";
        String requestMethod = requestLine.split(" ")[0];

        if(requestMethod.equals("POST")){
            queryString(request.getBody(), queryStringPost);
            productNames.add(queryStringPost.get("productName"));
            response = "HTTP/1.1 "+statusCode+" OK\r\n" +
                    "Content-Length: "+body.length() + "\r\n" +
                    "Location: "+location+"\r\n" +
                    "\r\n" +
                    body+"\r";
            klienten.getOutputStream().write(response.getBytes());
            return;
        }else if(whatClientWants.contains("?")){
            System.out.println("Nå kommer en queryString som må håndteres");
            String[] weSplitItUp = whatClientWants.split("\\?");
            String unmodifiedQueryString = weSplitItUp[1];
            if(unmodifiedQueryString.contains("&")){
                String[] splitOnAndSign = unmodifiedQueryString.split("&");
                for(String s : splitOnAndSign){
                    String[] splitItMore = s.split("=");
                    System.out.println("Server Sier: Parameter: "+splitItMore[0]+" med value: "+splitItMore[1]+" ble lagt til i map");
                    queryStringFromClient.put(splitItMore[0], splitItMore[1]);
                }
            }else{
                String[] tripleSplit = unmodifiedQueryString.split("=");
                queryStringFromClient.put(tripleSplit[0], tripleSplit[1]);
                System.out.println(tripleSplit[0]+" "+ tripleSplit[1]+" ble puttet i map");
            }
        }else if(whatClientWants.equals("/api/products")){
            handlegetProducts(klienten);

        } else if(!whatClientWants.contains("/echo")){
            File file = new File("target/", whatClientWants);
            if(!file.exists()){
                statusCode = "404";
                System.out.println("404 "+file.toString()+" not found");
                body = statusCode + " Not found\n";
                response = "HTTP/1.1 "+statusCode+" Not Found\r\n" +
                        "Content-Length: "+body.length() + "\r\n" +
                        "Location: "+location+"\r\n" +
                        "\r\n" +
                        body+"\r";
                klienten.getOutputStream().write(response.getBytes());
                return;
            }else{
                if(file.getName().endsWith(".html")){
                    contentType = "text/html";
                }else if(file.getName().endsWith(".css")){
                    contentType = "text/css";
                }
                response = "HTTP/1.1 "+statusCode+" Not Found\r\n" +
                        "Content-Length: "+file.length() + "\r\n" +
                        "Location: "+location+"\r\n" +
                        "Content-Type: "+contentType+"\r\n"+
                        "\r\n";
                klienten.getOutputStream().write(response.getBytes());
                new FileInputStream(file).transferTo(klienten.getOutputStream());
            }
        }


        if(queryStringFromClient.containsKey("status")){
            statusCode = queryStringFromClient.get("status");
        }
        if(queryStringFromClient.containsKey("Location")){
            location = queryStringFromClient.get("Location");
        }
        if(queryStringFromClient.containsKey("body")){
            body = queryStringFromClient.get("body");
        }
        if(location.contains("www.")){
            StringBuilder yo = new StringBuilder();
            yo.append(location, 0, 7);
            yo.append(location,11,location.length());
            location=yo.toString();
        }
        response = "HTTP/1.1 "+statusCode+" Not Found\r\n" +
                "Content-Length: "+body.length() + "\r\n" +
                "Location: "+location+"\r\n" +
                "\r\n" +
                body+"\r";
        klienten.getOutputStream().write(response.getBytes());
    }

    private void handlegetProducts(Socket klienten) throws IOException {
        String body = "<ul>";
        for (String productName : productNames) {
            body+="<li>"+productName+"</li>";
        }

        body+="</ul>";
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Length: "+body.length()+"\r\n" +
                "Content-Type: text/plain\r\n" +
                "\r\n" +
                body;
        klienten.getOutputStream().write(response.getBytes());
    }

    public int getActualPort() {
        return port;
    }

    public static void main(String[] args) throws IOException {
        HttpServer core = new HttpServer(8080);
        core.start();
    }

    public void setContentRoot(File contentRoot) {
        this.contentRoot = contentRoot;
    }

    public void setDocumentRoot(File documentRoot) {
        this.contentRoot = documentRoot;
    }

    public List<String> getProductNames() {
        return productNames;
    }

    private void queryString(String queryString, HashMap<String, String> queryStringPost) {
        if(queryString.contains("&")){
            String[] delerOpp = queryString.split("&");
            for(String s: delerOpp){
                String parameterName = s.split("=")[0];
                String parameterValue = s.split("=")[1];
                queryStringPost.put(parameterName, parameterValue);
            }
        }else if(queryString.contains("=")){
            String parameterName = queryString.split("=")[0];
            String parameterValue = queryString.split("=")[1];
            queryStringPost.put(parameterName, parameterValue);
        }
    }
}
