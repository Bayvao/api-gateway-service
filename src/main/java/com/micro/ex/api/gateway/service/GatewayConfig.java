package com.micro.ex.api.gateway.service;

import java.util.Date;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder routeBuilder) {
		return routeBuilder
				.routes().route(r -> r
						.path("/user-service/v1/users/status/check")
						.and().header("Authorization", "Bearer .*")
						.filters(p -> p.rewritePath("/user-service/?(?<segment>.*)", "/${segment}")
									.addResponseHeader("X-Response-Time", new Date().toString())
									.removeRequestHeader("Cookie"))
						.uri("lb://USER-SERVICE"))
				.build();
	}

}
