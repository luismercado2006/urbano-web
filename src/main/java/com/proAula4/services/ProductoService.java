package com.proAula4.services;

import com.proAula4.dto.ProductoVista;
import com.proAula4.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductoService {
    List<Producto> listarTodos();
    Producto guardarProducto(Producto producto);
    Producto obtenerPorId(String id);
    void eliminarProducto(String id);
    List<Producto> buscarPorIds(List<String> ids);
    Page<Producto> buscarProductos(String query, Pageable pageable);
    List<ProductoVista> listarProductosConProveedor();

    Producto actualizarStock(String idProducto, int cantidadAdicional);

    void decrementarStock(String idProducto, int cantidad);
}