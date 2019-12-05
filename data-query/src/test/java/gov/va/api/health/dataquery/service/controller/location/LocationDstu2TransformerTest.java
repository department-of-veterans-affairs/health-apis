package gov.va.api.health.dataquery.service.controller.location;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

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
import gov.va.dvp.cdw.xsd.model.CdwLocation100Root.CdwLocations.CdwLocation.CdwTelecoms.CdwTelecom;
import gov.va.dvp.cdw.xsd.model.CdwLocation100Root.CdwLocations.CdwLocation.CdwType;
import gov.va.dvp.cdw.xsd.model.CdwLocationAddress;
import gov.va.dvp.cdw.xsd.model.CdwLocationMode;
import gov.va.dvp.cdw.xsd.model.CdwLocationPhysicalType;
import gov.va.dvp.cdw.xsd.model.CdwLocationPhysicalType.CdwCoding;
import gov.va.dvp.cdw.xsd.model.CdwLocationPhysicalTypeCode;
import gov.va.dvp.cdw.xsd.model.CdwLocationPhysicalTypeDisplay;
import gov.va.dvp.cdw.xsd.model.CdwLocationStatus;
import gov.va.dvp.cdw.xsd.model.CdwLocationTypeCode;
import gov.va.dvp.cdw.xsd.model.CdwLocationTypeDisplay;
import gov.va.dvp.cdw.xsd.model.CdwReference;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.junit.Test;

public class LocationDstu2TransformerTest {
  private final LocationDstu2Transformer tx = new LocationDstu2Transformer();

  private final CdwSampleData cdw = CdwSampleData.get();

  private final Expected expected = Expected.get();

  @Test
  public void address() {
    assertThat(tx.address(null)).isNull();
    assertThat(tx.address(new CdwLocationAddress())).isNull();
    assertThat(tx.address(cdw.address())).isEqualTo(expected.address());
  }

  @Test
  public void contactPoint() {
    assertThat(tx.contactPointCode(null)).isNull();
    assertThat(tx.contactPointCode("")).isNull();
  }

  @Test
  public void location() {
    assertThat(tx.apply(cdw.location())).isEqualTo(expected.location());
  }

  @Test
  public void mode() {
    assertThat(tx.mode(CdwLocationMode.INSTANCE)).isEqualTo(Mode.instance);
    assertThat(tx.mode(CdwLocationMode.KIND)).isEqualTo(Mode.kind);
    assertThat(tx.mode(null)).isNull();
  }

  @Test
  public void physicalType() {
    assertThat(tx.locationPhysicalType(null)).isNull();
    assertThat(tx.locationPhysicalType(new CdwLocationPhysicalType())).isNull();
    assertThat(tx.locationPhysicalType(cdw.physicalType())).isEqualTo(expected.physicalType());
  }

  @Test
  public void physicalTypeCoding() {
    assertThat(tx.locationPhysicalTypeCoding(null)).isNull();
    assertThat(tx.locationPhysicalTypeCoding(new CdwCoding())).isNull();
    assertThat(tx.locationPhysicalTypeCoding(cdw.physicalTypeCoding()))
        .isEqualTo(expected.physicalTypeCoding());
  }

  @Test
  public void reference() {
    assertThat(tx.reference(cdw.reference("x", "y"))).isEqualTo(expected.reference("x", "y"));
    assertThat(tx.reference(null)).isNull();
    assertThat(tx.reference(new CdwReference())).isNull();
  }

  @Test
  public void status() {
    assertThat(tx.status(CdwLocationStatus.ACTIVE)).isEqualTo(Status.active);
    assertThat(tx.status(CdwLocationStatus.INACTIVE)).isEqualTo(Status.inactive);
    assertThat(tx.status(CdwLocationStatus.SUSPENDED)).isEqualTo(Status.suspended);
    assertThat(tx.status(null)).isNull();
  }

  @Test
  public void telecom() {
    assertThat(tx.telecoms(null)).isNull();
    assertThat(tx.telecoms(new CdwTelecoms())).isNull();
    assertThat(tx.telecoms(cdw.telecoms())).isEqualTo(expected.telecom());
  }

  @Test
  public void type() {
    assertThat(tx.locationType(null)).isNull();
    assertThat(tx.locationType(new CdwType())).isNull();
    assertThat(tx.locationType(cdw.type())).isEqualTo(expected.type());
  }

  @Test
  public void typeCoding() {
    assertThat(tx.locationTypeCodings(null)).isNull();
    assertThat(tx.locationTypeCodings(new CdwLocation.CdwType().getCoding())).isNull();
    assertThat(tx.locationTypeCodings(cdw.type().getCoding())).isEqualTo(expected.typeCoding());
    assertThat(tx.locationTypeCodings(Collections.singletonList(new CdwType.CdwCoding()))).isNull();
  }

  @NoArgsConstructor(staticName = "get", access = AccessLevel.PUBLIC)
  public static class CdwSampleData {
    CdwLocationAddress address() {
      CdwLocationAddress cdw = new CdwLocationAddress();
      cdw.setCity("ALBANY");
      cdw.setPostalCode("12208");
      cdw.setState("NEW YORK");
      cdw.getLine().add("113 Holland Avenue");
      return cdw;
    }

    public CdwLocation location() {
      CdwLocation cdw = new CdwLocation();
      cdw.setCdwId("166365:L");
      cdw.setDescription("VAMC ALBANY");
      cdw.setName("VAMC ALBANY");
      cdw.setRowNumber(BigInteger.ONE);
      cdw.setStatus(CdwLocationStatus.ACTIVE);
      cdw.setTelecoms(telecoms());
      cdw.setType(type());
      cdw.setAddress(address());
      cdw.setManagingOrganization(reference("Organization/185576:I", "ZZ ALBANY"));
      cdw.setMode(CdwLocationMode.INSTANCE);
      cdw.setPhysicalType(physicalType());
      return cdw;
    }

    CdwLocationPhysicalType physicalType() {
      CdwLocationPhysicalType cdw = new CdwLocationPhysicalType();
      cdw.setCoding(physicalTypeCoding());
      cdw.setText("");
      return cdw;
    }

    CdwLocationPhysicalType.CdwCoding physicalTypeCoding() {
      CdwLocationPhysicalType.CdwCoding cdw = new CdwLocationPhysicalType.CdwCoding();
      cdw.setSystem("https://www.hl7.org/fhir/DSTU2/valueset-location-physical-type.html");
      cdw.setDisplay(CdwLocationPhysicalTypeDisplay.ROOM);
      cdw.setCode(CdwLocationPhysicalTypeCode.RO);
      return cdw;
    }

    private CdwReference reference(String ref, String display) {
      CdwReference cdw = new CdwReference();
      cdw.setReference(ref);
      cdw.setDisplay(display);
      return cdw;
    }

    CdwTelecom telecom() {
      CdwTelecom cdw = new CdwTelecom();
      cdw.setSystem("phone");
      cdw.setValue("402-995-5393");
      return cdw;
    }

    CdwTelecoms telecoms() {
      CdwTelecoms cdw = new CdwTelecoms();
      cdw.getTelecom().add(telecom());
      return cdw;
    }

    CdwType type() {
      CdwType cdw = new CdwType();
      cdw.getCoding().add(typeCoding());
      return cdw;
    }

    CdwType.CdwCoding typeCoding() {
      CdwType.CdwCoding cdw = new CdwType.CdwCoding();
      cdw.setSystem("http://hl7.org/fhir/v3/RoleCode");
      cdw.setDisplay(CdwLocationTypeDisplay.GENERAL_INTERNAL_MEDICINE_CLINIC);
      cdw.setCode(CdwLocationTypeCode.GIM);
      return cdw;
    }
  }

  @NoArgsConstructor(staticName = "get", access = AccessLevel.PUBLIC)
  public static class Expected {
    Address address() {
      return Address.builder()
          .city("ALBANY")
          .state("NEW YORK")
          .postalCode("12208")
          .line(asList("113 Holland Avenue"))
          .build();
    }

    public Location location() {
      return Location.builder()
          .resourceType("Location")
          .id("166365:L")
          .address(address())
          .description("VAMC ALBANY")
          .managingOrganization(reference("Organization/185576:I", "ZZ ALBANY"))
          .mode(Mode.instance)
          .name("VAMC ALBANY")
          .physicalType(physicalType())
          .status(Status.active)
          .telecom(telecom())
          .type(type())
          .build();
    }

    CodeableConcept physicalType() {
      return CodeableConcept.builder().text("").coding(physicalTypeCoding()).build();
    }

    List<Coding> physicalTypeCoding() {
      return Collections.singletonList(
          Coding.builder()
              .display("Room")
              .system("https://www.hl7.org/fhir/DSTU2/valueset-location-physical-type.html")
              .code("ro")
              .build());
    }

    Reference reference(String ref, String display) {
      return Reference.builder().reference(ref).display(display).build();
    }

    List<ContactPoint> telecom() {
      return Collections.singletonList(
          ContactPoint.builder().system(ContactPointSystem.phone).value("402-995-5393").build());
    }

    CodeableConcept type() {
      return CodeableConcept.builder().coding(typeCoding()).build();
    }

    List<Coding> typeCoding() {
      return Collections.singletonList(
          Coding.builder()
              .display("General internal medicine clinic")
              .system("http://hl7.org/fhir/v3/RoleCode")
              .code("GIM")
              .build());
    }
  }
}
