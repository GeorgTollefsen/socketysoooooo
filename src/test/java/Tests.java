import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class Tests {

    @Test
    void shouldReadResponseCode() throws IOException {
        HttpServer server = new HttpServer(10001);
        server.start();
        int port = server.getActualPort();
        HttpClient client = new HttpClient("localhost", port, "/echo?test=super");
        HttpResponse response = client.executeRequest();
        assertEquals("200", response.getResponseCode());
    }
    @Test
    void shouldParseRequestParameters() throws IOException {
        HttpServer server = new HttpServer(10002);
        server.start();
        int port = server.getActualPort();
        HttpClient client = new HttpClient("localhost", port, "?status=401");
        HttpResponse response = client.executeRequest();
        assertEquals("401", response.getResponseCode());
    }
    @Test
    void shouldRemoveWWW() throws IOException {
        HttpServer server = new HttpServer(10003);
        server.start();
        int port = server.getActualPort();
        HttpClient client = new HttpClient("localhost", port, "?status=302&Location=http://www.example.com");
        HttpResponse response = client.executeRequest();
        assertEquals("http://example.com", response.getHeader("Location"));
    }

    @Test
    void shouldParseBody() throws IOException {
        HttpServer server = new HttpServer(10004);
        server.start();
        int port = server.getActualPort();
        HttpClient client = new HttpClient("localhost", port, "?body=HelloWorld");
        HttpResponse response = client.executeRequest();
        assertEquals("HelloWorld", response.getBody());
    }
    @Test
    void shouldGetContentLength() throws IOException {
        HttpServer server = new HttpServer(10005);
        server.start();
        int port = server.getActualPort();
        HttpClient client = new HttpClient("localhost", port, "?body=HelloWorld");
        HttpResponse response = client.executeRequest();
        assertEquals("10", response.getHeader("Content-Length"));
    }
    @Test
    void shouldReturnFileFromDisk() throws IOException {
        HttpServer server = new HttpServer(10006);
        File contentRoot = new File("target/");
        server.setContentRoot(contentRoot);
        String fileContent = "Hello World " + new Date();
        Files.writeString(new File(contentRoot, "test.txt").toPath(), fileContent);
        server.start();
        int port = server.getActualPort();
        HttpClient client = new HttpClient("localhost", port, "/test.txt");
        HttpResponse response = client.executeRequest();
        assertEquals(fileContent, client.getBody());
    }

    @Test
    void shouldReturnCorrectContentType() throws IOException {
        HttpServer server = new HttpServer(10007);
        server.start();
        int port = server.getActualPort();
        HttpClient client = new HttpClient("localhost",port,"/index.html");
        HttpResponse response = client.executeRequest();
        assertEquals("text/html", response.getHeader("Content-Type"));
    }
    @Test
    void shouldCreateNewProduct() throws IOException {
        HttpServer server = new HttpServer(10008);
        server.start();
        HttpClient client = new HttpClient("localhost", 10008,"/api/newProduct", "POST", "productName=apples&price=10");
        HttpResponse response = client.executeRequest();
        assertEquals("200", response.getResponseCode());
        assertEquals(List.of("apples"), server.getProductNames());
    }
    @Test
    void shouldReturnExitsingProducts() throws IOException {
        HttpServer server = new HttpServer(10009);
        server.start();
        server.getProductNames().add("Coconuts");
        HttpClient client = new HttpClient("localhost",10009,"/api/products");
        HttpResponse response = client.executeRequest();
        assertEquals("<ul><li>Coconuts</li></ul>", response.getBody());
    }

    @Test
    void shouldListInsertedProducts() throws SQLException {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:test");
     ProductDao productDao = new ProductDao(dataSource);
     String product = sampleProductName();
     productDao.insert(product);
     assertThat(productDao.list()).contains(product);

    }

    private String sampleProductName() {
        String[] options = {"Apples", "Bananas", "Dates", "Coconuts"};
        Random random = new Random();
        return options[random.nextInt(options.length)];
    }
}
