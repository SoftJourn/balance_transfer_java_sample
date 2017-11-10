package com.softjourn.balance.transfer.client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

import java.util.ArrayList;

@Configuration
public class WebSecurityConfigurer {

    @Configuration
    @EnableWebSecurity
    protected static class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.authenticationProvider(new AuthenticationProvider() {
                @Override
                public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                    String username = authentication.getName();
                    String password = authentication.getCredentials().toString();
                    if ("admin".equals(username) && "admin".equals(password)) {
                        return new UsernamePasswordAuthenticationToken(username, password, new ArrayList<GrantedAuthority>() {{
                            add((GrantedAuthority) () -> "ROLE_ADMIN");
                        }});
                    } else {
                        throw new
                                BadCredentialsException("External system authentication failed");
                    }
                }

                @Override
                public boolean supports(Class<?> authentication) {
                    return true;
                }
            });
        }

        @Bean
        @Override
        public AuthenticationManager authenticationManagerBean() throws Exception {
            return super.authenticationManagerBean();
        }
    }

    @Configuration
    @EnableResourceServer
    protected static class ResourceServer extends ResourceServerConfigurerAdapter {

        @Override
        public void configure(HttpSecurity http) throws Exception {
            // @formatter:off
            http.csrf().disable();
            http.authorizeRequests()
                    .antMatchers("/oauth/token").permitAll()
                    .antMatchers("/*").authenticated();
            // @formatter:on
        }
    }

}