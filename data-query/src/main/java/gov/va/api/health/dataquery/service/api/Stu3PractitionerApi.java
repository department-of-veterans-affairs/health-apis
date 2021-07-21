package gov.va.api.health.dataquery.service.api;

import gov.va.api.health.stu3.api.resources.OperationOutcome;
import gov.va.api.health.stu3.api.resources.Practitioner;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

public interface Stu3PractitionerApi {
  @Operation(
      summary = "Practitioner Read",
      description =
          "http://www.fhir.org/guides/argonaut/pd/StructureDefinition-argo-practitioner.html",
      tags = {"Practitioner"})
  @GET
  @Path("Practitioner/{id}")
  @ApiResponse(
      responseCode = "200",
      description = "Record found",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = Practitioner.class)))
  @ApiResponse(
      responseCode = "404",
      description = "Not found",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class)))
  @ApiResponse(
      responseCode = "400",
      description = "Bad request",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class)))
  Practitioner practitionerRead(
      @Parameter(in = ParameterIn.PATH, name = "id", required = true) String id);

  @Operation(
      summary = "Practitioner Search",
      description =
          "http://www.fhir.org/guides/argonaut/pd/StructureDefinition-argo-practitioner.html",
      tags = {"Practitioner"})
  @GET
  @Path("Practitioner")
  @ApiResponse(
      responseCode = "200",
      description = "Record found",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = Practitioner.Bundle.class)))
  @ApiResponse(
      responseCode = "404",
      description = "Not found",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class)))
  @ApiResponse(
      responseCode = "400",
      description = "Bad request",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class)))
  Practitioner.Bundle practitionerSearch(
      @Parameter(in = ParameterIn.QUERY, name = "identifier") String identifier,
      @Parameter(in = ParameterIn.QUERY, name = "family") String family,
      @Parameter(in = ParameterIn.QUERY, name = "given") String given,
      @Parameter(in = ParameterIn.QUERY, name = "page") @DefaultValue("1") int page,
      @Parameter(in = ParameterIn.QUERY, name = "_count") @DefaultValue("30") int count);
}
