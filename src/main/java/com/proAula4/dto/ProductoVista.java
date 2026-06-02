package com.proAula4.dto;

import com.proAula4.model.Producto;

public class ProductoVista {

    private Producto producto;
    private String nombreProveedor;

    public ProductoVista(Producto producto, String nombreProveedor) {
        this.producto = producto;
        this.nombreProveedor = nombreProveedor;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public String getNombreProveedor() {
        return nombreProveedor;
    }

    public void setNombreProveedor(String nombreProveedor) {
        this.nombreProveedor = nombreProveedor;
    }
}
