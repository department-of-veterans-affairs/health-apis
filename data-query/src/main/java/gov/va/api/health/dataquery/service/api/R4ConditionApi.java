package gov.va.api.health.dataquery.service.api;

import gov.va.api.health.r4.api.resources.Condition;
import gov.va.api.health.r4.api.resources.OperationOutcome;
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

public interface R4ConditionApi {

  @Operation(
      summary = "Condition Read",
      description =
          "https://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-condition.html",
      tags = {"Condition"})
  @GET
  @Path("Condition/{id}")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Record found",
        content =
            @Content(
                mediaType = "application/fhir+json",
                schema = @Schema(implementation = Condition.class))),
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
  Condition conditionRead(
      @Parameter(
              in = ParameterIn.PATH,
              name = "id",
              required = true,
              description =
                  "The logical id of the resource. Once assigned, this value never changes.",
              example = "I2-FOBJ7YQOH3RIQ5UZ6TRM32ZSQA000000")
          String id);

  @Operation(
      summary = "Condition Search",
      description =
          "https://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-condition.html",
      tags = {"Condition"})
  @GET
  @Path("Condition")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Record found",
        content =
            @Content(
                mediaType = "application/fhir+json",
                schema = @Schema(implementation = Condition.Bundle.class))),
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
  Condition.Bundle conditionSearch(
      @Parameter(
              in = ParameterIn.QUERY,
              name = "patient",
              description =
                  " The Integration Control Number (ICN) assigned by the Master Patient Index (MPI)"
                      + " that indicates the patient who the condition record is associated with.",
              example = "1011537977V693883")
          String patient,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "_id",
              description =
                  "The logical id of the resource. Once assigned, this value never changes.",
              example = "I2-FOBJ7YQOH3RIQ5UZ6TRM32ZSQA000000")
          String id,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "identifier",
              description =
                  "The logical identifier of the resource. Once assigned, this value "
                      + "never changes.",
              example = "I2-FOBJ7YQOH3RIQ5UZ6TRM32ZSQA000000")
          String identifier,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "category",
              description =
                  "The category the condition record belongs to. Can be used to "
                      + "distinguish between health concerns and problems. "
                      + "[Condition Category Codes](https://www.hl7.org/fhir/valueset-condition-category.html)",
              example =
                  "http://terminology.hl7.org/CodeSystem/condition-category|problem-list-item")
          String category,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "clinical-status",
              description =
                  "Indicates the clinical state of the condition described by "
                      + "the record, taking prior conditions into account."
                      + "[Condition Clinical Status Codes](https://hl7.org/fhir/r4/valueset-condition-clinical.html)",
              example = "http://terminology.hl7.org/CodeSystem/condition-clinical|active,resolved")
          String clinicalStatus,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "page",
              description = "The page number being requested.",
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
