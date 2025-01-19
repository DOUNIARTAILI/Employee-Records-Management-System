package com.drtaili.security;

import com.drtaili.security.auth.AuthenticationService;
import com.drtaili.security.auth.RegisterRequest;
import com.drtaili.security.user.Role;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class   SecurityApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecurityApplication.class, args);
	}
	@Bean
	public CommandLineRunner commandLineRunner (
			AuthenticationService service
	) {
		return args -> {
			var admin = RegisterRequest.builder()
					.firstname("admin")
					.lastname("admin")
					.email("admin@mail.com")
					.password("admin")
					.role(Role.ADMIN)
					.build();
			System.out.println("Admin token: " + service.register(admin).getToken());

			var manager = RegisterRequest.builder()
					.firstname("manager")
					.lastname("manager")
					.email("manager@mail.com")
					.password("manager")
					.role(Role.MANAGER)
					.build();
			System.out.println("manager token: " + service.register(manager).getToken());

			var rh = RegisterRequest.builder()
					.firstname("HR")
					.lastname("HR")
					.email("HR@mail.com")
					.password("HR")
					.role(Role.HR)
					.build();
			System.out.println("HR token: " + service.register(rh).getToken());

		};
	}
}
