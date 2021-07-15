package gov.va.api.health.dataquery.service.controller.practitionerrole;

import static gov.va.api.health.dataquery.service.controller.MockRequests.paging;
import static gov.va.api.health.dataquery.service.controller.MockRequests.requestFromUri;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.resources.PractitionerRole;
import gov.va.api.lighthouse.datamart.CompositeCdwId;
import gov.va.api.lighthouse.vulcan.InvalidRequest;
import gov.va.api.lighthouse.vulcan.VulcanResult;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public class R4PractitionerRoleControllerTest {
  IdentityService ids = mock(IdentityService.class);

  PractitionerRoleRepository repository = mock(PractitionerRoleRepository.class);

  static Registration idReg(String type, String pubId, String cdwId) {
    return Registration.builder()
        .uuid(pubId)
        .resourceIdentities(
            List.of(
                ResourceIdentity.builder().system("CDW").resource(type).identifier(cdwId).build()))
        .build();
  }

  R4PractitionerRoleController _controller() {
    return new R4PractitionerRoleController(
        WitnessProtection.builder().identityService(ids).build(),
        LinkProperties.builder()
            .publicUrl("http://fonzy.com")
            .publicR4BasePath("r4")
            .publicStu3BasePath("stu3")
            .publicDstu2BasePath("dstu2")
            .maxPageSize(20)
            .defaultPageSize(15)
            .build(),
        repository);
  }

  void _registerMockIdentities(Registration... regs) {
    for (Registration reg : regs) {
      when(ids.lookup(reg.uuid())).thenReturn(reg.resourceIdentities());
    }
    when(ids.register(Mockito.any())).thenReturn(ImmutableList.copyOf(regs));
  }

  @ParameterizedTest
  @SuppressWarnings("unchecked")
  @ValueSource(
      strings = {
        "?practitioner.identifier=http://hl7.org/fhir/sid/us-npi|",
        "?practitioner.identifier=foo|123",
        "?practitioner.identifier=|123"
      })
  void emptyRequest(String query) {
    _registerMockIdentities(
        idReg("PRACTITIONER_ROLE", "I2-111", "111:P"),
        idReg("PRACTITIONER", "I2-222", "222:S"),
        idReg("ORGANIZATION", "I2-333", "333:I"),
        idReg("LOCATION", "I2-444", "444:L"));
    PractitionerRoleSamples.Datamart dm = PractitionerRoleSamples.Datamart.create();
    when(repository.findAll(any(Specification.class), any(Pageable.class)))
        .thenAnswer(
            i ->
                new PageImpl<PractitionerRoleEntity>(
                    List.of(dm.entity("111:P", "222:S", "333:I", "444:L")),
                    i.getArgument(1, Pageable.class),
                    1));
    var r = requestFromUri("http://fonzy.com/r4/PractitionerRole" + query);
    var actual = _controller().search(r);
    assertThat(actual.entry()).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "",
        "?unknownparam=123",
        "?_id=foo&practitioner.identifier=123",
        "?_id=foo&practitioner.identifier=123&practitioner.name=bar",
        "?practitioner.identifier=123&practitioner.name=bar"
      })
  void invalidRequests(String query) {
    var r = requestFromUri("http://fonzy.com/r4/PractitionerRole" + query);
    assertThatExceptionOfType(InvalidRequest.class).isThrownBy(() -> _controller().search(r));
  }

  @Test
  void read() {
    String publicId = "I2-111";
    String cdwId = "111:P";
    String pracPubId = "I2-222";
    String pracCdwId = "222:S";
    String orgPubId = "I2-333";
    String orgCdwId = "333:I";
    String locPubId = "I2-444";
    String locCdwId = "444:L";
    _registerMockIdentities(
        idReg("PRACTITIONER_ROLE", publicId, cdwId),
        idReg("PRACTITIONER", pracPubId, pracCdwId),
        idReg("ORGANIZATION", orgPubId, orgCdwId),
        idReg("LOCATION", locPubId, locCdwId));
    PractitionerRoleEntity entity =
        PractitionerRoleSamples.Datamart.create().entity(cdwId, pracCdwId, orgCdwId, locCdwId);
    when(repository.findById(CompositeCdwId.fromCdwId(cdwId))).thenReturn(Optional.of(entity));
    assertThat(_controller().read(publicId))
        .isEqualTo(
            PractitionerRoleSamples.R4
                .create()
                .practitionerRole(publicId, pracPubId, orgPubId, locPubId));
  }

  @Test
  void readRaw() {
    String publicId = "I2-111";
    String cdwId = "111:P";
    _registerMockIdentities(idReg("PRACTITIONER_ROLE", publicId, cdwId));
    HttpServletResponse response = mock(HttpServletResponse.class);
    PractitionerRoleEntity entity =
        PractitionerRoleEntity.builder().npi("12345").payload("{}").build();
    when(repository.findById(CompositeCdwId.fromCdwId(cdwId))).thenReturn(Optional.of(entity));
    assertThat(_controller().readRaw(publicId, response)).isEqualTo("{}");
  }

  @Test
  void toBundle() {
    String pubId1 = "I2-111P";
    String cdwId1 = "111:P";
    String pubId2 = "I2-222P";
    String cdwId2 = "222:P";
    String pubId3 = "I2-333P";
    String cdwId3 = "333:P";
    String pracPubId1 = "I2-111S";
    String pracCdwId1 = "111:S";
    String pracPubId2 = "I2-222S";
    String pracCdwId2 = "222:S";
    String pracPubId3 = "I2-333S";
    String pracCdwId3 = "333:S";
    String orgPubId1 = "I2-111I";
    String orgCdwId1 = "111:I";
    String orgPubId2 = "I2-222I";
    String orgCdwId2 = "222:I";
    String orgPubId3 = "I2-333I";
    String orgCdwId3 = "333:I";
    String locPubId1 = "I2-111L";
    String locCdwId1 = "111:L";
    String locPubId2 = "I2-222L";
    String locCdwId2 = "222:L";
    String locPubId3 = "I2-333L";
    String locCdwId3 = "333:L";
    _registerMockIdentities(
        idReg("PRACTITIONER_ROLE", pubId1, cdwId1),
        idReg("PRACTITIONER_ROLE", pubId2, cdwId2),
        idReg("PRACTITIONER_ROLE", pubId3, cdwId3),
        idReg("PRACTITIONER", pracPubId1, pracCdwId1),
        idReg("PRACTITIONER", pracPubId2, pracCdwId2),
        idReg("PRACTITIONER", pracPubId3, pracCdwId3),
        idReg("ORGANIZATION", orgPubId1, orgCdwId1),
        idReg("ORGANIZATION", orgPubId2, orgCdwId2),
        idReg("ORGANIZATION", orgPubId3, orgCdwId3),
        idReg("LOCATION", locPubId1, locCdwId1),
        idReg("LOCATION", locPubId2, locCdwId2),
        idReg("LOCATION", locPubId3, locCdwId3));
    var bundler = _controller().toBundle();
    PractitionerRoleSamples.Datamart datamart = PractitionerRoleSamples.Datamart.create();
    var vr =
        VulcanResult.<PractitionerRoleEntity>builder()
            .paging(
                paging(
                    "http://fonzy.com/r4/PractitionerRole?practitioner.identifier=pr1&page=%d&_count=%d",
                    1, 4, 5, 6, 9, 15))
            .entities(
                Stream.of(
                    datamart.entity(cdwId1, pracCdwId1, orgCdwId1, locCdwId1),
                    datamart.entity(cdwId2, pracCdwId2, orgCdwId2, locCdwId2),
                    datamart.entity(cdwId3, pracCdwId3, orgCdwId3, locCdwId3)))
            .build();
    PractitionerRoleSamples.R4 r4 = PractitionerRoleSamples.R4.create();
    PractitionerRole.Bundle expected =
        PractitionerRoleSamples.R4.asBundle(
            "http://fonzy.com/r4",
            List.of(
                r4.practitionerRole(pubId1, pracPubId1, orgPubId1, locPubId1),
                r4.practitionerRole(pubId2, pracPubId2, orgPubId2, locPubId2),
                r4.practitionerRole(pubId3, pracPubId3, orgPubId3, locPubId3)),
            999,
            PractitionerRoleSamples.R4.link(
                BundleLink.LinkRelation.first,
                "http://fonzy.com/r4/PractitionerRole?practitioner.identifier=pr1",
                1,
                15),
            PractitionerRoleSamples.R4.link(
                BundleLink.LinkRelation.prev,
                "http://fonzy.com/r4/PractitionerRole?practitioner.identifier=pr1",
                4,
                15),
            PractitionerRoleSamples.R4.link(
                BundleLink.LinkRelation.self,
                "http://fonzy.com/r4/PractitionerRole?practitioner.identifier=pr1",
                5,
                15),
            PractitionerRoleSamples.R4.link(
                BundleLink.LinkRelation.next,
                "http://fonzy.com/r4/PractitionerRole?practitioner.identifier=pr1",
                6,
                15),
            PractitionerRoleSamples.R4.link(
                BundleLink.LinkRelation.last,
                "http://fonzy.com/r4/PractitionerRole?practitioner.identifier=pr1",
                9,
                15));
    var applied = bundler.apply(vr);
    assertThat(applied).isEqualTo(expected);
  }

  @ParameterizedTest
  @SuppressWarnings("unchecked")
  @ValueSource(
      strings = {
        "?_id=111:P",
        "?practitioner.identifier=111:S",
        "?practitioner.identifier=http://hl7.org/fhir/sid/us-npi|123",
        "?practitioner.name=harry",
        "?specialty=ABC123",
        "?specialty=http://nucc.org/provider-taxonomy|ABC123"
      })
  void validRequests(String query) {
    _registerMockIdentities(
        idReg("PRACTITIONER_ROLE", "I2-111", "111:P"),
        idReg("PRACTITIONER", "I2-222", "222:S"),
        idReg("ORGANIZATION", "I2-333", "333:I"),
        idReg("LOCATION", "I2-444", "444:L"));
    PractitionerRoleSamples.Datamart dm = PractitionerRoleSamples.Datamart.create();
    when(repository.findAll(any(Specification.class), any(Pageable.class)))
        .thenAnswer(
            i ->
                new PageImpl<PractitionerRoleEntity>(
                    List.of(dm.entity("111:P", "222:S", "333:I", "444:L")),
                    i.getArgument(1, Pageable.class),
                    1));
    var r = requestFromUri("http://fonzy.com/r4/PractitionerRole" + query);
    var actual = _controller().search(r);
    assertThat(actual.entry()).hasSize(1);
  }
}
