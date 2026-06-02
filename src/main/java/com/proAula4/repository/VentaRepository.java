package com.proAula4.repository;

import com.proAula4.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    boolean existsByCodigoVenta(String codigoVenta);

    @Query("SELECT v FROM Venta v WHERE v.fechaVenta >= :inicio AND v.fechaVenta <= :fin")
    List<Venta> findVentasDelMes(@Param("inicio") LocalDateTime inicio,
                                 @Param("fin") LocalDateTime fin);

    List<Venta> findByRegistradoPor(String registradoPor);

    // Trae todas las ventas con sus detalles en un solo JOIN
    @Query("SELECT DISTINCT v FROM Venta v LEFT JOIN FETCH v.detallesVenta")
    List<Venta> findAllConDetalles();

    // Años distintos con ventas, orden descendente
    @Query("SELECT DISTINCT YEAR(v.fechaVenta) FROM Venta v WHERE v.fechaVenta IS NOT NULL ORDER BY YEAR(v.fechaVenta) DESC")
    List<Integer> findAnosConVentas();

    // Ventas de un año con detalles en un solo JOIN
    @Query("SELECT DISTINCT v FROM Venta v LEFT JOIN FETCH v.detallesVenta WHERE YEAR(v.fechaVenta) = :anio")
    List<Venta> findAllConDetallesByAnio(@Param("anio") int anio);

    // ── Agregaciones para el chatbot ──────────────────────────────────────────

    // Cuenta TODAS las ventas sin excepción
    @Query("SELECT COUNT(v) FROM Venta v")
    long countTotal();

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v")
    double sumTotalVendido();

    @Query("SELECT COALESCE(MAX(v.total), 0) FROM Venta v")
    double maxVenta();

    @Query("SELECT COALESCE(MIN(v.total), 0) FROM Venta v")
    double minVenta();

    @Query("SELECT COUNT(v) FROM Venta v WHERE v.fechaVenta >= :inicio AND v.fechaVenta <= :fin")
    long countVentasEntreFechas(@Param("inicio") LocalDateTime inicio,
                                @Param("fin") LocalDateTime fin);

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.fechaVenta >= :inicio AND v.fechaVenta <= :fin")
    double sumVentasEntreFechas(@Param("inicio") LocalDateTime inicio,
                                @Param("fin") LocalDateTime fin);

    // COALESCE en GROUP BY evita perder ventas con registradoPor = null
    @Query("SELECT COALESCE(v.registradoPor, 'Sin asignar'), COUNT(v), COALESCE(SUM(v.total), 0) " +
            "FROM Venta v GROUP BY COALESCE(v.registradoPor, 'Sin asignar')")
    List<Object[]> statsVentasPorUsuario();
}