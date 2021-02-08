package gov.va.api.health.dataquery.service.controller.appointment;

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
import gov.va.api.lighthouse.vulcan.Vulcan;
import gov.va.api.lighthouse.vulcan.VulcanConfiguration;
import gov.va.api.lighthouse.vulcan.mappings.Mappings;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
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
public class R4AppointmentController {
  private WitnessProtection witnessProtection;

  private AppointmentRepository repository;

  private LinkProperties linkProperties;

  private VulcanConfiguration<AppointmentEntity> configuration() {
    return VulcanConfiguration.forEntity(AppointmentEntity.class)
        .paging(linkProperties.pagingConfiguration("Appointment", AppointmentEntity.naturalOrder()))
        .mappings(Mappings.forEntity(AppointmentEntity.class).value("patient", "icn").get())
        .defaultQuery(returnNothing())
        .build();
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
    return VulcanizedTransformation.toDatamart(AppointmentEntity::asDatamartAppointment)
        .toResource(
            dm ->
                R4AppointmentTransformer.builder()
                    .compositeCdwId(CompositeCdwId.fromCdwId(witnessProtection.toCdwId(dm.cdwId())))
                    .dm(dm)
                    .build()
                    .toFhir())
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
}
