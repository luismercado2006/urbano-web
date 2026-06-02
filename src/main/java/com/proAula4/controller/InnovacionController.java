package com.proAula4.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class InnovacionController {

    /**
     * Muestra la página HTML del módulo de Innovación de Operaciones.
     */
    @GetMapping("/admin/innovacion-operaciones")
    public String mostrarInnovacion() {
        return "innovacion-operaciones";
    }

    /**
     * API REST: recibe las dos variables de decisión del modelo Simplex,
     * ejecuta el script Python y devuelve la gráfica (base64) + resultados
     * de optimización como JSON.
     *
     * Modelo Urban Flair — Maximizar Z = 25,000·x1 + 40,000·x2
     *
     * @param x1     Unidades de camisetas a vender por mes (variable de decisión 1)
     * @param x2     Unidades de pantalones a vender por mes (variable de decisión 2)
     * @param x1Max  Capacidad total mensual de ventas — Restricción R1: x1 + x2 <= x1Max
     * @param x2Max  Máximo de pantalones por política comercial — Restricción R2: x2 <= x2Max
     */
    @GetMapping("/api/innovacion/optimizar")
    @ResponseBody
    public ResponseEntity<?> optimizar(
            @RequestParam(defaultValue = "200") double x1,
            @RequestParam(defaultValue = "100") double x2,
            @RequestParam(defaultValue = "500") double x1Max,
            @RequestParam(defaultValue = "300") double x2Max) {

        // Validaciones básicas
        if (x1 < 0 || x1 > x1Max) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "x1 (camisetas) debe estar entre 0 y " + (int) x1Max));
        }
        if (x2 < 0 || x2 > x2Max) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "x2 (pantalones) debe estar entre 0 y " + (int) x2Max));
        }
        if (x1 + x2 > x1Max) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "x1 + x2 (" + (int)(x1 + x2) + ") supera la capacidad total (" + (int) x1Max + ")"));
        }

        try {
            // ── Extraer el script desde el classpath a un archivo temporal ──
            // Funciona tanto en desarrollo (IntelliJ) como en el JAR empaquetado.
            InputStream scriptStream = getClass().getClassLoader()
                    .getResourceAsStream("scripts/optimizar_operaciones.py");

            if (scriptStream == null) {
                return ResponseEntity.internalServerError()
                        .body(Map.of("error", "No se encontró el script Python en el classpath (scripts/optimizar_operaciones.py)"));
            }

            Path tempScript = Files.createTempFile("optimizar_operaciones_", ".py");
            Files.copy(scriptStream, tempScript, StandardCopyOption.REPLACE_EXISTING);
            tempScript.toFile().deleteOnExit();

            // ── Ejecutar Python con los 4 parámetros ──
            ProcessBuilder pb = new ProcessBuilder(
                    "python", tempScript.toAbsolutePath().toString(),
                    String.valueOf(x1), String.valueOf(x2),
                    String.valueOf(x1Max), String.valueOf(x2Max)
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            String output = new BufferedReader(new InputStreamReader(process.getInputStream()))
                    .lines()
                    .collect(Collectors.joining("\n"));

            int exitCode = process.waitFor();

            // Limpiar archivo temporal
            Files.deleteIfExists(tempScript);

            if (exitCode != 0) {
                return ResponseEntity.internalServerError()
                        .body(Map.of("error", "Error en el script Python: " + output));
            }

            // Parsear JSON devuelto por Python
            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> resultado = mapper.readValue(output, Map.class);
            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al ejecutar optimización: " + e.getMessage()));
        }
    }
}