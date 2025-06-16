package com.example.config;

import com.example.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ✅ Only one AuthenticationManager bean - clean & correct
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//            .csrf(csrf -> csrf.disable())
//            .authorizeHttpRequests(auth -> auth
//                .requestMatchers(
//                    "/", "/index.html", "/register.html", "/login.html",
//                    "/api/auth/register", "/api/auth/login",
//                    "/api/auth/forgot-password", "/api/auth/reset-password",
//                    "/api/auth/send-otp", "/api/auth/reset-password-with-otp",
//                    "/swagger-ui/**", "/v3/api-docs/**"
//                ).permitAll()
//                .anyRequest().authenticated()
//            )
//            .formLogin(form -> form
//                .loginPage("/login.html")
//                .permitAll()
//            )
//            .exceptionHandling(ex -> ex
//                .accessDeniedHandler(accessDeniedHandler)
//            )
//            .sessionManagement(session -> session
//                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)); // ← key change
//
//        return http.build();
//    }
//
//}



    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/index.html", "/register.html", "/login.html",
                    "/api/auth/register", "/api/auth/login",
                    "/api/auth/forgot-password", "/api/auth/reset-password",
                    "/api/auth/send-otp", "/api/auth/reset-password-with-otp",
                    "/swagger-ui/**", "/v3/api-docs/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login.html") // Set your actual login page
                .permitAll()
            )
            .exceptionHandling(exception -> exception
                .accessDeniedHandler(accessDeniedHandler)
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
    
}


