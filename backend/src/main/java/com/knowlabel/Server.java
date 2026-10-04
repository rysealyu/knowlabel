package com.knowlabel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Knowlabel Spring Boot application.
 */
@SpringBootApplication
final class Server {
    /**
     * Prevents instantiation of the application entry-point class.
     */
    private Server() {
    }

    /**
     * Starts the Knowlabel Backend Server.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(final String[] args) {
        SpringApplication.run(Server.class, args);
    }
}
