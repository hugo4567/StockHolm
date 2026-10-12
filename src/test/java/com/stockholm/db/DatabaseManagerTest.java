package com.stockholm.db;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

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
}
