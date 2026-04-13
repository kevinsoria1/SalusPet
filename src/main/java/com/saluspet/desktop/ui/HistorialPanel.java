package com.saluspet.desktop.ui;

import com.saluspet.desktop.model.Cita;
import com.saluspet.desktop.model.Veterinario;
import com.saluspet.desktop.network.CitasService;
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
    private JLabel lblPaginacion;
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> rowSorter;

    private int paginaActual = 1;
    private int totalPaginas = 1;

    private final CitasService citasService;
    private final Veterinario actualVet;

    public HistorialPanel() {
        this.citasService = new CitasService();
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
        lblPaginacion = new JLabel("Página 1 de 1");

        btnPrev.putClientProperty("JButton.buttonType", "roundRect");
        btnNext.putClientProperty("JButton.buttonType", "roundRect");

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

        navBar.add(btnPrev);
        navBar.add(lblPaginacion);
        navBar.add(btnNext);
        
        navBar.add(Box.createHorizontalStrut(25));
        navBar.add(new JLabel(" Buscar local: "));
        searchField = new JTextField(20);
        searchField.putClientProperty("JTextField.placeholderText", "Buscar título o paciente...");
        searchField.putClientProperty("JTextField.showClearButton", true);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { filterLocal(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { filterLocal(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { filterLocal(); }
        });
        navBar.add(searchField);
        
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

            btnPrev.setEnabled(this.paginaActual > 1);
            btnNext.setEnabled(this.paginaActual < this.totalPaginas);

            loadDataToTable(dto.getCitas());
        } else {
            lblPaginacion.setText("Página 1 de 1");
            btnPrev.setEnabled(false);
            btnNext.setEnabled(false);
            loadDataToTable(new ArrayList<>());
        }
    }

    private void loadDataToTable(List<Cita> list) {
        tableModel.setRowCount(0); 
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
