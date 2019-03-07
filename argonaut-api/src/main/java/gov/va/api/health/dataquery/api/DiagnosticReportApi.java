package gov.va.api.health.dataquery.api;

import gov.va.api.health.dataquery.api.resources.DiagnosticReport;
import gov.va.api.health.dataquery.api.resources.OperationOutcome;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

public interface DiagnosticReportApi {
  @Operation(
    summary = "Diagnostic Report Read",
    description =
        "https://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-diagnosticreport.html",
    tags = {"Diagnostic Report"}
  )
  @GET
  @Path("DiagnosticReport/{id}")
  @ApiResponse(
    responseCode = "200",
    description = "Record found",
    content =
        @Content(
          mediaType = "application/json+fhir",
          schema = @Schema(implementation = DiagnosticReport.class)
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
  DiagnosticReport diagnosticReportRead(
      @Parameter(in = ParameterIn.PATH, name = "id", required = true) String id);

  @Operation(
    summary = "Diagnostic Report Search",
    description =
        "https://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-diagnosticreport.html",
    tags = {"Diagnostic Report"}
  )
  @GET
  @Path("DiagnosticReport")
  @ApiResponse(
    responseCode = "200",
    description = "Record Found",
    content =
        @Content(
          mediaType = "application/json+fhir",
          schema = @Schema(implementation = DiagnosticReport.Bundle.class)
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
  DiagnosticReport.Bundle diagnosticReportSearch(
      @Parameter(in = ParameterIn.QUERY, required = true, name = "patient") String id,
      @Parameter(in = ParameterIn.QUERY, name = "page") @DefaultValue("1") int page,
      @Parameter(in = ParameterIn.QUERY, name = "_count") @DefaultValue("15") int count);
}
