package org.glytoucan.api.security;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

import org.glycoinfo.rdf.SparqlException;
import org.glycoinfo.rdf.dao.virt.VirtSesameTransactionConfig;
import org.glycoinfo.rdf.service.impl.GlycanProcedureConfig;
import org.glycoinfo.rdf.service.impl.UserProcedure;
import org.glycoinfo.rdf.service.impl.UserProcedureConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@Order(1)
@EnableWebSecurity
@Import(value = { UserProcedureConfig.class })
@ComponentScan(basePackages="org.glycoinfo.rdf.service.impl")
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	
	@Bean
	AuthenticationProvider authenticationProvider() {
		return new UserAuthenticationProvider();
	}

    @Override
    protected void configure(HttpSecurity http) throws Exception {
    	http.csrf().disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    	.and().authenticationProvider(authenticationProvider())
                .authorizeRequests()
                .antMatchers(GET, "/").permitAll()
                .antMatchers(GET, "/documentation/apidoc.html").permitAll() // for the paper
                .antMatchers(GET, "/swagger-ui.html").permitAll()
                .antMatchers(GET, "/webjars/springfox-swagger-ui/**").permitAll()
                .antMatchers(GET, "/v2/api-docs/**").permitAll()
                .antMatchers(GET, "/configuration/**").permitAll()
                .antMatchers(GET, "/swagger-resources/**").permitAll()
                .antMatchers(GET, "/glycans/**").permitAll()
                .antMatchers(POST, "/glycan/**").hasRole("USER")
                .antMatchers(POST, "/Registries/**").hasAuthority("ROLE_USER")
                .anyRequest().authenticated()
    			.and().httpBasic()
    			.and().logout()
		.logoutRequestMatcher(new AntPathRequestMatcher("/logout"));
    }
}
