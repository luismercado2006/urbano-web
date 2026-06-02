package com.proAula4.controller;

import com.proAula4.model.Proveedor;
import com.proAula4.services.ProveedorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ProveedorController {

    private final ProveedorService proveedorService;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }


    //  MOSTRAR FORMULARI

    @GetMapping("/usuario/registrar-proveedor")
    public String mostrarFormularioProveedor(Model model) {
        model.addAttribute("titulo", "Registrar Proveedor");
        model.addAttribute("proveedor", new Proveedor());
        return "usuario/registrar-proveedor";
    }


    //  GURDAR PROVEDOR

    @PostMapping("/usuario/proveedor/guardar")
    public String guardarProveedor(Proveedor proveedor, Model model) {

        proveedorService.guardarProveedor(proveedor);

        model.addAttribute("mensaje", "Proveedor registrado correctamente");
        model.addAttribute("proveedor", new Proveedor());
        model.addAttribute("titulo", "Registrar Proveedor");

        return "usuario/registrar-proveedor";
    }


}
