package guipanels;

import categories.*;
import categories.ResourceAction;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;


public class PrincipalRulePanel {
    private JPanel innerPanel;
    private Map<ResourceAction, JCheckBox> actionJCheckBoxMap;
    private AssignCategories assignCategories;
    public PrincipalRulePanel(AssignCategories assignCategories){
        innerPanel = new JPanel();
        actionJCheckBoxMap = new HashMap<>();
        this.assignCategories = assignCategories;
        init();
    }

    public void init(){
        innerPanel.setLayout(new GridLayout(6, 6));
        for(ResourceAction a: assignCategories.getResourceActions()){
            JPanel currGridCell = new JPanel(new BorderLayout());
            innerPanel.add(currGridCell);
            String temp1 = a.getResource().getName();
            String temp2 = a.getName();
            currGridCell.add(new JLabel("<html>Resource: " + temp1 + "<br>Action: " + temp2 + "</html>"), BorderLayout.CENTER);
            JCheckBox isSelectedCheckBox = new JCheckBox();
            actionJCheckBoxMap.put(a, isSelectedCheckBox);
            currGridCell.add(isSelectedCheckBox, BorderLayout.NORTH);
            currGridCell.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        }
    }
    public Map<ResourceAction, JCheckBox> getActionJCheckBoxMap(){
        return actionJCheckBoxMap;
    }

    public JPanel getPanel(){
        return innerPanel;
    }

}
