package org.example;

import org.example.chat.Principal;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        // Crear una instancia del formulario
        javax.swing.SwingUtilities.invokeLater(() -> {
            Principal formulario = new Principal(); // Instancia de ChatForm
            formulario.setVisible(true);           // Mostrar la ventana
            formulario.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE); // Cerrar al salir

            formulario.actualizarUsuarios("Roberto");

        });
    }
}