package gov.va.api.health.dataquery.service.controller.organization;

import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.convert;
import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.convertAll;
import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.ifPresent;
import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static java.util.Collections.singletonList;

import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.dstu2.api.datatypes.Address;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.datatypes.ContactPoint;
import gov.va.api.health.dstu2.api.datatypes.ContactPoint.ContactPointSystem;
import gov.va.api.health.dstu2.api.datatypes.ContactPoint.ContactPointUse;
import gov.va.api.health.dstu2.api.resources.Organization;
import gov.va.dvp.cdw.xsd.model.CdwOrganization100Root.CdwOrganizations.CdwOrganization;
import gov.va.dvp.cdw.xsd.model.CdwOrganization100Root.CdwOrganizations.CdwOrganization.CdwAddresses;
import gov.va.dvp.cdw.xsd.model.CdwOrganization100Root.CdwOrganizations.CdwOrganization.CdwTelecoms;
import gov.va.dvp.cdw.xsd.model.CdwOrganizationAddress;
import gov.va.dvp.cdw.xsd.model.CdwOrganizationTelecomUse;
import gov.va.dvp.cdw.xsd.model.CdwOrganizationType;
import gov.va.dvp.cdw.xsd.model.CdwOrganizationType.CdwCoding;
import gov.va.dvp.cdw.xsd.model.CdwOrganizationTypeCode;
import gov.va.dvp.cdw.xsd.model.CdwOrganizationTypeDisplay;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class Dstu2OrganizationMrAndersonTransformer
    implements Dstu2OrganizationController.Transformer {
  List<String> addressLine(CdwOrganizationAddress source) {
    if (source == null || source.getLines() == null || source.getLines().getLine().isEmpty()) {
      return null;
    }
    return source.getLines().getLine();
  }

  List<Address> addresses(CdwAddresses optionalSource) {
    return convertAll(
        ifPresent(optionalSource, CdwAddresses::getAddress),
        cdw ->
            Address.builder()
                .line(addressLine(cdw))
                .city(cdw.getCity())
                .state(cdw.getState())
                .postalCode(cdw.getPostalCode())
                .build());
  }

  @Override
  public Organization apply(CdwOrganization source) {
    return Organization.builder()
        .resourceType("Organization")
        .id(source.getCdwId())
        .active(source.isActive())
        .type(type(source.getType()))
        .name(source.getName())
        .telecom(telecoms(source.getTelecoms()))
        .address(addresses(source.getAddresses()))
        .build();
  }

  Coding coding(CdwCoding cdw) {
    if (cdw == null || allBlank(cdw.getCode(), cdw.getDisplay(), cdw.getSystem())) {
      return null;
    }
    return Coding.builder()
        .system(cdw.getSystem())
        .code(convert(cdw.getCode(), CdwOrganizationTypeCode::value))
        .display(convert(cdw.getDisplay(), CdwOrganizationTypeDisplay::value))
        .build();
  }

  List<Coding> codings(CdwCoding optionalSource) {
    Coding coding = convert(optionalSource, this::coding);
    return coding == null ? null : singletonList(coding);
  }

  ContactPointSystem contactPointSystem(String sourceSystem) {
    if (StringUtils.isBlank(sourceSystem)) {
      return null;
    }
    return EnumSearcher.of(ContactPointSystem.class).find(sourceSystem.toLowerCase(Locale.ENGLISH));
  }

  ContactPointUse contactPointUse(CdwOrganizationTelecomUse source) {
    return convert(source, cdw -> EnumSearcher.of(ContactPointUse.class).find(cdw.value()));
  }

  List<ContactPoint> telecoms(CdwTelecoms optionalSource) {
    return convertAll(
        ifPresent(optionalSource, CdwTelecoms::getTelecom),
        source ->
            ContactPoint.builder()
                .system(contactPointSystem(source.getSystem()))
                .value(source.getValue())
                .use(contactPointUse(source.getUse()))
                .build());
  }

  CodeableConcept type(CdwOrganizationType maybeSource) {
    return convert(
        ifPresent(maybeSource, CdwOrganizationType::getCoding),
        cdw -> CodeableConcept.builder().coding(codings(cdw)).build());
  }
}
