package gov.va.api.health.dataquery.service.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.patient.PatientController;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient.BadRequest;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient.NotFound;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient.SearchFailed;
import gov.va.api.health.dataquery.service.mranderson.client.Query;
import gov.va.api.health.dataquery.service.mranderson.client.Query.Profile;
import java.lang.reflect.Method;
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

  @Parameter(0)
  public HttpStatus status;

  @Parameter(1)
  public Exception exception;

  @Mock HttpServletRequest request;
  @Mock MrAndersonClient mrAnderson;
  @Mock PatientController.Transformer tx;
  @Mock Bundler bundler;
  private PatientController controller;
  private WebExceptionHandler exceptionHandler;

  @Parameterized.Parameters(name = "{index}:{0} - {1}")
  public static List<Object[]> parameters() {
    Query<?> query =
        Query.builder()
            .profile(Profile.ARGONAUT)
            .resource("Patient")
            .version("1.01")
            .parameters(Parameters.forIdentity("123"))
            .build();
    return Arrays.asList(
        test(HttpStatus.NOT_FOUND, new NotFound(query)),
        test(HttpStatus.BAD_REQUEST, new BadRequest(query)),
        test(HttpStatus.BAD_REQUEST, new ConstraintViolationException(new HashSet<>())),
        test(HttpStatus.INTERNAL_SERVER_ERROR, new SearchFailed(query)),
        test(HttpStatus.INTERNAL_SERVER_ERROR, new RuntimeException())
        //
        );
  }

  private static Object[] test(HttpStatus status, Exception exception) {
    return new Object[] {status, exception};
  }

  @Before
  public void _init() {
    MockitoAnnotations.initMocks(this);
    controller = new PatientController(tx, mrAnderson, bundler);
    exceptionHandler = new WebExceptionHandler();
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

    when(mrAnderson.search(Mockito.any())).thenThrow(exception);
    when(request.getRequestURI()).thenReturn("/api/Patient/123");

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
    mvc.perform(get("/api/Patient/123"))
        .andExpect(status().is(status.value()))
        .andExpect(jsonPath("text.div", containsString("/api/Patient/123")))
        .andExpect(
            jsonPath("issue[0].diagnostics", containsString(exception.getClass().getSimpleName())));
  }
}
