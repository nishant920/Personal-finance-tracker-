package com.personaltracker.finance.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    //These two methods are two different ways of telling Spring the same thing — they exist because they solve CORS at two different layers of the stack
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        /*
        addCorsMappings (via WebMvcConfigurer) — this is Spring MVC's CORS handling.
        It applies to requests as they're processed by Spring's web layer (controllers)
        */
        registry.addMapping("/api/**")
                .allowedOriginPatterns(
                        "https://6a6516e397faf3d8b680f0e4--clinquant-bubblegum-a0389d.netlify.app",
                        "https://*.netlify.app",
                        "http://localhost:*",
                        "http://127.0.0.1:*",
                        "http://16.171.253.225:8081"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
                "https://6a6516e397faf3d8b680f0e4--clinquant-bubblegum-a0389d.netlify.app",
                "https://*.netlify.app",
                "http://localhost:*",
                "http://127.0.0.1:*",
                "http://16.171.253.225:8081"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

/*
before actually sending this POST, first automatically sends a separate, preliminary OPTIONS request to the same URL —
this is called a "preflight request." It happens silently, behind the scenes; you never see it in your JavaScript code,
 but you'd see it in the Network tab of DevTools as its own separate entry.
 The browser asks the server, roughly: "I'm a page running at https://your-frontend.netlify.app,
 I want to send a POST request with headers Authorization and Content-Type to /api/transactions — are you okay with that?
* Without a CorsConfigurationSource bean wired into Security, when a cross-origin OPTIONS preflight request arrives:

It hits Spring Security's filter chain first
Security's rules say something like anyRequest().authenticated() for most routes
Security looks at this OPTIONS request — it has no JWT token (browsers never attach your auth token to a preflight request), and Security has no CORS awareness telling it "oh, this OPTIONS preflight is a special case, let it through so the browser can check permissions"
So Security treats it like any other unauthenticated request to a protected route → rejects it with 401, right there in its own filter chain
* */
