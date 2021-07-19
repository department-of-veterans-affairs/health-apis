package gov.va.api.health.dataquery.service.api;

import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.r4.api.resources.Patient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

public interface R4PatientApi {
  @Operation(
      summary = "Patient Read",
      description =
          "https://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-patient.html",
      tags = {"Patient"})
  @GET
  @Path("Patient/{id}")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Record found",
        content =
            @Content(
                mediaType = "application/fhir+json",
                schema = @Schema(implementation = Patient.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Bad request",
        content =
            @Content(
                mediaType = "application/fhir+json",
                schema = @Schema(implementation = OperationOutcome.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = {
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class))
        }),
    @ApiResponse(
        responseCode = "403",
        description = "Forbidden",
        content = {
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class))
        }),
    @ApiResponse(
        responseCode = "404",
        description = "Not found",
        content =
            @Content(
                mediaType = "application/fhir+json",
                schema = @Schema(implementation = OperationOutcome.class))),
    @ApiResponse(
        responseCode = "500",
        description = "Server Error",
        content = {
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class))
        })
  })
  Patient patientRead(
      @Parameter(
              in = ParameterIn.PATH,
              name = "id",
              required = true,
              description =
                  "The logical id of the resource."
                      + " Once assigned, this value never changes."
                      + " For Patients this id is an Integration Control Number (ICN)"
                      + " assigned by the Master Patient Index (MPI).",
              example = "1011537977V693883")
          String id);

  @Operation(
      summary = "Patient Search",
      description =
          "https://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-patient.html",
      tags = {"Patient"})
  @GET
  @Path("Patient")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Record found",
        content =
            @Content(
                mediaType = "application/fhir+json",
                schema = @Schema(implementation = Patient.Bundle.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Bad request",
        content =
            @Content(
                mediaType = "application/fhir+json",
                schema = @Schema(implementation = OperationOutcome.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = {
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class))
        }),
    @ApiResponse(
        responseCode = "403",
        description = "Forbidden",
        content = {
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class))
        }),
    @ApiResponse(
        responseCode = "404",
        description = "Not found",
        content =
            @Content(
                mediaType = "application/fhir+json",
                schema = @Schema(implementation = OperationOutcome.class))),
    @ApiResponse(
        responseCode = "500",
        description = "Server Error",
        content = {
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class))
        })
  })
  Patient.Bundle patientSearch(
      @Parameter(
              in = ParameterIn.QUERY,
              name = "_id",
              description =
                  "The logical id of the resource."
                      + " Once assigned, this value never changes."
                      + " For Patients this id is an Integration Control Number (ICN)"
                      + " assigned by the Master Patient Index (MPI).",
              example = "1011537977V693883")
          String id,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "identifier",
              description =
                  "The logical identifier of the resource."
                      + " Once assigned, this value never changes."
                      + " For Patients this identifier is an Integration Control Number (ICN)"
                      + " assigned by the Master Patient Index (MPI).",
              example = "1011537977V693883")
          String identifier,
      // Search by Name, Gender, and BirthDate are available but not as part of the patient flow
      @Parameter(
              in = ParameterIn.QUERY,
              name = "page",
              description = "The page number of the search result.",
              example = "1")
          @DefaultValue("1")
          int page,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "_count",
              description =
                  "The number of resources that should be returned in a single page."
                      + " The maximum count size is 100.",
              example = "30")
          @DefaultValue("30")
          int count);
}
