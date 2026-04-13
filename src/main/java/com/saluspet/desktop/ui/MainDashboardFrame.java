package com.saluspet.desktop.ui;

import com.saluspet.desktop.model.Veterinario;
import com.saluspet.desktop.utils.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainDashboardFrame extends JFrame {

    private JPanel contentPanel;

    public MainDashboardFrame() {
        initComponents();
    }

    private void initComponents() {
        setTitle("SalusPet - Panel de Control Principal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        // 1. Crear la Barra Lateral (Sidebar)
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(new Color(240, 250, 250)); // Light pastel cyan
        sidebarPanel.setPreferredSize(new Dimension(240, 0));

        // Logo en la barra lateral
        JLabel logoLabel = new JLabel();
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoLabel.setBorder(new EmptyBorder(25, 10, 0, 10));
        try {
            java.net.URL imgUrl = getClass().getResource("/images/logo_transparente.png");
            ImageIcon icon = imgUrl != null ? new ImageIcon(imgUrl)
                    : new ImageIcon("src/main/resources/images/logo_transparente.png");
            Image img = icon.getImage().getScaledInstance(-1, 190, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(img));
        } catch (Exception ex) {
            System.err.println("No se pudo cargar el logo");
        }

        // Datos del Usuario Local
        Veterinario vet = SessionManager.getInstance().getVeterinarioActual();
        String displayNombre = vet != null ? vet.getNombre() : "Desconocido";
        String displayRol = vet != null ? vet.getRol() + " en " + vet.getEspecialidad() : "Sin Especialidad";

        JLabel userLabel = new JLabel("Dr. " + displayNombre);
        userLabel.setForeground(new Color(25, 90, 80));
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        userLabel.setFont(userLabel.getFont().deriveFont(Font.BOLD, 18f));
        userLabel.setBorder(new EmptyBorder(0, 10, 5, 10));

        JLabel rolLabel = new JLabel(displayRol);
        rolLabel.setForeground(new Color(50, 120, 100));
        rolLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        rolLabel.setFont(rolLabel.getFont().deriveFont(Font.ITALIC, 12f));
        rolLabel.setBorder(new EmptyBorder(0, 10, 30, 10));

        // Botones del menú
        JButton btnPacientes = createSidebarButton("Gestión Pacientes");
        JButton btnCitas = createSidebarButton("Agenda / Citas");
        JButton btnHistorial = createSidebarButton("Historial Clínico");
        JButton btnConfig = createSidebarButton("Configuración");
        JButton btnLogout = createSidebarButton("Cerrar Sesión");

        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "¿Seguro que quieres cerrar sesión?", "Cerrar Sesión",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                // Borrar memoria
                SessionManager.getInstance().logout();

                new AuthFrame().setVisible(true);
                this.dispose();
            }
        });

        sidebarPanel.add(logoLabel);
        sidebarPanel.add(userLabel);
        sidebarPanel.add(rolLabel);
        sidebarPanel.add(btnPacientes);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        sidebarPanel.add(btnCitas);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        sidebarPanel.add(btnHistorial);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        sidebarPanel.add(btnConfig);

        sidebarPanel.add(Box.createVerticalGlue());

        sidebarPanel.add(btnLogout);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // 2. Área Central
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(UIManager.getColor("Panel.background"));

        JLabel welcomeMessage = new JLabel("Bienvenido al sistema corporativo, " + displayNombre + ".",
                SwingConstants.CENTER);
        welcomeMessage.setFont(welcomeMessage.getFont().deriveFont(26f));
        welcomeMessage.setForeground(UIManager.getColor("Label.disabledForeground"));

        contentPanel.add(welcomeMessage, BorderLayout.CENTER);

        add(sidebarPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        // --- ENLACES Y NAVEGACIÓN ---
        btnPacientes.addActionListener(e -> {
            contentPanel.removeAll();
            JLabel loading = new JLabel("Descargando listado de Mascotas desde la Base de Datos...",
                    SwingConstants.CENTER);
            loading.setFont(loading.getFont().deriveFont(18f));
            loading.setForeground(UIManager.getColor("Component.accentColor"));
            contentPanel.add(loading, BorderLayout.CENTER);
            contentPanel.revalidate();
            contentPanel.repaint();
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            new com.saluspet.desktop.network.MascotasService().obtenerMascotasAsync().thenAccept(mascotas -> {
                SwingUtilities.invokeLater(() -> {
                    setCursor(Cursor.getDefaultCursor());
                    PacientesPanel panelPacientes = new PacientesPanel(mascotas);
                    contentPanel.removeAll();
                    contentPanel.add(panelPacientes, BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                });
            }).exceptionally(ex -> {
                SwingUtilities.invokeLater(() -> {
                    setCursor(Cursor.getDefaultCursor());
                    contentPanel.removeAll();
                    JLabel err = new JLabel("Error HTTP al recuperar pacientes: " + ex.getMessage(),
                            SwingConstants.CENTER);
                    contentPanel.add(err);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                });
                return null;
            });
        });

        btnCitas.addActionListener(e -> {
            contentPanel.removeAll();
            CitasPanel panelCitas = new CitasPanel();
            contentPanel.add(panelCitas, BorderLayout.CENTER);
            contentPanel.revalidate();
            contentPanel.repaint();
        });

        btnHistorial.addActionListener(e -> {
            contentPanel.removeAll();
            HistorialPanel panelHistorial = new HistorialPanel();
            contentPanel.add(panelHistorial, BorderLayout.CENTER);
            contentPanel.revalidate();
            contentPanel.repaint();
        });

        btnConfig.addActionListener(e -> {
            contentPanel.removeAll();
            ConfigPanel panelConfig = new ConfigPanel();
            contentPanel.add(panelConfig, BorderLayout.CENTER);
            contentPanel.revalidate();
            contentPanel.repaint();
        });
    }

    private JButton createSidebarButton(String text) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(220, 50));
        btn.setFont(btn.getFont().deriveFont(Font.BOLD, 14f));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        // btn.setContentAreaFilled(false);
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.setOpaque(true);
        btn.setBackground(new Color(168, 230, 207)); // Verde Mint (Pastel Green)
        btn.setForeground(new Color(40, 80, 60)); // Text dark green
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setBorder(new EmptyBorder(10, 10, 10, 10));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(150, 215, 190)); // Un poco más oscuro en hover
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(168, 230, 207)); // Volver al normal
            }
        });

        return btn;
    }
}
