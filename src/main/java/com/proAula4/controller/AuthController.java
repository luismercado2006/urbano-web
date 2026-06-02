package com.proAula4.controller;

import com.proAula4.model.Rol;
import com.proAula4.model.Usuario;
import com.proAula4.repository.RolRepository;
import com.proAula4.repository.UsuarioRepository;
import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;


@Controller
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository; // ✅ Añadir esto

    @Autowired
    private PasswordEncoder passwordEncoder;


    @GetMapping("/")
    public String login1() {
        return "homePage";
    }

    @GetMapping("/login")
    public String login2() {
        return "login";
    }

    @GetMapping("/registro")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrarUsuario(@Valid @ModelAttribute("usuario") Usuario usuario,
                                   BindingResult result,
                                   Model model) {
        if (result.hasErrors()) {
            return "registro";
        }

        if (usuarioRepository.existsByCorreo(usuario.getCorreo())) {
            model.addAttribute("error", "El correo ya está registrado");
            return "registro";
        }

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        // ✅ Buscar el rol en BD; si no existe, crearlo y guardarlo
        Rol rolUsuario = rolRepository.findByNombre("USER")
                .orElseGet(() -> rolRepository.save(new Rol("USER")));

        usuario.setRoles(Arrays.asList(rolUsuario));

        usuarioRepository.save(usuario);

        return "redirect:/login?registroExitoso";
    }
}