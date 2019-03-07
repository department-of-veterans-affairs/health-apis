package gov.va.api.health.dataquery.service.controller.medicationorder;

import static gov.va.api.health.dataquery.service.controller.Transformers.firstPayloadItem;
import static gov.va.api.health.dataquery.service.controller.Transformers.hasPayload;

import gov.va.api.health.dataquery.api.resources.MedicationOrder;
import gov.va.api.health.dataquery.api.resources.MedicationOrder.Bundle;
import gov.va.api.health.dataquery.api.resources.OperationOutcome;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.Bundler.BundleContext;
import gov.va.api.health.dataquery.service.controller.PageLinks.LinkConfig;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.Validator;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient;
import gov.va.api.health.dataquery.service.mranderson.client.Query;
import gov.va.dvp.cdw.xsd.model.CdwMedicationOrder103Root;
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
 * Request Mappings for the Argonaut Medication Order Profile, see
 * https://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-medicationorder.html for
 * implementation details.
 */
@SuppressWarnings("WeakerAccess")
@Validated
@RestController
@RequestMapping(
  value = {"/api/MedicationOrder"},
  produces = {"application/json", "application/json+fhir", "application/fhir+json"}
)
@AllArgsConstructor(onConstructor = @__({@Autowired}))
public class MedicationOrderController {

  private Transformer transformer;
  private MrAndersonClient mrAndersonClient;
  private Bundler bundler;

  private Bundle bundle(MultiValueMap<String, String> parameters, int page, int count) {
    CdwMedicationOrder103Root root = search(parameters);
    LinkConfig linkConfig =
        LinkConfig.builder()
            .path("MedicationOrder")
            .queryParams(parameters)
            .page(page)
            .recordsPerPage(count)
            .totalRecords(root.getRecordCount())
            .build();
    return bundler.bundle(
        BundleContext.of(
            linkConfig,
            root.getMedicationOrders() == null
                ? Collections.emptyList()
                : root.getMedicationOrders().getMedicationOrder(),
            transformer,
            MedicationOrder.Entry::new,
            MedicationOrder.Bundle::new));
  }

  /** Read by identifier. */
  @GetMapping(value = {"/{publicId}"})
  public MedicationOrder read(@PathVariable("publicId") String publicId) {
    return transformer.apply(
        firstPayloadItem(
            hasPayload(search(Parameters.forIdentity(publicId)).getMedicationOrders())
                .getMedicationOrder()));
  }

  private CdwMedicationOrder103Root search(MultiValueMap<String, String> params) {
    Query<CdwMedicationOrder103Root> query =
        Query.forType(CdwMedicationOrder103Root.class)
            .profile(Query.Profile.ARGONAUT)
            .resource("MedicationOrder")
            .version("1.03")
            .parameters(params)
            .build();
    return hasPayload(mrAndersonClient.search(query));
  }

  /** Search by _id. */
  @GetMapping(params = {"_id"})
  public MedicationOrder.Bundle searchById(
      @RequestParam("_id") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "15") @Min(0) int count) {
    return bundle(
        Parameters.builder().add("identifier", id).add("page", page).add("_count", count).build(),
        page,
        count);
  }

  /** Search by identifier. */
  @GetMapping(params = {"identifier"})
  public MedicationOrder.Bundle searchByIdentifier(
      @RequestParam("identifier") String identifier,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "15") @Min(0) int count) {
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
  public MedicationOrder.Bundle searchByPatient(
      @RequestParam("patient") String patient,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "15") @Min(0) int count) {
    return bundle(
        Parameters.builder().add("patient", patient).add("page", page).add("_count", count).build(),
        page,
        count);
  }

  /** Validate Endpoint. */
  @PostMapping(
    value = "/$validate",
    consumes = {"application/json", "application/json+fhir", "application/fhir+json"}
  )
  public OperationOutcome validate(@RequestBody MedicationOrder.Bundle bundle) {
    return Validator.create().validate(bundle);
  }

  public interface Transformer
      extends Function<
          CdwMedicationOrder103Root.CdwMedicationOrders.CdwMedicationOrder, MedicationOrder> {}
}
