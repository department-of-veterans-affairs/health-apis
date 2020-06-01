package gov.va.api.health.dataquery.service.controller.procedure;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Procedure hack logic used to swap one patient's information with another at service request time.
 */
@Slf4j
@Builder
@Component
public class ProcedureHack {

  /**
   * Optional ID for a patient with procedure data that can secretly service requests for {@link
   * #withoutRecordsId}.
   */
  final String withRecordsId;

  /**
   * Optional ID for a patient with no procedure data, whose requests can be secretly serviced by
   * {@link #withRecordsId}.
   */
  final String withoutRecordsId;

  final String withRecordsDisplay;

  final String withoutRecordsDisplay;

  /** Constructor. */
  @Autowired
  public ProcedureHack(
      @Value("${procedure.test-patient-workaround.id-with-records:}") String withRecordsId,
      @Value("${procedure.test-patient-workaround.id-without-records:}") String withoutRecordsId,
      @Value("${procedure.test-patient-workaround.display-with-records:}")
          String withRecordsDisplay,
      @Value("${procedure.test-patient-workaround.display-without-records:}")
          String withoutRecordsDisplay) {
    this.withRecordsId = withRecordsId;
    this.withoutRecordsId = withoutRecordsId;
    this.withRecordsDisplay = withRecordsDisplay;
    this.withoutRecordsDisplay = withoutRecordsDisplay;
  }

  /**
   * Disguise procedure data for patient-with-records as data for patient-without-records. {@link
   * #withRecordsId} is replaced with {@link #withoutRecordsId} and {@link #withRecordsDisplay} is
   * replaced with {@link #withoutRecordsDisplay}.
   */
  @SneakyThrows
  <T> T disguiseAsPatientWithoutRecords(T withRecords, Class<T> clazz) {
    log.info(
        "Disguising procedure for patient {} ({}) as patient {} ({}).",
        withRecordsId,
        withRecordsDisplay,
        withoutRecordsId,
        withoutRecordsDisplay);
    ObjectMapper mapper = JacksonConfig.createMapper();
    String withRecordsString = mapper.writeValueAsString(withRecords);
    String withoutRecordsString =
        withRecordsString
            .replaceAll(withRecordsId, withoutRecordsId)
            .replaceAll(withRecordsDisplay, withoutRecordsDisplay);
    return mapper.readValue(withoutRecordsString, clazz);
  }

  /**
   * In some environments, it is necessary to use procedure data for one patient, {@link
   * #withRecordsId}, to service requests for another patient, {@link #withoutRecordsId}, that has
   * none of its own. The displayed name of the patient-with-records, {@link #withRecordsDisplay},
   * is also replaced by the displayed name of the patient-without-records, {@link
   * #withoutRecordsDisplay}. This method returns {@code true} if patient {@link #withoutRecordsId}
   * is requested when all four of these values are configured.
   */
  boolean isPatientWithoutRecords(String patient) {
    return patient.equals(withoutRecordsId)
        && isNotBlank(withRecordsId)
        && isNotBlank(withoutRecordsId)
        && isNotBlank(withRecordsDisplay)
        && isNotBlank(withoutRecordsDisplay);
  }
}
