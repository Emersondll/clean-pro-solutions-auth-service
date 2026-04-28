package br.com.cleanprosolutions.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Entry point for the Clean Pro Solutions Authentication Service.
 *
 * <p>This service is responsible for:</p>
 * <ul>
 *   <li>User registration</li>
 *   <li>JWT-based authentication (access + refresh tokens)</li>
 *   <li>Token validation for the BFF layer</li>
 * </ul>
 *
 * <p>Registers itself with the Eureka Service Registry under the name {@code auth-service}.</p>
 *
 * @author Clean Pro Solutions Team
 * @since 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class AuthServiceApplication {

    public static void main(final String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
