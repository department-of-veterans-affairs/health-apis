package gov.va.api.health.dataquery.tests.mockcdw;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.sentinel.categories.Local;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Types;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RegExUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Local.class)
public class MockEntityReturnDriverTest {
  @Test
  @SneakyThrows
  public void driverCanExecutePrcEntityReturnCalls() {
    String response1 = executeQuery(1, 2, "/Whatever:1.00?id=123");
    assertThat(response1).isNotBlank();
    assertThat(RegExUtils.removeAll(response1, "\\s"))
        .isEqualTo("<?xmlversion=\"1.0\"encoding=\"UTF-8\"?><root><whatever>123</whatever></root>");

    String response2 = executeQuery(2, 3, "/Whatever:1.01?stuff=neat");
    assertThat(response2).isNotBlank();
    assertThat(RegExUtils.removeAll(response2, "\\s"))
        .isEqualTo("<?xmlversion=\"1.0\"encoding=\"UTF-8\"?><root><stuff>neat</stuff></root>");
  }

  /**
   * This method mimic the behavior used by Mr. Anderson to interact with the database. This allows
   * us to test the mock driver in isolation.
   */
  @SneakyThrows
  private String executeQuery(int page, int count, String query) {
    Class.forName(MockEntityReturnDriver.class.getName());
    Connection connection =
        DriverManager.getConnection(
            "jdbc:mockcdw://src/test/resources/cdw/index.yaml,"
                + "src/test/resources/gov/va/api/health/dataquery/tests/mockcdw/mockcdw/mock-cdw-test.yaml");
    assertThat(connection).isNotNull();
    CallableStatement cs =
        connection.prepareCall("{call [whatever].[prc_Entity_Return](?,?,?,?,?,?,?)}");
    cs.setObject(Index.FHIR_VERSION, FhirVersion.ARGONAUT, Types.TINYINT);
    cs.setObject(Index.RETURN_TYPE, ReturnType.FULL, Types.TINYINT);
    cs.setObject(Index.FORMAT, Format.XML, Types.TINYINT);
    cs.setObject(Index.RECORDS_PER_PAGE, count, Types.INTEGER);
    cs.setObject(Index.PAGE, page, Types.INTEGER);
    cs.setObject(Index.FHIR_STRING, query, Types.VARCHAR);
    cs.registerOutParameter(Index.RESPONSE_XML, Types.CLOB);
    cs.executeUpdate();
    Clob clob = (Clob) cs.getObject(Index.RESPONSE_XML);
    return clob.getSubString(1, (int) clob.length());
  }

  interface FhirVersion {
    int ARGONAUT = 1;
  }

  interface Index {
    int FHIR_VERSION = 1;
    int RETURN_TYPE = 2;
    int FORMAT = 3;
    int RECORDS_PER_PAGE = 4;
    int PAGE = 5;
    int FHIR_STRING = 6;
    int RESPONSE_XML = 7;
  }

  interface ReturnType {
    int FULL = 3;
  }

  @SuppressWarnings("unused")
  interface Format {
    int XML = 1;
    int JSON = 2;
  }
}
