package com.stockholm.repo;

import com.stockholm.db.DatabaseManager;
import com.stockholm.model.Part;
import com.stockholm.model.PartStatus;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PartRepository {
    private final DatabaseManager databaseManager;

    public PartRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public List<Part> findByProductId(long productId) {
        String sql = "SELECT * FROM parts WHERE product_id = ? ORDER BY id DESC";
        try (Connection connection = databaseManager.openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Part> parts = new ArrayList<>();
                while (rs.next()) {
                    parts.add(map(rs));
                }
                return parts;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to load parts", e);
        }
    }

    public long insert(Part part) {
        String sql = """
                INSERT INTO parts (product_id, name, follow_up_text, cost, quantity, follow_up_date, status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = databaseManager.openConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, part);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : -1L;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to insert part", e);
        }
    }

    public void update(Part part) {
        String sql = """
                UPDATE parts SET product_id = ?, name = ?, follow_up_text = ?, cost = ?, quantity = ?, follow_up_date = ?, status = ?
                WHERE id = ?
                """;
        try (Connection connection = databaseManager.openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, part);
            ps.setLong(8, part.id());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to update part", e);
        }
    }

    public void delete(long id) {
        try (Connection connection = databaseManager.openConnection();
             PreparedStatement ps = connection.prepareStatement("DELETE FROM parts WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to delete part " + id, e);
        }
    }

    public BigDecimal sumCostByProduct(long productId) {
        String sql = "SELECT COALESCE(SUM(cost * quantity), 0) AS total FROM parts WHERE product_id = ?";
        try (Connection connection = databaseManager.openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal("total") : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to sum part cost", e);
        }
    }

    private Part map(ResultSet rs) throws SQLException {
        return new Part(
                rs.getLong("id"),
                rs.getLong("product_id"),
                rs.getString("name"),
                rs.getString("follow_up_text"),
                rs.getBigDecimal("cost"),
                rs.getInt("quantity"),
                LocalDate.parse(rs.getString("follow_up_date")),
                PartStatus.valueOf(rs.getString("status"))
        );
    }

    private void bind(PreparedStatement ps, Part part) throws SQLException {
        ps.setLong(1, part.productId());
        ps.setString(2, part.name());
        ps.setString(3, part.followUpText());
        ps.setBigDecimal(4, part.cost());
        ps.setInt(5, part.quantity());
        ps.setString(6, part.followUpDate().toString());
        ps.setString(7, part.status().name());
    }
}
