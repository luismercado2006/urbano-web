package com.proAula4.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proAula4.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    @Autowired
    private VentaRepository ventaRepository;

    @Value("${groq.api.key:}")
    private String groqApiKey;

    // Cliente HTTP reutilizable — se crea una sola vez al arrancar Spring
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String responder(String pregunta) {

        // ── Fechas de referencia ──────────────────────────────────────────
        LocalDateTime ahora        = LocalDateTime.now();
        LocalDateTime inicioHoy    = ahora.toLocalDate().atStartOfDay();
        LocalDateTime inicioMes    = ahora.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime inicioMesPas = inicioMes.minusMonths(1);
        LocalDateTime finMesPas    = inicioMes.minusSeconds(1);

        // ── Queries de agregación directas en BD (sin traer objetos) ─────
        long   totalVentas     = ventaRepository.countTotal();
        double totalVendido    = ventaRepository.sumTotalVendido() * 1.19;
        double promedio        = totalVentas == 0 ? 0 : totalVendido / totalVentas;
        double ventaMax        = ventaRepository.maxVenta() * 1.19;
        double ventaMin        = totalVentas == 0 ? 0 : ventaRepository.minVenta() * 1.19;
        long   ventasHoy       = ventaRepository.countVentasEntreFechas(inicioHoy, ahora);
        long   ventasMesActual = ventaRepository.countVentasEntreFechas(inicioMes, ahora);
        long   ventasMesPasado = ventaRepository.countVentasEntreFechas(inicioMesPas, finMesPas);
        double totalMesPasado  = ventaRepository.sumVentasEntreFechas(inicioMesPas, finMesPas) * 1.19;

        // ── Ventas agrupadas por usuario (desde BD) ───────────────────────
        List<Object[]> statsPorUsuario = ventaRepository.statsVentasPorUsuario();
        StringBuilder usuariosCtx = new StringBuilder("=== VENTAS POR USUARIO ===\n");
        for (Object[] row : statsPorUsuario) {
            String usuario  = row[0] != null ? (String) row[0] : "Sin asignar";
            long   cantidad = ((Number) row[1]).longValue();
            double total    = ((Number) row[2]).doubleValue() * 1.19;
            usuariosCtx.append(String.format(
                    "- %s: %d ventas | Total: $%.2f%n",
                    usuario, cantidad, total
            ));
        }

        // ── Contexto para la IA ───────────────────────────────────────────
        String contexto = String.format("""
                Eres un asistente experto en ventas de una tienda de ropa llamada Urban Flair.
                Responde en español de forma clara y concisa.

                === RESUMEN ESTADÍSTICO ===
                - Total de ventas registradas: %d
                - Total vendido (con IVA 19%%): $%.2f
                - Promedio por venta: $%.2f
                - Venta más alta: $%.2f
                - Venta más baja: $%.2f
                - Ventas de hoy: %d
                - Ventas del mes actual: %d
                - Ventas del mes pasado: %d
                - Total vendido el mes pasado: $%.2f

                %s

                Responde SOLO con la información proporcionada arriba.
                Si no tienes el dato exacto, indícalo claramente.
                No inventes cifras ni datos que no estén en el resumen.
                """,
                totalVentas, totalVendido, promedio,
                ventaMax, ventaMin, ventasHoy,
                ventasMesActual, ventasMesPasado, totalMesPasado,
                usuariosCtx.toString()
        );

        return llamarGroq(contexto, pregunta);
    }

    // ── Llama a Groq y parsea la respuesta con Jackson ────────────────────────
    private String llamarGroq(String contexto, String pregunta) {
        try {
            String jsonBody = "{"
                    + "\"model\":\"llama-3.3-70b-versatile\","
                    + "\"temperature\":0.3,"
                    + "\"max_tokens\":512,"
                    + "\"messages\":["
                    + "{\"role\":\"system\",\"content\":" + toJson(contexto) + "},"
                    + "{\"role\":\"user\",\"content\":"   + toJson(pregunta) + "}"
                    + "]}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                    .header("Authorization", "Bearer " + groqApiKey)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println(">>> Groq error " + response.statusCode() + ": " + response.body());
                return "El asistente no está disponible en este momento. Intenta de nuevo.";
            }

            // Parsear con Jackson — robusto ante cualquier contenido
            JsonNode root = objectMapper.readTree(response.body());
            return root.path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText("No se pudo leer la respuesta de la IA.");

        } catch (java.net.http.HttpTimeoutException e) {
            System.err.println(">>> Groq timeout: " + e.getMessage());
            return "La IA tardó demasiado en responder. Intenta de nuevo.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error al conectar con la IA: " + e.getMessage();
        }
    }

    // Escapa una cadena para incluirla en JSON
    private String toJson(String text) {
        return "\"" + text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                + "\"";
    }
}