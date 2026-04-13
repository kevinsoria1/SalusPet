package com.saluspet.desktop.ui;

import com.saluspet.desktop.model.Veterinario;
import com.saluspet.desktop.utils.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ConfigPanel extends JPanel {

    public ConfigPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(UIManager.getColor("Panel.background"));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Configuración del Sistema");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 22f));
        titleLabel.setForeground(new Color(25, 90, 80));
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(0, 1, 15, 15));
        centerPanel.setOpaque(false);
        
        Veterinario vet = SessionManager.getInstance().getVeterinarioActual();
        String vetInfo = (vet != null) ? vet.getNombre() + " (" + vet.getEspecialidad() + ")" : "Desconocido";

        centerPanel.add(crearSeccion("Perfil de Facultativo", "<html>El doctor o doctores operando este terminal: <br/><b>" + vetInfo + "</b><br/><br/><i>Funcionalidad de cambio de clave en desarrollo para el próximo sprint.</i></html>"));
        centerPanel.add(crearSeccion("Apariencia", "<html>El terminal de SalusPet incorpora un sistema nativo. El Modo Oscuro estará disponible en futuras configuraciones.</html>"));

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(centerPanel, BorderLayout.NORTH);
        
        add(wrap, BorderLayout.CENTER);
    }

    private JPanel crearSeccion(String titulo, String texto) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), titulo));
        
        JLabel content = new JLabel(texto);
        content.setBorder(new EmptyBorder(10, 10, 10, 10));
        content.setFont(content.getFont().deriveFont(13f));
        panel.add(content, BorderLayout.CENTER);
        
        return panel;
    }
}
