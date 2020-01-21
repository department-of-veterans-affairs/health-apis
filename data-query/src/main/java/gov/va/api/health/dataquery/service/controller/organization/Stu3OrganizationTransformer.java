package gov.va.api.health.dataquery.service.controller.organization;

import static gov.va.api.health.dataquery.service.controller.Stu3Transformers.asCodeableConceptWrapping;
import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static gov.va.api.health.dataquery.service.controller.Transformers.convert;
import static gov.va.api.health.dataquery.service.controller.Transformers.emptyToNull;
import static gov.va.api.health.dataquery.service.controller.Transformers.isBlank;
import static java.util.Arrays.asList;

import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.stu3.api.datatypes.ContactPoint;
import gov.va.api.health.stu3.api.resources.Organization;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

@Builder
final class Stu3OrganizationTransformer {
  @NonNull private final DatamartOrganization datamart;

  static List<Organization.OrganizationAddress> addresses(DatamartOrganization.Address address) {
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
        Organization.OrganizationAddress.builder()
            .line(emptyToNull(asList(address.line1(), address.line2())))
            .city(address.city())
            .state(address.state())
            .postalCode(address.postalCode())
            .text(
                Stream.of(
                        address.line1(),
                        address.line2(),
                        address.city(),
                        address.state(),
                        address.postalCode())
                    .map(StringUtils::trimToNull)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(" ")))
            .build());
  }

  static List<Organization.OrganizationIdentifier> identifier(Optional<String> npi) {
    if (isBlank(npi)) {
      return null;
    }
    return asList(
        Organization.OrganizationIdentifier.builder()
            .system("http://hl7.org/fhir/sid/us-npi")
            .value(npi.get())
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
        .identifier(identifier(datamart.npi()))
        .active(datamart.active())
        .type(emptyToNull(asList(asCodeableConceptWrapping(datamart.type()))))
        .name(datamart.name())
        .telecom(telecoms())
        .address(addresses(datamart.address()))
        .build();
  }
}
