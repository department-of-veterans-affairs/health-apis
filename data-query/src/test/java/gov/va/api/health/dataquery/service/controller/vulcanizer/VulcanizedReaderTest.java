package gov.va.api.health.dataquery.service.controller.vulcanizer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions.NotFound;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.vulcanizer.Foos.FooDatamart;
import gov.va.api.health.dataquery.service.controller.vulcanizer.Foos.FooEntity;
import gov.va.api.health.dataquery.service.controller.vulcanizer.Foos.FooResource;
import gov.va.api.health.dataquery.service.controller.vulcanizer.Foos.Ids;
import gov.va.api.health.ids.api.IdentityService;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.repository.CrudRepository;

@ExtendWith(MockitoExtension.class)
class VulcanizedReaderTest {

  @Mock CrudRepository<FooEntity, String> repo;
  @Mock IdentityService ids;
  @Mock HttpServletResponse response;

  @Test
  void readRawReturnsPayload() {
    when(ids.lookup("pf1")).thenReturn(List.of(Ids.id("WHATEVER", "f1")));
    when(repo.findById("f1")).thenReturn(Optional.of(new FooEntity("f1", "p1")));
    var payload = reader().readRaw("pf1", response);
    assertThat(payload).isEqualTo("payload:f1:p1");
    verify(response).addHeader(IncludesIcnMajig.INCLUDES_ICN_HEADER, "p1");
  }

  @Test
  void readRawThrowsNotFound() {
    when(ids.lookup("pf1")).thenReturn(List.of(Ids.id("WHATEVER", "f1")));
    when(repo.findById("f1")).thenReturn(Optional.empty());
    assertThatExceptionOfType(NotFound.class).isThrownBy(() -> reader().readRaw("pf1", response));
  }

  @Test
  void readReturnsResource() {
    when(ids.register(any()))
        .thenReturn(List.of(Ids.registration("WHATEVER", "p1", "protected-p1")));
    when(ids.lookup("pf1")).thenReturn(List.of(Ids.id("WHATEVER", "f1")));
    when(repo.findById("f1")).thenReturn(Optional.of(new FooEntity("f1", "p1")));
    FooResource resource = reader().read("pf1");
    assertThat(resource).isEqualTo(FooResource.builder().id("f1").ref("protected-p1").build());
  }

  @Test
  void readThrowsNotFound() {
    when(ids.lookup("pf1")).thenReturn(List.of(Ids.id("WHATEVER", "f1")));
    when(repo.findById("f1")).thenReturn(Optional.empty());
    assertThatExceptionOfType(NotFound.class).isThrownBy(() -> reader().read("pf1"));
  }

  VulcanizedReader<FooEntity, FooDatamart, FooResource> reader() {
    return VulcanizedReader.forTransformation(
            VulcanizedTransformation.toDatamart(FooEntity::toDatamart)
                .toResource(FooDatamart::toResource)
                .witnessProtection(WitnessProtection.builder().identityService(ids).build())
                .replaceReferences(d -> Stream.of(d.patient()))
                .build())
        .toPayload(FooEntity::payload)
        .toPatientId(e -> Optional.of(e.ref()))
        .repository(repo)
        .build();
  }
}
