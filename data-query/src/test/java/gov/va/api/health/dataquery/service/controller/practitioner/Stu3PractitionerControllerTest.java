package gov.va.api.health.dataquery.service.controller.practitioner;

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
import gov.va.api.health.stu3.api.bundle.BundleLink;
import gov.va.api.health.stu3.api.resources.Practitioner;
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
public class Stu3PractitionerControllerTest {
  HttpServletResponse response = mock(HttpServletResponse.class);

  IdentityService ids = mock(IdentityService.class);

  @Autowired PractitionerRepository repository;

  static Registration idReg(String type, String pubId, String cdwId) {
    return Registration.builder()
        .uuid(pubId)
        .resourceIdentities(
            List.of(
                ResourceIdentity.builder().system("CDW").resource(type).identifier(cdwId).build()))
        .build();
  }

  @SneakyThrows
  static String json(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  @SneakyThrows
  static DatamartPractitioner toObject(String json) {
    return JacksonConfig.createMapper().readValue(json, DatamartPractitioner.class);
  }

  Stu3PractitionerController _controller() {
    return new Stu3PractitionerController(
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
    String publicId = "I2-abc";
    String cdwId = "111:S";
    String orgPubId = "I2-def";
    String orgCdwId = "222:I";
    String locPubId = "I2-ghi";
    String locCdwId = "333:L";
    _registerMockIdentities(
        idReg("PRACTITIONER", publicId, cdwId),
        idReg("ORGANIZATION", orgPubId, orgCdwId),
        idReg("LOCATION", locPubId, locCdwId));
    repository.save(PractitionerSamples.Datamart.create().entity(cdwId, orgCdwId, locCdwId));
    Practitioner actual = _controller().read(publicId);
    assertThat(actual).isEqualTo(PractitionerSamples.Stu3.create().practitioner(publicId));
  }

  @Test
  void readRaw() {
    String publicId = "I2-abc";
    String cdwId = "123:S";
    String orgPubId = "I2-def";
    String orgCdwId = "456:I";
    String locPubId = "I2-ghi";
    String locCdwId = "789:L";
    _registerMockIdentities(
        idReg("PRACTITIONER", publicId, cdwId),
        idReg("ORGANIZATION", orgPubId, orgCdwId),
        idReg("LOCATION", locPubId, locCdwId));
    HttpServletResponse servletResponse = mock(HttpServletResponse.class);
    DatamartPractitioner dm =
        PractitionerSamples.Datamart.create().practitioner(cdwId, orgCdwId, locCdwId);
    repository.save(PractitionerSamples.Datamart.create().entity(cdwId, orgCdwId, locCdwId));
    String json = _controller().readRaw(publicId, servletResponse);
    assertThat(toObject(json)).isEqualTo(dm);
    verify(servletResponse).addHeader("X-VA-INCLUDES-ICN", "NONE");
  }

  @Test
  void readRawThrowsNotFoundWhenDataIsMissing() {
    _registerMockIdentities(
        idReg("PRACTITIONER", "x", "x"),
        idReg("ORGANIZATION", "x", "x"),
        idReg("LOCATION", "x", "x"));
    assertThrows(ResourceExceptions.NotFound.class, () -> _controller().readRaw("x", response));
  }

  @Test
  void readRawThrowsNotFoundWhenIdIsUnknown() {
    assertThrows(ResourceExceptions.NotFound.class, () -> _controller().readRaw("x", response));
  }

  @Test
  void readThrowsNotFoundWhenDataIsMissing() {
    _registerMockIdentities(
        idReg("PRACTITIONER", "x", "x"),
        idReg("ORGANIZATION", "x", "x"),
        idReg("LOCATION", "x", "x"));
    assertThrows(ResourceExceptions.NotFound.class, () -> _controller().read("x"));
  }

  @Test
  void readThrowsNotFoundWhenIdIsUnknown() {
    assertThrows(ResourceExceptions.NotFound.class, () -> _controller().read("x"));
  }

  @Test
  void searchByFamilyNameAndGivenName() {
    String publicId = "I2-abc";
    String cdwId = "123:S";
    String orgPubId = "I2-def";
    String orgCdwId = "456:I";
    String locPubId = "I2-ghi";
    String locCdwId = "789:L";
    String familyName = "NELSON";
    String givenName = "BOB";
    repository.save(PractitionerSamples.Datamart.create().entity(cdwId, orgCdwId, locCdwId));
    _registerMockIdentities(
        idReg("PRACTITIONER", publicId, cdwId),
        idReg("ORGANIZATION", orgPubId, orgCdwId),
        idReg("LOCATION", locPubId, locCdwId));
    Practitioner.Bundle actual = _controller().searchByFamilyAndGiven(familyName, givenName, 1, 1);
    assertThat(json(actual))
        .isEqualTo(
            json(
                PractitionerSamples.Stu3.asBundle(
                    "http://fonzy.com/cool",
                    List.of(PractitionerSamples.Stu3.create().practitioner(publicId)),
                    PractitionerSamples.Stu3.link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Practitioner?family=NELSON&given=BOB",
                        1,
                        1),
                    PractitionerSamples.Stu3.link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Practitioner?family=NELSON&given=BOB",
                        1,
                        1),
                    PractitionerSamples.Stu3.link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Practitioner?family=NELSON&given=BOB",
                        1,
                        1))));
  }

  @Test
  void searchById() {
    String publicId = "I2-abc";
    String cdwId = "123:S";
    String orgPubId = "I2-def";
    String orgCdwId = "456:I";
    String locPubId = "I2-ghi";
    String locCdwId = "789:L";
    _registerMockIdentities(
        idReg("PRACTITIONER", publicId, cdwId),
        idReg("ORGANIZATION", orgPubId, orgCdwId),
        idReg("LOCATION", locPubId, locCdwId));
    repository.save(PractitionerSamples.Datamart.create().entity(cdwId, orgCdwId, locCdwId));
    Practitioner.Bundle actual = _controller().searchById(publicId, 1, 1);
    assertThat(json(actual))
        .isEqualTo(
            json(
                PractitionerSamples.Stu3.asBundle(
                    "http://fonzy.com/cool",
                    List.of(PractitionerSamples.Stu3.create().practitioner(publicId)),
                    PractitionerSamples.Stu3.link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Practitioner?identifier=I2-abc",
                        1,
                        1),
                    PractitionerSamples.Stu3.link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Practitioner?identifier=I2-abc",
                        1,
                        1),
                    PractitionerSamples.Stu3.link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Practitioner?identifier=I2-abc",
                        1,
                        1))));
  }

  @Test
  void searchByNpi() {
    String systemAndCode = "http://hl7.org/fhir/sid/us-npi|1234567890";
    String encoded = URLEncoder.encode(systemAndCode, StandardCharsets.UTF_8);
    String publicId = "I2-abc";
    String cdwId = "123:S";
    String orgPubId = "I2-def";
    String orgCdwId = "456:I";
    String locPubId = "I2-ghi";
    String locCdwId = "789:L";
    repository.save(PractitionerSamples.Datamart.create().entity(cdwId, orgCdwId, locCdwId));
    _registerMockIdentities(
        idReg("PRACTITIONER", publicId, cdwId),
        idReg("ORGANIZATION", orgPubId, orgCdwId),
        idReg("LOCATION", locPubId, locCdwId));
    Practitioner.Bundle actual = _controller().searchByNpi(systemAndCode, 1, 1);
    assertThat(json(actual))
        .isEqualTo(
            json(
                PractitionerSamples.Stu3.asBundle(
                    "http://fonzy.com/cool",
                    List.of(PractitionerSamples.Stu3.create().practitioner(publicId)),
                    PractitionerSamples.Stu3.link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Practitioner?identifier=" + encoded,
                        1,
                        1),
                    PractitionerSamples.Stu3.link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Practitioner?identifier=" + encoded,
                        1,
                        1),
                    PractitionerSamples.Stu3.link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Practitioner?identifier=" + encoded,
                        1,
                        1))));
  }
}
