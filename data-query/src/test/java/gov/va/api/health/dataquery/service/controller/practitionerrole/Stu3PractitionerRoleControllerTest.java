package gov.va.api.health.dataquery.service.controller.practitionerrole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.Stu3Bundler;
import gov.va.api.health.dataquery.service.controller.Stu3Validator;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.practitioner.DatamartPractitioner;
import gov.va.api.health.dataquery.service.controller.practitioner.PractitionerEntity;
import gov.va.api.health.dataquery.service.controller.practitioner.PractitionerRepository;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.stu3.api.resources.PractitionerRole;
import gov.va.api.health.stu3.api.bundle.BundleLink;
import java.util.List;

import gov.va.api.health.dataquery.service.controller.ResourceExceptions;

import javax.servlet.http.HttpServletResponse;

import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

@DataJpaTest
@RunWith(SpringRunner.class)
public class Stu3PractitionerRoleControllerTest {
  @Autowired private PractitionerRepository repository;

  private IdentityService ids = mock(IdentityService.class);

  @SneakyThrows
  private static PractitionerEntity asEntity(DatamartPractitioner dm) {
    return PractitionerEntity.builder()
        .cdwId(dm.cdwId())
        .npi(dm.npi().orElse(null))
        .familyName(dm.name().family())
        .givenName(dm.name().given())
        .payload(JacksonConfig.createMapper().writeValueAsString(dm))
        .build();
  }

  @SneakyThrows
  private static String asJson(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  private void addMockIdentities(
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
                Registration.builder().uuid(pracPubId).resourceIdentity(pracResource).build(),
                Registration.builder().uuid(locPubId).resourceIdentity(locResource).build(),
                Registration.builder().uuid(orgPubId).resourceIdentity(orgResource).build()));
  }

  @SneakyThrows
  private DatamartPractitioner asObject(String json) {
    return JacksonConfig.createMapper().readValue(json, DatamartPractitioner.class);
  }

  private Stu3PractitionerRoleController controller() {
    return new Stu3PractitionerRoleController(
        new Stu3Bundler(new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool", "cool")),
        repository,
        WitnessProtection.builder().identityService(ids).build());
  }

  @Test
  public void read() {
    String publicId = "p1";
    String cdwId = "c1";
    String locPubId = "p2";
    String locCdwId = "c2";
    String orgPubId = "p3";
    String orgCdwId = "c3";
    addMockIdentities(publicId, cdwId, locPubId, locCdwId, orgPubId, orgCdwId);
    DatamartPractitioner dm =
        PractitionerRoleSamples.Datamart.create().practitioner(cdwId, locCdwId, orgCdwId);
    repository.save(asEntity(dm));
    PractitionerRole actual = controller().read(publicId);
    assertThat(actual)
        .isEqualTo(
            PractitionerRoleSamples.Stu3.create().practitionerRole(publicId, locPubId, orgPubId));
  }

  @Test
  public void readRaw() {
    String publicId = "p1";
    String cdwId = "c1";
    String locPubId = "p2";
    String locCdwId = "c2";
    String orgPubId = "p3";
    String orgCdwId = "c3";
    addMockIdentities(publicId, cdwId, locPubId, locCdwId, orgPubId, orgCdwId);
    HttpServletResponse servletResponse = mock(HttpServletResponse.class);
    DatamartPractitioner dm =
        PractitionerRoleSamples.Datamart.create().practitioner(cdwId, locCdwId, orgCdwId);
    repository.save(asEntity(dm));
    String json = controller().readRaw(publicId, servletResponse);
    assertThat(asObject(json)).isEqualTo(dm);
    verify(servletResponse).addHeader("X-VA-INCLUDES-ICN", "NONE");
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readRawThrowsNotFoundWhenDataIsMissing() {
    addMockIdentities("x", "x", "y", "y", "z", "z");
    controller().readRaw("x", mock(HttpServletResponse.class));
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readRawThrowsNotFoundWhenIdIsUnknown() {
    controller().readRaw("x", mock(HttpServletResponse.class));
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readThrowsNotFoundWhenDataIsMissing() {
    addMockIdentities("x", "x", "y", "y", "z", "z");
    controller().read("x");
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readThrowsNotFoundWhenIdIsUnknown() {
    controller().read("x");
  }

  @Test
  public void searchById() {
    String publicId = "p1";
    String cdwId = "c1";
    String locPubId = "p2";
    String locCdwId = "c2";
    String orgPubId = "p3";
    String orgCdwId = "c3";
    addMockIdentities(publicId, cdwId, locPubId, locCdwId, orgPubId, orgCdwId);
    DatamartPractitioner dm =
        PractitionerRoleSamples.Datamart.create().practitioner(cdwId, locCdwId, orgCdwId);
    repository.save(asEntity(dm));
    PractitionerRole.Bundle actual = controller().searchById(publicId, 1, 1);
    assertThat(asJson(actual))
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
  public void searchByIdentifier() {
    String publicId = "p1";
    String cdwId = "c1";
    String locPubId = "p2";
    String locCdwId = "c2";
    String orgPubId = "p3";
    String orgCdwId = "c3";
    addMockIdentities(publicId, cdwId, locPubId, locCdwId, orgPubId, orgCdwId);
    DatamartPractitioner dm =
        PractitionerRoleSamples.Datamart.create().practitioner(cdwId, locCdwId, orgCdwId);
    repository.save(asEntity(dm));
    PractitionerRole.Bundle actual = controller().searchByIdentifier(publicId, 1, 1);
    assertThat(asJson(actual))
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
  public void searchByName() {
    String family = "Nelson";
    String given = "Bob";
    String publicId = "p1";
    String cdwId = "c1";
    String locPubId = "p2";
    String locCdwId = "c2";
    String orgPubId = "p3";
    String orgCdwId = "c3";
    addMockIdentities(publicId, cdwId, locPubId, locCdwId, orgPubId, orgCdwId);
    DatamartPractitioner dm =
        PractitionerRoleSamples.Datamart.create().practitioner(cdwId, locCdwId, orgCdwId);
    repository.save(asEntity(dm));
    PractitionerRole.Bundle actual = controller().searchByName(family, given, 1, 1);
    assertThat(asJson(actual))
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

  //  searchByNpi(String, int, int)

  //  @Test
  //  public void searchByAddress() {
  //    String street = "1901 VETERANS MEMORIAL DRIVE";
  //    String publicId = "abc";
  //    String cdwId = "123";
  //    String orgPubId = "def";
  //    String orgCdwId = "456";
  //    addMockIdentities(publicId, cdwId, orgPubId, orgCdwId);
  //    DatamartPractitioner dm = PractitionerRoleSamples.Datamart.create().location(cdwId,
  // orgCdwId);
  //    repository.save(asEntity(dm));
  //   PractitionerRole.Bundle actual = controller().searchByAddress(street, null, null, null, 1,
  // 1);
  //    assertThat(asJson(actual))
  //        .isEqualTo(
  //            asJson(
  //                PractitionerRoleSamples.Stu3.asBundle(
  //                    "http://fonzy.com/cool",
  //                    List.of(PractitionerRoleSamples.Stu3.create().location(publicId, orgPubId)),
  //                    PractitionerRoleSamples.Stu3.link(
  //                        BundleLink.LinkRelation.first,
  //                        "http://fonzy.com/cool/Location?address=" + street,
  //                        1,
  //                        1),
  //                    PractitionerRoleSamples.Stu3.link(
  //                        BundleLink.LinkRelation.self,
  //                        "http://fonzy.com/cool/Location?address=" + street,
  //                        1,
  //                        1),
  //                    PractitionerRoleSamples.Stu3.link(
  //                        BundleLink.LinkRelation.last,
  //                        "http://fonzy.com/cool/Location?address=" + street,
  //                        1,
  //                        1))));
  //  }

  @Test
  @SneakyThrows
  public void validate() {
    assertThat(
            controller()
                .validate(
                    PractitionerRoleSamples.Stu3.asBundle(
                        "http://fonzy.com/cool",
                        List.of(
                            PractitionerRoleSamples.Stu3.create().practitionerRole("x", "y", "z")),
                        PractitionerRoleSamples.Stu3.link(
                            BundleLink.LinkRelation.first,
                            "http://fonzy.com/cool/PractitionerRole?identifier=x",
                            1,
                            1),
                        PractitionerRoleSamples.Stu3.link(
                            BundleLink.LinkRelation.self,
                            "http://fonzy.com/cool/PractitionerRole?identifier=x",
                            1,
                            1),
                        PractitionerRoleSamples.Stu3.link(
                            BundleLink.LinkRelation.last,
                            "http://fonzy.com/cool/PractitionerRole?identifier=x",
                            1,
                            1))))
        .isEqualTo(Stu3Validator.ok());
  }
}
