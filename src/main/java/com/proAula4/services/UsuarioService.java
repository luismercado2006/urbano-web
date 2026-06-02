package com.proAula4.services;

import com.proAula4.model.Rol;
import com.proAula4.model.Usuario;
import com.proAula4.repository.RolRepository;
import com.proAula4.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Usuario obtenerUsuarioPorId(String id) {
        try {
            Long longId = Long.parseLong(id);
            return usuarioRepository.findById(longId).orElse(null);
        } catch (NumberFormatException e) {
            System.err.println(">>> ID de usuario inválido: " + id);
            return null;
        }
    }

    @Transactional
    public Usuario guardarUsuario(Usuario usuarioActualizado) {

        if (usuarioActualizado.getId() != null) {
            Usuario usuarioExistente = obtenerUsuarioPorId(String.valueOf(usuarioActualizado.getId()));

            if (usuarioExistente != null) {
                // 1. PRESERVAR ROLES
                usuarioActualizado.setRoles(usuarioExistente.getRoles());

                // 2. PRESERVAR CONTRASEÑA si no fue modificada
                if (usuarioActualizado.getPassword() == null || usuarioActualizado.getPassword().isEmpty()) {
                    usuarioActualizado.setPassword(usuarioExistente.getPassword());
                }
            } else {
                throw new RuntimeException("Error: No se encontró el usuario con ID: " + usuarioActualizado.getId());
            }
        }

        // Cifrar contraseña si es nueva (no está cifrada con bcrypt)
        if (!usuarioActualizado.getPassword().startsWith("$2a$")) {
            usuarioActualizado.setPassword(passwordEncoder.encode(usuarioActualizado.getPassword()));
        }

        return usuarioRepository.save(usuarioActualizado);
    }

    @Transactional
    public Usuario crearUsuarioAdmin(String nombre, String apellido, String correo, String password) {

        if (usuarioRepository.existsByCorreo(correo)) {
            return null;
        }

        // ✅ Buscar el rol en BD con Optional; si no existe, crearlo y guardarlo
        Rol rolAdmin = rolRepository.findByNombre("ADMIN")
                .orElseGet(() -> rolRepository.save(new Rol("ADMIN")));

        Usuario admin = new Usuario();
        admin.setNombre(nombre);
        admin.setApellido(apellido);
        admin.setCorreo(correo);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRoles(Arrays.asList(rolAdmin));

        return usuarioRepository.save(admin);
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo);
    }

    public boolean existePorCorreo(String correo) {
        return usuarioRepository.existsByCorreo(correo);
    }

    @Transactional
    public void eliminar(String id) {
        try {
            Long longId = Long.parseLong(id);
            usuarioRepository.deleteById(longId);
        } catch (NumberFormatException e) {
            System.err.println(">>> ID de usuario inválido para eliminar: " + id);
        }
    }
}