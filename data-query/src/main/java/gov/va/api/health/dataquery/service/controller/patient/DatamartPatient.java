package gov.va.api.health.dataquery.service.controller.patient;

import gov.va.api.lighthouse.datamart.DatamartReference;
import gov.va.api.lighthouse.datamart.HasReplaceableId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Datamart JSON model. */
@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatamartPatient implements HasReplaceableId {
  @Builder.Default private String objectType = "Patient";

  @Builder.Default private int objectVersion = 1;

  private String fullIcn;

  private String cdwId;

  private String ssn;

  private String name;

  private String lastName;

  private String firstName;

  private String birthDateTime;

  private String deceased;

  private String deathDateTime;

  private String gender;

  private Optional<String> selfIdentifiedGender;

  private Optional<String> religion;

  private Optional<String> managingOrganization;

  private MaritalStatus maritalStatus;

  private Ethnicity ethnicity;

  private List<Race> race;

  private List<Telecom> telecom;

  private List<Address> address;

  private List<Contact> contact;

  /** Lazy getter. */
  public List<Address> address() {
    if (address == null) {
      address = new ArrayList<>();
    }
    return address;
  }

  @Override
  public DatamartReference asReference() {
    return DatamartReference.of().type(this.objectType()).reference(this.fullIcn()).build();
  }

  /** Lazy getter. */
  public List<Contact> contact() {
    if (contact == null) {
      contact = new ArrayList<>();
    }
    return contact;
  }

  /** Lazy getter. */
  public Optional<String> managingOrganization() {
    if (managingOrganization == null) {
      managingOrganization = Optional.empty();
    }
    return managingOrganization;
  }

  /** Lazy getter. */
  public List<Race> race() {
    if (race == null) {
      race = new ArrayList<>();
    }
    return race;
  }

  /** Lazy getter. */
  public Optional<String> religion() {
    if (religion == null) {
      religion = Optional.empty();
    }
    return religion;
  }

  /** Lazy getter. */
  public Optional<String> selfIdentifiedGender() {
    if (selfIdentifiedGender == null) {
      selfIdentifiedGender = Optional.empty();
    }
    return selfIdentifiedGender;
  }

  /** Lazy getter. */
  public List<Telecom> telecom() {
    if (telecom == null) {
      telecom = new ArrayList<>();
    }
    return telecom;
  }

  /** Address. */
  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class Address {
    private String type;

    private String street1;

    private String street2;

    private String street3;

    private String city;

    private String state;

    private String postalCode;

    private String county;

    private String country;
  }

  /** Contact. */
  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class Contact {
    private String name;

    private String type;

    private String relationship;

    private Phone phone;

    private Address address;

    /** Phone. */
    @Data
    @Builder
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Phone {
      private String phoneNumber;

      private String workPhoneNumber;

      private String email;
    }
  }

  /** Ethnicity. */
  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class Ethnicity {
    private String display;

    private String abbrev;

    private String hl7;
  }

  /** MaritalStatus. */
  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class MaritalStatus {
    private String display;

    private String abbrev;

    private String code;
  }

  /** Race. */
  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class Race {
    private String display;

    private String abbrev;
  }

  /** Telecom. */
  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class Telecom {
    private String type;

    private String phoneNumber;

    private String workPhoneNumber;

    private String email;
  }
}
