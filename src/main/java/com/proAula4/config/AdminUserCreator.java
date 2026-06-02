package com.proAula4.config;


import com.proAula4.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class AdminUserCreator {

    @Autowired
    private UsuarioService usuarioService;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            if (!usuarioService.existePorCorreo("UrbanFlair@admin.com")) {
                usuarioService.crearUsuarioAdmin(
                        "Urban",
                        "Flair",
                        "UrbanFlair@admin.com",
                        "admin123"
                );
            }
        };
    }
}