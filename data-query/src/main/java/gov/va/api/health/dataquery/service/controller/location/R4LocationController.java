package gov.va.api.health.dataquery.service.controller.location;

import static gov.va.api.lighthouse.vulcan.Rules.forbidUnknownParameters;
import static gov.va.api.lighthouse.vulcan.Rules.parametersNeverSpecifiedTogether;
import static gov.va.api.lighthouse.vulcan.Vulcan.returnNothing;

import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.vulcanizer.Bundling;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedBundler;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedReader;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedTransformation;
import gov.va.api.health.r4.api.resources.Location;
import gov.va.api.lighthouse.vulcan.Vulcan;
import gov.va.api.lighthouse.vulcan.VulcanConfiguration;
import gov.va.api.lighthouse.vulcan.mappings.Mappings;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Request Mappings for Location Profile.
 *
 * @implSpec https://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-location.html
 */
@Validated
@RestController
@RequestMapping(
    value = {"/r4/Location"},
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor = @__({@Autowired}))
public class R4LocationController {

  private final LinkProperties linkProperties;

  private LocationRepository repository;

  private WitnessProtection witnessProtection;

  private VulcanConfiguration<LocationEntity> configuration() {
    return VulcanConfiguration.forEntity(LocationEntity.class)
        .paging(linkProperties.pagingConfiguration("Location", LocationEntity.naturalOrder()))
        .mappings(
            Mappings.forEntity(LocationEntity.class)
                .value("_id", "cdwId", witnessProtection::toCdwId)
                .value("identifier", "cdwId", witnessProtection::toCdwId)
                .value("name")
                .string("address", "street")
                .string("address-city", "city")
                .string("address-state", "state")
                .string("address-postalcode", "postalCode")
                .get())
        .defaultQuery(returnNothing())
        .rule(parametersNeverSpecifiedTogether("_id", "identifier"))
        .rule(forbidUnknownParameters())
        .build();
  }

  /** Read Support. */
  @GetMapping(value = {"/{publicId}"})
  public Location read(@PathVariable("publicId") String publicId) {
    return vulcanizedReader().read(publicId);
  }

  /** Read Raw Datamart Payload Support. */
  @GetMapping(
      value = "/{publicId}",
      headers = {"raw=true"})
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    return vulcanizedReader().readRaw(publicId, response);
  }

  /** Search support. */
  @GetMapping
  public Location.Bundle search(HttpServletRequest request) {
    return Vulcan.forRepo(repository)
        .config(configuration())
        .build()
        .search(request)
        .map(toBundle());
  }

  VulcanizedBundler<LocationEntity, DatamartLocation, Location, Location.Entry, Location.Bundle>
      toBundle() {
    return VulcanizedBundler.forTransformation(transformation())
        .bundling(
            Bundling.newBundle(Location.Bundle::new)
                .newEntry(Location.Entry::new)
                .linkProperties(linkProperties)
                .build())
        .build();
  }

  VulcanizedTransformation<LocationEntity, DatamartLocation, Location> transformation() {
    return VulcanizedTransformation.toDatamart(LocationEntity::asDatamartLocation)
        .toResource(dm -> R4LocationTransformer.builder().datamart(dm).build().toFhir())
        .witnessProtection(witnessProtection)
        .replaceReferences(x -> Stream.empty())
        .build();
  }

  VulcanizedReader<LocationEntity, DatamartLocation, Location, String> vulcanizedReader() {
    return VulcanizedReader.<LocationEntity, DatamartLocation, Location, String>forTransformation(
            transformation())
        .repository(repository)
        .toPatientId(e -> Optional.of(e.cdwId()))
        .toPrimaryKey(Function.identity())
        .toPayload(LocationEntity::payload)
        .build();
  }
}
