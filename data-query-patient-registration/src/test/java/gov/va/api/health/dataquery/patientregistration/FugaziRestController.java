package gov.va.api.health.dataquery.patientregistration;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FugaziRestController {

  @GetMapping("/fugazi/Patient/{id}")
  String hello(@PathVariable(name = "id", required = false) String id) {
    return "hello " + id;
  }
}
