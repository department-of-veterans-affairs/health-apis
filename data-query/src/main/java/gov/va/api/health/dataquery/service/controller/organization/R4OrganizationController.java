package gov.va.api.health.dataquery.service.controller.organization;

import static gov.va.api.lighthouse.vulcan.Rules.parametersNeverSpecifiedTogether;
import static gov.va.api.lighthouse.vulcan.Vulcan.returnNothing;

import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.vulcanizer.Bundling;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedBundler;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedReader;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedTransformation;
import gov.va.api.health.r4.api.resources.Organization;
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
                .value("identifier", "cdwId", witnessProtection::toCdwId)
                .string("name", "name")
                .get())
        .rule(parametersNeverSpecifiedTogether("_id", "identifier"))
        .defaultQuery(returnNothing())
        .build();
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

  VulcanizedTransformation<OrganizationEntity, DatamartOrganization, Organization>
      transformation() {
    return VulcanizedTransformation.toDatamart(OrganizationEntity::asDatamartOrganization)
        .toResource(dm -> R4OrganizationTransformer.builder().datamart(dm).build().toFhir())
        .witnessProtection(witnessProtection)
        .replaceReferences(resource -> Stream.empty())
        .build();
  }
}
