package com.stockholm.repo;

import com.stockholm.db.DatabaseManager;
import com.stockholm.model.Battery;
import com.stockholm.model.BatteryCell;
import com.stockholm.model.BatteryCellState;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BatteryRepository {
    private final DatabaseManager databaseManager;

    public BatteryRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public List<Battery> findAll() {
        try (Connection connection = databaseManager.openConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT * FROM batteries ORDER BY id DESC")) {
            List<Battery> result = new ArrayList<>();
            while (rs.next()) {
                result.add(mapBattery(rs));
            }
            return result;
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de charger les batteries", exception);
        }
    }

    public long insert(Battery battery) {
        String sql = "INSERT INTO batteries (model, cell_count, voltage, capacity_ah, format, notes) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = databaseManager.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindBattery(statement, battery);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : -1L;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible d'enregistrer la batterie", exception);
        }
    }

    public void update(Battery battery) {
        String sql = "UPDATE batteries SET model = ?, cell_count = ?, voltage = ?, capacity_ah = ?, format = ?, notes = ? WHERE id = ?";
        try (Connection connection = databaseManager.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindBattery(statement, battery);
            statement.setLong(7, battery.id());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de modifier la batterie", exception);
        }
    }

    public void delete(long id) {
        try (Connection connection = databaseManager.openConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM batteries WHERE id = ?")) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de supprimer la batterie", exception);
        }
    }

    public List<BatteryCell> findCells(long batteryId) {
        String sql = "SELECT * FROM battery_cells WHERE battery_id = ? ORDER BY cell_number";
        try (Connection connection = databaseManager.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, batteryId);
            try (ResultSet rs = statement.executeQuery()) {
                List<BatteryCell> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(new BatteryCell(
                            rs.getLong("id"),
                            rs.getLong("battery_id"),
                            rs.getInt("cell_number"),
                            BatteryCellState.valueOf(rs.getString("state"))
                    ));
                }
                return result;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de charger les cellules", exception);
        }
    }

    public void replaceCells(long batteryId, List<BatteryCellState> states) {
        String deleteSql = "DELETE FROM battery_cells WHERE battery_id = ?";
        String insertSql = "INSERT INTO battery_cells (battery_id, cell_number, state) VALUES (?, ?, ?)";
        try (Connection connection = databaseManager.openConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement delete = connection.prepareStatement(deleteSql);
                 PreparedStatement insert = connection.prepareStatement(insertSql)) {
                delete.setLong(1, batteryId);
                delete.executeUpdate();
                for (int index = 0; index < states.size(); index++) {
                    insert.setLong(1, batteryId);
                    insert.setInt(2, index + 1);
                    insert.setString(3, states.get(index).name());
                    insert.addBatch();
                }
                insert.executeBatch();
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible d'enregistrer l'état des cellules", exception);
        }
    }

    public BatteryMetrics metrics() {
        String sql = """
                SELECT
                    COALESCE(SUM(bc.state = 'SAINE'), 0) AS healthy_cells,
                    COALESCE(COUNT(bc.id), 0) AS total_cells,
                    COALESCE(COUNT(DISTINCT b.id), 0) AS battery_count
                FROM batteries b
                LEFT JOIN battery_cells bc ON bc.battery_id = b.id
                """;
        try (Connection connection = databaseManager.openConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            return rs.next()
                    ? new BatteryMetrics(rs.getInt("healthy_cells"), rs.getInt("total_cells"), rs.getInt("battery_count"))
                    : new BatteryMetrics(0, 0, 0);
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de calculer les indicateurs batteries", exception);
        }
    }

    private Battery mapBattery(ResultSet rs) throws SQLException {
        return new Battery(
                rs.getLong("id"),
                rs.getString("model"),
                rs.getInt("cell_count"),
                rs.getDouble("voltage"),
                rs.getDouble("capacity_ah"),
                rs.getString("format"),
                rs.getString("notes")
        );
    }

    private void bindBattery(PreparedStatement statement, Battery battery) throws SQLException {
        statement.setString(1, battery.model());
        statement.setInt(2, battery.cellCount());
        statement.setDouble(3, battery.voltage());
        statement.setDouble(4, battery.capacityAh());
        statement.setString(5, battery.format());
        statement.setString(6, battery.notes());
    }

    public record BatteryMetrics(int healthyCells, int totalCells, int batteryCount) {
    }
}
