package edu.ucr.cs.cs226.GameScout;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "edu.ucr.cs.cs226.GameScout")
public class GameScoutApplication {

	public static void main(String[] args) {
		SpringApplication.run(GameScoutApplication.class, args);
	}

}
