package gov.va.api.health.dataquery.service.controller;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import gov.va.api.health.stu3.api.bundle.AbstractBundle;
import gov.va.api.health.stu3.api.bundle.AbstractEntry;
import gov.va.api.health.stu3.api.resources.Resource;
import lombok.Value;

@Value
public final class Stu3IncludesIcnMajig<
        R extends Resource, E extends AbstractEntry<R>, B extends AbstractBundle<E>>
    implements IncludesIcnMajig {
  private final Class<R> type;

  private final Class<B> bundleType;

  private final Function<R, Stream<String>> extractIcns;

  @Override
  @SuppressWarnings("unchecked")
  public String extractBundleIcns(Object bundle) {
    return ((B) bundle)
        .entry()
        .stream()
        .map(AbstractEntry::resource)
        .flatMap(resource -> extractIcns.apply(resource))
        .distinct()
        .collect(Collectors.joining(","));
  }

  @Override
  @SuppressWarnings("unchecked")
  public String extractResourceIcns(Object resource) {
    return extractIcns.apply((R) resource).collect(Collectors.joining());
  }
}
