package ru.practicum.explorewithme;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServerApplication {
    public static void main(String[] args) {
        System.setProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/postgres");
        System.setProperty("spring.datasource.username", "postgres");
        System.setProperty("spring.datasource.password", "1234");
        System.setProperty("spring.datasource.driver-class-name", "org.postgresql.Driver");

        SpringApplication.run(ServerApplication.class, args);
    }
}
