package gov.va.api.health.dataquery.service.controller.organization;

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
import gov.va.api.health.r4.api.resources.Organization;
import gov.va.api.lighthouse.vulcan.CircuitBreaker;
import gov.va.api.lighthouse.vulcan.Specifications;
import gov.va.api.lighthouse.vulcan.SystemIdFields;
import gov.va.api.lighthouse.vulcan.Vulcan;
import gov.va.api.lighthouse.vulcan.VulcanConfiguration;
import gov.va.api.lighthouse.vulcan.mappings.Mappings;
import gov.va.api.lighthouse.vulcan.mappings.TokenParameter;
import java.util.List;
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
 * Request Mappings for Organization Profile.
 *
 * @implSpec https://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-organization.html
 */
@Validated
@RestController
@RequestMapping(
    value = {"/r4/Organization"},
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor_ = @Autowired)
public class R4OrganizationController {
  private final LinkProperties linkProperties;

  private OrganizationRepository repository;

  private WitnessProtection witnessProtection;

  private VulcanConfiguration<OrganizationEntity> configuration() {
    return VulcanConfiguration.forEntity(OrganizationEntity.class)
        .paging(
            linkProperties.pagingConfiguration("Organization", OrganizationEntity.naturalOrder()))
        .mappings(
            Mappings.forEntity(OrganizationEntity.class)
                .string("address", "street")
                .string("address-city", "city")
                .string("address-state", "state")
                .string("address-postalcode", "postalCode")
                .value("_id", "cdwId", witnessProtection::toCdwId)
                .tokens(
                    "identifier",
                    this::tokenIdentifierIsSupported,
                    this::tokenIdentifierSpecification)
                .string("name", "name")
                .get())
        .rules(List.of(parametersNeverSpecifiedTogether("_id", "identifier")))
        .defaultQuery(returnNothing())
        .build();
  }

  private Specification<OrganizationEntity> facilityIdSpec(String maybeFacilityId) {
    try {
      var facilityId = FacilityId.from(maybeFacilityId);
      return Specifications.<OrganizationEntity>select("facilityType", facilityId.type().toString())
          .and(select("stationNumber", facilityId.stationNumber()));
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  /** Read Support. */
  @GetMapping(value = "/{publicId}")
  public Organization read(@PathVariable("publicId") String publicId) {
    return reader().read(publicId);
  }

  /** Read Raw Datamart Payload Support. */
  @GetMapping(
      value = "/{publicId}",
      headers = {"raw=true"})
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    return reader().readRaw(publicId, response);
  }

  VulcanizedReader<OrganizationEntity, DatamartOrganization, Organization, String> reader() {
    return VulcanizedReader
        .<OrganizationEntity, DatamartOrganization, Organization, String>forTransformation(
            transformation())
        .repository(repository)
        .toPatientId(e -> Optional.empty())
        .toPrimaryKey(Function.identity())
        .toPayload(OrganizationEntity::payload)
        .build();
  }

  /** Search Support. */
  @GetMapping
  public Organization.Bundle search(HttpServletRequest request) {
    return Vulcan.forRepo(repository)
        .config(configuration())
        .build()
        .search(request)
        .map(toBundle());
  }

  VulcanizedBundler<
          OrganizationEntity,
          DatamartOrganization,
          Organization,
          Organization.Entry,
          Organization.Bundle>
      toBundle() {
    return VulcanizedBundler.forTransformation(transformation())
        .bundling(
            Bundling.newBundle(Organization.Bundle::new)
                .newEntry(Organization.Entry::new)
                .linkProperties(linkProperties)
                .build())
        .build();
  }

  /**
   * Supported identifiers:
   *
   * <p>I3-1a2b3c4d
   *
   * <p>vha_123
   *
   * <p>https://api.va.gov/services/fhir/v0/r4/NamingSystem/va-facility-identifier|vha_123
   */
  private boolean tokenIdentifierIsSupported(TokenParameter token) {
    return token.hasSupportedSystem(
            FacilityTransformers.FAPI_IDENTIFIER_SYSTEM, "http://hl7.org/fhir/sid/us-npi")
        || token.hasAnySystem();
  }

  /** Use a token value to determine which database columns to select from. */
  private Specification<OrganizationEntity> tokenIdentifierSpecification(TokenParameter token) {
    return token
        .behavior()
        .onAnySystemAndExplicitCode(
            code ->
                Specifications.<OrganizationEntity>select("cdwId", witnessProtection.toCdwId(code))
                    .or(facilityIdSpec(code)))
        .onExplicitSystemAndAnyCode(
            SystemIdFields.forEntity(OrganizationEntity.class)
                .parameterName("identifier")
                .add(FacilityTransformers.FAPI_IDENTIFIER_SYSTEM, "stationNumber")
                .add("http://hl7.org/fhir/sid/us-npi", "npi")
                .matchSystemOnly())
        .onExplicitSystemAndExplicitCode(
            SystemIdFields.forEntity(OrganizationEntity.class)
                .parameterName("identifier")
                .addWithCustomSystemAndCodeHandler(
                    FacilityTransformers.FAPI_IDENTIFIER_SYSTEM,
                    "stationNumber",
                    (system, code) -> {
                      var facilityIdSpec = facilityIdSpec(code);
                      if (facilityIdSpec == null) {
                        throw CircuitBreaker.noResultsWillBeFound(
                            "identifier", code, "Invalid facility ID");
                      }
                      return facilityIdSpec;
                    })
                .add("http://hl7.org/fhir/sid/us-npi", "npi")
                .matchSystemAndCode())
        .build()
        .execute();
  }

  VulcanizedTransformation<OrganizationEntity, DatamartOrganization, Organization>
      transformation() {
    return VulcanizedTransformation.toDatamart(OrganizationEntity::asDatamartOrganization)
        .toResource(dm -> R4OrganizationTransformer.builder().datamart(dm).build().toFhir())
        .witnessProtection(witnessProtection)
        .replaceReferences(resource -> Stream.empty())
        .build();
  }
}
