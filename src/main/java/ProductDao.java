import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ProductDao {
    private DataSource datasource;
    private List<String> products = new ArrayList<>();

    public ProductDao(DataSource dataSource) {
        this.datasource = dataSource;
    }

    public void insert(String product) throws SQLException {
        products.add(product);
        try (Connection connection = datasource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO products (product_name) VALUES(?)")) {
                statement.setString(1,product); // for å forhindre sql injections
                statement.executeUpdate();
            }
        }
    }

    public List<String> list() {
        return products;
    }
    public static void main(String[] args) throws SQLException {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl("jdbc:postgresql://localhost:5432/kristianiashop");
        dataSource.setUser("kristianiashop");
        dataSource.setPassword("1nirgrefoo");

        System.out.println("whats the name of the new product?");
        Scanner scanner = new Scanner(System.in);

        String productName=scanner.nextLine();




        try(Connection connection = dataSource.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement("SELECT * FROM products")){
                try(ResultSet rs = statement.executeQuery()){
                    while (rs.next()){
                        System.out.println(rs.getString("product_name"));
                    }
                }
            }
        }
    }


}
