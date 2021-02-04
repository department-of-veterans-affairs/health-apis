package gov.va.api.health.dataquery.service.controller.appointment;

import gov.va.api.lighthouse.datamart.DatamartReference;
import gov.va.api.lighthouse.datamart.HasReplaceableId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatamartAppointment implements HasReplaceableId {
  @Builder.Default private String objectType = "Appointment";

  @Builder.Default private String objectVersion = "1";

  private String cdwId;

  private Optional<String> status;

  private Optional<String> cancelationReason;

  private Optional<String> serviceCategory;

  private String serviceType;

  private Optional<String> specialty;

  private Optional<String> appointmentType;

  private Optional<String> description;

  private Optional<Instant> start;

  private Optional<Instant> end;

  private Optional<Integer> minutesDuration;

  private String created;

  private Optional<String> comment;

  private Optional<String> basedOn;

  private List<DatamartReference> participant;

  public Optional<String> appointmentType() {
    appointmentType = lazyGetter(appointmentType);
    return appointmentType;
  }

  public Optional<String> basedOn() {
    basedOn = lazyGetter(basedOn);
    return basedOn;
  }

  public Optional<String> cancelationReason() {
    cancelationReason = lazyGetter(cancelationReason);
    return cancelationReason;
  }

  public Optional<String> comment() {
    comment = lazyGetter(comment);
    return comment;
  }

  public Optional<String> description() {
    description = lazyGetter(description);
    return description;
  }

  public Optional<Instant> end() {
    end = lazyGetter(end);
    return end;
  }

  private <T> Optional<T> lazyGetter(Optional<T> value) {
    if (value == null) {
      return Optional.empty();
    }
    return value;
  }

  public Optional<Integer> minutesDuration() {
    minutesDuration = lazyGetter(minutesDuration);
    return minutesDuration;
  }

  public Optional<String> serviceCategory() {
    serviceCategory = lazyGetter(serviceCategory);
    return serviceCategory;
  }

  public Optional<String> specialty() {
    specialty = lazyGetter(specialty);
    return specialty;
  }

  public Optional<Instant> start() {
    start = lazyGetter(start);
    return start;
  }

  public Optional<String> status() {
    status = lazyGetter(status);
    return status;
  }
}
