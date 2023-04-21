package guipanels;

import java.awt.*;
import javax.swing.*;

public class RegistrationForm {

    private JPanel panel;
    private int NUM_OF_ROWS = 6;
    private int NUM_OF_COLUMNS = 2;
    private int[] columnWidths;
    private JPanel[][] panelArray;

    private JPanel[] leftPanels;
    private JPanel[][] rightPanels;


    public RegistrationForm() {
        columnWidths = new int[NUM_OF_COLUMNS];
        panelArray = new JPanel[NUM_OF_ROWS][NUM_OF_COLUMNS];
        panel = new JPanel(new GridBagLayout());
        leftPanels = new JPanel[NUM_OF_ROWS];
        rightPanels = new JPanel[NUM_OF_ROWS][3];
        init();
    }

    public void init(){
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;

        columnWidths[0] = 1;
        columnWidths[1] = 2;
        for (int row = 0; row < NUM_OF_ROWS; row++) {
            gbc.gridy = row;
            gbc.weighty = 1.0;
            for (int col = 0; col < NUM_OF_COLUMNS; col++) {
                JPanel currentPanel = new JPanel();
                panelArray[row][col] = currentPanel;
                gbc.gridx = col;
                gbc.weightx = columnWidths[col];
                panel.add(currentPanel, gbc);
            }
        }


        for(int j = 0; j< NUM_OF_ROWS; j++){
            String[] attributeTypes = {"String attribute", "Integer attribute", "Date attribute"}; // KEEP THIS HERE OTHERWISE GETSELECTEDINDEX WONT WORK!
            JPanel currLeftPanel = panelArray[j][0];
            JPanel currRightPanel = panelArray[j][1];
//            currLeftPanel.setBorder(BorderFactory.createLineBorder(Color.ORANGE));
            JComboBox<String> attributeTypeComboBox = new JComboBox<>(attributeTypes);
            currLeftPanel.add(attributeTypeComboBox);
            JTextField currField = new JTextField(10);

            CardLayout cl = new CardLayout();
            currRightPanel.setLayout(cl);

            JPanel stringPanel = new JPanel();
            stringPanel.setLayout(new BorderLayout());
            stringPanel.setBackground(Color.WHITE);
//            stringPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            JTextField tempField = new JTextField();
            stringPanel.add(tempField, BorderLayout.CENTER);
            currRightPanel.add(stringPanel, attributeTypes[0]);
            rightPanels[j][0] = stringPanel;

            JPanel numberPanel = new JPanel();
            numberPanel.setBackground(Color.WHITE);
            numberPanel.setLayout(new BorderLayout());
//            numberPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            NumericTextField tempNumericField = new NumericTextField();
            numberPanel.add(tempNumericField, BorderLayout.CENTER);
            currRightPanel.add(numberPanel, attributeTypes[1]);
            rightPanels[j][1] = numberPanel;

            JPanel datePanel = new JPanel();
            datePanel.setBackground(Color.WHITE);
//            datePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            currRightPanel.add(datePanel, attributeTypes[2]);
            JTextField dateField = new JTextField(30);
            dateField.setEditable(false);
            datePanel.add(dateField);
            JButton selectDateButton = new JButton("Select date");
            datePanel.add(selectDateButton);
            rightPanels[j][2] = datePanel;

            selectDateButton.addActionListener(e -> {
                if(!HelperClass.isFrameOpen) {
                    HelperClass.showCalendar(dateField);
                }
            });

            String selectedItem = String.valueOf(attributeTypeComboBox.getSelectedItem());
            if(selectedItem != null){
                cl.show(currRightPanel, selectedItem);
            }

            attributeTypeComboBox.addActionListener(e -> {
                // Get the selected item from the JComboBox and print it
                String selectedItem1 = String.valueOf(attributeTypeComboBox.getSelectedItem());
                if (selectedItem1 != null) {
                    cl.show(currRightPanel, selectedItem1);
                }
            });
            JCheckBox enabledCheckbox = new JCheckBox();
            if(j == 0){
                currField.setText("Name");
                currField.setEditable(false);
                currField.setEnabled(false);
                enabledCheckbox.setSelected(true);
                enabledCheckbox.setEnabled(false);
                attributeTypeComboBox.setSelectedIndex(0);
                attributeTypeComboBox.setEnabled(false);
            } else {
                currField.setText("Custom " + j);
                enabledCheckbox.setSelected(false);
            }
            currLeftPanel.add(currField);
            currLeftPanel.add(enabledCheckbox);

            leftPanels[j] = currLeftPanel;
            }

        }

    public int getNUM_OF_ROWS(){
        return NUM_OF_ROWS;
    }
    public int getNUM_OF_COLUMNS(){
        return NUM_OF_COLUMNS;
    }

    public JPanel[] getLeftPanels() {
        return leftPanels;
    }

    public JPanel[][] getRightPanels() {
        return rightPanels;
    }

    public JPanel[][] getPanelArray(){
        return panelArray;
    }

    public JPanel getPanel() {
        return panel;
    }

}