package gov.va.api.health.dataquery.service.api;

import gov.va.api.health.dstu2.api.resources.DiagnosticReport;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

public interface Dstu2DiagnosticReportApi {
  @Operation(
      summary = "Diagnostic Report Read",
      description =
          "https://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-diagnosticreport.html",
      tags = {"Diagnostic Report"})
  @GET
  @Path("DiagnosticReport/{id}")
  @ApiResponse(
      responseCode = "200",
      description = "Record found",
      content =
          @Content(
              mediaType = "application/json+fhir",
              schema = @Schema(implementation = DiagnosticReport.class)))
  @ApiResponse(
      responseCode = "400",
      description = "Bad request",
      content =
          @Content(
              mediaType = "application/json+fhir",
              schema = @Schema(implementation = OperationOutcome.class)))
  @ApiResponse(
      responseCode = "404",
      description = "Not found",
      content =
          @Content(
              mediaType = "application/json+fhir",
              schema = @Schema(implementation = OperationOutcome.class)))
  DiagnosticReport diagnosticReportRead(
      @Parameter(
              in = ParameterIn.PATH,
              name = "id",
              required = true,
              description =
                  "The logical id of the resource. Once assigned, this value never changes.")
          String id);

  @Operation(
      summary = "Diagnostic Report Search",
      description =
          "https://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-diagnosticreport.html",
      tags = {"Diagnostic Report"})
  @GET
  @Path("DiagnosticReport")
  @ApiResponse(
      responseCode = "200",
      description = "Record Found",
      content =
          @Content(
              mediaType = "application/json+fhir",
              schema = @Schema(implementation = DiagnosticReport.Bundle.class)))
  @ApiResponse(
      responseCode = "400",
      description = "Bad request",
      content =
          @Content(
              mediaType = "application/json+fhir",
              schema = @Schema(implementation = OperationOutcome.class)))
  @ApiResponse(
      responseCode = "404",
      description = "Not found",
      content =
          @Content(
              mediaType = "application/json+fhir",
              schema = @Schema(implementation = OperationOutcome.class)))
  DiagnosticReport.Bundle diagnosticReportSearch(
      @Parameter(
              in = ParameterIn.QUERY,
              name = "patient",
              description =
                  "The Integration Control Number (ICN) assigned by the Master Patient Index (MPI)"
                      + " that indicates the patient who the record is associated with.")
          String patient,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "_id",
              description =
                  "The logical id of the resource. Once assigned, this value never changes.")
          String id,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "identifier",
              description =
                  "The logical identifier of the resource. Once assigned, this value "
                      + "never changes.")
          String identifier,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "category",
              description =
                  "The category classifies the clinical discipline, department "
                      + "or diagnostic service that created the report. "
                      + "[Diagnostic Service Section Codes](http://hl7.org/fhir/DSTU2/valueset-diagnostic-service-sections.html)")
          String category,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "date",
              description =
                  "A date or range of dates (maximum of 2) that describe "
                      + "the date that the diagnostic report was recorded.")
          String[] date,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "code",
              description =
                  "A code that indicates the type of information contained within "
                      + "the diagnostic report. [LOINC Diagnostic Report Codes](http://hl7.org/fhir/dstu2/valueset-report-codes.html)")
          String code,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "page",
              description = "The page number of the search result.")
          @DefaultValue("1")
          int page,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "_count",
              description =
                  "The number of resources that should be returned in a single page."
                      + " The maximum count size is 100.")
          @DefaultValue("30")
          int count);
}
