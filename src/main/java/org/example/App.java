package org.example;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        try {
            Properties props = loadConfig();
            if (props == null) return;

            Class.forName("org.h2.Driver");

            try (Connection conn = DriverManager.getConnection(
                    props.getProperty("jdbc.url"),
                    props.getProperty("jdbc.username"),
                    props.getProperty("jdbc.password"));
                 Scanner scanner = new Scanner(System.in)) {

                System.out.println("Подключение установлено. Введите SQL (QUIT для выхода):");

                while (true) {
                    System.out.print("> ");
                    String sql = scanner.nextLine().trim();

                    if ("QUIT".equalsIgnoreCase(sql)) {
                        System.out.println("Завершение работы...");
                        break;
                    }

                    if (sql.isEmpty()) continue;

                    executeSQL(conn, sql);
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    private static Properties loadConfig() {
        Properties props = new Properties();
        try (InputStream input = App.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.err.println("Файл config.properties не найден!");
                System.err.println("Поместите его в src/main/resources/");
                return null;
            }
            props.load(input);
            return props;
        } catch (Exception e) {
            System.err.println("Ошибка загрузки конфигурации: " + e.getMessage());
            return null;
        }
    }

    private static void executeSQL(Connection conn, String sql) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            boolean isResultSet = stmt.execute(sql);

            if (isResultSet) {
                printResultSet(stmt.getResultSet());
            } else {
                System.out.println("Выполнено. Затронуто строк: " + stmt.getUpdateCount());
            }
        }
    }

    private static void printResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();
        int rowCount = 0;

        for (int i = 1; i <= colCount; i++) {
            System.out.printf("%-20s", meta.getColumnName(i));
        }
        System.out.println();

        while (rs.next() && rowCount < 10) {
            for (int i = 1; i <= colCount; i++) {
                System.out.printf("%-20s", rs.getString(i));
            }
            System.out.println();
            rowCount++;
        }

        if (rs.next()) {
            System.out.println("\nВ БД есть еще записи...");
        }
    }
}