package com.proAula4.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proAula4.model.DetalleVenta;
import com.proAula4.model.Producto;
import com.proAula4.model.Venta;
import com.proAula4.services.ProductoService;
import com.proAula4.services.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class VentaController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private VentaService ventaService;

    @GetMapping("/usuario/registrar-venta")
    public String registrarVenta(Model model) {
        model.addAttribute("titulo", "Registrar Venta");
        model.addAttribute("productosDisponibles", productoService.listarTodos());
        return "usuario/registrar-venta";
    }

    @PostMapping("/guardarVenta")
    public String guardarVenta(
            @RequestParam("ventaData") String ventaDataJson,
            Authentication auth,
            RedirectAttributes redirectAttributes
    ) {
        if (ventaDataJson == null || ventaDataJson.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("mensaje", "No se recibieron datos de la venta.");
            redirectAttributes.addFlashAttribute("alerta", "danger");
            return "redirect:/usuario/registrar-venta";
        }

        ObjectMapper mapper = new ObjectMapper();
        Venta venta;

        try {
            venta = mapper.readValue(ventaDataJson, Venta.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("mensaje", "Error al procesar los datos JSON de la venta.");
            redirectAttributes.addFlashAttribute("alerta", "danger");
            return "redirect:/usuario/registrar-venta";
        }

        List<DetalleVenta> detalles = venta.getDetallesVenta();

        if (detalles == null || detalles.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensaje", "La venta no contiene detalles de productos.");
            redirectAttributes.addFlashAttribute("alerta", "danger");
            return "redirect:/usuario/registrar-venta";
        }

        for (DetalleVenta det : detalles) {
            Producto producto = productoService.obtenerPorId(det.getIdProducto());

            if (producto == null) {
                redirectAttributes.addFlashAttribute("mensaje", "Producto no encontrado (id: " + det.getIdProducto() + ").");
                redirectAttributes.addFlashAttribute("alerta", "danger");
                return "redirect:/usuario/registrar-venta";
            }

            if (producto.getStock() < det.getCantidad()) {
                redirectAttributes.addFlashAttribute("mensaje", "Stock insuficiente para el producto: " + producto.getDescripcion());
                redirectAttributes.addFlashAttribute("alerta", "danger");
                return "redirect:/usuario/registrar-venta";
            }

            double precioCompra = producto.getValorProdu();
            double porcentaje = producto.getPorcentajeGanancia() == null ? 0.0 : producto.getPorcentajeGanancia();
            double precioVenta = precioCompra * (1.0 + porcentaje / 100.0);

            det.setPrecioUnitario(precioVenta);
            det.setSubtotal(precioVenta * (det.getCantidad() == null ? 0 : det.getCantidad()));

            // 🔥 SOLUCIÓN CLAVE: guardar el nombre del producto
            det.setNombreProducto(producto.getDescripcion());
        }

        venta.recalcularTotal();
        String registradoPor;
        if (auth.getPrincipal() instanceof OAuth2User oauthUser) {
            registradoPor = oauthUser.getAttribute("email");
        } else {
            registradoPor = auth.getName();
        }
        venta.setRegistradoPor(registradoPor);

        try {
            ventaService.guardarVenta(venta);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al guardar la venta: " + e.getMessage());
            redirectAttributes.addFlashAttribute("alerta", "danger");
            return "redirect:/usuario/registrar-venta";
        }

        redirectAttributes.addFlashAttribute("mensaje", "Venta registrada exitosamente.");
        redirectAttributes.addFlashAttribute("alerta", "success");

        return "redirect:/usuario/registrar-venta";
    }
}