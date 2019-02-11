package gov.va.api.health.argonaut.service.controller.medication;

import static gov.va.api.health.argonaut.service.controller.Transformers.allBlank;
import static gov.va.api.health.argonaut.service.controller.Transformers.convert;
import static gov.va.api.health.argonaut.service.controller.Transformers.convertAll;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.argonaut.api.datatypes.CodeableConcept;
import gov.va.api.health.argonaut.api.datatypes.Coding;
import gov.va.api.health.argonaut.api.elements.Narrative;
import gov.va.api.health.argonaut.api.elements.Narrative.NarrativeStatus;
import gov.va.api.health.argonaut.api.resources.Medication;
import gov.va.api.health.argonaut.api.resources.Medication.Product;
import gov.va.dvp.cdw.xsd.model.CdwCodeableConcept;
import gov.va.dvp.cdw.xsd.model.CdwCoding;
import gov.va.dvp.cdw.xsd.model.CdwMedication101Root.CdwMedications.CdwMedication;
import gov.va.dvp.cdw.xsd.model.CdwMedication101Root.CdwMedications.CdwMedication.CdwProduct;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MedicationTransformer implements MedicationController.Transformer {

  @Override
  public Medication apply(CdwMedication medication) {
    return medication(medication);
  }

  CodeableConcept code(CdwCodeableConcept optionalSource) {
    if (optionalSource == null) {
      return null;
    }
    if (optionalSource.getCoding().isEmpty() && isBlank(optionalSource.getText())) {
      return null;
    }
    return convert(
        optionalSource,
        cdw ->
            CodeableConcept.builder()
                .text(cdw.getText())
                .coding(codeCodings(cdw.getCoding()))
                .build());
  }

  private Coding codeCoding(CdwCoding cdw) {
    if (cdw == null || allBlank(cdw.getSystem(), cdw.getCode(), cdw.getDisplay())) {
      return null;
    }
    return Coding.builder()
        .system(cdw.getSystem())
        .code(cdw.getCode())
        .display(cdw.getDisplay())
        .build();
  }

  List<Coding> codeCodings(List<CdwCoding> source) {
    return convertAll(source, this::codeCoding);
  }

  CodeableConcept form(CdwCodeableConcept source) {
    if (source == null) {
      return null;
    }
    if (source.getCoding().isEmpty() && isBlank(source.getText())) {
      return null;
    }
    return CodeableConcept.builder()
        .text(source.getText())
        .coding(formCodings(source.getCoding()))
        .build();
  }

  private Coding formCoding(CdwCoding cdw) {
    if (cdw == null || allBlank(cdw.getCode(), cdw.getDisplay(), cdw.getSystem())) {
      return null;
    }
    return Coding.builder()
        .system(cdw.getSystem())
        .code(cdw.getCode())
        .display(cdw.getDisplay())
        .build();
  }

  List<Coding> formCodings(List<CdwCoding> source) {
    return convertAll(source, this::formCoding);
  }

  private Medication medication(CdwMedication source) {
    return Medication.builder()
        .id(source.getCdwId())
        .resourceType("Medication")
        .text(text(source.getText()))
        .code(code(source.getCode()))
        .product(product(source.getProduct()))
        .build();
  }

  Product product(CdwProduct optionalSource) {
    if (optionalSource == null || allBlank(optionalSource.getForm(), optionalSource.getId())) {
      return null;
    }
    return convert(
        optionalSource, cdw -> Product.builder().id(cdw.getId()).form(form(cdw.getForm())).build());
  }

  Narrative text(String optionalSource) {
    if (optionalSource == null || isBlank(optionalSource)) {
      return null;
    }
    return convert(
        optionalSource,
        cdw ->
            Narrative.builder()
                .div("<div>" + cdw + "</div>")
                .status(NarrativeStatus.additional)
                .build());
  }
}
