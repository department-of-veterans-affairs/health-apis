package gov.va.api.health.dataquery.service.controller.practitioner;

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

  /** Lazy initialization. */
  public Optional<String> npi() {
    if (npi == null) {
      npi = Optional.empty();
    }
    return npi;
  }

  private Boolean active;

  private Name name;

  private List<Telecom> telecom;

  /** Lazy initialization. */
  public List<Telecom> telecom() {
    if (telecom == null) {
      telecom = new ArrayList<>();
    }
    return telecom;
  }

  private List<Address> address;

  /** Lazy initialization. */
  public List<Address> address() {
    if (address == null) {
      address = new ArrayList<>();
    }
    return address;
  }

  private Gender gender;

  public enum Gender {
    male,
    female,
    unknown
  }

  private LocalDate birthDate;

  private PractitionerRole practitionerRole;

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
    //	  "managingOrganization": {
    //      "reference": "561596:I",
    //      "display": "CHEYENNE VA MEDICAL"
    //    },
    //    "role": {
    //      "system": "rpcmm",
    //      "code": "37",
    //      "display": "PSYCHOLOGIST"
    //    },
    //    "specialty": [{
    //        "providerType": "Physicians (M.D. and D.O.)",
    //        "classification": "Physician\/Osteopath",
    //        "areaOfSpecialization": "Internal Medicine",
    //        "vaCode": "V111500",
    //        "x12Code": null
    //      }, {
    //        "providerType": "Physicians (M.D. and D.O.)",
    //        "classification": "Physician\/Osteopath",
    //        "areaOfSpecialization": "General Practice",
    //        "vaCode": "V111000",
    //        "x12Code": null
    //      }, {
    //        "providerType": "Physicians (M.D. and D.O.)",
    //        "classification": "Physician\/Osteopath",
    //        "areaOfSpecialization": "Family Practice",
    //        "vaCode": "V110900",
    //        "x12Code": null
    //      }, {
    //        "providerType": "Allopathic & Osteopathic Physicians",
    //        "classification": "Family Medicine",
    //        "areaOfSpecialization": null,
    //        "vaCode": "V180700",
    //        "x12Code": "207Q00000X"
    //      }
    //    ],
    //    "period": {
    //      "start": "1988-08-19",
    //      "end": null
    //    },
    //    "location": [{
    //        "reference": "43817:L",
    //        "display": "CHEY MEDICAL"
    //      }, {
    //        "reference": "43829:L",
    //        "display": "ZZCHY LASTNAME MEDICAL"
    //      }, {
    //        "reference": "43841:L",
    //        "display": "ZZCHY WID BACK"
    //      }
    //    ],
    //    "healthCareService": "MEDICAL SERVICE"
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class Telecom {
    private System system;

    private String value;

    private Use use;

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
