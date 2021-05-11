package gov.va.api.health.dataquery.service.controller.procedure;

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
import gov.va.api.health.r4.api.resources.Procedure;
import gov.va.api.lighthouse.vulcan.Vulcan;
import gov.va.api.lighthouse.vulcan.VulcanConfiguration;
import gov.va.api.lighthouse.vulcan.mappings.Mappings;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Request Mappings for Procedure Profile, see
 * https://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-procedure.html for
 * implementation details.
 */
@Builder
@Validated
@RestController
@AllArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping(
    value = {"/r4/Procedure"},
    produces = {"application/json", "application/fhir+json"})
public class R4ProcedureController {
  private final LinkProperties linkProperties;

  private final ProcedureRepository repository;

  private final WitnessProtection witnessProtection;

  private VulcanConfiguration<ProcedureEntity> configuration() {
    return VulcanConfiguration.forEntity(ProcedureEntity.class)
        .paging(linkProperties.pagingConfiguration("Procedure", ProcedureEntity.naturalOrder()))
        .mappings(
            Mappings.forEntity(ProcedureEntity.class)
                .dateAsLongMilliseconds("date", "performedOnEpochTime")
                .value("_id", "cdwId", witnessProtection::toCdwId)
                .value("identifier", "cdwId", witnessProtection::toCdwId)
                .add(R4PatientReferenceMapping.<ProcedureEntity>forLinks(linkProperties).get())
                .get())
        .rule(parametersNeverSpecifiedTogether("_id", "identifier", "patient"))
        .rule(ifParameter("date").thenAlsoAtLeastOneParameterOf("patient"))
        .rule(ifParameter("patient").thenAllowOnlyKnownModifiers("identifier"))
        .defaultQuery(returnNothing())
        .build();
  }

  /** Read by id. */
  @GetMapping(value = {"/{publicId}"})
  public Procedure read(@PathVariable("publicId") String publicId) {
    return reader().read(publicId);
  }

  /** Read Raw Datamart Payload Support. */
  @GetMapping(
      value = {"/{publicId}"},
      headers = {"raw=true"})
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    return reader().readRaw(publicId, response);
  }

  VulcanizedReader<ProcedureEntity, DatamartProcedure, Procedure, String> reader() {
    return VulcanizedReader
        .<ProcedureEntity, DatamartProcedure, Procedure, String>forTransformation(transformation())
        .repository(repository)
        .toPatientId(e -> Optional.of(e.icn()))
        .toPrimaryKey(Function.identity())
        .toPayload(ProcedureEntity::payload)
        .build();
  }

  /** Search Support. */
  @GetMapping
  public Procedure.Bundle search(HttpServletRequest request) {
    return Vulcan.forRepo(repository)
        .config(configuration())
        .build()
        .search(request)
        .map(toBundle());
  }

  VulcanizedBundler<
          ProcedureEntity, DatamartProcedure, Procedure, Procedure.Entry, Procedure.Bundle>
      toBundle() {
    return VulcanizedBundler.forTransformation(transformation())
        .bundling(
            Bundling.newBundle(Procedure.Bundle::new)
                .newEntry(Procedure.Entry::new)
                .linkProperties(linkProperties)
                .build())
        .build();
  }

  VulcanizedTransformation<ProcedureEntity, DatamartProcedure, Procedure> transformation() {
    return VulcanizedTransformation.toDatamart(ProcedureEntity::asDatamartProcedure)
        .toResource(dm -> R4ProcedureTransformer.builder().datamart(dm).build().toFhir())
        .witnessProtection(witnessProtection)
        .replaceReferences(
            resource ->
                Stream.concat(
                    Stream.of(resource.patient(), resource.location().orElse(null)),
                    resource.encounter().stream()))
        .build();
  }
}
