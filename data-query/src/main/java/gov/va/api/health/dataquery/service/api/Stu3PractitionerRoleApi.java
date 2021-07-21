package gov.va.api.health.dataquery.service.api;

import gov.va.api.health.stu3.api.resources.OperationOutcome;
import gov.va.api.health.stu3.api.resources.PractitionerRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

public interface Stu3PractitionerRoleApi {
  @Operation(
      summary = "Practitioner Role Read",
      description =
          "http://www.fhir.org/guides/argonaut/pd/StructureDefinition-argo-practitionerrole.html",
      tags = {"Practitioner Role"})
  @GET
  @Path("PractitionerRole/{id}")
  @ApiResponse(
      responseCode = "200",
      description = "Record found",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = PractitionerRole.class)))
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
  PractitionerRole practitionerRoleRead(
      @Parameter(in = ParameterIn.PATH, name = "id", required = true) String id);

  @Operation(
      summary = "Practitioner Role Search",
      description =
          "http://www.fhir.org/guides/argonaut/pd/StructureDefinition-argo-practitionerrole.html",
      tags = {"Practitioner Role"})
  @GET
  @Path("PractitionerRole")
  @ApiResponse(
      responseCode = "200",
      description = "Record found",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = PractitionerRole.Bundle.class)))
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
  PractitionerRole.Bundle practitionerRoleSearch(
      @Parameter(in = ParameterIn.QUERY, name = "identifier") String identifier,
      @Parameter(in = ParameterIn.QUERY, name = "specialty") String specialty,
      @Parameter(in = ParameterIn.QUERY, name = "family") String family,
      @Parameter(in = ParameterIn.QUERY, name = "given") String given,
      @Parameter(in = ParameterIn.QUERY, name = "page") @DefaultValue("1") int page,
      @Parameter(in = ParameterIn.QUERY, name = "_count") @DefaultValue("30") int count);
}
