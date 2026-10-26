package backend.academy.linktracker.scrapper.configuration;

import backend.academy.linktracker.scrapper.infrastructure.api.auth.InternalTokenFilter;
import backend.academy.linktracker.scrapper.infrastructure.api.auth.JwtAuthenticationEntryPoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final int MANAGEMENT_CHAIN_ORDER = 0;
    private static final int MAIN_CHAIN_ORDER = 1;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtDecoder jwtDecoder(@Value("${app.jwt.secret}") String secret) {
        SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }

    @Bean
    public InternalTokenFilter internalTokenFilter(
            ObjectMapper objectMapper, @Value("${app.internal-token}") String internalToken) {
        return new InternalTokenFilter(objectMapper, internalToken);
    }

    @Bean
    public FilterRegistrationBean<InternalTokenFilter> internalTokenFilterRegistration(
            InternalTokenFilter internalTokenFilter) {
        FilterRegistrationBean<InternalTokenFilter> registration = new FilterRegistrationBean<>(internalTokenFilter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    @Order(MANAGEMENT_CHAIN_ORDER)
    public SecurityFilterChain managementPortSecurityFilterChain(
            HttpSecurity http, @Value("${management.server.port:8012}") int managementPort) throws Exception {
        return http.securityMatcher(request -> request.getLocalPort() == managementPort)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }

    @Bean
    @Order(MAIN_CHAIN_ORDER)
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, InternalTokenFilter internalTokenFilter, JwtAuthenticationEntryPoint entryPoint)
            throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.requestMatchers("/auth/me")
                        .authenticated()
                        .requestMatchers(
                                "/auth/**",
                                "/internal/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/actuator/**")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()).authenticationEntryPoint(entryPoint))
                .addFilterBefore(internalTokenFilter, BearerTokenAuthenticationFilter.class)
                .build();
    }
}
