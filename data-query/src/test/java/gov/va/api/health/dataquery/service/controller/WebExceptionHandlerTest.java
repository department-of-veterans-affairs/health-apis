package gov.va.api.health.dataquery.service.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonMappingException;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.patient.Dstu2PatientController;
import gov.va.api.health.dataquery.service.controller.patient.PatientRepositoryV2;
import gov.va.api.health.dataquery.service.controller.patient.PatientSearchRepository;
import gov.va.api.health.ids.client.IdEncoder.BadId;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod;

@SuppressWarnings("DefaultAnnotationParam")
@RunWith(Parameterized.class)
public class WebExceptionHandlerTest {
  private final String basePath = "/dstu2";

  @Parameter(0)
  public HttpStatus status;

  @Parameter(1)
  public Exception exception;

  @Mock HttpServletRequest request;
  @Mock Dstu2Bundler bundler;
  @Mock PatientSearchRepository repository;
  @Mock PatientRepositoryV2 repositoryV2;
  @Mock WitnessProtection witnessProtection;
  private Dstu2PatientController controller;
  private WebExceptionHandler exceptionHandler;

  @SuppressWarnings("deprecation")
  @Parameterized.Parameters(name = "{index}:{0} - {1}")
  public static List<Object[]> parameters() {
    return Arrays.asList(
        test(HttpStatus.NOT_FOUND, new BadId("x", null)),
        test(HttpStatus.BAD_REQUEST, new ConstraintViolationException(new HashSet<>())),
        test(HttpStatus.INTERNAL_SERVER_ERROR, new RuntimeException()),
        test(
            HttpStatus.INTERNAL_SERVER_ERROR,
            new UndeclaredThrowableException(
                new JsonMappingException("Failed to convert string '.' to double.")))
        //
        );
  }

  private static Object[] test(HttpStatus status, Exception exception) {
    return new Object[] {status, exception};
  }

  @Before
  public void _init() {
    MockitoAnnotations.initMocks(this);
    controller =
        new Dstu2PatientController(false, bundler, repository, repositoryV2, witnessProtection);
    exceptionHandler = new WebExceptionHandler("1234567890123456");
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

  @Test
  @SneakyThrows
  public void expectStatus() {
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
}
