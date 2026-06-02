package com.proAula4.services;

import com.proAula4.model.Proveedor;
import com.proAula4.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProveedorServiceImpl implements ProveedorService {

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Override
    public Proveedor guardarProveedor(Proveedor proveedor) {
        return proveedorRepository.save(proveedor);
    }

    @Override
    public List<Proveedor> listarProveedores() {
        return proveedorRepository.findAll();
    }

    @Override
    public Proveedor obtenerProveedorPorId(String id) {
        try {
            Long longId = Long.parseLong(id);
            return proveedorRepository.findById(longId).orElse(null);
        } catch (NumberFormatException e) {
            System.err.println(">>> ID de proveedor inválido: " + id);
            return null;
        }
    }

    @Override
    public Proveedor eliminar(String id) {
        try {
            Long longId = Long.parseLong(id);
            proveedorRepository.deleteById(longId);
        } catch (NumberFormatException e) {
            System.err.println(">>> ID de proveedor inválido para eliminar: " + id);
        }
        return null;
    }
}