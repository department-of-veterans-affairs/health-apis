package gov.va.api.health.dataquery.service.controller.device;

import static gov.va.api.lighthouse.vulcan.Rules.forbidUnknownParameters;
import static gov.va.api.lighthouse.vulcan.Rules.parametersNeverSpecifiedTogether;
import static gov.va.api.lighthouse.vulcan.Vulcan.returnNothing;

import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.vulcanizer.Bundling;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedBundler;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedReader;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedTransformation;
import gov.va.api.health.r4.api.resources.Device;
import gov.va.api.lighthouse.vulcan.Vulcan;
import gov.va.api.lighthouse.vulcan.VulcanConfiguration;
import gov.va.api.lighthouse.vulcan.mappings.Mappings;
import java.util.Optional;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Request mappings for the US-Core-R4 Implantable Device resource.
 *
 * @implSpec http://hl7.org/fhir/us/core/StructureDefinition-us-core-implantable-device.html
 */
@Validated
@RestController
@RequestMapping(
    value = {"/r4/Device"},
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor_ = @Autowired)
public class R4DeviceController {

  private final WitnessProtection witnessProtection;

  private final DeviceRepository repository;

  private final LinkProperties linkProperties;

  private VulcanConfiguration<DeviceEntity> configuration() {
    return VulcanConfiguration.forEntity(DeviceEntity.class)
        .paging(linkProperties.pagingConfiguration("Device", DeviceEntity.naturalOrder()))
        .mappings(
            Mappings.forEntity(DeviceEntity.class)
                .value("_id", "cdwId", witnessProtection::toCdwId)
                // ToDo -- cdwId system for a token search?
                .value("identifier", "cdwId", witnessProtection::toCdwId)
                .value("patient", "icn")
                // ToDo -- Search by Patient+Type (Missing: db column "type")
                .get())
        .defaultQuery(returnNothing())
        .rule(parametersNeverSpecifiedTogether("patient", "_id", "identifier"))
        .rule(forbidUnknownParameters())
        .build();
  }

  @GetMapping(value = "/{publicId}")
  public Device read(@PathVariable("publicId") String publicId) {
    return vulcanizedReader().read(publicId);
  }

  @GetMapping(value = "/{publicId}", headers = "raw=true")
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    return vulcanizedReader().readRaw(publicId, response);
  }

  /** US-Core-R4 Implantable Device Search Support. */
  @GetMapping
  public Device.Bundle search(HttpServletRequest request) {
    return Vulcan.forRepo(repository)
        .config(configuration())
        .build()
        .search(request)
        .map(toBundle());
  }

  VulcanizedBundler<DeviceEntity, DatamartDevice, Device, Device.Entry, Device.Bundle> toBundle() {
    return VulcanizedBundler.forTransformation(transformation())
        .bundling(
            Bundling.newBundle(Device.Bundle::new)
                .newEntry(Device.Entry::new)
                .linkProperties(linkProperties)
                .build())
        .build();
  }

  VulcanizedTransformation<DeviceEntity, DatamartDevice, Device> transformation() {
    return VulcanizedTransformation.toDatamart(DeviceEntity::asDatamartDevice)
        .toResource(dm -> R4DeviceTransformer.builder().datamart(dm).build().toFhir())
        .witnessProtection(witnessProtection)
        .replaceReferences(
            resource -> Stream.of(resource.patient, resource.location().orElse(null)))
        .build();
  }

  VulcanizedReader<DeviceEntity, DatamartDevice, Device> vulcanizedReader() {
    return VulcanizedReader.forTransformation(transformation())
        .repository(repository)
        .toPatientId(e -> Optional.of(e.icn()))
        .toPayload(DeviceEntity::payload)
        .build();
  }
}
