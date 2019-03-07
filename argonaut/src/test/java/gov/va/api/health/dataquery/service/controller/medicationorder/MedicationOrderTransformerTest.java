package gov.va.api.health.dataquery.service.controller.medicationorder;

import static gov.va.api.health.dataquery.service.controller.Transformers.asDateTimeString;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.api.DataAbsentReason;
import gov.va.api.health.dataquery.api.DataAbsentReason.Reason;
import gov.va.api.health.dataquery.api.datatypes.CodeableConcept;
import gov.va.api.health.dataquery.api.datatypes.Coding;
import gov.va.api.health.dataquery.api.datatypes.Duration;
import gov.va.api.health.dataquery.api.datatypes.SimpleQuantity;
import gov.va.api.health.dataquery.api.datatypes.Timing;
import gov.va.api.health.dataquery.api.elements.Reference;
import gov.va.api.health.dataquery.api.resources.MedicationOrder;
import gov.va.api.health.dataquery.api.resources.MedicationOrder.DispenseRequest;
import gov.va.api.health.dataquery.api.resources.MedicationOrder.DosageInstruction;
import gov.va.api.health.dataquery.api.resources.MedicationOrder.Status;
import gov.va.dvp.cdw.xsd.model.CdwCodeableConcept;
import gov.va.dvp.cdw.xsd.model.CdwCoding;
import gov.va.dvp.cdw.xsd.model.CdwDuration;
import gov.va.dvp.cdw.xsd.model.CdwMedicationOrder103Root;
import gov.va.dvp.cdw.xsd.model.CdwMedicationOrder103Root.CdwMedicationOrders.CdwMedicationOrder.CdwDispenseRequest;
import gov.va.dvp.cdw.xsd.model.CdwMedicationOrder103Root.CdwMedicationOrders.CdwMedicationOrder.CdwDosageInstructions;
import gov.va.dvp.cdw.xsd.model.CdwMedicationOrder103Root.CdwMedicationOrders.CdwMedicationOrder.CdwDosageInstructions.CdwDosageInstruction;
import gov.va.dvp.cdw.xsd.model.CdwMedicationOrder103Root.CdwMedicationOrders.CdwMedicationOrder.CdwDosageInstructions.CdwDosageInstruction.CdwRoute;
import gov.va.dvp.cdw.xsd.model.CdwMedicationOrder103Root.CdwMedicationOrders.CdwMedicationOrder.CdwDosageInstructions.CdwDosageInstruction.CdwTiming;
import gov.va.dvp.cdw.xsd.model.CdwReference;
import gov.va.dvp.cdw.xsd.model.CdwSimpleQuantity;
import java.util.List;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.junit.Test;

public class MedicationOrderTransformerTest {
  MedicationOrderTransformer tx = new MedicationOrderTransformer();

  private CdwSampleData cdw = new CdwSampleData();

  private Expected expected = new Expected();

  @Test
  public void additionalInstructions() {
    assertThat(tx.additionalInstructions(cdw.additionalInstructions()))
        .isEqualTo(expected.additionalInstructions());
    assertThat(tx.additionalInstructions(null)).isNull();
    assertThat(tx.additionalInstructions(new CdwCodeableConcept()));
  }

  @Test
  public void additionalInstructionsCodings() {
    assertThat(tx.additionalInstructionsCodings(cdw.additionalInstructionsCodings()))
        .isEqualTo(expected.additionalInstructionsCodings());
    assertThat(tx.additionalInstructionsCodings(null)).isNull();
    assertThat(tx.additionalInstructionsCodings(emptyList())).isNull();
    assertThat(tx.additionalInstructionsCodings(singletonList(new CdwCoding()))).isNull();
  }

  @Test
  public void dateTimeString() {
    assertThat(asDateTimeString(cdw.dateWritten()))
        .isEqualTo(expected.medicationOrder().dateWritten());
    assertThat(asDateTimeString(null)).isNull();
  }

  @Test
  public void dispenseRequest() {
    assertThat(tx.dispenseRequest(cdw.dispenseRequest())).isEqualTo(expected.dispenseRequest());
    assertThat(tx.dispenseRequest(null)).isNull();
    assertThat(tx.dispenseRequest(new CdwDispenseRequest())).isNull();
  }

  @Test
  public void dosageInstruction() {
    assertThat(tx.dosageInstruction(null)).isNull();
    assertThat(tx.dosageInstruction(new CdwDosageInstruction())).isNull();
    assertThat(tx.dosageInstruction(cdw.dosageInstruction()))
        .isEqualTo(expected.dosageInstruction());
  }

  @Test
  public void dosageInstructions() {
    assertThat(tx.dosageInstructions(null)).isNull();
    assertThat(tx.dosageInstructions(new CdwDosageInstructions())).isNull();
    assertThat(tx.dosageInstructions(cdw.dosageInstructions()))
        .isEqualTo(expected.dosageInstructions());
  }

  @Test
  public void doseQuantity() {
    assertThat(tx.doseQuantity(null)).isNull();
    assertThat(tx.doseQuantity(new CdwSimpleQuantity())).isNull();
    assertThat(tx.doseQuantity(cdw.doseQuantity())).isEqualTo(expected.doseQuantity());
  }

  @Test
  public void doseQuantityValue() {
    assertThat(tx.doseQuantityValue(null)).isNull();
    assertThat(tx.doseQuantityValue(new CdwSimpleQuantity().getValue())).isNull();
    assertThat(tx.doseQuantityValue(cdw.doseQuantity().getValue()))
        .isEqualTo(expected.doseQuantity().value());
  }

  @Test(expected = IllegalArgumentException.class)
  public void doseQuantityValueCannotTransformStringToDouble() {
    tx.doseQuantityValue("ten");
  }

  @Test
  public void expectedSupplyDuration() {
    assertThat(tx.expectedSupplyDuration(cdw.expectedSupplyDuration()))
        .isEqualTo(expected.expectedSupplyDuration());
    assertThat(tx.expectedSupplyDuration(null)).isNull();
  }

  @Test
  public void medicationOrder() {
    assertThat(tx.apply(cdw.medicationOrder())).isEqualTo(expected.medicationOrder());
    assertThat(tx.apply(cdw.medicationOrderNullPrescriber()))
        .isEqualTo(expected.medicationOrderNullPrescriber());
  }

  @Test
  public void numberOfRepeatsAllowed() {
    assertThat(tx.numberOfRepeatsAllowed(null)).isNull();
    assertThat(tx.numberOfRepeatsAllowed(0)).isNull();
  }

  @Test
  public void prescriber() {
    assertThat(tx.prescriber(cdw.prescriber())).isEqualTo(expected.prescriber());
    assertThat(tx.prescriber(null)).isNull();
    assertThat(tx.prescriber(new CdwReference())).isNull();
    // _prescriber field
    assertThat(tx.prescriberExtension(null)).isEqualTo(DataAbsentReason.of(Reason.unknown));
    assertThat(tx.prescriberExtension(cdw.prescriber())).isNull();
  }

  @Test
  public void quantity() {
    assertThat(tx.quantity(cdw.dispenseRequest().getQuantity())).isEqualTo(expected.quantity());
    assertThat(tx.quantity(null)).isNull();
    assertThat(tx.quantity("")).isNull();
  }

  @Test(expected = IllegalArgumentException.class)
  public void quantityCannotTransformStringToDouble() {
    tx.quantity("ten");
  }

  @Test
  public void reference() {
    assertThat(tx.reference(null)).isNull();
    assertThat(tx.reference(new CdwReference())).isNull();
    assertThat(
            tx.reference(
                cdw.cdwReference(
                    "https://www.freedomstream.io/CDCArgonaut/api/Patient/185601V825290",
                    "VETERAN,JOHN Q")))
        .isEqualTo(
            expected.reference(
                "https://www.freedomstream.io/CDCArgonaut/api/Patient/185601V825290",
                "VETERAN,JOHN Q"));
  }

  @Test
  public void route() {
    assertThat(tx.route(null)).isNull();
    assertThat(tx.route(new CdwRoute())).isNull();
    assertThat(tx.route(cdw.route())).isEqualTo(expected.route());
  }

  @Test
  public void status() {
    assertThat(tx.status(cdw.medicationOrder().getStatus()))
        .isEqualTo(expected.medicationOrder().status());
    assertThat(tx.status(null)).isNull();
    assertThat(tx.status("")).isNull();
  }

  @Test
  public void timeCodeCoding() {
    assertThat(tx.timeCodeCodings(cdw.timingCode().getCoding()))
        .isEqualTo(expected.timingCode().coding());
    assertThat(tx.timeCodeCodings(null)).isNull();
    assertThat(tx.timeCodeCodings(emptyList())).isNull();
    assertThat(tx.timeCodeCodings(singletonList(new CdwCoding()))).isNull();
  }

  @Test
  public void timing() {
    assertThat(tx.timing(cdw.timing())).isEqualTo(expected.timing());
    assertThat(tx.timing(null)).isNull();
    assertThat(tx.timing(new CdwTiming())).isNull();
  }

  @Test
  public void timingCode() {
    assertThat(tx.timingCode(cdw.timingCode())).isEqualTo(expected.timingCode());
    assertThat(tx.timingCode(new CdwCodeableConcept())).isNull();
    assertThat(tx.timingCode(null)).isNull();
  }

  private static class CdwSampleData {
    private DatatypeFactory datatypeFactory;

    @SneakyThrows
    private CdwSampleData() {
      datatypeFactory = DatatypeFactory.newInstance();
    }

    CdwCodeableConcept additionalInstructions() {
      CdwCodeableConcept additionalInstructions = new CdwCodeableConcept();
      additionalInstructions.setText("additional instructions text");
      return additionalInstructions;
    }

    List<CdwCoding> additionalInstructionsCodings() {
      CdwCoding coding = new CdwCoding();
      coding.setSystem("http://example.com");
      coding.setDisplay("Additional Instructions display");
      coding.setCode("Additional Instructions code");
      return singletonList(coding);
    }

    private CdwReference cdwReference(String reference, String display) {
      CdwReference ref = new CdwReference();
      ref.setReference(reference);
      ref.setDisplay(display);
      return ref;
    }

    @SneakyThrows
    XMLGregorianCalendar dateEnded() {
      XMLGregorianCalendar dateEnded = datatypeFactory.newXMLGregorianCalendar();
      dateEnded.setYear(2018);
      dateEnded.setMonth(11);
      dateEnded.setDay(6);
      return dateEnded;
    }

    @SneakyThrows
    private XMLGregorianCalendar dateWritten() {
      XMLGregorianCalendar dateWritten = datatypeFactory.newXMLGregorianCalendar();
      dateWritten.setYear(2018);
      dateWritten.setMonth(11);
      dateWritten.setDay(6);
      return dateWritten;
    }

    CdwDispenseRequest dispenseRequest() {
      CdwDispenseRequest dispenseRequest = new CdwDispenseRequest();
      dispenseRequest.setExpectedSupplyDuration(expectedSupplyDuration());
      dispenseRequest.setNumberOfRepeatsAllowed(10);
      dispenseRequest.setQuantity("10");
      return dispenseRequest;
    }

    CdwDosageInstruction dosageInstruction() {
      CdwDosageInstruction dosageInstruction = new CdwDosageInstruction();
      dosageInstruction.setText(" TAKE ONE TABLET BY MOUTH ONE TIME EACH DAY FOR CHOLESTEROL");
      dosageInstruction.setAdditionalInstructions(additionalInstructions());
      dosageInstruction.setTiming(timing());
      dosageInstruction.setAsNeededBoolean("true");
      dosageInstruction.setRoute(route());
      dosageInstruction.setDoseQuantity(doseQuantity());
      return dosageInstruction;
    }

    CdwDosageInstructions dosageInstructions() {
      CdwDosageInstructions dosageInstructions = new CdwDosageInstructions();
      dosageInstructions.getDosageInstruction().add(dosageInstruction());
      return dosageInstructions;
    }

    CdwSimpleQuantity doseQuantity() {
      CdwSimpleQuantity doseQuantity = new CdwSimpleQuantity();
      doseQuantity.setValue("10");
      doseQuantity.setUnit("Dose quantity unit");
      doseQuantity.setSystem("http://example.com");
      doseQuantity.setCode("Dose quantity code");
      return doseQuantity;
    }

    CdwDuration expectedSupplyDuration() {
      CdwDuration expectedSupplyDuration = new CdwDuration();
      expectedSupplyDuration.setCode("d");
      expectedSupplyDuration.setSystem("http://unitsofmeasure");
      expectedSupplyDuration.setUnit("days");
      expectedSupplyDuration.setValue(30);
      return expectedSupplyDuration;
    }

    CdwMedicationOrder103Root.CdwMedicationOrders.CdwMedicationOrder medicationOrder() {
      CdwMedicationOrder103Root.CdwMedicationOrders.CdwMedicationOrder sampleMedicationOrder =
          new CdwMedicationOrder103Root.CdwMedicationOrders.CdwMedicationOrder();
      sampleMedicationOrder.setRowNumber(1);
      sampleMedicationOrder.setCdwId("1234");
      sampleMedicationOrder.setPatient(
          cdwReference(
              "https://www.freedomstream.io/CDCArgonaut/api/Patient/185601V825290",
              "VETERAN,JOHN Q"));
      sampleMedicationOrder.setDateWritten(dateWritten());
      sampleMedicationOrder.setStatus("active");
      sampleMedicationOrder.setDateEnded(dateEnded());
      sampleMedicationOrder.setPrescriber(prescriber());
      sampleMedicationOrder.setMedicationReference(
          cdwReference(
              "https://www.freedomstream.io/CDCArgonaut/api/Medication/2f773f73-ad7f-56ca-891e-8e364c913fe0",
              "ATORVASTATIN CALCIUM 80MG TAB"));
      sampleMedicationOrder.setDosageInstructions(dosageInstructions());
      sampleMedicationOrder.setDispenseRequest(dispenseRequest());
      return sampleMedicationOrder;
    }

    CdwMedicationOrder103Root.CdwMedicationOrders.CdwMedicationOrder
        medicationOrderNullPrescriber() {
      CdwMedicationOrder103Root.CdwMedicationOrders.CdwMedicationOrder sampleMedicationOrder =
          new CdwMedicationOrder103Root.CdwMedicationOrders.CdwMedicationOrder();
      sampleMedicationOrder.setRowNumber(1);
      sampleMedicationOrder.setCdwId("1234");
      sampleMedicationOrder.setPatient(
          cdwReference(
              "https://www.freedomstream.io/CDCArgonaut/api/Patient/185601V825290",
              "VETERAN,JOHN Q"));
      sampleMedicationOrder.setDateWritten(dateWritten());
      sampleMedicationOrder.setStatus("active");
      sampleMedicationOrder.setDateEnded(dateEnded());
      sampleMedicationOrder.setPrescriber(null);
      sampleMedicationOrder.setMedicationReference(
          cdwReference(
              "https://www.freedomstream.io/CDCArgonaut/api/Medication/2f773f73-ad7f-56ca-891e-8e364c913fe0",
              "ATORVASTATIN CALCIUM 80MG TAB"));
      sampleMedicationOrder.setDosageInstructions(dosageInstructions());
      sampleMedicationOrder.setDispenseRequest(dispenseRequest());
      return sampleMedicationOrder;
    }

    CdwReference prescriber() {
      CdwReference prescriber = new CdwReference();
      prescriber.setDisplay("SMITH,ATTENDING D");
      prescriber.setReference(
          "https://www.freedomstream.io/CDCArgonaut/api/Practitioner/93e4e3c3-8d8c-5b53-996f-6047d0232231");
      return prescriber;
    }

    CdwRoute route() {
      CdwRoute route = new CdwRoute();
      route.setText("ORAL");
      return route;
    }

    CdwTiming timing() {
      CdwTiming timing = new CdwTiming();
      timing.setCode(timingCode());
      return timing;
    }

    CdwCodeableConcept timingCode() {
      CdwCodeableConcept timingCode = new CdwCodeableConcept();
      timingCode.setText("QDAILY");
      timingCode.getCoding().add(timingCodeCoding());
      return timingCode;
    }

    CdwCoding timingCodeCoding() {
      CdwCoding coding = new CdwCoding();
      coding.setSystem("http://example.com");
      coding.setDisplay("Time display");
      coding.setCode("Time code");
      return coding;
    }
  }

  @NoArgsConstructor(staticName = "get")
  private static class Expected {
    CodeableConcept additionalInstructions() {
      return CodeableConcept.builder().text("additional instructions text").build();
    }

    private List<Coding> additionalInstructionsCodings() {
      return singletonList(
          Coding.builder()
              .code("Additional Instructions code")
              .display("Additional Instructions display")
              .system("http://example.com")
              .build());
    }

    DispenseRequest dispenseRequest() {
      return DispenseRequest.builder()
          .expectedSupplyDuration(expectedSupplyDuration())
          .numberOfRepeatsAllowed(10)
          .quantity(quantity())
          .build();
    }

    private DosageInstruction dosageInstruction() {
      return DosageInstruction.builder()
          .text(" TAKE ONE TABLET BY MOUTH ONE TIME EACH DAY FOR CHOLESTEROL")
          .additionalInstructions(additionalInstructions())
          .timing(timing())
          .asNeededBoolean(true)
          .route(route())
          .doseQuantity(doseQuantity())
          .build();
    }

    private List<DosageInstruction> dosageInstructions() {
      return singletonList(dosageInstruction());
    }

    private SimpleQuantity doseQuantity() {
      return SimpleQuantity.builder()
          .value(Double.valueOf(10))
          .system("http://example.com")
          .unit("Dose quantity unit")
          .code("Dose quantity code")
          .build();
    }

    Duration expectedSupplyDuration() {
      return Duration.builder()
          .code("d")
          .system("http://unitsofmeasure")
          .unit("days")
          .value(Double.valueOf(30))
          .build();
    }

    MedicationOrder medicationOrder() {
      return MedicationOrder.builder()
          .resourceType("MedicationOrder")
          .id("1234")
          .patient(
              reference(
                  "https://www.freedomstream.io/CDCArgonaut/api/Patient/185601V825290",
                  "VETERAN,JOHN Q"))
          .dateWritten("2018-11-06")
          .status(Status.active)
          .dateEnded("2018-11-06")
          .prescriber(prescriber())
          ._prescriber(null)
          .medicationReference(
              reference(
                  "https://www.freedomstream.io/CDCArgonaut/api/Medication/2f773f73-ad7f-56ca-891e-8e364c913fe0",
                  "ATORVASTATIN CALCIUM 80MG TAB"))
          .dosageInstruction(dosageInstructions())
          .dispenseRequest(dispenseRequest())
          .build();
    }

    MedicationOrder medicationOrderNullPrescriber() {
      return MedicationOrder.builder()
          .resourceType("MedicationOrder")
          .id("1234")
          .patient(
              reference(
                  "https://www.freedomstream.io/CDCArgonaut/api/Patient/185601V825290",
                  "VETERAN,JOHN Q"))
          .dateWritten("2018-11-06")
          .status(Status.active)
          .dateEnded("2018-11-06")
          ._prescriber(DataAbsentReason.of(Reason.unknown))
          .medicationReference(
              reference(
                  "https://www.freedomstream.io/CDCArgonaut/api/Medication/2f773f73-ad7f-56ca-891e-8e364c913fe0",
                  "ATORVASTATIN CALCIUM 80MG TAB"))
          .dosageInstruction(dosageInstructions())
          .dispenseRequest(dispenseRequest())
          .build();
    }

    Reference prescriber() {
      return Reference.builder()
          .reference(
              "https://www.freedomstream.io/CDCArgonaut/api/Practitioner/93e4e3c3-8d8c-5b53-996f-6047d0232231")
          .display("SMITH,ATTENDING D")
          .build();
    }

    SimpleQuantity quantity() {
      return SimpleQuantity.builder().value(Double.valueOf("10")).build();
    }

    private Reference reference(String reference, String display) {
      return Reference.builder().reference(reference).display(display).build();
    }

    private CodeableConcept route() {
      return CodeableConcept.builder().text("ORAL").build();
    }

    private Timing timing() {
      return Timing.builder().code(timingCode()).build();
    }

    private CodeableConcept timingCode() {
      return CodeableConcept.builder().text("QDAILY").coding(timingCodeCoding()).build();
    }

    private List<Coding> timingCodeCoding() {
      return singletonList(
          Coding.builder()
              .code("Time code")
              .display("Time display")
              .system("http://example.com")
              .build());
    }
  }
}
