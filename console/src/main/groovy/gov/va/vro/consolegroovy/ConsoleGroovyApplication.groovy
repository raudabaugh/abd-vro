package gov.va.vro.consolegroovy

import org.springdoc.hateoas.SpringDocHateoasConfiguration
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

// Exclude to avoid error "org.springdoc.hateoas.SpringDocHateoasConfiguration required a bean ..."
@SpringBootApplication(exclude = [SpringDocHateoasConfiguration.class])
class ConsoleGroovyApplication {
  static void main(String[] args) {
    SpringApplication.run(ConsoleGroovyApplication, args)
  }
}