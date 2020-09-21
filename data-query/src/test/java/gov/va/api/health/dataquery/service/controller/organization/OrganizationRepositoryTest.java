package gov.va.api.health.dataquery.service.controller.organization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;

@DataJpaTest
public class OrganizationRepositoryTest {
  @Autowired OrganizationRepository repository;

  @Test
  void addressSpecifications() {
    initializeData();
    searchByAddressContainingState();
    searchByAddressContainingPostalCodeAndByAddressCity();
    searchByAddressContainingStreet();
    searchByAddressContainingCityAndByStreet();
    searchByAddressContainingStateAndByAddressPostalCode();
    searchByAddressContainingCityAndByAddressStateAndByAddressPostalCode();
    emptyPredicatesWillThrowInvalidDataAccessApiUsageException();
  }

  private void emptyPredicatesWillThrowInvalidDataAccessApiUsageException() {
    assertThrows(
        InvalidDataAccessApiUsageException.class,
        () -> repository.findAll(OrganizationRepository.AddressSpecification.builder().build()));
  }

  private void initializeData() {
    repository.save(Samples.ENTITY_ONE);
    repository.save(Samples.ENTITY_TWO);
    repository.save(Samples.ENTITY_THREE);
  }

  private void searchByAddressContainingCityAndByAddressStateAndByAddressPostalCode() {
    assertThat(
            repository.findAll(
                OrganizationRepository.AddressSpecification.builder()
                    .address("SecondCity")
                    .state("SecondState")
                    .postalCode("22222")
                    .build()))
        .isEqualTo(List.of(Samples.ENTITY_TWO));
  }

  private void searchByAddressContainingCityAndByStreet() {
    assertThat(
            repository.findAll(
                OrganizationRepository.AddressSpecification.builder()
                    .address("SecondCity")
                    .street("456 Second Street")
                    .build()))
        .isEqualTo(List.of(Samples.ENTITY_TWO));
  }

  private void searchByAddressContainingPostalCodeAndByAddressCity() {
    assertThat(
            repository.findAll(
                OrganizationRepository.AddressSpecification.builder()
                    .address("22222")
                    .city("FirstCity")
                    .build()))
        .isEqualTo(List.of(Samples.ENTITY_THREE));
    // partial match
    assertThat(
            repository.findAll(
                OrganizationRepository.AddressSpecification.builder()
                    .address("222")
                    .city("FirstCity")
                    .build()))
        .isEqualTo(List.of(Samples.ENTITY_THREE));
  }

  private void searchByAddressContainingState() {
    assertThat(
            repository.findAll(
                OrganizationRepository.AddressSpecification.builder()
                    .address("SecondState")
                    .build()))
        .isEqualTo(List.of(Samples.ENTITY_TWO, Samples.ENTITY_THREE));
    // partial match
    assertThat(
            repository.findAll(
                OrganizationRepository.AddressSpecification.builder()
                    .address("econdstate")
                    .build()))
        .isEqualTo(List.of(Samples.ENTITY_TWO, Samples.ENTITY_THREE));
  }

  private void searchByAddressContainingStateAndByAddressPostalCode() {
    assertThat(
            repository.findAll(
                OrganizationRepository.AddressSpecification.builder()
                    .address("FirstState")
                    .postalCode("22222")
                    .build()))
        .isEqualTo(Lists.emptyList());
  }

  private void searchByAddressContainingStreet() {
    assertThat(
            repository.findAll(
                OrganizationRepository.AddressSpecification.builder()
                    .street("123 First Street")
                    .build()))
        .isEqualTo(List.of(Samples.ENTITY_ONE));
    // partial match
    assertThat(
            repository.findAll(
                OrganizationRepository.AddressSpecification.builder().address("3 first").build()))
        .isEqualTo(List.of(Samples.ENTITY_ONE));
  }

  static class Samples {
    private static final OrganizationEntity ENTITY_ONE =
        OrganizationEntity.builder()
            .cdwId("123")
            .npi("npi")
            .name("First")
            .street("123 First Street")
            .city("FirstCity")
            .state("FirstState")
            .postalCode("11111")
            .payload("First Payload")
            .build();

    private static final OrganizationEntity ENTITY_TWO =
        OrganizationEntity.builder()
            .cdwId("456")
            .npi("npi")
            .name("Second")
            .street("456 Second Street")
            .city("SecondCity")
            .state("SecondState")
            .postalCode("22222")
            .payload("Second Payload")
            .build();

    private static final OrganizationEntity ENTITY_THREE =
        OrganizationEntity.builder()
            .cdwId("789")
            .npi("npi")
            .name("Third")
            .street("789 Third Street")
            .city("FirstCity")
            .state("SecondState")
            .postalCode("22222")
            .payload("Third Payload")
            .build();
  }
}
