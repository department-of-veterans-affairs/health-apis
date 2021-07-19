package gov.va.api.health.dataquery.service.api;

import gov.va.api.health.r4.api.resources.Appointment;
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

public interface R4AppointmentApi {
  @Operation(
      summary = "Appointment Read",
      description = "https://www.hl7.org/fhir/R4/appointment.html",
      tags = {"Appointment"})
  @GET
  @Path("Appointment/{id}")
  @ApiResponse(
      responseCode = "200",
      description = "Record found",
      content =
          @Content(
              mediaType = "application/json+fhir",
              schema = @Schema(implementation = Appointment.class)))
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
  Appointment appointmentRead(
      @Parameter(
              in = ParameterIn.PATH,
              name = "id",
              required = true,
              description =
                  "The logical id of the resource. Once assigned, this value never changes.")
          String id);

  @Operation(
      summary = "Appointment Search",
      description = "https://www.hl7.org/fhir/R4/appointment.html",
      tags = {"Appointment"})
  @GET
  @Path("Appointment")
  @ApiResponse(
      responseCode = "200",
      description = "Record found",
      content =
          @Content(
              mediaType = "application/json+fhir",
              schema = @Schema(implementation = Appointment.Bundle.class)))
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
  Appointment.Bundle appointmentSearch(
      @Parameter(
              in = ParameterIn.QUERY,
              name = "patient",
              description =
                  "An Integration Control Number (ICN) assigned by the Master Patient Index (MPI)"
                      + " that refers to a patient that is participating in the appointment.")
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
              name = "location",
              description = "The location ID of the facility where the appointment takes place.")
          String location,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "_lastUpdated",
              description = "The date when the appointment record was last updated.")
          String lastUpdated,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "date",
              description =
                  "A date or range of dates (maximum of 2) that "
                      + "describe the date of the appointment.")
          String[] date,
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
          int count,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "_sort",
              description = "Parameters by which to sort results.")
          String sort);
}
