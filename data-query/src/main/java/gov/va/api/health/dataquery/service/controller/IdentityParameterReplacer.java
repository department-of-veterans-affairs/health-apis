package gov.va.api.health.dataquery.service.controller;

import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.ResourceIdentity;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.MultiValueMap;

/** Leverages the Identity Service to replace _identifier_ type parameters in Queries. */
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
    // PETERTODO is this still necessary ?
    // @Singular Map<String, String> emits a compiler warning from the Lombok code (??)
    // List of pairs is a workaround.
    this.aliases =
        aliases
            .stream()
            .collect(Collectors.toMap(alias -> alias.getKey(), alias -> alias.getValue()));
  }

  /** Return true if the given identity belongs to the CDW system. */
  static boolean isCdw(@NonNull ResourceIdentity identity) {
    return "CDW".equals(identity.system());
  }

  private String aliasOf(String key) {
    return aliases.getOrDefault(key, key);
  }

  private boolean isIdentity(String key) {
    return identityKeys.contains(key);
  }

  private String lookupCdwId(String uuid) {
    return identityService
        .lookup(uuid)
        .stream()
        .filter(IdentityParameterReplacer::isCdw)
        .map(ResourceIdentity::identifier)
        .findFirst()
        .orElse(uuid);
  }

  /**
   * Return a new Query that matches the given original query except identity type parameters will
   * have been replaced with CDW identity values returned for the Identity Service.
   */
  MultiValueMap<String, String> rebuildWithCdwIdentities(
      MultiValueMap<String, String> originalParameters) {
    if (originalParameters == null) {
      return Parameters.empty();
    }
    Parameters parameters = Parameters.builder();
    for (Entry<String, List<String>> entry : originalParameters.entrySet()) {
      if (isIdentity(entry.getKey())) {
        for (String value : entry.getValue()) {
          if (StringUtils.isBlank(value)) {
            throw new WitnessProtection.MissingSearchParameters(originalParameters);
          }
          parameters.add(aliasOf(entry.getKey()), lookupCdwId(value));
        }
      } else {
        parameters.addAll(aliasOf(entry.getKey()), entry.getValue());
      }
    }
    return parameters.build();
  }
}
