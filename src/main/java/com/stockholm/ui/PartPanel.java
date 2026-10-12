package com.stockholm.ui;

import com.stockholm.model.Part;
import com.stockholm.model.PartStatus;
import com.stockholm.model.Product;
import com.stockholm.repo.PartRepository;
import com.stockholm.repo.ProductRepository;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PartPanel extends JPanel {
    private final ProductRepository productRepository;
    private final PartRepository partRepository;
    private final JComboBox<Product> productBox = new JComboBox<>();
    private final PartTableModel tableModel = new PartTableModel();
    private final JTable table = new JTable(tableModel);

    private final JTextField nameField = new JTextField();
    private final JTextArea textArea = new JTextArea(5, 20);
    private final JFormattedTextField costField = new JFormattedTextField(BigDecimal.ZERO);
    private final JFormattedTextField quantityField = new JFormattedTextField(1);
    private final JTextField dateField = new JTextField(LocalDate.now().toString());
    private final JComboBox<PartStatus> statusBox = new JComboBox<>(PartStatus.values());

    private Part selectedPart;

    public PartPanel(ProductRepository productRepository, PartRepository partRepository) {
        this.productRepository = productRepository;
        this.partRepository = partRepository;

        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(this::handleSelection);

        add(buildTop(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildForm(), BorderLayout.EAST);

        refreshProducts();
    }

    private JComponent buildTop() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Produit"));
        panel.add(productBox);

        JButton load = new JButton("Charger");
        load.addActionListener(e -> refreshParts());
        panel.add(load);
        return panel;
    }

    private JComponent buildForm() {
        JPanel form = new JPanel(new BorderLayout(8, 8));
        form.setPreferredSize(new Dimension(380, 0));

        JPanel fields = new JPanel(new GridLayout(0, 2, 8, 8));
        fields.add(new JLabel("Nom piece"));
        fields.add(nameField);
        fields.add(new JLabel("Cout"));
        fields.add(costField);
        fields.add(new JLabel("Quantite"));
        fields.add(quantityField);
        fields.add(new JLabel("Date"));
        fields.add(dateField);
        fields.add(new JLabel("Statut"));
        fields.add(statusBox);

        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JPanel textPanel = new JPanel(new BorderLayout(6, 6));
        textPanel.add(new JLabel("Texte de suivi"), BorderLayout.NORTH);
        textPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(0, 1, 8, 8));
        JButton add = new JButton("Ajouter");
        add.addActionListener(e -> savePart());
        JButton delete = new JButton("Supprimer");
        delete.addActionListener(e -> deleteSelected());
        JButton clear = new JButton("Vider");
        clear.addActionListener(e -> clearForm());
        JButton refresh = new JButton("Actualiser");
        refresh.addActionListener(e -> refreshParts());

        buttons.add(add);
        buttons.add(delete);
        buttons.add(clear);
        buttons.add(refresh);

        form.add(fields, BorderLayout.NORTH);
        form.add(textPanel, BorderLayout.CENTER);
        form.add(buttons, BorderLayout.SOUTH);
        return form;
    }

    private void refreshProducts() {
        productBox.removeAllItems();
        for (Product product : productRepository.findAll()) {
            productBox.addItem(product);
        }
        if (productBox.getItemCount() > 0) {
            productBox.setSelectedIndex(0);
            refreshParts();
        } else {
            tableModel.setRows(List.of());
        }
    }

    private void refreshParts() {
        Product selectedProduct = getSelectedProduct();
        if (selectedProduct == null) {
            tableModel.setRows(List.of());
            return;
        }
        tableModel.setRows(partRepository.findByProductId(selectedProduct.id()));
    }

    private void handleSelection(ListSelectionEvent event) {
        if (event.getValueIsAdjusting()) {
            return;
        }
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        selectedPart = tableModel.getAt(table.convertRowIndexToModel(row));
        nameField.setText(selectedPart.name());
        textArea.setText(selectedPart.followUpText());
        costField.setValue(selectedPart.cost());
        quantityField.setValue(selectedPart.quantity());
        dateField.setText(selectedPart.followUpDate().toString());
        statusBox.setSelectedItem(selectedPart.status());
    }

    private void savePart() {
        try {
            Product product = getSelectedProduct();
            if (product == null) {
                DialogUtil.showError(this, "Selectionnez un produit");
                return;
            }
            Part part = new Part(
                    selectedPart == null ? null : selectedPart.id(),
                    product.id(),
                    require(nameField.getText(), "Nom de piece obligatoire"),
                    textArea.getText().trim(),
                    toMoney(costField.getValue()),
                    toInt(quantityField.getValue(), 1),
                    LocalDate.parse(dateField.getText().trim()),
                    (PartStatus) statusBox.getSelectedItem()
            );
            if (part.id() == null) {
                partRepository.insert(part);
            } else {
                partRepository.update(part);
            }
            updateProductPartsCost(product.id());
            refreshParts();
            clearForm();
            DialogUtil.showInfo(this, "Piece enregistree");
        } catch (Exception ex) {
            DialogUtil.showError(this, ex.getMessage());
        }
    }

    private void deleteSelected() {
        if (selectedPart == null) {
            DialogUtil.showError(this, "Selectionnez une piece");
            return;
        }
        if (DialogUtil.confirm(this, "Supprimer cette piece ?")) {
            partRepository.delete(selectedPart.id());
            updateProductPartsCost(getSelectedProduct().id());
            clearForm();
            refreshParts();
        }
    }

    private void updateProductPartsCost(long productId) {
        BigDecimal total = partRepository.sumCostByProduct(productId);
        productRepository.findById(productId).ifPresent(product -> productRepository.update(
                new Product(
                        product.id(),
                        product.name(),
                        product.purchasePlatform(),
                        product.purchasePrice(),
                        product.shippingCost(),
                        product.purchaseDate(),
                        product.initialDiagnosis(),
                        product.timeSpentMinutes(),
                        total,
                        product.status(),
                        product.saleChannel(),
                        product.salePrice(),
                        product.saleFee()
                )));
    }

    private void clearForm() {
        selectedPart = null;
        table.clearSelection();
        nameField.setText("");
        textArea.setText("");
        costField.setValue(BigDecimal.ZERO);
        quantityField.setValue(1);
        dateField.setText(LocalDate.now().toString());
        statusBox.setSelectedItem(PartStatus.EN_ATTENTE);
    }

    private Product getSelectedProduct() {
        return (Product) productBox.getSelectedItem();
    }

    private String require(String text, String message) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return text.trim();
    }

    private BigDecimal toMoney(Object value) {
        try {
            if (value instanceof BigDecimal bigDecimal) {
                return bigDecimal.setScale(2, java.math.RoundingMode.HALF_UP);
            }
            return new BigDecimal(String.valueOf(value).trim()).setScale(2, java.math.RoundingMode.HALF_UP);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Cout invalide");
        }
    }

    private int toInt(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    private static final class PartTableModel extends AbstractTableModel {
        private final String[] columns = {"ID", "Nom", "Cout", "Qt", "Date", "Statut"};
        private List<Part> rows = List.of();

        void setRows(List<Part> rows) {
            this.rows = rows;
            fireTableDataChanged();
        }

        Part getAt(int row) {
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
        public Object getValueAt(int rowIndex, int columnIndex) {
            Part part = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> part.id();
                case 1 -> part.name();
                case 2 -> part.cost();
                case 3 -> part.quantity();
                case 4 -> part.followUpDate();
                case 5 -> part.status();
                default -> "";
            };
        }
    }
}
