package gov.va.api.health.dataquery.service.controller.practitionerrole;

import static gov.va.api.lighthouse.vulcan.Rules.atLeastOneParameterOf;
import static gov.va.api.lighthouse.vulcan.Vulcan.returnNothing;
import static gov.va.api.lighthouse.vulcan.VulcanConfiguration.PagingConfiguration.noSortableParameters;

import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.CompositeCdwIds;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.practitioner.DatamartPractitioner;
import gov.va.api.health.dataquery.service.controller.practitioner.PractitionerEntity;
import gov.va.api.health.dataquery.service.controller.practitioner.PractitionerRepository;
import gov.va.api.health.dataquery.service.controller.vulcanizer.Bundling;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedBundler;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedReader;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedTransformation;
import gov.va.api.health.r4.api.resources.PractitionerRole;
import gov.va.api.lighthouse.datamart.CompositeCdwId;
import gov.va.api.lighthouse.vulcan.Vulcan;
import gov.va.api.lighthouse.vulcan.VulcanConfiguration;
import gov.va.api.lighthouse.vulcan.mappings.Mappings;
import java.util.List;
import java.util.Optional;
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
 * Request Mappings for Practitioner Role Profile, see
 * https://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-practitionerrole.html for
 * implementation details.
 */
@Validated
@RestController
@SuppressWarnings("WeakerAccess")
@RequestMapping(
    value = "/r4/PractitionerRole",
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor_ = @Autowired)
public class R4PractitionerRoleController {
  private final WitnessProtection witnessProtection;

  private final LinkProperties linkProperties;

  private final PractitionerRepository repository;

  private VulcanConfiguration<PractitionerEntity> configuration() {
    return VulcanConfiguration.forEntity(PractitionerEntity.class)
        .paging(
            linkProperties.pagingConfiguration(
                "PractitionerRole", PractitionerEntity.naturalOrder(), noSortableParameters()))
        .mappings(
            Mappings.forEntity(PractitionerEntity.class)
                .value("_id", "cdwId", witnessProtection::toCdwId)
                .get())
        .defaultQuery(returnNothing())
        .rules(List.of(atLeastOneParameterOf("_id")))
        .build();
  }

  @GetMapping(value = {"/{publicId}"})
  PractitionerRole read(@PathVariable("publicId") String publicId) {
    return vulcanizedReader().read(publicId);
  }

  @GetMapping(
      value = {"/{publicId}"},
      headers = {"raw=true"})
  String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    return vulcanizedReader().readRaw(publicId, response);
  }

  @GetMapping
  PractitionerRole.Bundle search(HttpServletRequest request) {
    return Vulcan.forRepo(repository)
        .config(configuration())
        .build()
        .search(request)
        .map(toBundle());
  }

  VulcanizedBundler<
          PractitionerEntity,
          DatamartPractitioner,
          PractitionerRole,
          PractitionerRole.Entry,
          PractitionerRole.Bundle>
      toBundle() {
    return VulcanizedBundler.forTransformation(transformation())
        .bundling(
            Bundling.newBundle(PractitionerRole.Bundle::new)
                .newEntry(PractitionerRole.Entry::new)
                .linkProperties(linkProperties)
                .build())
        .build();
  }

  VulcanizedTransformation<PractitionerEntity, DatamartPractitioner, PractitionerRole>
      transformation() {
    return VulcanizedTransformation.toDatamart(PractitionerEntity::asDatamartPractitioner)
        .toResource(dm -> R4PractitionerRoleTransformer.builder().datamart(dm).build().toFhir())
        .witnessProtection(witnessProtection)
        .replaceReferences(
            resource ->
                Stream.concat(
                    resource
                        .practitionerRole()
                        .stream()
                        .map(role -> role.managingOrganization().orElse(null)),
                    resource.practitionerRole().stream().flatMap(role -> role.location().stream())))
        .build();
  }

  VulcanizedReader<PractitionerEntity, DatamartPractitioner, PractitionerRole, CompositeCdwId>
      vulcanizedReader() {
    return VulcanizedReader
        .<PractitionerEntity, DatamartPractitioner, PractitionerRole, CompositeCdwId>
            forTransformation(transformation())
        .repository(repository)
        .toPatientId(e -> Optional.empty())
        .toPrimaryKey(CompositeCdwIds::requireCompositeIdStringFormat)
        .toPayload(PractitionerEntity::payload)
        .build();
  }
}
