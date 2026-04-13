package com.saluspet.desktop.ui;

import com.saluspet.desktop.model.Mascota;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DetalleMascotaFrame extends JFrame {

    private final Mascota mascota;

    public DetalleMascotaFrame(Mascota mascota) {
        this.mascota = mascota;
        initComponents();
    }

    private void initComponents() {
        setTitle("Ficha Técnica: " + (mascota.getNombre() != null ? mascota.getNombre() : "Paciente"));
        setSize(400, 500);
        setLocationRelativeTo(null); // Centrar en medio de la pantalla
        // Muy importante: DISPOSE_ON_CLOSE destruirá esta ventana al cerrarla SIN apagar la APP principal
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        mainPanel.setBackground(UIManager.getColor("Panel.background"));

        // Cabecera Ficha Mádica
        JLabel headerLabel = new JLabel("FICHA CLÍNICA", SwingConstants.CENTER);
        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD, 22f));
        headerLabel.setForeground(new Color(25, 90, 80));
        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Lógica de carga decodificadora para imagen binaria Base64
        JLabel fotoLabel = new JLabel();
        fotoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        boolean imagenCargada = false;
        String b64 = mascota.getUrlFoto();
        
        if (b64 != null && !b64.trim().isEmpty() && !b64.equalsIgnoreCase("null")) {
            try {
                // Alternativa 2: Decodificar a File para Evitar Cuellos de Java Swing
                byte[] imageBytes = java.util.Base64.getDecoder().decode(b64.trim());
                java.io.File tempFile = java.io.File.createTempFile("perfil_mascota_", ".jpg");
                tempFile.deleteOnExit();
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile)) {
                    fos.write(imageBytes);
                }
                
                ImageIcon nativeIcon = new ImageIcon(tempFile.getAbsolutePath());
                
                if (nativeIcon.getIconWidth() > 0) {
                    java.awt.Image img = nativeIcon.getImage();
                    int targetWidth = 150;
                    int targetHeight = (nativeIcon.getIconHeight() * targetWidth) / nativeIcon.getIconWidth();
                    java.awt.Image scaledImg = img.getScaledInstance(targetWidth, targetHeight, java.awt.Image.SCALE_SMOOTH);
                    fotoLabel.setIcon(new ImageIcon(scaledImg));
                    imagenCargada = true;
                } else {
                    System.err.println("Imagen cargada desde archivo temporal pero el ancho es 0. Formato no compatible.");
                }
            } catch (Exception e) {
                System.err.println("Fallo decodificando imagen física: " + e.getMessage());
            }
        }// Si falló el algoritmo o no había foto, fijamos el placeholder
        if (!imagenCargada) {
            fotoLabel.setText("📸");
            fotoLabel.setFont(fotoLabel.getFont().deriveFont(40f));
            fotoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        }

        mainPanel.add(headerLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(fotoLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        // Bloque Detalles clave-Valor
        mainPanel.add(createDetailRow("ID Mascota:", String.valueOf(mascota.getIdMascota())));
        
        JPanel ownerRow = createDetailRow("Dueño:", mascota.getDueño() != null ? mascota.getDueño() : "Desconocido");
        mainPanel.add(ownerRow);
        
        mainPanel.add(createDetailRow("Nombre:", mascota.getNombre()));
        mainPanel.add(createDetailRow("Especie:", mascota.getEspecie()));
        mainPanel.add(createDetailRow("Género:", mascota.getGenero() != null ? mascota.getGenero() : "N/D"));
        mainPanel.add(createDetailRow("Peso Local:", mascota.getPeso() != null ? mascota.getPeso() + " kg" : "Sin pesar"));
        mainPanel.add(createDetailRow("Nacimiento:", formatFechaEspanol(mascota.getFechaNacimiento())));

        // Botón Salir
        mainPanel.add(Box.createVerticalGlue()); // Empuja el boton al final
        JButton btnCerrar = new JButton("Cerrar Ficha");
        btnCerrar.setFont(btnCerrar.getFont().deriveFont(Font.BOLD, 14f));
        btnCerrar.setBackground(new Color(168, 230, 207)); // Pastel green
        btnCerrar.setForeground(Color.DARK_GRAY);
        btnCerrar.putClientProperty("JButton.buttonType", "roundRect");
        btnCerrar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCerrar.addActionListener(e -> this.dispose());
        
        // Wrap button in flow layout to prevent stretching if any
        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.add(btnCerrar);
        
        mainPanel.add(btnPanel);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane);
    }

    /**
     * Construye una fila visual limpia con un Titulo a la izquierda y el Valor a la derecha.
     */
    private JPanel createDetailRow(String labelText, String valueText) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        row.setBorder(new EmptyBorder(5, 0, 5, 0));

        JLabel label = new JLabel(labelText);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 13f));

        JLabel value = new JLabel(valueText, SwingConstants.RIGHT);
        value.setFont(value.getFont().deriveFont(Font.PLAIN, 13f));
        value.setForeground(UIManager.getColor("Label.disabledForeground"));

        row.add(label, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);
        return row;
    }

    private String formatFechaEspanol(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) return "Desconocida";
        try {
            String datePart = isoDate.contains("T") ? isoDate.split("T")[0] : isoDate;
            String[] parts = datePart.split("-");
            if (parts.length >= 3) {
                return parts[2] + "/" + parts[1] + "/" + parts[0];
            }
        } catch(Exception ignored) {}
        return isoDate;
    }
}
