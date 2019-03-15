package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * The `prc_Entity_Return` stored procedure implementation of the resource repository. No error
 * checking is performed.
 */
@Slf4j
@Component
public class DiagnosticReportSql {
  private final JdbcTemplate jdbc;

  /**
   * Create a new instance with a configurable schema and stored procedure name from
   * application.properties.
   */
  @Autowired
  public DiagnosticReportSql(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public void execute() {
    log.error("data-query sql test");
    jdbc.query(
        "SELECT * FROM app.DiagnosticReport",
        (rs, rowNum) -> {
          log.error("data-query sql test, row " + rowNum);
          return null;
        });

    //    try (Connection connection = jdbc.getDataSource().getConnection()) {
    //      try (CallableStatement cs = connection.prepareCall()) {
    //        //cs.closeOnCompletion();
    //        //        cs.setObject(Index.FHIR_VERSION, FhirVersion.of(query.profile()),
    // Types.TINYINT);
    //        //        cs.setObject(Index.RETURN_TYPE, ReturnType.FULL, Types.TINYINT);
    //        //        cs.setObject(Index.FORMAT, Format.XML, Types.TINYINT);
    //        //        cs.setObject(Index.RECORDS_PER_PAGE, query.count(), Types.INTEGER);
    //        //        cs.setObject(Index.PAGE, query.page(), Types.INTEGER);
    //        //        cs.setObject(Index.FHIR_STRING, query.toQueryString(), Types.VARCHAR);
    //        //        cs.registerOutParameter(Index.RESPONSE_XML, Types.CLOB);
    //        cs.executeUpdate();
    //        Clob clob = (Clob) cs.getObject(Index.RESPONSE_XML);
    //        return clob.getSubString(1, (int) clob.length());
    //      }
    //    } catch (SQLException e) {
    //      //throw new SearchFailed(query, e);
    //    	throw e;
    //    }
  }
}
