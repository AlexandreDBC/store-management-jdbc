package bdd;

import java.sql.*;

public class JDBC {
    public Connection connection;

    public JDBC() throws SQLException {
        this.registerDriver();
        this.connect();
        if (connection == null) {
            throw new SQLException("Failed to initiate connection.");
        }
    }

    private void registerDriver() {
        try {
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Driver Registered!");
    }

    private void connect() {
        Connection connection = null;
        System.out.println("Connecting to database...");
        try {
            // TODO: Mettre ça dans des variables d'environnement
            String url = "jdbc:oracle:thin:@oracle1.ensimag.fr:1521:oracle1";
            String user = "berthnat";
            String password = "berthnat";
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (connection != null) {
            System.out.println("Connected to the database!");
            this.connection = connection;
        } else {
            System.out.println("Connection Failed! Check output console");
        }
    }

    public ResultSet ask(String query) {
        ResultSet resultSet = null;
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            resultSet = statement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultSet;
    }

    public void close() throws SQLException {
        connection.close();
    }
}