package gov.va.api.health.dataquery.service.controller.medicationstatement;

import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DatamartMedicationStatementSamples {

  @AllArgsConstructor(staticName = "create")
  static class Datamart {
    public DatamartMedicationStatement medicationStatement() {
      return medicationStatement("800000000707", "666V666", "2019-07-01");
    }

    public DatamartMedicationStatement medicationStatement(String cdwId, String patientId, String dateRecorded) {
      return DatamartMedicationStatement.builder()
      /*{
        "objectType": "MedicationStatement",
          "objectVersion": "1",
          "cdwId": "800000000707",
          "etlDate": "2014-12-06T05:53:02Z",
          "patient": {
        "type": "Patient",
            "reference": "111222333V000999",
            "display": "Olson653, Conrad619"
      },
        "dateAsserted": "2014-12-06T05:53:02Z",
          "status": "active",
          "effectiveDateTime": "2010-12-06T05:53:02Z",
          "note": null,
          "medication": {
        "type": "Medication",
            "reference": "1004301",
            "display": "PITO PITO"
      },
        "dosage": {
        "text": "BAG",
            "timingCodeText": "AFTER BREAKFAST",
            "routeText": "*Missing*"
      }
      }*/
          .build();
    }
  }


}
