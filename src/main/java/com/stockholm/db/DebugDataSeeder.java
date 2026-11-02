package com.stockholm.db;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

public final class DebugDataSeeder {
    private final DatabaseManager databaseManager;

    public DebugDataSeeder(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void seedIfEmpty() {
        try (Connection connection = databaseManager.openConnection()) {
            if (!isEmpty(connection)) {
                return;
            }

            connection.setAutoCommit(false);
            try {
                long repairedConsole = insertProduct(connection, "Nintendo Switch Lite", "Leboncoin",
                        "Port USB-C à remplacer, écran fonctionnel.", 189.00, 8.50,
                        LocalDate.now().minusDays(35), 90, 42.50, "REPARE", "Vinted", 279.00, 13.95);
                long pendingLaptop = insertProduct(connection, "Lenovo ThinkPad T480", "eBay",
                        "Démarre mais batterie absente. À tester et nettoyer.", 145.00, 12.00,
                        LocalDate.now().minusDays(12), 45, 0, "EN_ATTENTE", "Leboncoin", null, 0);
                long inProgressPhone = insertProduct(connection, "iPhone 11 64 Go", "Vinted",
                        "Face ID non testé, vitre arrière fissurée.", 230.00, 0,
                        LocalDate.now().minusDays(20), 150, 68.00, "EN_COURS", "Vinted", null, 0);
                long scrapTablet = insertProduct(connection, "Samsung Galaxy Tab A", "Vide-greniers",
                        "Carte mère oxydée, pièce de remplacement introuvable.", 35.00, 5.00,
                        LocalDate.now().minusDays(60), 30, 12.00, "REBUT", "Revente pièces", 25.00, 1.25);
                long profitableConsole = insertProduct(connection, "Sony PSP 3000", "Brocante",
                        "Très bon état général, joystick à nettoyer.", 55.00, 0,
                        LocalDate.now().minusDays(7), 35, 7.50, "REPARE", "Vinted", 139.00, 6.95);

                insertPart(connection, repairedConsole, "Port USB-C", "Installé et testé avec chargeur 30 W.",
                        21.25, 1, LocalDate.now().minusDays(4), "INSTALLEE");
                insertPart(connection, repairedConsole, "Coque arrière", "Remplacée, légère différence de teinte.",
                        21.25, 1, LocalDate.now().minusDays(3), "INSTALLEE");
                insertPart(connection, pendingLaptop, "Batterie compatible T480", "Commande à valider.",
                        48.00, 1, LocalDate.now().plusDays(3), "EN_ATTENTE");
                insertPart(connection, inProgressPhone, "Écran LCD complet", "Réception prévue cette semaine.",
                        62.00, 1, LocalDate.now().plusDays(2), "EN_COURS");
                insertPart(connection, inProgressPhone, "Adhésif vitre arrière", "À poser après le test Face ID.",
                        6.00, 2, LocalDate.now().plusDays(5), "EN_ATTENTE");
                insertPart(connection, scrapTablet, "Connecteur de charge", "Pièce récupérée pour stock.",
                        6.00, 2, LocalDate.now().minusDays(45), "REBUT");
                insertPart(connection, profitableConsole, "Joystick analogique", "Nettoyé et calibré.",
                        7.50, 1, LocalDate.now().minusDays(2), "INSTALLEE");

                long switchBattery = insertBattery(connection, "Switch Lite - batterie test", 1, 3.7, 3.57,
                        "Li-ion", "Batterie de test issue d'un échange.");
                long laptopBattery = insertBattery(connection, "Pack Lenovo 01AV421", 6, 11.4, 2.10,
                        "Li-ion 18650", "Pack à équilibrer avant remontage.");
                long spareBattery = insertBattery(connection, "Pack générique 18650", 4, 7.4, 2.60,
                        "Li-ion 18650", "Stock de cellules pour réparations.");

                insertCells(connection, switchBattery, List.of("SAINE"));
                insertCells(connection, laptopBattery, List.of("SAINE", "SAINE", "FAIBLE", "SAINE", "HS", "INCONNUE"));
                insertCells(connection, spareBattery, List.of("SAINE", "FAIBLE", "SAINE", "SAINE"));

                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to seed debug data", exception);
        }
    }

    private boolean isEmpty(Connection connection) throws SQLException {
        String sql = """
                SELECT
                    (SELECT COUNT(*) FROM products)
                    + (SELECT COUNT(*) FROM parts)
                    + (SELECT COUNT(*) FROM batteries)
                    + (SELECT COUNT(*) FROM battery_cells) AS total
                """;
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            return resultSet.next() && resultSet.getLong("total") == 0;
        }
    }

    private long insertProduct(
            Connection connection,
            String name,
            String platform,
            String diagnosis,
            double purchasePrice,
            double shippingCost,
            LocalDate purchaseDate,
            int timeSpentMinutes,
            double partsCost,
            String status,
            String saleChannel,
            Double salePrice,
            double saleFee
    ) throws SQLException {
        String sql = """
                INSERT INTO products (
                    name, purchase_platform, purchase_price, shipping_cost, purchase_date,
                    initial_diagnosis, time_spent_minutes, parts_cost, status, sale_channel,
                    sale_price, sale_fee
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setString(2, platform);
            statement.setBigDecimal(3, money(purchasePrice));
            statement.setBigDecimal(4, money(shippingCost));
            statement.setString(5, purchaseDate.toString());
            statement.setString(6, diagnosis);
            statement.setInt(7, timeSpentMinutes);
            statement.setBigDecimal(8, money(partsCost));
            statement.setString(9, status);
            statement.setString(10, saleChannel);
            if (salePrice == null) {
                statement.setObject(11, null);
            } else {
                statement.setBigDecimal(11, money(salePrice));
            }
            statement.setBigDecimal(12, money(saleFee));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No generated product id");
                }
                return keys.getLong(1);
            }
        }
    }

    private void insertPart(
            Connection connection,
            long productId,
            String name,
            String followUpText,
            double cost,
            int quantity,
            LocalDate followUpDate,
            String status
    ) throws SQLException {
        String sql = """
                INSERT INTO parts (product_id, name, follow_up_text, cost, quantity, follow_up_date, status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, productId);
            statement.setString(2, name);
            statement.setString(3, followUpText);
            statement.setBigDecimal(4, money(cost));
            statement.setInt(5, quantity);
            statement.setString(6, followUpDate.toString());
            statement.setString(7, status);
            statement.executeUpdate();
        }
    }

    private long insertBattery(
            Connection connection,
            String model,
            int cellCount,
            double voltage,
            double capacityAh,
            String format,
            String notes
    ) throws SQLException {
        String sql = """
                INSERT INTO batteries (model, cell_count, voltage, capacity_ah, format, notes)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, model);
            statement.setInt(2, cellCount);
            statement.setDouble(3, voltage);
            statement.setDouble(4, capacityAh);
            statement.setString(5, format);
            statement.setString(6, notes);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No generated battery id");
                }
                return keys.getLong(1);
            }
        }
    }

    private void insertCells(Connection connection, long batteryId, List<String> states) throws SQLException {
        String sql = "INSERT INTO battery_cells (battery_id, cell_number, state) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < states.size(); index++) {
                statement.setLong(1, batteryId);
                statement.setInt(2, index + 1);
                statement.setString(3, states.get(index));
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private BigDecimal money(double value) {
        return BigDecimal.valueOf(value).setScale(2);
    }
}
