package gov.va.api.health.dataquery.service.api;

import gov.va.api.health.uscorer4.api.AllergyIntoleranceApi;
import gov.va.api.health.uscorer4.api.ConditionApi;
import gov.va.api.health.uscorer4.api.PatientApi;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import javax.ws.rs.Path;

@OpenAPIDefinition(
    security =
        @SecurityRequirement(
            name = "OauthFlow",
            scopes = {
              "patient/AllergyIntolerance.read",
              "patient/Condition.read",
              "patient/Patient.read",
              "offline_access",
              "launch/patient"
            }),
    info =
        @Info(
            title = "US Core R4",
            version = "v1",
            description =
                " This service is compliant with the FHIR US Core Implementation"
                    + " Guide. This service does not provide or replace the consultation, guidance,"
                    + " or care of a health care professional or other qualified provider."
                    + " This service provides a supplement for informational and educational"
                    + " purposes only. Health care professionals and other qualified providers"
                    + " should continue to consult authoritative records when making decisions."),
    servers = {
      @Server(
          url = "https://sandbox-api.va.gov/services/fhir/v0/r4/",
          description = "Development server")
    },
    externalDocs =
        @ExternalDocumentation(
            description = "US Core Implementation Guide",
            url = "https://build.fhir.org/ig/HL7/US-Core-R4/index.html"))
@SecurityScheme(
    name = "OauthFlow",
    type = SecuritySchemeType.OAUTH2,
    in = SecuritySchemeIn.HEADER,
    flows =
        @OAuthFlows(
            implicit =
                @OAuthFlow(
                    authorizationUrl = "https://sandbox-api.va.gov/oauth2/authorization",
                    tokenUrl = "https://sandbox-api.va.gov/services/fhir/v0/r4/token",
                    scopes = {
                      @OAuthScope(
                          name = "patient/AllergyIntolerance.read",
                          description = "read allergy intolerances"),
                      @OAuthScope(name = "patient/Condition.read", description = "read conditions"),
                      @OAuthScope(name = "patient/Patient.read", description = "read patient"),
                      @OAuthScope(name = "offline_access", description = "offline access"),
                      @OAuthScope(name = "launch/patient", description = "patient launch"),
                    })))
@Path("/")
public interface R4DataQueryService extends AllergyIntoleranceApi, ConditionApi, PatientApi {}
