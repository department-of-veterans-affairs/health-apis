package gov.va.api.health.dataquery.service.controller.organization;

import gov.va.api.health.dstu2.api.datatypes.Address;
import gov.va.api.health.dstu2.api.datatypes.ContactPoint;
import org.junit.Test;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class Dstu2OrganizationTransformerTest {
    @Test
    public void address() {
        assertThat(Dstu2OrganizationTransformer.address(null)).isNull();
        assertThat(
                Dstu2OrganizationTransformer.address(
                        DatamartOrganization.Address.builder().line1(" ").line2(" ").city(" ").state(" ").postalCode(" ").build()))
                .isNull();
        assertThat(
                Dstu2OrganizationTransformer.address(
                        DatamartOrganization.Address.builder()
                                .line1("1111 Test Ln")
                                .city("Delta")
                                .state("ZZ")
                                .postalCode("22222")
                                .build()))
                .isEqualTo(asList(
                        Address.builder().line(asList("1111 Test Ln")).city("Delta").state("ZZ").postalCode("22222").build()));
    }

    @Test
    public void telecom() {
        assertThat(Dstu2OrganizationTransformer.telecom(null)).isNull();
        assertThat(
                Dstu2OrganizationTransformer.telecom(
                        DatamartOrganization.Telecom.builder()
                        .system(DatamartOrganization.Telecom.System.phone)
                        .value("abc")
                        .build()))
        .isEqualTo(
                ContactPoint.builder()
                        .system(ContactPoint.ContactPointSystem.phone)
                        .value("abc")
                        .build());
    }

    @Test
    public void nullChecks() {
        assertThat(Dstu2OrganizationTransformer.type(null)).isNull();
        assertThat(Dstu2OrganizationTransformer.typeCoding(null)).isNull();
    }
}
