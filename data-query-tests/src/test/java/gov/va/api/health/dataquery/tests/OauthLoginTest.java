package gov.va.api.health.dataquery.tests;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.sentinel.LabBot;
import gov.va.api.health.sentinel.LabBot.LabBotUserResult;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class OauthLoginTest {

  private List<String> winners = new CopyOnWriteArrayList<>();

  private List<String> losers = new CopyOnWriteArrayList<>();

  public static List<String> dataQueryScopes() {
    return Arrays.asList(
        "patient/AllergyIntolerance.read",
        "patient/Condition.read",
        "patient/DiagnosticReport.read",
        "patient/Immunization.read",
        "patient/Medication.read",
        "patient/MedicationOrder.read",
        "patient/MedicationStatement.read",
        "patient/Observation.read",
        "patient/Patient.read",
        "patient/Procedure.read");
  }

  @Test
  @SneakyThrows
  public void RequestTest() {
    List<LabBotUserResult> labBotUserResultList =
        LabBot.builder()
            .userIds(LabBot.allUsers())
            .scopes(dataQueryScopes())
            .configPathName("config/lab.properties")
            .build()
            .request("/services/fhir/v0/dstu2/Patient/");
    winnersLosers(labBotUserResultList);
    String report =
        Stream.concat(winners.stream().map(w -> w + " - OK"), losers.stream())
            .sorted()
            .collect(Collectors.joining("\n"));
    Files.write(new File("lab-users.txt").toPath(), report.getBytes(StandardCharsets.UTF_8));
    log.info("Lab Users:\n{}", report);
    assertThat(losers.size()).isZero();
  }

  private void winnersLosers(List<LabBotUserResult> labBotUserResultList) {
    for (LabBotUserResult labBotUserResult : labBotUserResultList) {
      if (!labBotUserResult.tokenExchange.isError()
          && labBotUserResult.response.contains("\"resourceType\":\"Patient\"")) {
        log.info(
            "Winner: {} is patient {}.",
            labBotUserResult.user.id(),
            labBotUserResult.tokenExchange.patient());
        winners.add(labBotUserResult.user.id());
      } else {
        log.info(
            "Loser: {} is patient {}.",
            labBotUserResult.user.id(),
            labBotUserResult.tokenExchange.patient());
        losers.add(
            labBotUserResult.user.id()
                + " - "
                + labBotUserResult.tokenExchange.error()
                + ": "
                + labBotUserResult.tokenExchange.errorDescription());
      }
    }
  }
}
