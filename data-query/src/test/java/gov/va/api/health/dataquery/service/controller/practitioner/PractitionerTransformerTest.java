package gov.va.api.health.dataquery.service.controller.practitioner;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dstu2.api.datatypes.Address;
import gov.va.api.health.dstu2.api.datatypes.Address.AddressUse;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.datatypes.ContactPoint;
import gov.va.api.health.dstu2.api.datatypes.ContactPoint.ContactPointSystem;
import gov.va.api.health.dstu2.api.datatypes.ContactPoint.ContactPointUse;
import gov.va.api.health.dstu2.api.datatypes.HumanName;
import gov.va.api.health.dstu2.api.datatypes.HumanName.NameUse;
import gov.va.api.health.dstu2.api.elements.Reference;
import gov.va.api.health.dstu2.api.resources.Practitioner;
import gov.va.api.health.dstu2.api.resources.Practitioner.Gender;
import gov.va.api.health.dstu2.api.resources.Practitioner.PractitionerRole;
import gov.va.dvp.cdw.xsd.model.CdwPractitioner100Root.CdwPractitioners.CdwPractitioner;
import gov.va.dvp.cdw.xsd.model.CdwPractitioner100Root.CdwPractitioners.CdwPractitioner.CdwAddresses;
import gov.va.dvp.cdw.xsd.model.CdwPractitioner100Root.CdwPractitioners.CdwPractitioner.CdwAddresses.CdwAddress;
import gov.va.dvp.cdw.xsd.model.CdwPractitioner100Root.CdwPractitioners.CdwPractitioner.CdwAddresses.CdwAddress.CdwLines;
import gov.va.dvp.cdw.xsd.model.CdwPractitioner100Root.CdwPractitioners.CdwPractitioner.CdwName;
import gov.va.dvp.cdw.xsd.model.CdwPractitioner100Root.CdwPractitioners.CdwPractitioner.CdwPractitionerRoles;
import gov.va.dvp.cdw.xsd.model.CdwPractitioner100Root.CdwPractitioners.CdwPractitioner.CdwPractitionerRoles.CdwPractitionerRole;
import gov.va.dvp.cdw.xsd.model.CdwPractitioner100Root.CdwPractitioners.CdwPractitioner.CdwPractitionerRoles.CdwPractitionerRole.CdwHealthcareServices;
import gov.va.dvp.cdw.xsd.model.CdwPractitioner100Root.CdwPractitioners.CdwPractitioner.CdwPractitionerRoles.CdwPractitionerRole.CdwLocations;
import gov.va.dvp.cdw.xsd.model.CdwPractitioner100Root.CdwPractitioners.CdwPractitioner.CdwTelecoms;
import gov.va.dvp.cdw.xsd.model.CdwPractitioner100Root.CdwPractitioners.CdwPractitioner.CdwTelecoms.CdwTelecom;
import gov.va.dvp.cdw.xsd.model.CdwPractitionerAddressUse;
import gov.va.dvp.cdw.xsd.model.CdwPractitionerGender;
import gov.va.dvp.cdw.xsd.model.CdwPractitionerNameUse;
import gov.va.dvp.cdw.xsd.model.CdwPractitionerRoleCoding;
import gov.va.dvp.cdw.xsd.model.CdwPractitionerRoleCoding.CdwCoding;
import gov.va.dvp.cdw.xsd.model.CdwPractitionerRoleCodingCode;
import gov.va.dvp.cdw.xsd.model.CdwPractitionerRoleCodingDisplay;
import gov.va.dvp.cdw.xsd.model.CdwPractitionerTelecomSystem;
import gov.va.dvp.cdw.xsd.model.CdwPractitionerTelecomUse;
import gov.va.dvp.cdw.xsd.model.CdwReference;
import java.util.List;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.SneakyThrows;
import org.junit.Test;

public class PractitionerTransformerTest {
  private PractitionerTransformer tx = new PractitionerTransformer();
  private CdwSampleData cdw = new CdwSampleData();
  private Expected expected = new Expected();

  @Test
  public void address() {
    assertThat(tx.addresses(null)).isNull();
    assertThat(tx.addresses(new CdwAddresses())).isNull();
    assertThat(tx.addresses(cdw.addresses())).isEqualTo(expected.addresses());
  }

  @Test
  public void addressLines() {
    assertThat(tx.addressLines(null)).isNull();
    assertThat(tx.addressLines(new CdwLines())).isNull();
    assertThat(tx.addressLines(cdw.addressLines())).isEqualTo(expected.addresses().get(0).line());
  }

  @Test
  public void addressUse() {
    assertThat(tx.addressUse(CdwPractitionerAddressUse.HOME)).isEqualTo(AddressUse.home);
    assertThat(tx.addressUse(CdwPractitionerAddressUse.OLD)).isEqualTo(AddressUse.old);
    assertThat(tx.addressUse(CdwPractitionerAddressUse.TEMP)).isEqualTo(AddressUse.temp);
    assertThat(tx.addressUse(CdwPractitionerAddressUse.WORK)).isEqualTo(AddressUse.work);
  }

  @Test
  public void gender() {
    assertThat(tx.gender(CdwPractitionerGender.FEMALE)).isEqualTo(Gender.female);
    assertThat(tx.gender(CdwPractitionerGender.MALE)).isEqualTo(Gender.male);
    assertThat(tx.gender(CdwPractitionerGender.OTHER)).isEqualTo(Gender.other);
    assertThat(tx.gender(CdwPractitionerGender.UNKNOWN)).isEqualTo(Gender.unknown);
  }

  @Test
  public void healthcareServices() {
    assertThat(tx.healthcareService(null)).isNull();
    assertThat(tx.healthcareService(new CdwHealthcareServices())).isNull();
    assertThat(tx.healthcareService(cdw.healthcareServices()))
        .isEqualTo(expected.healthcareServices());
  }

  @Test
  public void locations() {
    assertThat(tx.locations(null)).isNull();
    assertThat(tx.locations(new CdwLocations())).isNull();
    assertThat(tx.locations(cdw.locations())).isEqualTo(expected.locations());
  }

  @Test
  public void managingOrganization() {
    assertThat(tx.managingOrganization(null)).isNull();
    assertThat(tx.managingOrganization(new CdwReference())).isNull();
    assertThat(tx.managingOrganization(cdw.managingOrganization()))
        .isEqualTo(expected.managingOrganization());
  }

  @Test
  public void name() {
    assertThat(tx.name(null)).isNull();
    assertThat(tx.name(new CdwName())).isNull();
    assertThat(tx.name(cdw.name())).isEqualTo(expected.name());
  }

  @Test
  public void nameList() {
    assertThat(tx.nameList(cdw.name().getSuffix())).isEqualTo(expected.name().suffix());
    assertThat(tx.nameList(null)).isNull();
    assertThat(tx.nameList("")).isNull();
  }

  @Test
  public void nameUse() {
    assertThat(tx.nameUse(CdwPractitionerNameUse.ANONYMOUS)).isEqualTo(NameUse.anonymous);
    assertThat(tx.nameUse(CdwPractitionerNameUse.MAIDEN)).isEqualTo(NameUse.maiden);
    assertThat(tx.nameUse(CdwPractitionerNameUse.NICKNAME)).isEqualTo(NameUse.nickname);
    assertThat(tx.nameUse(CdwPractitionerNameUse.OFFICIAL)).isEqualTo(NameUse.official);
    assertThat(tx.nameUse(CdwPractitionerNameUse.OLD)).isEqualTo(NameUse.old);
    assertThat(tx.nameUse(CdwPractitionerNameUse.TEMP)).isEqualTo(NameUse.temp);
    assertThat(tx.nameUse(CdwPractitionerNameUse.USUAL)).isEqualTo(NameUse.usual);
  }

  @Test
  public void practitioner() {
    assertThat(tx.apply(cdw.practitioner())).isEqualTo(expected.practitioner());
  }

  @Test
  public void practitionerRole() {
    assertThat(tx.practitionerRole(null)).isNull();
    assertThat(tx.practitionerRole(new CdwPractitionerRole())).isNull();
    assertThat(tx.practitionerRole(cdw.practitionerRole()))
        .isEqualTo(expected.practitionerRoles().get(0));
  }

  @Test
  public void practitionerRoles() {
    assertThat(tx.practitionerRoles(null)).isNull();
    assertThat(tx.practitionerRoles(new CdwPractitionerRoles())).isNull();
    assertThat(tx.practitionerRoles(cdw.practitionerRoles()))
        .isEqualTo(expected.practitionerRoles());
  }

  @Test
  public void role() {
    assertThat(tx.role(null)).isNull();
    assertThat(tx.role(new CdwPractitionerRoleCoding())).isNull();
    assertThat(tx.role(cdw.role())).isEqualTo(expected.role());
  }

  @Test
  public void roleCoding() {
    assertThat(tx.roleCoding(null)).isNull();
    assertThat(tx.roleCoding(new CdwCoding())).isNull();
    assertThat(tx.roleCoding(cdw.roleCoding())).isEqualTo(expected.roleCoding());
  }

  @Test
  public void telecom() {
    assertThat(tx.telecom(null)).isNull();
    assertThat(tx.telecom(new CdwTelecom())).isNull();
    assertThat(tx.telecom(cdw.telecom())).isEqualTo(expected.telecom());
  }

  @Test
  public void telecomSystem() {
    assertThat(tx.telecomSystem(CdwPractitionerTelecomSystem.EMAIL))
        .isEqualTo(ContactPointSystem.email);
    assertThat(tx.telecomSystem(CdwPractitionerTelecomSystem.FAX))
        .isEqualTo(ContactPointSystem.fax);
    assertThat(tx.telecomSystem(CdwPractitionerTelecomSystem.OTHER))
        .isEqualTo(ContactPointSystem.other);
    assertThat(tx.telecomSystem(CdwPractitionerTelecomSystem.PAGER))
        .isEqualTo(ContactPointSystem.pager);
    assertThat(tx.telecomSystem(CdwPractitionerTelecomSystem.PHONE))
        .isEqualTo(ContactPointSystem.phone);
  }

  @Test
  public void telecomUse() {
    assertThat(tx.telecomUse(CdwPractitionerTelecomUse.HOME)).isEqualTo(ContactPointUse.home);
    assertThat(tx.telecomUse(CdwPractitionerTelecomUse.MOBILE)).isEqualTo(ContactPointUse.mobile);
    assertThat(tx.telecomUse(CdwPractitionerTelecomUse.OLD)).isEqualTo(ContactPointUse.old);
    assertThat(tx.telecomUse(CdwPractitionerTelecomUse.TEMP)).isEqualTo(ContactPointUse.temp);
    assertThat(tx.telecomUse(CdwPractitionerTelecomUse.WORK)).isEqualTo(ContactPointUse.work);
  }

  @Test
  public void telecoms() {
    assertThat(tx.telecoms(null)).isNull();
    assertThat(tx.telecoms(new CdwTelecoms())).isNull();
    assertThat(tx.telecoms(cdw.telecoms())).isEqualTo(expected.telecoms());
  }

  private static class CdwSampleData {
    CdwAddress address() {
      CdwAddress address = new CdwAddress();
      address.setLines(addressLines());
      address.setCity("Melbourne");
      address.setState("FL");
      address.setPostalCode("32904");
      return address;
    }

    CdwLines addressLines() {
      CdwLines lines = new CdwLines();
      lines.getLine().add("Address Line");
      return lines;
    }

    CdwAddresses addresses() {
      CdwAddresses addresses = new CdwAddresses();
      addresses.getAddress().add(address());
      return addresses;
    }

    @SneakyThrows
    private XMLGregorianCalendar birthDate(String s) {
      return DatatypeFactory.newInstance().newXMLGregorianCalendar(s);
    }

    CdwReference cdwReference(String reference, String display) {
      CdwReference cdwReference = new CdwReference();
      cdwReference.setReference(reference);
      cdwReference.setDisplay(display);
      return cdwReference;
    }

    CdwHealthcareServices healthcareServices() {
      CdwHealthcareServices healthcareServices = new CdwHealthcareServices();
      CdwReference healthcareService =
          cdwReference("HealthcareService/0", "*Unknown at this time*");
      healthcareServices.getHealthcareService().add(healthcareService);
      return healthcareServices;
    }

    CdwLocations locations() {
      CdwLocations locations = new CdwLocations();
      CdwReference location = cdwReference("Location/0", "*Unknown at this time*");
      locations.getLocation().add(location);
      return locations;
    }

    CdwReference managingOrganization() {
      return cdwReference("Organization/0", "*Unknown at this time*");
    }

    CdwName name() {
      CdwName name = new CdwName();
      name.setFamily("Hello Family");
      name.setGiven("Hello Given");
      name.setPrefix("Hello Prefix");
      name.setSuffix("Hello Suffix");
      name.setText("Hello Text");
      name.setUse(CdwPractitionerNameUse.OFFICIAL);
      return name;
    }

    CdwPractitioner practitioner() {
      CdwPractitioner cdw = new CdwPractitioner();
      cdw.setCdwId("1234");
      cdw.setActive(true);
      cdw.setName(name());
      cdw.setTelecoms(telecoms());
      cdw.setAddresses(addresses());
      cdw.setGender(CdwPractitionerGender.UNKNOWN);
      cdw.setBirthDate(birthDate("1999-03-29T18:23:27Z"));
      cdw.setPractitionerRoles(practitionerRoles());
      return cdw;
    }

    CdwPractitionerRole practitionerRole() {
      CdwPractitionerRole practitionerRole = new CdwPractitionerRole();
      practitionerRole.setHealthcareServices(healthcareServices());
      practitionerRole.setLocations(locations());
      practitionerRole.setManagingOrganization(managingOrganization());
      practitionerRole.setRole(role());
      return practitionerRole;
    }

    CdwPractitionerRoles practitionerRoles() {
      CdwPractitionerRoles practitionerRoles = new CdwPractitionerRoles();
      practitionerRoles.getPractitionerRole().add(practitionerRole());
      return practitionerRoles;
    }

    CdwPractitionerRoleCoding role() {
      CdwPractitionerRoleCoding role = new CdwPractitionerRoleCoding();
      role.setCoding(roleCoding());
      return role;
    }

    CdwCoding roleCoding() {
      CdwCoding coding = new CdwCoding();
      coding.setCode(CdwPractitionerRoleCodingCode.DOCTOR);
      coding.setDisplay(CdwPractitionerRoleCodingDisplay.DOCTOR);
      coding.setSystem("Role System");
      return coding;
    }

    private CdwTelecom telecom() {
      CdwTelecom telecom = new CdwTelecom();
      telecom.setSystem(CdwPractitionerTelecomSystem.PHONE);
      telecom.setUse(CdwPractitionerTelecomUse.MOBILE);
      telecom.setValue("Hello Telecom Value");
      return telecom;
    }

    CdwTelecoms telecoms() {
      CdwTelecoms telecoms = new CdwTelecoms();
      telecoms.getTelecom().add(telecom());
      return telecoms;
    }
  }

  private static class Expected {
    List<Address> addresses() {
      return singletonList(
          Address.builder()
              .line(singletonList("Address Line"))
              .city("Melbourne")
              .state("FL")
              .postalCode("32904")
              .build());
    }

    List<Reference> healthcareServices() {
      return singletonList(reference("HealthcareService/0", "*Unknown at this time*"));
    }

    List<Reference> locations() {
      return singletonList(reference("Location/0", "*Unknown at this time*"));
    }

    Reference managingOrganization() {
      return reference("Organization/0", "*Unknown at this time*");
    }

    HumanName name() {
      return HumanName.builder()
          .family(singletonList("Hello Family"))
          .given(singletonList("Hello Given"))
          .prefix(singletonList("Hello Prefix"))
          .suffix(singletonList("Hello Suffix"))
          .text("Hello Text")
          .use(NameUse.official)
          .build();
    }

    Practitioner practitioner() {
      return Practitioner.builder()
          .resourceType("Practitioner")
          .id("1234")
          .active(true)
          .name(name())
          .telecom(telecoms())
          .address(addresses())
          .gender(Gender.unknown)
          .birthDate("1999-03-29T18:23:27Z")
          .practitionerRole(practitionerRoles())
          .build();
    }

    List<PractitionerRole> practitionerRoles() {
      return singletonList(
          PractitionerRole.builder()
              .healthcareService(healthcareServices())
              .location(locations())
              .managingOrganization(managingOrganization())
              .role(role())
              .build());
    }

    private Reference reference(String reference, String display) {
      return Reference.builder().reference(reference).display(display).build();
    }

    CodeableConcept role() {
      return CodeableConcept.builder().coding(roleCoding()).build();
    }

    List<Coding> roleCoding() {
      return singletonList(
          Coding.builder().code("doctor").system("Role System").display("Doctor").build());
    }

    private ContactPoint telecom() {
      return ContactPoint.builder()
          .system(ContactPointSystem.phone)
          .value("Hello Telecom Value")
          .use(ContactPointUse.mobile)
          .build();
    }

    List<ContactPoint> telecoms() {
      return singletonList(telecom());
    }
  }
}
