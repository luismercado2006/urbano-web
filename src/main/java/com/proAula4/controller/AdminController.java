package com.proAula4.controller;

import com.proAula4.model.Usuario;
import com.proAula4.model.Proveedor;
import com.proAula4.services.UsuarioService;
import com.proAula4.services.ProveedorService;
import com.proAula4.services.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.Optional;
import java.util.List;

@Controller
public class AdminController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ProveedorService proveedorService;

    @Autowired
    private VentaService ventaService;

    @GetMapping("/Admin")
    public String adminDashboard(Model model) {
        model.addAttribute("titulo", "Panel de Administración");
        return "admin/page-admin";
    }

    @GetMapping("/admin/listar-usuarios")
    public String listarUsuariosYProveedores(Model model) {
        model.addAttribute("listaUsuarios", usuarioService.listarTodos());
        model.addAttribute("listaProveedores", proveedorService.listarProveedores());
        model.addAttribute("titulo", "Lista de Usuarios y Proveedores");
        return "admin/listar-usuarios";
    }

    // --- Control de Usuarios ---

    @GetMapping("/admin/usuarios/editar")
    public String editarUsuario(@RequestParam("id") String id, Model model, RedirectAttributes redirect) {
        Usuario usuario = usuarioService.obtenerUsuarioPorId(id);

        if (usuario == null) {
            redirect.addFlashAttribute("error", "El usuario no existe.");
            return "redirect:/admin/listar-usuarios";
        }

        // Manejo de NullPointer: Si getRoles() es null, se usa una lista vacía para la comprobación.
        boolean esAdmin = Optional.ofNullable(usuario.getRoles())
                .orElseGet(Collections::emptyList)
                .stream()
                .anyMatch(r -> r.getNombre().equalsIgnoreCase("ADMIN"));

        if (esAdmin) {
            redirect.addFlashAttribute("error", "No puedes editar a un Administrador.");
            return "redirect:/admin/listar-usuarios";
        }

        model.addAttribute("usuario", usuario);
        return "admin/usuario-editar";
    }

    @PostMapping("/admin/usuarios/guardar")
    public String guardarUsuarioEditado(@ModelAttribute Usuario usuario, RedirectAttributes redirect) {
        // El servicio se encarga de cargar los roles existentes para evitar la pérdida (Preservación de Roles).
        usuarioService.guardarUsuario(usuario);

        redirect.addAttribute("editSuccess", "Usuario");
        return "redirect:/admin/listar-usuarios";
    }

    @GetMapping("/admin/usuarios/eliminar")
    public String eliminarUsuario(@RequestParam("id") String id, RedirectAttributes redirect) {
        Usuario usuario = usuarioService.obtenerUsuarioPorId(id);

        if (usuario == null) {
            redirect.addFlashAttribute("error", "El usuario no existe.");
            return "redirect:/admin/listar-usuarios";
        }

        // Manejo de NullPointer: Si getRoles() es null, se usa una lista vacía para la comprobación.
        boolean esAdmin = Optional.ofNullable(usuario.getRoles())
                .orElseGet(Collections::emptyList)
                .stream()
                .anyMatch(r -> r.getNombre().equalsIgnoreCase("ADMIN"));

        if (esAdmin) {
            redirect.addFlashAttribute("error", "No puedes eliminar un Administrador.");
            return "redirect:/admin/listar-usuarios";
        }

        usuarioService.eliminar(id);

        redirect.addAttribute("deleteSuccess", "Usuario");
        return "redirect:/admin/listar-usuarios";
    }

    // --- Control de Proveedores ---

    @GetMapping("/admin/proveedores/editar")
    public String editarProveedor(@RequestParam("id") String id, Model model, RedirectAttributes redirect) {
        Proveedor proveedor = proveedorService.obtenerProveedorPorId(id);
        if (proveedor == null) {
            redirect.addFlashAttribute("error", "El proveedor no existe.");
            return "redirect:/admin/listar-usuarios";
        }
        model.addAttribute("proveedor", proveedor);
        return "admin/proveedor-editar";
    }

    @PostMapping("/admin/proveedores/guardar")
    public String guardarProveedor(@ModelAttribute Proveedor proveedor, RedirectAttributes redirect) {
        proveedorService.guardarProveedor(proveedor);

        redirect.addAttribute("editSuccess", "Proveedor");
        return "redirect:/admin/listar-usuarios";
    }

    @GetMapping("/admin/proveedores/eliminar")
    public String eliminarProveedor(@RequestParam("id") String id, RedirectAttributes redirect) {
        Proveedor proveedor = proveedorService.obtenerProveedorPorId(id);
        if (proveedor == null) {
            redirect.addFlashAttribute("error", "El proveedor no existe.");
            return "redirect:/admin/listar-usuarios";
        }
        proveedorService.eliminar(id);

        redirect.addAttribute("deleteSuccess", "Proveedor");
        return "redirect:/admin/listar-usuarios";
    }

    @GetMapping("/admin/historial-ventas")
    public String historialVentas(Model model) {
        model.addAttribute("titulo", "Historial de Ventas");

        model.addAttribute("ventas", ventaService.listarTodas());
        return "admin/historial-ventas";
    }
}