package gov.va.api.health.dataquery.service.controller.medicationstatement;

import static gov.va.api.health.dataquery.service.controller.Transformers.firstPayloadItem;
import static gov.va.api.health.dataquery.service.controller.Transformers.hasPayload;
import static java.util.Collections.emptyList;

import gov.va.api.health.argonaut.api.resources.MedicationStatement;
import gov.va.api.health.argonaut.api.resources.MedicationStatement.Bundle;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.Bundler.BundleContext;
import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.PageLinks.LinkConfig;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions.NotFound;
import gov.va.api.health.dataquery.service.controller.Validator;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient;
import gov.va.api.health.dataquery.service.mranderson.client.Query;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.dvp.cdw.xsd.model.CdwMedicationStatement102Root;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Request Mappings for Medication Statement Profile, see
 * https://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-medicationstatement.html for
 * implementation details.
 */
@SuppressWarnings("WeakerAccess")
@Slf4j
@Validated
@RestController
@RequestMapping(
  value = {"MedicationStatement", "/api/MedicationStatement"},
  produces = {"application/json", "application/json+fhir", "application/fhir+json"}
)
public class MedicationStatementController {

  private final Datamart datamart = new Datamart();

  private Transformer transformer;

  private MrAndersonClient mrAndersonClient;

  private Bundler bundler;

  private WitnessProtection witnessProtection;

  private MedicationStatementRepository repository;

  private boolean defaultToDatamart;

  /** Spring constructor. */
  @SuppressWarnings("ParameterHidesMemberVariable")
  public MedicationStatementController(
      @Value("${datamart.medication-statement}") boolean defaultToDatamart,
      @Autowired MedicationStatementController.Transformer transformer,
      @Autowired MrAndersonClient mrAndersonClient,
      @Autowired Bundler bundler,
      @Autowired MedicationStatementRepository repository,
      @Autowired WitnessProtection witnessProtection) {
    this.defaultToDatamart = defaultToDatamart;
    this.transformer = transformer;
    this.mrAndersonClient = mrAndersonClient;
    this.bundler = bundler;
    this.repository = repository;
    this.witnessProtection = witnessProtection;
  }

  private MedicationStatement.Bundle bundle(
      MultiValueMap<String, String> parameters, int page, int count) {
    CdwMedicationStatement102Root root = search(parameters);
    LinkConfig linkConfig =
        LinkConfig.builder()
            .path("MedicationStatement")
            .queryParams(parameters)
            .page(page)
            .recordsPerPage(count)
            .totalRecords(root.getRecordCount().intValue())
            .build();
    return bundler.bundle(
        BundleContext.of(
            linkConfig,
            root.getMedicationStatements() == null
                ? emptyList()
                : root.getMedicationStatements().getMedicationStatement(),
            transformer,
            MedicationStatement.Entry::new,
            MedicationStatement.Bundle::new));
  }

  /** Read by id. */
  @GetMapping(value = {"/{publicId}"})
  public MedicationStatement read(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamartHeader,
      @PathVariable("publicId") String publicId) {
    if (datamart.isDatamartRequest(datamartHeader)) {
      return datamart.read(publicId);
    }
    return transformer.apply(
        firstPayloadItem(
            hasPayload(search(Parameters.forIdentity(publicId)).getMedicationStatements())
                .getMedicationStatement()));
  }

  /** Read by id, raw data. */
  @GetMapping(
    value = {"/{publicId}"},
    headers = {"raw=true"}
  )
  public String readRaw(@PathVariable("publicId") String publicId) {
    return datamart.readRaw(publicId);
  }

  private CdwMedicationStatement102Root search(MultiValueMap<String, String> params) {
    Query<CdwMedicationStatement102Root> query =
        Query.forType(CdwMedicationStatement102Root.class)
            .profile(Query.Profile.ARGONAUT)
            .resource("MedicationStatement")
            .version("1.02")
            .parameters(params)
            .build();
    return hasPayload(mrAndersonClient.search(query));
  }

  /** Search by _id. */
  @GetMapping(params = {"_id"})
  public MedicationStatement.Bundle searchById(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamartHeader,
      @RequestParam("_id") String publicId,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    if (datamart.isDatamartRequest(datamartHeader)) {
      return datamart.searchById(publicId, page, count);
    }
    return bundle(
        Parameters.builder()
            .add("identifier", publicId)
            .add("page", page)
            .add("_count", count)
            .build(),
        page,
        count);
  }

  /** Search by Identifier. */
  @GetMapping(params = {"identifier"})
  public MedicationStatement.Bundle searchByIdentifier(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamartHeader,
      @RequestParam("identifier") String publicId,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return searchById(datamartHeader, publicId, page, count);
  }

  /** Search by patient. */
  @GetMapping(params = {"patient"})
  public MedicationStatement.Bundle searchByPatient(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamartHeader,
      @RequestParam("patient") String patient,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    if (datamart.isDatamartRequest(datamartHeader)) {
      return datamart.searchByPatient(patient, page, count);
    }
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
  public OperationOutcome validate(@RequestBody MedicationStatement.Bundle bundle) {
    return Validator.create().validate(bundle);
  }

  public interface Transformer
      extends Function<
          CdwMedicationStatement102Root.CdwMedicationStatements.CdwMedicationStatement,
          MedicationStatement> {}

  /**
   * This class is being used to help organize the code such that all the datamart logic is
   * contained together. In the future when Mr. Anderson support is dropped, this class can be
   * eliminated.
   */
  private class Datamart {

    private Bundle bundle(
        MultiValueMap<String, String> parameters,
        List<MedicationStatement> reports,
        int totalRecords) {
      PageLinks.LinkConfig linkConfig =
          PageLinks.LinkConfig.builder()
              .path("MedicationStatement")
              .queryParams(parameters)
              .page(Parameters.pageOf(parameters))
              .recordsPerPage(Parameters.countOf(parameters))
              .totalRecords(totalRecords)
              .build();
      return bundler.bundle(
          Bundler.BundleContext.of(
              linkConfig,
              reports,
              Function.identity(),
              MedicationStatement.Entry::new,
              MedicationStatement.Bundle::new));
    }

    private Bundle bundle(
        MultiValueMap<String, String> parameters,
        int count,
        Page<MedicationStatementEntity> entities) {
      log.info("Search {} found {} results", parameters, entities.getTotalElements());
      if (count == 0) {
        return bundle(parameters, emptyList(), (int) entities.getTotalElements());
      }
      return bundle(
          parameters,
          replaceReferences(
                  entities
                      .get()
                      .map(MedicationStatementEntity::asDatamartMedicationStatement)
                      .collect(Collectors.toList()))
              .stream()
              .map(this::transform)
              .collect(Collectors.toList()),
          (int) entities.getTotalElements());
    }

    MedicationStatementEntity findById(String publicId) {
      Optional<MedicationStatementEntity> entity =
          repository.findById(witnessProtection.toCdwId(publicId));
      return entity.orElseThrow(() -> new NotFound(publicId));
    }

    boolean isDatamartRequest(String datamartHeader) {
      if (StringUtils.isBlank(datamartHeader)) {
        return defaultToDatamart;
      }
      return BooleanUtils.isTrue(BooleanUtils.toBooleanObject(datamartHeader));
    }

    private PageRequest page(int page, int count) {
      return PageRequest.of(
          page - 1, count == 0 ? 1 : count, MedicationStatementEntity.naturalOrder());
    }

    MedicationStatement read(String publicId) {
      DatamartMedicationStatement medicationStatement =
          findById(publicId).asDatamartMedicationStatement();
      replaceReferences(List.of(medicationStatement));
      return transform(medicationStatement);
    }

    String readRaw(String publicId) {
      return findById(publicId).payload();
    }

    Collection<DatamartMedicationStatement> replaceReferences(
        Collection<DatamartMedicationStatement> resources) {
      witnessProtection.registerAndUpdateReferences(
          resources, resource -> Stream.of(resource.patient(), resource.medication()));
      return resources;
    }

    Bundle searchById(String publicId, int page, int count) {
      MedicationStatement resource = read(publicId);
      return bundle(
          Parameters.builder()
              .add("identifier", publicId)
              .add("page", page)
              .add("_count", count)
              .build(),
          resource == null || count == 0 ? emptyList() : List.of(resource),
          resource == null ? 0 : 1);
    }

    Bundle searchByPatient(String patient, int page, int count) {
      String icn = witnessProtection.toCdwId(patient);
      log.info("Looking for {} ({})", patient, icn);
      return bundle(
          Parameters.builder()
              .add("patient", patient)
              .add("page", page)
              .add("_count", count)
              .build(),
          count,
          repository.findByIcn(icn, page(page, count)));
    }

    MedicationStatement transform(DatamartMedicationStatement dm) {
      return DatamartMedicationStatementTransformer.builder().datamart(dm).build().toFhir();
    }
  }
}
