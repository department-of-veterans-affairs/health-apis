package gov.va.api.health.dataquery.service.api;

import gov.va.api.health.argonaut.api.AllergyIntoleranceApi;
import gov.va.api.health.argonaut.api.ConditionApi;
import gov.va.api.health.argonaut.api.DiagnosticReportApi;
import gov.va.api.health.argonaut.api.ImmunizationApi;
import gov.va.api.health.argonaut.api.MedicationApi;
import gov.va.api.health.argonaut.api.MedicationOrderApi;
import gov.va.api.health.argonaut.api.MedicationStatementApi;
import gov.va.api.health.argonaut.api.ObservationApi;
import gov.va.api.health.argonaut.api.PatientApi;
import gov.va.api.health.argonaut.api.ProcedureApi;
import gov.va.api.health.dstu2.api.MetadataApi;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import javax.ws.rs.Path;

@OpenAPIDefinition(
    security =
        @SecurityRequirement(
            name = "OauthFlow",
            scopes = {
              "patient/AllergyIntolerance.read",
              "patient/Condition.read",
              "patient/DiagnosticReport.read",
              "patient/Immunization.read",
              "patient/Medication.read",
              "patient/MedicationOrder.read",
              "patient/MedicationStatement.read",
              "patient/Observation.read",
              "patient/Patient.read",
              "patient/Procedure.read",
              "offline_access",
              "launch/patient"
            }),
    info =
        @Info(
            title = "Argonaut Data Query",
            version = "v1",
            description =
                " This service is compliant with the FHIR Argonaut Data Query Implementation"
                    + " Guide. This service does not provide or replace the consultation, guidance,"
                    + " or care of a health care professional or other qualified provider."
                    + " This service provides a supplement for informational and educational"
                    + " purposes only. Health care professionals and other qualified providers"
                    + " should continue to consult authoritative records when making decisions."),
    servers = {
      @Server(
          url = "https://sandbox-api.va.gov/services/fhir/v0/argonaut/data-query/",
          description = "Development server")
    },
    externalDocs =
        @ExternalDocumentation(
            description = "Argonaut Data Query Implementation Guide",
            url = "http://www.fhir.org/guides/argonaut/r2/index.html"))
@SecurityScheme(
    name = "OauthFlow",
    type = SecuritySchemeType.OAUTH2,
    in = SecuritySchemeIn.HEADER,
    flows =
        @OAuthFlows(
            implicit =
                @OAuthFlow(
                    authorizationUrl = "https://sandbox-api.va.gov/oauth2/authorization",
                    tokenUrl = "https://sandbox-api.va.gov/services/fhir/v0/dstu2/token",
                    scopes = {
                      @OAuthScope(
                          name = "patient/AllergyIntolerance.read",
                          description = "read allergy intolerances"),
                      @OAuthScope(name = "patient/Condition.read", description = "read conditions"),
                      @OAuthScope(
                          name = "patient/DiagnosticReport.read",
                          description = "read diagnostic reports"),
                      @OAuthScope(
                          name = "patient/Immunization.read",
                          description = "read immunizations"),
                      @OAuthScope(
                          name = "patient/Medication.read",
                          description = "read medications"),
                      @OAuthScope(
                          name = "patient/MedicationOrder.read",
                          description = "read medication orders"),
                      @OAuthScope(
                          name = "patient/MedicationStatement.read",
                          description = "read medication statements"),
                      @OAuthScope(
                          name = "patient/Observation.read",
                          description = "read observations"),
                      @OAuthScope(name = "patient/Patient.read", description = "read patient"),
                      @OAuthScope(name = "patient/Procedure.read", description = "read procedures"),
                      @OAuthScope(name = "offline_access", description = "offline access"),
                      @OAuthScope(name = "launch/patient", description = "patient launch"),
                    })))
@Path("/")
public interface DataQueryService
    extends AllergyIntoleranceApi,
        ConditionApi,
        DiagnosticReportApi,
        ImmunizationApi,
        MedicationOrderApi,
        MedicationApi,
        MedicationStatementApi,
        MetadataApi,
        ObservationApi,
        PatientApi,
        ProcedureApi {
  class DataQueryServiceException extends RuntimeException {
    DataQueryServiceException(String message) {
      super(message);
    }
  }

  class SearchFailed extends DataQueryServiceException {
    @SuppressWarnings("WeakerAccess")
    public SearchFailed(String id, String reason) {
      super(id + " Reason: " + reason);
    }
  }

  class UnknownResource extends DataQueryServiceException {
    @SuppressWarnings("WeakerAccess")
    public UnknownResource(String id) {
      super(id);
    }
  }
}
