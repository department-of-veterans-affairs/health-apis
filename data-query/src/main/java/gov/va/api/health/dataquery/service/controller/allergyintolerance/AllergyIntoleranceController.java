package gov.va.api.health.dataquery.service.controller.allergyintolerance;

import static gov.va.api.health.dataquery.service.controller.Transformers.firstPayloadItem;
import static gov.va.api.health.dataquery.service.controller.Transformers.hasPayload;

import gov.va.api.health.dataquery.api.resources.AllergyIntolerance;
import gov.va.api.health.dataquery.api.resources.AllergyIntolerance.Bundle;
import gov.va.api.health.dataquery.api.resources.OperationOutcome;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.Bundler.BundleContext;
import gov.va.api.health.dataquery.service.controller.PageLinks.LinkConfig;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.Validator;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient;
import gov.va.api.health.dataquery.service.mranderson.client.Query;
import gov.va.dvp.cdw.xsd.model.CdwAllergyIntolerance103Root;
import java.util.Collections;
import java.util.function.Function;
import javax.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
 * Request Mappings for Allergy Intolerance Profile, see
 * http://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-allergyintolerance.html for
 * implementation details.
 */
@SuppressWarnings("WeakerAccess")
@Validated
@RestController
@RequestMapping(
  value = {"/api/AllergyIntolerance"},
  produces = {"application/json", "application/json+fhir", "application/fhir+json"}
)
@AllArgsConstructor(onConstructor = @__({@Autowired}))
public class AllergyIntoleranceController {
  private Transformer transformer;
  private MrAndersonClient mrAndersonClient;
  private Bundler bundler;

  private Bundle bundle(MultiValueMap<String, String> parameters, int page, int count) {
    CdwAllergyIntolerance103Root root = search(parameters);
    LinkConfig linkConfig =
        LinkConfig.builder()
            .path("AllergyIntolerance")
            .queryParams(parameters)
            .page(page)
            .recordsPerPage(count)
            .totalRecords(root.getRecordCount().intValue())
            .build();
    return bundler.bundle(
        BundleContext.of(
            linkConfig,
            root.getAllergyIntolerances() == null
                ? Collections.emptyList()
                : root.getAllergyIntolerances().getAllergyIntolerance(),
            transformer,
            AllergyIntolerance.Entry::new,
            AllergyIntolerance.Bundle::new));
  }

  /** Read by id. */
  @GetMapping(value = {"/{publicId}"})
  public AllergyIntolerance read(@PathVariable("publicId") String publicId) {
    return transformer.apply(
        firstPayloadItem(
            hasPayload(search(Parameters.forIdentity(publicId)).getAllergyIntolerances())
                .getAllergyIntolerance()));
  }

  private CdwAllergyIntolerance103Root search(MultiValueMap<String, String> params) {
    Query<CdwAllergyIntolerance103Root> query =
        Query.forType(CdwAllergyIntolerance103Root.class)
            .profile(Query.Profile.ARGONAUT)
            .resource("AllergyIntolerance")
            .version("1.03")
            .parameters(params)
            .build();
    return hasPayload(mrAndersonClient.search(query));
  }

  /** Search by _id. */
  @GetMapping(params = {"_id"})
  public AllergyIntolerance.Bundle searchById(
      @RequestParam("_id") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "1") @Min(0) int count) {
    return bundle(
        Parameters.builder().add("identifier", id).add("page", page).add("_count", count).build(),
        page,
        count);
  }

  /** Search by identifier. */
  @GetMapping(params = {"identifier"})
  public AllergyIntolerance.Bundle searchByIdentifier(
      @RequestParam("identifier") String identifier,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "1") @Min(0) int count) {
    return bundle(
        Parameters.builder()
            .add("identifier", identifier)
            .add("page", page)
            .add("_count", count)
            .build(),
        page,
        count);
  }

  /** Search by patient. */
  @GetMapping(params = {"patient"})
  public AllergyIntolerance.Bundle searchByPatient(
      @RequestParam("patient") String patient,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "15") @Min(0) int count) {
    return bundle(
        Parameters.builder().add("patient", patient).add("page", page).add("_count", count).build(),
        page,
        count);
  }

  /** Hey, this is a validate endpoint. It validates. */
  @PostMapping(
    value = "/$validate",
    consumes = {"application/json", "application/json+fhir", "application/fhir+json"}
  )
  public OperationOutcome validate(@RequestBody AllergyIntolerance.Bundle bundle) {
    return Validator.create().validate(bundle);
  }

  public interface Transformer
      extends Function<
          CdwAllergyIntolerance103Root.CdwAllergyIntolerances.CdwAllergyIntolerance,
          AllergyIntolerance> {}
}
