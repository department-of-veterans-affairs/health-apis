package gov.va.api.health.dataquery.tests.crawler;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class Swiggity {
  private static final String SAPIDER =
      "\n"
          + "                   /\\\n"
          + "                  /  \\\n"
          + "                 |  _ \\                  _\n"
          + "                 | / \\ \\                / \\\n"
          + "                 |/   \\ \\              /   \\\n"
          + "                 /     \\ |        /\\  /     \\\n"
          + "                /|      \\| ~  ~  /  \\/       \\\n"
          + "        _______/_|_______\\(o)(o)/___/\\_____   \\\n"
          + "       /      /  |       (______)     \\    \\   \\_\n"
          + "      /      /   |       /      \\      \\    \\\n"
          + "     /      /    |      /    []D_\\      \\    \\\n"
          + "    /      /     |      \\                \\    \\\n"
          + "   /     _/      |      _\\                \\    \\\n"
          + "  /             _|                         \\    \\_\n"
          + "_/                                          \\\n"
          + "                                             \\\n"
          + "                                              \\_"
          + "\n";

  public static void swooty(String patient) {
    log.info(
        "\n\n                     SWIGGITY SWOOTY!\n"
            + SAPIDER
            + "\n          doot! doot! "
            + "Keep on creeping on patient {}\n",
        patient);
  }
}
