package gov.va.api.health.dataquery.patientregistration;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FugaziRestController {

  @GetMapping("/hello")
  String hello() {
    return "hello";
  }
}
