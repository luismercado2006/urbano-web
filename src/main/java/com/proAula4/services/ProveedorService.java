package com.proAula4.services;

import com.proAula4.dto.ProductoVista;
import com.proAula4.model.Proveedor;
import java.util.List;

public interface ProveedorService {
    Proveedor guardarProveedor(Proveedor proveedor);
    List<Proveedor> listarProveedores();
    Proveedor obtenerProveedorPorId(String id);
    Proveedor eliminar(String id);



}
