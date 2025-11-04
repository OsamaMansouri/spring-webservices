package com.example.Jaxrs.config;

import com.example.Jaxrs.controller.CategoryJaxrsApi;
import com.example.Jaxrs.controller.CompteJaxrsApi;
import com.example.Jaxrs.controller.ItemJaxrsApi;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyConfig {
    @Bean
    public ResourceConfig resourceConfig() {
        ResourceConfig jerseyServlet = new ResourceConfig();
        jerseyServlet.register(CompteJaxrsApi.class);
        jerseyServlet.register(CategoryJaxrsApi.class);
        jerseyServlet.register(ItemJaxrsApi.class);
        return jerseyServlet;
    }
}