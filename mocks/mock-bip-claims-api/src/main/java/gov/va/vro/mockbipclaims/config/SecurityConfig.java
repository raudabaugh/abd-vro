package gov.va.vro.mockbipclaims.config;

import gov.va.vro.mockshared.rest.JwtRequestFilter;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@AllArgsConstructor
public class SecurityConfig {
  private final JwtRequestFilter filter;

  @Qualifier("delegatedAuthenticationEntryPoint")
  private final AuthenticationEntryPoint authEntryPoint;

  /**
   * Spring security to validate JWT.
   *
   * @param http HttpSecurity
   * @return SecurityFilterChain
   * @throws Exception Authentication failuure
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf((AbstractHttpConfigurer::disable));
    http.sessionManagement(
        (securitySessionManagementConfigurer) ->
            securitySessionManagementConfigurer.sessionCreationPolicy(
                SessionCreationPolicy.STATELESS));
    http.authorizeHttpRequests(
        (managerRequestMatcherRegistry) -> {
          managerRequestMatcherRegistry.requestMatchers("/updates/**").permitAll();
          managerRequestMatcherRegistry.requestMatchers("/actuator/health").permitAll();
          managerRequestMatcherRegistry.requestMatchers("/").permitAll();
          managerRequestMatcherRegistry.requestMatchers("/swagger-ui.html").permitAll();
          managerRequestMatcherRegistry.requestMatchers("/swagger-ui/**").permitAll();
          managerRequestMatcherRegistry.requestMatchers("/v3/api-docs/**").permitAll();
          managerRequestMatcherRegistry.anyRequest().authenticated();
        });
    http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
    http.exceptionHandling(
        (exceptionHandlingConfigurer) ->
            exceptionHandlingConfigurer.authenticationEntryPoint(authEntryPoint));
    return http.build();
  }
}
