package gov.va.api.health.dataquery.service.controller.practitioner;

import static gov.va.api.health.autoconfig.configuration.JacksonConfig.createMapper;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import java.time.LocalDate;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.Test;

public class Dstu2PractitionerTest {
  private static Dstu2Practitioner sample() {
    return Dstu2Practitioner.builder()
        .cdwId("416704")
        .npi(Optional.of("1932127842"))
        .active(true)
        .name(
            Dstu2Practitioner.Name.builder()
                .family("LASTNAME")
                .given("FIRSTNAME A.")
                .prefix(Optional.of("DR."))
                .suffix(Optional.of("PHD"))
                .build())
        .telecom(
            asList(
                Dstu2Practitioner.Telecom.builder()
                    .system(Dstu2Practitioner.Telecom.System.phone)
                    .value("555-555-1137")
                    .use(Dstu2Practitioner.Telecom.Use.work)
                    .build(),
                Dstu2Practitioner.Telecom.builder()
                    .system(Dstu2Practitioner.Telecom.System.phone)
                    .value("555-4055")
                    .use(Dstu2Practitioner.Telecom.Use.home)
                    .build(),
                Dstu2Practitioner.Telecom.builder()
                    .system(Dstu2Practitioner.Telecom.System.pager)
                    .value("5-541")
                    .use(Dstu2Practitioner.Telecom.Use.mobile)
                    .build()))
        .address(
            asList(
                Dstu2Practitioner.Address.builder()
                    .temp(false)
                    .line1("555 E 5TH ST")
                    .line2("SUITE B")
                    .city("CHEYENNE")
                    .state("WYOMING")
                    .postalCode("82001")
                    .build()))
        .gender(Dstu2Practitioner.Gender.female)
        .birthDate(Optional.of(LocalDate.of(1965, 3, 16)))
        .practitionerRole(
            Optional.of(
                Dstu2Practitioner.PractitionerRole.builder()
                    .managingOrganization(
                        Optional.of(
                            DatamartReference.builder()
                                .reference(Optional.of("561596:I"))
                                .display(Optional.of("CHEYENNE VA MEDICAL"))
                                .build()))
                    .role(
                        Optional.of(
                            DatamartCoding.builder()
                                .system(Optional.of("rpcmm"))
                                .code(Optional.of("37"))
                                .display(Optional.of("PSYCHOLOGIST"))
                                .build()))
                    .specialty(
                        asList(
                            Dstu2Practitioner.PractitionerRole.Specialty.builder()
                                .providerType(Optional.of("Physicians (M.D. and D.O.)"))
                                .classification(Optional.of("Physician/Osteopath"))
                                .areaOfSpecialization(Optional.of("Internal Medicine"))
                                .vaCode(Optional.of("V111500"))
                                .build(),
                            Dstu2Practitioner.PractitionerRole.Specialty.builder()
                                .providerType(Optional.of("Physicians (M.D. and D.O.)"))
                                .classification(Optional.of("Physician/Osteopath"))
                                .areaOfSpecialization(Optional.of("General Practice"))
                                .vaCode(Optional.of("V111000"))
                                .build(),
                            Dstu2Practitioner.PractitionerRole.Specialty.builder()
                                .providerType(Optional.of("Physicians (M.D. and D.O.)"))
                                .classification(Optional.of("Physician/Osteopath"))
                                .areaOfSpecialization(Optional.of("Family Practice"))
                                .vaCode(Optional.of("V110900"))
                                .build(),
                            Dstu2Practitioner.PractitionerRole.Specialty.builder()
                                .providerType(Optional.of("Allopathic & Osteopathic Physicians"))
                                .classification(Optional.of("Family Medicine"))
                                .vaCode(Optional.of("V180700"))
                                .x12Code(Optional.of("207Q00000X"))
                                .build()))
                    .period(
                        Optional.of(
                            Dstu2Practitioner.PractitionerRole.Period.builder()
                                .start(Optional.of(LocalDate.of(1988, 8, 19)))
                                .build()))
                    .location(
                        asList(
                            DatamartReference.builder()
                                .reference(Optional.of("43817:L"))
                                .display(Optional.of("CHEY MEDICAL"))
                                .build(),
                            DatamartReference.builder()
                                .reference(Optional.of("43829:L"))
                                .display(Optional.of("ZZCHY LASTNAME MEDICAL"))
                                .build(),
                            DatamartReference.builder()
                                .reference(Optional.of("43841:L"))
                                .display(Optional.of("ZZCHY WID BACK"))
                                .build()))
                    .healthCareService(Optional.of("MEDICAL SERVICE"))
                    .build()))
        .build();
  }

  @SneakyThrows
  private void assertReadable(String json) {
    Dstu2Practitioner dm =
        createMapper().readValue(getClass().getResourceAsStream(json), Dstu2Practitioner.class);
    assertThat(dm).isEqualTo(sample());
  }

  @Test
  public void lazy() {
    Dstu2Practitioner dm = Dstu2Practitioner.builder().build();
    assertThat(dm.address()).isEmpty();
    assertThat(dm.birthDate()).isEqualTo(empty());
    assertThat(dm.npi()).isEqualTo(empty());
    assertThat(dm.practitionerRole()).isEqualTo(empty());
    assertThat(dm.telecom()).isEmpty();

    Dstu2Practitioner.Name name = Dstu2Practitioner.Name.builder().build();
    assertThat(name.prefix()).isEqualTo(empty());
    assertThat(name.suffix()).isEqualTo(empty());

    Dstu2Practitioner.PractitionerRole role = Dstu2Practitioner.PractitionerRole.builder().build();
    assertThat(role.healthCareService()).isEqualTo(empty());
    assertThat(role.location()).isEmpty();
    assertThat(role.managingOrganization()).isEqualTo(empty());
    assertThat(role.period()).isEqualTo(empty());
    assertThat(role.role()).isEqualTo(empty());
    assertThat(role.specialty()).isEmpty();

    Dstu2Practitioner.PractitionerRole.Period period =
        Dstu2Practitioner.PractitionerRole.Period.builder().build();
    assertThat(period.end()).isEqualTo(empty());
    assertThat(period.start()).isEqualTo(empty());

    Dstu2Practitioner.PractitionerRole.Specialty specialty =
        Dstu2Practitioner.PractitionerRole.Specialty.builder().build();
    assertThat(specialty.areaOfSpecialization()).isEqualTo(empty());
    assertThat(specialty.classification()).isEqualTo(empty());
    assertThat(specialty.providerType()).isEqualTo(empty());
    assertThat(specialty.vaCode()).isEqualTo(empty());
    assertThat(specialty.x12Code()).isEqualTo(empty());
  }

  @Test
  public void unmarshalSample() {
    assertReadable("datamart-practitioner.json");
  }
}
