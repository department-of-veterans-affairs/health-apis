package gov.va.api.health.dataquery.service.api;

import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.r4.api.resources.Practitioner;
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

public interface R4PractitionerApi {
  @Operation(
      summary = "Practitioner Read",
      description =
          "https://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-practitioner.html",
      tags = {"Practitioner"})
  @GET
  @Path("Practitioner/{id}")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Record found",
        content =
            @Content(
                mediaType = "application/fhir+json",
                schema = @Schema(implementation = Practitioner.class))),
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
  Practitioner practitionerRead(
      @Parameter(
              in = ParameterIn.PATH,
              name = "id",
              required = true,
              description =
                  "The logical id of the resource. Once assigned, this value never changes.",
              example = "I2-HRJI2MVST2IQSPR7U5SACWIWZA000000")
          String id);

  @Operation(
      summary = "Practitioner Search",
      description =
          "https://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-practitioner.html",
      tags = {"Practitioner"})
  @GET
  @Path("Practitioner")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Record found",
        content =
            @Content(
                mediaType = "application/fhir+json",
                schema = @Schema(implementation = Practitioner.Bundle.class))),
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
  Practitioner.Bundle practitionerSearch(
      @Parameter(
              in = ParameterIn.QUERY,
              name = "_id",
              description =
                  "The logical id of the resource. Once assigned, this value never changes.",
              example = "I2-HRJI2MVST2IQSPR7U5SACWIWZA000000")
          String id,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "identifier",
              description = "A unique identifier for a practitioner within a given system.",
              example = "http://hl7.org/fhir/sid/us-npi|1932127842")
          String identifier,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "family",
              description = "The family name of the practitioner.",
              example = "DOE922")
          String family,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "given",
              description = "The given name of the practitioner.",
              example = "JANE460")
          String given,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "name",
              description = "The given or family name of the practitioner.",
              example = "DOE922")
          String name,
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
