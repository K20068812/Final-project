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
import guipanels.HelperClass;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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


    @Test
    public void testUndoRemovePrincipal() {
        AssignCategories assignCategories = new AssignCategories(new ArrayList<>(), new ArrayList<>());
        int prevSize = assignCategories.getPrincipals().size();
        UndoClass undoClass = new UndoClass();
        undoClass.addRemovePrincipal(new Principal("John"));
        simulateUndo(assignCategories, undoClass);
        assertEquals(prevSize+1, assignCategories.getPrincipals().size());
    }

    @Test
    public void testUndoCreatePrincipal() {
        AssignCategories assignCategories = new AssignCategories(new ArrayList<>(), new ArrayList<>());
        int prevSize = assignCategories.getPrincipals().size();
        Principal p = new Principal("John");

        UndoClass undoClass = new UndoClass();
        undoClass.addCreatePrincipal(p);
        simulateUndo(assignCategories, undoClass);
        assertEquals(prevSize, assignCategories.getPrincipals().size());
    }

    @Test
    public void testUndoCreateCategory() {
        AssignCategories assignCategories = new AssignCategories(new ArrayList<>(), new ArrayList<>());
        int prevSize = assignCategories.getPrincipals().size();
        PrincipalCategory pc = new PrincipalCategory("Everyone");
        pc.addPrincipal(new Principal("John"));
        assignCategories.addPrincipalCategory(pc);
        UndoClass undoClass = new UndoClass();
        undoClass.addCreateCategory(pc);
        simulateUndo(assignCategories, undoClass);
        assertEquals(prevSize, assignCategories.getPrincipalCategories().size());
    }

    @Test
    public void testUndoRemoveCategory() {
        AssignCategories assignCategories = new AssignCategories(new ArrayList<>(), new ArrayList<>());
        int prevSize = assignCategories.getPrincipals().size();
        UndoClass undoClass = new UndoClass();
        undoClass.addRemovePrincipal(new Principal("John"));
        simulateUndo(assignCategories, undoClass);
        assertEquals(prevSize+1, assignCategories.getPrincipals().size());
    }

    public void simulateUndo(AssignCategories assignCategories, UndoClass undoClass){
            if (!undoClass.getActionTracker().isEmpty()) {
                    List<Object> lastEntry = undoClass.getActionTracker().get(undoClass.getActionTracker().size() - 1);
                    try {
                        UndoClass.UNDO_TYPE actionType = (UndoClass.UNDO_TYPE) lastEntry.get(0);
                        if (actionType == UndoClass.UNDO_TYPE.CREATE_PRINCIPAL) {
                            Principal principal = (Principal) lastEntry.get(1);
                            assignCategories.removePrincipal(HelperClass.getPrincipalByName(assignCategories.getPrincipals(), principal.getName()));
                            assignCategories.evaluatePrincipalCategories();

                        } else if (actionType == UndoClass.UNDO_TYPE.REMOVE_PRINCIPAL) {
                            Principal oldPrincipal = (Principal) lastEntry.get(1);
                            assignCategories.addPrincipal(oldPrincipal);
                            assignCategories.evaluatePrincipalCategories();
                        }
                            else if (actionType == UndoClass.UNDO_TYPE.CREATE_CATEGORY) {
                            PrincipalCategory category = (PrincipalCategory) lastEntry.get(1);
                            PrincipalCategory fixedRef = HelperClass.getCategoryByName(assignCategories.getPrincipalCategories(), category.getName());
                            assignCategories.removePrincipalCategory(fixedRef);
                            for (PrincipalCategory pc : assignCategories.getPrincipalCategories()) {
                                pc.getJuniorCategories().remove(fixedRef);
                            }

                        } else if (actionType == UndoClass.UNDO_TYPE.REMOVE_CATEGORY) {
                            PrincipalCategory oldCategory = (PrincipalCategory) lastEntry.get(1);
                            List<PrincipalCategory> oldSeniorCategories = (List<PrincipalCategory>) lastEntry.get(2);
                            oldCategory.getPrincipals().clear();
                            List<ResourceAction> fixedActions = new ArrayList<>();
                            for (ResourceAction oldAction : oldCategory.getActions()) {
                                ResourceAction actionRef = HelperClass.getActionByName(assignCategories.getResourceActions(), oldAction.getName(), oldAction.getResource().getName());
                                fixedActions.add(actionRef);
                            }
                            oldCategory.setActions(fixedActions);
                            List<Principal> fixedPrincipals = new ArrayList<>();
                            for (Principal p : oldCategory.getPrincipals()) {
                                Principal principalRef = HelperClass.getPrincipalByName(assignCategories.getPrincipals(), p.getName());
                                fixedPrincipals.add(principalRef);
                            }
                            oldCategory.setPrincipals(fixedPrincipals);
                            List<PrincipalCategory> fixedJrCategories = new ArrayList<>();
                            for (PrincipalCategory jr : oldCategory.getJuniorCategories()) {
                                PrincipalCategory jrRef = HelperClass.getCategoryByName(assignCategories.getPrincipalCategories(), jr.getName());
                                fixedJrCategories.add(jrRef);
                            }
                            oldCategory.setJuniorCategories(fixedJrCategories);
                            for (PrincipalCategory oldSenior : oldSeniorCategories) {
                                PrincipalCategory oldRef = HelperClass.getCategoryByName(assignCategories.getPrincipalCategories(), oldSenior.getName());
                                oldRef.addJuniorCategory(oldCategory);
                            }
                            assignCategories.addPrincipalCategory(oldCategory);
                            assignCategories.evaluatePrincipalCategories();

                        }
                    } catch(Exception err){
                        err.printStackTrace();
                    }
                    undoClass.getActionTracker().remove(lastEntry);
                }
    }

}


