package database;

import categories.AssignCategories;
import com.mongodb.client.MongoClients;
import guipanels.GraphDisplay;
import guipanels.HelperClass;

import javax.swing.*;
import java.awt.*;

import java.util.HashMap;
import java.util.Map;

public class LoadDataWorker extends SwingWorker<Map<String, Object>, Void> {
    private final JFrame frame;
    private final JPanel START_PANEL;
    private final CardLayout cl;
    private final JButton tempButton;
    private final JButton otherButton;

    public LoadDataWorker(JFrame frame, JPanel startPanel, CardLayout cl, JButton tempButton, JButton otherButton) {
        this.frame = frame;
        this.START_PANEL = startPanel;
        this.cl = cl;
        this.tempButton = tempButton;
        this.otherButton = otherButton;
    }
    @Override
    protected Map<String, Object> doInBackground() throws Exception {
        tempButton.setEnabled(false);
        MongoMain mongoMain = new MongoMain(MongoClients.create(MongoDB_CONFIG.DATABASE_URL));
        if (mongoMain.databaseEmpty()) {
            JOptionPane.showMessageDialog(frame, "Database empty, create new policy!");
            return null;
        } else {
            AssignCategories currentState = mongoMain.getAssignCategories();
            AssignCategories fixedAfter = HelperClass.fixAssignCategories(currentState);
            UndoClass undoClass = mongoMain.getUndoClass();
            Map<String, Object> result = new HashMap<>();
            result.put("assignCategories", fixedAfter);
            result.put("undoClass", undoClass);
            return result;
        }
    }

    @Override
    protected void done() {
        try {
            Map<String, Object> result = get();
            if (result != null) {
                AssignCategories fixedAfter = (AssignCategories) result.get("assignCategories");
                UndoClass undoClass = (UndoClass) result.get("undoClass");
                // Do something with undoClass if needed
                new GraphDisplay(fixedAfter, undoClass, START_PANEL, frame, cl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            tempButton.setEnabled(true);
            otherButton.setEnabled(true);
        }
    }

}



