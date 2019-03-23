package gov.va.api.health.dataquery.service.controller;

import gov.va.api.health.dataquery.service.controller.XmlDocuments.ParseFailed;
import gov.va.api.health.ids.api.IdentityService;
import java.net.URLDecoder;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.MultiValueMap;
import org.w3c.dom.Document;

@Slf4j
@UtilityClass
public final class WitnessProtection {
  @SneakyThrows
  private static String decode(String value) {
    return URLDecoder.decode(value, "UTF-8");
  }

  public static Document parse(MultiValueMap<String, String> parameters, String xml) {
    try {
      return XmlDocuments.create().parse(xml);
    } catch (ParseFailed e) {
      log.error("Failed to parse CDW response: {} ", e.getMessage());
      throw new SearchFailed(parameters, e);
    }
  }

  public static Document replaceCdwIdsWithPublicIds(
      IdentityService identityService,
      String resource,
      MultiValueMap<String, String> parameters,
      Document xml) {
    try {
      return InPlaceReferenceReplacer.builder()
          .resource(resource)
          .parameters(parameters)
          .document(xml)
          .identityService(identityService)
          .build()
          .replaceReferences();
    } catch (IdentityService.RegistrationFailed e) {
      throw new SearchFailed(parameters, e);
    }
  }

  public static MultiValueMap<String, String> replacePublicIdsWithCdwIds(
      IdentityService identityService, MultiValueMap<String, String> originalParameters) {
    try {
      return IdentityParameterReplacer.builder()
          .identityService(identityService)
          .identityKey("patient")
          .identityKey("patient_identifier")
          .identityKey("patient_identifier:exact")
          .identityKey("identifier")
          .identityKey("identifier:exact")
          .identityKey("_id")
          .alias(Pair.of("_id", "identifier"))
          .build()
          .rebuildWithCdwIdentities(originalParameters);
    } catch (IdentityService.LookupFailed e) {
      log.error("Failed to lookup CDW identities: {}", e.getMessage());
      throw new SearchFailed(originalParameters, e);
    } catch (IdentityService.UnknownIdentity e) {
      log.error("Identity is not known: {}", e.getMessage());
      throw new UnknownIdentityInSearchParameter(originalParameters, e);
    }
  }

  private static Stream<String> toKeyValueString(@NonNull Map.Entry<String, List<String>> entry) {
    return entry.getValue().stream().map((value) -> entry.getKey() + '=' + decode(value));
  }

  private static String toParametersString(MultiValueMap<String, String> parameters) {
    if (parameters == null || parameters.isEmpty()) {
      return "";
    }
    StringBuilder msg = new StringBuilder();
    String params =
        parameters
            .entrySet()
            .stream()
            .sorted(Comparator.comparing(Entry::getKey))
            .flatMap(WitnessProtection::toKeyValueString)
            .collect(Collectors.joining("&"));
    msg.append('?').append(params);
    return msg.toString();
  }

  static final class MissingSearchParameters extends ResourcesException {
    public MissingSearchParameters(MultiValueMap<String, String> parameters) {
      super(toParametersString(parameters));
    }
  }

  static class ResourcesException extends RuntimeException {
    ResourcesException(String message, Throwable cause) {
      super(message, cause);
    }

    ResourcesException(String message) {
      super(message);
    }
  }

  public static final class SearchFailed extends ResourcesException {
    public SearchFailed(MultiValueMap<String, String> parameters, Exception cause) {
      super(toParametersString(parameters), cause);
    }

    public SearchFailed(MultiValueMap<String, String> parameters, String message) {
      super(toParametersString(parameters) + " Reason: " + message);
    }
  }

  static final class UnknownIdentityInSearchParameter extends ResourcesException {
    public UnknownIdentityInSearchParameter(
        MultiValueMap<String, String> parameters, Exception cause) {
      super(toParametersString(parameters), cause);
    }
  }

  static final class UnknownResource extends ResourcesException {
    public UnknownResource(MultiValueMap<String, String> parameters) {
      super(toParametersString(parameters));
    }
  }
}
