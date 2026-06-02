package com.proAula4.services;

import com.proAula4.repository.ProductoRepository;
import com.proAula4.repository.ProveedorRepository;
import com.proAula4.repository.VentaRepository;
import com.proAula4.model.Venta;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class DashboardService {

    private final ProductoRepository productoRepository;
    private final ProveedorRepository proveedorRepository;
    private final VentaRepository ventaRepository;

    public DashboardService(ProductoRepository productoRepository,
                            ProveedorRepository proveedorRepository,
                            VentaRepository ventaRepository) {
        this.productoRepository = productoRepository;
        this.proveedorRepository = proveedorRepository;
        this.ventaRepository = ventaRepository;
    }

    public long contarProductos() {
        return productoRepository.count();
    }

    public long contarProveedores() {
        return proveedorRepository.count();
    }

    public long contarStockDisponible() {
        // findAllStockOnly() ahora devuelve List<Integer> con JPA (solo los valores de stock)
        List<Integer> stocks = productoRepository.findAllStockOnly();
        return stocks.stream().mapToLong(Integer::longValue).sum();
    }

    public double ventasDelMes() {
        LocalDate today = LocalDate.now();
        LocalDateTime inicio = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime fin    = today.withDayOfMonth(today.lengthOfMonth()).atTime(LocalTime.MAX);

        return ventaRepository.findVentasDelMes(inicio, fin)
                .stream()
                .mapToDouble(Venta::getTotal)
                .sum();
    }
}