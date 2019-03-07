package gov.va.api.health.dataquery.service.controller.observation;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.api.datatypes.CodeableConcept;
import gov.va.api.health.dataquery.api.datatypes.Coding;
import gov.va.api.health.dataquery.api.datatypes.Quantity;
import gov.va.api.health.dataquery.api.datatypes.SimpleQuantity;
import gov.va.api.health.dataquery.api.elements.Reference;
import gov.va.api.health.dataquery.api.resources.Observation;
import gov.va.api.health.dataquery.api.resources.Observation.ObservationComponent;
import gov.va.api.health.dataquery.api.resources.Observation.ObservationReferenceRange;
import gov.va.api.health.dataquery.api.resources.Observation.Status;
import gov.va.dvp.cdw.xsd.model.CdwObservation104Root.CdwObservations.CdwObservation;
import gov.va.dvp.cdw.xsd.model.CdwObservation104Root.CdwObservations.CdwObservation.CdwCategory;
import gov.va.dvp.cdw.xsd.model.CdwObservation104Root.CdwObservations.CdwObservation.CdwCategory.CdwCoding;
import gov.va.dvp.cdw.xsd.model.CdwObservation104Root.CdwObservations.CdwObservation.CdwCode;
import gov.va.dvp.cdw.xsd.model.CdwObservation104Root.CdwObservations.CdwObservation.CdwComponents;
import gov.va.dvp.cdw.xsd.model.CdwObservation104Root.CdwObservations.CdwObservation.CdwComponents.CdwComponent;
import gov.va.dvp.cdw.xsd.model.CdwObservation104Root.CdwObservations.CdwObservation.CdwComponents.CdwComponent.CdwValueCodeableConcept;
import gov.va.dvp.cdw.xsd.model.CdwObservation104Root.CdwObservations.CdwObservation.CdwInterpretation;
import gov.va.dvp.cdw.xsd.model.CdwObservation104Root.CdwObservations.CdwObservation.CdwPerformers;
import gov.va.dvp.cdw.xsd.model.CdwObservation104Root.CdwObservations.CdwObservation.CdwReferenceRanges;
import gov.va.dvp.cdw.xsd.model.CdwObservation104Root.CdwObservations.CdwObservation.CdwReferenceRanges.CdwReferenceRange;
import gov.va.dvp.cdw.xsd.model.CdwObservation104Root.CdwObservations.CdwObservation.CdwValueQuantity;
import gov.va.dvp.cdw.xsd.model.CdwObservationCategoryCode;
import gov.va.dvp.cdw.xsd.model.CdwObservationCategoryDisplay;
import gov.va.dvp.cdw.xsd.model.CdwObservationCategorySystem;
import gov.va.dvp.cdw.xsd.model.CdwObservationInterpretationSystem;
import gov.va.dvp.cdw.xsd.model.CdwObservationRefRangeQuantity;
import gov.va.dvp.cdw.xsd.model.CdwObservationStatus;
import gov.va.dvp.cdw.xsd.model.CdwReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.junit.Test;

public class ObservationTransformerTest {
  private final ObservationTransformer tx = new ObservationTransformer();

  private final CdwSampleData cdw = CdwSampleData.get();

  private final Expected expected = Expected.get();

  @Test
  public void category() {
    assertThat(tx.category(null)).isNull();
    assertThat(tx.category(new CdwCategory())).isNull();
    assertThat(tx.category(cdw.category())).isEqualTo(expected.category());
  }

  @Test
  public void categoryCodings() {
    assertThat(tx.categoryCodings(cdw.category().getCoding()))
        .isEqualTo(expected.category().coding());
    assertThat(tx.categoryCodings(null)).isNull();
    assertThat(tx.categoryCodings(emptyList())).isNull();
    assertThat(tx.categoryCodings(singletonList(null))).isNull();
  }

  @Test
  public void code() {
    assertThat(tx.code(null)).isNull();
    assertThat(tx.code(new CdwCode())).isNull();
    assertThat(tx.code(cdw.code())).isEqualTo(expected.code());
  }

  @Test
  public void codeCoding() {
    assertThat(tx.codeCodings(cdw.code().getCoding())).isEqualTo(expected.code().coding());
    assertThat(tx.codeCodings(null)).isNull();
    assertThat(tx.codeCodings(emptyList())).isNull();
    assertThat(tx.codeCodings(singletonList(null))).isNull();
  }

  @Test
  public void component() {
    assertThat(tx.component(null)).isNull();
    assertThat(tx.component(new CdwComponent())).isNull();
    assertThat(tx.component(cdw.componentWithCodeableConcept()))
        .isEqualTo(expected.componentWithCodeableConcept());
    assertThat(tx.component(cdw.componentWithQuantity()))
        .isEqualTo(expected.componentWithQuantity());
  }

  @Test
  public void componentCode() {
    assertThat(tx.componentCode(null)).isNull();
    assertThat(tx.componentCode(new CdwComponent.CdwCode())).isNull();
    assertThat(tx.componentCode(cdw.componentCode())).isEqualTo(expected.componentCode());
  }

  @Test
  public void componentCoding() {
    assertThat(tx.componentCodings(cdw.componentCode().getCoding()))
        .isEqualTo(expected.componentCode().coding());
    assertThat(tx.componentCodings(null)).isNull();
    assertThat(tx.componentCodings(emptyList())).isNull();
    assertThat(tx.componentCodings(singletonList(null))).isNull();
  }

  @Test
  public void componentValueCodeableConcept() {
    assertThat(tx.componentValueCodeableConcept(null)).isNull();
    assertThat(tx.componentValueCodeableConcept(new CdwValueCodeableConcept())).isNull();
    assertThat(tx.componentValueCodeableConcept(cdw.componentCodeableConcept()))
        .isEqualTo(expected.componentCodeableConcept());
  }

  @Test
  public void componentValueCoding() {
    assertThat(tx.componentValueCodings(cdw.componentCodeableConcept().getCoding()))
        .isEqualTo(expected.componentCodeableConcept().coding());
    assertThat(tx.componentValueCodings(null)).isNull();
    assertThat(tx.componentValueCodings(emptyList())).isNull();
    assertThat(tx.componentValueCodings(singletonList(null))).isNull();
  }

  @Test
  public void componentValueQuantity() {
    assertThat(tx.componentValueQuantity(null)).isNull();
    assertThat(tx.componentValueQuantity(new CdwComponent.CdwValueQuantity())).isNull();
    assertThat(tx.componentValueQuantity(cdw.componentQuantity()))
        .isEqualTo(expected.componentQuantity());
  }

  @Test
  public void components() {
    assertThat(tx.components(null)).isNull();
    assertThat(tx.components(new CdwComponents())).isNull();
    assertThat(tx.components(cdw.components())).isEqualTo(expected.components());
  }

  @Test
  public void interpretation() {
    assertThat(tx.interpretation(null)).isNull();
    assertThat(tx.interpretation(new CdwInterpretation())).isNull();
    assertThat(tx.interpretation(cdw.interpretation())).isEqualTo(expected.interpretation());
  }

  @Test
  public void interpretationCoding() {
    assertThat(tx.interpretationCodings(cdw.interpretation().getCoding()))
        .isEqualTo(expected.interpretation().coding());
    assertThat(tx.interpretationCodings(null)).isNull();
    assertThat(tx.interpretationCodings(emptyList())).isNull();
    assertThat(tx.interpretationCodings(singletonList(null))).isNull();
  }

  @Test
  public void observation() {
    assertThat(tx.apply(cdw.observation())).isEqualTo(expected.observation());
    assertThat(tx.apply(cdw.observationWithValueCodeableConcept()))
        .isEqualTo(expected.observationWithValueCodeableConcept());
  }

  @Test
  public void performers() {
    assertThat(tx.performers(null)).isNull();
    assertThat(tx.performers(new CdwPerformers())).isNull();
    assertThat(tx.performers(cdw.performers())).isEqualTo(expected.performers());
  }

  @Test
  public void reference() {
    assertThat(tx.reference(null)).isNull();
    assertThat(tx.reference(cdw.reference("x", "y"))).isEqualTo(expected.reference("x", "y"));
  }

  @Test
  public void referenceRange() {
    assertThat(tx.referenceRange(null)).isNull();
    assertThat(tx.referenceRange(new CdwReferenceRange())).isNull();
    assertThat(tx.referenceRange(cdw.referenceRange())).isEqualTo(expected.referenceRange());
  }

  @Test
  public void referenceRangeQuantity() {
    assertThat(tx.referenceRangeQuantity(null)).isNull();
    assertThat(tx.referenceRangeQuantity(new CdwObservationRefRangeQuantity())).isNull();
    assertThat(tx.referenceRangeQuantity(cdw.referenceRangeQuantity(1)))
        .isEqualTo(expected.referenceRangeQuantity(1));
    assertThat(tx.referenceRangeQuantity(cdw.emptyReferenceRangeQuantity(1)))
        .isEqualTo(expected.emptyCodeReferenceRangeQuantity(1));
  }

  @Test
  public void referenceRanges() {
    assertThat(tx.referenceRanges(null)).isNull();
    assertThat(tx.referenceRanges(new CdwReferenceRanges())).isNull();
    assertThat(tx.referenceRanges(cdw.referenceRanges())).isEqualTo(expected.referenceRanges());
  }

  @Test
  public void status() {
    assertThat(tx.status(CdwObservationStatus.REGISTERED)).isEqualTo(Status.registered);
    assertThat(tx.status(CdwObservationStatus.PRELIMINARY)).isEqualTo(Status.preliminary);
    assertThat(tx.status(CdwObservationStatus.FINAL)).isEqualTo(Status._final);
    assertThat(tx.status(CdwObservationStatus.AMENDED)).isEqualTo(Status.amended);
    assertThat(tx.status(CdwObservationStatus.CANCELLED)).isEqualTo(Status.cancelled);
    assertThat(tx.status(CdwObservationStatus.ENTERED_IN_ERROR)).isEqualTo(Status.entered_in_error);
    assertThat(tx.status(CdwObservationStatus.UNKNOWN)).isEqualTo(Status.unknown);
  }

  @Test
  public void valueCodeableConcept() {
    assertThat(tx.valueCodeableConcept(null)).isNull();
    assertThat(tx.valueCodeableConcept(new CdwObservation.CdwValueCodeableConcept())).isNull();
    assertThat(tx.valueCodeableConcept(cdw.valueCodeableConcept()))
        .isEqualTo(expected.valueCodeableConcept());
  }

  @Test
  public void valueCoding() {
    assertThat(tx.valueCodings(cdw.valueCodeableConcept().getCoding()))
        .isEqualTo(expected.valueCodeableConcept().coding());
    assertThat(tx.valueCodings(null)).isNull();
    assertThat(tx.valueCodings(emptyList())).isNull();
    assertThat(tx.valueCodings(singletonList(null))).isNull();
  }

  @Test
  public void valueQuantity() {
    assertThat(tx.valueQuantity(null)).isNull();
    assertThat(tx.valueQuantity(new CdwValueQuantity())).isNull();
    assertThat(tx.valueQuantity(cdw.valueQuantity())).isEqualTo(expected.valueQuantity());
    assertThat(tx.valueQuantity(cdw.emptyCodeValueQuantity()))
        .isEqualTo(expected.emptyCodeValueQuantity());
  }

  @NoArgsConstructor(staticName = "get", access = AccessLevel.PUBLIC)
  public static class CdwSampleData {
    private CdwCategory category() {
      CdwCategory cdw = new CdwCategory();
      cdw.getCoding().add(categoryCoding());
      return cdw;
    }

    private CdwCoding categoryCoding() {
      CdwCoding cdw = new CdwCoding();
      cdw.setSystem(CdwObservationCategorySystem.HTTP_HL_7_ORG_FHIR_OBSERVATION_CATEGORY);
      cdw.setCode(CdwObservationCategoryCode.VITAL_SIGNS);
      cdw.setDisplay(CdwObservationCategoryDisplay.VITAL_SIGNS);
      return cdw;
    }

    private CdwCode code() {
      CdwCode cdw = new CdwCode();
      cdw.setText("<3");
      cdw.getCoding().add(coding());
      return cdw;
    }

    private CdwCode.CdwCoding coding() {
      CdwCode.CdwCoding cdw = new CdwCode.CdwCoding();
      cdw.setSystem("http://loinc.org");
      cdw.setCode("8867-4");
      cdw.setDisplay("Heart rate");
      return cdw;
    }

    private CdwComponent.CdwCode componentCode() {
      CdwComponent.CdwCode cdw = new CdwComponent.CdwCode();
      cdw.setText("Systolic blood pressure");
      cdw.getCoding().add(componentCodeCoding());
      return cdw;
    }

    private CdwComponent.CdwCode.CdwCoding componentCodeCoding() {
      CdwComponent.CdwCode.CdwCoding cdw = new CdwComponent.CdwCode.CdwCoding();
      cdw.setCode("8480-6");
      cdw.setDisplay("Systolic blood pressure");
      cdw.setSystem("http://loinc.org");
      return cdw;
    }

    private CdwValueCodeableConcept componentCodeableConcept() {
      CdwValueCodeableConcept cdw = new CdwValueCodeableConcept();
      cdw.setText("component cc");
      cdw.getCoding().add(componentCodeableConceptCoding());
      return cdw;
    }

    private CdwValueCodeableConcept.CdwCoding componentCodeableConceptCoding() {
      CdwValueCodeableConcept.CdwCoding cdw = new CdwValueCodeableConcept.CdwCoding();
      cdw.setCode("cccc1");
      cdw.setSystem("http://example.com");
      cdw.setDisplay("component codeable concept coding");
      return cdw;
    }

    private CdwComponent.CdwValueQuantity componentQuantity() {
      CdwComponent.CdwValueQuantity cdw = new CdwComponent.CdwValueQuantity();
      cdw.setCode("mm[Hg]");
      cdw.setSystem("http://unitsofmeasure.org");
      cdw.setUnit("mm[Hg]");
      cdw.setValue(BigDecimal.valueOf(67));
      cdw.setComparator(">");
      return cdw;
    }

    private CdwComponent componentWithCodeableConcept() {
      CdwComponent cdw = new CdwComponent();
      cdw.setCode(componentCode());
      cdw.setId("component1");
      cdw.setValueCodeableConcept(componentCodeableConcept());
      return cdw;
    }

    private CdwComponent componentWithQuantity() {
      CdwComponent cdw = new CdwComponent();
      cdw.setCode(componentCode());
      cdw.setId("component1");
      cdw.setValueQuantity(componentQuantity());
      return cdw;
    }

    private CdwComponents components() {
      CdwComponents cdw = new CdwComponents();
      cdw.getComponent().add(componentWithQuantity());
      return cdw;
    }

    @SneakyThrows
    private XMLGregorianCalendar dateTime(String timestamp) {
      return DatatypeFactory.newInstance().newXMLGregorianCalendar(timestamp);
    }

    private CdwValueQuantity emptyCodeValueQuantity() {
      CdwValueQuantity cdw = new CdwValueQuantity();
      cdw.setComparator("<");
      cdw.setSystem("http://unitsofmeasure.org");
      cdw.setUnit("/min");
      cdw.setValue(BigDecimal.valueOf(74));
      return cdw;
    }

    private CdwObservationRefRangeQuantity emptyReferenceRangeQuantity(long value) {
      CdwObservationRefRangeQuantity cdw = new CdwObservationRefRangeQuantity();
      cdw.setUnit("k/cmm");
      cdw.setSystem("http://unitsofmeasure.org");
      cdw.setValue(BigDecimal.valueOf(value));
      return cdw;
    }

    private CdwInterpretation interpretation() {
      CdwInterpretation cdw = new CdwInterpretation();
      cdw.setText("L");
      cdw.getCoding().add(interpretationCoding());
      return cdw;
    }

    private CdwInterpretation.CdwCoding interpretationCoding() {
      CdwInterpretation.CdwCoding cdw = new CdwInterpretation.CdwCoding();
      cdw.setSystem(CdwObservationInterpretationSystem.HTTP_HL_7_ORG_FHIR_V_2_0078);
      cdw.setCode("L");
      cdw.setDisplay("Low");
      return cdw;
    }

    public CdwObservation observation() {
      CdwObservation cdw = new CdwObservation();
      cdw.setRowNumber(BigInteger.ONE);
      cdw.setCdwId("1201051417263:V");
      cdw.setStatus(CdwObservationStatus.FINAL);
      cdw.setCategory(category());
      cdw.setCode(code());
      cdw.setSubject(reference("Patient/185601V825290", "VETERAN,JOHN Q"));
      cdw.setEffectiveDateTime(dateTime("2015-04-15T14:16:38Z"));
      cdw.setIssued(dateTime("2015-04-15T14:19:45Z"));
      cdw.setPerformers(performers());
      cdw.setValueQuantity(valueQuantity());
      cdw.setComments("observe, ladies and gentlemen");
      cdw.setComponents(components());
      cdw.setEncounter(reference("Encounter/1234", "The 3rd Kind"));
      cdw.setInterpretation(interpretation());
      cdw.setReferenceRanges(referenceRanges());
      cdw.setSpecimen(reference("Specimen/1200004290", "BLOOD"));
      cdw.setSubject(reference("Patient/185601V825290", "VETERAN,JOHN Q"));
      return cdw;
    }

    CdwObservation observationWithValueCodeableConcept() {
      CdwObservation cdw = observation();
      cdw.setValueCodeableConcept(valueCodeableConcept());
      cdw.setValueQuantity(null);
      return cdw;
    }

    private CdwPerformers performers() {
      CdwPerformers cdw = new CdwPerformers();
      cdw.getPerformer().add(reference("Practitioner/1715142", "SMITH,ATTENDING D"));
      return cdw;
    }

    private CdwReference reference(String ref, String display) {
      CdwReference cdw = new CdwReference();
      cdw.setReference(ref);
      cdw.setDisplay(display);
      return cdw;
    }

    private CdwReferenceRange referenceRange() {
      CdwReferenceRange cdw = new CdwReferenceRange();
      cdw.setHigh(referenceRangeQuantity(10));
      cdw.setLow(referenceRangeQuantity(1));
      return cdw;
    }

    private CdwObservationRefRangeQuantity referenceRangeQuantity(long value) {
      CdwObservationRefRangeQuantity cdw = new CdwObservationRefRangeQuantity();
      cdw.setCode("k/cmm");
      cdw.setUnit("k/cmm");
      cdw.setSystem("http://unitsofmeasure.org");
      cdw.setValue(BigDecimal.valueOf(value));
      return cdw;
    }

    private CdwReferenceRanges referenceRanges() {
      CdwReferenceRanges cdw = new CdwReferenceRanges();
      cdw.getReferenceRange().add(referenceRange());
      return cdw;
    }

    private CdwObservation.CdwValueCodeableConcept valueCodeableConcept() {
      CdwObservation.CdwValueCodeableConcept cdw = new CdwObservation.CdwValueCodeableConcept();
      cdw.setText("value cc");
      cdw.getCoding().add(valueCodeableConceptCoding());
      return cdw;
    }

    private CdwObservation.CdwValueCodeableConcept.CdwCoding valueCodeableConceptCoding() {
      CdwObservation.CdwValueCodeableConcept.CdwCoding cdw =
          new CdwObservation.CdwValueCodeableConcept.CdwCoding();
      cdw.setCode("vcc");
      cdw.setDisplay("value codeable concept coding");
      cdw.setSystem("http://example.com");
      return cdw;
    }

    private CdwValueQuantity valueQuantity() {
      CdwValueQuantity cdw = new CdwValueQuantity();
      cdw.setCode("/min");
      cdw.setComparator("<");
      cdw.setSystem("http://unitsofmeasure.org");
      cdw.setUnit("/min");
      cdw.setValue(BigDecimal.valueOf(74));
      return cdw;
    }
  }

  @NoArgsConstructor(staticName = "get")
  public static class Expected {
    CodeableConcept category() {
      return codeableConcept(
          coding("http://hl7.org/fhir/observation-category", "vital-signs", "Vital Signs"));
    }

    CodeableConcept code() {
      return codeableConcept(coding("http://loinc.org", "8867-4", "Heart rate")).text("<3");
    }

    CodeableConcept codeableConcept(Coding coding) {
      return CodeableConcept.builder().coding(singletonList(coding)).build();
    }

    Coding coding(String system, String code, String display) {
      return Coding.builder().system(system).code(code).display(display).build();
    }

    private CodeableConcept componentCode() {
      return CodeableConcept.builder()
          .text("Systolic blood pressure")
          .coding(singletonList(componentCodeCoding()))
          .build();
    }

    private Coding componentCodeCoding() {
      return Coding.builder()
          .code("8480-6")
          .display("Systolic blood pressure")
          .system("http://loinc.org")
          .build();
    }

    private CodeableConcept componentCodeableConcept() {
      return CodeableConcept.builder()
          .text("component cc")
          .coding(singletonList(componentCodeableConceptCoding()))
          .build();
    }

    private Coding componentCodeableConceptCoding() {
      return coding("http://example.com", "cccc1", "component codeable concept coding");
    }

    private Quantity componentQuantity() {
      return Quantity.builder()
          .code("mm[Hg]")
          .system("http://unitsofmeasure.org")
          .unit("mm[Hg]")
          .value(67D)
          .comparator(">")
          .build();
    }

    private ObservationComponent componentWithCodeableConcept() {
      return ObservationComponent.builder()
          .id("component1")
          .code(componentCode())
          .valueCodeableConcept(componentCodeableConcept())
          .build();
    }

    private ObservationComponent componentWithQuantity() {
      return ObservationComponent.builder()
          .id("component1")
          .code(componentCode())
          .valueQuantity(componentQuantity())
          .build();
    }

    List<ObservationComponent> components() {
      return singletonList(componentWithQuantity());
    }

    private SimpleQuantity emptyCodeReferenceRangeQuantity(int value) {
      return SimpleQuantity.builder().unit("k/cmm").value((double) value).build();
    }

    Quantity emptyCodeValueQuantity() {
      return Quantity.builder().comparator("<").unit("/min").value(74D).build();
    }

    public CodeableConcept interpretation() {
      return codeableConcept(coding("http://hl7.org/fhir/v2/0078", "L", "Low")).text("L");
    }

    public Observation observation() {
      return Observation.builder()
          .resourceType("Observation")
          .id("1201051417263:V")
          .status(Status._final)
          .category(category())
          .code(code())
          .subject(reference("Patient/185601V825290", "VETERAN,JOHN Q"))
          .encounter(reference("Encounter/1234", "The 3rd Kind"))
          .effectiveDateTime("2015-04-15T14:16:38Z")
          .issued("2015-04-15T14:19:45Z")
          .performer(performers())
          .valueQuantity(valueQuantity())
          .interpretation(interpretation())
          .comments("observe, ladies and gentlemen")
          .referenceRange(referenceRanges())
          .component(components())
          .build();
    }

    Observation observationWithValueCodeableConcept() {
      return observation().valueQuantity(null).valueCodeableConcept(valueCodeableConcept());
    }

    List<Reference> performers() {
      return singletonList(reference("Practitioner/1715142", "SMITH,ATTENDING D"));
    }

    Reference reference(String ref, String display) {
      return Reference.builder().reference(ref).display(display).build();
    }

    private ObservationReferenceRange referenceRange() {
      return ObservationReferenceRange.builder()
          .high(referenceRangeQuantity(10))
          .low(referenceRangeQuantity(1))
          .build();
    }

    private SimpleQuantity referenceRangeQuantity(int value) {
      return SimpleQuantity.builder()
          .system("http://unitsofmeasure.org")
          .code("k/cmm")
          .unit("k/cmm")
          .value((double) value)
          .build();
    }

    private List<ObservationReferenceRange> referenceRanges() {
      return singletonList(referenceRange());
    }

    CodeableConcept valueCodeableConcept() {
      return codeableConcept(coding("http://example.com", "vcc", "value codeable concept coding"))
          .text("value cc");
    }

    Quantity valueQuantity() {
      return Quantity.builder()
          .system("http://unitsofmeasure.org")
          .code("/min")
          .comparator("<")
          .unit("/min")
          .value(74D)
          .build();
    }
  }
}
