package com.saluspet.desktop.ui;

import com.saluspet.desktop.model.Cita;
import com.saluspet.desktop.model.Veterinario;
import com.saluspet.desktop.network.CitasService;
import com.saluspet.desktop.network.HistorialesService;
import com.saluspet.desktop.network.RespuestaPaginadaCitas;
import com.saluspet.desktop.utils.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class HistorialPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnPrev;
    private JButton btnNext;
    private JButton btnVerPdf;
    private JLabel lblPaginacion;
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private List<Cita> currentCitas;

    private int paginaActual = 1;
    private int totalPaginas = 1;

    private final CitasService citasService;
    private final HistorialesService historialesService;
    private final Veterinario actualVet;

    public HistorialPanel() {
        this.citasService = new CitasService();
        this.historialesService = new HistorialesService();
        this.actualVet = SessionManager.getInstance().getVeterinarioActual();
        
        initComponents();
        cargarPagina(paginaActual);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(UIManager.getColor("Panel.background"));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- TOP PANEL ---
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = new JLabel("Historial Clínico (Completadas)");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 22f));
        titleLabel.setForeground(new Color(25, 90, 80));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        topPanel.add(titleLabel);

        topPanel.add(Box.createVerticalStrut(10));

        // Navigation Bar
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        navBar.setOpaque(false);
        navBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        btnPrev = new JButton("<< Anterior");
        btnNext = new JButton("Siguiente >>");
        btnVerPdf = new JButton("Ver PDF");
        lblPaginacion = new JLabel("Página 1 de 1");

        btnPrev.putClientProperty("JButton.buttonType", "roundRect");
        btnPrev.setBackground(new Color(168, 230, 207));
        btnPrev.setForeground(Color.DARK_GRAY);
        btnPrev.setVisible(false);

        btnNext.putClientProperty("JButton.buttonType", "roundRect");
        btnNext.setBackground(new Color(168, 230, 207));
        btnNext.setForeground(Color.DARK_GRAY);
        btnNext.setVisible(false);

        btnVerPdf.putClientProperty("JButton.buttonType", "roundRect");
        btnVerPdf.setEnabled(false);
        btnVerPdf.setBackground(new Color(168, 230, 207)); // Pastel green
        btnVerPdf.setForeground(Color.DARK_GRAY);

        btnPrev.addActionListener(e -> {
            if (paginaActual > 1) {
                cargarPagina(paginaActual - 1);
            }
        });

        btnNext.addActionListener(e -> {
            if (paginaActual < totalPaginas) {
                cargarPagina(paginaActual + 1);
            }
        });

        btnVerPdf.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow == -1 || currentCitas == null) return;
            int modelRow = table.convertRowIndexToModel(viewRow);
            Cita target = currentCitas.get(modelRow);
            
            String diagGuardado = target.getDescripcion();
            boolean isDefault = diagGuardado == null || diagGuardado.trim().isEmpty() || 
                                diagGuardado.contains("Solicitud enviada") || 
                                diagGuardado.contains("Esperando respuesta");
                                
            if (!isDefault) {
                // Usamos el diagnóstico que ya está guardado en la propia cita (gracias al parche anterior)
                CitasPanel.generarYAbrirPdf(target, diagGuardado, actualVet, HistorialPanel.this);
                return;
            }

            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            btnVerPdf.setEnabled(false);

            // Fallback robusto para citas antiguas: Emparejamiento por orden cronológico
            java.util.concurrent.CompletableFuture<List<Cita>> citasFuture = citasService.obtenerCitasAsync();
            java.util.concurrent.CompletableFuture<List<com.saluspet.desktop.model.Historial>> histFuture = historialesService.obtenerHistorialesMascotaAsync(target.getIdMascota());

            citasFuture.thenCombine(histFuture, (todasCitas, historiales) -> {
                String diagFinal = "Diagnóstico no disponible en el archivo histórico.";
                
                if (historiales != null && !historiales.isEmpty() && todasCitas != null) {
                    // Filtrar y ordenar las citas completadas de esta mascota
                    List<Cita> citasMascota = todasCitas.stream()
                        .filter(c -> c.getIdMascota() == target.getIdMascota() && "Completada".equalsIgnoreCase(c.getEstado()))
                        .sorted(java.util.Comparator.comparingInt(Cita::getIdCita))
                        .collect(java.util.stream.Collectors.toList());
                    
                    // Ordenar historiales por su ID para mantener la secuencia
                    historiales.sort(java.util.Comparator.comparingInt(com.saluspet.desktop.model.Historial::getIdHistorial));
                    
                    // Buscar índice de esta cita en la secuencia
                    int idx = -1;
                    for (int i = 0; i < citasMascota.size(); i++) {
                        if (citasMascota.get(i).getIdCita() == target.getIdCita()) {
                            idx = i;
                            break;
                        }
                    }
                    
                    if (idx != -1 && idx < historiales.size()) {
                        // ¡Match 1 a 1 perfecto por orden!
                        diagFinal = historiales.get(idx).getDescripcion();
                    } else {
                        // Fallback de emergencia si hay desajustes en DB (cálculo de fechas minDiff)
                        long minDiff = Long.MAX_VALUE;
                        com.saluspet.desktop.model.Historial matchedHistorial = null;
                        for (com.saluspet.desktop.model.Historial h : historiales) {
                            if (h.getDescripcion() == null || h.getDescripcion().trim().isEmpty()) continue;
                            String hF = h.getFecha(); String cF = target.getFecha();
                            if (hF != null && hF.contains("T")) hF = hF.split("T")[0];
                            if (cF != null && cF.contains("T")) cF = cF.split("T")[0];
                            try {
                                java.time.LocalDate hDate = java.time.LocalDate.parse(hF);
                                java.time.LocalDate cDate = java.time.LocalDate.parse(cF);
                                long diff = java.time.temporal.ChronoUnit.DAYS.between(cDate, hDate);
                                if (diff >= 0 && diff < minDiff) { minDiff = diff; matchedHistorial = h; }
                            } catch (Exception ignored) {}
                        }
                        if (matchedHistorial != null) diagFinal = matchedHistorial.getDescripcion();
                    }
                }
                
                final String safeDiagFinal = diagFinal;
                SwingUtilities.invokeLater(() -> {
                    setCursor(Cursor.getDefaultCursor());
                    btnVerPdf.setEnabled(true);
                    CitasPanel.generarYAbrirPdf(target, safeDiagFinal, actualVet, HistorialPanel.this);
                });
                return null;
            }).exceptionally(ex -> {
                SwingUtilities.invokeLater(() -> {
                    setCursor(Cursor.getDefaultCursor());
                    btnVerPdf.setEnabled(true);
                    JOptionPane.showMessageDialog(HistorialPanel.this, "Error al sincronizar historial histórico: " + ex.getMessage());
                });
                return null;
            });
        });

        navBar.add(new JLabel(" Buscador: "));
        searchField = new JTextField(20);
        searchField.putClientProperty("JTextField.placeholderText", "Buscar título o paciente...");
        searchField.putClientProperty("JTextField.showClearButton", true);
        searchField.setMargin(new Insets(4, 8, 4, 8));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { filterLocal(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { filterLocal(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { filterLocal(); }
        });
        navBar.add(searchField);
        
        navBar.add(btnVerPdf);
        navBar.add(Box.createHorizontalStrut(15));
        
        lblPaginacion.setVisible(true);
        navBar.add(lblPaginacion);
        navBar.add(Box.createHorizontalStrut(10));
        navBar.add(btnPrev);
        navBar.add(btnNext);
        
        topPanel.add(navBar);

        // --- ZONA CENTRAL: Tabla ---
        String[] columnNames = {"ID Cita", "Paciente", "Día", "Hora", "Tipo", "Asunto", "Estado"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(32);
        table.setFont(table.getFont().deriveFont(13f));
        table.getTableHeader().setFont(table.getFont().deriveFont(Font.BOLD, 13f));
        table.getTableHeader().setOpaque(false);
        table.getTableHeader().setBackground(new Color(168, 230, 207));
        table.getTableHeader().setForeground(Color.DARK_GRAY);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setCursor(new Cursor(Cursor.HAND_CURSOR));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(JLabel.CENTER);
        for(int i=0; i<table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRender);
        }
        
        ((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);

        table.getSelectionModel().addListSelectionListener(e -> {
            btnVerPdf.setEnabled(table.getSelectedRow() != -1);
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void cargarPagina(int page) {
        if (actualVet == null) return;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        citasService.obtenerCitasVeterinarioHistorialAsync(actualVet.getIdVeterinario(), page)
            .thenAccept(respuesta -> {
                SwingUtilities.invokeLater(() -> {
                    setCursor(Cursor.getDefaultCursor());
                    refrescarTabla(respuesta);
                });
            }).exceptionally(ex -> {
                SwingUtilities.invokeLater(() -> {
                    setCursor(Cursor.getDefaultCursor());
                    JOptionPane.showMessageDialog(this, "Error al cargar historial: " + ex.getMessage());
                });
                return null;
            });
    }

    private void refrescarTabla(RespuestaPaginadaCitas dto) {
        if (dto != null) {
            this.paginaActual = dto.getPaginaActual();
            int totalRegistros = dto.getTotal();
            
            // Calculamos total de páginas sabiendo que pageSize = 10
            this.totalPaginas = (int) Math.ceil((double) totalRegistros / 10.0);
            if (this.totalPaginas == 0) this.totalPaginas = 1;

            lblPaginacion.setText("Página " + this.paginaActual + " de " + this.totalPaginas);

            btnPrev.setVisible(this.paginaActual > 1);
            btnNext.setVisible(this.paginaActual < this.totalPaginas);

            loadDataToTable(dto.getCitas());
        } else {
            lblPaginacion.setText("Página 1 de 1");
            btnPrev.setVisible(false);
            btnNext.setVisible(false);
            loadDataToTable(new ArrayList<>());
        }
    }

    private void loadDataToTable(List<Cita> list) {
        tableModel.setRowCount(0); 
        this.currentCitas = list;
        if (list == null) return;

        for (Cita c : list) {
            Object[] row = {
                    c.getIdCita(),
                    (c.getNombreMascota() != null && !c.getNombreMascota().isEmpty()) ? c.getNombreMascota() : "ID " + c.getIdMascota(),
                    formatFechaEspanol(c.getFecha()),
                    (c.getHora() != null) ? c.getHora().substring(0, Math.min(c.getHora().length(), 5)) : "--:--",
                    c.getTipo() != null ? c.getTipo() : "N/A",
                    c.getTitulo() != null ? c.getTitulo() : "Sin Asunto",
                    c.getEstado() != null ? c.getEstado() : "Pendiente"
            };
            tableModel.addRow(row);
        }
        tableModel.fireTableDataChanged();
    }

    private String formatFechaEspanol(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) return "-";
        try {
            String datePart = isoDate.contains("T") ? isoDate.split("T")[0] : isoDate;
            String[] parts = datePart.split("-");
            if (parts.length >= 3) {
                return parts[2] + "/" + parts[1] + "/" + parts[0];
            }
        } catch(Exception ignored) {}
        return isoDate;
    }

    private void filterLocal() {
        String query = searchField.getText();
        if (query.trim().length() == 0) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + query));
        }
    }
}
