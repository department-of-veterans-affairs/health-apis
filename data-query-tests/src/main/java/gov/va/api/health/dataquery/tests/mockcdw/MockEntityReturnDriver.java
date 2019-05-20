package gov.va.api.health.dataquery.tests.mockcdw;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.tests.mockcdw.MockResponses.MockResponsesBuilder;
import java.io.File;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;

/**
 * A very specialized JDBC driver that supports exactly one stored procedure.
 *
 * <p>THIS CLASS AND RELATED DRIVER CLASSES SUPPORT JUST ENOUGH OF THE JDBC API TO ALLOW THE MR.
 * ANDERSON APPLICATION TO FUNCTION.
 *
 * <p>Procedure calls are analyzed and matched to sample XML files stored on disk. This supports the
 * same signature as the production stored procedure, but the there is no logic behind this
 * implementation. Data must be defined in advanced.
 *
 * <pre>
 * {call [schema].[prc_Entity_Return](?,?,?,?,?,?,?)}
 *
 * Input Parameters:
 * 1 - Ignored
 * 2 - Ignored
 * 3 - Ignored
 * 4 - Count (records per page)
 * 5 - Page
 * 6 - Fhir string
 *
 * Output Parameter
 * 7 - XML payload
 * </pre>
 *
 * The index is YAML file in the following format:
 *
 * <pre>
 * entries:
 * - file: AllInt103i1000001782544.xml
 *   query: /AllergyIntolerance:1.03?identifier=1000001782544
 *   page: 1
 *   count: 15
 * - file: DiaRep102p185601V825290.xml
 *   query: /DiagnosticReport:1.02?patient=185601V825290
 *   page: 1
 *   count: 15
 * ...
 * </pre>
 *
 * Files are specified relative to the index.yaml file.
 */
@Slf4j
public class MockEntityReturnDriver implements Driver {
  private static final String URL_PREFIX = "jdbc:mockcdw://";

  static {
    try {
      MockEntityReturnDriver instance = new MockEntityReturnDriver();
      DriverManager.registerDriver(instance);
    } catch (SQLException e) {
      log.error("Failed to register driver", e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean acceptsURL(String url) {
    return url.startsWith(URL_PREFIX);
  }

  @Override
  public Connection connect(String url, Properties info) throws SQLException {
    if (!acceptsURL(url)) {
      return null;
    }

    MockResponsesBuilder responses = MockResponses.builder();
    for (String indexFile : url.replace(URL_PREFIX, "").split(",")) {
      File index = new File(indexFile);
      log.debug("Using index: {}", index);
      if (!index.exists()) {
        throw new SQLException("Mock CDW mapping file does not exist: " + index.getAbsolutePath());
      }
      try {
        MockEntries mockEntries =
            JacksonConfig.createMapper(new YAMLFactory()).readValue(index, MockEntries.class);
        responses.source(
            MockResponseSource.builder()
                .baseDirectory(index.getParentFile())
                .entries(mockEntries)
                .build());
      } catch (Exception e) {
        log.error("Failed create connection", e);
        throw new SQLException("Failed read mock entries: " + index.getAbsolutePath(), e);
      }
    }
    return MockEntityReturnConnection.of(responses.build());
  }

  @Override
  public int getMajorVersion() {
    return 1;
  }

  @Override
  public int getMinorVersion() {
    return 0;
  }

  @Override
  public Logger getParentLogger() {
    return Logger.getLogger(MockEntityReturnDriver.class.getName());
  }

  @Override
  public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
    return new DriverPropertyInfo[0];
  }

  @Override
  public boolean jdbcCompliant() {
    return false;
  }
}
