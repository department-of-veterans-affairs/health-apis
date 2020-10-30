package gov.va.api.health.dataquery.service.controller.vulcanizer;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dataquery.service.controller.datamart.HasReplaceableId;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.resources.Resource;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

class Foos {
  @Builder
  static class FooBundle extends AbstractBundle<FooEntry> {}

  @Data
  @AllArgsConstructor
  static class FooDatamart implements HasReplaceableId {
    String cdwId;

    DatamartReference patient;

    public static FooResource toResource(FooDatamart fooDatamart) {
      return FooResource.builder()
          .id(fooDatamart.cdwId)
          .ref(fooDatamart.patient.reference().orElse(null))
          .build();
    }

    @Override
    public String objectType() {
      return "FOO";
    }
  }

  @AllArgsConstructor
  @Data
  static class FooEntity {
    String cdwId;

    String ref;

    String payload() {
      return "payload:" + cdwId + ":" + ref;
    }

    FooDatamart toDatamart() {
      return new FooDatamart(cdwId, DatamartReference.of().type("WHATEVER").reference(ref).build());
    }
  }

  static class FooEntry extends AbstractEntry<FooResource> {}

  @Data
  @Builder
  static class FooResource implements Resource {
    String id;

    String implicitRules;

    String language;

    Meta meta;

    String ref;
  }

  static class Ids {
    public static ResourceIdentity id(String resource, String cdwId) {
      return ResourceIdentity.builder().system("CDW").resource(resource).identifier(cdwId).build();
    }

    public static Registration registration(String resource, String cdwId, String publicId) {
      return Registration.builder()
          .uuid(publicId)
          .resourceIdentities(List.of(id(resource, cdwId)))
          .build();
    }
  }
}
