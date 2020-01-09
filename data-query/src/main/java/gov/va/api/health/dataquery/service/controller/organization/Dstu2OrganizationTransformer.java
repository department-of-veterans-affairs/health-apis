package gov.va.api.health.dataquery.service.controller.organization;

import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.asCodeableConceptWrapping;
import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.convert;
import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.emptyToNull;
import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static java.util.Arrays.asList;

import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.dstu2.api.datatypes.Address;
import gov.va.api.health.dstu2.api.datatypes.ContactPoint;
import gov.va.api.health.dstu2.api.resources.Organization;
import java.util.List;
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
            .line(emptyToNull(asList(address.line1(), address.line2())))
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

  List<ContactPoint> telecoms() {
    return emptyToNull(
        datamart.telecom().stream().map(tel -> telecom(tel)).collect(Collectors.toList()));
  }

  public Organization toFhir() {
    return Organization.builder()
        .resourceType("Organization")
        .id(datamart.cdwId())
        .active(datamart.active())
        .type(asCodeableConceptWrapping(datamart.type()))
        .name(datamart.name())
        .telecom(telecoms())
        .address(address(datamart.address()))
        .build();
  }
}
