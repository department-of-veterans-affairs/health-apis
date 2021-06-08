package gov.va.api.health.dataquery.service.controller.immunization;

import static gov.va.api.health.dataquery.service.config.LinkProperties.noSortableParameters;
import static gov.va.api.lighthouse.vulcan.Rules.ifParameter;
import static gov.va.api.lighthouse.vulcan.Rules.parametersNeverSpecifiedTogether;
import static gov.va.api.lighthouse.vulcan.Vulcan.returnNothing;

import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.R4PatientReferenceMapping;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.vulcanizer.Bundling;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedBundler;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedReader;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedTransformation;
import gov.va.api.health.r4.api.resources.Immunization;
import gov.va.api.lighthouse.vulcan.Vulcan;
import gov.va.api.lighthouse.vulcan.VulcanConfiguration;
import gov.va.api.lighthouse.vulcan.mappings.Mappings;
import java.util.List;
import java.util.Optional;
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
 * Request Mappings for Immunization Profile, see
 * https://www.hl7.org/fhir/us/core/StructureDefinition-us-core-immunization.html for implementation
 * details.
 */
@SuppressWarnings("WeakerAccess")
@Validated
@RestController
@AllArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping(
    value = {"/r4/Immunization"},
    produces = {"application/json", "application/fhir+json"})
public class R4ImmunizationController {
  private final LinkProperties linkProperties;

  private final ImmunizationRepository repository;

  private final WitnessProtection witnessProtection;

  private VulcanConfiguration<ImmunizationEntity> configuration() {
    return VulcanConfiguration.forEntity(ImmunizationEntity.class)
        .paging(
            linkProperties.pagingConfiguration(
                "Immunization", ImmunizationEntity.naturalOrder(), noSortableParameters()))
        .mappings(
            Mappings.forEntity(ImmunizationEntity.class)
                .value("_id", "cdwId", witnessProtection::toCdwId)
                .value("identifier", "cdwId", witnessProtection::toCdwId)
                .add(R4PatientReferenceMapping.<ImmunizationEntity>forLinks(linkProperties).get())
                .get())
        .defaultQuery(returnNothing())
        .rules(
            List.of(
                parametersNeverSpecifiedTogether("patient", "_id", "identifier"),
                ifParameter("patient").thenAllowOnlyKnownModifiers("identifier")))
        .build();
  }

  /** Read Support. */
  @GetMapping(value = {"/{publicId}"})
  public Immunization read(@PathVariable("publicId") String publicId) {
    return vulcanizedReader().read(publicId);
  }

  /** Read Raw Datamart Payload Support. */
  @GetMapping(
      value = "/{publicId}",
      headers = {"raw=true"})
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    return vulcanizedReader().readRaw(publicId, response);
  }

  /** Search Support. */
  @GetMapping
  public Immunization.Bundle search(HttpServletRequest request) {
    return Vulcan.forRepo(repository)
        .config(configuration())
        .build()
        .search(request)
        .map(toBundle());
  }

  VulcanizedBundler<
          ImmunizationEntity,
          DatamartImmunization,
          Immunization,
          Immunization.Entry,
          Immunization.Bundle>
      toBundle() {
    return VulcanizedBundler.forTransformation(transformation())
        .bundling(
            Bundling.newBundle(Immunization.Bundle::new)
                .newEntry(Immunization.Entry::new)
                .linkProperties(linkProperties)
                .build())
        .build();
  }

  private VulcanizedTransformation<ImmunizationEntity, DatamartImmunization, Immunization>
      transformation() {
    return VulcanizedTransformation.toDatamart(ImmunizationEntity::asDatamartImmunization)
        .toResource(dm -> R4ImmunizationTransformer.builder().datamart(dm).build().toFhir())
        .witnessProtection(witnessProtection)
        .replaceReferences(
            resource ->
                Stream.of(
                    resource.patient(),
                    resource.performer().orElse(null),
                    resource.requester().orElse(null),
                    resource.location().orElse(null)))
        .build();
  }

  private VulcanizedReader<ImmunizationEntity, DatamartImmunization, Immunization, String>
      vulcanizedReader() {
    return VulcanizedReader
        .<ImmunizationEntity, DatamartImmunization, Immunization, String>forTransformation(
            transformation())
        .repository(repository)
        .toPatientId(e -> Optional.of(e.icn()))
        .toPrimaryKey(Function.identity())
        .toPayload(ImmunizationEntity::payload)
        .build();
  }
}
