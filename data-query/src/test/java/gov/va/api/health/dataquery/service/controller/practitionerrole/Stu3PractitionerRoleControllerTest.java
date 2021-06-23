package gov.va.api.health.dataquery.service.controller.practitionerrole;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.google.common.collect.ImmutableList;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.Stu3Bundler;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.stu3.api.bundle.AbstractBundle;
import gov.va.api.health.stu3.api.bundle.BundleLink;
import gov.va.api.health.stu3.api.resources.PractitionerRole;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@DataJpaTest
@ExtendWith(SpringExtension.class)
public class Stu3PractitionerRoleControllerTest {
  @Autowired private PractitionerRoleRepository repository;

  private IdentityService ids = mock(IdentityService.class);

  @SneakyThrows
  static String asJson(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  @SneakyThrows
  static DatamartPractitionerRole asObject(String json) {
    return JacksonConfig.createMapper().readValue(json, DatamartPractitionerRole.class);
  }

  static PractitionerRole.Bundle emptyBundle(String linkBase) {
    return PractitionerRole.Bundle.builder()
        .type(AbstractBundle.BundleType.searchset)
        .total(1)
        .link(
            asList(PractitionerRoleSamples.Stu3.link(BundleLink.LinkRelation.self, linkBase, 1, 0)))
        .entry(emptyList())
        .build();
  }

  static String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  static Registration idReg(String type, String pubId, String cdwId) {
    return Registration.builder()
        .uuid(pubId)
        .resourceIdentities(
            List.of(
                ResourceIdentity.builder().system("CDW").resource(type).identifier(cdwId).build()))
        .build();
  }

  private Stu3PractitionerRoleController _controller() {
    return new Stu3PractitionerRoleController(
        new Stu3Bundler(
            new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool", "cool", "cool")),
        repository,
        WitnessProtection.builder().identityService(ids).build());
  }

  void _registerMockIdentities(Registration... regs) {
    for (Registration reg : regs) {
      when(ids.lookup(reg.uuid())).thenReturn(reg.resourceIdentities());
    }
    when(ids.register(any())).thenReturn(ImmutableList.copyOf(regs));
  }

  @Test
  void read() {
    String pracRolePubId = "I2-000";
    String pracRoleCdwId = "000:P";
    String pracPubId = "I2-111";
    String pracCdwId = "111:S";
    String orgPubId = "I2-222";
    String orgCdwId = "222:I";
    String locPubId = "I2-333";
    String locCdwId = "333:L";
    _registerMockIdentities(
        idReg("PRACTITIONER_ROLE", pracRolePubId, pracRoleCdwId),
        idReg("PRACTITIONER", pracPubId, pracCdwId),
        idReg("ORGANIZATION", orgPubId, orgCdwId),
        idReg("LOCATION", locPubId, locCdwId));
    repository.save(
        PractitionerRoleSamples.Datamart.create()
            .entity(pracRoleCdwId, pracCdwId, orgCdwId, locCdwId));
    PractitionerRole actual = _controller().read(pracRolePubId);
    assertThat(actual)
        .isEqualTo(
            PractitionerRoleSamples.Stu3.create()
                .practitionerRole(pracRolePubId, pracPubId, orgPubId, locPubId));
  }

  @Test
  void readRaw() {
    String pracRolePubId = "I2-000";
    String pracRoleCdwId = "000:P";
    String pracPubId = "I2-111";
    String pracCdwId = "111:S";
    String orgPubId = "I2-222";
    String orgCdwId = "222:I";
    String locPubId = "I2-333";
    String locCdwId = "333:L";
    _registerMockIdentities(
        idReg("PRACTITIONER_ROLE", pracRolePubId, pracRoleCdwId),
        idReg("PRACTITIONER", pracPubId, pracCdwId),
        idReg("ORGANIZATION", orgPubId, orgCdwId),
        idReg("LOCATION", locPubId, locCdwId));
    HttpServletResponse servletResponse = mock(HttpServletResponse.class);
    PractitionerRoleEntity entity =
        PractitionerRoleSamples.Datamart.create()
            .entity(pracRoleCdwId, pracCdwId, orgCdwId, locCdwId);
    repository.save(entity);
    String json = _controller().readRaw(pracRolePubId, servletResponse);
    assertThat(asObject(json))
        .isEqualTo(
            PractitionerRoleSamples.Datamart.create()
                .practitionerRole(pracRoleCdwId, pracCdwId, orgCdwId, locCdwId));
    verify(servletResponse).addHeader("X-VA-INCLUDES-ICN", "NONE");
  }

  @Test
  void readRawThrowsNotFoundWhenDataIsMissing() {
    _registerMockIdentities(
        idReg("PRACTITIONER_ROLE", "x", "x"),
        idReg("ORGANIZATION", "y", "y"),
        idReg("LOCATION", "z", "z"));
    assertThrows(
        ResourceExceptions.NotFound.class,
        () -> _controller().readRaw("x", mock(HttpServletResponse.class)));
  }

  @Test
  void readRawThrowsNotFoundWhenIdIsUnknown() {
    assertThrows(
        ResourceExceptions.NotFound.class,
        () -> _controller().readRaw("x", mock(HttpServletResponse.class)));
  }

  @Test
  void readThrowsNotFoundWhenDataIsMissing() {
    _registerMockIdentities(
        idReg("PRACTITIONER_ROLE", "x", "x"),
        idReg("ORGANIZATION", "y", "y"),
        idReg("LOCATION", "z", "z"));
    assertThrows(ResourceExceptions.NotFound.class, () -> _controller().read("x"));
  }

  @Test
  void readThrowsNotFoundWhenIdIsUnknown() {
    assertThrows(ResourceExceptions.NotFound.class, () -> _controller().read("x"));
  }

  @Test
  void searchById() {
    String pracRolePubId = "I2-000";
    String pracRoleCdwId = "000:P";
    String pracPubId = "I2-111";
    String pracCdwId = "111:S";
    String orgPubId = "I2-222";
    String orgCdwId = "222:I";
    String locPubId = "I2-333";
    String locCdwId = "333:L";
    _registerMockIdentities(
        idReg("PRACTITIONER_ROLE", pracRolePubId, pracRoleCdwId),
        idReg("PRACTITIONER", pracPubId, pracCdwId),
        idReg("ORGANIZATION", orgPubId, orgCdwId),
        idReg("LOCATION", locPubId, locCdwId));
    repository.save(
        PractitionerRoleSamples.Datamart.create()
            .entity(pracRoleCdwId, pracCdwId, orgCdwId, locCdwId));
    assertThat(asJson(_controller().searchById(pracRolePubId, 1, 0)))
        .isEqualTo(
            asJson(
                emptyBundle("http://fonzy.com/cool/PractitionerRole?identifier=" + pracRolePubId)));
    assertThat(asJson(_controller().searchById(pracRolePubId, 1, 1)))
        .isEqualTo(
            asJson(
                PractitionerRoleSamples.Stu3.asBundle(
                    "http://fonzy.com/cool",
                    List.of(
                        PractitionerRoleSamples.Stu3.create()
                            .practitionerRole(pracRolePubId, pracPubId, orgPubId, locPubId)),
                    PractitionerRoleSamples.Stu3.link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/PractitionerRole?identifier=" + pracRolePubId,
                        1,
                        1),
                    PractitionerRoleSamples.Stu3.link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/PractitionerRole?identifier=" + pracRolePubId,
                        1,
                        1),
                    PractitionerRoleSamples.Stu3.link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/PractitionerRole?identifier=" + pracRolePubId,
                        1,
                        1))));
  }

  @Test
  void searchByIdentifier() {
    String pracRolePubId = "I2-000";
    String pracRoleCdwId = "000:P";
    String pracPubId = "I2-111";
    String pracCdwId = "111:S";
    String orgPubId = "I2-222";
    String orgCdwId = "222:I";
    String locPubId = "I2-333";
    String locCdwId = "333:L";
    _registerMockIdentities(
        idReg("PRACTITIONER_ROLE", pracRolePubId, pracRoleCdwId),
        idReg("PRACTITIONER", pracPubId, pracCdwId),
        idReg("ORGANIZATION", orgPubId, orgCdwId),
        idReg("LOCATION", locPubId, locCdwId));
    //  DatamartPractitioner dm =
    //     PractitionerSamples.Datamart.create().practitioner(cdwId, orgCdwId, locCdwId);
    repository.save(
        PractitionerRoleSamples.Datamart.create()
            .entity(pracRoleCdwId, pracCdwId, orgCdwId, locCdwId));
    assertThat(asJson(_controller().searchByIdentifier(pracRolePubId, 1, 0)))
        .isEqualTo(
            asJson(
                emptyBundle("http://fonzy.com/cool/PractitionerRole?identifier=" + pracRolePubId)));
    assertThat(asJson(_controller().searchByIdentifier(pracRolePubId, 1, 1)))
        .isEqualTo(
            asJson(
                PractitionerRoleSamples.Stu3.asBundle(
                    "http://fonzy.com/cool",
                    List.of(
                        PractitionerRoleSamples.Stu3.create()
                            .practitionerRole(pracRolePubId, pracPubId, orgPubId, locPubId)),
                    PractitionerRoleSamples.Stu3.link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/PractitionerRole?identifier=" + pracRolePubId,
                        1,
                        1),
                    PractitionerRoleSamples.Stu3.link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/PractitionerRole?identifier=" + pracRolePubId,
                        1,
                        1),
                    PractitionerRoleSamples.Stu3.link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/PractitionerRole?identifier=" + pracRolePubId,
                        1,
                        1))));
  }

  @Test
  void searchByName() {
    String pracRolePubId = "I2-000";
    String pracRoleCdwId = "000:P";
    String pracPubId = "I2-111";
    String pracCdwId = "111:S";
    String orgPubId = "I2-222";
    String orgCdwId = "222:I";
    String locPubId = "I2-333";
    String locCdwId = "333:L";
    String family = "NELSON";
    String given = "BOB";
    _registerMockIdentities(
        idReg("PRACTITIONER_ROLE", pracRolePubId, pracRoleCdwId),
        idReg("PRACTITIONER", pracPubId, pracCdwId),
        idReg("ORGANIZATION", orgPubId, orgCdwId),
        idReg("LOCATION", locPubId, locCdwId));
    repository.save(
        PractitionerRoleSamples.Datamart.create()
            .entity(pracRoleCdwId, pracCdwId, orgCdwId, locCdwId));
    assertThat(asJson(_controller().searchByName(family, given, 1, 0)))
        .isEqualTo(
            asJson(
                emptyBundle(
                    String.format(
                        "http://fonzy.com/cool/PractitionerRole?given=%s&practitioner.family=%s",
                        given, family))));
    assertThat(asJson(_controller().searchByName(family, given, 1, 1)))
        .isEqualTo(
            asJson(
                PractitionerRoleSamples.Stu3.asBundle(
                    "http://fonzy.com/cool",
                    List.of(
                        PractitionerRoleSamples.Stu3.create()
                            .practitionerRole(pracRolePubId, pracPubId, orgPubId, locPubId)),
                    PractitionerRoleSamples.Stu3.link(
                        BundleLink.LinkRelation.first,
                        String.format(
                            "http://fonzy.com/cool/PractitionerRole?given=%s&practitioner.family=%s",
                            given, family),
                        1,
                        1),
                    PractitionerRoleSamples.Stu3.link(
                        BundleLink.LinkRelation.self,
                        String.format(
                            "http://fonzy.com/cool/PractitionerRole?given=%s&practitioner.family=%s",
                            given, family),
                        1,
                        1),
                    PractitionerRoleSamples.Stu3.link(
                        BundleLink.LinkRelation.last,
                        String.format(
                            "http://fonzy.com/cool/PractitionerRole?given=%s&practitioner.family=%s",
                            given, family),
                        1,
                        1))));
  }

  @Test
  void searchByNpi() {
    String pracRolePubId = "I2-000";
    String pracRoleCdwId = "000:P";
    String pracPubId = "I2-111";
    String pracCdwId = "111:S";
    String orgPubId = "I2-222";
    String orgCdwId = "222:I";
    String locPubId = "I2-333";
    String locCdwId = "333:L";
    String systemAndCode = "http://hl7.org/fhir/sid/us-npi|1234567890";
    _registerMockIdentities(
        idReg("PRACTITIONER_ROLE", pracRolePubId, pracRoleCdwId),
        idReg("PRACTITIONER", pracPubId, pracCdwId),
        idReg("ORGANIZATION", orgPubId, orgCdwId),
        idReg("LOCATION", locPubId, locCdwId));
    repository.save(
        PractitionerRoleSamples.Datamart.create()
            .entity(pracRoleCdwId, pracCdwId, orgCdwId, locCdwId));
    assertThat(asJson(_controller().searchByNpi(systemAndCode, 1, 0)))
        .isEqualTo(
            asJson(
                emptyBundle(
                    "http://fonzy.com/cool/PractitionerRole?practitioner.identifier="
                        + encode(systemAndCode))));
    assertThat(asJson(_controller().searchByNpi(systemAndCode, 1, 1)))
        .isEqualTo(
            asJson(
                PractitionerRoleSamples.Stu3.asBundle(
                    "http://fonzy.com/cool",
                    List.of(
                        PractitionerRoleSamples.Stu3.create()
                            .practitionerRole(pracRolePubId, pracPubId, orgPubId, locPubId)),
                    PractitionerRoleSamples.Stu3.link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/PractitionerRole?practitioner.identifier="
                            + encode(systemAndCode),
                        1,
                        1),
                    PractitionerRoleSamples.Stu3.link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/PractitionerRole?practitioner.identifier="
                            + encode(systemAndCode),
                        1,
                        1),
                    PractitionerRoleSamples.Stu3.link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/PractitionerRole?practitioner.identifier="
                            + encode(systemAndCode),
                        1,
                        1))));
  }

  @Test
  void searchByNpi_badSystem() {
    assertThrows(
        ResourceExceptions.BadSearchParameter.class,
        () -> _controller().searchByNpi("not_npi|12345", 1, 1));
  }

  @Test
  void searchByNpi_noDelimiter() {
    assertThrows(
        ResourceExceptions.BadSearchParameter.class,
        () -> _controller().searchByNpi("http://hl7.org/fhir/sid/us-npi", 1, 1));
  }

  @Test
  void searchBySpecialty() {
    assertThrows(
        ResourceExceptions.NotImplemented.class,
        () -> _controller().searchBySpecialty("specialty", 1, 1));
  }
}
