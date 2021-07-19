package gov.va.api.health.dataquery.service.api;

import gov.va.api.health.r4.api.resources.Location;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

public interface R4LocationApi {
  @Operation(
      summary = "Location Read",
      description =
          "https://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-location.html",
      tags = {"Location"})
  @GET
  @Path("Location/{id}")
  @ApiResponse(
      responseCode = "200",
      description = "Record found",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = Location.class)))
  @ApiResponse(
      responseCode = "400",
      description = "Bad request",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class)))
  @ApiResponse(
      responseCode = "404",
      description = "Not found",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class)))
  Location locationRead(
      @Parameter(
              in = ParameterIn.PATH,
              name = "id",
              required = true,
              description =
                  "The logical id of the resource. Once assigned, this value never changes.")
          String id);

  @Operation(
      summary = "Location Search",
      description =
          "https://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-location.html",
      tags = {"Location"})
  @GET
  @Path("Location")
  @ApiResponse(
      responseCode = "200",
      description = "Record found",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = Location.Bundle.class)))
  @ApiResponse(
      responseCode = "400",
      description = "Bad request",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class)))
  @ApiResponse(
      responseCode = "404",
      description = "Not found",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class)))
  Location.Bundle locationSearch(
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
              name = "address",
              description =
                  "Indicates the physical location of the record "
                      + "expressed using postal conventions "
                      + "(as opposed to GPS or other location definition formats).")
          String address,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "address-city",
              description = "Indicates the geographical city where the location resides.")
          String addressCity,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "address-state",
              description = "Indicates the geographical state where the location resides.")
          String addressState,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "address-postalcode",
              description =
                  "Indicates the postal code that designates the region"
                      + " where the location resides.")
          String addressPostalCode,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "name",
              description = "Indicates the name of the location as it is referenced by humans.")
          String name,
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
