package gov.va.api.health.mranderson.cdw.impl;

import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.mranderson.cdw.Query;
import gov.va.api.health.mranderson.cdw.Resources.MissingSearchParameters;
import gov.va.api.health.mranderson.util.Parameters;
import gov.va.api.health.mranderson.util.TimeIt;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

/** Leverages the Identity Service to replace _identifier_ type parameters in Queries. */
@Slf4j
class IdentityParameterReplacer {

  private final IdentityService identityService;
  private final Set<String> identityKeys;
  private final Map<String, String> aliases;

  @Builder
  private IdentityParameterReplacer(
      IdentityService identityService,
      @Singular Set<String> identityKeys,
      @Singular List<Pair<String, String>> aliases) {
    this.identityService = identityService;
    this.identityKeys = identityKeys;

    // @Singular Map<String, String> emits a compiler warning from the Lombok code (??)
    // List of pairs is a workaround.
    this.aliases =
        aliases
            .stream()
            .collect(Collectors.toMap(alias -> alias.getKey(), alias -> alias.getValue()));
  }

  private String aliasOf(String key) {
    return aliases.getOrDefault(key, key);
  }

  private boolean isIdentity(String key) {
    return identityKeys.contains(key);
  }

  private String lookupCdwId(String uuid) {
    List<ResourceIdentity> identities =
        TimeIt.logTime(() -> identityService.lookup(uuid), "Identity Service Lookup");
    return identities
        .stream()
        .filter(ResourceIdentities::isCdw)
        .map(ResourceIdentity::identifier)
        .findFirst()
        .orElse(uuid);
  }

  /**
   * Return a new Query that matches the given original query except identity type parameters will
   * have been replaced with CDW identity values returned for the Identity Service.
   */
  Query rebuildWithCdwIdentities(Query originalQuery) {
    Parameters parameters = Parameters.builder();
    for (Entry<String, List<String>> entry : originalQuery.parameters().entrySet()) {
      if (isIdentity(entry.getKey())) {
        for (String value : entry.getValue()) {
          if (StringUtils.isBlank(value)) {
            throw new MissingSearchParameters(originalQuery);
          }
          parameters.add(aliasOf(entry.getKey()), lookupCdwId(value));
        }
      } else {
        parameters.addAll(aliasOf(entry.getKey()), entry.getValue());
      }
    }
    return originalQuery.toBuilder().parameters(parameters.build()).build();
  }
}
