package com.stockholm.db;

import com.stockholm.config.AppPaths;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private final Path databaseFile;

    public DatabaseManager() {
        this(AppPaths.resolveDatabaseFile());
    }

    public DatabaseManager(Path databaseFile) {
        this.databaseFile = databaseFile;
    }

    public Path getDatabaseFile() {
        return databaseFile;
    }

    public Connection openConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + databaseFile.toAbsolutePath());
    }

    public void initialize() {
        try {
            Files.createDirectories(databaseFile.getParent());
            try (Connection connection = openConnection(); Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
                statement.execute("""
                        CREATE TABLE IF NOT EXISTS products (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            name TEXT NOT NULL,
                            purchase_platform TEXT NOT NULL DEFAULT 'Vinted',
                            purchase_price NUMERIC NOT NULL DEFAULT 0,
                            shipping_cost NUMERIC NOT NULL DEFAULT 0,
                            purchase_date TEXT NOT NULL DEFAULT (DATE('now')),
                            initial_diagnosis TEXT NOT NULL DEFAULT '',
                            time_spent_minutes INTEGER NOT NULL DEFAULT 0,
                            parts_cost NUMERIC NOT NULL DEFAULT 0,
                            status TEXT NOT NULL DEFAULT 'EN_ATTENTE',
                            sale_channel TEXT NOT NULL DEFAULT 'Vinted',
                            sale_price NUMERIC,
                            sale_fee NUMERIC NOT NULL DEFAULT 0
                        )
                        """);
                statement.execute("""
                        CREATE TABLE IF NOT EXISTS parts (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            product_id INTEGER NOT NULL,
                            name TEXT NOT NULL,
                            follow_up_text TEXT NOT NULL DEFAULT '',
                            cost NUMERIC NOT NULL DEFAULT 0,
                            quantity INTEGER NOT NULL DEFAULT 1,
                            follow_up_date TEXT NOT NULL DEFAULT (DATE('now')),
                            status TEXT NOT NULL DEFAULT 'EN_ATTENTE',
                            FOREIGN KEY(product_id) REFERENCES products(id) ON DELETE CASCADE
                        )
                        """);
                statement.execute("CREATE INDEX IF NOT EXISTS idx_parts_product_id ON parts(product_id)");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to initialize database at " + databaseFile, e);
        }
    }

    public BigDecimal scaleMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
