package gov.va.api.health.dataquery.service.controller.practitioner;

import gov.va.api.lighthouse.datamart.HasReplaceableId;
import java.time.LocalDate;
import java.util.ArrayList;
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
public class DatamartPractitioner implements HasReplaceableId {
  @Builder.Default private String objectType = "Practitioner";

  @Builder.Default private int objectVersion = 1;

  private String cdwId;

  private Optional<String> npi;

  private Boolean active;

  private Name name;

  private List<Telecom> telecom;

  private List<Address> address;

  private Gender gender;

  private Optional<LocalDate> birthDate;

  /** Lazy initialization. */
  public List<Address> address() {
    if (address == null) {
      address = new ArrayList<>();
    }
    return address;
  }

  /** Lazy initialization. */
  public Optional<LocalDate> birthDate() {
    if (birthDate == null) {
      birthDate = Optional.empty();
    }
    return birthDate;
  }

  /** Lazy initialization. */
  public Optional<String> npi() {
    if (npi == null) {
      npi = Optional.empty();
    }
    return npi;
  }

  /** Lazy initialization. */
  public List<Telecom> telecom() {
    if (telecom == null) {
      telecom = new ArrayList<>();
    }
    return telecom;
  }

  /** Gender. */
  public enum Gender {
    male,
    female,
    unknown
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class Address {
    private Boolean temp;

    private String line1;

    private String line2;

    private String line3;

    private String city;

    private String state;

    private String postalCode;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class Name {
    private String family;

    private String given;

    private Optional<String> prefix;

    private Optional<String> suffix;

    /** Lazy initialization. */
    public Optional<String> prefix() {
      if (prefix == null) {
        prefix = Optional.empty();
      }
      return prefix;
    }

    /** Lazy initialization. */
    public Optional<String> suffix() {
      if (suffix == null) {
        suffix = Optional.empty();
      }
      return suffix;
    }
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class Telecom {
    private System system;

    private String value;

    private Use use;

    @SuppressWarnings("JavaLangClash")
    public enum System {
      phone,
      fax,
      pager,
      email
    }

    public enum Use {
      work,
      home,
      mobile
    }
  }
}
