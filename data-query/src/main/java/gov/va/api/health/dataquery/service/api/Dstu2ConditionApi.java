package gov.va.api.health.dataquery.service.api;

import gov.va.api.health.dstu2.api.resources.Condition;
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

public interface Dstu2ConditionApi {
  @Operation(
      summary = "Condition Read",
      description =
          "https://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-condition.html",
      tags = {"Condition"})
  @GET
  @Path("Condition/{id}")
  @ApiResponse(
      responseCode = "200",
      description = "Record found",
      content =
          @Content(
              mediaType = "application/json+fhir",
              schema = @Schema(implementation = Condition.class)))
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
  Condition conditionRead(
      @Parameter(
              in = ParameterIn.PATH,
              name = "id",
              required = true,
              description =
                  "The logical id of the resource. Once assigned, this value never changes.")
          String id);

  @Operation(
      summary = "Condition Search",
      description =
          "https://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-condition.html",
      tags = {"Condition"})
  @GET
  @Path("Condition")
  @ApiResponse(
      responseCode = "200",
      description = "Record Found",
      content =
          @Content(
              mediaType = "application/json+fhir",
              schema = @Schema(implementation = Condition.Bundle.class)))
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
  Condition.Bundle conditionSearch(
      @Parameter(
              in = ParameterIn.QUERY,
              name = "patient",
              description =
                  " The Integration Control Number (ICN) assigned by the Master Patient Index (MPI)"
                      + " that indicates the patient who the condition record is associated with.")
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
                  "The category the condition record belongs to. Can be used to "
                      + "distinguish between health concerns and problems. "
                      + "[Argonaut Condition Category Codes](https://www.fhir.org/guides/argonaut/r2/ValueSet-condition-category.html)")
          String category,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "clinicalstatus",
              description =
                  "Indicates the clinical state of the condition described by "
                      + "the record, taking prior conditions into account."
                      + "[Condition Clinical Status Codes](http://hl7.org/fhir/DSTU2/valueset-condition-clinical.html)")
          String clinicalstatus,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "page",
              description = "The page number being requested.")
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
