package com.proAula4.security;

import com.proAula4.model.Rol;
import com.proAula4.model.Usuario;
import com.proAula4.repository.RolRepository;
import com.proAula4.repository.UsuarioRepository;
import com.proAula4.services.EmailService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class GoogleOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository; // ✅ Añadir esto

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        String correo   = oauthUser.getAttribute("email");
        String nombre   = oauthUser.getAttribute("given_name");
        String apellido = oauthUser.getAttribute("family_name");

        if (apellido == null || apellido.isBlank()) {
            apellido = "-";
        }

        if (!usuarioRepository.existsByCorreo(correo)) {
            // ✅ Buscar el rol en BD; si no existe, crearlo y guardarlo
            Rol rolUser = rolRepository.findByNombre("USER")
                    .orElseGet(() -> rolRepository.save(new Rol("USER")));

            Usuario nuevo = new Usuario();
            nuevo.setNombre(nombre);
            nuevo.setApellido(apellido);
            nuevo.setCorreo(correo);
            // Sustituye el UUID por una clave real
            nuevo.setPassword(passwordEncoder.encode("admin123"));
            nuevo.setRoles(Arrays.asList(rolUser)); // ✅ Rol ya persistido
            usuarioRepository.save(nuevo);
        }

        // Enviar correo de notificación
        try {
            emailService.enviarNotificacionAcceso(correo, nombre != null ? nombre : correo);
        } catch (Exception e) {
            System.err.println(">>> Error enviando correo de acceso: " + e.getMessage());
        }

        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        if (roles.contains("ROLE_ADMIN")) {
            response.sendRedirect("/Admin");
        } else {
            response.sendRedirect("/usuario");
        }
    }
}