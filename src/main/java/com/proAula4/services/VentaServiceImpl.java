package com.proAula4.services;

import com.proAula4.model.DetalleVenta;
import com.proAula4.model.Producto;
import com.proAula4.model.Venta;
import com.proAula4.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import weka.classifiers.Classifier;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.Attribute;
import weka.core.SerializationHelper;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VentaServiceImpl implements VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ProductoService productoService;

    private Classifier modeloPrediccion;
    private Instances datasetEstructura;
    private final String MODEL_PATH = "ventas_mensuales.model";

    @PostConstruct
    public void inicializarModelo() {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream(MODEL_PATH);
            if (is == null) {
                System.err.println(">>> Modelo Weka no encontrado. Predicciones deshabilitadas.");
                return;
            }
            this.modeloPrediccion = (Classifier) SerializationHelper.read(is);
            System.out.println(">>> [PREDICCIÓN] Modelo de Regresión Lineal cargado exitosamente.");

            Attribute ingresoAttr = new Attribute("ingreso_total_tienda");
            Attribute anioAttr    = new Attribute("anio");

            ArrayList<String> mesValues = new ArrayList<>();
            for (int i = 1; i <= 12; i++) mesValues.add(String.valueOf(i));
            Attribute mesAttr = new Attribute("mes", mesValues);

            ArrayList<Attribute> attributes = new ArrayList<>();
            attributes.add(ingresoAttr);
            attributes.add(anioAttr);
            attributes.add(mesAttr);

            this.datasetEstructura = new Instances("PrediccionDataset", attributes, 0);
            this.datasetEstructura.setClassIndex(0);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(">>> [PREDICCIÓN] Error al cargar o inicializar el modelo de Weka.");
        }
    }

    @Override
    public double predecirVentaMensual(int anio, int mes) throws Exception {
        if (modeloPrediccion == null) {
            throw new IllegalStateException("El modelo de predicción no está cargado.");
        }

        Instances dataParaPredecir = new Instances(this.datasetEstructura);
        DenseInstance nuevaInstancia = new DenseInstance(dataParaPredecir.numAttributes());

        nuevaInstancia.setValue(dataParaPredecir.attribute("anio"), anio);
        nuevaInstancia.setValue(dataParaPredecir.attribute("mes"), String.valueOf(mes));

        dataParaPredecir.add(nuevaInstancia);
        return modeloPrediccion.classifyInstance(dataParaPredecir.lastInstance());
    }

    @Override
    @Transactional
    public Venta guardarVenta(Venta venta) {
        // Asignar FK bidireccional antes de guardar (requerido por JPA)
        if (venta.getDetallesVenta() != null) {
            for (DetalleVenta det : venta.getDetallesVenta()) {
                det.setVenta(venta); // ← obligatorio en JPA para la relación OneToMany

                if (det.getIdProducto() != null) {
                    Producto p = productoService.obtenerPorId(det.getIdProducto());
                    if (p != null) {
                        if (det.getNombreProducto() == null) {
                            det.setNombreProducto(p.getDescripcion());
                        }
                        det.setCategoria(p.getCategoria());
                        det.setMarca(p.getMarca());
                        det.setColor(p.getColor());
                        det.setTalla(p.getTalla());
                        productoService.decrementarStock(det.getIdProducto(), det.getCantidad());
                    }
                }
                det.setSubtotal(det.getPrecioUnitario() * det.getCantidad());
            }
        }

        venta.recalcularTotal();
        return ventaRepository.save(venta);
    }

    @Override
    public List<Integer> obtenerAnosConVentas() {
        return ventaRepository.findAnosConVentas();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Venta> listarTodas() {
        List<Venta> ventas = ventaRepository.findAllConDetalles();
        populateProductos(ventas);
        return ventas;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Venta> obtenerVentasPorAno(int anio) {
        List<Venta> ventas = ventaRepository.findAllConDetallesByAnio(anio);
        populateProductos(ventas);
        return ventas;
    }

    // Rellena nombreProducto en detalles que no lo tengan
    private void populateProductos(List<Venta> ventas) {
        // 1. Recolectar solo los IDs de productos que realmente faltan
        List<String> idsFaltantes = ventas.stream()
                .filter(v -> v.getDetallesVenta() != null)
                .flatMap(v -> v.getDetallesVenta().stream())
                .filter(d -> d.getIdProducto() != null && d.getNombreProducto() == null)
                .map(DetalleVenta::getIdProducto)
                .distinct()
                .collect(Collectors.toList());

        if (idsFaltantes.isEmpty()) return; // nada que buscar

        // 2. Un solo query para todos los productos necesarios
        Map<String, Producto> productoMap = productoService.buscarPorIds(idsFaltantes)
                .stream()
                .collect(Collectors.toMap(
                        p -> String.valueOf(p.getId()),
                        p -> p
                ));

        // 3. Asignar con el mapa (O(1) por detalle)
        for (Venta venta : ventas) {
            if (venta.getDetallesVenta() == null) continue;
            for (DetalleVenta detalle : venta.getDetallesVenta()) {
                if (detalle.getIdProducto() != null && detalle.getNombreProducto() == null) {
                    Producto p = productoMap.get(detalle.getIdProducto());
                    if (p != null) detalle.setNombreProducto(p.getDescripcion());
                }
            }
        }
    }

    @Override
    public Venta obtenerVentaPorId(String id) {
        try {
            Long longId = Long.parseLong(id);
            Venta venta = ventaRepository.findById(longId).orElse(null);
            if (venta != null) populateProductos(List.of(venta));
            return venta;
        } catch (NumberFormatException e) {
            System.err.println(">>> ID de venta inválido: " + id);
            return null;
        }
    }

    @Override
    @Transactional
    public void eliminarVenta(String id) {
        try {
            Long longId = Long.parseLong(id);
            ventaRepository.deleteById(longId);
        } catch (NumberFormatException e) {
            System.err.println(">>> ID de venta inválido para eliminar: " + id);
        }
    }

    @Override
    public List<Venta> listarPorUsuario(String username) {
        List<Venta> ventas = ventaRepository.findByRegistradoPor(username);
        populateProductos(ventas);
        return ventas;
    }
}