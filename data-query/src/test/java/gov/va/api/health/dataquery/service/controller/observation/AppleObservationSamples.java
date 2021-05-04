package gov.va.api.health.dataquery.service.controller.observation;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.dataquery.idsmapping.DataQueryIdsCodebookSupplier;
import gov.va.api.health.dataquery.service.config.DataQueryJacksonMapper;
import gov.va.api.health.dataquery.service.config.MagicReferenceConfig;
import gov.va.api.health.dataquery.service.config.ReferenceSerializerProperties;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.observation.R4ObservationTransformer.Mode;
import gov.va.api.health.ids.client.EncodedIdFormat;
import gov.va.api.health.ids.client.EncodingIdentityServiceClient;
import gov.va.api.health.ids.client.EncryptingIdEncoder;
import gov.va.api.health.ids.client.EncryptingIdEncoder.BinaryRepresentations;
import gov.va.api.health.ids.client.EncryptingIdEncoder.EncryptionMechanisms;
import gov.va.api.health.ids.client.EncryptingIdEncoder.UrlSafeEncodings;
import gov.va.api.health.r4.api.resources.Observation;
import gov.va.api.lighthouse.datamart.DatamartReference;
import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class AppleObservationSamples {
  ObjectMapper mapper =
      new DataQueryJacksonMapper(
              new MagicReferenceConfig(
                  "http://api.va.gov/services/fhir/v0",
                  "dstu2",
                  "stu3",
                  "r4",
                  new ReferenceSerializerProperties(true, true, true)))
          .objectMapper();

  private EncodingIdentityServiceClient ids() {
    return EncodingIdentityServiceClient.of(
        List.of(
            EncodedIdFormat.of(
                "I2-",
                EncryptingIdEncoder.builder()
                    .codebook(new DataQueryIdsCodebookSupplier().get())
                    .encoding(UrlSafeEncodings.base32())
                    .encryptionMechanism(EncryptionMechanisms.aes())
                    .textBinaryRepresentation(BinaryRepresentations.utf8())
                    .password("whatever")
                    .build())));
  }

  private DatamartObservation loadDatamart() throws java.io.IOException {
    var dmJson = new File("apple/dm.json");
    var dm = mapper.readValue(dmJson, DatamartObservation.class);
    new WitnessProtection(ids()).registerAndUpdateReferences(List.of(dm), referencesOfResource());
    return dm;
  }

  @SneakyThrows
  @ParameterizedTest
  @ValueSource(ints = {0, 1, 2})
  void r4sample(int ordinal) {

    var mode = Mode.values()[ordinal];

    DatamartObservation dm = loadDatamart();
    R4ObservationTransformer tx =
        R4ObservationTransformer.builder().datamart(dm).mode(mode).build();
    Observation observation = tx.toFhir();

    save(mode, observation, "");
    AtomicInteger i = new AtomicInteger(0);
    tx.externalObservations.forEach(o -> save(mode, o, "-" + i.incrementAndGet()));
  }

  private Function<DatamartObservation, Stream<DatamartReference>> referencesOfResource() {
    return resource ->
        Stream.concat(
            Stream.of(resource.subject().orElse(null), resource.encounter().orElse(null)),
            resource.performer().stream());
  }

  @SneakyThrows
  private void save(Mode mode, Observation observation, Object suffix) {
    mapper
        .writerWithDefaultPrettyPrinter()
        .writeValue(
            new File(
                "apple/sample-r4-"
                    + mode.toString().toLowerCase(Locale.ENGLISH).replace('_', '-')
                    + suffix
                    + ".json"),
            observation);
  }
}
