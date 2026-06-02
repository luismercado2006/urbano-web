package com.proAula4.services;

import com.proAula4.dto.ProductoVista;
import com.proAula4.model.Producto;
import com.proAula4.model.Proveedor;
import com.proAula4.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductoServiceImpl implements ProductoService {

    @Autowired
    private ProductoRepository productoRepository;


    @Override
    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    @Override
    @Transactional
    public Producto guardarProducto(Producto producto) {
        if (producto.getRegistradoPor() == null || producto.getRegistradoPor().isEmpty()) {
            throw new IllegalArgumentException("El producto debe tener un usuario que lo registró.");
        }
        if (producto.getCodigoP() == null || producto.getCodigoP().isEmpty()) {
            String codigo = producto.generarCodigoProducto(productoRepository);
            producto.setCodigoP(codigo);
        }
        return productoRepository.save(producto);
    }

    // Busca por Long id (JPA usa Long en lugar de String)
    @Override
    public Producto obtenerPorId(String id) {
        try {
            Long longId = Long.parseLong(id);
            return productoRepository.findById(longId).orElse(null);
        } catch (NumberFormatException e) {
            System.err.println(">>> ID inválido: " + id);
            return null;
        }
    }

    @Override
    @Transactional
    public void eliminarProducto(String id) {
        try {
            Long longId = Long.parseLong(id);
            productoRepository.deleteById(longId);
        } catch (NumberFormatException e) {
            System.err.println(">>> ID inválido para eliminar: " + id);
        }
    }

    @Override
    public List<Producto> buscarPorIds(List<String> ids) {
        List<Long> longIds = ids.stream()
                .map(id -> {
                    try { return Long.parseLong(id); }
                    catch (NumberFormatException e) { return null; }
                })
                .filter(id -> id != null)
                .toList();
        return productoRepository.findAllById(longIds);
    }

    @Override
    public Page<Producto> buscarProductos(String query, Pageable pageable) {
        return productoRepository
                .findByDescripcionContainingIgnoreCaseOrCodigoPContainingIgnoreCaseOrMarcaContainingIgnoreCase(
                        query, query, query, pageable
                );
    }

    @Override
    public List<ProductoVista> listarProductosConProveedor() {
        return productoRepository.findAllConProveedor()
                .stream()
                .map(p -> {
                    String nombreProveedor = (p.getProveedor() != null)
                            ? p.getProveedor().getNombre()
                            : "Sin proveedor";
                    return new ProductoVista(p, nombreProveedor);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Producto actualizarStock(String idProducto, int cantidadAdicional) {
        if (cantidadAdicional <= 0) {
            throw new IllegalArgumentException("La cantidad adicional debe ser mayor a cero.");
        }
        Producto producto = obtenerPorId(idProducto);
        if (producto == null) {
            throw new IllegalArgumentException("Producto no encontrado con id: " + idProducto);
        }
        producto.setStock(producto.getStock() + cantidadAdicional);
        return productoRepository.save(producto);
    }

    // Reemplaza el MongoTemplate.updateFirst() — JPA hace el decremento con save()
    @Override
    @Transactional
    public void decrementarStock(String idProducto, int cantidad) {
        if (cantidad <= 0) return;
        try {
            Producto producto = obtenerPorId(idProducto);
            if (producto == null) {
                System.err.println(">>> Producto no encontrado para decrementar stock: " + idProducto);
                return;
            }
            int nuevoStock = producto.getStock() - cantidad;
            if (nuevoStock < 0) {
                System.err.println(">>> Stock insuficiente para producto: " + idProducto);
                return;
            }
            producto.setStock(nuevoStock);
            productoRepository.save(producto);
        } catch (Exception e) {
            System.err.println(">>> Error decrementando stock id: " + idProducto + " | " + e.getMessage());
        }
    }
}