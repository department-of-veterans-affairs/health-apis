package gov.va.api.health.dataquery.service.controller.practitioner;

import static java.util.Collections.emptyList;

import com.google.common.collect.Iterables;
import gov.va.api.health.dataquery.service.controller.AbstractIncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.Stu3Bundler;
import gov.va.api.health.dataquery.service.controller.Stu3Validator;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.stu3.api.resources.OperationOutcome;
import gov.va.api.health.stu3.api.resources.Practitioner;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Min;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
@RequestMapping(
  value = {"/stu3/Practitioner"},
  produces = {"application/json", "application/json+fhir", "application/fhir+json"}
)
@AllArgsConstructor(onConstructor = @__({@Autowired}))
public class Stu3PractitionerController {

  private Stu3Bundler bundler;

  private PractitionerRepository repository;

  private WitnessProtection witnessProtection;

  private static PageRequest page(int page, int count) {
    return PageRequest.of(page - 1, count == 0 ? 1 : count, PractitionerEntity.naturalOrder());
  }

  private Practitioner.Bundle bundle(
      MultiValueMap<String, String> parameters, List<Practitioner> reports, int totalRecords) {
    return bundler.bundle(
        PageLinks.LinkConfig.builder()
            .path("Practitioner")
            .queryParams(parameters)
            .page(Parameters.pageOf(parameters))
            .recordsPerPage(Parameters.countOf(parameters))
            .totalRecords(totalRecords)
            .build(),
        reports,
        Practitioner.Entry::new,
        Practitioner.Bundle::new);
  }

  private PractitionerEntity entityById(String publicId) {
    Optional<PractitionerEntity> entity = repository.findById(witnessProtection.toCdwId(publicId));
    return entity.orElseThrow(() -> new ResourceExceptions.NotFound(publicId));
  }

  /** Read by id. */
  @GetMapping(value = {"/{publicId}"})
  public Practitioner read(@PathVariable("publicId") String publicId) {
    PractitionerEntity entity = entityById(publicId);
    return Iterables.getOnlyElement(transform(Stream.of(entity)));
  }

  /** Read raw. */
  @GetMapping(
    value = {"/{publicId}"},
    headers = {"raw=true"}
  )
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    AbstractIncludesIcnMajig.addHeaderForNoPatients(response);
    return entityById(publicId).payload();
  }

  /** Search by _id. */
  @GetMapping(params = {"_id"})
  public Practitioner.Bundle searchById(
      @RequestParam("_id") String publicId,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    Practitioner resource = read(publicId);
    return bundle(
        Parameters.builder()
            .add("identifier", publicId)
            .add("page", page)
            .add("_count", count)
            .build(),
        resource == null || count == 0 ? emptyList() : List.of(resource),
        resource == null ? 0 : 1);
  }

  /** Search by Identifier. */
  @GetMapping(params = {"identifier"})
  public Practitioner.Bundle searchByIdentifier(
      @RequestParam("identifier") String publicId,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return searchById(publicId, page, count);
  }

  private List<Practitioner> transform(Stream<PractitionerEntity> entities) {
    List<DatamartPractitioner> datamarts =
        entities.map(PractitionerEntity::asDatamartPractitioner).collect(Collectors.toList());
    witnessProtection.registerAndUpdateReferences(
        datamarts,
        resource ->
            Stream.concat(
                Stream.of(resource.practitionerRole().get().managingOrganization().orElse(null)),
                resource.practitionerRole().get().location().stream()));
    return datamarts
        .stream()
        .map(dm -> Stu3PractitionerTransformer.builder().datamart(dm).build().toFhir())
        .collect(Collectors.toList());
  }

  // /** Search by Family and Given. */
  // @GetMapping(params = {"family", "given"})
  // @SneakyThrows
  // public Practitioner.Bundle searchByFamilyandGiven(
  // @RequestParam("given") String given,
  // @RequestParam("family") String family,
  // @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
  // @CountParameter @Min(0) int count) {
  // Page<PractitionerEntity> entitiesPage = repository.findAll(spec, page(page, count));
  // if (count == 0) {
  // return bundle(Parameters.builder()
  // .add("family", family)
  // .add("given", given)
  // .add("page", page)
  // .add("_count", count)
  // .build(), emptyList(), (int) entitiesPage.getTotalElements());
  // }
  // return bundle(parameters, transform(entitiesPage.get()), (int)
  // entitiesPage.getTotalElements());
  //
  // return bundle(
  // Parameters.builder()
  // .add("family", family)
  // .add("given", given)
  // .add("page", page)
  // .add("_count", count)
  // .build(),
  // resource == null || count == 0 ? emptyList() : List.of(resource),
  // resource == null ? 0 : 1);
  // }



  /** Hey, this is a validate endpoint. It validates. */
  @PostMapping(
    value = "/$validate",
    consumes = {"application/json", "application/json+fhir", "application/fhir+json"}
  )
  public OperationOutcome validate(@RequestBody Practitioner.Bundle bundle) {
    return Stu3Validator.create().validate(bundle);
  }
}
