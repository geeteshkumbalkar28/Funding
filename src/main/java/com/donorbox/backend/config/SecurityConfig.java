package com.donorbox.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/public/blogs").permitAll()
                .requestMatchers("/api/public/blogs/**").permitAll()
                .requestMatchers("/api/images/**").permitAll()  // Allow public access to images
                .requestMatchers("/api/documents/**").permitAll() // Allow public access to documents
                .requestMatchers("/api/personal-cause-submissions/**").permitAll() // Allow public access to personal cause submissions
                .requestMatchers("/api/media/**").permitAll()    // Allow public access to media
                .requestMatchers("/uploads/**").permitAll()     // Allow direct access to uploads folder
                .requestMatchers("/donate").permitAll()
                .requestMatchers("/donations").permitAll()
                .requestMatchers("/causes").permitAll()
                .requestMatchers("/causes/**").permitAll()
                .requestMatchers("/events").permitAll()
                .requestMatchers("/blogs").permitAll()
                .requestMatchers("/events/**").permitAll()
                .requestMatchers("/volunteer/register").permitAll()
                .requestMatchers("/contact/send").permitAll()
                .requestMatchers("/homepage-stats").permitAll()
                .requestMatchers("/payment/**").permitAll()
                
                // Swagger endpoints
                .requestMatchers("/api-docs/**").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/webjars/**").permitAll()
                .requestMatchers("/swagger-resources/**").permitAll()
                
                // Admin endpoints - require authentication
                .requestMatchers("/admin/**").authenticated()
                
                // H2 Console (for testing)
                .requestMatchers("/h2-console/**").permitAll()
                
                // Root context redirect
                .requestMatchers("/").permitAll()
                .requestMatchers("/health").permitAll()
                
                // Any other request
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> {});

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("admin123"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
