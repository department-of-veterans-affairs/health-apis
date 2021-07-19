package gov.va.api.health.dataquery.service.api;

import gov.va.api.health.r4.api.resources.Device;
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

public interface R4DeviceApi {
  @Operation(
      summary = "Implantable Device Read",
      description =
          "http://hl7.org/fhir/us/core/StructureDefinition-us-core-implantable-device.html",
      tags = {"Device"})
  @GET
  @Path("Device/{id}")
  @ApiResponse(
      responseCode = "200",
      description = "Record found",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = Device.class)))
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
  Device deviceRead(
      @Parameter(
              in = ParameterIn.PATH,
              name = "id",
              required = true,
              description =
                  "The logical id of the resource. Once assigned, this value never changes.")
          String id);

  @Operation(
      summary = "Implantable Device Search",
      description =
          "http://hl7.org/fhir/us/core/StructureDefinition-us-core-implantable-device.html",
      tags = {"Device"})
  @GET
  @Path("Device")
  @ApiResponse(
      responseCode = "200",
      description = "Records Found",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = Device.Bundle.class)))
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
  Device.Bundle deviceSearch(
      @Parameter(
              in = ParameterIn.QUERY,
              name = "patient",
              description =
                  " The Integration Control Number (ICN) assigned by the "
                      + "Master Patient Index (MPI) that indicates "
                      + "the patient who the device record is "
                      + "associated with.")
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
              name = "type",
              description = "The code used to identify the medical device.")
          String type,
      /* TODO Add type search parameter when it becomes available. */
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
