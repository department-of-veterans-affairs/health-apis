package gov.va.api.health.dataquery.service.controller.patient;

import static gov.va.api.health.dataquery.service.controller.Transformers.firstPayloadItem;
import static gov.va.api.health.dataquery.service.controller.Transformers.hasPayload;
import static java.util.Arrays.asList;

import gov.va.api.health.argonaut.api.resources.Patient;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.JpaDateTimeParameter;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.Validator;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient;
import gov.va.api.health.dataquery.service.mranderson.client.Query;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.dvp.cdw.xsd.model.CdwPatient103Root;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
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
 * Request Mappings for Patient Profile, see
 * https://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-patient.html for implementation
 * details.
 */
@Slf4j
@Validated
@RestController
@SuppressWarnings("WeakerAccess")
@RequestMapping(
  value = {"Patient", "/api/Patient"},
  produces = {"application/json", "application/json+fhir", "application/fhir+json"}
)
@AllArgsConstructor(onConstructor = @__({@Autowired}))
public class PatientController {
  private Transformer transformer;

  private MrAndersonClient mrAndersonClient;

  private Bundler bundler;

  private WitnessProtection witnessProtection;

  private EntityManager entityManager;

  private static void jpaAddQueryParameters(
      TypedQuery<?> query, MultiValueMap<String, String> parameters) {
    if (parameters.containsKey("family")) {
      query.setParameter("family", "%" + parameters.getFirst("family") + "%");
    }
    if (parameters.containsKey("gender")) {
      query.setParameter("gender", parameters.getFirst("gender"));
    }
    if (parameters.containsKey("given")) {
      query.setParameter("given", "%" + parameters.getFirst("given") + "%");
    }
    if (parameters.containsKey("identifier")) {
      query.setParameter("identifier", parameters.getFirst("identifier"));
    }
    if (parameters.containsKey("name")) {
      query.setParameter("name", "%" + parameters.getFirst("name") + "%");
    }
    if (parameters.containsKey("birthdate")) {
      JpaDateTimeParameter.addQueryParametersForEach(query, parameters.get("birthdate"));
    }
  }

  private static MultiValueMap<String, String> mapFhirGenderToCdw(
      MultiValueMap<String, String> cdwParameters) {
    String fhirGender = cdwParameters.getFirst("gender");
    if (fhirGender == null) {
      return cdwParameters;
    }
    String cdw = GenderMapping.toCdw(fhirGender);
    if (cdw == null) {
      throw new IllegalArgumentException("unknown gender: " + fhirGender);
    }
    MultiValueMap<String, String> newParameters = new LinkedMultiValueMap<>(cdwParameters);
    newParameters.put("gender", asList(cdw));
    return newParameters;
  }

  private Patient.Bundle datamartBundle(
      String query, String totalRecordsQuery, MultiValueMap<String, String> publicParameters) {
    MultiValueMap<String, String> cdwParameters =
        witnessProtection.replacePublicIdsWithCdwIds(publicParameters);
    cdwParameters = mapFhirGenderToCdw(cdwParameters);
    List<PatientEntity> entities = jpaQueryForEntities(query, cdwParameters);
    List<Patient> fhir =
        entities
            .stream()
            .map(entity -> entity.asDatamartPatient())
            .map(dm -> DatamartPatientTransformer.builder().datamart(dm).build().toFhirPatient())
            .collect(Collectors.toList());
    PageLinks.LinkConfig linkConfig =
        PageLinks.LinkConfig.builder()
            .path("Patient")
            .queryParams(publicParameters)
            .page(Integer.parseInt(publicParameters.getOrDefault("page", asList("1")).get(0)))
            .recordsPerPage(
                Integer.parseInt(publicParameters.getOrDefault("_count", asList("15")).get(0)))
            .totalRecords(jpaQueryForTotalRecords(totalRecordsQuery, cdwParameters))
            .build();
    return bundler.bundle(
        Bundler.BundleContext.of(
            linkConfig, fhir, Function.identity(), Patient.Entry::new, Patient.Bundle::new));
  }

  private Patient datamartRead(String publicId) {
    MultiValueMap<String, String> publicParameters = Parameters.forIdentity(publicId);
    MultiValueMap<String, String> cdwParameters =
        witnessProtection.replacePublicIdsWithCdwIds(publicParameters);

    PatientEntity entity =
        entityManager.find(PatientEntity.class, cdwParameters.getFirst("identifier"));
    if (entity == null) {
      return null;
    }

    return DatamartPatientTransformer.builder()
        .datamart(entity.asDatamartPatient())
        .build()
        .toFhirPatient();
  }

  private List<PatientEntity> jpaQueryForEntities(
      String queryString, MultiValueMap<String, String> cdwParameters) {
    TypedQuery<PatientEntity> query = entityManager.createQuery(queryString, PatientEntity.class);
    jpaAddQueryParameters(query, cdwParameters);
    int page = Integer.parseInt(cdwParameters.getOrDefault("page", asList("1")).get(0));
    int count = Integer.parseInt(cdwParameters.getOrDefault("_count", asList("15")).get(0));
    query.setFirstResult((page - 1) * count);
    query.setMaxResults(count);
    List<PatientEntity> results = query.getResultList();
    log.info(
        "For parameters {} and query '{}', found entities with IDs {}.",
        cdwParameters,
        queryString,
        results.stream().map(entity -> entity.icn()).collect(Collectors.toList()));
    return results;
  }

  private int jpaQueryForTotalRecords(
      String queryString, MultiValueMap<String, String> cdwParameters) {
    TypedQuery<Long> query = entityManager.createQuery(queryString, Long.class);
    jpaAddQueryParameters(query, cdwParameters);
    int totalRecords = query.getSingleResult().intValue();
    log.info(
        "For parameters {} and query '{}', found {} total records.",
        cdwParameters,
        queryString,
        totalRecords);
    return totalRecords;
  }

  private Patient.Bundle mrAndersonBundle(MultiValueMap<String, String> parameters) {
    CdwPatient103Root root = mrAndersonSearch(parameters);
    PageLinks.LinkConfig linkConfig =
        PageLinks.LinkConfig.builder()
            .path("Patient")
            .queryParams(parameters)
            .page(Integer.parseInt(parameters.getOrDefault("page", asList("1")).get(0)))
            .recordsPerPage(
                Integer.parseInt(parameters.getOrDefault("_count", asList("15")).get(0)))
            .totalRecords(root.getRecordCount())
            .build();
    return bundler.bundle(
        Bundler.BundleContext.of(
            linkConfig,
            root.getPatients() == null ? Collections.emptyList() : root.getPatients().getPatient(),
            transformer,
            Patient.Entry::new,
            Patient.Bundle::new));
  }

  private CdwPatient103Root mrAndersonSearch(MultiValueMap<String, String> params) {
    Query<CdwPatient103Root> query =
        Query.forType(CdwPatient103Root.class)
            .profile(Query.Profile.ARGONAUT)
            .resource("Patient")
            .version("1.03")
            .parameters(params)
            .build();
    return hasPayload(mrAndersonClient.search(query));
  }

  /** Read by id. */
  @SneakyThrows
  @GetMapping(value = {"/{publicId}"})
  public Patient read(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamart,
      @PathVariable("publicId") String publicId) {
    if (BooleanUtils.isTrue(BooleanUtils.toBooleanObject(datamart))) {
      return datamartRead(publicId);
    }

    StopWatch mraWatch = StopWatch.createStarted();
    Patient mrAndersonPatient =
        transformer.apply(
            firstPayloadItem(
                hasPayload(mrAndersonSearch(Parameters.forIdentity(publicId)).getPatients())
                    .getPatient()));
    mraWatch.stop();

    if ("both".equalsIgnoreCase(datamart)) {
      StopWatch datamartWatch = StopWatch.createStarted();
      Patient datamartPatient = datamartRead(publicId);
      datamartWatch.stop();
      log.info(
          "mr-anderson took {} millis and datamart took {} millis."
              + " mr-anderson is {} and datamart is {}",
          mraWatch.getTime(),
          datamartWatch.getTime(),
          JacksonConfig.createMapper().writeValueAsString(mrAndersonPatient),
          JacksonConfig.createMapper().writeValueAsString(datamartPatient));
    }

    return mrAndersonPatient;
  }

  @SneakyThrows
  private Patient.Bundle search(
      String datamart,
      String query,
      String totalRecordsQuery,
      MultiValueMap<String, String> parameters) {
    if (BooleanUtils.isTrue(BooleanUtils.toBooleanObject(datamart))) {
      return datamartBundle(query, totalRecordsQuery, parameters);
    }

    StopWatch mraWatch = StopWatch.createStarted();
    Patient.Bundle mrAndersonBundle = mrAndersonBundle(parameters);
    mraWatch.stop();

    if ("both".equalsIgnoreCase(datamart)) {
      StopWatch datamartWatch = StopWatch.createStarted();
      Patient.Bundle datamartBundle = datamartBundle(query, totalRecordsQuery, parameters);
      datamartWatch.stop();
      log.info(
          "mr-anderson took {} millis and datamart took {} millis."
              + " {} mr-anderson results, {} datamart results."
              + " mr-anderson bundle is {} and datamart bundle is {}",
          mraWatch.getTime(),
          datamartWatch.getTime(),
          mrAndersonBundle.total(),
          datamartBundle.total(),
          JacksonConfig.createMapper().writeValueAsString(mrAndersonBundle),
          JacksonConfig.createMapper().writeValueAsString(datamartBundle));
    }

    return mrAndersonBundle;
  }

  /** Search by Family+Gender. */
  @GetMapping(params = {"family", "gender"})
  public Patient.Bundle searchByFamilyAndGender(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamart,
      @RequestParam("family") String family,
      @RequestParam("gender") String gender,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "15") @Min(0) int count) {
    return search(
        datamart,
        "Select p from PatientEntity p"
            + " where p.search.lastName like :family and p.search.gender is :gender",
        "Select count(p.icn) from PatientEntity p"
            + " where p.search.lastName like :family and p.search.gender is :gender",
        Parameters.builder()
            .add("family", family)
            .add("gender", gender)
            .add("page", page)
            .add("_count", count)
            .build());
  }

  /** Search by Given+Gender. */
  @GetMapping(params = {"given", "gender"})
  public Patient.Bundle searchByGivenAndGender(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamart,
      @RequestParam("given") String given,
      @RequestParam("gender") String gender,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "15") @Min(0) int count) {
    return search(
        datamart,
        "Select p from PatientEntity p where"
            + " p.search.firstName like :given and p.search.gender is :gender",
        "Select count(p.icn) from PatientEntity p where"
            + " p.search.firstName like :given and p.search.gender is :gender",
        Parameters.builder()
            .add("given", given)
            .add("gender", gender)
            .add("page", page)
            .add("_count", count)
            .build());
  }

  /** Search by _id. */
  @GetMapping(params = {"_id"})
  public Patient.Bundle searchById(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamart,
      @RequestParam("_id") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "1") @Min(0) int count) {
    return searchByIdentifier(datamart, id, page, count);
  }

  /** Search by Identifier. */
  @GetMapping(params = {"identifier"})
  public Patient.Bundle searchByIdentifier(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamart,
      @RequestParam("identifier") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "1") @Min(0) int count) {
    return search(
        datamart,
        "Select p from PatientEntity p where p.icn is :identifier",
        "Select count(p.icn) from PatientEntity p where p.icn is :identifier",
        Parameters.builder().add("identifier", id).add("page", page).add("_count", count).build());
  }

  /** Search by Name+Birthdate. */
  @GetMapping(params = {"name", "birthdate"})
  public Patient.Bundle searchByNameAndBirthdate(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamart,
      @RequestParam("name") String name,
      @RequestParam("birthdate") String[] birthdate,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "15") @Min(0) int count) {
    return search(
        datamart,
        "Select p from PatientEntity p where p.search.name like :name"
            + JpaDateTimeParameter.querySnippet(
                birthdate, "p.search.birthDateTime", "p.search.birthDateTime"),
        "Select count(p.icn) from PatientEntity p where p.search.name like :name"
            + JpaDateTimeParameter.querySnippet(
                birthdate, "p.search.birthDateTime", "p.search.birthDateTime"),
        Parameters.builder()
            .add("name", name)
            .addAll("birthdate", birthdate)
            .add("page", page)
            .add("_count", count)
            .build());
  }

  /** Search by Name+Gender. */
  @GetMapping(params = {"name", "gender"})
  public Patient.Bundle searchByNameAndGender(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamart,
      @RequestParam("name") String name,
      @RequestParam("gender") String gender,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "15") @Min(0) int count) {
    return search(
        datamart,
        "Select p from PatientEntity p where"
            + " p.search.name like :name and p.search.gender is :gender",
        "Select count(p.icn) from PatientEntity p where"
            + " p.search.name like :name and p.search.gender is :gender",
        Parameters.builder()
            .add("name", name)
            .add("gender", gender)
            .add("page", page)
            .add("_count", count)
            .build());
  }

  /** Hey, this is a validate endpoint. It validates. */
  @PostMapping(
    value = "/$validate",
    consumes = {"application/json", "application/json+fhir", "application/fhir+json"}
  )
  public OperationOutcome validate(@RequestBody Patient.Bundle bundle) {
    return Validator.create().validate(bundle);
  }

  public interface Transformer
      extends Function<CdwPatient103Root.CdwPatients.CdwPatient, Patient> {}
}
