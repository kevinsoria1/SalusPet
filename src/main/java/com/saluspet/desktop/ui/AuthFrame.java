package com.saluspet.desktop.ui;

import com.google.gson.Gson;
import com.saluspet.desktop.model.ApiError;
import com.saluspet.desktop.model.Veterinario;
import com.saluspet.desktop.network.AuthService;
import com.saluspet.desktop.utils.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AuthFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private AuthService authService;
    private Gson gson;

    public AuthFrame() {
        this.authService = new AuthService();
        this.gson = new Gson();
        initComponents();
    }

    private void initComponents() {
        setTitle("SalusPet - Entrada");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 580);
        setLocationRelativeTo(null);
        setResizable(false);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createLoginPanel(), "LOGIN");
        mainPanel.add(createRegisterPanel(), "REGISTER");

        add(mainPanel);
        cardLayout.show(mainPanel, "LOGIN");
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 250, 250)); // Light pastel cyan
        panel.setBorder(new EmptyBorder(10, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Logo
        JLabel logoLabel = new JLabel("", SwingConstants.CENTER);
        try {
            java.net.URL imgUrl = getClass().getResource("/images/logo_transparente.png");
            ImageIcon icon = imgUrl != null ? new ImageIcon(imgUrl)
                    : new ImageIcon("src/main/resources/images/logo_transparente.png");
            Image img = icon.getImage().getScaledInstance(-1, 250, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(img));
        } catch (Exception ex) {
            System.err.println("No se pudo cargar el logo");
        }

        JLabel titleLabel = new JLabel("Iniciar Sesión", SwingConstants.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 26f));
        titleLabel.setForeground(new Color(25, 90, 80)); // Dark Mint/Teal

        JLabel subtitle = new JLabel("Usa el email y clave de acceso de saluspet", SwingConstants.CENTER);
        subtitle.setForeground(Color.GRAY);

        JTextField emailField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        // padding interno para los campos
        emailField.setMargin(new Insets(10, 10, 10, 10));
        passwordField.setMargin(new Insets(10, 10, 10, 10));
        emailField.putClientProperty("JTextField.placeholderText", "Correo electrónico");
        passwordField.putClientProperty("JTextField.placeholderText", "Contraseña");

        JButton loginBtn = new JButton("Entrar");
        loginBtn.setFont(loginBtn.getFont().deriveFont(Font.BOLD, 16f));
        loginBtn.putClientProperty("JButton.buttonType", "roundRect");
        loginBtn.setBackground(new Color(168, 230, 207)); // Mint green
        loginBtn.setForeground(new Color(40, 80, 60)); // Dark Green
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.setPreferredSize(new Dimension(0, 45));

        JButton openRegisterBtn = new JButton("¿No tienes cuenta? Regístrate");
        openRegisterBtn.setContentAreaFilled(false);
        openRegisterBtn.setBorderPainted(false);
        openRegisterBtn.setForeground(new Color(50, 100, 90));
        openRegisterBtn.setFont(openRegisterBtn.getFont().deriveFont(Font.BOLD, 13f));
        openRegisterBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Acción asíncrona real
        loginBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            String pwd = new String(passwordField.getPassword());

            // UX: Deshabilitar evitar multiclicks y mostrar cursor de carga
            enablePanelComponents(panel, false);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            authService.loginAsync(email, pwd).thenAccept(response -> {
                // Volver al hilo de la UI
                SwingUtilities.invokeLater(() -> {
                    enablePanelComponents(panel, true);
                    setCursor(Cursor.getDefaultCursor());

                    if (response.statusCode() == 200) {
                        try {
                            System.out.println("JSON LOGIN RECIBIDO: " + response.body());
                            Veterinario vet = gson.fromJson(response.body(), Veterinario.class);
                            SessionManager.getInstance().setVeterinarioActual(vet);

                            // Navegar al Main Dashboard
                            MainDashboardFrame dashboardFrame = new MainDashboardFrame();
                            dashboardFrame.setVisible(true);
                            AuthFrame.this.dispose();

                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this, "Error al procesar los datos de sesión",
                                    "Error Crítico", JOptionPane.ERROR_MESSAGE);
                        }
                    } else if (response.statusCode() == 401 || response.statusCode() == 404) {
                        ApiError error = gson.fromJson(response.body(), ApiError.class);
                        String msg = (error != null && error.getMensaje() != null) ? error.getMensaje()
                                : "Credenciales rechazadas";
                        JOptionPane.showMessageDialog(this, msg, "Error de Inicio", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "HTTP " + response.statusCode(), "Subiendo",
                                JOptionPane.WARNING_MESSAGE);
                    }
                });
            }).exceptionally(ex -> {
                SwingUtilities.invokeLater(() -> {
                    enablePanelComponents(panel, true);
                    setCursor(Cursor.getDefaultCursor());
                    JOptionPane.showMessageDialog(this,
                            "El servidor no responde. ¿DevTunnels está apagado?\n\nDetalle:\n" + ex.getMessage(),
                            "Fallo de Red", JOptionPane.ERROR_MESSAGE);
                });
                return null;
            });
        });

        openRegisterBtn.addActionListener(e -> cardLayout.show(mainPanel, "REGISTER"));

        // Montaje GridBag (1 sola columna, expande en gridx=0)
        gbc.gridx = 0;

        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(logoLabel, gbc);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel.add(titleLabel, gbc);
        gbc.gridy = 2;
        gbc.insets = new Insets(5, 5, 20, 5);
        panel.add(subtitle, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(2, 5, 10, 5);
        panel.add(emailField, gbc);
        gbc.gridy = 5;
        gbc.insets = new Insets(2, 5, 20, 5);
        panel.add(passwordField, gbc);

        gbc.gridy = 6;
        gbc.ipady = 5;
        panel.add(loginBtn, gbc);
        gbc.ipady = 0;
        gbc.gridy = 7;
        gbc.insets = new Insets(10, 5, 10, 5);
        panel.add(openRegisterBtn, gbc);

        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 250, 250)); // Light pastel cyan
        panel.setBorder(new EmptyBorder(5, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Logo
        JLabel logoLabel = new JLabel("", SwingConstants.CENTER);
        try {
            java.net.URL imgUrl = getClass().getResource("/images/logo_transparente.png");
            ImageIcon icon = imgUrl != null ? new ImageIcon(imgUrl)
                    : new ImageIcon("src/main/resources/images/logo_transparente.png");
            Image img = icon.getImage().getScaledInstance(-1, 160, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(img));
        } catch (Exception ex) {
            System.err.println("No se pudo cargar el logo");
        }

        JLabel titleLabel = new JLabel("Alta de Veterinario", SwingConstants.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 22f));
        titleLabel.setForeground(new Color(25, 90, 80));

        // 6 campos según backend
        JTextField nombreField = new JTextField(15);
        JTextField especialidadField = new JTextField(15);
        JTextField colegiadoField = new JTextField(15);
        JTextField emailField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        JPasswordField codigoField = new JPasswordField(15); // Es secreto

        // Estilos para los campos
        Component[] fields = { nombreField, especialidadField, colegiadoField, emailField, passwordField, codigoField };
        for (Component c : fields) {
            ((javax.swing.text.JTextComponent) c).setMargin(new Insets(6, 6, 6, 6));
        }

        JButton registerBtn = new JButton("Registrate ahora");
        registerBtn.putClientProperty("JButton.buttonType", "roundRect");
        registerBtn.setBackground(new Color(168, 230, 207)); // Verde pastel mint
        registerBtn.setForeground(new Color(40, 80, 60));
        registerBtn.setFont(registerBtn.getFont().deriveFont(Font.BOLD, 15f));
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerBtn.setPreferredSize(new Dimension(0, 45));

        JButton openLoginBtn = new JButton("¿Ya tienes cuenta? Entrar");
        openLoginBtn.setContentAreaFilled(false);
        openLoginBtn.setBorderPainted(false);
        openLoginBtn.setForeground(new Color(50, 100, 90));
        openLoginBtn.setFont(openLoginBtn.getFont().deriveFont(Font.BOLD, 13f));
        openLoginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Acción asíncrona de registro
        registerBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            String pwd = new String(passwordField.getPassword());
            String codigo = new String(codigoField.getPassword());
            String nombre = nombreField.getText().trim();
            String obs = especialidadField.getText().trim();
            String co = colegiadoField.getText().trim();

            enablePanelComponents(panel, false);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            authService.registerAsync(email, pwd, codigo, nombre, obs, co).thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    enablePanelComponents(panel, true);
                    setCursor(Cursor.getDefaultCursor());

                    if (response.statusCode() == 200) {
                        JOptionPane.showMessageDialog(this, "Cuenta creada exitosamente. ¡Ya puedes entrar!", "Éxito",
                                JOptionPane.INFORMATION_MESSAGE);
                        nombreField.setText("");
                        especialidadField.setText("");
                        colegiadoField.setText("");
                        emailField.setText("");
                        passwordField.setText("");
                        codigoField.setText("");
                        cardLayout.show(mainPanel, "LOGIN");
                    } else {
                        try {
                            ApiError err = gson.fromJson(response.body(), ApiError.class);
                            JOptionPane.showMessageDialog(this, err.getMensaje(), "Error", JOptionPane.ERROR_MESSAGE);
                        } catch (Exception x) {
                            JOptionPane.showMessageDialog(this, "Fallo desconocido de servidor.", "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
            }).exceptionally(ex -> {
                SwingUtilities.invokeLater(() -> {
                    enablePanelComponents(panel, true);
                    setCursor(Cursor.getDefaultCursor());
                    JOptionPane.showMessageDialog(this, "No hay conexión al backend.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                });
                return null;
            });
        });

        openLoginBtn.addActionListener(e -> cardLayout.show(mainPanel, "LOGIN"));

        // Montaje GridBag (Formato Tablas 2 Columnas)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; // Expande logo
        gbc.insets = new Insets(0, 10, 0, 10);
        panel.add(logoLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(5, 10, 15, 10);
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1; // Reseteamos
        gbc.insets = new Insets(5, 5, 2, 5);

        // Fila 1: Nombre y Especialidad
        gbc.gridy = 2;
        gbc.gridx = 0;
        panel.add(new JLabel("Nombre Completo:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel("Especialidad:"), gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        panel.add(nombreField, gbc);
        gbc.gridx = 1;
        panel.add(especialidadField, gbc);

        // Fila 2: Colegiado y Email
        gbc.gridy = 4;
        gbc.gridx = 0;
        panel.add(new JLabel("Nº Colegiado:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel("Email Académico:"), gbc);

        gbc.gridy = 5;
        gbc.gridx = 0;
        panel.add(colegiadoField, gbc);
        gbc.gridx = 1;
        panel.add(emailField, gbc);

        // Fila 3: Contraseña y Código
        gbc.gridy = 6;
        gbc.gridx = 0;
        panel.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel("Código Clínica Secreto:"), gbc);

        gbc.gridy = 7;
        gbc.gridx = 0;
        panel.add(passwordField, gbc);
        gbc.gridx = 1;
        panel.add(codigoField, gbc);

        // Fila 4: Botones
        gbc.gridy = 8;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 20, 5, 20);
        panel.add(registerBtn, gbc);

        gbc.gridy = 9;
        gbc.insets = new Insets(0, 20, 10, 20);
        panel.add(openLoginBtn, gbc);

        return panel;
    }

    private void enablePanelComponents(Container container, boolean enable) {
        Component[] components = container.getComponents();
        for (Component component : components) {
            component.setEnabled(enable);
            if (component instanceof Container) {
                enablePanelComponents((Container) component, enable);
            }
        }
    }
}
