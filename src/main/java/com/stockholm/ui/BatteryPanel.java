package com.stockholm.ui;

import com.stockholm.model.Battery;
import com.stockholm.model.BatteryCell;
import com.stockholm.model.BatteryCellState;
import com.stockholm.repo.BatteryRepository;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BatteryPanel extends JPanel {
    private final BatteryRepository repository;
    private final BatteryTableModel tableModel = new BatteryTableModel();
    private final JTable table = new JTable(tableModel);
    private final JTextField modelField = new JTextField();
    private final JSpinner cellCountField = new JSpinner(new SpinnerNumberModel(1, 1, 500, 1));
    private final JFormattedTextField voltageField = new JFormattedTextField(0.0);
    private final JFormattedTextField capacityField = new JFormattedTextField(0.0);
    private final JTextField formatField = new JTextField();
    private final JTextArea notesArea = new JTextArea(4, 20);
    private final JPanel cellsPanel = new JPanel(new GridLayout(0, 4, 6, 6));
    private final List<JComboBox<BatteryCellState>> cellSelectors = new ArrayList<>();
    private Battery selectedBattery;

    public BatteryPanel(BatteryRepository repository) {
        this.repository = repository;
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(26);
        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                loadBattery(tableModel.getAt(table.convertRowIndexToModel(table.getSelectedRow())));
            }
        });

        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(cellCountField, "0");
        cellCountField.setEditor(editor);
        cellCountField.addChangeListener(event -> rebuildCellFields(null));

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildForm(), BorderLayout.EAST);
        refresh();
    }

    private JComponent buildForm() {
        JPanel form = new JPanel(new BorderLayout(8, 8));
        form.setPreferredSize(new Dimension(440, 0));

        JPanel fields = new JPanel(new GridLayout(0, 2, 8, 8));
        fields.add(new JLabel("Modèle"));
        fields.add(modelField);
        fields.add(new JLabel("Nombre de cellules"));
        fields.add(cellCountField);
        fields.add(new JLabel("Tension (V)"));
        fields.add(voltageField);
        fields.add(new JLabel("Capacité (Ah)"));
        fields.add(capacityField);
        fields.add(new JLabel("Format"));
        fields.add(formatField);

        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JPanel notes = new JPanel(new BorderLayout(6, 6));
        notes.add(new JLabel("Notes"), BorderLayout.NORTH);
        notes.add(new JScrollPane(notesArea), BorderLayout.CENTER);

        JPanel cellContainer = new JPanel(new BorderLayout(6, 6));
        cellContainer.add(new JLabel("État de chaque cellule"), BorderLayout.NORTH);
        cellContainer.add(new JScrollPane(cellsPanel), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(0, 1, 8, 8));
        JButton save = new JButton("Enregistrer");
        save.addActionListener(event -> saveBattery());
        JButton clear = new JButton("Nouvelle batterie");
        clear.addActionListener(event -> clearForm());
        JButton delete = new JButton("Supprimer");
        delete.addActionListener(event -> deleteBattery());
        buttons.add(save);
        buttons.add(clear);
        buttons.add(delete);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.add(fields, BorderLayout.NORTH);
        center.add(notes, BorderLayout.CENTER);
        center.add(cellContainer, BorderLayout.SOUTH);
        form.add(center, BorderLayout.CENTER);
        form.add(buttons, BorderLayout.SOUTH);
        return form;
    }

    private void rebuildCellFields(List<BatteryCellState> states) {
        int count = (Integer) cellCountField.getValue();
        cellsPanel.removeAll();
        cellSelectors.clear();
        for (int index = 0; index < count; index++) {
            JPanel cell = new JPanel(new BorderLayout(4, 4));
            cell.add(new JLabel("Cellule " + (index + 1)), BorderLayout.NORTH);
            JComboBox<BatteryCellState> selector = new JComboBox<>(BatteryCellState.values());
            if (states != null && index < states.size()) {
                selector.setSelectedItem(states.get(index));
            }
            cell.add(selector, BorderLayout.CENTER);
            cellSelectors.add(selector);
            cellsPanel.add(cell);
        }
        cellsPanel.revalidate();
        cellsPanel.repaint();
    }

    private void loadBattery(Battery battery) {
        selectedBattery = battery;
        modelField.setText(battery.model());
        cellCountField.setValue(battery.cellCount());
        voltageField.setValue(battery.voltage());
        capacityField.setValue(battery.capacityAh());
        formatField.setText(battery.format());
        notesArea.setText(battery.notes());
        List<BatteryCellState> states = repository.findCells(battery.id()).stream().map(BatteryCell::state).toList();
        rebuildCellFields(states);
    }

    private void saveBattery() {
        try {
            String model = modelField.getText().trim();
            if (model.isEmpty()) {
                throw new IllegalArgumentException("Le modèle est obligatoire");
            }
            int cellCount = (Integer) cellCountField.getValue();
            Battery battery = new Battery(
                    selectedBattery == null ? null : selectedBattery.id(),
                    model,
                    cellCount,
                    number(voltageField.getValue(), "Tension invalide"),
                    number(capacityField.getValue(), "Capacité invalide"),
                    formatField.getText().trim(),
                    notesArea.getText().trim()
            );
            long id;
            if (battery.id() == null) {
                id = repository.insert(battery);
            } else {
                id = battery.id();
                repository.update(battery);
            }
            List<BatteryCellState> states = cellSelectors.stream()
                    .map(selector -> (BatteryCellState) selector.getSelectedItem())
                    .toList();
            repository.replaceCells(id, states);
            refresh();
            DialogUtil.showInfo(this, "Batterie enregistrée");
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    private void deleteBattery() {
        if (selectedBattery == null) {
            DialogUtil.showError(this, "Sélectionnez une batterie");
            return;
        }
        if (DialogUtil.confirm(this, "Supprimer cette batterie et ses cellules ?")) {
            repository.delete(selectedBattery.id());
            clearForm();
            refresh();
        }
    }

    private void clearForm() {
        selectedBattery = null;
        table.clearSelection();
        modelField.setText("");
        cellCountField.setValue(1);
        voltageField.setValue(0.0);
        capacityField.setValue(0.0);
        formatField.setText("");
        notesArea.setText("");
        rebuildCellFields(null);
    }

    private void refresh() {
        tableModel.setRows(repository.findAll());
        if (selectedBattery == null) {
            rebuildCellFields(null);
        }
    }

    private double number(Object value, String message) {
        try {
            return Double.parseDouble(String.valueOf(value).trim());
        } catch (Exception exception) {
            throw new IllegalArgumentException(message);
        }
    }

    private static final class BatteryTableModel extends AbstractTableModel {
        private final String[] columns = {"ID", "Modèle", "Cellules", "Tension (V)", "Capacité (Ah)", "Format"};
        private List<Battery> rows = List.of();

        void setRows(List<Battery> rows) {
            this.rows = rows;
            fireTableDataChanged();
        }

        Battery getAt(int row) {
            return rows.get(row);
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int row, int column) {
            Battery battery = rows.get(row);
            return switch (column) {
                case 0 -> battery.id();
                case 1 -> battery.model();
                case 2 -> battery.cellCount();
                case 3 -> battery.voltage();
                case 4 -> battery.capacityAh();
                case 5 -> battery.format();
                default -> "";
            };
        }
    }
}
