package gov.va.api.health.dataquery.service.controller.practitioner;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dataquery.service.controller.datamart.HasReplaceableId;
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

  private Optional<PractitionerRole> practitionerRole;

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
  public Optional<PractitionerRole> practitionerRole() {
    if (practitionerRole == null) {
      practitionerRole = Optional.empty();
    }
    return practitionerRole;
  }

  /** Lazy initialization. */
  public List<Telecom> telecom() {
    if (telecom == null) {
      telecom = new ArrayList<>();
    }
    return telecom;
  }

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
  public static final class PractitionerRole {
    private Optional<DatamartReference> managingOrganization;

    private Optional<DatamartCoding> role;

    private List<Specialty> specialty;

    private Optional<Period> period;

    private List<DatamartReference> location;

    private Optional<String> healthCareService;

    /** Lazy initialization. */
    public Optional<String> healthCareService() {
      if (healthCareService == null) {
        healthCareService = Optional.empty();
      }
      return healthCareService;
    }

    /** Lazy initialization. */
    public List<DatamartReference> location() {
      if (location == null) {
        location = new ArrayList<>();
      }
      return location;
    }

    /** Lazy initialization. */
    public Optional<DatamartReference> managingOrganization() {
      if (managingOrganization == null) {
        managingOrganization = Optional.empty();
      }
      return managingOrganization;
    }

    /** Lazy initialization. */
    public Optional<Period> period() {
      if (period == null) {
        period = Optional.empty();
      }
      return period;
    }

    /** Lazy initialization. */
    public Optional<DatamartCoding> role() {
      if (role == null) {
        role = Optional.empty();
      }
      return role;
    }

    /** Lazy initialization. */
    public List<Specialty> specialty() {
      if (specialty == null) {
        specialty = new ArrayList<>();
      }
      return specialty;
    }

    @Data
    @Builder
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Period {
      private Optional<LocalDate> start;

      private Optional<LocalDate> end;

      /** Lazy initialization. */
      public Optional<LocalDate> end() {
        if (end == null) {
          end = Optional.empty();
        }
        return end;
      }

      /** Lazy initialization. */
      public Optional<LocalDate> start() {
        if (start == null) {
          start = Optional.empty();
        }
        return start;
      }
    }

    @Data
    @Builder
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Specialty {
      private Optional<String> providerType;

      private Optional<String> classification;

      private Optional<String> areaOfSpecialization;

      private Optional<String> vaCode;

      private Optional<String> x12Code;

      private Optional<String> specialtyCode;

      /** Lazy initialization. */
      public Optional<String> areaOfSpecialization() {
        if (areaOfSpecialization == null) {
          areaOfSpecialization = Optional.empty();
        }
        return areaOfSpecialization;
      }

      /** Lazy initialization. */
      public Optional<String> classification() {
        if (classification == null) {
          classification = Optional.empty();
        }
        return classification;
      }

      /** Lazy initialization. */
      public Optional<String> providerType() {
        if (providerType == null) {
          providerType = Optional.empty();
        }
        return providerType;
      }

      /** Lazy Initialization. */
      public Optional<String> specialtyCode() {
        if (specialtyCode == null) {
          specialtyCode = Optional.empty();
        }
        return specialtyCode;
      }

      /** Lazy initialization. */
      public Optional<String> vaCode() {
        if (vaCode == null) {
          vaCode = Optional.empty();
        }
        return vaCode;
      }

      /** Lazy initialization. */
      public Optional<String> x12Code() {
        if (x12Code == null) {
          x12Code = Optional.empty();
        }
        return x12Code;
      }
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
