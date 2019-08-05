package gov.va.api.health.dataquery.service.controller.datamart;

/**
 * This interface is used to indicate that this object has CDW ID that should be replaced with a
 * public ID. The {@link gov.va.api.health.dataquery.service.controller.WitnessProtection} utility
 * can make use of this to automatically register and update the ID value.
 */
public interface HasReplaceableId {

  /** Represent this object as a reference. */
  default DatamartReference asReference() {
    return DatamartReference.of().type(objectType()).reference(cdwId()).build();
  }

  /** Update the ID of this object. */
  Object cdwId(String id);

  /** The ID of this object. */
  String cdwId();

  /** The resource type of this object. */
  String objectType();
}
