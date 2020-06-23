package gov.va.api.health.dataquery.service.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonMappingException;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.patient.Dstu2PatientController;
import gov.va.api.health.dataquery.service.controller.patient.PatientRepositoryV2;
import gov.va.api.health.ids.client.IdEncoder.BadId;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashSet;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod;

@SuppressWarnings("DefaultAnnotationParam")
public class WebExceptionHandlerTest {
  private final String basePath = "/dstu2";

  HttpServletRequest request = mock(HttpServletRequest.class);
  Dstu2Bundler bundler = mock(Dstu2Bundler.class);
  PatientRepositoryV2 repository = mock(PatientRepositoryV2.class);
  WitnessProtection witnessProtection = mock(WitnessProtection.class);
  private Dstu2PatientController controller =
      new Dstu2PatientController(bundler, repository, witnessProtection);
  private WebExceptionHandler exceptionHandler = new WebExceptionHandler("1234567890123456");

  @SuppressWarnings("deprecation")
  public static Stream<Arguments> parameters() {
    return Stream.of(
        Arguments.of(HttpStatus.NOT_FOUND, new BadId("x", null)),
        Arguments.of(HttpStatus.BAD_REQUEST, new ConstraintViolationException(new HashSet<>())),
        Arguments.of(HttpStatus.INTERNAL_SERVER_ERROR, new RuntimeException()),
        Arguments.of(
            HttpStatus.INTERNAL_SERVER_ERROR,
            new UndeclaredThrowableException(
                new JsonMappingException("Failed to convert string '.' to double."))));
  }

  private static Object[] test(HttpStatus status, Exception exception) {
    return new Object[] {status, exception};
  }

  private ExceptionHandlerExceptionResolver createExceptionResolver() {
    ExceptionHandlerExceptionResolver exceptionResolver =
        new ExceptionHandlerExceptionResolver() {
          @Override
          protected ServletInvocableHandlerMethod getExceptionHandlerMethod(
              HandlerMethod handlerMethod, Exception ex) {
            Method method =
                new ExceptionHandlerMethodResolver(WebExceptionHandler.class).resolveMethod(ex);
            assertThat(method).isNotNull();
            return new ServletInvocableHandlerMethod(exceptionHandler, method);
          }
        };
    exceptionResolver
        .getMessageConverters()
        .add(new MappingJackson2HttpMessageConverter(JacksonConfig.createMapper()));
    exceptionResolver.afterPropertiesSet();
    return exceptionResolver;
  }

  @ParameterizedTest
  @MethodSource("parameters")
  @SneakyThrows
  public void expectStatus(HttpStatus status, Exception exception) {
    when(repository.findById(Mockito.anyString())).thenThrow(exception);
    when(witnessProtection.toCdwId(Mockito.anyString())).thenReturn("whatever");
    when(request.getRequestURI()).thenReturn(basePath + "/Patient/123");
    MockMvc mvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setHandlerExceptionResolvers(createExceptionResolver())
            .setMessageConverters()
            .build();
    /*
     * Actual:
     *
     * <pre>
     * {
     *   "id":"99bfd970-d6c5-4998-a59c-9e9c2848d2b6",
     *   "text":{
     *     "status":"additional",
     *      "div":"<div>Failure: /api/Patient/123</div>"
     *   },
     *   "issue":
     *   [
     *     {
     *       "severity":"fatal",
     *       "code":"not-found",
     *       "diagnostics":"Error: NotFound Timestamp:2018-11-08T19:10:24.198Z"
     *     }
     *   ]
     * }
     * </pre>
     */
    mvc.perform(get(basePath + "/Patient/123"))
        .andExpect(status().is(status.value()))
        .andExpect(jsonPath("text.div", containsString(basePath + "/Patient/123")))
        .andExpect(jsonPath("extension[0].url", equalTo("timestamp")))
        .andExpect(jsonPath("extension[1].url", equalTo("type")))
        .andExpect(
            jsonPath("extension[1].valueString", equalTo(exception.getClass().getSimpleName())));
  }

  @Test
  @SneakyThrows
  public void unsupportedMethodThrowsNotAllowed() {
    MockMvc mvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setHandlerExceptionResolvers(createExceptionResolver())
            .setMessageConverters()
            .build();
    mvc.perform(post(basePath + "/Patient/123"))
        .andExpect(
            result ->
                assertTrue(
                    result.getResolvedException()
                        instanceof HttpRequestMethodNotSupportedException));
  }
}
