package com.stockholm.db;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseManagerTest {
    @Test
    void initializesDatabaseFileAndSchema() throws Exception {
        Path tempDir = Files.createTempDirectory("stockholm-db-test");
        Path db = tempDir.resolve("stockholm.db");
        DatabaseManager manager = new DatabaseManager(db);
        manager.initialize();

        assertTrue(Files.exists(db));
        assertTrue(Files.size(db) > 0);
    }

    @Test
    void seedsDebugDataOnlyForAnEmptyDatabase() throws Exception {
        DatabaseManager manager = new DatabaseManager(createDatabasePath());
        manager.initialize();

        DebugDataSeeder seeder = new DebugDataSeeder(manager);
        seeder.seedIfEmpty();

        assertEquals(5, count(manager, "products"));
        assertEquals(7, count(manager, "parts"));
        assertEquals(3, count(manager, "batteries"));
        assertEquals(11, count(manager, "battery_cells"));
        assertEquals(5, count(manager, "products", "status IN ('EN_ATTENTE', 'EN_COURS', 'REPARE', 'REBUT')"));

        seeder.seedIfEmpty();
        assertEquals(5, count(manager, "products"));
        assertEquals(7, count(manager, "parts"));
    }

    @Test
    void doesNotSeedWhenDatabaseAlreadyContainsData() throws Exception {
        DatabaseManager manager = new DatabaseManager(createDatabasePath());
        manager.initialize();
        try (var connection = manager.openConnection();
             var statement = connection.prepareStatement("""
                     INSERT INTO products (name) VALUES (?)
                     """)) {
            statement.setString(1, "Produit existant");
            statement.executeUpdate();
        }

        new DebugDataSeeder(manager).seedIfEmpty();

        assertEquals(1, count(manager, "products"));
        assertEquals(0, count(manager, "batteries"));
    }

    private Path createDatabasePath() throws Exception {
        return Files.createTempDirectory("stockholm-db-test").resolve("stockholm.db");
    }

    private long count(DatabaseManager manager, String table) throws Exception {
        return count(manager, table, "1 = 1");
    }

    private long count(DatabaseManager manager, String table, String condition) throws Exception {
        try (var connection = manager.openConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM " + table + " WHERE " + condition)) {
            resultSet.next();
            return resultSet.getLong(1);
        }
    }
}
