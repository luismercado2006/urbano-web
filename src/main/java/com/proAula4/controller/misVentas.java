package com.proAula4.controller;

import com.proAula4.model.Producto;
import com.proAula4.model.Venta;
import com.proAula4.repository.ProductoRepository;
import com.proAula4.services.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class misVentas {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private ProductoRepository productoRepository;



    @GetMapping("/usuario/mis-ventas")
    public String misVentas(Model model, Authentication auth) {

        String usuario;
        if (auth.getPrincipal() instanceof OAuth2User oauthUser) {
            usuario = oauthUser.getAttribute("email");
        } else {
            usuario = auth.getName();
        }

        List<Venta> ventas = ventaService.listarPorUsuario(usuario);

        model.addAttribute("titulo", "Mis Ventas");
        model.addAttribute("ventas", ventas);

        return "usuario/mis-ventas";
    }
}