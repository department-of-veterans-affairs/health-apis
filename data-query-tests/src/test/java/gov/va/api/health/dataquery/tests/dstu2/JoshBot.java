package gov.va.api.health.dataquery.tests.dstu2;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import gov.va.api.health.sentinel.LabBot;
import gov.va.api.health.sentinel.LabBot.LabBotUserResult;
import gov.va.api.health.sentinel.selenium.VaOauthRobot;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;

/**
 * This is intended to stream line testing the Production environment with real Veteran credentials.
 * Ensure properties are configured in sentinel/config/josh-bot.properties. Default access token
 * route is through ID.me. Set the 'va-oauth-robot.credentials-type' property to 'MY_HEALTHE_VET'
 * for My HealtheVet. ID.me two-factor authentication is supported via phone and code generator app.
 */
@Slf4j
public class JoshBot {

  private static Properties properties;

  private static String configurationFile() {
    return System.getProperty("joshbot.properties.file", "config/josh-bot.properties");
  }

  private static boolean doAccessTokenRefresh() {
    return BooleanUtils.toBoolean(
        properties.getProperty("va-oauth-robot.refresh-user-token", "true"));
  }

  @SneakyThrows
  private static void loadPropertiesFile() {
    File confFile = new File(configurationFile());
    assertThat(confFile.exists())
        .withFailMessage("Configuration file %s not found", confFile.getAbsolutePath())
        .isTrue();
    properties = new Properties(System.getProperties());
    try (FileInputStream inputStream = new FileInputStream(confFile)) {
      properties.load(inputStream);
    }
  }

  private static String lookupUserId() {
    String userId = properties.getProperty("va-oauth-robot.user-id");
    assertThat(userId).withFailMessage("va-oauth-robot.user-id not specified.").isNotBlank();
    return userId;
  }

  @SneakyThrows
  public static void main(String[] args) {
    loadPropertiesFile();
    LabBot bot =
        LabBot.builder()
            .userIds(Arrays.asList(lookupUserId()))
            .scopes(
                Arrays.asList(
                    "patient/AllergyIntolerance.read",
                    "patient/Condition.read",
                    "patient/DiagnosticReport.read",
                    "patient/Immunization.read",
                    "patient/Medication.read",
                    "patient/MedicationOrder.read",
                    "patient/MedicationStatement.read",
                    "patient/Observation.read",
                    "patient/Patient.read",
                    "patient/Procedure.read",
                    "openid",
                    "profile",
                    "offline_access",
                    "launch/patient"))
            .configFile(configurationFile())
            .build();
    String basePath = "/services/fhir/v0/dstu2/";
    List<LabBotUserResult> patientCallUsingLabBot = bot.request(basePath + "Patient?_id={icn}");
    List<String> winners = new ArrayList<>();
    List<String> losers = new ArrayList<>();
    LabBotUserResult patientCall = Iterables.getOnlyElement(patientCallUsingLabBot);
    if (!patientCall.tokenExchange().isError()) {
      log.info(
          "Winner: {} is patient {}.",
          patientCall.user().id(),
          patientCall.tokenExchange().patient());
      winners.add(patientCall.user().id() + " is patient " + patientCall.tokenExchange().patient());
      List<String> resourceToCheck =
          Arrays.asList(
              "AllergyIntolerance",
              "Condition",
              "DiagnosticReport",
              "Immunization",
              "MedicationOrder",
              "MedicationStatement",
              "Observation",
              "Procedure");
      String patientIcn = patientCall.tokenExchange().patient();
      for (String resource : resourceToCheck) {
        String path = basePath + resource + "?patient=" + patientIcn;
        String response = request(bot, patientCall, path);
        try {
          JsonNode root = new ObjectMapper().readTree(response);
          winners.add(resource + " search returned with " + root.findValue("total") + " records");
        } catch (Exception e) {
          losers.add(
              "\n"
                  + resource
                  + " call failed with following error message\n"
                  + e.getMessage()
                  + "\ns");
        }
      }
    } else {
      log.info(
          "Loser: {} is patient {}.",
          patientCall.user().id(),
          patientCall.tokenExchange().patient());
      losers.add(
          patientCall.user().id()
              + " is patient "
              + patientCall.tokenExchange().patient()
              + " - Token Exchange Error"
              + patientCall.tokenExchange().error()
              + ": "
              + patientCall.tokenExchange().errorDescription()
              + " - Request Error: "
              + patientCall.response());
    }
    String report =
        Stream.concat(winners.stream().map(w -> w + " - OK"), losers.stream())
            .collect(Collectors.joining("\n"));
    Files.write(
        new File("target/production-results.txt").toPath(),
        report.getBytes(StandardCharsets.UTF_8));
    log.info("Prod Users:\n{}", report);
    // Use the patient from the original request when requesting with the refresh token.
    if (doAccessTokenRefresh()) {
      String refreshTestPath = basePath + "Patient?_id=" + patientCall.tokenExchange().patient();
      VaOauthRobot.TokenExchange refreshedAccessToken =
          patientCall.vaOauthRobot().refreshUserAccessToken();
      log.info("Making Patient request using new access token...");
      request(
          bot,
          LabBotUserResult.builder()
              .user(patientCall.user())
              .tokenExchange(refreshedAccessToken)
              .build(),
          refreshTestPath);
    }
    log.info("JoshBot Has Completed All It's Tasks...");
  }

  @SneakyThrows
  private static String request(LabBot bot, LabBotUserResult patientReadResult, String path) {
    try {
      return bot.request(path, patientReadResult.tokenExchange().accessToken());
    } catch (Exception e) {
      log.error(
          "Request failure {} {}: {}",
          patientReadResult.user().id(),
          path,
          e.getMessage(),
          e.getCause());
      return "ERROR: " + e.getClass().getName() + ": " + e.getMessage();
    }
  }
}
