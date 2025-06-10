package com.github.dimitryivaniuta.bankaccount;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Bank Account application.
 * <p>
 * Configures and launches the Spring Boot application context, which
 * initializes all components, database migrations, and the embedded server.
 * </p>
 *
 * <p>To start the application, execute:</p>
 * <pre>java -jar bank-account-backend.jar</pre>
 *
 * <p>This class is annotated with {@code @SpringBootApplication},
 * which encompasses {@code @Configuration}, {@code @EnableAutoConfiguration},
 * and {@code @ComponentScan}.</p>
 */
@SpringBootApplication
public class BankAccountApplication {

    /**
     * Application bootstrap method.
     * <p>
     * Invokes {@link SpringApplication#run(Class, String[])} to launch the
     * Spring application context and start the embedded servlet container.
     * </p>
     *
     * @param args command-line arguments (e.g., profile selection, custom properties)
     */
    public static void main(final String[] args) {
        SpringApplication.run(BankAccountApplication.class, args);
    }
}
