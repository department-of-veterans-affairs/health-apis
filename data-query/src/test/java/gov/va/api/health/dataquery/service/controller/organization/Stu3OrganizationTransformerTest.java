//package gov.va.api.health.dataquery.service.controller.organization;
//
//import gov.va.api.health.autoconfig.configuration.JacksonConfig;
//import lombok.SneakyThrows;
//import org.junit.Test;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//public class Stu3OrganizationTransformerTest {
//    @Test
//    public void address() {}
//
//    @SneakyThrows
//    String json(Object o) {
//        return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
//    }
//
//    @Test
//    public void identifier() {}
//
//    @Test
//    public void telecom() {}
//
//    @Test
//    public void organization() {
//        assertThat(
//                json(
//                        Stu3OrganizationTransformer.builder()
//                                .datamart(Dstu2OrganizationSamples.Datamart.create().organization())
//                                .build()
//                                .toFhir()))
//                .isEqualTo(json(Dstu2OrganizationSamples.Stu3.create().organization()));
//    }
//
//
//
//
//}
