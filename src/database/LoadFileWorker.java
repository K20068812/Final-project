package database;

import categories.AssignCategories;
import com.mongodb.client.MongoClients;
import guipanels.GraphDisplay;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class LoadFileWorker extends SwingWorker<Void, Void> {
    private final JFrame frame;
    private final JPanel START_PANEL;
    private final CardLayout cl;
    private final JButton loadFileButton;
    private final JButton otherButton;
    private final AssignCategories assignCategories;
    private final UndoClass undoClass;

    public LoadFileWorker(JFrame frame, JPanel startPanel, CardLayout cl, JButton loadFileButton, JButton otherButton,
                          AssignCategories assignCategories, UndoClass undoClass) {
        this.frame = frame;
        this.START_PANEL = startPanel;
        this.cl = cl;
        this.loadFileButton = loadFileButton;
        this.otherButton = otherButton;
        this.undoClass = undoClass;
        this.assignCategories = assignCategories;
    }

    @Override
    protected Void doInBackground() throws Exception {
        loadFileButton.setEnabled(false);
        otherButton.setEnabled(false);
        MongoMain mongoMain = new MongoMain(MongoClients.create(MongoDB_CONFIG.DATABASE_URL));
            mongoMain.saveAssignCategories(assignCategories, undoClass);
        return null;
    }

    @Override
    protected void done() {
        try {
            new GraphDisplay(assignCategories, undoClass, START_PANEL, frame, cl);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            loadFileButton.setEnabled(true);
            otherButton.setEnabled(true);
        }
    }
}

