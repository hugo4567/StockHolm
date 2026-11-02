package com.stockholm.ui;

import com.stockholm.db.DatabaseManager;
import com.stockholm.model.Battery;
import com.stockholm.model.Product;
import com.stockholm.model.ProductStatus;
import com.stockholm.repo.BatteryRepository;
import com.stockholm.repo.PartRepository;
import com.stockholm.repo.ProductRepository;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class FxMainView extends BorderPane {
    private final ProductRepository productRepository;
    private final BatteryRepository batteryRepository;
    private final PartRepository partRepository;
    private final StackPane pageContainer = new StackPane();
    private final VBox sidebar = new VBox(10);
    private final Label pageTitle = new Label();
    private final Label pageSubtitle = new Label();

    public FxMainView(DatabaseManager databaseManager) {
        productRepository = new ProductRepository(databaseManager);
        batteryRepository = new BatteryRepository(databaseManager);
        partRepository = new PartRepository(databaseManager);
        getStyleClass().add("root");

        setLeft(buildSidebar());
        setCenter(pageContainer);
        showDashboard();
    }

    private VBox buildSidebar() {
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(218);

        Label brand = new Label("StockHolm");
        brand.getStyleClass().add("brand");
        Label subtitle = new Label("REPAIR & RESALE STUDIO");
        subtitle.getStyleClass().add("brand-subtitle");
        sidebar.getChildren().addAll(brand, subtitle, spacer(22));

        addNav("⌂", "Vue générale", this::showDashboard);
        addNav("▣", "Produits", this::showProducts);
        addNav("◈", "Stock batteries", this::showBatteries);
        addNav("↗", "Rentabilité", this::showProfitability);
        addNav("⚙", "Suivi & pièces", this::showParts);

        Region bottomSpacer = new Region();
        VBox.setVgrow(bottomSpacer, Priority.ALWAYS);
        sidebar.getChildren().add(bottomSpacer);

        Label storage = new Label("STOCKAGE LOCAL\nSQLite sécurisé");
        storage.getStyleClass().add("brand-subtitle");
        storage.setPadding(new Insets(0, 0, 8, 8));
        sidebar.getChildren().add(storage);
        return sidebar;
    }

    private void addNav(String icon, String text, Runnable action) {
        Button button = new Button(icon + "   " + text);
        button.getStyleClass().add("nav-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(event -> {
            sidebar.getChildren().stream()
                    .filter(node -> node instanceof Button)
                    .forEach(node -> node.getStyleClass().remove("active"));
            button.getStyleClass().add("active");
            action.run();
        });
        sidebar.getChildren().add(button);
    }

    private void showDashboard() {
        setPage("Vue générale", "La synthèse de votre activité, en un coup d’œil", buildDashboard());
    }

    private void showProducts() {
        setPage("Produits", "Gérez vos achats, réparations et reventes", buildProducts());
    }

    private void showBatteries() {
        setPage("Stock batteries", "Suivez chaque cellule et la capacité disponible", buildBatteries());
    }

    private void showProfitability() {
        setPage("Rentabilité", "Comprenez vos gains et vos coûts réels", buildProfitability());
    }

    private void showParts() {
        setPage("Suivi & pièces", "Gardez une trace des opérations sur chaque produit", buildParts());
    }

    private void setPage(String title, String subtitle, Node content) {
        pageTitle.setText(title);
        pageSubtitle.setText(subtitle);
        VBox page = new VBox(24, buildPageHeader(), content);
        page.getStyleClass().add("content");
        VBox.setVgrow(content, Priority.ALWAYS);
        pageContainer.getChildren().setAll(page);
    }

    private VBox buildPageHeader() {
        pageTitle.getStyleClass().setAll("page-title");
        pageSubtitle.getStyleClass().setAll("page-subtitle");
        return new VBox(4, pageTitle, pageSubtitle);
    }

    private Node buildDashboard() {
        ProductRepository.GlobalKpis kpis = productRepository.globalKpis();
        BatteryRepository.BatteryMetrics batteries = batteryRepository.metrics();

        HBox metrics = new HBox(14,
                metric("Produits suivis", String.valueOf(kpis.totalProducts()), "tous statuts"),
                metric("Gains nets", money(kpis.netGain()), "après tous les coûts"),
                metric("ROI global", kpis.roiPercent() + " %", "performance"),
                metric("Cellules saines", batteries.healthyCells() + " / " + batteries.totalCells(), "stock batteries")
        );
        for (Node node : metrics.getChildren()) {
            HBox.setHgrow(node, Priority.ALWAYS);
        }

        PieChart statusChart = new PieChart();
        statusChart.setTitle("Répartition des produits");
        productRepository.countByStatus().forEach(item ->
                statusChart.getData().add(new PieChart.Data(statusLabel(item.status()), item.total())));
        statusChart.setLegendVisible(true);
        statusChart.setLabelsVisible(false);
        statusChart.setMinHeight(330);

        BarChart<String, Number> financeChart = buildFinanceChart();
        financeChart.setTitle("Vue financière");
        financeChart.setMinHeight(330);

        HBox charts = new HBox(16, chartCard(statusChart), chartCard(financeChart));
        HBox.setHgrow(charts.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(charts.getChildren().get(1), Priority.ALWAYS);
        return new VBox(18, metrics, charts);
    }

    private Node buildProducts() {
        TableView<Product> table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(productRepository.findAll()));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().addAll(
                textColumn("Nom", Product::name),
                textColumn("Plateforme", Product::purchasePlatform),
                moneyColumn("Achat", Product::purchasePrice),
                textColumn("Statut", product -> statusLabel(product.status().name())),
                moneyColumn("Vente", Product::salePrice)
        );
        VBox.setVgrow(table, Priority.ALWAYS);

        TextField name = new TextField();
        name.setPromptText("Nom du produit");
        TextField price = new TextField("0");
        price.setPromptText("Prix d’achat");
        ComboBox<ProductStatus> status = new ComboBox<>(FXCollections.observableArrayList(ProductStatus.values()));
        status.setValue(ProductStatus.EN_ATTENTE);
        Button add = new Button("Ajouter le produit");
        add.getStyleClass().add("primary-button");
        add.setOnAction(event -> {
            if (name.getText().isBlank()) {
                return;
            }
            Product product = new Product(null, name.getText().trim(), "Vinted",
                    decimal(price.getText()), BigDecimal.ZERO, LocalDate.now(), "",
                    0, BigDecimal.ZERO, status.getValue(), "Vinted", null, BigDecimal.ZERO);
            productRepository.insert(product);
            table.setItems(FXCollections.observableArrayList(productRepository.findAll()));
            name.clear();
            price.setText("0");
        });

        HBox quickAdd = new HBox(10, field("Produit", name), field("Prix", price), status, add);
        quickAdd.getStyleClass().add("card");
        quickAdd.setAlignment(Pos.CENTER_LEFT);
        return new VBox(14, quickAdd, table);
    }

    private Node buildBatteries() {
        TableView<Battery> table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(batteryRepository.findAll()));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().addAll(
                textColumn("Modèle", Battery::model),
                numberColumn("Cellules", Battery::cellCount),
                numberColumn("Tension", Battery::voltage),
                numberColumn("Capacité Ah", Battery::capacityAh),
                textColumn("Format", Battery::format)
        );
        VBox.setVgrow(table, Priority.ALWAYS);

        TextField model = new TextField();
        model.setPromptText("Modèle");
        TextField cells = new TextField("1");
        cells.setPromptText("Cellules");
        TextField voltage = new TextField("0");
        voltage.setPromptText("Volts");
        TextField capacity = new TextField("0");
        capacity.setPromptText("Ah");
        TextField format = new TextField();
        format.setPromptText("Format");
        Button add = new Button("Ajouter la batterie");
        add.getStyleClass().add("primary-button");
        add.setOnAction(event -> {
            if (model.getText().isBlank()) {
                return;
            }
            Battery battery = new Battery(null, model.getText().trim(), integer(cells.getText()),
                    number(voltage.getText()), number(capacity.getText()), format.getText().trim(), "");
            long id = batteryRepository.insert(battery);
            batteryRepository.replaceCells(id, java.util.stream.Stream.generate(() -> com.stockholm.model.BatteryCellState.INCONNUE)
                    .limit(battery.cellCount()).toList());
            table.setItems(FXCollections.observableArrayList(batteryRepository.findAll()));
            model.clear();
        });
        HBox quickAdd = new HBox(10, model, cells, voltage, capacity, format, add);
        quickAdd.getStyleClass().add("card");
        quickAdd.setAlignment(Pos.CENTER_LEFT);
        return new VBox(14, quickAdd, table);
    }

    private Node buildProfitability() {
        ListView<ProductRepository.Profitability> list = new ListView<>(
                FXCollections.observableArrayList(productRepository.profitability()));
        list.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(ProductRepository.Profitability item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("#" + item.id() + "  " + item.name() + "     " + money(item.netGain()));
                    setTextFill(item.netGain().signum() >= 0 ? Color.web("#b98cff") : Color.web("#ff778b"));
                }
            }
        });
        VBox.setVgrow(list, Priority.ALWAYS);
        return new VBox(14, cardTitle("Objets vendus et gains nets"), list);
    }

    private Node buildParts() {
        ProductRepository.GlobalKpis kpis = productRepository.globalKpis();
        return new VBox(16,
                cardTitle("Suivi des opérations"),
                metric("Coûts investis", money(kpis.invested()), "achats, port et pièces"),
                metric("Gains purs", money(kpis.pureGain()), "ventes moins frais de vente"),
                cardTitle("Le suivi détaillé des pièces reste lié à chaque produit dans SQLite.")
        );
    }

    private BarChart<String, Number> buildFinanceChart() {
        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(x, y);
        ProductRepository.FinancialSummary summary = productRepository.summarizeFinancials().get(0);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Montant");
        series.getData().add(new XYChart.Data<>("Coûts", summary.totalCost()));
        series.getData().add(new XYChart.Data<>("Ventes", summary.totalRevenue()));
        chart.getData().add(series);
        chart.setLegendVisible(false);
        return chart;
    }

    private VBox metric(String label, String value, String hint) {
        Label top = new Label(label);
        top.getStyleClass().add("metric-label");
        Label amount = new Label(value);
        amount.getStyleClass().addAll("metric-value", "accent");
        Label bottom = new Label(hint);
        bottom.getStyleClass().add("metric-label");
        VBox card = new VBox(8, top, amount, bottom);
        card.getStyleClass().add("card");
        return card;
    }

    private VBox chartCard(Node chart) {
        VBox card = new VBox(chart);
        card.getStyleClass().add("card");
        VBox.setVgrow(chart, Priority.ALWAYS);
        return card;
    }

    private Label cardTitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("card");
        return label;
    }

    private HBox field(String label, TextField input) {
        VBox box = new VBox(4, styledLabel(label), input);
        HBox.setHgrow(box, Priority.ALWAYS);
        return new HBox(box);
    }

    private Label styledLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("form-label");
        return label;
    }

    private <T> TableColumn<T, String> textColumn(String title, java.util.function.Function<T, String> mapper) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(mapper.apply(cell.getValue())));
        return column;
    }

    private <T> TableColumn<T, Number> numberColumn(String title, java.util.function.Function<T, Number> mapper) {
        TableColumn<T, Number> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(mapper.apply(cell.getValue())));
        return column;
    }

    private <T> TableColumn<T, String> moneyColumn(String title, java.util.function.Function<T, BigDecimal> mapper) {
        return textColumn(title, item -> mapper.apply(item) == null ? "—" : money(mapper.apply(item)));
    }

    private Region spacer(double height) {
        Region region = new Region();
        region.setMinHeight(height);
        return region;
    }

    private String money(BigDecimal value) {
        return value.setScale(2, java.math.RoundingMode.HALF_UP) + " €";
    }

    private BigDecimal decimal(String value) {
        try {
            return new BigDecimal(value.replace(',', '.'));
        } catch (NumberFormatException exception) {
            return BigDecimal.ZERO;
        }
    }

    private double number(String value) {
        try {
            return Double.parseDouble(value.replace(',', '.'));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private int integer(String value) {
        try {
            return Math.max(1, Integer.parseInt(value));
        } catch (NumberFormatException exception) {
            return 1;
        }
    }

    private String statusLabel(String status) {
        return switch (status) {
            case "EN_ATTENTE" -> "En attente";
            case "EN_COURS" -> "En cours";
            case "REPARE" -> "Réparé";
            case "REBUT" -> "Rebut";
            default -> status;
        };
    }
}
