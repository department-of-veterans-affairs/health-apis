package gov.va.api.health.dataquery.patientregistration;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

/** This filter intercepts requests to perform patient registration. */
@Slf4j
@Builder
public class PatientRegistrationFilter extends OncePerRequestFilter {

  public static final String REGISTRATION_HEADER = "X-VA-PATIENT-REGISTRATION";

  private final PatientRegistrar registrar;

  @Builder.Default private final List<IcnDistiller> distillers = defaultDistillers();

  private static List<IcnDistiller> defaultDistillers() {
    return List.of(new PatientReadIcnDistiller(), new PatientSearchByIcnDistiller());
  }

  private Optional<String> distillIcnFromRequest(HttpServletRequest httpServletRequest) {
    return distillers.stream()
        .map(d -> d.distillFromUri(httpServletRequest))
        .filter(Objects::nonNull)
        .findFirst();
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest httpServletRequest,
      @NonNull HttpServletResponse httpServletResponse,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    Optional<String> icn = distillIcnFromRequest(httpServletRequest);
    Future<PatientRegistration> maybeRegistration = null;
    if (icn.isPresent()) {
      maybeRegistration = quietlyRegister(httpServletResponse, icn);
    }
    filterChain.doFilter(httpServletRequest, httpServletResponse);
    printRegistrationResult(maybeRegistration);
  }

  private void printRegistrationResult(Future<PatientRegistration> maybeRegistration) {
    if (maybeRegistration == null) {
      return;
    }
    if (!maybeRegistration.isDone()) {
      log.warn("Patient registration is still in progress.");
      return;
    }
    try {
      PatientRegistration registration = maybeRegistration.get(10, TimeUnit.MILLISECONDS);
      log.debug("Patient registration has completed. {}", registration);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      log.warn("Ignoring patient registration results error.", e.getMessage());
    }
  }

  private Future<PatientRegistration> quietlyRegister(
      HttpServletResponse httpServletResponse, Optional<String> icn) {
    assert icn.isPresent() : "icn is empty";
    httpServletResponse.addHeader(REGISTRATION_HEADER, Instant.now().toString());
    log.debug("Registering patient");
    try {
      return registrar.register(icn.get());
    } catch (Exception e) {
      /*
       * Registration is not essential to servicing the request. If an error occurs, we will
       * suppress it and proceed with servicing the request.
       */
      log.warn("Ignoring patient registration error", e);
    }
    return null;
  }

  @FunctionalInterface
  interface IcnDistiller {
    String distillFromUri(HttpServletRequest request);
  }

  static class PatientReadIcnDistiller implements IcnDistiller {
    private static final Pattern PATIENT_URI_PATTERN = Pattern.compile(".*/Patient/([0-9V]+)$");

    @Override
    public String distillFromUri(HttpServletRequest request) {
      var matcher = PATIENT_URI_PATTERN.matcher(request.getRequestURI());
      if (matcher.matches()) {
        return matcher.group(1);
      }
      return null;
    }
  }

  static class PatientSearchByIcnDistiller implements IcnDistiller {

    @Override
    public String distillFromUri(HttpServletRequest request) {
      if (request.getRequestURI().endsWith("/Patient")) {
        if (request.getParameter("_id") != null) {
          return request.getParameter("_id");
        }
        if (request.getParameter("identifier") != null) {
          return request.getParameter("identifier");
        }
      }
      return null;
    }
  }
}
