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

/** Leverages Identity Service to replace _identifier_ type parameters. */
final class IdentityParameterReplacer {
  @NonNull private final IdentityService identityService;

  @NonNull private final Set<String> identityKeys;

  @NonNull private final Map<String, String> aliases;

  @Builder
  private IdentityParameterReplacer(
      IdentityService identityService,
      @Singular Set<String> identityKeys,
      @Singular List<Pair<String, String>> aliases) {
    this.identityService = identityService;
    this.identityKeys = identityKeys;
    // @Singular Map<String, String> emits a compiler warning from the Lombok code (?)
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
   * have been replaced with CDW identity values returned from the Identity Service.
   */
  MultiValueMap<String, String> rebuildWithCdwIdentities(
      MultiValueMap<String, String> publicParameters) {
    if (publicParameters == null) {
      return Parameters.empty();
    }

    Parameters results = Parameters.builder();
    for (Entry<String, List<String>> entry : publicParameters.entrySet()) {
      if (!isIdentity(entry.getKey())) {
        results.addAll(aliasOf(entry.getKey()), entry.getValue());
        continue;
      }

      for (String value : entry.getValue()) {
        if (StringUtils.isBlank(value)) {
          throw new ResourceExceptions.MissingSearchParameters(publicParameters);
        }
        results.add(aliasOf(entry.getKey()), lookupCdwId(value));
      }
    }
    return results.build();
  }
}
