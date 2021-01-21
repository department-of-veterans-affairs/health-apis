package gov.va.api.health.dataquery.service.controller.organization;

import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static gov.va.api.health.dataquery.service.controller.Transformers.convert;
import static gov.va.api.health.dataquery.service.controller.Transformers.emptyToNull;
import static gov.va.api.health.dataquery.service.controller.Transformers.isBlank;
import static gov.va.api.health.dataquery.service.controller.organization.DatamartOrganization.FacilityId;
import static java.util.Arrays.asList;

import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.dataquery.service.controller.organization.DatamartOrganization.Telecom;
import gov.va.api.health.r4.api.datatypes.Address;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.ContactPoint;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.resources.Organization;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

@Builder
public class R4OrganizationTransformer {
  @NonNull private final DatamartOrganization datamart;

  static List<Address> address(DatamartOrganization.Address maybeAddress) {
    if (maybeAddress == null
        || allBlank(
            maybeAddress.line1(),
            maybeAddress.line2(),
            maybeAddress.city(),
            maybeAddress.state(),
            maybeAddress.postalCode())) {
      return null;
    }
    return Collections.singletonList(
        Address.builder()
            .line(emptyToNull(asList(maybeAddress.line1(), maybeAddress.line2())))
            .city(maybeAddress.city())
            .state(maybeAddress.state())
            .postalCode(maybeAddress.postalCode())
            .text(
                Stream.of(
                        maybeAddress.line1(),
                        maybeAddress.line2(),
                        maybeAddress.city(),
                        maybeAddress.state(),
                        maybeAddress.postalCode())
                    .map(StringUtils::trimToNull)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(" ")))
            .build());
  }

  static Identifier buildFacilityIdentifier(FacilityId facilityId) {
    String facilityIdPref;
    switch (facilityId.type()) {
      case HEALTH:
        facilityIdPref = "vha_";
        break;
      case BENEFITS:
        facilityIdPref = "vba_";
        break;
      case VET_CENTER:
        facilityIdPref = "vc_";
        break;
      case CEMETERY:
        facilityIdPref = "nca_";
        break;
      case NONNATIONAL_CEMETERY:
        facilityIdPref = "ncas_";
        break;
      default:
        throw new IllegalStateException("Unsupported facility type: " + facilityId.type());
    }
    return Identifier.builder()
        .use(Identifier.IdentifierUse.usual)
        .type(
            CodeableConcept.builder()
                .coding(
                    Collections.singletonList(
                        Coding.builder()
                            .system("http://terminology.hl7.org/CodeSystem/v2-0203")
                            .code("FI")
                            .display("Facility ID")
                            .build()))
                .build())
        .system("https://api.va.gov/services/fhir/v0/r4/NamingSystem/va-facility-identifier")
        .value(facilityIdPref + facilityId.stationNumber())
        .build();
  }

  static List<Identifier> identifiers(
      Optional<String> maybeNpi, Optional<FacilityId> maybeFacility) {
    List<Identifier> results = new ArrayList<>(2);
    if (!isBlank(maybeNpi)) {
      results.add(
          Identifier.builder()
              .system("http://hl7.org/fhir/sid/us-npi")
              .value(maybeNpi.get())
              .build());
    }
    if (!isBlank(maybeFacility)) {
      results.add(buildFacilityIdentifier(maybeFacility.get()));
    }
    if (results.isEmpty()) {
      return null;
    }
    return results;
  }

  static ContactPoint telecom(Telecom telecom) {
    if (telecom == null || allBlank(telecom.system(), telecom.value())) {
      return null;
    }
    return ContactPoint.builder()
        .system(telecomSystem(telecom.system()))
        .value(telecom.value())
        .build();
  }

  static ContactPoint.ContactPointSystem telecomSystem(Telecom.System tel) {
    return convert(
        tel,
        source -> EnumSearcher.of(ContactPoint.ContactPointSystem.class).find(source.toString()));
  }

  static List<ContactPoint> telecoms(List<Telecom> maybeTelecoms) {
    return emptyToNull(
        maybeTelecoms.stream().map(tel -> telecom(tel)).collect(Collectors.toList()));
  }

  Organization toFhir() {
    return Organization.builder()
        .resourceType("Organization")
        .id(datamart.cdwId())
        .identifier(identifiers(datamart.npi(), datamart.facilityId()))
        .active(datamart.active())
        .name(datamart.name())
        .telecom(telecoms(datamart.telecom()))
        .address(address(datamart.address()))
        .build();
  }
}
