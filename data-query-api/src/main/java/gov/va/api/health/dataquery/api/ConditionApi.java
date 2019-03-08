package gov.va.api.health.dataquery.api;

import gov.va.api.health.dataquery.api.resources.Condition;
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

public interface ConditionApi {
  @Operation(
    summary = "Condition Read",
    description = "https://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-condition.html",
    tags = {"Condition"}
  )
  @GET
  @Path("Condition/{id}")
  @ApiResponse(
    responseCode = "200",
    description = "Record found",
    content =
        @Content(
          mediaType = "application/json+fhir",
          schema = @Schema(implementation = Condition.class)
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
  Condition conditionRead(
      @Parameter(in = ParameterIn.PATH, name = "id", required = true) String id);

  @Operation(
    summary = "Condition Search",
    description = "https://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-condition.html",
    tags = {"Condition"}
  )
  @GET
  @Path("Condition")
  @ApiResponse(
    responseCode = "200",
    description = "Record Found",
    content =
        @Content(
          mediaType = "application/json+fhir",
          schema = @Schema(implementation = Condition.Bundle.class)
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
  Condition.Bundle conditionSearch(
      @Parameter(in = ParameterIn.QUERY, required = true, name = "patient") String id,
      @Parameter(in = ParameterIn.QUERY, name = "page") @DefaultValue("1") int page,
      @Parameter(in = ParameterIn.QUERY, name = "_count") @DefaultValue("15") int count);
}
