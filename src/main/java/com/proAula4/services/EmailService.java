package com.proAula4.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;


    public void enviarNotificacionAcceso(String correoDestino, String nombreUsuario) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom("mluisercado@gmail.com");
        mensaje.setTo(correoDestino);
        mensaje.setSubject("Acceso detectado - Urban Flair");
        mensaje.setText(
                "Hola " + nombreUsuario + ",\n\n" +
                        "Se ha detectado un acceso a tu cuenta en Urban Flair mediante Google.\n\n" +
                        "Si fuiste tú, puedes ignorar este mensaje.\n\n" +
                        "Si no reconoces este acceso, te recomendamos cambiar tu contraseña.\n" +
                        "Ponte en contacto con nuestro administrador: urbanflair@admin.com\n" +
                        "Linea de atencion:  +57 315 765 5758\n\n" +
                        "Equipo Urban Flair"
        );
        mailSender.send(mensaje);
    }
}