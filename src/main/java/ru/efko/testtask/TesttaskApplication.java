package ru.efko.testtask;

import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TesttaskApplication {

	@SneakyThrows
	public static void main(String[] args) {
		SpringApplication.run(TesttaskApplication.class, args);
	}

}
