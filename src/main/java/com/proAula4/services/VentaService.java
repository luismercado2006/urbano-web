package com.proAula4.services;

import com.proAula4.model.DetalleVenta;
import com.proAula4.model.Venta;
import java.util.List;

public interface VentaService {

    Venta guardarVenta(Venta venta);

    List<Venta> listarPorUsuario(String username);

    List<Venta> listarTodas();

    List<Venta> obtenerVentasPorAno(int anio);

    double predecirVentaMensual(int anio, int mes) throws Exception;

    Venta obtenerVentaPorId(String id);

    List<Integer> obtenerAnosConVentas();

    void eliminarVenta(String id);
}