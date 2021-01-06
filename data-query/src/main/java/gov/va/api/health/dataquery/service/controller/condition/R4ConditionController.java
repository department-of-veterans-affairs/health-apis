package gov.va.api.health.dataquery.service.controller.condition;

import static gov.va.api.lighthouse.vulcan.Rules.ifParameter;
import static gov.va.api.lighthouse.vulcan.Rules.parametersNeverSpecifiedTogether;
import static gov.va.api.lighthouse.vulcan.Vulcan.returnNothing;

import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.vulcanizer.Bundling;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedBundler;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedReader;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedTransformation;
import gov.va.api.health.r4.api.resources.Condition;
import gov.va.api.lighthouse.vulcan.Vulcan;
import gov.va.api.lighthouse.vulcan.VulcanConfiguration;
import gov.va.api.lighthouse.vulcan.mappings.Mappings;
import gov.va.api.lighthouse.vulcan.mappings.TokenParameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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
 * Request Mappings for Condition Profile, see
 * https://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-condition.html for
 * implementation details.
 */
@Validated
@RestController
@RequestMapping(
    value = {"/r4/Condition"},
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor_ = @Autowired)
public class R4ConditionController {
  private static final String CONDITION_CATEGORY_SYSTEM =
      "http://terminology.hl7.org/CodeSystem/condition-category";

  private static final String CONDITION_CLINICAL_STATUS_SYSTEM =
      "http://terminology.hl7.org/CodeSystem/condition-clinical";

  private WitnessProtection witnessProtection;

  private ConditionRepository repository;

  private LinkProperties linkProperties;

  private String clinicalStatusFor(String clinicalStatusCode) {
    return "inactive".equals(clinicalStatusCode) ? "resolved" : clinicalStatusCode;
  }

  // ToDo ADD code
  // ToDo ADD onset-date
  // ToDo UPDATE _id to token
  // ToDo UPDATE identifier to token
  // ToDo UPDATE patient to reference
  private VulcanConfiguration<ConditionEntity> configuration() {
    return VulcanConfiguration.forEntity(ConditionEntity.class)
        .paging(linkProperties.pagingConfiguration("Condition", ConditionEntity.naturalOrder()))
        .mappings(
            Mappings.forEntity(ConditionEntity.class)
                .token("category", this::tokenCategoryIsSupported, this::tokenCategoryValues)
                .tokenList(
                    "clinical-status",
                    "clinicalStatus",
                    this::tokenClinicalStatusIsSupported,
                    this::tokenClinicalStatusValues)
                .value("_id", "cdwId", witnessProtection::toCdwId)
                .value("identifier", "cdwId", witnessProtection::toCdwId)
                .value("patient", "icn")
                .get())
        .rule(parametersNeverSpecifiedTogether("_id", "identifier", "patient"))
        .rule(ifParameter("category").thenAlsoAtLeastOneParameterOf("patient"))
        .rule(ifParameter("clinical-status").thenAlsoAtLeastOneParameterOf("patient"))
        .defaultQuery(returnNothing())
        .build();
  }

  /** Read Condition by id. */
  @GetMapping(value = {"/{publicId}"})
  public Condition read(@PathVariable("publicId") String publicId) {
    return vulcanizedReader().read(publicId);
  }

  /** Get raw DatamartCondition by id. */
  @GetMapping(
      value = {"/{publicId}"},
      headers = {"raw=true"})
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    return vulcanizedReader().readRaw(publicId, response);
  }

  /** US-Core-R4 Condition Search Support. */
  @GetMapping
  public Condition.Bundle search(HttpServletRequest request) {
    return Vulcan.forRepo(repository)
        .config(configuration())
        .build()
        .search(request)
        .map(toBundle());
  }

  VulcanizedBundler<
          ConditionEntity, DatamartCondition, Condition, Condition.Entry, Condition.Bundle>
      toBundle() {
    return VulcanizedBundler.forTransformation(transformation())
        .bundling(
            Bundling.newBundle(Condition.Bundle::new)
                .newEntry(Condition.Entry::new)
                .linkProperties(linkProperties)
                .build())
        .build();
  }

  /**
   * Transforms category from an R4 code to a datamart code that can be searched in the database.
   *
   * <p>Datamart: problem | diagnosis
   *
   * <p>R4: problem-list-item | encounter-diagnosis
   *
   * <p>health-concern not currently supported
   */
  private String toDatamartCategory(String category) {
    switch (category) {
      case "problem-list-item":
        return DatamartCondition.Category.problem.toString();
      case "encounter-diagnosis":
        return DatamartCondition.Category.diagnosis.toString();
      default:
        throw new IllegalStateException("Unsupported category code value: " + category);
    }
  }

  /**
   * Supported Categories:
   *
   * <p>problem-list-item
   *
   * <p>encounter-diagnosis
   *
   * <p>http://terminology.hl7.org/CodeSystem/condition-category|
   *
   * <p>http://terminology.hl7.org/CodeSystem/condition-category|problem-list-item
   *
   * <p>http://terminology.hl7.org/CodeSystem/condition-category|encounter-diagnosis
   */
  private boolean tokenCategoryIsSupported(TokenParameter token) {
    return (token.hasSupportedSystem(CONDITION_CATEGORY_SYSTEM)
            && (token.hasAnyCode()
                || token.hasSupportedCode("problem-list-item", "encounter-diagnosis")))
        || token.hasAnySystem();
  }

  private Collection<String> tokenCategoryValues(TokenParameter token) {
    return token
        .behavior()
        .onExplicitSystemAndAnyCode(
            s ->
                Arrays.stream(DatamartCondition.Category.values())
                    .map(Enum::name)
                    .collect(Collectors.toList()))
        .onExplicitSystemAndExplicitCode((s, c) -> List.of(toDatamartCategory(c)))
        .onAnySystemAndExplicitCode(c -> List.of(toDatamartCategory(c)))
        .build()
        .execute();
  }

  /**
   * Supported Clinical Statuses:
   *
   * <p>active
   *
   * <p>resolved
   *
   * <p>inactive (becomes resolved)
   *
   * <p>http://terminology.hl7.org/CodeSystem/condition-clinical|
   *
   * <p>http://terminology.hl7.org/CodeSystem/condition-clinical|active
   *
   * <p>http://terminology.hl7.org/CodeSystem/condition-clinical|resolved
   *
   * <p>http://terminology.hl7.org/CodeSystem/condition-clinical|inactive (becomes resolved)
   */
  private boolean tokenClinicalStatusIsSupported(TokenParameter token) {
    return (token.hasSupportedSystem(CONDITION_CLINICAL_STATUS_SYSTEM)
            && (token.hasAnyCode() || token.hasSupportedCode("active", "resolved", "inactive")))
        || token.hasAnySystem();
  }

  private Collection<String> tokenClinicalStatusValues(TokenParameter token) {
    return token
        .behavior()
        .onExplicitSystemAndAnyCode(s -> List.of("active", "resolved"))
        .onExplicitSystemAndExplicitCode((s, c) -> List.of(clinicalStatusFor(c)))
        .onAnySystemAndExplicitCode(c -> List.of(clinicalStatusFor(c)))
        .build()
        .execute();
  }

  VulcanizedTransformation<ConditionEntity, DatamartCondition, Condition> transformation() {
    return VulcanizedTransformation.toDatamart(ConditionEntity::asDatamartCondition)
        .toResource(dm -> R4ConditionTransformer.builder().datamart(dm).build().toFhir())
        .witnessProtection(witnessProtection)
        .replaceReferences(
            resource -> Stream.of(resource.patient(), resource.asserter().orElse(null)))
        .build();
  }

  VulcanizedReader<ConditionEntity, DatamartCondition, Condition> vulcanizedReader() {
    return VulcanizedReader.<ConditionEntity, DatamartCondition, Condition>forTransformation(
            transformation())
        .repository(repository)
        .toPatientId(e -> Optional.of(e.icn()))
        .toPayload(ConditionEntity::payload)
        .build();
  }
}
