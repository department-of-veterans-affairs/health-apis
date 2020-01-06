package gov.va.api.health.dataquery.service.controller.organization;

import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.allBlank;
import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.convert;
import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.emptyToNull;
import static java.util.Arrays.asList;

import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dstu2.api.datatypes.Address;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.datatypes.ContactPoint;
import gov.va.api.health.dstu2.api.resources.Organization;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.NonNull;

@Builder
final class Dstu2OrganizationTransformer {

  @NonNull private final DatamartOrganization datamart;

  static List<Address> address(DatamartOrganization.Address address) {
    if (address == null
        || allBlank(
            address.line1(),
            address.line2(),
            address.city(),
            address.state(),
            address.postalCode())) {
      return null;
    }
    return asList(
        Address.builder()
            .line(emptyToNull(Arrays.asList(address.line1(), address.line2())))
            .city(address.city())
            .state(address.state())
            .postalCode(address.postalCode())
            .build());
  }

  static ContactPoint telecom(DatamartOrganization.Telecom telecom) {
    if (telecom == null || allBlank(telecom.system(), telecom.value())) {
      return null;
    }
    return convert(
        telecom,
        tel ->
            ContactPoint.builder().system(telecomSystem(tel.system())).value(tel.value()).build());
  }

  static ContactPoint.ContactPointSystem telecomSystem(DatamartOrganization.Telecom.System tel) {
    return convert(
        tel, source -> EnumSearcher.of(ContactPoint.ContactPointSystem.class).find(tel.toString()));
  }

  static CodeableConcept type(Optional<DatamartCoding> source) {
    if (source == null || source.get().code().get() == null) {
      return null;
    }
    return CodeableConcept.builder().coding(typeCoding(source.get())).build();
  }

  static List<Coding> typeCoding(DatamartCoding source) {
    if (source == null || allBlank(source.system(), source.display(), source.code())) {
      return null;
    }
    return convert(
        source,
        cdw ->
            List.of(
                Coding.builder()
                    .code(cdw.code().get())
                    .display(cdw.display().get())
                    .system(cdw.system().get())
                    .build()));
  }

  List<ContactPoint> telecoms() {
    return emptyToNull(
        datamart.telecom().stream().map(tel -> telecom(tel)).collect(Collectors.toList()));
  }

  public Organization toFhir() {
    return Organization.builder()
        .resourceType("Organization")
        .id(datamart.cdwId())
        .active(datamart.active())
        .type(type(datamart.type()))
        .name(datamart.name())
        .telecom(telecoms())
        .address(address(datamart.address()))
        .build();
  }
}
