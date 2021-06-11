package gov.va.api.health.dataquery.service.controller.practitioner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.Dstu2Bundler;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dstu2.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.dstu2.api.resources.Practitioner;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.lighthouse.datamart.CompositeCdwId;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@DataJpaTest
@ExtendWith(SpringExtension.class)
public class Dstu2PractitionerControllerTest {
  HttpServletResponse response = mock(HttpServletResponse.class);

  IdentityService ids = mock(IdentityService.class);

  @Autowired PractitionerRepository repository;

  @SneakyThrows
  static PractitionerEntity asEntity(DatamartPractitioner dm) {
    CompositeCdwId cdwId = CompositeCdwId.fromCdwId(dm.cdwId());
    return PractitionerEntity.builder()
        .cdwIdNumber(cdwId.cdwIdNumber())
        .cdwIdResourceCode(cdwId.cdwIdResourceCode())
        .familyName("Joe")
        .givenName("Johnson")
        .npi("1234567")
        .payload(JacksonConfig.createMapper().writeValueAsString(dm))
        .build();
  }

  @SneakyThrows
  static String json(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  @SneakyThrows
  private static DatamartPractitioner toObject(String json) {
    return JacksonConfig.createMapper().readValue(json, DatamartPractitioner.class);
  }

  Dstu2PractitionerController _controller() {
    return new Dstu2PractitionerController(
        new Dstu2Bundler(
            new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool", "cool", "cool")),
        repository,
        WitnessProtection.builder().identityService(ids).build());
  }

  void _mockPractitionerIdentity(
      String practPubID,
      String practCdwId,
      String orgPubId,
      String orgCdwId,
      String locPubId,
      String locCdwId) {
    ResourceIdentity practResource =
        ResourceIdentity.builder()
            .system("CDW")
            .resource("PRACTITIONER")
            .identifier(practCdwId)
            .build();
    ResourceIdentity orgResource =
        ResourceIdentity.builder()
            .system("CDW")
            .resource("ORGANIZATION")
            .identifier(orgCdwId)
            .build();
    ResourceIdentity locResource =
        ResourceIdentity.builder().system("CDW").resource("LOCATION").identifier(locCdwId).build();
    when(ids.lookup(practPubID)).thenReturn(List.of(practResource));
    when(ids.register(Mockito.any()))
        .thenReturn(
            List.of(
                Registration.builder()
                    .uuid(practPubID)
                    .resourceIdentities(List.of(practResource))
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

  @Test
  void read() {
    String publicId = "I2-abc";
    String cdwId = "111:S";
    String orgPubId = "I2-def";
    String orgCdwId = "222:O";
    String locPubId = "I2-ghi";
    String locCdwId = "333:L";
    DatamartPractitioner dm = PractitionerSamples.Datamart.create().practitioner();
    repository.save(asEntity(dm));
    _mockPractitionerIdentity(publicId, cdwId, orgPubId, orgCdwId, locPubId, locCdwId);
    Practitioner actual = _controller().read("123:S");
    assertThat(actual).isEqualTo(PractitionerSamples.Dstu2.create().practitioner("123:S"));
  }

  @Test
  void readRaw() {
    String publicId = "I2-abc";
    String cdwId = "123:S";
    String orgPubId = "I2-def";
    String orgCdwId = "456:O";
    String locPubId = "I2-ghi";
    String locCdwId = "789:L";
    _mockPractitionerIdentity(publicId, cdwId, orgPubId, orgCdwId, locPubId, locCdwId);
    HttpServletResponse servletResponse = mock(HttpServletResponse.class);
    DatamartPractitioner dm = PractitionerSamples.Datamart.create().practitioner(cdwId);
    repository.save(asEntity(dm));
    String json = _controller().readRaw(publicId, servletResponse);
    assertThat(toObject(json)).isEqualTo(dm);
    verify(servletResponse).addHeader("X-VA-INCLUDES-ICN", "NONE");
  }

  @Test
  void readRawThrowsNotFoundWhenDataIsMissing() {
    _mockPractitionerIdentity("x", "x", "x", "x", "x", "x");
    assertThrows(ResourceExceptions.NotFound.class, () -> _controller().readRaw("x", response));
  }

  @Test
  void readRawThrowsNotFoundWhenIdIsUnknown() {
    assertThrows(ResourceExceptions.NotFound.class, () -> _controller().readRaw("x", response));
  }

  @Test
  void readThrowsNotFoundWhenDataIsMissing() {
    _mockPractitionerIdentity("x", "x", "x", "x", "x", "x");
    assertThrows(ResourceExceptions.NotFound.class, () -> _controller().read("x"));
  }

  @Test
  void readThrowsNotFoundWhenIdIsUnknown() {
    assertThrows(ResourceExceptions.NotFound.class, () -> _controller().read("x"));
  }

  @Test
  void searchById() {
    String publicId = "I2-abc";
    String cdwId = "123:S";
    String orgPubId = "I2-def";
    String orgCdwId = "456:O";
    String locPubId = "I2-ghi";
    String locCdwId = "789:L";
    _mockPractitionerIdentity(publicId, cdwId, orgPubId, orgCdwId, locPubId, locCdwId);
    DatamartPractitioner dm = PractitionerSamples.Datamart.create().practitioner(cdwId);
    repository.save(asEntity(dm));
    Practitioner.Bundle actual = _controller().searchById(publicId, 1, 1);
    assertThat(json(actual))
        .isEqualTo(
            json(
                PractitionerSamples.Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    List.of(PractitionerSamples.Dstu2.create().practitioner(publicId)),
                    PractitionerSamples.Dstu2.link(
                        LinkRelation.first,
                        "http://fonzy.com/cool/Practitioner?identifier=I2-abc",
                        1,
                        1),
                    PractitionerSamples.Dstu2.link(
                        LinkRelation.self,
                        "http://fonzy.com/cool/Practitioner?identifier=I2-abc",
                        1,
                        1),
                    PractitionerSamples.Dstu2.link(
                        LinkRelation.last,
                        "http://fonzy.com/cool/Practitioner?identifier=I2-abc",
                        1,
                        1))));
  }

  @Test
  void searchByIdentifier() {
    String publicId = "I2-abc";
    String cdwId = "123:S";
    String orgPubId = "I2-def";
    String orgCdwId = "456:O";
    String locPubId = "I2-ghi";
    String locCdwId = "789:L";
    DatamartPractitioner dm = PractitionerSamples.Datamart.create().practitioner(cdwId);
    repository.save(asEntity(dm));
    _mockPractitionerIdentity(publicId, cdwId, orgPubId, orgCdwId, locPubId, locCdwId);
    Practitioner.Bundle actual = _controller().searchByIdentifier(publicId, 1, 1);
    assertThat(json(actual))
        .isEqualTo(
            json(
                PractitionerSamples.Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    List.of(PractitionerSamples.Dstu2.create().practitioner(publicId)),
                    PractitionerSamples.Dstu2.link(
                        LinkRelation.first,
                        "http://fonzy.com/cool/Practitioner?identifier=I2-abc",
                        1,
                        1),
                    PractitionerSamples.Dstu2.link(
                        LinkRelation.self,
                        "http://fonzy.com/cool/Practitioner?identifier=I2-abc",
                        1,
                        1),
                    PractitionerSamples.Dstu2.link(
                        LinkRelation.last,
                        "http://fonzy.com/cool/Practitioner?identifier=I2-abc",
                        1,
                        1))));
  }
}
