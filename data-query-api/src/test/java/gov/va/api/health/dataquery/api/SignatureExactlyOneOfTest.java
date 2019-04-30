package gov.va.api.health.dataquery.api;

import static java.util.Collections.singletonList;
import static org.junit.Assert.*;
import java.util.Set;
import javax.validation.Validation;
import javax.validation.ConstraintViolation;
import org.junit.Test;
import gov.va.api.health.dataquery.api.datatypes.Signature;
import gov.va.api.health.dataquery.api.samples.SampleDataTypes;

public class SignatureExactlyOneOfTest {
	private final SampleDataTypes data = SampleDataTypes.get();
	private final Signature psuedoSignature = signature();
	
	@Test
	public void test() {		
		Set<ConstraintViolation<Signature>> problems = Validation
				.buildDefaultValidatorFactory()
				.getValidator()
				.validate(psuedoSignature);
		
		assertEquals(problems.size(), 0);
	}
	
//	Sample Signature
	private Signature signature() {
	    return Signature.builder()
	        .id("0714")
	        .extension(singletonList(data.extension()))
	        .type(data.codingList())
	    	.when("2000-01-01T00:00:00-00:00")
	    	.whoUri(null)
	    	.whoReference(data.reference())
	    	.contentType("contentTypeTest")
	    	.blob("aGVsbG8=")
	        .build();
	  }
}
