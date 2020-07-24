package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.R4Bundler;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.servlet.http.HttpServletResponse;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ExtendWith(SpringExtension.class)
public class R4DiagnosticReportControllerTest {
    HttpServletResponse response = mock(HttpServletResponse.class);

    private IdentityService ids = mock(IdentityService.class);

    @Autowired
    private DiagnosticReportRepository repository;

    R4DiagnosticReportController controller() {
        return new R4DiagnosticReportController(new R4Bundler(new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool", "cool", "cool")),
                WitnessProtection.builder()
                        .identityService(ids)
                        .build(),
                repository);
    }

    @SneakyThrows
    DiagnosticReportEntity entity(DatamartDiagnosticReport dm) {
        return DiagnosticReportEntity.builder()
                .cdwId(dm.cdwId())
                .icn(dm.patient().reference().orElse(null))
                .category("CH")
                .dateUtc(Instant.parse(dm.issuedDateTime()))
                .payload(json(dm))
                .build();
    }

    @SneakyThrows
    String json(Object o) {
        return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
    }

    void mockDiagnosticReportEntity(String publicId, String cdwId) {
        ResourceIdentity resourceIdentity =
                ResourceIdentity.builder()
                        .system("CDW")
                        .resource("DIAGNOSTIC_REPORT")
                        .identifier(cdwId)
                        .build();
        when(ids.lookup(publicId)).thenReturn(List.of(resourceIdentity));
        when(ids.register(Mockito.any()))
                .thenReturn(
                        List.of(
                                Registration.builder()
                                        .uuid(publicId)
                                        .resourceIdentities(List.of(resourceIdentity))
                                        .build()));
    }

    @Test
    void read() {
        DatamartDiagnosticReport dm = DiagnosticReportSamples.DatamartV2.create().diagnosticReport();
        repository.save(entity(dm));
        mockDiagnosticReportEntity("x", dm.cdwId());
        assertThat(controller().read("x")).isEqualTo(DiagnosticReportSamples.R4.create().diagnosticReport("x"));
    }


}
