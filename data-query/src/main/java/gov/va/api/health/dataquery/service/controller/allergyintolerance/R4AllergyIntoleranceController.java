package gov.va.api.health.dataquery.service.controller.allergyintolerance;

import static gov.va.api.lighthouse.vulcan.Rules.parametersNeverSpecifiedTogether;
import static gov.va.api.lighthouse.vulcan.Vulcan.returnNothing;

import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.vulcanizer.Bundling;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedBundler;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedReader;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedTransformation;
import gov.va.api.health.r4.api.resources.AllergyIntolerance;
import gov.va.api.lighthouse.vulcan.InvalidRequest;
import gov.va.api.lighthouse.vulcan.Rule;
import gov.va.api.lighthouse.vulcan.Vulcan;
import gov.va.api.lighthouse.vulcan.VulcanConfiguration;
import gov.va.api.lighthouse.vulcan.mappings.Mappings;
import gov.va.api.lighthouse.vulcan.mappings.ReferenceParameter;
import java.util.Optional;
import java.util.Set;
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
 * Request Mappings for Allergy Intolerance Profile, see
 * https://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-allergyintolerance.html for
 * implementation details.
 */
@Validated
@RestController
@RequestMapping(
    value = "/r4/AllergyIntolerance",
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor_ = @Autowired)
public class R4AllergyIntoleranceController {
  private final LinkProperties linkProperties;

  private final WitnessProtection witnessProtection;

  private final AllergyIntoleranceRepository repository;

  private VulcanConfiguration<AllergyIntoleranceEntity> configuration() {
    return VulcanConfiguration.<AllergyIntoleranceEntity>forEntity(AllergyIntoleranceEntity.class)
        .paging(
            linkProperties.pagingConfiguration(
                "AllergyIntolerance", AllergyIntoleranceEntity.naturalOrder()))
        .mappings(
            Mappings.forEntity(AllergyIntoleranceEntity.class)
                .value("_id", "cdwId", witnessProtection::toCdwId)
                .value("identifier", "cdwId", witnessProtection::toCdwId)
                .reference(
                    "patient",
                    "icn",
                    referencePatientSupportedResourceTypes(),
                    "Patient",
                    this::referencePatientIsSupported,
                    this::referencePatientValues)
                .get())
        .defaultQuery(returnNothing())
        .rule(parametersNeverSpecifiedTogether("patient", "_id", "identifier"))
        .rule(forbidUnknownResourceModifiersOnPatientParameter())
        .build();
  }

  private Rule forbidUnknownResourceModifiersOnPatientParameter() {
    return (ctx) -> {
      var patientParamWithModifier = "patient:";
      ctx.request().getParameterMap().keySet().stream()
          .filter(param -> param.contains(patientParamWithModifier))
          .map(param -> param.substring(patientParamWithModifier.length()))
          .forEach(
              modifier -> {
                if (!referencePatientSupportedResourceTypes().contains(modifier)) {
                  throw InvalidRequest.because(
                      "Modifier not allowed for patient reference searches: " + modifier);
                }
              });
    };
  }

  /** Read Support. */
  @GetMapping(value = {"/{publicId}"})
  public AllergyIntolerance read(@PathVariable("publicId") String publicId) {
    return vulcanizedReader().read(publicId);
  }

  /** Read Raw Datamart Payload Support. */
  @GetMapping(
      value = "/{publicId}",
      headers = {"raw=true"})
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    return vulcanizedReader().readRaw(publicId, response);
  }

  private boolean referencePatientIsSupported(ReferenceParameter reference) {
    // Only support R4 Patient Read urls
    if (reference.url().isPresent()) {
      var allowedUrl = linkProperties.r4().readUrl("Patient", reference.publicId());
      return reference.url().get().equals(allowedUrl);
    }
    return "PATIENT".equalsIgnoreCase(reference.type());
  }

  private Set<String> referencePatientSupportedResourceTypes() {
    return Set.of("Patient");
  }

  private String referencePatientValues(ReferenceParameter reference) {
    return reference.publicId();
  }

  /** Search Support. */
  @GetMapping
  public AllergyIntolerance.Bundle search(HttpServletRequest request) {
    return Vulcan.forRepo(repository)
        .config(configuration())
        .build()
        .search(request)
        .map(toBundle());
  }

  VulcanizedBundler<
          AllergyIntoleranceEntity,
          DatamartAllergyIntolerance,
          AllergyIntolerance,
          AllergyIntolerance.Entry,
          AllergyIntolerance.Bundle>
      toBundle() {
    return VulcanizedBundler.forTransformation(transformation())
        .bundling(
            Bundling.newBundle(AllergyIntolerance.Bundle::new)
                .newEntry(AllergyIntolerance.Entry::new)
                .linkProperties(linkProperties)
                .build())
        .build();
  }

  private VulcanizedTransformation<
          AllergyIntoleranceEntity, DatamartAllergyIntolerance, AllergyIntolerance>
      transformation() {
    return VulcanizedTransformation.toDatamart(
            AllergyIntoleranceEntity::asDatamartAllergyIntolerance)
        .toResource(dm -> R4AllergyIntoleranceTransformer.builder().datamart(dm).build().toFhir())
        .witnessProtection(witnessProtection)
        .replaceReferences(
            resource ->
                Stream.concat(
                    Stream.of(resource.recorder().orElse(null), resource.patient()),
                    resource.notes().stream().map(n -> n.practitioner().orElse(null))))
        .build();
  }

  private VulcanizedReader<
          AllergyIntoleranceEntity, DatamartAllergyIntolerance, AllergyIntolerance, String>
      vulcanizedReader() {
    return VulcanizedReader
        .<AllergyIntoleranceEntity, DatamartAllergyIntolerance, AllergyIntolerance, String>
            forTransformation(transformation())
        .repository(repository)
        .toPatientId(e -> Optional.of(e.icn()))
        .toPrimaryKey(Function.identity())
        .toPayload(AllergyIntoleranceEntity::payload)
        .build();
  }
}
