package gov.va.api.health.dataquery.service.controller.practitionerrole;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.Stu3Bundler;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.practitioner.DatamartPractitioner;
import gov.va.api.health.dataquery.service.controller.practitioner.PractitionerEntity;
import gov.va.api.health.dataquery.service.controller.practitioner.PractitionerRepository;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.stu3.api.bundle.AbstractBundle;
import gov.va.api.health.stu3.api.bundle.BundleLink;
import gov.va.api.health.stu3.api.resources.PractitionerRole;
import gov.va.api.lighthouse.datamart.CompositeCdwId;
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
  @Autowired private PractitionerRepository repository;

  private IdentityService ids = mock(IdentityService.class);

  @SneakyThrows
  static PractitionerEntity asEntity(DatamartPractitioner dm) {
    return PractitionerEntity.builder()
        .cdwIdNumber(CompositeCdwId.fromCdwId(dm.cdwId()).cdwIdNumber())
        .cdwIdResourceCode(CompositeCdwId.fromCdwId(dm.cdwId()).cdwIdResourceCode())
        .npi(dm.npi().orElse(null))
        .familyName(dm.name().family())
        .givenName(dm.name().given())
        .payload(JacksonConfig.createMapper().writeValueAsString(dm))
        .build();
  }

  @SneakyThrows
  static String asJson(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  static PractitionerRole.Bundle emptyBundle(String linkBase) {
    return PractitionerRole.Bundle.builder()
        .resourceType("Bundle")
        .type(AbstractBundle.BundleType.searchset)
        .total(1)
        .link(
            asList(PractitionerRoleSamples.Stu3.link(BundleLink.LinkRelation.self, linkBase, 1, 0)))
        .entry(emptyList())
        .build();
  }

  void _addMockIdentities(
      String pracPubId,
      String pracCdwId,
      String locPubId,
      String locCdwId,
      String orgPubId,
      String orgCdwId) {
    ResourceIdentity pracResource =
        ResourceIdentity.builder()
            .system("CDW")
            .resource("PRACTITIONER")
            .identifier(pracCdwId)
            .build();
    ResourceIdentity locResource =
        ResourceIdentity.builder().system("CDW").resource("LOCATION").identifier(locCdwId).build();
    ResourceIdentity orgResource =
        ResourceIdentity.builder()
            .system("CDW")
            .resource("ORGANIZATION")
            .identifier(orgCdwId)
            .build();
    when(ids.lookup(pracPubId)).thenReturn(List.of(pracResource));
    when(ids.register(any()))
        .thenReturn(
            List.of(
                Registration.builder()
                    .uuid(pracPubId)
                    .resourceIdentities(List.of(pracResource))
                    .build(),
                Registration.builder()
                    .uuid(locPubId)
                    .resourceIdentities(List.of(locResource))
                    .build(),
                Registration.builder()
                    .uuid(orgPubId)
                    .resourceIdentities(List.of(orgResource))
                    .build()));
  }

  @SneakyThrows
  static DatamartPractitioner asObject(String json) {
    return JacksonConfig.createMapper().readValue(json, DatamartPractitioner.class);
  }

  private Stu3PractitionerRoleController _controller() {
    return new Stu3PractitionerRoleController(
        new Stu3Bundler(
            new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool", "cool", "cool")),
        repository,
        WitnessProtection.builder().identityService(ids).build());
  }

  static String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  @Test
  void read() {
    String publicId = "I2-111";
    String cdwId = "111:S";
    String locPubId = "I2-222";
    String locCdwId = "222:L";
    String orgPubId = "I2-333";
    String orgCdwId = "333:O";
    _addMockIdentities(publicId, cdwId, locPubId, locCdwId, orgPubId, orgCdwId);
    DatamartPractitioner dm =
        PractitionerRoleSamples.Datamart.create().practitioner(cdwId, locCdwId, orgCdwId);
    repository.save(asEntity(dm));
    PractitionerRole actual = _controller().read(publicId);
    assertThat(actual)
        .isEqualTo(
            PractitionerRoleSamples.Stu3.create().practitionerRole(publicId, locPubId, orgPubId));
  }

  @Test
  void readRaw() {
    String publicId = "I2-111";
    String cdwId = "111:S";
    String locPubId = "I2-222";
    String locCdwId = "222:L";
    String orgPubId = "I2-333";
    String orgCdwId = "333:O";
    _addMockIdentities(publicId, cdwId, locPubId, locCdwId, orgPubId, orgCdwId);
    HttpServletResponse servletResponse = mock(HttpServletResponse.class);
    DatamartPractitioner dm =
        PractitionerRoleSamples.Datamart.create().practitioner(cdwId, locCdwId, orgCdwId);
    repository.save(asEntity(dm));
    String json = _controller().readRaw(publicId, servletResponse);
    assertThat(asObject(json)).isEqualTo(dm);
    verify(servletResponse).addHeader("X-VA-INCLUDES-ICN", "NONE");
  }

  @Test
  void readRawThrowsNotFoundWhenDataIsMissing() {
    _addMockIdentities("x", "x", "y", "y", "z", "z");
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
    _addMockIdentities("x", "x", "y", "y", "z", "z");
    assertThrows(ResourceExceptions.NotFound.class, () -> _controller().read("x"));
  }

  @Test
  void readThrowsNotFoundWhenIdIsUnknown() {
    assertThrows(ResourceExceptions.NotFound.class, () -> _controller().read("x"));
  }

  @Test
  void searchById() {
    String publicId = "I2-111";
    String cdwId = "111:S";
    String locPubId = "I2-222";
    String locCdwId = "222:L";
    String orgPubId = "I2-333";
    String orgCdwId = "333:O";
    _addMockIdentities(publicId, cdwId, locPubId, locCdwId, orgPubId, orgCdwId);
    DatamartPractitioner dm =
        PractitionerRoleSamples.Datamart.create().practitioner(cdwId, locCdwId, orgCdwId);
    repository.save(asEntity(dm));
    assertThat(asJson(_controller().searchById(publicId, 1, 0)))
        .isEqualTo(
            asJson(emptyBundle("http://fonzy.com/cool/PractitionerRole?identifier=" + publicId)));
    assertThat(asJson(_controller().searchById(publicId, 1, 1)))
        .isEqualTo(
            asJson(
                PractitionerRoleSamples.Stu3.asBundle(
                    "http://fonzy.com/cool",
                    List.of(
                        PractitionerRoleSamples.Stu3.create()
                            .practitionerRole(publicId, locPubId, orgPubId)),
                    PractitionerRoleSamples.Stu3.link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/PractitionerRole?identifier=" + publicId,
                        1,
                        1),
                    PractitionerRoleSamples.Stu3.link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/PractitionerRole?identifier=" + publicId,
                        1,
                        1),
                    PractitionerRoleSamples.Stu3.link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/PractitionerRole?identifier=" + publicId,
                        1,
                        1))));
  }

  @Test
  void searchByIdentifier() {
    String publicId = "I2-111";
    String cdwId = "111:S";
    String locPubId = "I2-222";
    String locCdwId = "222:L";
    String orgPubId = "I2-333";
    String orgCdwId = "333:O";
    _addMockIdentities(publicId, cdwId, locPubId, locCdwId, orgPubId, orgCdwId);
    DatamartPractitioner dm =
        PractitionerRoleSamples.Datamart.create().practitioner(cdwId, locCdwId, orgCdwId);
    repository.save(asEntity(dm));
    assertThat(asJson(_controller().searchByIdentifier(publicId, 1, 0)))
        .isEqualTo(
            asJson(emptyBundle("http://fonzy.com/cool/PractitionerRole?identifier=" + publicId)));
    assertThat(asJson(_controller().searchByIdentifier(publicId, 1, 1)))
        .isEqualTo(
            asJson(
                PractitionerRoleSamples.Stu3.asBundle(
                    "http://fonzy.com/cool",
                    List.of(
                        PractitionerRoleSamples.Stu3.create()
                            .practitionerRole(publicId, locPubId, orgPubId)),
                    PractitionerRoleSamples.Stu3.link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/PractitionerRole?identifier=" + publicId,
                        1,
                        1),
                    PractitionerRoleSamples.Stu3.link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/PractitionerRole?identifier=" + publicId,
                        1,
                        1),
                    PractitionerRoleSamples.Stu3.link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/PractitionerRole?identifier=" + publicId,
                        1,
                        1))));
  }

  @Test
  void searchByName() {
    String family = "Nelson";
    String given = "Bob";
    String publicId = "I2-111";
    String cdwId = "111:S";
    String locPubId = "I2-222";
    String locCdwId = "222:L";
    String orgPubId = "I2-333";
    String orgCdwId = "333:O";
    _addMockIdentities(publicId, cdwId, locPubId, locCdwId, orgPubId, orgCdwId);
    DatamartPractitioner dm =
        PractitionerRoleSamples.Datamart.create().practitioner(cdwId, locCdwId, orgCdwId);
    repository.save(asEntity(dm));
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
                            .practitionerRole(publicId, locPubId, orgPubId)),
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
    String systemAndCode = "http://hl7.org/fhir/sid/us-npi|12345";
    String publicId = "I2-111";
    String cdwId = "111:S";
    String locPubId = "I2-222";
    String locCdwId = "222:L";
    String orgPubId = "I2-333";
    String orgCdwId = "333:O";
    _addMockIdentities(publicId, cdwId, locPubId, locCdwId, orgPubId, orgCdwId);
    DatamartPractitioner dm =
        PractitionerRoleSamples.Datamart.create().practitioner(cdwId, locCdwId, orgCdwId);
    repository.save(asEntity(dm));
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
                            .practitionerRole(publicId, locPubId, orgPubId)),
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
