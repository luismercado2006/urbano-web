package com.proAula4.controller;

import com.proAula4.services.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class PrediccionesController {

    @Autowired
    private VentaService ventaService;

    @GetMapping("/admin/predicciones")
    public String mostrarPredicciones() {
        return "predicciones";
    }

    @GetMapping("/api/predicciones/ventas")
    @ResponseBody
    public ResponseEntity<?> obtenerProyeccionVenta(
            @RequestParam int anio,
            @RequestParam int mes) {

        if (mes < 1 || mes > 12) {
            return ResponseEntity.badRequest().body("El valor del mes debe estar entre 1 y 12.");
        }

        try {
            double ventaPredicha = ventaService.predecirVentaMensual(anio, mes);

            Map<String, Object> response = new HashMap<>();
            response.put("anio", anio);
            response.put("mes", mes);
            response.put("prediccion", ventaPredicha); // ← CORREGIDO
            response.put("mensaje", "Proyección exitosa");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al predecir: " + e.getMessage());
        }
    }
}
