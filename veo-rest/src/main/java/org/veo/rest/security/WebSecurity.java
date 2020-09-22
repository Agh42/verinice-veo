/*******************************************************************************
 * Copyright (c) 2018 Alexander Ben Nasrallah.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.veo.rest.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * This class bundles custom API security configurations.
 */
@EnableWebSecurity
public class WebSecurity extends WebSecurityConfigurerAdapter {

    @Value("${veo.cors.origins}")
    private List<String> origins;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        // Keep the formatter off or you will never understand what this does.
        // #whitespacematters #pythonmeetsjava.
        http.csrf()
                .disable()
            .cors()

            // Anonymous access (a user with role "ROLE_ANONYMOUS" must be enabled for
            // swagger-ui. We cannot disable it.
            // Make sure that no critical API can be accessed by an anonymous user!
            // .anonymous()
            //     .disable()

            .and()
            .authorizeRequests()
            .antMatchers("/actuator/**")
            .permitAll()

            .and()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

            .and()
            .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/", "/v2/api-docs/**", "/v3/api-docs/**", "/swagger.json",
                         "/swagger-ui.html", "/swagger-resources/**", "/webjars/**",
                         "/swagger-ui/**")
                    .permitAll()
                .antMatchers("/units/**", "/assets/**", "/controls/**", "/groups/**", "persons/**",
                             "/processes/**", "/schemas/**", "/translations/**")
                    .hasAuthority("SCOPE_veo-user")
                .anyRequest()
                    .authenticated() // CAUTION: this includes anonymous users, see above

            .and()
            .oauth2ResourceServer()
                .jwt();

        // @formatter:on
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(inMemoryUserDetailsManager());
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfig = new CorsConfiguration();
        origins.forEach(corsConfig::addAllowedOrigin);
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }

    @Bean
    public InMemoryUserDetailsManager inMemoryUserDetailsManager() {
        final String NIL_UUID = "00000000-0000-0000-0000-000000000000";

        ApplicationUser basicUser = ApplicationUser.authenticatedUser("user", NIL_UUID, "veo-user");
        basicUser.setAuthorities(List.of(new SimpleGrantedAuthority("SCOPE_veo-user")));

        ApplicationUser adminUser = ApplicationUser.authenticatedUser("admin", NIL_UUID,
                                                                      "veo-admin");
        adminUser.setAuthorities(List.of(new SimpleGrantedAuthority("SCOPE_veo-admin")));

        return new CustomUserDetailsManager(List.of(basicUser, adminUser));
    }
}