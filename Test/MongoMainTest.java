import categories.*;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import database.MongoMain;
import database.UndoClass;
import de.flapdoodle.embed.mongo.*;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MongoMainTest {
    MongoMain mongoMain;
    MongodExecutable mongodExecutable;
    MongodProcess mongodProcess;

    @BeforeEach
    public void setUp() throws IOException {
        String ip = "localhost";
        int port = getFreePort();

        MongodConfig config = MongodConfig.builder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(ip, port, de.flapdoodle.embed.process.runtime.Network.localhostIsIPv6()))
                .build();

        MongodStarter starter = MongodStarter.getDefaultInstance();
        mongodExecutable = starter.prepare(config);
//        mongodExecutable.start();
        //mongodExecutable.start();

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString("mongodb://" + ip + ":" + port))
                .build();

        MongoClient mongoClient = MongoClients.create(settings);
        mongoMain = new MongoMain(mongoClient);
        mongodProcess = mongodExecutable.start();

        mongodExecutable.start();

    }

    @AfterEach
    public void tearDown() {
        if (mongodExecutable != null) {
            mongodProcess.stop();
            mongodExecutable.stop();
        }
    }

    private PrincipalCategory createSamplePrincipalCategory(String name) {
        return new PrincipalCategory(name);
    }

    private ResourceAction createAction(String actionName, String resourceName) {
        Resource resource = new Resource(resourceName);
        return new ResourceAction(actionName, resource);
    }

    private Principal createSamplePrincipal(String name, List<PrincipalCategory> categories) {
        Principal principal = new Principal(name);
        return principal;
    }

    @Test
    public void testGetUndoClass() {
        // Create sample data
        PrincipalCategory pc1 = createSamplePrincipalCategory("Category1");
        PrincipalCategory pc2 = createSamplePrincipalCategory("Category2");
        Principal principal = createSamplePrincipal("TestPrincipal", Arrays.asList(pc1, pc2));
        ResourceAction action = createAction("TestAction", "TestResource");

        // Create an UndoClass object and add some actions
        UndoClass undoClass = new UndoClass();
        undoClass.addCreatePrincipal(principal);
        undoClass.addAddAction(action);

        // Save the UndoClass object
        mongoMain.saveUndoClass(undoClass);

        // Retrieve the UndoClass object
        UndoClass retrievedUndoClass = mongoMain.getUndoClass();

        // Test if the retrieved object is not null and has the correct number of actions
        assertNotNull(retrievedUndoClass);
        assertEquals(undoClass.getActionTracker().size(), retrievedUndoClass.getActionTracker().size());
    }

    @Test
    public void testGetAssignCategories() {
        // Create sample data
        PrincipalCategory pc1 = createSamplePrincipalCategory("Category1");
        PrincipalCategory pc2 = createSamplePrincipalCategory("Category2");
        Principal principal = createSamplePrincipal("TestPrincipal", Arrays.asList(pc1, pc2));

        // Create an AssignCategories object and add some data
        AssignCategories assignCategories = new AssignCategories(new ArrayList<>(), new ArrayList<>());
        assignCategories.addPrincipal(principal);
        assignCategories.addResource(new Resource("TestResource"));
        mongoMain.saveAssignCategories(assignCategories, new UndoClass());

        // Retrieve the AssignCategories object
        AssignCategories retrievedAssignCategories = mongoMain.getAssignCategories();

        // Test if the retrieved object is not null and has the correct number of items
        assertNotNull(retrievedAssignCategories);
        assertEquals(assignCategories.getPrincipals().size(), retrievedAssignCategories.getPrincipals().size());
        assertEquals(assignCategories.getResourceActions().size(), retrievedAssignCategories.getResourceActions().size());
        assertEquals(assignCategories.getPrincipalCategories().size(), retrievedAssignCategories.getPrincipalCategories().size());
    }

    private static int getFreePort() {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException("Failed to find a free port", e);
        }
    }
}
