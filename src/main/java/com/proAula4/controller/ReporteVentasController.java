package com.proAula4.controller;

import com.proAula4.model.Venta;
import com.proAula4.services.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.Month;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Arrays;
import java.util.Collections;

@Controller
@RequestMapping("/admin")
public class ReporteVentasController {

    @Autowired
    private VentaService ventaService;

    @GetMapping("/reporte-ventas")
    public String mostrarReporteVentas(
            @RequestParam(value = "year", required = false) Integer year,
            Model model) {

        final int anoActual = LocalDate.now().getYear();

        // --- INICIO: Lógica de selección de Año Dinámica ---
        // 1. Obtener los años disponibles desde el servicio (años con ventas reales, orden descendente)
        List<Integer> anosDisponibles = ventaService.obtenerAnosConVentas();

        // 2. Si no hay años con ventas, agregamos el año actual como fallback
        if (anosDisponibles.isEmpty()) {
            anosDisponibles.add(anoActual);
        }

        // 3. Determinar el año seleccionado
        int anoSeleccionado;
        if (year != null && anosDisponibles.contains(year)) {
            // Usar el año del parámetro si es válido y existe en la lista de años con ventas
            anoSeleccionado = year;
        } else {
            // Usar el año más reciente de los disponibles (el primero en la lista ordenada descendentemente)
            anoSeleccionado = anosDisponibles.get(0);
        }

        model.addAttribute("anosDisponibles", anosDisponibles);
        model.addAttribute("anoSeleccionado", anoSeleccionado);
        // --- FIN: Lógica de selección de Año Dinámica ---


        List<Venta> ventasDelAno = Collections.emptyList();
        try {
            // Usamos el año seleccionado para obtener las ventas
            ventasDelAno = ventaService.obtenerVentasPorAno(anoSeleccionado);
        } catch (UnsupportedOperationException e) {
            System.err.println("Warning: VentaService.obtenerVentasPorAno is not yet implemented.");
        }


        double totalVentasAnual = ventasDelAno.stream()
                .mapToDouble(Venta::getTotal)
                .sum();

        Map<Month, Double> ventasPorMesMap = ventasDelAno.stream()
                .filter(venta -> venta.getFechaVenta() != null) // Asegurarse de que la fecha no es nula antes de acceder al mes
                .collect(Collectors.groupingBy(
                        venta -> venta.getFechaVenta().getMonth(),
                        Collectors.summingDouble(Venta::getTotal)
                ));

        List<Map<String, Object>> ventasMensualesList = Arrays.stream(Month.values())
                .map(month -> {
                    double monto = ventasPorMesMap.getOrDefault(month, 0.0);
                    String mesAbreviado = getNombreMesAbreviado(month);

                    Map<String, Object> data = new LinkedHashMap<>();
                    data.put("mes", mesAbreviado);
                    data.put("monto", monto);
                    return data;
                })
                .collect(Collectors.toList());

        double promedioMensual = totalVentasAnual / 12.0;

        String mesPico = "N/A";
        if (!ventasPorMesMap.isEmpty()) {
            Month mesMax = ventasPorMesMap.entrySet().stream()
                    .max(Comparator.comparingDouble(Map.Entry::getValue))
                    .map(Map.Entry::getKey)
                    .orElse(null);

            if (mesMax != null) {
                mesPico = getNombreMesCompleto(mesMax);
            }
        }

        model.addAttribute("totalVentasAnual", totalVentasAnual);
        model.addAttribute("promedioMensual", promedioMensual);
        model.addAttribute("mesPico", mesPico);
        model.addAttribute("ventasMensuales", ventasMensualesList);

        return "admin/reporte_ventas";
    }

    private String getNombreMesAbreviado(Month month) {
        return switch (month) {
            case JANUARY -> "Ene";
            case FEBRUARY -> "Feb";
            case MARCH -> "Mar";
            case APRIL -> "Abr";
            case MAY -> "May";
            case JUNE -> "Jun";
            case JULY -> "Jul";
            case AUGUST -> "Ago";
            case SEPTEMBER -> "Sep";
            case OCTOBER -> "Oct";
            case NOVEMBER -> "Nov";
            case DECEMBER -> "Dic";
        };
    }

    private String getNombreMesCompleto(Month month) {
        return switch (month) {
            case JANUARY -> "Enero";
            case FEBRUARY -> "Febrero";
            case MARCH -> "Marzo";
            case APRIL -> "Abril";
            case MAY -> "Mayo";
            case JUNE -> "Junio";
            case JULY -> "Julio";
            case AUGUST -> "Agosto";
            case SEPTEMBER -> "Septiembre";
            case OCTOBER -> "Octubre";
            case NOVEMBER -> "Noviembre";
            case DECEMBER -> "Diciembre";
        };
    }
}