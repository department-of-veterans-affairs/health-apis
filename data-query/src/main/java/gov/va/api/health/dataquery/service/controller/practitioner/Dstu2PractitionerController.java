package gov.va.api.health.dataquery.service.controller.practitioner;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import gov.va.api.health.dataquery.service.controller.CompositeCdwIds;
import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.Dstu2Bundler;
import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.practitionerrole.DatamartPractitionerRole;
import gov.va.api.health.dataquery.service.controller.practitionerrole.PractitionerRoleEntity;
import gov.va.api.health.dataquery.service.controller.practitionerrole.PractitionerRoleRepository;
import gov.va.api.health.dstu2.api.resources.Practitioner;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Request Mappings for Practitioner Profile, see http://hl7.org/fhir/DSTU2/practitioner.html for
 * implementation details.
 */
@Validated
@RestController
@SuppressWarnings("WeakerAccess")
@AllArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping(
    value = {"/dstu2/Practitioner"},
    produces = {"application/json", "application/json+fhir", "application/fhir+json"})
public class Dstu2PractitionerController {
  private Dstu2Bundler bundler;

  private PractitionerRepository repository;

  private PractitionerRoleRepository roleRepository;

  private WitnessProtection witnessProtection;

  Practitioner.Bundle bundle(
      MultiValueMap<String, String> parameters, List<Practitioner> reports, int totalRecords) {
    PageLinks.LinkConfig linkConfig =
        PageLinks.LinkConfig.builder()
            .path("Practitioner")
            .queryParams(parameters)
            .page(Parameters.pageOf(parameters))
            .recordsPerPage(Parameters.countOf(parameters))
            .totalRecords(totalRecords)
            .build();
    return bundler.bundle(
        Dstu2Bundler.BundleContext.of(
            linkConfig,
            reports,
            Function.identity(),
            Practitioner.Entry::new,
            Practitioner.Bundle::new));
  }

  PractitionerEntity findById(String publicId) {
    String cdwId = witnessProtection.toCdwId(publicId);
    Optional<PractitionerEntity> entity =
        CompositeCdwIds.optionalFromCdwId(cdwId).map(i -> repository.findById(i).orElse(null));
    if (!entity.isPresent() && !cdwId.endsWith(":S")) {
      entity =
          CompositeCdwIds.optionalFromCdwId(cdwId + ":S")
              .map(i -> repository.findById(i).orElse(null));
    }
    return entity.orElseThrow(() -> new ResourceExceptions.NotFound(publicId));
  }

  List<PractitionerRoleEntity> findRolesById(String publicId) {
    String cdwId = witnessProtection.toCdwId(publicId);
    if (cdwId.length() <= 1 || cdwId.charAt(cdwId.length() - 2) != ':') {
      cdwId = cdwId + ":S";
    }
    return CompositeCdwIds.optionalFromCdwId(cdwId)
        .map(
            id ->
                roleRepository.findByPractitionerIdNumberAndPractitionerResourceCode(
                    id.cdwIdNumber(), id.cdwIdResourceCode()))
        .orElse(List.of());
  }

  @GetMapping(value = {"/{publicId}"})
  Practitioner read(@PathVariable("publicId") String publicId) {
    DatamartPractitioner practitioner = findById(publicId).asDatamartPractitioner();
    List<DatamartPractitionerRole> practitionerRoles =
        findRolesById(publicId).stream().map(e -> e.asDatamartPractitionerRole()).collect(toList());
    replaceReferences(List.of(practitioner), practitionerRoles);
    return transform(practitioner, practitionerRoles);
  }

  @GetMapping(
      value = {"/{publicId}"},
      headers = {"raw=true"})
  String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    PractitionerEntity entity = findById(publicId);
    IncludesIcnMajig.addHeaderForNoPatients(response);
    return entity.payload();
  }

  private void replaceReferences(
      Collection<DatamartPractitioner> resources,
      Collection<DatamartPractitionerRole> roleResources) {
    witnessProtection.registerAndUpdateReferences(resources, resource -> Stream.empty());
    witnessProtection.registerAndUpdateReferences(
        roleResources,
        resource ->
            Stream.concat(
                Stream.of(
                    resource.practitioner().orElse(null),
                    resource.managingOrganization().orElse(null)),
                resource.location().stream()));
  }

  @GetMapping(params = {"_id"})
  Practitioner.Bundle searchById(
      @RequestParam("_id") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    Practitioner resource = read(id);
    return bundle(
        Parameters.builder().add("identifier", id).add("page", page).add("_count", count).build(),
        resource == null || count == 0 ? emptyList() : List.of(resource),
        resource == null ? 0 : 1);
  }

  @GetMapping(params = {"identifier"})
  Practitioner.Bundle searchByIdentifier(
      @RequestParam("identifier") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return searchById(id, page, count);
  }

  Practitioner transform(DatamartPractitioner dm, List<DatamartPractitionerRole> dmRoles) {
    return Dstu2PractitionerTransformer.builder()
        .datamart(dm)
        .datamartRoles(dmRoles)
        .build()
        .toFhir();
  }
}
