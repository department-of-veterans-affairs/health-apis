package gov.va.api.health.dataquery.api;

import gov.va.api.health.dataquery.api.resources.Medication;
import gov.va.api.health.dataquery.api.resources.OperationOutcome;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

public interface MedicationApi {
  @Operation(
    summary = "Medication Read",
    description = "http://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-medication.html",
    tags = {"Medication"}
  )
  @GET
  @Path("Medication/{id}")
  @ApiResponse(
    responseCode = "200",
    description = "Record found",
    content =
        @Content(
          mediaType = "application/json+fhir",
          schema = @Schema(implementation = Medication.class)
        )
  )
  @ApiResponse(
    responseCode = "400",
    description = "Not found",
    content =
        @Content(
          mediaType = "application/json+fhir",
          schema = @Schema(implementation = OperationOutcome.class)
        )
  )
  @ApiResponse(
    responseCode = "404",
    description = "Bad request",
    content =
        @Content(
          mediaType = "application/json+fhir",
          schema = @Schema(implementation = OperationOutcome.class)
        )
  )
  Medication medicationRead(
      @Parameter(in = ParameterIn.PATH, name = "id", required = true) String id);
}
