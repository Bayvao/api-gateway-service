package com.micro.ex.api.gateway.service.filters;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

@Component
public class AuthorizationFilter extends AbstractGatewayFilterFactory<AuthorizationFilter.Config> {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationFilter.class);

	@Autowired
	private Environment environment;

	public AuthorizationFilter() {
		super(Config.class);
	}

	public static class Config {
		// put custom configuration properties here
	}

	@Override
	public GatewayFilter apply(Config config) {

		return (exchange, chain) -> {

			ServerHttpRequest request = exchange.getRequest();
			if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
				return onError(exchange, "No authorization header found", HttpStatus.UNAUTHORIZED);
			}

			String authorizationHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
			String jwtToken = authorizationHeader.replace("Bearer", "");
			if (!isValidJwtToken(jwtToken)) {
				return onError(exchange, "Jwt Token is not valid", HttpStatus.UNAUTHORIZED);
			}
			return chain.filter(exchange);
		};
	}

	private Mono<Void> onError(ServerWebExchange exchange, String errorDesc, HttpStatus status) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(status);

		return response.setComplete();
	}

	private boolean isValidJwtToken(String jwtToken) {
		boolean returnValue = true;
		String subject = null;
		try {
			SecretKey key = Keys.hmacShaKeyFor(environment.getRequiredProperty("token.secret").getBytes());
			JwtParser jwtParser = Jwts.parserBuilder().setSigningKey(key).build();
			subject = jwtParser.parseClaimsJws(jwtToken).getBody().getSubject();
		} catch (Exception e) {
			LOGGER.debug("error while pasring jwtToken ", e);
			returnValue = false;
		}
		if (!StringUtils.hasText(subject)) {
			returnValue = false;
		}
		return returnValue;
	}
}
