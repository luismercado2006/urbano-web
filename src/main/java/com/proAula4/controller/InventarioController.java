package com.proAula4.controller;


import com.proAula4.services.ProductoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InventarioController {

    private final ProductoService productoService;

    public InventarioController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping("/usuario/inventario")
    public String verInventario(Model model) {

        model.addAttribute("titulo", "Inventario de Productos");

        model.addAttribute("productos", productoService.listarProductosConProveedor());

        return "usuario/inventario";
    }
}
