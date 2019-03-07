package gov.va.api.health.dataquery.api;

import gov.va.api.health.dataquery.api.resources.AllergyIntolerance;
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

public interface AllergyIntoleranceApi {
  @Operation(
    summary = "Allergy Intolerance Read",
    description =
        "http://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-allergyintolerance.html",
    tags = {"Allergy Intolerance"}
  )
  @GET
  @Path("AllergyIntolerance/{id}")
  @ApiResponse(
    responseCode = "200",
    description = "Record found",
    content =
        @Content(
          mediaType = "application/json+fhir",
          schema = @Schema(implementation = AllergyIntolerance.class)
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
  AllergyIntolerance allergyIntoleranceRead(
      @Parameter(in = ParameterIn.PATH, name = "id", required = true) String id);

  @Operation(
    summary = "Allergy Intolerance Search",
    description =
        "http://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-allergyintolerance.html",
    tags = {"Allergy Intolerance"}
  )
  @GET
  @Path("AllergyIntolerance")
  @ApiResponse(
    responseCode = "200",
    description = "Record found",
    content =
        @Content(
          mediaType = "application/json+fhir",
          schema = @Schema(implementation = AllergyIntolerance.Bundle.class)
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
  AllergyIntolerance.Bundle allergyIntoleranceSearch(
      @Parameter(in = ParameterIn.QUERY, required = true, name = "patient") String id,
      @Parameter(in = ParameterIn.QUERY, name = "page") @DefaultValue("1") int page,
      @Parameter(in = ParameterIn.QUERY, name = "_count") @DefaultValue("15") int count);
}
