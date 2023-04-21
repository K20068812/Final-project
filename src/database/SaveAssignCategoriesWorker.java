package database;

import categories.AssignCategories;
import com.mongodb.client.MongoClients;

import javax.swing.*;
import java.awt.*;

public class SaveAssignCategoriesWorker extends SwingWorker<Void, Void> {
    private final JFrame frame;
    private final AssignCategories assignCategories;
    private final JButton saveButton;
    private final UndoClass undoClass;

    public SaveAssignCategoriesWorker(JFrame frame, AssignCategories assignCategories, UndoClass undoClass, JButton saveButton) {
        this.frame = frame;
        this.assignCategories = assignCategories;
        this.saveButton = saveButton;
        this.undoClass = undoClass;
    }

    @Override
    protected Void doInBackground() throws Exception {
        saveButton.setEnabled(false);
        MongoMain mongoMain = new MongoMain(MongoClients.create(MongoDB_CONFIG.DATABASE_URL));
        mongoMain.saveAssignCategories(assignCategories, undoClass);
        return null;
    }

    @Override
    protected void done() {
        try {
            JOptionPane.showMessageDialog(frame, "Saved");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            saveButton.setEnabled(true);
        }
    }
}
