package gov.va.api.health.dataquery.service.controller.bulk;

import static gov.va.api.health.dataquery.service.controller.Transformers.isBlank;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.condition.ConditionEntity;
import gov.va.api.health.dataquery.service.controller.condition.ConditionRepository;
import gov.va.api.health.dataquery.service.controller.condition.R4ConditionTransformer;
import gov.va.api.health.dataquery.service.controller.datamart.HasPayload;
import gov.va.api.health.dataquery.service.controller.observation.ObservationPayloadDto;
import gov.va.api.health.dataquery.service.controller.observation.ObservationRepository;
import gov.va.api.health.dataquery.service.controller.observation.R4ObservationTransformer;
import gov.va.api.health.dataquery.service.controller.patient.PatientEntityV2;
import gov.va.api.health.dataquery.service.controller.patient.PatientRepositoryV2;
import gov.va.api.health.dataquery.service.controller.patient.R4PatientTransformer;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Slf4j
@Validated
@RestController
@SuppressWarnings("WeakerAccess")
@RequestMapping(
    value = {"/bulk"},
    produces = {"application/ndjson", "application/fhir+ndjson"})
@AllArgsConstructor(onConstructor_ = {@Autowired})
public class StreamingBulkController {
  private final ConditionRepository conditionRepository;

  private final ObservationRepository observationRepository;

  private final PatientRepositoryV2 patientRepository;

  private final ObjectMapper mapper = JacksonConfig.createMapper();

  <T> Consumer<T> appendLine(OutputStream out) {
    var ndjson = new PrintWriter(out);
    return json -> {
      if (json == null) {
        return;
      }
      try {
        ndjson.println(json);
        ndjson.flush();
      } catch (Exception e) {
        log.warn("Ignoring failure: {}", e.getMessage());
      }
    };
  }

  @GetMapping(value = "/Condition")
  public ResponseEntity<StreamingResponseBody> conditions(
      final HttpServletResponse response,
      @RequestParam(value = "_since", required = false) String since,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "1000000") @Min(0) int count) {
    return stream(
        response,
        page,
        count,
        ConditionEntity.naturalOrder(),
        databaseQuery(conditionRepository, since),
        dm -> R4ConditionTransformer.builder().datamart(dm).build());
  }

  <DTO extends HasPayload<?>> Function<PageRequest, Page<DTO>> databaseQuery(
      BulkRepository<DTO> repository, String since) {
    if (!isBlank(since)) {
      return pageRequest ->
          repository.findByLastUpdatedGreaterThan(Instant.parse(since), pageRequest);
    }
    return repository::findAllProjectedBy;
  }

  @GetMapping
  public ResponseEntity<StreamingResponseBody> export(
      @RequestParam(value = "_outputFormat", required = false) String outputFormat,
      @RequestParam(value = "_since", required = false) String since,
      @RequestParam(value = "_type", required = false) String type) {
    if (isBlank(type)) {
      return null;
    }
    List<String> resourceTypes =
        Splitter.on(",").splitToList(type).stream()
            .map(String::toUpperCase)
            .collect(Collectors.toList());
    return null;
  }

  @GetMapping(value = "/Observation")
  public ResponseEntity<StreamingResponseBody> observations(
      final HttpServletResponse response,
      @RequestParam(value = "_since", required = false) String since,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "1000000") @Min(0) int count) {
    Function<PageRequest, Page<ObservationPayloadDto>> funk;
    if (!isBlank(since)) {
      Instant sinceInstant = Instant.parse(since);
      funk =
          pageRequest -> observationRepository.findByDateUtcGreaterThan(sinceInstant, pageRequest);
    } else {
      funk = observationRepository::findAllProjectedBy;
    }
    return stream(
        response,
        page,
        count,
        null,
        funk,
        dm -> R4ObservationTransformer.builder().datamart(dm).build().toFhir());
  }

  PageRequest pageRequestFor(int page, int count, Sort sort) {
    if (sort == null) {
      return PageRequest.of(page - 1, count == 0 ? 1 : count);
    }
    return PageRequest.of(page - 1, count == 0 ? 1 : count, sort);
  }

  @GetMapping(value = "/Patient")
  public ResponseEntity<StreamingResponseBody> patients(
      final HttpServletResponse response,
      @RequestParam(value = "_since", required = false) String since,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "1000000") @Min(0) int count) {
    return stream(
        response,
        page,
        count,
        PatientEntityV2.naturalOrder(),
        databaseQuery(patientRepository, since),
        dm -> R4PatientTransformer.builder().datamart(dm).build().toFhir());
  }

  <T, R> Function<T, R> quietly(Function<T, R> function) {
    return t -> {
      if (t == null) {
        return null;
      }
      try {
        return function.apply(t);
      } catch (Exception e) {
        log.warn("Ignoring failure: {}", e.getMessage());
        return null;
      }
    };
  }

  public <DM, DTO extends HasPayload<DM>, FHIR> ResponseEntity<StreamingResponseBody> stream(
      HttpServletResponse response,
      int page,
      int count,
      Sort sort,
      Function<PageRequest, Page<DTO>> query,
      Function<DM, FHIR> transform) {
    StreamingResponseBody stream =
        out -> {
          query.apply(pageRequestFor(page, count, sort)).stream()
              .parallel()
              .map(quietly(HasPayload::deserialize))
              .map(quietly(transform))
              .map(quietly(this::toJson))
              .forEach(appendLine(out));
        };
    return new ResponseEntity<>(stream, HttpStatus.OK);
  }

  @SneakyThrows
  String toJson(Object o) {
    return mapper.writeValueAsString(o);
  }
}
