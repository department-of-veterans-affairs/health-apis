package gov.va.api.health.dataquery.service.controller.appointment;

import static gov.va.api.lighthouse.vulcan.Rules.parametersNeverSpecifiedTogether;
import static gov.va.api.lighthouse.vulcan.Vulcan.returnNothing;

import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.CompositeCdwIds;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.vulcanizer.Bundling;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedBundler;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedReader;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedTransformation;
import gov.va.api.health.r4.api.resources.Appointment;
import gov.va.api.lighthouse.datamart.CompositeCdwId;
import gov.va.api.lighthouse.vulcan.CircuitBreaker;
import gov.va.api.lighthouse.vulcan.Vulcan;
import gov.va.api.lighthouse.vulcan.VulcanConfiguration;
import gov.va.api.lighthouse.vulcan.mappings.Mappings;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(
    value = {"/r4/Appointment"},
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class R4AppointmentController {
  private final WitnessProtection witnessProtection;

  private final AppointmentRepository repository;

  private final LinkProperties linkProperties;

  private VulcanConfiguration<AppointmentEntity> configuration() {
    return VulcanConfiguration.forEntity(AppointmentEntity.class)
        .paging(linkProperties.pagingConfiguration("Appointment", AppointmentEntity.naturalOrder()))
        .mappings(
            Mappings.forEntity(AppointmentEntity.class)
                .dateAsInstant("_lastUpdated", "lastUpdated")
                .value("patient", "icn")
                .value("location", "locationSid", this::publicIdToCdwIdNumber)
                .values("_id", this::loadCdwId)
                .values("identifier", this::loadCdwId)
                .value("patient", "icn")
                .get())
        .rule(parametersNeverSpecifiedTogether("_id", "identifier", "patient"))
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

  Integer publicIdToCdwIdNumber(String publicId) {
    try {
      return CompositeCdwId.fromCdwId(witnessProtection.toCdwId(publicId))
          .cdwIdNumber()
          .intValueExact();

    } catch (IllegalArgumentException | ArithmeticException e) {
      throw CircuitBreaker.noResultsWillBeFound("location", publicId, "Unknown ID.");
    }
  }

  /** Read Appointment by id. */
  @GetMapping(value = {"/{publicId}"})
  public Appointment read(@PathVariable("publicId") String publicId) {
    return vulcanizedReader().read(publicId);
  }

  /** Read Raw DatamartAppointment by id. */
  @GetMapping(
      value = {"/{publicId}"},
      headers = {"raw=true"})
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    return vulcanizedReader().readRaw(publicId, response);
  }

  /** US-Core-R4 Appointment Search Support. */
  @GetMapping
  public Appointment.Bundle search(HttpServletRequest request) {
    return Vulcan.forRepo(repository)
        .config(configuration())
        .build()
        .search(request)
        .map(toBundle());
  }

  VulcanizedBundler<
          AppointmentEntity,
          DatamartAppointment,
          Appointment,
          Appointment.Entry,
          Appointment.Bundle>
      toBundle() {
    return VulcanizedBundler.forTransformation(transformation())
        .bundling(
            Bundling.newBundle(Appointment.Bundle::new)
                .newEntry(Appointment.Entry::new)
                .linkProperties(linkProperties)
                .build())
        .build();
  }

  VulcanizedTransformation<AppointmentEntity, DatamartAppointment, Appointment> transformation() {
    var state = new StatefulTransformation();
    return VulcanizedTransformation.toDatamart(state::asDatamartAppointment)
        .toResource(state::toAppointment)
        .witnessProtection(witnessProtection)
        .replaceReferences(resource -> resource.participant().stream())
        .build();
  }

  VulcanizedReader<AppointmentEntity, DatamartAppointment, Appointment, CompositeCdwId>
      vulcanizedReader() {
    return VulcanizedReader
        .<AppointmentEntity, DatamartAppointment, Appointment, CompositeCdwId>forTransformation(
            transformation())
        .repository(repository)
        .toPrimaryKey(CompositeCdwIds::requireCompositeIdStringFormat)
        .toPatientId(e -> Optional.of(e.icn()))
        .toPayload(AppointmentEntity::payload)
        .build();
  }

  /**
   * During the transformation process from database entity to FHIR record, we will need the
   * original CDW ID to properly detect the type of a appointment, which is encoded in it's ID.
   */
  private static class StatefulTransformation {

    /**
     * We're tracking the pair of entity-to-appointment as this thread safe queue. We're not using a
     * map here because we know the DatamartAppointment.cdwId will be mutated along the way and we
     * cannot safe use the CDW ID or the entire appointment object as map key. We only will
     * transform each entity/appointment once, so we will remove entries as they are processed to
     * keep sequential searching to a minimum.
     */
    private final Queue<State> states = new ConcurrentLinkedQueue<>();

    DatamartAppointment asDatamartAppointment(AppointmentEntity entity) {
      DatamartAppointment payload = entity.asDatamartAppointment();
      states.add(State.of(entity, payload));
      return payload;
    }

    private Supplier<IllegalStateException> missingStateException(DatamartAppointment witness) {
      return () ->
          new IllegalStateException(
              "Attempting to convert datamart appointment with missing state entry: "
                  + witness.cdwId());
    }

    Appointment toAppointment(DatamartAppointment witness) {
      State state =
          states.stream()
              .filter(s -> s.payload().equals(witness))
              .findFirst()
              .orElseThrow(missingStateException(witness));
      states.remove(state);
      CompositeCdwId witnessCompositeId = state.entity().compositeCdwId();

      return R4AppointmentTransformer.builder()
          .compositeCdwId(witnessCompositeId)
          .dm(witness)
          .build()
          .toFhir();
    }

    @AllArgsConstructor(staticName = "of")
    @Getter
    private static class State {
      private final AppointmentEntity entity;
      private final DatamartAppointment payload;
    }
  }
}
