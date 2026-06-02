package com.proAula4.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ventas")
public class Venta implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // OneToMany: una venta tiene muchos detalles
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DetalleVenta> detallesVenta = new ArrayList<>();

    @Column(unique = true)
    private String codigoVenta;

    private LocalDateTime fechaVenta;
    private double total;
    private String registradoPor;

    public Venta() {
        this.codigoVenta = generarCodigoVenta();
        this.fechaVenta = LocalDateTime.now();
        this.total = 0.0;
    }

    public Venta(List<DetalleVenta> detalles) {
        this.codigoVenta = generarCodigoVenta();
        this.fechaVenta = LocalDateTime.now();
        setDetallesVenta(detalles);
    }

    private String generarCodigoVenta() {
        return "PJFD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private double calcularTotal() {
        if (detallesVenta == null || detallesVenta.isEmpty()) return 0.0;
        return detallesVenta.stream()
                .mapToDouble(d -> d.getSubtotal() == null ? 0.0 : d.getSubtotal())
                .sum();
    }

    public void recalcularTotal() {
        this.total = calcularTotal();
    }

    // ── Cálculos dinámicos ───────────────────────────────────────────────────
    public double getSubtotalArticulos() { return this.total; }
    public double getIva() { return this.total * 0.19; }
    public double getTotalFinal() { return this.total + getIva(); }

    // ── Getters & Setters ────────────────────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public List<DetalleVenta> getDetallesVenta() { return detallesVenta; }
    public void setDetallesVenta(List<DetalleVenta> detalles) {
        this.detallesVenta = detalles;
        if (detalles != null) {
            detalles.forEach(d -> d.setVenta(this)); // asegurar FK bidireccional
        }
        recalcularTotal();
    }

    public String getCodigoVenta() { return codigoVenta; }
    public void setCodigoVenta(String codigoVenta) { this.codigoVenta = codigoVenta; }

    public LocalDateTime getFechaVenta() { return fechaVenta; }
    public void setFechaVenta(LocalDateTime fechaVenta) { this.fechaVenta = fechaVenta; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getRegistradoPor() { return registradoPor; }
    public void setRegistradoPor(String registradoPor) { this.registradoPor = registradoPor; }
}