package gov.va.api.health.dataquery.service.mranderson.client;

/**
 * This is the abstraction for communicating with the Mr. Anderson service. This service works with
 * a JAXB model that represents the CDW schemas. Queries to Mr. Anderson are contained in type-safe
 * objects.
 */
public interface MrAndersonClient {
  /** Return a JAXB de-serialized root object for the result of the search. */
  <T> T search(Query<T> query);

  /** A request to Mr. Anderson was malformed, such as missing required search parameters. */
  class BadRequest extends MrAndersonServiceException {
    public BadRequest(Query<?> query) {
      super(query);
    }
  }

  /** The generic exception for working with Mr. Anderson. */
  class MrAndersonServiceException extends RuntimeException {
    MrAndersonServiceException(Query<?> query) {
      super(query.toQueryString());
    }
  }

  /** The resource requested was not found. */
  class NotFound extends MrAndersonServiceException {
    public NotFound(Query<?> query) {
      super(query);
    }
  }

  /** An unspecified error occurred while performing a search. */
  class SearchFailed extends MrAndersonServiceException {
    public SearchFailed(Query<?> query) {
      super(query);
    }
  }
}
