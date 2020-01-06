package gov.va.api.health.dataquery.service.controller;

public interface IncludesIcnMajig {
  String extractBundleIcns(Object bundle);

  String extractResourceIcns(Object resource);
  
  Object extractIcns();

  Class<?> type();

  Class<?> bundleType();
}
