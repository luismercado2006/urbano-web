package com.proAula4.controller;

import com.proAula4.model.Producto;
import com.proAula4.services.ProductoService;
import com.proAula4.services.ProveedorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal; // Importación necesaria

@Controller
@RequestMapping("/admin")
public class InventarioAdminController {

    private final ProductoService productoService;
    private final ProveedorService proveedorService;

    public InventarioAdminController(ProductoService productoService, ProveedorService proveedorService) {
        this.productoService = productoService;
        this.proveedorService = proveedorService;
    }

    // ===============================================
    // 1. INVENTARIO (Ver tabla y Stock)
    // ===============================================

    /**
     * Muestra la tabla de inventario para el Administrador.
     */
    @GetMapping("/inventario")
    public String verInventario(Model model) {
        model.addAttribute("titulo", "Inventario y Gestión de Productos");
        model.addAttribute("productos", productoService.listarProductosConProveedor());
        return "admin/inventario";
    }

    /**
     * Procesa la actualización de stock (solo aumento).
     */
    @PostMapping("/inventario/actualizar-stock")
    public String actualizarStock(
            @RequestParam("idProducto") String idProducto,
            @RequestParam("cantidadAdicional") int cantidadAdicional,
            RedirectAttributes redirectAttributes) {

        if (cantidadAdicional <= 0) {
            redirectAttributes.addFlashAttribute("mensaje", "La cantidad de compra debe ser positiva.");
            redirectAttributes.addFlashAttribute("alerta", "danger");
            return "redirect:/admin/inventario";
        }

        try {
            productoService.actualizarStock(idProducto, cantidadAdicional);
            redirectAttributes.addFlashAttribute("mensaje", "Stock de producto actualizado exitosamente!");
            redirectAttributes.addFlashAttribute("alerta", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al actualizar el stock: " + e.getMessage());
            redirectAttributes.addFlashAttribute("alerta", "danger");
        }
        return "redirect:/admin/inventario";
    }

    // ===============================================
    // 2. REGISTRO DE NUEVOS PRODUCTOS (Inicio)
    // ===============================================

    /**
     * Muestra el formulario para registrar un nuevo producto.
     */
    @GetMapping("/registrar-producto")
    public String registrarProducto(Model model) {
        model.addAttribute("titulo", "Registrar Nuevo Producto");
        model.addAttribute("producto", new Producto());
        model.addAttribute("proveedores", proveedorService.listarProveedores());
        return "admin/registrar-producto.html";
    }

    /**
     * Procesa el guardado de un producto (Nuevo).
     * NOTA: Falta agregar Principal aquí si deseas registrar quién creó el producto.
     */
    @PostMapping("/registrar-producto")
    public String guardarNuevoProducto(@ModelAttribute("producto") Producto producto, RedirectAttributes redirectAttributes, Principal principal) {

        // 🔑 CORRECCIÓN: Asignar el usuario que registra el nuevo producto
        if (principal != null) {
            producto.setRegistradoPor(principal.getName());
        }

        try {
            productoService.guardarProducto(producto);
            redirectAttributes.addFlashAttribute("mensaje", "Producto registrado exitosamente!");
            redirectAttributes.addFlashAttribute("alerta", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al registrar el producto: " + e.getMessage());
            redirectAttributes.addFlashAttribute("alerta", "danger");
            return "redirect:/admin/registrar-producto";
        }
        return "redirect:/admin/inventario";
    }

    // ===============================================
    // 3. EDICIÓN COMPLETA DE PRODUCTOS
    // ===============================================

    /**
     * Muestra el formulario para editar un producto.
     */
    @GetMapping("/inventario/editar/{id}")
    public String editarProducto(@PathVariable("id") String idProducto, Model model, RedirectAttributes redirectAttributes) {
        try {
            Producto producto = productoService.obtenerPorId(idProducto);
            model.addAttribute("titulo", "Editar Producto");
            model.addAttribute("producto", producto);
            model.addAttribute("proveedores", proveedorService.listarProveedores());
            return "admin/editar-producto";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("mensaje", "Producto no encontrado.");
            redirectAttributes.addFlashAttribute("alerta", "danger");
            return "redirect:/admin/inventario";
        }
    }

    /**
     * Procesa la edición completa del producto.
     * 🔑 CORRECCIÓN PRINCIPAL: Se agrega Principal para asignar 'registradoPor' y evitar el error de validación.
     */
    @PostMapping("/inventario/guardar-edicion")
    public String guardarEdicionProducto(@ModelAttribute("producto") Producto producto, RedirectAttributes redirectAttributes, Principal principal) {

        // 🔑 CORRECCIÓN: Asignar el usuario para satisfacer la validación
        if (principal != null) {
            producto.setRegistradoPor(principal.getName());
        } else {
            redirectAttributes.addFlashAttribute("mensaje", "Error de seguridad: No se pudo identificar al usuario.");
            redirectAttributes.addFlashAttribute("alerta", "danger");
            return "redirect:/admin/inventario";
        }

        try {
            productoService.guardarProducto(producto);
            redirectAttributes.addFlashAttribute("mensaje", "Producto editado exitosamente!");
            redirectAttributes.addFlashAttribute("alerta", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al editar el producto: " + e.getMessage());
            redirectAttributes.addFlashAttribute("alerta", "danger");
        }
        return "redirect:/admin/inventario";
    }
}