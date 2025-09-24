package com.siam.mpl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class MplApplication {

	public static void main(String[] args) {
		SpringApplication.run(MplApplication.class, args);
	}

}
