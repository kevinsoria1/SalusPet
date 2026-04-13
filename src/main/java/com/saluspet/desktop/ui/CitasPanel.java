package com.saluspet.desktop.ui;

import com.saluspet.desktop.model.Cita;
import com.saluspet.desktop.model.Historial;
import com.saluspet.desktop.model.Veterinario;
import com.saluspet.desktop.network.CitasService;
import com.saluspet.desktop.network.HistorialesService;
import com.saluspet.desktop.utils.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;

public class CitasPanel extends JPanel {

    private List<Cita> citasList;
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTextField searchField;
    private JComboBox<String> filterCombo;
    private JButton btnAccion;
    private JLabel lblPaginacion;

    private final CitasService citasService;
    private final HistorialesService historialesService;
    private final Veterinario actualVet;

    public CitasPanel() {
        this.citasList = new ArrayList<>();
        this.citasService = new CitasService();
        this.historialesService = new HistorialesService();
        this.actualVet = SessionManager.getInstance().getVeterinarioActual();
        
        initComponents();
        recargarDatos();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(UIManager.getColor("Panel.background"));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- BARRRA SUPERIOR: Buscador e Interacciones ---
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new javax.swing.BoxLayout(topPanel, javax.swing.BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = new JLabel("Agenda de Citas");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 22f));
        titleLabel.setForeground(new Color(25, 90, 80));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        topPanel.add(titleLabel);
        
        topPanel.add(Box.createVerticalStrut(10));

        JPanel searchContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchContainer.setOpaque(false);
        searchContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        filterCombo = new JComboBox<>(new String[]{"Mis Citas (Aceptadas)", "Citas Pendientes de Asignar", "Todas las Citas (General)"});
        filterCombo.addActionListener(e -> recargarDatos());
        
        searchContainer.add(filterCombo);
        searchContainer.add(new JLabel(" Buscador: "));
        searchField = new JTextField(20);
        searchField.putClientProperty("JTextField.placeholderText", "Buscar título o estado...");
        searchField.putClientProperty("JTextField.showClearButton", true);
        searchField.setMargin(new Insets(4, 8, 4, 8));
        searchContainer.add(searchField);

        btnAccion = new JButton("Acción");
        btnAccion.setEnabled(false);
        btnAccion.putClientProperty("JButton.buttonType", "roundRect");
        btnAccion.setBackground(new Color(168, 230, 207)); // Pastel green
        btnAccion.setForeground(Color.DARK_GRAY);
        searchContainer.add(btnAccion);

        lblPaginacion = new JLabel("Página 1 de 1");
        lblPaginacion.setVisible(false);
        searchContainer.add(Box.createHorizontalStrut(15));
        searchContainer.add(lblPaginacion);

        topPanel.add(searchContainer);

        // --- ZONA CENTRAL: Tabla ---
        String[] columnNames = {"ID Cita", "Paciente", "Día", "Hora", "Tipo", "Título", "Estado"};
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
        
        // Centrar columnas tabulares
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

        // --- LISTENERS ---
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterLocal(); }
            @Override public void removeUpdate(DocumentEvent e) { filterLocal(); }
            @Override public void changedUpdate(DocumentEvent e) { filterLocal(); }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            actualizarEstadoBotonAccion();
        });

        btnAccion.addActionListener(e -> {
            if (filterCombo.getSelectedIndex() == 1) {
                aceptarCitaSeleccionada();
            } else if (filterCombo.getSelectedIndex() == 0) {
                finalizarConsultaSeleccionada();
            }
        });
    }

    private void actualizarEstadoBotonAccion() {
        boolean hasSelection = table.getSelectedRow() != -1;
        int nav = filterCombo.getSelectedIndex();
        btnAccion.setEnabled(hasSelection && (nav == 1 || nav == 0));

        if (nav == 1) {
            btnAccion.setText("Asignarme Cita");
            btnAccion.setBackground(new Color(168, 230, 207)); // Pastel green
            btnAccion.setForeground(Color.DARK_GRAY);
        } else if (nav == 0) {
            // Solo permitir si el estado no es ya Completada
            if (hasSelection) {
                int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
                Cita target = citasList.get(modelRow);
                if ("Completada".equalsIgnoreCase(target.getEstado())) {
                    btnAccion.setEnabled(false);
                }
            }
            btnAccion.setText("Completar Consulta");
            btnAccion.setBackground(new Color(135, 206, 250)); // Pastel blue
            btnAccion.setForeground(Color.DARK_GRAY);
        } else {
            btnAccion.setText("Solo Lectura");
            btnAccion.setEnabled(false);
            btnAccion.setBackground(Color.LIGHT_GRAY);
            btnAccion.setForeground(Color.DARK_GRAY);
        }
    }

    private void recargarDatos() {
        if(actualVet == null) return;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        int navObj = filterCombo.getSelectedIndex();
        actualizarEstadoBotonAccion();
        // 0 -> Mis Citas (todas filtradas por mi ID)
        // 1 -> Pendientes (llamar al endpoint de SinAsignar)
        // 2 -> Todas

        if (navObj == 1) { // Pendientes
            lblPaginacion.setVisible(false);
            citasService.obtenerCitasSinAsignarAsync().thenAccept(citas -> {
                SwingUtilities.invokeLater(() -> setCitas(citas));
            }).exceptionally(ex -> handleError(ex));
        } else if (navObj == 0) { // Mis Citas (Confirmadas via Backend Paginated)
            citasService.obtenerCitasVeterinarioConfirmadasAsync(actualVet.getIdVeterinario())
                .thenAccept(respuesta -> {
                    SwingUtilities.invokeLater(() -> {
                        refrescarTabla(respuesta);
                    });
                }).exceptionally(ex -> handleError(ex));
        } else { // Todas (General)
            lblPaginacion.setVisible(false);
            citasService.obtenerCitasAsync().thenAccept(todas -> {
                SwingUtilities.invokeLater(() -> {
                    // Filtrado global de "Personal" para no mostrarlas en Desktop nunca:
                    java.util.List<Cita> soloVets = todas.stream()
                        .filter(c -> "Veterinaria".equalsIgnoreCase(c.getTipo()))
                        .collect(java.util.stream.Collectors.toList());
                    setCitas(soloVets);
                });
            }).exceptionally(ex -> handleError(ex));
        }
    }

    private void refrescarTabla(com.saluspet.desktop.network.RespuestaPaginadaCitas dto) {
        lblPaginacion.setVisible(true);
        if (dto != null) {
            int y = (int) Math.ceil((double) dto.getTotal() / 10.0);
            if (y == 0) y = 1;
            lblPaginacion.setText("Página " + dto.getPaginaActual() + " de " + y);
            setCitas(dto.getCitas());
        } else {
            lblPaginacion.setText("Página 1 de 1");
            setCitas(new java.util.ArrayList<>());
        }
    }

    private Void handleError(Throwable ex) {
        SwingUtilities.invokeLater(() -> {
            setCursor(Cursor.getDefaultCursor());
            JOptionPane.showMessageDialog(this, "Fallo al conectar con servidor: " + ex.getMessage());
        });
        return null;
    }

    private void setCitas(java.util.List<Cita> list) {
        this.citasList = list != null ? list : new java.util.ArrayList<>();
        SwingUtilities.invokeLater(() -> {
            setCursor(Cursor.getDefaultCursor());
            loadDataToTable();
            tableModel.fireTableDataChanged();
        });
    }

    private void filterLocal() {
        String query = searchField.getText();
        if (query.trim().length() == 0) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + query));
        }
    }

    private void loadDataToTable() {
        tableModel.setRowCount(0); 
        for (Cita c : citasList) {
            String identidadMascota = (c.getNombreMascota() != null && !c.getNombreMascota().trim().isEmpty()) 
                                        ? c.getNombreMascota() 
                                        : "ID " + c.getIdMascota();

            Object[] row = {
                    c.getIdCita(),
                    identidadMascota,
                    formatFechaEspanol(c.getFecha()),
                    (c.getHora() != null) ? c.getHora().substring(0, Math.min(c.getHora().length(), 5)) : "--:--",
                    c.getTipo() != null ? c.getTipo() : "N/A",
                    c.getTitulo() != null ? c.getTitulo() : "Sin Asunto",
                    c.getEstado() != null ? c.getEstado() : "Pendiente"
            };
            tableModel.addRow(row);
        }
    }

    private void aceptarCitaSeleccionada() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) return;
        
        int modelRow = table.convertRowIndexToModel(viewRow);
        Cita target = citasList.get(modelRow);
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        // 1. Obtener la Cita COMPLETA primero
        citasService.obtenerCitaPorIdAsync(target.getIdCita()).thenCompose(completa -> {
            // 2. Modificar la Cita COMPLETA
            completa.setIdVeterinario(actualVet.getIdVeterinario());
            completa.setEstado("Confirmada");

            // Imprimir el objeto a enviar para debugging local
            System.out.println("Enviando PUT Cita con Estado: " + completa.getEstado() + " y Vet: " + completa.getIdVeterinario());

            // 3. Ejecutar PUT
            return citasService.actualizarCitaAsync(completa);
        }).thenAccept(ok -> {
            SwingUtilities.invokeLater(() -> {
                setCursor(Cursor.getDefaultCursor());
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Cita aceptada y asignada con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    recargarDatos();
                } else {
                    JOptionPane.showMessageDialog(this, "El servidor rechazó la aceptación de la Cita completa.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }).exceptionally(ex -> handleError(ex));
    }

    private void finalizarConsultaSeleccionada() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) return;
        
        int modelRow = table.convertRowIndexToModel(viewRow);
        Cita target = citasList.get(modelRow);

        JPanel formPanel = new JPanel(new BorderLayout(5, 5));
        formPanel.add(new JLabel("Informe / Diagnóstico (Obligatorio):"), BorderLayout.NORTH);
        JTextArea txtDiag = new JTextArea(6, 30);
        txtDiag.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        formPanel.add(new JScrollPane(txtDiag), BorderLayout.CENTER);

        int res = JOptionPane.showConfirmDialog(this, formPanel, "Registrar Historial Médico", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String descripcion = txtDiag.getText().trim();

            if (descripcion.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El informe es obligatorio.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            
            // Construir el objeto de historial según el nuevo modelo
            // "idMascota": 1, "idVeterinario": 5, "tipoEvento": "Consulta", "fecha": "2026-04-02", ...
            String fechaHoy = java.time.LocalDate.now().toString();
            Historial h = new Historial(target.getIdMascota(), actualVet.getIdVeterinario(), "Consulta", fechaHoy, descripcion, null);
            
            historialesService.crearHistorialAsync(h).thenCompose(histOk -> {
                if (!histOk) throw new RuntimeException("No se guardó el historial en BBDD.");
                return citasService.obtenerCitaPorIdAsync(target.getIdCita());
            }).thenCompose(completa -> {
                completa.setEstado("Completada");
                return citasService.actualizarCitaAsync(completa);
            }).thenAccept(citaOk -> {
                SwingUtilities.invokeLater(() -> {
                    setCursor(Cursor.getDefaultCursor());
                    if (citaOk) {
                        generarYAbrirPdf(target, descripcion);
                        JOptionPane.showMessageDialog(this, "Historial archivado y Cita completada. PDF generado.", "Excelente", JOptionPane.INFORMATION_MESSAGE);
                        recargarDatos();
                    } else {
                        JOptionPane.showMessageDialog(this, "El historial se guardó, pero falló el cambio de estado de la Cita.", "Atención", JOptionPane.WARNING_MESSAGE);
                    }
                });
            }).exceptionally(ex -> handleError(ex));
        }
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

    private void generarYAbrirPdf(Cita cita, String diagnostico) {
        try {
            File pdfFile = File.createTempFile("informe_" + cita.getIdMascota() + "_" + System.currentTimeMillis(), ".pdf");
            pdfFile.deleteOnExit();

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, com.itextpdf.text.BaseColor.BLUE);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, com.itextpdf.text.BaseColor.DARK_GRAY);
            Font pFont = FontFactory.getFont(FontFactory.HELVETICA, 12, com.itextpdf.text.BaseColor.BLACK);

            Paragraph title = new Paragraph("INFORME CLÍNICO - SALUSPET", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            document.add(new Paragraph("Veterinario Responsable: " + actualVet.getNombre() + " (" + actualVet.getEspecialidad() + ")", headerFont));
            document.add(new Paragraph("Fecha de la Consulta: " + formatFechaEspanol(cita.getFecha()) + " a las " + cita.getHora(), headerFont));
            document.add(new Paragraph("\n"));

            String mNombre = cita.getNombreMascota() != null && !cita.getNombreMascota().isEmpty() ? cita.getNombreMascota() : "ID: " + cita.getIdMascota();
            document.add(new Paragraph("Paciente: " + mNombre, pFont));
            document.add(new Paragraph("Asunto Inicial: " + (cita.getTitulo() != null ? cita.getTitulo() : "No especificado"), pFont));
            document.add(new Paragraph("\n"));

            Paragraph diagTitle = new Paragraph("DIAGNÓSTICO Y TRATAMIENTO", headerFont);
            diagTitle.setSpacingAfter(10);
            document.add(diagTitle);

            Paragraph diagBody = new Paragraph(diagnostico, pFont);
            document.add(diagBody);

            document.add(new Paragraph("\n\nFirma:\n______________________\n" + actualVet.getNombre(), pFont));

            document.close();

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(pdfFile);
            }
        } catch (Exception e) {
            System.err.println("Fallo al generar PDF: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "No se pudo generar o abrir el archivo PDF:\n" + e.getMessage(), "Error PDF", JOptionPane.WARNING_MESSAGE);
        }
    }
}
