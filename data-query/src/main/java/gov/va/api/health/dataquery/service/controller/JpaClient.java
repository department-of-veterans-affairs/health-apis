package gov.va.api.health.dataquery.service.controller;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(onConstructor = @__({@Autowired}))
public class JpaClient {
  private EntityManager entityManager;

  public <T> List<T> queryForEntities(
      String queryString,
      MultiValueMap<String, String> cdwParameters,
      int page,
      int count,
      Class<T> entityClass) {
    TypedQuery<T> query = entityManager.createQuery(queryString, entityClass);
    query.setFirstResult((page - 1) * count);
    query.setMaxResults(count);
    queryAddParameters(query, cdwParameters);
    List<T> results = query.getResultList();
    log.error("Found {} entities for parameters {}", results.size(), cdwParameters);
    return results;
  }

  public int queryForTotalRecords(String queryString, MultiValueMap<String, String> cdwParameters) {
    TypedQuery<Long> query = entityManager.createQuery(queryString, Long.class);
    queryAddParameters(query, cdwParameters);
    int totalRecords = query.getSingleResult().intValue();
    log.error("total records: " + totalRecords);
    return totalRecords;
  }

  private static void queryAddParameters(
      TypedQuery<?> query, MultiValueMap<String, String> parameters) {
    // PETERTODO examine all params and just skip page and _count
    if (parameters.containsKey("identifier")) {
      query.setParameter("identifier", parameters.getFirst("identifier"));
    }
    if (parameters.containsKey("patient")) {
      query.setParameter("patient", Long.valueOf(parameters.getFirst("patient")));
    }
  }
}
