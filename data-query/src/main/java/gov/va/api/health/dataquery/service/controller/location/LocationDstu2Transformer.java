package gov.va.api.health.dataquery.service.controller.location;

import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static gov.va.api.health.dataquery.service.controller.Transformers.convert;
import static gov.va.api.health.dataquery.service.controller.Transformers.convertAll;
import static gov.va.api.health.dataquery.service.controller.Transformers.ifPresent;

import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.dstu2.api.datatypes.Address;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.datatypes.ContactPoint;
import gov.va.api.health.dstu2.api.datatypes.ContactPoint.ContactPointSystem;
import gov.va.api.health.dstu2.api.elements.Reference;
import gov.va.api.health.dstu2.api.resources.Location;
import gov.va.api.health.dstu2.api.resources.Location.Mode;
import gov.va.api.health.dstu2.api.resources.Location.Status;
import gov.va.dvp.cdw.xsd.model.CdwLocation100Root.CdwLocations.CdwLocation;
import gov.va.dvp.cdw.xsd.model.CdwLocation100Root.CdwLocations.CdwLocation.CdwTelecoms;
import gov.va.dvp.cdw.xsd.model.CdwLocationAddress;
import gov.va.dvp.cdw.xsd.model.CdwLocationMode;
import gov.va.dvp.cdw.xsd.model.CdwLocationPhysicalType;
import gov.va.dvp.cdw.xsd.model.CdwLocationStatus;
import gov.va.dvp.cdw.xsd.model.CdwLocationTypeCode;
import gov.va.dvp.cdw.xsd.model.CdwLocationTypeDisplay;
import gov.va.dvp.cdw.xsd.model.CdwReference;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class LocationDstu2Transformer implements LocationDstu2Controller.Transformer {
  Address address(CdwLocationAddress maybeCdw) {
    if (isUnusableAddress(maybeCdw)) {
      return null;
    }
    return Address.builder()
        .city(maybeCdw.getCity())
        .line(maybeCdw.getLine())
        .postalCode(maybeCdw.getPostalCode())
        .state(maybeCdw.getState())
        .build();
  }

  @Override
  public Location apply(CdwLocation location) {
    return location(location);
  }

  ContactPointSystem contactPointCode(String maybeCdw) {
    if (StringUtils.isBlank(maybeCdw)) {
      return null;
    }
    return EnumSearcher.of(ContactPointSystem.class).find(maybeCdw);
  }

  Boolean isUnusableAddress(CdwLocationAddress maybeCdw) {
    return (maybeCdw == null
        || allBlank(
            maybeCdw.getLine(), maybeCdw.getCity(), maybeCdw.getPostalCode(), maybeCdw.getState())
        || maybeCdw.getLine().isEmpty());
  }

  private Location location(CdwLocation source) {
    return Location.builder()
        .resourceType("Location")
        .address(address(source.getAddress()))
        .id(source.getCdwId())
        .description(source.getDescription())
        .managingOrganization(reference(source.getManagingOrganization()))
        .mode(mode(source.getMode()))
        .name(source.getName())
        .physicalType(locationPhysicalType(source.getPhysicalType()))
        .status(status(source.getStatus()))
        .telecom(telecoms(source.getTelecoms()))
        .type(locationType(source.getType()))
        .build();
  }

  CodeableConcept locationPhysicalType(CdwLocationPhysicalType maybeCdw) {
    if (maybeCdw == null || allBlank(maybeCdw.getCoding(), maybeCdw.getText())) {
      return null;
    }
    return CodeableConcept.builder()
        .coding(locationPhysicalTypeCoding(maybeCdw.getCoding()))
        .text(maybeCdw.getText())
        .build();
  }

  List<Coding> locationPhysicalTypeCoding(CdwLocationPhysicalType.CdwCoding maybeCdw) {
    if (maybeCdw == null
        || allBlank(maybeCdw.getCode(), maybeCdw.getDisplay(), maybeCdw.getSystem())) {
      return null;
    }
    return Collections.singletonList(
        Coding.builder()
            .code(maybeCdw.getCode().value())
            .system(maybeCdw.getSystem())
            .display(maybeCdw.getDisplay().value())
            .build());
  }

  CodeableConcept locationType(CdwLocation.CdwType maybeCdw) {
    if (maybeCdw == null || maybeCdw.getCoding().isEmpty()) {
      return null;
    }
    return convert(
        maybeCdw,
        source ->
            CodeableConcept.builder().coding(locationTypeCodings(source.getCoding())).build());
  }

  private Coding locationTypeCoding(CdwLocation.CdwType.CdwCoding cdw) {
    if (cdw == null || allBlank(cdw.getCode(), cdw.getDisplay(), cdw.getSystem())) {
      return null;
    }
    return Coding.builder()
        .system(cdw.getSystem())
        .code(ifPresent(cdw.getCode(), CdwLocationTypeCode::value))
        .display(ifPresent(cdw.getDisplay(), CdwLocationTypeDisplay::value))
        .build();
  }

  List<Coding> locationTypeCodings(List<CdwLocation.CdwType.CdwCoding> maybeCdw) {
    return convertAll(maybeCdw, this::locationTypeCoding);
  }

  Mode mode(CdwLocationMode maybeCdw) {
    if (maybeCdw == null) {
      return null;
    }
    return EnumSearcher.of(Mode.class).find(maybeCdw.value());
  }

  Reference reference(CdwReference maybeCdw) {
    if (maybeCdw == null || allBlank(maybeCdw.getReference(), maybeCdw.getDisplay())) {
      return null;
    }
    return convert(
        maybeCdw,
        source ->
            Reference.builder()
                .reference(source.getReference())
                .display(source.getDisplay())
                .build());
  }

  Status status(CdwLocationStatus maybeCdw) {
    if (maybeCdw == null) {
      return null;
    }
    return EnumSearcher.of(Status.class).find(maybeCdw.value());
  }

  List<ContactPoint> telecoms(CdwTelecoms maybeCdw) {
    if (maybeCdw == null || maybeCdw.getTelecom().isEmpty()) {
      return null;
    }
    return convertAll(
        maybeCdw.getTelecom(),
        source ->
            ContactPoint.builder()
                .system(contactPointCode(source.getSystem()))
                .value(source.getValue())
                .build());
  }
}
