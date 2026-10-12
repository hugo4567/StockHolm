package com.stockholm.repo;

import com.stockholm.db.DatabaseManager;
import com.stockholm.model.Product;
import com.stockholm.model.ProductStatus;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductRepository {
    private final DatabaseManager databaseManager;

    public ProductRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public List<Product> findAll() {
        String sql = "SELECT * FROM products ORDER BY id DESC";
        try (Connection connection = databaseManager.openConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            List<Product> products = new ArrayList<>();
            while (rs.next()) {
                products.add(map(rs));
            }
            return products;
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to load products", e);
        }
    }

    public Optional<Product> findById(long id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection connection = databaseManager.openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to load product " + id, e);
        }
    }

    public long insert(Product product) {
        String sql = """
                INSERT INTO products (
                    name, purchase_platform, purchase_price, shipping_cost, purchase_date,
                    initial_diagnosis, time_spent_minutes, parts_cost, status, sale_channel,
                    sale_price, sale_fee
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = databaseManager.openConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, product);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : -1L;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to insert product", e);
        }
    }

    public void update(Product product) {
        String sql = """
                UPDATE products SET
                    name = ?, purchase_platform = ?, purchase_price = ?, shipping_cost = ?, purchase_date = ?,
                    initial_diagnosis = ?, time_spent_minutes = ?, parts_cost = ?, status = ?, sale_channel = ?,
                    sale_price = ?, sale_fee = ?
                WHERE id = ?
                """;
        try (Connection connection = databaseManager.openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, product);
            ps.setLong(13, product.id());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to update product", e);
        }
    }

    public void delete(long id) {
        try (Connection connection = databaseManager.openConnection();
             PreparedStatement ps = connection.prepareStatement("DELETE FROM products WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to delete product " + id, e);
        }
    }

    public List<StatusCount> countByStatus() {
        String sql = "SELECT status, COUNT(*) AS total FROM products GROUP BY status ORDER BY status";
        try (Connection connection = databaseManager.openConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            List<StatusCount> result = new ArrayList<>();
            while (rs.next()) {
                result.add(new StatusCount(rs.getString("status"), rs.getInt("total")));
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to aggregate product statuses", e);
        }
    }

    public List<FinancialSummary> summarizeFinancials() {
        String sql = """
                SELECT
                    COALESCE(SUM(purchase_price + shipping_cost + parts_cost), 0) AS total_cost,
                    COALESCE(SUM(COALESCE(sale_price, 0) - sale_fee), 0) AS total_revenue
                FROM products
                """;
        try (Connection connection = databaseManager.openConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            if (rs.next()) {
                return List.of(new FinancialSummary(rs.getBigDecimal("total_cost"), rs.getBigDecimal("total_revenue")));
            }
            return List.of(new FinancialSummary(BigDecimal.ZERO, BigDecimal.ZERO));
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to summarize financials", e);
        }
    }

    private Product map(ResultSet rs) throws SQLException {
        return new Product(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("purchase_platform"),
                rs.getBigDecimal("purchase_price"),
                rs.getBigDecimal("shipping_cost"),
                LocalDate.parse(rs.getString("purchase_date")),
                rs.getString("initial_diagnosis"),
                rs.getInt("time_spent_minutes"),
                rs.getBigDecimal("parts_cost"),
                ProductStatus.valueOf(rs.getString("status")),
                rs.getString("sale_channel"),
                rs.getBigDecimal("sale_price"),
                rs.getBigDecimal("sale_fee")
        );
    }

    private void bind(PreparedStatement ps, Product product) throws SQLException {
        ps.setString(1, product.name());
        ps.setString(2, product.purchasePlatform());
        ps.setBigDecimal(3, product.purchasePrice());
        ps.setBigDecimal(4, product.shippingCost());
        ps.setString(5, product.purchaseDate().toString());
        ps.setString(6, product.initialDiagnosis());
        ps.setInt(7, product.timeSpentMinutes());
        ps.setBigDecimal(8, product.partsCost());
        ps.setString(9, product.status().name());
        ps.setString(10, product.saleChannel());
        if (product.salePrice() == null) {
            ps.setObject(11, null);
        } else {
            ps.setBigDecimal(11, product.salePrice());
        }
        ps.setBigDecimal(12, product.saleFee());
    }

    public record StatusCount(String status, int total) {
    }

    public record FinancialSummary(BigDecimal totalCost, BigDecimal totalRevenue) {
    }
}
