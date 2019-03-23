package gov.va.api.health.dataquery.service.controller;

import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;

/** Utility methods for working with CDW-related Resource Identity objects. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ResourceIdentities {
  /** The CDW system value. */
  @SuppressWarnings("SameReturnValue")
  public static String cdw() {
    return "CDW";
  }

  /**
   * Extract the CDW identity from the possible identities in the given registration. An error is
   * thrown if a CDW id cannot be found.
   */
  private static ResourceIdentity findCdwIdentity(@NonNull Registration registration) {
    return registration
        .resourceIdentities()
        .stream()
        .filter(ResourceIdentities::isCdw)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("No CDW identity for: " + registration));
  }

  /** Return true if the given identity belongs to the CDW system. */
  static boolean isCdw(@NonNull ResourceIdentity identity) {
    return cdw().equals(identity.system());
  }

  /** Convert the 'resource/identity' reference into a CDW Resource Identity instance. */
  static ResourceIdentity referenceToResourceIdentity(@NonNull String typeSlashId) {
    String[] parts = typeSlashId.split("/");
    if (parts.length != 2) {
      throw new IllegalArgumentException(
          "Invalid reference. Expected resource/identity. Got: " + typeSlashId);
    }
    String type = parts[0];
    String id = parts[1];
    return ResourceIdentity.builder()
        .system(ResourceIdentities.cdw())
        .resource(ResourceNameTranslation.create().fhirToIdentityService(type))
        .identifier(id)
        .build();
  }

  /** Create reference pairs of CDW and Universal identities in the `resource/identity` form. */
  static ReferencePair referencesOf(@NonNull Registration registration) {
    ResourceIdentity identity = findCdwIdentity(registration);
    String resource = ResourceNameTranslation.create().identityServiceToFhir(identity.resource());
    return ReferencePair.builder()
        .cdw(resource + "/" + identity.identifier())
        .universal(resource + "/" + registration.uuid())
        .build();
  }

  /** A pair of references that represent the same object, in the `resource/identity` form. */
  @Value
  @Builder
  static class ReferencePair {
    String cdw;

    String universal;
  }
}
