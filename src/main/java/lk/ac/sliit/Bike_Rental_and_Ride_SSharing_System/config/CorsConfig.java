package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CorsConfig
 * Package : config
 *
 * CORS = Cross-Origin Resource Sharing
 *
 * WHY NEEDED?
 *   When your HTML frontend (e.g. file:// or localhost:5500) calls
 *   your Spring Boot API (localhost:8080), the browser blocks it
 *   by default for security. This config tells Spring to ALLOW it.
 *
 * This applies globally to ALL API endpoints.
 * (The @CrossOrigin on BikeController also works — this is the global backup.)
 *
 * @Configuration → Spring reads this class at startup
 */
@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")           // Apply to all /api/ routes
                        .allowedOrigins("*")             // Allow ALL origins (frontend URLs)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                        .allowedHeaders("*")
                        .maxAge(3600);                   // Cache preflight for 1 hour
            }
        };
    }
}
