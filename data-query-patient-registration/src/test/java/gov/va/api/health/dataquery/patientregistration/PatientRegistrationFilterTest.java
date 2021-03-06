package gov.va.api.health.dataquery.patientregistration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import gov.va.api.health.dataquery.patientregistration.PatientRegistrationFilter.IcnDistiller;
import gov.va.api.health.dataquery.patientregistration.PatientRegistrationFilter.PatientReadIcnDistiller;
import gov.va.api.health.dataquery.patientregistration.PatientRegistrationFilter.PatientSearchByIcnDistiller;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.AsyncResult;

@ExtendWith(MockitoExtension.class)
class PatientRegistrationFilterTest {
  @Mock FilterChain chain;

  @Mock IcnDistiller distiller;

  @Mock PatientRegistrar registrar;

  @Mock HttpServletRequest request;

  @Mock HttpServletResponse response;

  PatientRegistrationFilter filter() {
    return PatientRegistrationFilter.builder()
        .registrar(registrar)
        .distillers(List.of(distiller))
        .build();
  }

  @Test
  @SneakyThrows
  void filterAppliesWhenIcnIsDistilled() {
    when(distiller.distillFromUri(request)).thenReturn("123");
    var registration = PatientRegistration.builder().build();
    when(registrar.register("123")).thenReturn(new AsyncResult<>(registration).completable());
    invokeDoFilterInternal();
    verify(registrar).register("123");
  }

  @Test
  void filterDoesNotApplyWhenIcnIsNotDistilled() {
    when(distiller.distillFromUri(request)).thenReturn(null);
    invokeDoFilterInternal();
    verifyNoInteractions(registrar);
  }

  @Test
  void filterDoesNotApplyWhenRegistrarReturnsNull() {
    when(distiller.distillFromUri(request)).thenReturn("123");
    var registration = PatientRegistration.builder().build();
    when(registrar.register("123")).thenReturn(null);
    invokeDoFilterInternal();
    verify(registrar).register("123");
  }

  @Test
  void filterIgnoresRegistrarErrors() {
    when(distiller.distillFromUri(request)).thenReturn("123");
    when(registrar.register("123")).thenThrow(new RuntimeException("fugazi"));
    invokeDoFilterInternal();
    verify(registrar).register("123");
  }

  @Test
  @SneakyThrows
  void filterIgnoresRegistrarResultsErrors() {
    when(distiller.distillFromUri(request)).thenReturn("123");
    var registration = PatientRegistration.builder().build();
    CompletableFuture<PatientRegistration> future = mock(CompletableFuture.class);
    when(future.isDone()).thenReturn(true);
    when(future.get(anyLong(), any(TimeUnit.class)))
        .thenThrow(new ExecutionException("fugazi", null));
    when(registrar.register("123")).thenReturn(future);
    invokeDoFilterInternal();
    verify(registrar).register("123");
  }

  @Test
  void filterIgnoresRegistrarResultsStillInProgress() {
    when(distiller.distillFromUri(request)).thenReturn("123");
    var registration = PatientRegistration.builder().build();
    CompletableFuture<PatientRegistration> future = mock(CompletableFuture.class);
    when(future.isDone()).thenReturn(false);
    when(registrar.register("123")).thenReturn(future);
    invokeDoFilterInternal();
    verify(registrar).register("123");
  }

  @SneakyThrows
  private void invokeDoFilterInternal() {
    filter().doFilterInternal(request, response, chain);
    verify(chain).doFilter(request, response);
    verifyNoMoreInteractions(chain);
  }

  @ParameterizedTest
  @ValueSource(strings = {"/r4/Immunization/123", "/r4/Patient", "/r4/Patient/"})
  void patientReadIcnDistillerDoesNotFindIcnForNonPatientReadUrls(String uri) {
    String icn = new PatientReadIcnDistiller().distillFromUri(requestForUri(uri));
    assertThat(icn).isNull();
  }

  @ParameterizedTest
  @ValueSource(strings = {"/r4/Patient/123", "/dstu2/Patient/123V456", "/whatever/r4/Patient/123"})
  void patientReadIcnDistillerFindsIcnForPatientReadUrls(String uri) {
    String expected = uri.replaceAll("^.*/", "");
    String icn = new PatientReadIcnDistiller().distillFromUri(requestForUri(uri));
    assertThat(icn).isEqualTo(expected);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/r4/Immunization?_id=123",
        "/dstu2/Observation?_id=123V456",
        "/whatever/r4/Practitioner?_id=123",
        "/whatever/tehe/PatientGotcha?_id=123"
      })
  void patientSearchIcnDistillerDoesNotFindIcnForNonPatientSearchByIdUrls(String uri) {
    String icn =
        new PatientSearchByIcnDistiller()
            .distillFromUri(requestForUri(StringUtils.substringBefore(uri, "?")));
    assertThat(icn).isNull();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/r4/Immunization?identifier=123",
        "/dstu2/Observation?identifier=123V456",
        "/whatever/r4/Practitioner?identifier=123",
        "/whatever/tehe/PatientGotcha?identifier=123"
      })
  void patientSearchIcnDistillerDoesNotFindIcnForNonPatientSearchByIdentifierUrls(String uri) {
    String icn =
        new PatientSearchByIcnDistiller()
            .distillFromUri(requestForUri(StringUtils.substringBefore(uri, "?")));
    assertThat(icn).isNull();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/r4/Patient?_id=123",
        "/dstu2/Patient?_id=123V456",
        "/whatever/r4/Patient?_id=123"
      })
  void patientSearchIcnDistillerFindsIcnForPatientSearchByIdUrls(String uri) {
    String expected = uri.replaceAll("^.*=", "");
    when(request.getParameter("_id")).thenReturn(expected);
    String icn =
        new PatientSearchByIcnDistiller()
            .distillFromUri(requestForUri(StringUtils.substringBefore(uri, "?")));
    assertThat(icn).isEqualTo(expected);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/r4/Patient?identifier=123",
        "/dstu2/Patient?identifier=123V456",
        "/whatever/r4/Patient?identifier=123"
      })
  void patientSearchIcnDistillerFindsIcnForPatientSearchByIdentifierUrls(String uri) {
    String expected = uri.replaceAll("^.*=", "");
    when(request.getParameter("_id")).thenReturn(null);
    when(request.getParameter("identifier")).thenReturn(expected);
    String icn =
        new PatientSearchByIcnDistiller()
            .distillFromUri(requestForUri(StringUtils.substringBefore(uri, "?")));
    assertThat(icn).isEqualTo(expected);
  }

  HttpServletRequest requestForUri(String uri) {
    when(request.getRequestURI()).thenReturn(uri);
    return request;
  }
}
