package com.proAula4.controller;

import com.proAula4.model.Producto;
import com.proAula4.services.ProductoService;
import com.proAula4.services.ProveedorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;

@Controller
public class ProductoController {

    private final ProductoService productoService;
    private final ProveedorService proveedorService;

    public ProductoController(ProductoService productoService, ProveedorService proveedorService) {
        this.productoService = productoService;
        this.proveedorService = proveedorService;
    }


    @GetMapping("/usuario/registrar-producto")
    public String mostrarFormulario(Model model) {
        model.addAttribute("titulo", "Registrar Producto");
        model.addAttribute("producto", new Producto());
        model.addAttribute("proveedores", proveedorService.listarProveedores());
        return "usuario/registrar-producto";
    }


    @PostMapping("/usuario/producto/guardar")
    public String guardarProducto(Producto producto, Model model, Principal principal) {


        if (principal != null) {
            producto.setRegistradoPor(principal.getName());
        }

        productoService.guardarProducto(producto);
        model.addAttribute("producto", new Producto());
        model.addAttribute("proveedores", proveedorService.listarProveedores());
        model.addAttribute("mensaje", "Producto registrado con éxito 🎉");
        return "usuario/registrar-producto";
    }


}
