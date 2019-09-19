package gov.va.api.health.dataquery.tests;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.sentinel.LabBot;
import gov.va.api.health.sentinel.LabBot.LabBotUserResult;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class OauthLoginTest {

  @Test
  @SneakyThrows
  public void RequestTest() {
    List<LabBotUserResult> labBotUserResultList =
        LabBot.builder()
            .userIds(userIds())
            .scopes(DataQueryScopes.labResources())
            .configFile("config/lab.properties")
            .build()
            .request("/services/fhir/v0/dstu2/Patient/{icn}");
    List<String> winners = new ArrayList<>();
    List<String> losers = new ArrayList<>();
    for (LabBotUserResult labBotUserResult : labBotUserResultList) {
      if (!labBotUserResult.tokenExchange().isError()
          && labBotUserResult.response().contains("\"resourceType\":\"Patient\"")) {
        log.info(
            "Winner: {} is patient {}.",
            labBotUserResult.user().id(),
            labBotUserResult.tokenExchange().patient());
        winners.add(
            labBotUserResult.user().id()
                + " is patient "
                + labBotUserResult.tokenExchange().patient()
                + " - "
                + labBotUserResult.tokenExchange().accessToken());
      } else {
        log.info(
            "Loser: {} is patient {}.",
            labBotUserResult.user().id(),
            labBotUserResult.tokenExchange().patient());
        losers.add(
            labBotUserResult.user().id()
                + " is patient "
                + labBotUserResult.tokenExchange().patient()
                + " - Token Exchange Error: "
                + labBotUserResult.tokenExchange().error()
                + ": "
                + labBotUserResult.tokenExchange().errorDescription()
                + "  "
                + labBotUserResult.response());
      }
    }
    String report =
        Stream.concat(winners.stream().map(w -> w + " - OK"), losers.stream())
            .sorted()
            .collect(Collectors.joining("\n"));
    Files.write(new File("lab-users.txt").toPath(), report.getBytes(StandardCharsets.UTF_8));
    log.info("Lab Users:\n{}", report);
    assertThat(losers.size()).isZero();
  }

  public List<String> userIds() {
    String userSpecified = System.getProperty("lab.user");
    if (isBlank(userSpecified)) {
      return LabBot.allUsers();
    }
    return List.of(userSpecified);
  }
}
