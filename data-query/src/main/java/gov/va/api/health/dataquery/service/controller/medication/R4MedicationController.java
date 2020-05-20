package gov.va.api.health.dataquery.service.controller.medication;

import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.Dstu2Validator;
import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.R4Bundler;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.api.health.uscorer4.api.resources.Medication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Min;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

/**
 * Request Mappings for Medication Profile, see
 * https://www.hl7.org/fhir/us/core/StructureDefinition-us-core-medication.html for implementation
 * details.
 */
@SuppressWarnings("WeakerAccess")
@Slf4j
@Validated
@RestController
@RequestMapping(
        value = {"/r4/Medication"},
        produces = {"application/json", "application/fhir+json"})
public class R4MedicationController {

    R4Bundler bundler;

    private MedicationRepository repository;

    private WitnessProtection witnessProtection;

    /** R4 Medication Constructor */
    public R4MedicationController(
            @Autowired R4Bundler bundler,
            @Autowired MedicationRepository repository,
            @Autowired WitnessProtection witnessProtection) {
        this.bundler = bundler;
        this.repository = repository;
        this.witnessProtection = witnessProtection;
    }

    private Medication.Bundle bundle(
            MultiValueMap<String, String> parameters, List<Medication> reports, int totalRecords) {

        PageLinks.LinkConfig linkConfig = PageLinks.LinkConfig.builder()
                .path("Medication")
                .queryParams(parameters)
                .page(Parameters.pageOf(parameters))
                .recordsPerPage(Parameters.countOf(parameters))
                .totalRecords(totalRecords)
                .build();

        return bundler.bundle(linkConfig, reports, Medication.Entry::new, Medication.Bundle::new);
    }

    MedicationEntity findById(String publicId) {
        Optional<MedicationEntity> entity = repository.findById(witnessProtection.toCdwId(publicId));
        return entity.orElseThrow(() -> new ResourceExceptions.NotFound(publicId));
    }

    /** Read by ID */
    @GetMapping(value = {"/{publicId}"})
    public Medication read(@PathVariable("publicId") String publicId) {
        DatamartMedication dmMedication = findById(publicId).asDatamartMedication();
        witnessProtection.registerAndUpdateReferences(List.of(dmMedication), resource -> Stream.empty());
        return R4MedicationTransformer.builder().datamart(dmMedication).build().toFhir();
    }

    /** Read by id, raw data. */
    @GetMapping(
            value = {"/{publicId}"},
            headers = {"raw=true"})
    public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
        IncludesIcnMajig.addHeaderForNoPatients(response);
        return findById(publicId).payload();
    }

    /** Search by _id. */
    @GetMapping(params = {"_id"})
    public Medication.Bundle searchById(
            @RequestParam("_id") String id,
            @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
            @CountParameter @Min(0) int count) {
        Medication resource = read(id);
        return bundle(
                Parameters.builder().add("identifier", id).add("page", page).add("_count", count).build(),
                resource == null || count == 0 ? emptyList() : List.of(resource),
                resource == null ? 0 : 1);
    }

    /** Search by Identifier. */
    @GetMapping(params = {"identifier"})
    public Medication.Bundle searchByIdentifier(
            @RequestParam("identifier") String id,
            @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
            @CountParameter @Min(0) int count) {
        return searchById(id, page, count);
    }

    Medication transform(DatamartMedication datamartMedication) {
        return R4MedicationTransformer.builder().datamart(datamartMedication).build().toFhir();
    }
}
