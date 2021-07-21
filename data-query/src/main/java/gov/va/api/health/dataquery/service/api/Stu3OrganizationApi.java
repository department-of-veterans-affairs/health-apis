package gov.va.api.health.dataquery.service.api;

import gov.va.api.health.stu3.api.resources.OperationOutcome;
import gov.va.api.health.stu3.api.resources.Organization;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

public interface Stu3OrganizationApi {
  @Operation(
      summary = "Organization Read",
      description =
          "http://www.fhir.org/guides/argonaut/pd/StructureDefinition-argo-organization.html",
      tags = {"Organization"})
  @GET
  @Path("Organization/{id}")
  @ApiResponse(
      responseCode = "200",
      description = "Record found",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = Organization.class)))
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
  Organization organizationRead(
      @Parameter(in = ParameterIn.PATH, name = "id", required = true) String id);

  @Operation(
      summary = "Organization Search",
      description =
          "http://www.fhir.org/guides/argonaut/pd/StructureDefinition-argo-organization.html",
      tags = {"Organization"})
  @GET
  @Path("Organization")
  @ApiResponse(
      responseCode = "200",
      description = "Record found",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = Organization.Bundle.class)))
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
  Organization.Bundle organizationSearch(
      @Parameter(in = ParameterIn.QUERY, name = "identifier") String identifier,
      @Parameter(in = ParameterIn.QUERY, name = "address") String address,
      @Parameter(in = ParameterIn.QUERY, name = "name") String name,
      @Parameter(in = ParameterIn.QUERY, name = "page") @DefaultValue("1") int page,
      @Parameter(in = ParameterIn.QUERY, name = "_count") @DefaultValue("30") int count);
}
