package com.saluspet.desktop.main;

import com.formdev.flatlaf.FlatLightLaf;
import com.saluspet.desktop.ui.AuthFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        // Ejecutar la inicialización de la UI en el Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            // Aplicar el tema FlatLaf antes de inicializar la UI
            try {
                UIManager.setLookAndFeel(new FlatLightLaf());
                // Opcional: configurar variables globales u custom defaults
            } catch (Exception ex) {
                System.err.println("Error al inicializar FlatLaf: " + ex.getMessage());
            }

            // Iniciar la ventana de autenticación
            AuthFrame authFrame = new AuthFrame();
            authFrame.setVisible(true);
        });
    }
}
