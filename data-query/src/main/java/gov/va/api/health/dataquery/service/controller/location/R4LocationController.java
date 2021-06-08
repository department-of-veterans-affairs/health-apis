package gov.va.api.health.dataquery.service.controller.location;

import static gov.va.api.health.dataquery.service.config.LinkProperties.noSortableParameters;
import static gov.va.api.lighthouse.vulcan.Rules.forbidUnknownParameters;
import static gov.va.api.lighthouse.vulcan.Rules.parametersNeverSpecifiedTogether;
import static gov.va.api.lighthouse.vulcan.Specifications.select;
import static gov.va.api.lighthouse.vulcan.Vulcan.returnNothing;

import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.FacilityId;
import gov.va.api.health.dataquery.service.controller.FacilityTransformers;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.vulcanizer.Bundling;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedBundler;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedReader;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedTransformation;
import gov.va.api.health.r4.api.resources.Location;
import gov.va.api.lighthouse.datamart.CompositeCdwId;
import gov.va.api.lighthouse.vulcan.CircuitBreaker;
import gov.va.api.lighthouse.vulcan.Specifications;
import gov.va.api.lighthouse.vulcan.SystemIdFields;
import gov.va.api.lighthouse.vulcan.Vulcan;
import gov.va.api.lighthouse.vulcan.VulcanConfiguration;
import gov.va.api.lighthouse.vulcan.mappings.Mappings;
import gov.va.api.lighthouse.vulcan.mappings.TokenParameter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
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
@AllArgsConstructor(onConstructor_ = @Autowired)
public class R4LocationController {
  private final LinkProperties linkProperties;

  private LocationRepository repository;

  private WitnessProtection witnessProtection;

  private Specification<LocationEntity> clinicIdSpec(String maybeClinicId) {
    try {
      if (!maybeClinicId.contains("_")) {
        return null;
      }
      var facilityId = FacilityId.from(maybeClinicId.substring(0, maybeClinicId.lastIndexOf("_")));
      var locationIen = maybeClinicId.substring(maybeClinicId.lastIndexOf("_") + 1);
      Specification<LocationEntity> spec =
          Specifications.<LocationEntity>select("facilityType", facilityId.type().toString());
      if (spec == null) {
        return null;
      }
      return spec.and(select("locationIen", locationIen))
          .and(select("stationNumber", facilityId.stationNumber()));
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private VulcanConfiguration<LocationEntity> configuration() {
    return VulcanConfiguration.forEntity(LocationEntity.class)
        .paging(
            linkProperties.pagingConfiguration(
                "Location", LocationEntity.naturalOrder(), noSortableParameters()))
        .mappings(
            Mappings.forEntity(LocationEntity.class)
                .value("_id", "cdwId", witnessProtection::toCdwId)
                .tokens(
                    "identifier",
                    this::tokenIdentifierIsSupported,
                    this::tokenIdentifierSpecification)
                .value("name")
                .string("address", "street")
                .string("address-city", "city")
                .string("address-state", "state")
                .string("address-postalcode", "postalCode")
                .values("organization", this::organizationMapping)
                .get())
        .defaultQuery(returnNothing())
        .rules(
            List.of(
                parametersNeverSpecifiedTogether("_id", "identifier"), forbidUnknownParameters()))
        .build();
  }

  private Map<String, ?> organizationMapping(String publicId) {
    try {
      String cdwId = witnessProtection.toCdwId(publicId);
      CompositeCdwId compositeCdwId = CompositeCdwId.fromCdwId(cdwId);
      return Map.of(
          "managingOrgIdNumber",
          compositeCdwId.cdwIdNumber(),
          "managingOrgResourceCode",
          compositeCdwId.cdwIdResourceCode());
    } catch (IllegalArgumentException e) {
      return Map.of();
    }
  }

  @GetMapping(value = {"/{publicId}"})
  Location read(@PathVariable("publicId") String publicId) {
    return vulcanizedReader().read(publicId);
  }

  @GetMapping(
      value = "/{publicId}",
      headers = {"raw=true"})
  String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    return vulcanizedReader().readRaw(publicId, response);
  }

  @GetMapping
  Location.Bundle search(HttpServletRequest request) {
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

  private boolean tokenIdentifierIsSupported(TokenParameter token) {
    return token.hasSupportedSystem(FacilityTransformers.FAPI_CLINIC_IDENTIFIER_SYSTEM)
        || token.hasAnySystem();
  }

  private Specification<LocationEntity> tokenIdentifierSpecification(TokenParameter token) {
    return token
        .behavior()
        .onAnySystemAndExplicitCode(
            code ->
                Specifications.<LocationEntity>select("cdwId", witnessProtection.toCdwId(code))
                    .or(clinicIdSpec(code)))
        .onExplicitSystemAndAnyCode(
            SystemIdFields.forEntity(LocationEntity.class)
                .parameterName("identifier")
                .add(FacilityTransformers.FAPI_CLINIC_IDENTIFIER_SYSTEM, "stationNumber")
                .matchSystemOnly())
        .onExplicitSystemAndExplicitCode(
            SystemIdFields.forEntity(LocationEntity.class)
                .parameterName("identifier")
                .addWithCustomSystemAndCodeHandler(
                    FacilityTransformers.FAPI_CLINIC_IDENTIFIER_SYSTEM,
                    "stationNumber",
                    (system, code) -> {
                      var clinicIdSpec = clinicIdSpec(code);
                      if (clinicIdSpec == null) {
                        throw CircuitBreaker.noResultsWillBeFound(
                            "identifier", code, "Invalid clinic ID");
                      }
                      return clinicIdSpec;
                    })
                .matchSystemAndCode())
        .build()
        .execute();
  }

  VulcanizedTransformation<LocationEntity, DatamartLocation, Location> transformation() {
    return VulcanizedTransformation.toDatamart(LocationEntity::asDatamartLocation)
        .toResource(dm -> R4LocationTransformer.builder().datamart(dm).build().toFhir())
        .witnessProtection(witnessProtection)
        .replaceReferences(resource -> Stream.of(resource.managingOrganization()))
        .build();
  }

  VulcanizedReader<LocationEntity, DatamartLocation, Location, String> vulcanizedReader() {
    return VulcanizedReader.<LocationEntity, DatamartLocation, Location, String>forTransformation(
            transformation())
        .repository(repository)
        .toPatientId(e -> Optional.empty())
        .toPrimaryKey(Function.identity())
        .toPayload(LocationEntity::payload)
        .build();
  }
}
