package guipanels;

import javax.swing.*;
import java.awt.*;

public class ResourceRulePanel {
    private JPanel innerPanel;
    private JTextField[] textFieldArray;
    private JTextField resourceNameField;

    public ResourceRulePanel(){
        innerPanel = new JPanel();
        textFieldArray = new JTextField[10];
        resourceNameField = new JTextField();
        init();
    }

    public void init(){
        innerPanel.setLayout(new GridBagLayout()); // Set the layout for innerPanel to GridBagLayout
        GridBagConstraints innerPanelGBC = new GridBagConstraints(); // Create GridBagConstraints object for innerPanel

        innerPanelGBC.fill = GridBagConstraints.BOTH;
        innerPanelGBC.gridx = 0;
        innerPanelGBC.gridy = 0;
        innerPanelGBC.weightx = 1.0;
        innerPanelGBC.weighty = 0.2; // Set JLabel to take up 40% of the height
        innerPanel.add(new JLabel("Enter the resource name"), innerPanelGBC);

        innerPanelGBC.weighty = 0.15;
        innerPanelGBC.gridy = 1;
        innerPanel.add(resourceNameField, innerPanelGBC);

        innerPanelGBC.weighty = 0.2;
        innerPanelGBC.gridy = 2;
        innerPanel.add(new JLabel("Enter the actions for this resource"), innerPanelGBC);

        JPanel nestedPanel = new JPanel();
        nestedPanel.setLayout(new GridLayout(5, 2));
        innerPanelGBC.gridy = 3;
        innerPanelGBC.weighty = 0.4; // Set nestedPanel to take up 60% of the height
        innerPanel.add(nestedPanel, innerPanelGBC);
        for(int i = 0; i<textFieldArray.length; i++){
            textFieldArray[i] = new JTextField();
            nestedPanel.add(textFieldArray[i]);
        }
    }

    public JTextField[] getTextFieldArray(){
        return textFieldArray;
    }

    public JTextField getResourceNameField(){
        return resourceNameField;
    }

    public JPanel getPanel(){
        return innerPanel;
    }
}
