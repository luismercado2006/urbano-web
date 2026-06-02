package com.proAula4.repository;

import com.proAula4.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    boolean existsByCodigoP(String codigoP);

    // Búsqueda con paginación por descripción, código o marca (equivalente al @Query de Mongo)
    Page<Producto> findByDescripcionContainingIgnoreCaseOrCodigoPContainingIgnoreCaseOrMarcaContainingIgnoreCase(
            String descripcion, String codigoP, String marca, Pageable pageable
    );

    // Solo trae stock de todos los productos (equivalente al @Query Mongo con fields projection)
    @Query("SELECT p.stock FROM Producto p")
    List<Integer> findAllStockOnly();

    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.proveedor")
    List<Producto> findAllConProveedor();
}