package org.glytoucan.api.security;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
    	http.csrf().disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    	.and().authenticationProvider( new UserAuthenticationProvider())
                .authorizeRequests()
                .antMatchers(GET, "/").permitAll()
                .antMatchers(GET, "/login").permitAll()
                .antMatchers(GET, "/Structures/**").permitAll()
                .antMatchers(POST, "/Structures/**").permitAll()
                .antMatchers(POST, "/glycans/**").hasRole("USER")
                .antMatchers(GET, "/glycans/**").hasRole("USER")
                .antMatchers(GET, "/test").hasRole("USER")
                .antMatchers(GET, "/user").hasRole("ADMIN")
                .antMatchers(POST, "/Registries/**").hasAuthority("ROLE_USER")
                .anyRequest().authenticated()
    			.and().httpBasic()
    			.and().logout()
		.logoutRequestMatcher(new AntPathRequestMatcher("/logout"));
    }
}
