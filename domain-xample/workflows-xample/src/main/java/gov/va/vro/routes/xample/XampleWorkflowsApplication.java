package gov.va.vro.routes.xample;

import org.springdoc.hateoas.SpringDocHateoasConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(
    scanBasePackages = {"gov.va.vro.routes.xample", "gov.va.vro.camel"},
    // Exclude to avoid error "org.springdoc.hateoas.SpringDocHateoasConfiguration required a bean
    // ..."
    // Add DataSourceAutoConfiguration.class if no Spring DataSources are set up.
    exclude = {SpringDocHateoasConfiguration.class})
// Needed to interface with the DB
@EnableJpaRepositories("gov.va.vro.persistence.repository")
@EntityScan("gov.va.vro.persistence.model")
// Needed to auto-populate created_at and updated_at DB columns --
// https://stackoverflow.com/a/56873616
@EnableJpaAuditing
public class XampleWorkflowsApplication {
  public static void main(String[] args) {
    SpringApplication.run(XampleWorkflowsApplication.class, args);
  }
}