package gov.va.api.health.dataquery.tools.minimart.transformers;

import gov.va.api.health.argonaut.api.resources.Observation;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dataquery.service.controller.observation.DatamartObservation;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class F2DObservationTransformer {
  public DatamartObservation fhirToDatamart(Observation observation){
        return DatamartObservation.builder()
                .antibioticComponents(antibioticObservations(observation.component()))
                .build();
    }

    private List<DatamartObservation.AntibioticComponent> antibioticObservations(List<Observation.ObservationComponent> component) {
     return component.stream().filter(c -> c != null).map(c->antibioticObservation(c)).collect(Collectors.toList());
    }

    private DatamartObservation.AntibioticComponent antibioticObservation(Observation.ObservationComponent component) {
      if (component==null){
          return null;
      }
        String codeText = null;
      if (component.code()!=null){
          codeText = component.code().text();
      }

     return DatamartObservation.AntibioticComponent
              .builder()
              .code(codeableConcept(component.code()))
              .codeText(codeText)
              .id(component.id())
              .valueCodeableConcept(valueCodeableConcept(component.valueCodeableConcept()))
              .build();
    }

    private Optional<DatamartCoding> valueCodeableConcept(CodeableConcept valueCodeableConcept) {
      if (valueCodeableConcept==null||valueCodeableConcept.coding()==null||valueCodeableConcept.coding().isEmpty()||valueCodeableConcept.coding().get(0)==null){
          return null;
      }
        Coding coding = valueCodeableConcept.coding().get(0);
      return Optional.of(DatamartCoding.builder()
              .system(Optional.of(coding.system()))
              .code(Optional.of(coding.code()))
              .display(Optional.of(coding.display()))
              .build());
    }

    private Optional<DatamartObservation.CodeableConcept> codeableConcept(CodeableConcept codeableConcept) {
      return Optional.of(DatamartObservation.CodeableConcept.builder().coding(coding(codeableConcept.coding())).text(codeableConcept.text()).build());
    }

    private Optional<DatamartCoding> coding(List<Coding> coding) {
      if (coding==null||coding.isEmpty()||coding.get(0)==null){
          return null;
      }
      return Optional.of(DatamartCoding.builder()
              .display(Optional.of(coding.get(0).display()))
              .code(Optional.of(coding.get(0).code()))
              .system(Optional.of(coding.get(0).system())).build());
    }
}
