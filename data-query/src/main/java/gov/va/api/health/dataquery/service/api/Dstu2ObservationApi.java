package gov.va.api.health.dataquery.service.api;

import gov.va.api.health.dstu2.api.resources.Observation;
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

public interface Dstu2ObservationApi {
  @Operation(
      summary = "Observation Read",
      description =
          "http://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-observationresults.html",
      tags = {"Observation"})
  @GET
  @Path("Observation/{id}")
  @ApiResponse(
      responseCode = "200",
      description = "Record found",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = Observation.class)))
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
  Observation observationRead(
      @Parameter(
              in = ParameterIn.PATH,
              name = "id",
              required = true,
              description =
                  "The logical id of the resource. Once assigned, this value never changes.")
          String id);

  @Operation(
      summary = "Observation Search",
      description =
          "http://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-observationresults.html",
      tags = {"Observation"})
  @GET
  @Path("Observation")
  @ApiResponse(
      responseCode = "200",
      description = "Record found",
      content =
          @Content(
              schema = @Schema(implementation = Observation.Bundle.class),
              mediaType = "application/fhir+json"))
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
  Observation.Bundle observationSearch(
      @Parameter(
              in = ParameterIn.QUERY,
              name = "patient",
              description =
                  "The Integration Control Number (ICN) assigned by the Master Patient Index (MPI)"
                      + " that refers to the patient that is described by the observation")
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
              name = "code",
              description =
                  "A code that indicates the type of information contained within "
                      + "the observation. [LOINC Observation Codes](http://hl7.org/fhir/DSTU2/valueset-observation-codes.html)")
          String code,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "category",
              description =
                  "The general classification of the type of observation. "
                      + "[Observation Category Codes](http://hl7.org/fhir/DSTU2/valueset-observation-category.html)")
          String category,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "date",
              description =
                  "A date or range of dates (maximum of 2) that "
                      + "describe the date that the observation was recorded.")
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
                  "The number of resources that should be returned in a single page. "
                      + "The maximum count size is 100.")
          @DefaultValue("30")
          int count);
}
