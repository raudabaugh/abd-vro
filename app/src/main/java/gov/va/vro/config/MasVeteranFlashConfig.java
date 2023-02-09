package gov.va.vro.config;

import gov.va.vro.model.mas.MasVeteranFlashProps;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configure Veteran Flash IDs. */
@Configuration
public class MasVeteranFlashConfig {

  @Bean
  MasVeteranFlashProps MasVeteranFlashConfigLoad(
      @Value("${masVeteranFlashIds.agentOrange}") String[] agentOrangeFlashIds) {
    return MasVeteranFlashProps.getInstance(agentOrangeFlashIds);
  }
}