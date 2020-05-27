package gov.va.api.health.dataquery.service.controller.observation;


import com.google.common.base.Splitter;
import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.DateTimeParameter;
import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.R4Bundler;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.condition.R4ConditionController;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.uscorer4.api.resources.Observation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

/**
 * Request Mappings for Observation Profile, see
 * https://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-observation-lab.html for
 * implementation details.
 */
@Validated
@RestController
@RequestMapping(
        value = {"/r4/Observation"},
        produces = {"application/json", "application/fhir+json"}
)
public class R4ObservationController {

    //ToDo /$validate
    //ToDo SHOULD patient, category, and status

    private R4Bundler bundler;

    private ObservationRepository repository;

    private WitnessProtection witnessProtection;

    /** Constructor. */
    public R4ObservationController(
            @Autowired R4Bundler bundler,
            @Autowired ObservationRepository repository,
            @Autowired WitnessProtection witnessProtection
    ) {
        this.bundler = bundler;
        this.repository = repository;
        this.witnessProtection = witnessProtection;

    }

    Observation.Bundle bundle(MultiValueMap<String, String> parameters, List<Observation> records, int totalRecords) {
        PageLinks.LinkConfig linkConfig =
                PageLinks.LinkConfig.builder()
                        .path("Observation")
                        .queryParams(parameters)
                        .page(Parameters.pageOf(parameters))
                        .recordsPerPage(Parameters.countOf(parameters))
                        .totalRecords(totalRecords)
                        .build();
        return bundler.bundle(linkConfig, records, Observation.Entry::new, Observation.Bundle::new);
    }

    Observation.Bundle bundle(MultiValueMap<String, String> parameters, Page<ObservationEntity> entitiesPage) {
        if (Parameters.countOf(parameters) <= 0) {
            return bundle(parameters, emptyList(), (int) entitiesPage.getTotalElements());
        }
        List<DatamartObservation> datamarts = entitiesPage.stream().map(ObservationEntity::asDatamartObservation).collect(Collectors.toList());
        replaceReferences(datamarts);
        List<Observation> fhir =
                datamarts.stream().map(dm -> R4ObservationTransformer.builder().datamart(dm).build().toFhir())
                .collect(Collectors.toList());
        return bundle(parameters, fhir, (int) entitiesPage.getTotalElements());
    }

    ObservationEntity findById(String publicId) {
        String cdwId = witnessProtection.toCdwId(publicId);
        Optional<ObservationEntity> maybeEntity = repository.findById(cdwId);
        return maybeEntity.orElseThrow(() -> new ResourceExceptions.NotFound(publicId));
    }

    Pageable page(int page, int count) {
        return PageRequest.of(page - 1, Math.max(count, 1), ObservationEntity.naturalOrder());
    }

    /** Read R4 Observation By Id. */
    @GetMapping(value = "/{publicId}")
    public Observation read(@PathVariable("publicId") String publicId) {
        DatamartObservation dm = findById(publicId).asDatamartObservation();
        replaceReferences(List.of(dm));
        return R4ObservationTransformer.builder().datamart(dm).build().toFhir();
    }

    /** Return the raw datamart document for the given Observation Id. */
    @GetMapping(
            value = "/{publicId}",
            headers = {"raw=true"}
    )
    public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
        ObservationEntity entity = findById(publicId);
        IncludesIcnMajig.addHeader(response, entity.icn());
        return entity.payload();
    }

    void replaceReferences(Collection<DatamartObservation> resources) {
    // Omits Observation References that are unsupported
    witnessProtection.registerAndUpdateReferences(resources, resource -> Stream.concat(
            Stream.of(resource.subject().orElse(null)), resource.performer().stream())
    );
    }

    /** Search R4 Observation by _id. */
    @GetMapping(params = {"_id"})
    public Observation.Bundle searchById(@RequestParam("_id") String id,
                                                 @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
                                                 @CountParameter @Min(0) int count) {
        return searchByIdentifier(id, page, count);
    }

    /** Search R4 Observation by identifier. */
    @GetMapping(params = {"identifier"})
    public Observation.Bundle searchByIdentifier(@RequestParam("identifier") String id,
                                         @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
                                         @CountParameter @Min(0) int count) {
        MultiValueMap<String, String> parameters =
                Parameters.builder()
                        .add("identifier", id)
                        .add("page", page)
                        .add("_count", count)
                .build();
        Observation resource = read(id);
        int totalRecords = resource == null ? 0 : 1;
        if(resource == null || page != 1 || count <= 0) {
            return bundle(parameters, emptyList(), totalRecords);
        }
        return bundle(parameters, asList(resource), totalRecords);
    }

    /** Search R4 Observation by Patient. */
    @GetMapping(params = {"patient"})
    public Observation.Bundle searchByPatient(
            @RequestHeader(name = "query-hack", required = false, defaultValue = "true") boolean queryHack,
            @RequestParam("patient") String patient,
            @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
            @CountParameter @Min(0) int count
    ) {
        MultiValueMap<String, String> parameters =
                Parameters.builder()
                        .add("patient", patient)
                        .add("page", page)
                        .add("_count", count)
                        .build();
        String cdwPatient = witnessProtection.toCdwId(patient);
        if(queryHack) {
            Observation.Bundle bundleWithWrongPageLinks =
                    searchByPatientAndCategory(true, patient, "laboratory,vital-signs", null, page, count);
            return bundle(parameters,
                    bundleWithWrongPageLinks.entry().stream()
                    .map(AbstractEntry::resource)
                    .collect(Collectors.toList()),
                    bundleWithWrongPageLinks.total()
            );
        }
        Page<ObservationEntity> entitiesPage = repository.findByIcn(cdwPatient, page(page, count));
        return bundle(parameters, entitiesPage);
    }

    /** Search R4 Observation by patient and category (and date if exists). */
    @GetMapping(params = {"patient", "category"})
    public Observation.Bundle searchByPatientAndCategory(
            @RequestHeader(name = "query-hack", required = false, defaultValue = "true") boolean queryHack,
            @RequestParam("patient") String patient,
            @RequestParam("category") String categoryCsv,
            @RequestParam(value = "date", required = false) @Valid @DateTimeParameter @Size(max = 2) String[] date,
            @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
            @CountParameter @Min(0) int count
    ) {
        MultiValueMap<String, String> parameters =
                Parameters.builder()
                        .add("patient", patient)
                        .add("category", categoryCsv)
                        .addAll("date", date)
                        .add("page", page)
                        .add("_count", count)
                        .build();
        String cdwPatient = witnessProtection.toCdwId(patient);
        ObservationRepository.PatientAndCategoryAndDateSpecification spec =
                ObservationRepository.PatientAndCategoryAndDateSpecification.builder()
                        .patient(cdwPatient)
                        .categories(Splitter.on(",").trimResults().splitToList(categoryCsv))
                        .dates(date)
                        .build();
        if(queryHack) {
            List<ObservationEntity> all = repository.findAll(spec);
            int firstIndex = Math.min((page - 1) * count, all.size());
            int lastIndex = Math.min(firstIndex + count, all.size());
            List<DatamartObservation> pageOfEntities =
                    firstIndex >= all.size()
                            ? emptyList()
                            : all.subList(firstIndex, lastIndex).stream()
                    .map(ObservationEntity::asDatamartObservation)
                    .collect(Collectors.toList());
            replaceReferences(pageOfEntities);
            List<Observation> pageOfResults =
                    pageOfEntities.stream().map(dm -> R4ObservationTransformer.builder().datamart(dm).build().toFhir())
                    .collect(Collectors.toList());
            return bundle(parameters, pageOfResults, all.size());
        }
        Page<ObservationEntity> entitiesPage = repository.findAll(spec, page(page, count));
        return bundle(parameters, entitiesPage);
    }

    /** Search R4 Observation by patient and code. */
    @GetMapping(params = {"patient", "code"})
    public Observation.Bundle searchByPatientAndCode(
            @RequestParam("patient") String patient,
            @RequestParam("code") String codeCsv,
            @RequestParam(value = "date", required = false) @Valid @DateTimeParameter @Size(max = 2) String[] date,
            @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
            @CountParameter @Min(0) int count
    ) {
        MultiValueMap<String, String> parameters = Parameters.builder()
                .add("patient", patient)
                .add("code", codeCsv)
                .addAll("date", date)
                .add("page", page)
                .add("_count", count)
                .build();
        String cdwPatient = witnessProtection.toCdwId(patient);
        ObservationRepository.PatientAndCodeAndDateSpecification spec =
                ObservationRepository.PatientAndCodeAndDateSpecification.builder()
                        .patient(cdwPatient)
                        .codes(Splitter.on(",").splitToList(codeCsv))
                        .dates(date)
                        .build();
        Page<ObservationEntity> entitiesPage = repository.findAll(spec, page(page, count));
        return bundle(parameters, entitiesPage);
    }
}
