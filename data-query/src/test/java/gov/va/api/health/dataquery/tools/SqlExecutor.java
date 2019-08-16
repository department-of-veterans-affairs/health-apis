package gov.va.api.health.dataquery.tools;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.Properties;

public class SqlExecutor {

  static long start = currentTimeMillis();

  public static void main(String[] args) throws Exception {
    Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    out.println("Usage: <properties> <file-with-query>");
    var properties = new Properties(System.getProperties());
    properties.load(new FileInputStream(args[0]));
    var url = properties.getProperty("spring.datasource.url");
    var username = properties.getProperty("spring.datasource.username");
    var password = properties.getProperty("spring.datasource.password");
    var sql = Files.readString(Path.of(args[1]));
    mark("loaded");
    var conn = DriverManager.getConnection(url, username, password);
    mark("connected");
    var statement = conn.prepareStatement(sql);
    statement.setString(1, args[2]);
    mark("create statement");
    var resultSet = statement.executeQuery();
    var elapsed = mark("execute query");
    var columns = resultSet.getMetaData().getColumnCount();
    while (resultSet.next()) {
      for (int i = 1; i <= columns; ++i) {
        out.println("COLUMN " + i);
        out.println(resultSet.getObject(i));
      }
    }
    mark("done");
    out.println("Query took " + elapsed + " ms");
  }

  static long mark(String m) {
    var now = currentTimeMillis();
    var elapsed = now - start;
    out.println(m + " " + elapsed + " ms");
    start = now;
    return elapsed;
  }
}
