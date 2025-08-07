package com.school.sim.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security Configuration with JWT authentication and role-based access control
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
                // Exception handling
                .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .and()
                
                // Session management - stateless
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                
                // Authorization rules
                .authorizeRequests()
                
                // Public endpoints
                .antMatchers("/api/v1/auth/**").permitAll()
                .antMatchers("/api/v1/health/**").permitAll()
                .antMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
                .antMatchers("/actuator/health", "/actuator/info").permitAll()
                
                // Admin only endpoints
                .antMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .antMatchers("/actuator/**").hasRole("ADMIN")
                
                // Student management - Admin and Teacher access
                .antMatchers("/api/v1/students/**").hasAnyRole("ADMIN", "TEACHER")
                
                // Attendance management - Admin and Teacher access
                .antMatchers("/api/v1/attendance/**").hasAnyRole("ADMIN", "TEACHER")
                
                // Assessment management - Admin and Teacher access
                .antMatchers("/api/v1/assessments/**").hasAnyRole("ADMIN", "TEACHER")
                
                // Schedule management - Admin and Teacher access
                .antMatchers("/api/v1/schedules/**").hasAnyRole("ADMIN", "TEACHER")
                
                // Extracurricular management - Admin and Teacher access
                .antMatchers("/api/v1/extracurricular/**").hasAnyRole("ADMIN", "TEACHER")
                
                // Reports - Admin and Teacher access
                .antMatchers("/api/v1/reports/**").hasAnyRole("ADMIN", "TEACHER")
                
                // User profile - authenticated users
                .antMatchers("/api/v1/profile/**").authenticated()
                
                // All other requests require authentication
                .anyRequest().authenticated();

        // Add JWT filter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
