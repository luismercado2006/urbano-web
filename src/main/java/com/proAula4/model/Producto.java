package com.proAula4.model;

import com.proAula4.repository.ProductoRepository;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import java.io.Serializable;
import java.util.Random;

@Entity
@Table(name = "productos")
public class Producto implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String codigoP;

    private String categoria;
    private String descripcion;
    private String color;
    private String marca;
    private String talla;

    // Relación con Proveedor (FK proveedor_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    private String registradoPor;

    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(nullable = false)
    private int stock;

    @Min(value = 0, message = "El valor del producto no puede ser negativo")
    @Column(nullable = false)
    private float valorProdu;

    @Column(nullable = false)
    private Double porcentajeGanancia;

    public Producto() {
        this.porcentajeGanancia = 40.0;
    }

    public Producto(String codigoP, String descripcion, Proveedor proveedor, String categoria,
                    String color, String marca, String talla, int stock, float valorProdu, Double porcentajeGanancia) {
        this.codigoP = codigoP;
        this.proveedor = proveedor;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.color = color;
        this.marca = marca;
        this.talla = talla;
        this.stock = stock;
        this.valorProdu = valorProdu;
        this.porcentajeGanancia = (porcentajeGanancia == null || porcentajeGanancia == 0.0) ? 40.0 : porcentajeGanancia;
    }

    // Genera código único PRD-XXXXXX
    public String generarCodigoProducto(ProductoRepository productoRepository) {
        String codigoGenerado;
        do {
            int numero = new Random().nextInt(1000000);
            codigoGenerado = "PRD-" + String.format("%06d", numero);
        } while (productoRepository.existsByCodigoP(codigoGenerado));
        return codigoGenerado;
    }

    // Devuelve el proveedorId como String para compatibilidad con código existente
    public String getProveedorId() {
        return proveedor != null ? String.valueOf(proveedor.getId()) : null;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigoP() { return codigoP; }
    public void setCodigoP(String codigoP) { this.codigoP = codigoP; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getTalla() { return talla; }
    public void setTalla(String talla) { this.talla = talla; }

    public Proveedor getProveedor() { return proveedor; }
    public void setProveedor(Proveedor proveedor) { this.proveedor = proveedor; }

    public String getRegistradoPor() { return registradoPor; }
    public void setRegistradoPor(String registradoPor) { this.registradoPor = registradoPor; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public float getValorProdu() { return valorProdu; }
    public void setValorProdu(float valorProdu) { this.valorProdu = valorProdu; }

    public Double getPorcentajeGanancia() {
        return porcentajeGanancia == null ? 40.0 : porcentajeGanancia;
    }
    public void setPorcentajeGanancia(Double porcentajeGanancia) {
        this.porcentajeGanancia = (porcentajeGanancia == null || porcentajeGanancia == 0.0) ? 40.0 : porcentajeGanancia;
    }
}