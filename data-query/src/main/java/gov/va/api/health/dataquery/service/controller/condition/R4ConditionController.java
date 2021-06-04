package gov.va.api.health.dataquery.service.controller.condition;

import static gov.va.api.lighthouse.vulcan.Rules.ifParameter;
import static gov.va.api.lighthouse.vulcan.Rules.parametersNeverSpecifiedTogether;
import static gov.va.api.lighthouse.vulcan.Specifications.strings;
import static gov.va.api.lighthouse.vulcan.Vulcan.returnNothing;

import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.CompositeCdwIds;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.vulcanizer.Bundling;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedBundler;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedReader;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedTransformation;
import gov.va.api.health.r4.api.resources.Condition;
import gov.va.api.lighthouse.datamart.CompositeCdwId;
import gov.va.api.lighthouse.vulcan.Specifications;
import gov.va.api.lighthouse.vulcan.Vulcan;
import gov.va.api.lighthouse.vulcan.VulcanConfiguration;
import gov.va.api.lighthouse.vulcan.mappings.Mappings;
import gov.va.api.lighthouse.vulcan.mappings.TokenParameter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
 * Request Mappings for Condition Profile.
 *
 * @implSpec https://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-condition.html
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

  // ToDo ADD code
  // ToDo ADD onset-date
  // ToDo UPDATE _id to token
  // ToDo UPDATE identifier to token
  // ToDo UPDATE patient to reference
  private VulcanConfiguration<ConditionEntity> configuration() {
    return VulcanConfiguration.forEntity(ConditionEntity.class)
        .paging(
            linkProperties.pagingConfiguration("Condition", ConditionEntity.naturalOrder(), null))
        .mappings(
            Mappings.forEntity(ConditionEntity.class)
                .tokens(
                    "category", this::tokenCategoryIsSupported, this::tokenCategorySpecification)
                .tokens(
                    "clinical-status",
                    this::tokenClinicalStatusIsSupported,
                    this::tokenClinicalStatusSpecification)
                .values("_id", this::loadCdwId)
                .values("identifier", this::loadCdwId)
                .value("patient", "icn")
                .get())
        .rules(
            List.of(
                parametersNeverSpecifiedTogether("_id", "identifier", "patient"),
                ifParameter("category").thenAlsoAtLeastOneParameterOf("patient"),
                ifParameter("clinical-status").thenAlsoAtLeastOneParameterOf("patient")))
        .defaultQuery(returnNothing())
        .build();
  }

  private Map<String, ?> loadCdwId(String publicId) {
    String cdwId = witnessProtection.toCdwId(publicId);
    try {
      CompositeCdwId compositeCdwId = CompositeCdwId.fromCdwId(cdwId);
      return Map.of(
          "cdwIdNumber",
          compositeCdwId.cdwIdNumber(),
          "cdwIdResourceCode",
          compositeCdwId.cdwIdResourceCode());
    } catch (IllegalArgumentException e) {
      return Map.of();
    }
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
   * <pre>
   * Datamart: problem | diagnosis
   * R4: problem-list-item | encounter-diagnosis
   * health-concern not currently supported
   * </pre>
   */
  private String toDatamartCategoryValue(String category) {
    switch (category) {
      case "problem-list-item":
        return DatamartCondition.Category.problem.toString();
      case "encounter-diagnosis":
        return DatamartCondition.Category.diagnosis.toString();
      default:
        throw new IllegalStateException("Unsupported category code value: " + category);
    }
  }

  private String toDatamartClinicalStatusValue(String clinicalStatus) {
    switch (clinicalStatus) {
      case "active":
        return DatamartCondition.ClinicalStatus.active.toString();
      case "inactive":
        // fall-through
      case "resolved":
        return DatamartCondition.ClinicalStatus.resolved.toString();
      default:
        throw new IllegalStateException("Unsupported clinical-status value: " + clinicalStatus);
    }
  }

  /**
   * Supported Categories.
   *
   * <pre>
   * problem-list-item
   * encounter-diagnosis
   * http://terminology.hl7.org/CodeSystem/condition-category|
   * http://terminology.hl7.org/CodeSystem/condition-category|problem-list-item
   * http://terminology.hl7.org/CodeSystem/condition-category|encounter-diagnosis
   * </pre>
   */
  private boolean tokenCategoryIsSupported(TokenParameter token) {
    boolean codeIsSupported = token.hasSupportedCode("problem-list-item", "encounter-diagnosis");
    return (token.hasSupportedSystem(CONDITION_CATEGORY_SYSTEM)
            && (token.hasAnyCode() || codeIsSupported))
        || (token.hasAnySystem() && codeIsSupported);
  }

  private Specification<ConditionEntity> tokenCategorySpecification(TokenParameter token) {
    return token
        .behavior()
        .onExplicitSystemAndAnyCode(
            s ->
                Specifications.<ConditionEntity>selectInList(
                    "category", strings(DatamartCondition.Category.class)))
        .onExplicitSystemAndExplicitCode(
            (s, c) ->
                Specifications.<ConditionEntity>select("category", toDatamartCategoryValue(c)))
        .onAnySystemAndExplicitCode(
            c -> Specifications.<ConditionEntity>select("category", toDatamartCategoryValue(c)))
        .build()
        .execute();
  }

  /**
   * Supported Clinical Statuses.
   *
   * <pre>
   * active
   * resolved
   * inactive (becomes resolved)
   * http://terminology.hl7.org/CodeSystem/condition-clinical|
   * http://terminology.hl7.org/CodeSystem/condition-clinical|active
   * http://terminology.hl7.org/CodeSystem/condition-clinical|resolved
   * http://terminology.hl7.org/CodeSystem/condition-clinical|inactive (becomes resolved)
   * </pre>
   */
  private boolean tokenClinicalStatusIsSupported(TokenParameter token) {
    boolean codeIsSupported = token.hasSupportedCode("active", "resolved", "inactive");
    return (token.hasSupportedSystem(CONDITION_CLINICAL_STATUS_SYSTEM)
            && (token.hasAnyCode() || codeIsSupported))
        || (token.hasAnySystem() && codeIsSupported);
  }

  private Specification<ConditionEntity> tokenClinicalStatusSpecification(TokenParameter token) {
    return token
        .behavior()
        .onExplicitSystemAndAnyCode(
            s ->
                Specifications.<ConditionEntity>selectInList(
                    "clinicalStatus", strings(DatamartCondition.ClinicalStatus.class)))
        .onExplicitSystemAndExplicitCode(
            (s, c) ->
                Specifications.<ConditionEntity>select(
                    "clinicalStatus", toDatamartClinicalStatusValue(c)))
        .onAnySystemAndExplicitCode(
            c ->
                Specifications.<ConditionEntity>select(
                    "clinicalStatus", toDatamartClinicalStatusValue(c)))
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

  VulcanizedReader<ConditionEntity, DatamartCondition, Condition, CompositeCdwId>
      vulcanizedReader() {
    return VulcanizedReader
        .<ConditionEntity, DatamartCondition, Condition, CompositeCdwId>forTransformation(
            transformation())
        .repository(repository)
        .toPatientId(e -> Optional.of(e.icn()))
        .toPrimaryKey(CompositeCdwIds::requireCompositeIdStringFormat)
        .toPayload(ConditionEntity::payload)
        .build();
  }
}
