package guipanels;

import categories.AssignCategories;
import database.LoadDataWorker;
import database.LoadFileWorker;
import database.UndoClass;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class StartPanel {

    CardLayout cl;
    JFrame frame;
    JPanel START_PANEL;

    public StartPanel() {
        cl = new CardLayout();
        frame = new JFrame();
        init();
    }

    public void init(){
        START_PANEL = new JPanel();
        START_PANEL.setLayout(cl);

        // Create a panel with two buttons
        JPanel buttonPanel = new JPanel(new BorderLayout());

        // Load File button
        JButton loadFileButton = new JButton("New policy");
        JButton tempButton = new JButton("Load policy");

        loadFileButton.addActionListener(e -> {
            int res = JOptionPane.showConfirmDialog(frame, "This will clear any previous data in the database!");
            if (res == JOptionPane.YES_OPTION) {
                LoadFileWorker loadFileWorker = new LoadFileWorker(frame, START_PANEL, cl, loadFileButton, tempButton, new AssignCategories(new ArrayList<>(), new ArrayList<>()),
                        new UndoClass());
                loadFileWorker.execute();
            }
        });

        buttonPanel.add(loadFileButton, BorderLayout.WEST);


        tempButton.addActionListener(acl -> {
            LoadDataWorker loadDataWorker = new LoadDataWorker(frame, START_PANEL, cl, tempButton, loadFileButton);
            loadDataWorker.execute();
        });

        buttonPanel.add(tempButton, BorderLayout.EAST);

        JLabel label = new JLabel("Policy manager", SwingConstants.CENTER);
        label.setFont(new Font("Calibri", Font.BOLD, 30));
        buttonPanel.add(label, BorderLayout.CENTER);

        START_PANEL.add(buttonPanel, "buttonPanel");

        // Show the first panel
        cl.show(START_PANEL, "buttonPanel");

        frame.add(START_PANEL);
        frame.setPreferredSize(new Dimension(1600, 900));
//        frame.setPreferredSize(new Dimension(1280, 720));
        frame.setResizable(true);
        frame.setVisible(true);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StartPanel::new);
    }
}
