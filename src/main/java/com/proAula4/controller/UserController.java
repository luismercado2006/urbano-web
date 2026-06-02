package com.proAula4.controller;

import com.proAula4.model.Usuario;
import com.proAula4.services.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import com.proAula4.repository.UsuarioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private final DashboardService dashboardService;

    public UserController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/usuario")
    public String dashboard(Model model, Authentication auth) {

        // Obtener nombre según tipo de login
        String nombreMostrar = "Usuario";

        if (auth != null) {
            if (auth.getPrincipal() instanceof OAuth2User oauthUser) {
                // Login con Google
                String nombre = oauthUser.getAttribute("given_name");
                String apellido = oauthUser.getAttribute("family_name");
                nombreMostrar = nombre + (apellido != null ? " " + apellido : "");
            } else {
                // Login normal - buscar por correo en BD
                Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
                if (usuario != null) {
                    nombreMostrar = usuario.getNombre() + " " + usuario.getApellido();
                }
            }
        }

        model.addAttribute("nombreUsuario", nombreMostrar);
        model.addAttribute("productos", dashboardService.contarProductos());
        model.addAttribute("proveedores", dashboardService.contarProveedores());
        model.addAttribute("ventasMes", dashboardService.ventasDelMes());
        model.addAttribute("stockDisponible", dashboardService.contarStockDisponible());

        return "usuario/home";
    }

    @GetMapping("/usuario/perfil")
    public String perfil(Model model, Authentication auth) {

        String correo;
        if (auth.getPrincipal() instanceof OAuth2User oauthUser) {
            correo = oauthUser.getAttribute("email");
        } else {
            correo = auth.getName();
        }

        Usuario usuario = usuarioRepository.findByCorreo(correo);

        model.addAttribute("usuario", usuario);
        model.addAttribute("titulo", "Perfil de Usuario");

        return "Usuario/perfil";
    }
}