package gov.va.api.health.dataquery.service.controller.vulcanizer;

import gov.va.api.lighthouse.vulcan.CircuitBreaker;
import gov.va.api.lighthouse.vulcan.Specifications;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import org.springframework.data.jpa.domain.Specification;

/** Provides Specification function shortcuts for simple system to field name mappings. */
@Data
@NoArgsConstructor
public class SystemIdColumns<EntityT> {
  private final List<SystemColumnMapping<EntityT>> columns = new ArrayList<>();
  private String param;

  public static <E> SystemIdColumns<E> forEntity(Class<E> entity) {
    return new SystemIdColumns<>();
  }

  public SystemIdColumns<EntityT> add(String system, String column) {
    columns.add(SystemColumnMapping.<EntityT>of(system, column));
    return this;
  }

  public SystemIdColumns<EntityT> add(
      String system, String column, Function<String, String> converter) {
    columns.add(SystemColumnMapping.<EntityT>of(system, column, converter));
    return this;
  }

  public SystemIdColumns<EntityT> add(
      String system, BiFunction<String, String, Specification<EntityT>> function) {
    columns.add(SystemColumnMapping.<EntityT>of(system, function));
    return this;
  }

  /** Generates BiFunction from mappings. */
  public BiFunction<String, String, Specification<EntityT>> matchSystemAndCode() {
    return (system, code) -> {
      for (SystemColumnMapping<EntityT> column : columns) {
        if (column.system().equals(system)) {
          return column.withSystemAndCode().apply(system, code);
        }
      }
      throw CircuitBreaker.noResultsWillBeFound(param, system, "Unknown system");
    };
  }

  /** Generates Function from mappings. */
  public Function<String, Specification<EntityT>> matchSystemOnly() {
    return system -> {
      for (SystemColumnMapping<EntityT> column : columns) {
        if (column.system().equals(system)) {
          return column.withSystem().apply(system);
        }
      }
      throw CircuitBreaker.noResultsWillBeFound(param, system, "Unknown system");
    };
  }

  @Builder
  @Value
  public static class SystemColumnMapping<EntityT> {
    @NonNull String system;

    String column;

    Function<String, String> converter;

    Function<String, Specification<EntityT>> withSystem;

    BiFunction<String, String, Specification<EntityT>> withSystemAndCode;

    /** Creates mapping for system and column. */
    public static <E> SystemColumnMapping<E> of(String system, String column) {
      return of(system, column, Function.identity());
    }

    /** Creates mapping for system and column with value converter. */
    public static <E> SystemColumnMapping<E> of(
        String system, String column, Function<String, String> converter) {
      return SystemColumnMapping.<E>builder()
          .system(system)
          .column(column)
          .withSystem(
              (s) -> {
                return Specifications.<E>selectNotNull(column);
              })
          .withSystemAndCode(
              (s, c) -> {
                return Specifications.<E>select(column, converter.apply(c));
              })
          .build();
    }

    /** Creates mapping with a custom function. */
    public static <E> SystemColumnMapping<E> of(
        String system, BiFunction<String, String, Specification<E>> customSystemAndCodeFunction) {
      return SystemColumnMapping.<E>builder()
          .system(system)
          .withSystemAndCode(customSystemAndCodeFunction)
          .build();
    }
  }
}
