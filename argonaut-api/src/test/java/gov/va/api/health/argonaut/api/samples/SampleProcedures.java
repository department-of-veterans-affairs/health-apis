package gov.va.api.health.argonaut.api.samples;

import static java.util.Collections.singletonList;

import gov.va.api.health.argonaut.api.resources.Procedure;
import gov.va.api.health.argonaut.api.resources.Procedure.FocalDevice;
import gov.va.api.health.argonaut.api.resources.Procedure.Status;
import java.util.Arrays;
import java.util.Collections;
import lombok.NoArgsConstructor;
import lombok.experimental.Delegate;

@NoArgsConstructor(staticName = "get")
public class SampleProcedures {

  @Delegate SampleDataTypes dataTypes = SampleDataTypes.get();

  private FocalDevice focalDevice() {
    return Procedure.FocalDevice.builder()
        .id("2468")
        .extension(Arrays.asList(extension(), extension()))
        .modifierExtension(Arrays.asList(extension(), extension()))
        .action(codeableConcept())
        .manipulated(reference())
        .build();
  }

  Procedure.Performer performer() {
    return Procedure.Performer.builder()
        .id("9876")
        .extension(Arrays.asList(extension(), extension()))
        .modifierExtension(Arrays.asList(extension(), extension()))
        .actor(reference())
        .role(codeableConcept())
        .build();
  }

  public Procedure procedure() {
    return Procedure.builder()
        .id("1234")
        .resourceType("Procedure")
        .meta(meta())
        .implicitRules("http://HelloRules.com")
        .language("Hello Language")
        .text(narrative())
        .contained(Collections.singletonList(resource()))
        .extension(Arrays.asList(extension(), extension()))
        .modifierExtension(
            Arrays.asList(extension(), extensionWithQuantity(), extensionWithRatio()))
        .identifier(singletonList(identifier()))
        .subject(reference())
        .status(Status.in_progress)
        .category(codeableConcept())
        .code(codeableConcept())
        .notPerformed(false)
        .reasonNotPerformed(Arrays.asList(codeableConcept(), codeableConcept()))
        .bodySite(Arrays.asList(codeableConcept(), codeableConcept()))
        .reasonCodeableConcept(codeableConcept())
        .performer(Arrays.asList(performer(), performer()))
        .performedDateTime("2000-01-01T00:00:00-00:00")
        .encounter(reference())
        .location(reference())
        .outcome(codeableConcept())
        .complication(Arrays.asList(codeableConcept(), codeableConcept()))
        .followUp(Arrays.asList(codeableConcept(), codeableConcept()))
        .request(reference())
        .notes(Arrays.asList(annotation(), annotation()))
        .focalDevice(Collections.singletonList(focalDevice()))
        .build();
  }
}
