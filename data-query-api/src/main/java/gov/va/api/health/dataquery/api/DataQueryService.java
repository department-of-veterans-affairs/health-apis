package gov.va.api.health.dataquery.api;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import javax.ws.rs.Path;

@OpenAPIDefinition(
  info =
      @Info(
        title = "Argonaut",
        version = "v1",
        description =
            "FHIR (Fast Healthcare Interoperability Resources) specification defines a set of"
                + " \"Resources\" that represent granular clinical concepts."
                + " This service is compliant with the FHIR Argonaut Data Query Implementation"
                + " Guide."
      ),
  servers = {
    @Server(
      url = "https://dev-api.va.gov/services/argonaut/v0/",
      description = "Development server"
    )
  },
  externalDocs =
      @ExternalDocumentation(
        description = "Argonaut Data Query Implementation Guide",
        url = "http://www.fhir.org/guides/argonaut/r2/index.html"
      )
)
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
