package gov.va.api.health.dataquery.service.controller.location;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;

@DataJpaTest
public class LocationRepositoryTest {
  @Autowired LocationRepository repository;

  private void emptyPredicatesWillThrowInvalidDataAccessApiUsageException() {
    assertThrows(
        InvalidDataAccessApiUsageException.class,
        () -> repository.findAll(LocationRepository.AddressSpecification.builder().build()));
  }

  private void initializeData() {
    repository.save(Samples.ENTITY_ONE);
    repository.save(Samples.ENTITY_TWO);
    repository.save(Samples.ENTITY_THREE);
  }

  @Test
  void locationSpecifications() {
    initializeData();
    searchByAddressContainingState();
    searchByAddressContainingStreet();
    searchByAddressContainingPostalCodeAndByAddressCity();
    searchByAddressContainingStateAndByAddressPostalCode();
    searchByAddressContainingCityAndByStreet();
    searchByAddressContainingCityAndByAddressStateAndByAddressPostalCode();
    emptyPredicatesWillThrowInvalidDataAccessApiUsageException();
  }

  private void searchByAddressContainingCityAndByAddressStateAndByAddressPostalCode() {
    assertThat(
            repository.findAll(
                LocationRepository.AddressSpecification.builder()
                    .address("SecondCity")
                    .state("SecondState")
                    .postalCode("22222")
                    .build()))
        .isEqualTo(List.of(Samples.ENTITY_TWO));
  }

  private void searchByAddressContainingCityAndByStreet() {
    assertThat(
            repository.findAll(
                LocationRepository.AddressSpecification.builder()
                    .address("SecondCity")
                    .street("456 Second Street")
                    .build()))
        .isEqualTo(List.of(Samples.ENTITY_TWO));
  }

  private void searchByAddressContainingPostalCodeAndByAddressCity() {
    assertThat(
            repository.findAll(
                LocationRepository.AddressSpecification.builder()
                    .address("22222")
                    .city("FirstCity")
                    .build()))
        .isEqualTo(List.of(Samples.ENTITY_THREE));
    // partial match
    assertThat(
            repository.findAll(
                LocationRepository.AddressSpecification.builder()
                    .address("222")
                    .city("FirstCity")
                    .build()))
        .isEqualTo(List.of(Samples.ENTITY_THREE));
  }

  private void searchByAddressContainingState() {
    assertThat(
            repository.findAll(
                LocationRepository.AddressSpecification.builder().address("SecondState").build()))
        .isEqualTo(List.of(Samples.ENTITY_TWO, Samples.ENTITY_THREE));
    // partial match
    assertThat(
            repository.findAll(
                LocationRepository.AddressSpecification.builder().address("econdstat").build()))
        .isEqualTo(List.of(Samples.ENTITY_TWO, Samples.ENTITY_THREE));
  }

  private void searchByAddressContainingStateAndByAddressPostalCode() {
    assertThat(
            repository.findAll(
                LocationRepository.AddressSpecification.builder()
                    .address("FirstState")
                    .postalCode("22222")
                    .build()))
        .isEqualTo(Lists.emptyList());
  }

  private void searchByAddressContainingStreet() {
    assertThat(
            repository.findAll(
                LocationRepository.AddressSpecification.builder()
                    .street("123 First Street")
                    .build()))
        .isEqualTo(List.of(Samples.ENTITY_ONE));
    // partial match
    assertThat(
            repository.findAll(
                LocationRepository.AddressSpecification.builder().address("3 first").build()))
        .isEqualTo(List.of(Samples.ENTITY_ONE));
  }

  static class Samples {
    private static final LocationEntity ENTITY_ONE =
        LocationEntity.builder()
            .cdwId("123")
            .name("First")
            .street("123 First Street")
            .city("FirstCity")
            .state("FirstState")
            .postalCode("11111")
            .payload("First Payload")
            .build();

    private static final LocationEntity ENTITY_TWO =
        LocationEntity.builder()
            .cdwId("456")
            .name("Second")
            .street("456 Second Street")
            .city("SecondCity")
            .state("SecondState")
            .postalCode("22222")
            .payload("Second Payload")
            .build();

    private static final LocationEntity ENTITY_THREE =
        LocationEntity.builder()
            .cdwId("789")
            .name("Third")
            .street("789 Third Street")
            .city("FirstCity")
            .state("SecondState")
            .postalCode("22222")
            .payload("Third Payload")
            .build();
  }
}
