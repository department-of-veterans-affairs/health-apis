package gov.va.api.health.mranderson.cdw;

import org.springframework.stereotype.Component;

/** The primary interface for executing queries in CDW. */
@Component
public interface Resources {
  String search(Query query);

  class MissingSearchParameters extends ResourcesException {
    public MissingSearchParameters(Query query) {
      super(query.toQueryString());
    }
  }

  class ResourcesException extends RuntimeException {
    ResourcesException(String message, Throwable cause) {
      super(message, cause);
    }

    ResourcesException(String message) {
      super(message);
    }
  }

  class SearchFailed extends ResourcesException {
    public SearchFailed(Query query, Exception cause) {
      super(query.toQueryString(), cause);
    }

    public SearchFailed(Query query, String message) {
      super(query.toQueryString() + " Reason: " + message);
    }
  }

  class UnknownIdentityInSearchParameter extends ResourcesException {
    public UnknownIdentityInSearchParameter(Query query, Exception cause) {
      super(query.toQueryString(), cause);
    }

    public UnknownIdentityInSearchParameter(Query query) {
      super(query.toQueryString());
    }
  }

  class UnknownResource extends ResourcesException {
    public UnknownResource(Query query) {
      super(query.toResourceString());
    }
  }
}
