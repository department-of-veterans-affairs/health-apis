package gov.va.api.health.dataquery.service.api;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.servers.Server;
import javax.ws.rs.Path;

/** R4 Swagger Definition. */
@OpenAPIDefinition(
    security = {
      @SecurityRequirement(
          name = "OauthFlowSandbox",
          scopes = {
            "patient/AllergyIntolerance.read",
            "system/Appointment.read",
            "patient/Condition.read",
            "patient/Device.read",
            "patient/DiagnosticReport.read",
            "patient/Immunization.read",
            "patient/Medication.read",
            "patient/MedicationRequest.read",
            "patient/Observation.read",
            "patient/Patient.read",
            "patient/Practitioner.read",
            "patient/PractitionerRole.read",
            "patient/Procedure.read",
            "offline_access",
            "launch/patient"
          }),
      @SecurityRequirement(
          name = "OauthFlowProduction",
          scopes = {
            "patient/AllergyIntolerance.read",
            "system/Appointment.read",
            "patient/Condition.read",
            "patient/Device.read",
            "patient/DiagnosticReport.read",
            "patient/Immunization.read",
            "patient/Medication.read",
            "patient/MedicationRequest.read",
            "patient/Observation.read",
            "patient/Patient.read",
            "patient/Practitioner.read",
            "patient/PractitionerRole.read",
            "patient/Procedure.read",
            "offline_access",
            "launch/patient"
          })
    },
    info =
        @Info(
            title = "US Core R4",
            version = "v1",
            description =
                "In adherence to changes per the [21st Century Cures Act]"
                    + "(https://www.federalregister.gov/documents/2020/05/01/2020-07419"
                    + "/21st-century-cures-act-interoperability-information-blocking-and-the"
                    + "-onc-health-it-certification#h-13), the Veteran Health API profile follows "
                    + "the US Core Implementation Guide. Per these regulations, we will be adding "
                    + "new FHIR resources to this tab as they are available.\n\n"
                    + "This service is compliant with the FHIR US Core Implementation Guide. "
                    + "This service does not provide or replace the consultation, guidance, or "
                    + "care of a health care professional or other qualified provider. This service"
                    + " provides a supplement for informational and educational purposes only. "
                    + "Health care professionals and other qualified providers should continue to "
                    + "consult authoritative records when making decisions."),
    servers = {
      @Server(url = "https://sandbox-api.va.gov/services/fhir/v0/r4/", description = "Sandbox"),
      @Server(url = "https://api.va.gov/services/fhir/v0/r4/", description = "Production")
    },
    externalDocs =
        @ExternalDocumentation(
            description = "US Core Implementation Guide",
            url = "https://build.fhir.org/ig/HL7/US-Core-R4/index.html"))
@SecuritySchemes({
  @SecurityScheme(
      name = "OauthFlowSandbox",
      type = SecuritySchemeType.OAUTH2,
      flows =
          @OAuthFlows(
              authorizationCode =
                  @OAuthFlow(
                      authorizationUrl = "https://sandbox-api.va.gov/oauth2/authorization",
                      tokenUrl = "https://sandbox-api.va.gov/services/fhir/v0/r4/token",
                      scopes = {
                        @OAuthScope(
                            name = "patient/AllergyIntolerance.read",
                            description = "read allergy intolerances"),
                        @OAuthScope(
                            name = "system/Appointment.read",
                            description = "read appointments"),
                        @OAuthScope(
                            name = "patient/Condition.read",
                            description = "read conditions"),
                        @OAuthScope(name = "patient/Device.read", description = "read devices"),
                        @OAuthScope(
                            name = "patient/DiagnosticReport.read",
                            description = "read diagnostic reports"),
                        @OAuthScope(
                            name = "patient/Immunization.read",
                            description = "read immunizations"),
                        @OAuthScope(
                            name = "patient/Medication.read",
                            description = "read medications"),
                        @OAuthScope(
                            name = "patient/MedicationRequest.read",
                            description = "read medication requests"),
                        @OAuthScope(
                            name = "patient/Observation.read",
                            description = "read observations"),
                        @OAuthScope(name = "patient/Patient.read", description = "read patient"),
                        @OAuthScope(
                            name = "patient/Practitioner.read",
                            description = "read practitioner"),
                        @OAuthScope(
                            name = "patient/PractitionerRole.read",
                            description = "read practitioner roles"),
                        @OAuthScope(
                            name = "patient/Procedure.read",
                            description = "read procedures"),
                        @OAuthScope(name = "offline_access", description = "offline access"),
                        @OAuthScope(name = "launch/patient", description = "patient launch"),
                      }))),
  @SecurityScheme(
      name = "OauthFlowProduction",
      type = SecuritySchemeType.OAUTH2,
      flows =
          @OAuthFlows(
              authorizationCode =
                  @OAuthFlow(
                      authorizationUrl = "https://api.va.gov/oauth2/authorization",
                      tokenUrl = "https://api.va.gov/services/fhir/v0/r4/token",
                      scopes = {
                        @OAuthScope(
                            name = "patient/AllergyIntolerance.read",
                            description = "read allergy intolerances"),
                        @OAuthScope(
                            name = "system/Appointment.read",
                            description = "read appointments"),
                        @OAuthScope(
                            name = "patient/Condition.read",
                            description = "read conditions"),
                        @OAuthScope(name = "patient/Device.read", description = "read devices"),
                        @OAuthScope(
                            name = "patient/DiagnosticReport.read",
                            description = "read diagnostic reports"),
                        @OAuthScope(
                            name = "patient/Immunization.read",
                            description = "read immunizations"),
                        @OAuthScope(
                            name = "patient/Medication.read",
                            description = "read medications"),
                        @OAuthScope(
                            name = "patient/MedicationRequest.read",
                            description = "read medication requests"),
                        @OAuthScope(
                            name = "patient/Observation.read",
                            description = "read observations"),
                        @OAuthScope(name = "patient/Patient.read", description = "read patient"),
                        @OAuthScope(
                            name = "patient/Practitioner.read",
                            description = "read practitioner"),
                        @OAuthScope(
                            name = "patient/PractitionerRole.read",
                            description = "read practitioner roles"),
                        @OAuthScope(
                            name = "patient/Procedure.read",
                            description = "read procedures"),
                        @OAuthScope(name = "offline_access", description = "offline access"),
                        @OAuthScope(name = "launch/patient", description = "patient launch"),
                      })))
})
@Path("/")
public interface R4DataQueryService
    extends R4AllergyIntoleranceApi,
        R4AppointmentApi,
        R4ConditionApi,
        R4DeviceApi,
        R4DiagnosticReportApi,
        R4ImmunizationApi,
        R4LocationApi,
        R4MedicationApi,
        R4MedicationRequestApi,
        R4MetadataApi,
        R4ObservationApi,
        R4OrganizationApi,
        R4PatientApi,
        R4PractitionerApi,
        R4PractitionerRoleApi,
        R4ProcedureApi {}
