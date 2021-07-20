package gov.va.api.health.dataquery.service.api;

import gov.va.api.health.dstu2.api.resources.MedicationOrder;
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

public interface Dstu2MedicationOrderApi {
  @Operation(
      summary = "Medication Order Read",
      description =
          "http://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-medicationorder.html",
      tags = {"Medication Order"})
  @GET
  @Path("MedicationOrder/{id}")
  @ApiResponse(
      responseCode = "200",
      description = "Record found",
      content =
          @Content(
              mediaType = "application/json+fhir",
              schema = @Schema(implementation = MedicationOrder.class)))
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
  MedicationOrder medicationOrderRead(
      @Parameter(
              in = ParameterIn.PATH,
              name = "id",
              required = true,
              description =
                  "The logical id of the resource. Once assigned, this value never changes.")
          String id);

  @Operation(
      summary = "MedicationOrder Search",
      description =
          "http://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-medicationorder.html",
      tags = {"Medication Order"})
  @GET
  @Path("MedicationOrder")
  @ApiResponse(
      responseCode = "200",
      description = "Record found",
      content =
          @Content(
              mediaType = "application/json+fhir",
              schema = @Schema(implementation = MedicationOrder.Bundle.class)))
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
  MedicationOrder.Bundle medicationOrderSearch(
      @Parameter(
              in = ParameterIn.QUERY,
              name = "patient",
              description =
                  "Integration Control Number (ICN) assigned by the Master Patient Index (MPI)"
                      + " of the person to whom the medication will be given.")
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
