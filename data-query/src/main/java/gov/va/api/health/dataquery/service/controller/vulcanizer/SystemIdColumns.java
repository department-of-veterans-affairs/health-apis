package gov.va.api.health.dataquery.service.controller.vulcanizer;

import gov.va.api.lighthouse.vulcan.CircuitBreaker;
import gov.va.api.lighthouse.vulcan.Specifications;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

/** Provides Specification function shortcuts for simple system to field name mappings. */
@RequiredArgsConstructor
public class SystemIdColumns<EntityT> {
  private final List<SystemColumnMapping<EntityT>> columns = new ArrayList<>();
  private final String param;

  public static <E> SystemIdColumns<E> forEntity(Class<E> entity, String param) {
    return new SystemIdColumns<>(param);
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
  public BiFunction<String, String, Specification<EntityT>> forSystemAndCode() {
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
  public Function<String, Specification<EntityT>> forSystemOnly() {
    return system -> {
      for (SystemColumnMapping<EntityT> column : columns) {
        if (column.system().equals(system)) {
          return column.withSystem().apply(system);
        }
      }
      throw CircuitBreaker.noResultsWillBeFound(param, system, "Unknown system");
    };
  }

  @Data
  public static class SystemColumnMapping<EntityT> {
    String system;

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
      var c = new SystemColumnMapping<E>();
      c.system(system);
      c.column(column);
      c.withSystem(
          (s) -> {
            return Specifications.<E>selectNotNull(column);
          });
      c.withSystemAndCode(
          (s, code) -> {
            return Specifications.<E>select(column, converter.apply(code));
          });
      return c;
    }

    /** Creates mapping with a custom function. */
    public static <E> SystemColumnMapping<E> of(
        String system, BiFunction<String, String, Specification<E>> customSystemAndCodeFunction) {
      var c = new SystemColumnMapping<E>();
      c.system(system);
      c.withSystemAndCode(customSystemAndCodeFunction);
      return c;
    }
  }
}
