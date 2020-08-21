package gov.va.api.health.dataquery.service.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;
import com.amazonaws.services.simplesystemsmanagement.model.Parameter;
import com.amazonaws.services.simplesystemsmanagement.model.ParameterNotFoundException;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.config.DatabaseCredentialManager.ConnectionInfo;
import java.util.List;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class DatabaseCredentialManagerTest {
  String parameterName = "/local/test/param";

  DatabaseCredentialManager dcm = new DatabaseCredentialManager();

  @Test
  void buildConnectionUrl() {
    ConnectionInfo ci = connectionInfo();
    assertThat(dcm.buildConnectionUrl(ci))
        .isEqualTo(
            "jdbc:sqlserver://localhost:8001;DatabaseName=dq;sendStringParametersAsUnicode=false;integratedSecurity=true");
  }

  private ConnectionInfo connectionInfo() {
    return ConnectionInfo.builder()
        .host(Optional.of("localhost"))
        .port(Optional.of(8001))
        .db(Optional.of("dq"))
        .user(Optional.of("user"))
        .password(Optional.of("password"))
        .configurations(List.of("sendStringParametersAsUnicode=false", "integratedSecurity=true"))
        .build();
  }

  @Test
  void getParameterValueReturnsEmptyWhenNotExists() {
    AWSSimpleSystemsManagement ssm = mock(AWSSimpleSystemsManagement.class);
    when(ssm.getParameter(any())).thenThrow(ParameterNotFoundException.class);
    assertThat(dcm.getParameterValue(ssm, parameterName)).isEmpty();
  }

  @Test
  void getParameterValueReturnsValueStringWhenExists() {
    AWSSimpleSystemsManagement ssm = mock(AWSSimpleSystemsManagement.class);
    ConnectionInfo ci = connectionInfo();
    GetParameterRequest req = new GetParameterRequest();
    req.setName(parameterName);
    req.setWithDecryption(true);
    String parameterValue = toJson(ci);
    Parameter parameter = parameterOf(parameterName, parameterValue);
    GetParameterResult result = new GetParameterResult();
    result.setParameter(parameter);
    when(ssm.getParameter(eq(req))).thenReturn(result);
    assertThat(dcm.getParameterValue(ssm, parameterName).orElse(null)).isEqualTo(toJson(ci));
  }

  private Parameter parameterOf(String name, String value) {
    Parameter parameter = new Parameter();
    parameter.setName(name);
    parameter.setValue(value);
    return parameter;
  }

  @SneakyThrows
  private String toJson(Object o) {
    return JacksonConfig.createMapper().writeValueAsString(o);
  }
}
