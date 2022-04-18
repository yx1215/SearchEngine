package edu.upenn.cis.cis455;

import edu.upenn.cis.cis455.storage.documentStorage.Storage;
import edu.upenn.cis.cis455.storage.StorageFactory;
import junit.framework.TestCase;
import org.apache.logging.log4j.Level;
import org.junit.Before;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestStorage extends TestCase {
    private final String host = "http://localhost:45555";
    private final String startUrl = "https://crawltest.cis.upenn.edu/";

    @Before
    public void setUp() {
        org.apache.logging.log4j.core.config.Configurator.setLevel("edu.upenn.cis.cis455", Level.DEBUG);
    }

    public void testAddUser() throws IOException {
        Path tmp_path = Files.createTempDirectory("tmpDatabase");
        tmp_path.toFile().deleteOnExit();
        Storage database = (Storage) StorageFactory.getDocumentDatabase(tmp_path.toString());
        String username = "Xiao";
        String password = "123";

        database.addUser(username, password);
        assertTrue(database.getSessionForUser(username, password));
        database.close();
    }

    public void testAddDocument() throws IOException {
        Path tmp_path = Files.createTempDirectory("tmpDatabase");
        tmp_path.toFile().deleteOnExit();
        Storage database = (Storage) StorageFactory.getDocumentDatabase(tmp_path.toString());

        String url = "https://www.test.com";
        String content = "empty file";
        String contentType = "text/html";
        database.addDocument(url, content, contentType);
        assertEquals(database.getCorpusSize(), 1);
        database.close();
    }

}
