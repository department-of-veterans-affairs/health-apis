package gov.va.api.health.mranderson.cdw.impl;

import gov.va.api.health.mranderson.cdw.Profile;
import gov.va.api.health.mranderson.cdw.Query;
import gov.va.api.health.mranderson.cdw.ResourceRepository;
import gov.va.api.health.mranderson.cdw.Resources.SearchFailed;
import gov.va.api.health.mranderson.util.Checks;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * The `prc_Entity_Return` stored procedure implementation of the resource repository.  No error
 * checking is performed.
 */
@Component
public class SqlResourceRepository implements ResourceRepository {

  private final JdbcTemplate jdbc;

  private final String schema;

  private final String storedProcedure;

  /**
   * Create a new instance with a configurable schema and stored procedure name from
   * application.properties.
   */
  @Autowired
  public SqlResourceRepository(
      JdbcTemplate jdbc,
      @Value("${cdw.schema:App}") String schema,
      @Value("${cdw.stored-procedure:prc_Entity_Return}") String storedProcedure) {
    this.jdbc = jdbc;
    this.schema = Checks.argumentMatches(schema, "[A-Za-z0-9_]+");
    this.storedProcedure = storedProcedure;
  }

  @Override
  public String execute(Query query) {
    try (Connection connection = Checks.notNull(jdbc.getDataSource()).getConnection()) {
      try (CallableStatement cs = connection.prepareCall(storedProcedureCallSql())) {
        cs.closeOnCompletion();
        cs.setObject(Index.FHIR_VERSION, FhirVersion.of(query.profile()), Types.TINYINT);
        cs.setObject(Index.RETURN_TYPE, ReturnType.FULL, Types.TINYINT);
        cs.setObject(Index.FORMAT, Format.XML, Types.TINYINT);
        cs.setObject(Index.RECORDS_PER_PAGE, query.count(), Types.INTEGER);
        cs.setObject(Index.PAGE, query.page(), Types.INTEGER);
        cs.setObject(Index.FHIR_STRING, query.toQueryString(), Types.VARCHAR);
        cs.registerOutParameter(Index.RESPONSE_XML, Types.CLOB);
        cs.executeUpdate();
        Clob clob = (Clob) cs.getObject(Index.RESPONSE_XML);
        return clob.getSubString(1, (int) clob.length());
      }
    } catch (SQLException e) {
      throw new SearchFailed(query, e);
    }
  }

  private String storedProcedureCallSql() {
    /*
     * String concatenation is safe because the schema value is verified to contain only letters,
     * numbers, and underscores and does not allow any SQL sensitive characters that could enable
     * SQL injection.
     */
    return "{call [" + schema + "].[" + storedProcedure + "](?,?,?,?,?,?,?)}";
  }

  interface FhirVersion {

    int ARGONAUT = 1;

    int DSTU2 = 2;

    int STU3 = 3;

    static int of(Profile profile) {
      switch (profile) {
        case ARGONAUT:
          return ARGONAUT;
        case STU3:
          return STU3;
        default:
          return 2;
      }
    }
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

    int COUNT = 1;

    int SUMMARY = 2;

    int FULL = 3;
  }

  interface Format {

    int XML = 1;

    int JSON = 2;
  }
}
