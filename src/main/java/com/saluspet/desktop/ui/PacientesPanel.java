package com.saluspet.desktop.ui;

import com.saluspet.desktop.model.Mascota;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class PacientesPanel extends JPanel {

    private final List<Mascota> mascotasList;
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTextField searchField;

    public PacientesPanel(List<Mascota> mascotas) {
        this.mascotasList = mascotas;
        initComponents();
        loadDataToTable();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(UIManager.getColor("Panel.background"));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- BARRRA SUPERIOR: Buscador ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = new JLabel("Directorio de Pacientes");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 22f));
        titleLabel.setForeground(new Color(25, 90, 80));

        JPanel searchContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchContainer.setOpaque(false);
        searchContainer.add(new JLabel("Buscador: "));
        searchField = new JTextField(25);
        searchField.putClientProperty("JTextField.placeholderText", "Escribe nombre, especie o género...");
        searchField.putClientProperty("JTextField.showClearButton", true);
        searchField.setMargin(new Insets(4, 8, 4, 8));
        searchContainer.add(searchField);

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(searchContainer, BorderLayout.EAST);

        // --- ZONA CENTRAL: Tabla ---
        String[] columnNames = {"Cód", "Nombre", "Especie", "Género", "Fecha Nacimiento"};
        // El DefaultTableModel se instancia desactivando la edición de celdas
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
        
        // Centrar todas las columnas
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

        // --- LISTENERS REACTIVOS ---
        // Filtrado dinámico (tecla a tecla)
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filter(); }
            @Override public void removeUpdate(DocumentEvent e) { filter(); }
            @Override public void changedUpdate(DocumentEvent e) { filter(); }
        });

        // Click en la celda para mostrar Ficha Técnica
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                if (me.getClickCount() == 1 && table.getSelectedRow() != -1) {
                    mostrarDetallesMascota();
                }
            }
        });
    }

    private void filter() {
        String query = searchField.getText();
        if (query.trim().length() == 0) {
            rowSorter.setRowFilter(null);
        } else {
            // Filtrado global usando RegEx (insensible a mayusculas/minusculas)
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + query));
        }
    }

    private void loadDataToTable() {
        if (mascotasList == null) return;
        tableModel.setRowCount(0); // Vaciar por si acaso

        for (Mascota m : mascotasList) {
            Object[] row = {
                    m.getIdMascota(),
                    m.getNombre() != null ? m.getNombre() : "S/N",
                    m.getEspecie() != null ? m.getEspecie() : "?",
                    m.getGenero() != null ? m.getGenero() : "Desconocido",
                    formatFechaEspanol(m.getFechaNacimiento())
            };
            tableModel.addRow(row);
        }
    }

    /**
     * Algoritmo veloz para re-formatear Fechas ISO-8601 del Backend C#
     */
    private String formatFechaEspanol(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) return "Borrador";
        try {
            String datePart = isoDate;
            if(isoDate.contains("T")) {
                datePart = isoDate.split("T")[0]; // Coger solo el YYYY-MM-DD
            }
            String[] parts = datePart.split("-");
            if (parts.length >= 3) {
                return parts[2] + "/" + parts[1] + "/" + parts[0];
            }
        } catch(Exception ignored) {}
        return isoDate;
    }

    /**
     * Popup / Modal visual mostrando todo el scope de la base de datos
     */
    private void mostrarDetallesMascota() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) return;
        
        // El Sorcer/Filtrer mezcla las IDs visuales con las reales, usamos convertRowIndexToModel!
        int modelRow = table.convertRowIndexToModel(viewRow);
        Mascota mascota = mascotasList.get(modelRow);

        // Lanzamos una ventana gráfica totalmente independiente (JFrame) sin bloquear el Dashboard
        DetalleMascotaFrame ficha = new DetalleMascotaFrame(mascota);
        ficha.setVisible(true);
    }
}
