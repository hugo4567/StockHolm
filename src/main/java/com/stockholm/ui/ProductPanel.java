package com.stockholm.ui;

import com.stockholm.model.Product;
import com.stockholm.model.ProductStatus;
import com.stockholm.repo.PartRepository;
import com.stockholm.repo.ProductRepository;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ProductPanel extends JPanel {
    private final ProductRepository productRepository;
    private final PartRepository partRepository;
    private final ProductTableModel tableModel = new ProductTableModel();
    private final JTable table = new JTable(tableModel);

    private final JTextField nameField = new JTextField();
    private final JTextField platformField = new JTextField("Vinted");
    private final JFormattedTextField purchasePriceField = new JFormattedTextField(BigDecimal.ZERO);
    private final JFormattedTextField shippingCostField = new JFormattedTextField(BigDecimal.ZERO);
    private final JTextField purchaseDateField = new JTextField(LocalDate.now().toString());
    private final JTextArea diagnosisArea = new JTextArea(5, 20);
    private final JFormattedTextField timeSpentField = new JFormattedTextField(0);
    private final JFormattedTextField partsCostField = new JFormattedTextField(BigDecimal.ZERO);
    private final JComboBox<ProductStatus> statusBox = new JComboBox<>(ProductStatus.values());
    private final JTextField saleChannelField = new JTextField("Vinted");
    private final JFormattedTextField salePriceField = new JFormattedTextField();
    private final JFormattedTextField saleFeeField = new JFormattedTextField(BigDecimal.ZERO);

    private Product selectedProduct;

    public ProductPanel(ProductRepository productRepository, PartRepository partRepository) {
        this.productRepository = productRepository;
        this.partRepository = partRepository;

        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(26);
        table.getSelectionModel().addListSelectionListener(this::handleSelection);

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildForm(), BorderLayout.EAST);

        refresh();
    }

    private JComponent buildForm() {
        JPanel form = new JPanel(new BorderLayout(8, 8));
        form.setPreferredSize(new Dimension(420, 0));
        form.add(new JLabel("Produit"), BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(0, 2, 8, 8));
        fields.add(new JLabel("Nom"));
        fields.add(nameField);
        fields.add(new JLabel("Plateforme achat"));
        fields.add(platformField);
        fields.add(new JLabel("Prix achat"));
        fields.add(purchasePriceField);
        fields.add(new JLabel("Frais de port"));
        fields.add(shippingCostField);
        fields.add(new JLabel("Date"));
        fields.add(purchaseDateField);
        fields.add(new JLabel("Temps passé (min)"));
        fields.add(timeSpentField);
        fields.add(new JLabel("Pièces achetées"));
        fields.add(partsCostField);
        fields.add(new JLabel("Statut"));
        fields.add(statusBox);
        fields.add(new JLabel("Canal de revente"));
        fields.add(saleChannelField);
        fields.add(new JLabel("Prix vente"));
        fields.add(salePriceField);
        fields.add(new JLabel("Frais vente"));
        fields.add(saleFeeField);

        diagnosisArea.setLineWrap(true);
        diagnosisArea.setWrapStyleWord(true);
        JPanel diagnosisPanel = new JPanel(new BorderLayout(6, 6));
        diagnosisPanel.add(new JLabel("Diagnostic initial"), BorderLayout.NORTH);
        diagnosisPanel.add(new JScrollPane(diagnosisArea), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(0, 1, 8, 8));
        JButton save = new JButton("Enregistrer");
        save.addActionListener(e -> saveProduct());
        JButton clear = new JButton("Nouveau");
        clear.addActionListener(e -> clearForm());
        JButton delete = new JButton("Supprimer");
        delete.addActionListener(e -> deleteSelected());
        JButton parts = new JButton("Mettre à jour les pièces");
        parts.addActionListener(e -> updatePartsCost());

        buttons.add(save);
        buttons.add(clear);
        buttons.add(delete);
        buttons.add(parts);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.add(fields, BorderLayout.NORTH);
        center.add(diagnosisPanel, BorderLayout.CENTER);

        form.add(center, BorderLayout.CENTER);
        form.add(buttons, BorderLayout.SOUTH);
        return form;
    }

    private void handleSelection(ListSelectionEvent event) {
        if (event.getValueIsAdjusting()) {
            return;
        }
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        selectedProduct = tableModel.getAt(table.convertRowIndexToModel(row));
        loadProduct(selectedProduct);
    }

    private void loadProduct(Product product) {
        nameField.setText(product.name());
        platformField.setText(product.purchasePlatform());
        purchasePriceField.setValue(product.purchasePrice());
        shippingCostField.setValue(product.shippingCost());
        purchaseDateField.setText(product.purchaseDate().toString());
        diagnosisArea.setText(product.initialDiagnosis());
        timeSpentField.setValue(product.timeSpentMinutes());
        partsCostField.setValue(product.partsCost());
        statusBox.setSelectedItem(product.status());
        saleChannelField.setText(product.saleChannel());
        salePriceField.setValue(product.salePrice());
        saleFeeField.setValue(product.saleFee());
    }

    private void clearForm() {
        selectedProduct = null;
        table.clearSelection();
        nameField.setText("");
        platformField.setText("Vinted");
        purchasePriceField.setValue(BigDecimal.ZERO);
        shippingCostField.setValue(BigDecimal.ZERO);
        purchaseDateField.setText(LocalDate.now().toString());
        diagnosisArea.setText("");
        timeSpentField.setValue(0);
        partsCostField.setValue(BigDecimal.ZERO);
        statusBox.setSelectedItem(ProductStatus.EN_ATTENTE);
        saleChannelField.setText("Vinted");
        salePriceField.setValue(null);
        saleFeeField.setValue(BigDecimal.ZERO);
    }

    private void saveProduct() {
        try {
            Product product = new Product(
                    selectedProduct == null ? null : selectedProduct.id(),
                    require(nameField.getText(), "Le nom est obligatoire"),
                    defaultText(platformField.getText(), "Vinted"),
                    toMoney(purchasePriceField.getValue(), "Prix d'achat invalide"),
                    toMoney(shippingCostField.getValue(), "Frais de port invalides"),
                    LocalDate.parse(purchaseDateField.getText().trim()),
                    diagnosisArea.getText().trim(),
                    toInt(timeSpentField.getValue(), "Temps passé invalide"),
                    toMoney(partsCostField.getValue(), "Valeur des pièces invalide"),
                    (ProductStatus) statusBox.getSelectedItem(),
                    defaultText(saleChannelField.getText(), "Vinted"),
                    salePriceField.getValue() == null ? null : toMoney(salePriceField.getValue(), "Prix de vente invalide"),
                    toMoney(saleFeeField.getValue(), "Frais de vente invalides")
            );

            if (product.id() == null) {
                long id = productRepository.insert(product);
                selectedProduct = productRepository.findById(id).orElse(product);
            } else {
                productRepository.update(product);
                selectedProduct = product;
            }
            refresh();
            DialogUtil.showInfo(this, "Produit enregistré");
        } catch (Exception ex) {
            DialogUtil.showError(this, ex.getMessage());
        }
    }

    private void deleteSelected() {
        if (selectedProduct == null) {
            DialogUtil.showError(this, "Selectionnez un produit");
            return;
        }
        if (DialogUtil.confirm(this, "Supprimer ce produit et ses pièces ?")) {
            productRepository.delete(selectedProduct.id());
            selectedProduct = null;
            clearForm();
            refresh();
        }
    }

    private void updatePartsCost() {
        if (selectedProduct == null) {
            DialogUtil.showError(this, "Selectionnez un produit");
            return;
        }
        BigDecimal total = partRepository.sumCostByProduct(selectedProduct.id());
        Product updated = new Product(
                selectedProduct.id(),
                selectedProduct.name(),
                selectedProduct.purchasePlatform(),
                selectedProduct.purchasePrice(),
                selectedProduct.shippingCost(),
                selectedProduct.purchaseDate(),
                selectedProduct.initialDiagnosis(),
                selectedProduct.timeSpentMinutes(),
                total,
                selectedProduct.status(),
                selectedProduct.saleChannel(),
                selectedProduct.salePrice(),
                selectedProduct.saleFee()
        );
        productRepository.update(updated);
        selectedProduct = updated;
        refresh();
        loadProduct(updated);
    }

    private void refresh() {
        tableModel.setRows(productRepository.findAll());
    }

    private String require(String text, String message) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return text.trim();
    }

    private String defaultText(String text, String fallback) {
        return text == null || text.trim().isEmpty() ? fallback : text.trim();
    }

    private BigDecimal toMoney(Object value, String message) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        try {
            if (value instanceof BigDecimal bigDecimal) {
                return bigDecimal.setScale(2, java.math.RoundingMode.HALF_UP);
            }
            return new BigDecimal(value.toString().trim()).setScale(2, java.math.RoundingMode.HALF_UP);
        } catch (Exception ex) {
            throw new IllegalArgumentException(message);
        }
    }

    private int toInt(Object value, String message) {
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (Exception ex) {
            throw new IllegalArgumentException(message);
        }
    }

    private static final class ProductTableModel extends AbstractTableModel {
        private final String[] columns = {"ID", "Nom", "Plateforme", "Achat", "Port", "Date", "Statut", "Pièces", "Vente"};
        private List<Product> rows = List.of();

        void setRows(List<Product> rows) {
            this.rows = rows;
            fireTableDataChanged();
        }

        Product getAt(int row) {
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
            Product product = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> product.id();
                case 1 -> product.name();
                case 2 -> product.purchasePlatform();
                case 3 -> product.purchasePrice();
                case 4 -> product.shippingCost();
                case 5 -> product.purchaseDate();
                case 6 -> product.status();
                case 7 -> product.partsCost();
                case 8 -> product.salePrice();
                default -> "";
            };
        }
    }
}
