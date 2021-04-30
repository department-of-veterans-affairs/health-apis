package gov.va.api.health.dataquery.service.controller.procedure;

import static gov.va.api.lighthouse.vulcan.Rules.ifParameter;
import static gov.va.api.lighthouse.vulcan.Rules.parametersNeverSpecifiedTogether;
import static gov.va.api.lighthouse.vulcan.Vulcan.returnNothing;

import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.vulcanizer.Bundling;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedBundler;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedReader;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedTransformation;
import gov.va.api.health.r4.api.resources.Procedure;
import gov.va.api.lighthouse.vulcan.InvalidRequest;
import gov.va.api.lighthouse.vulcan.Vulcan;
import gov.va.api.lighthouse.vulcan.VulcanConfiguration;
import gov.va.api.lighthouse.vulcan.mappings.DateMapping;
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
                .date(
                    "date",
                    "performedOnEpochTime",
                    (DateMapping.PredicateFactory<Long>)
                        (date, field, criteriaBuilder) -> {
                          final DateMapping.DateApproximation approximation =
                              DateMapping.defaultGraduatedApproximation();
                          switch (date.operator()) {
                            case EQ:
                              return criteriaBuilder.and(
                                  criteriaBuilder.greaterThanOrEqualTo(
                                      field, date.upperBound().toEpochMilli()),
                                  criteriaBuilder.lessThanOrEqualTo(
                                      field, date.upperBound().toEpochMilli()));
                            case NE:
                              return criteriaBuilder.or(
                                  criteriaBuilder.lessThan(field, date.lowerBound().toEpochMilli()),
                                  criteriaBuilder.greaterThan(
                                      field, date.upperBound().toEpochMilli()));
                            case GT:
                            case SA:
                              return criteriaBuilder.greaterThan(
                                  field, date.upperBound().toEpochMilli());
                            case LT:
                            case EB:
                              return criteriaBuilder.lessThan(
                                  field, date.lowerBound().toEpochMilli());
                            case GE:
                              return criteriaBuilder.greaterThanOrEqualTo(
                                  field, date.lowerBound().toEpochMilli());
                            case LE:
                              return criteriaBuilder.lessThanOrEqualTo(
                                  field, date.upperBound().toEpochMilli());
                            case AP:
                              return criteriaBuilder.and(
                                  criteriaBuilder.greaterThanOrEqualTo(
                                      field, approximation.expandLowerBound(date).toEpochMilli()),
                                  criteriaBuilder.lessThanOrEqualTo(
                                      field, approximation.expandUpperBound(date).toEpochMilli()));
                            default:
                              throw new InvalidRequest(
                                  "Unknown date search operator: " + date.operator());
                          }
                        })
                .value("_id", "cdwId", witnessProtection::toCdwId)
                .value("identifier", "cdwId", witnessProtection::toCdwId)
                .value("patient", "icn")
                .get())
        .rule(parametersNeverSpecifiedTogether("_id", "identifier", "patient"))
        .rule(ifParameter("date").thenAlsoAtLeastOneParameterOf("patient"))
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
