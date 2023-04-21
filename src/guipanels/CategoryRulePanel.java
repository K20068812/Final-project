package guipanels;

import principal_resource_attributes.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class CategoryRulePanel {
    List<StringAttribute> ALL_STRING_ATTRIBUTES;
    List<IntegerAttribute> ALL_INTEGER_ATTRIBUTES;
    List<DateAttribute> ALL_DATE_ATTRIBUTES;
    JPanel[] panelArray; // to store the rows of grid panel
    private JPanel panel;
    private JTextField categoryNameField;

    private Map<StringAttribute, JTextField> stringAttributeFieldMap;

    private Map<Integer, JCheckBox> isEnabledMap;

    private Map<IntegerAttribute, JPanel> integerRangeValueMap;

    private Map<DateAttribute, JPanel> dateBetweenValueMap;

    int combined_row_size;

    public CategoryRulePanel(List<StringAttribute> ALL_STRING_ATTRIBUTES, List<IntegerAttribute> ALL_INTEGER_ATTRIBUTES,
                             List<DateAttribute> ALL_DATE_ATTRIBUTES){
        this.ALL_STRING_ATTRIBUTES = ALL_STRING_ATTRIBUTES;
        this.ALL_DATE_ATTRIBUTES = ALL_DATE_ATTRIBUTES;
        this.ALL_INTEGER_ATTRIBUTES = ALL_INTEGER_ATTRIBUTES;
        combined_row_size = ALL_STRING_ATTRIBUTES.size() + ALL_DATE_ATTRIBUTES.size() + ALL_INTEGER_ATTRIBUTES.size();
        panelArray = new JPanel[combined_row_size];
        stringAttributeFieldMap = new HashMap<>();
        integerRangeValueMap = new HashMap<>();
        isEnabledMap = new HashMap<>();
        dateBetweenValueMap = new HashMap<>();
        panel = new JPanel();
        categoryNameField = new JTextField();
        init();
    }

    public Map<DateAttribute, JPanel> getDateBetweenValueMap() {
        return dateBetweenValueMap;
    }

    public Map<Integer, JCheckBox> getIsEnabledMap(){
        return isEnabledMap;
    }

    public Map<StringAttribute, JTextField> getStringAttributeJTextFieldMap(){
        return stringAttributeFieldMap;
    }

    public Map<IntegerAttribute, JPanel> getIntegerRangeValueMap() {
        return integerRangeValueMap;
    }

    public JTextField getCategoryNameField(){
        return categoryNameField;
    }

    public JPanel[] getPanelArray() {
        return panelArray;
    }

    public void init(){
        panel.setLayout(new GridBagLayout());
        GridBagConstraints mainGBC = new GridBagConstraints();
        mainGBC.fill = GridBagConstraints.BOTH;
        mainGBC.gridx = 0;
        mainGBC.gridy = 0;
        mainGBC.weightx = 1.0;
        mainGBC.weighty = 10.0;

        JPanel selectorPanel = new JPanel();
        selectorPanel.setName("Selector panel");

//        selectorPanel.setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));
        panel.add(selectorPanel, mainGBC);
        selectorPanel.setLayout(new GridBagLayout());
        GridBagConstraints selectorGBC = new GridBagConstraints();

        selectorGBC.fill = GridBagConstraints.BOTH;
        selectorGBC.weighty = 1;

        selectorGBC.gridx = 1;
        selectorGBC.gridy = 0;
        JPanel categoryNamePanel = new JPanel();
        categoryNamePanel.setLayout(new BorderLayout());
        selectorGBC.weightx = 0.9;
        categoryNameField = new JTextField(50);
        categoryNamePanel.add(categoryNameField, BorderLayout.CENTER);
        selectorPanel.add(categoryNamePanel, selectorGBC);
//        categoryNamePanel.setBorder(BorderFactory.createLineBorder(Color.MAGENTA));
        JLabel categoryLabel = new JLabel("<html>Category<br>Name</html>");
        categoryNamePanel.add(categoryLabel, BorderLayout.WEST);

        mainGBC.gridx = 0;
        mainGBC.gridy = 1;
        mainGBC.weightx = 1.0;
        mainGBC.weighty = 10.0;
        JPanel gridPanel = new JPanel();
        gridPanel.setName("Grid panel");
//        gridPanel.setPreferredSize(new Dimension(1200, 500));

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setPreferredSize(new Dimension(1200, 500));
        panel.add(scrollPane, mainGBC);

//        gridPanel.setLayout(new GridLayout(10, 0));
        gridPanel.setLayout(new GridBagLayout());
        GridBagConstraints tempGBC = new GridBagConstraints();
        tempGBC.fill = GridBagConstraints.HORIZONTAL;
        tempGBC.weightx = 1;
        tempGBC.weighty = 0.1;

        for(int i=0; i<combined_row_size; i++){
                JPanel currPanel = new JPanel();
                currPanel.setPreferredSize(new Dimension(1200, 60));
                currPanel.setLayout(new BorderLayout());
                JCheckBox checkBox = new JCheckBox();
                checkBox.setSelected(false);
                currPanel.add(checkBox, BorderLayout.WEST);
                panelArray[i] = currPanel;
                isEnabledMap.put(i, checkBox);
                if(i < ALL_STRING_ATTRIBUTES.size()){
                    StringAttribute currAttribute = ALL_STRING_ATTRIBUTES.get(i);
                    JPanel stringJPanel = createStringAttributePanel(currAttribute);
                    currPanel.add(stringJPanel, BorderLayout.CENTER);
                    for(Component tempC: stringJPanel.getComponents()){
                        if(tempC instanceof JTextField){
                            stringAttributeFieldMap.put(currAttribute, (JTextField) tempC);
                        }
                    }
                } else {
                    int fixedIndex = i - ALL_STRING_ATTRIBUTES.size();
                    if(fixedIndex < ALL_INTEGER_ATTRIBUTES.size()){
                        IntegerAttribute currAttribute = ALL_INTEGER_ATTRIBUTES.get(fixedIndex);
                        JPanel integerJPanel = createIntegerAttributePanel(currAttribute);
                        currPanel.add(integerJPanel, BorderLayout.CENTER);
                    } else {
                        int finalFixedIndex = i - ALL_STRING_ATTRIBUTES.size() - ALL_INTEGER_ATTRIBUTES.size();
                        if (finalFixedIndex < ALL_DATE_ATTRIBUTES.size()) {
                            DateAttribute currAttribute = ALL_DATE_ATTRIBUTES.get(finalFixedIndex);
                            JPanel dateJPanel = createDateAttributePanel(currAttribute);
                            currPanel.add(dateJPanel, BorderLayout.CENTER);
                        }
                    }
                }
            tempGBC.gridx = 0;
            tempGBC.gridy = i;
            gridPanel.add(panelArray[i], tempGBC);
        }

    }

    public JPanel createStringAttributePanel(StringAttribute currAttr){
        JPanel tempPanel = new JPanel();
        tempPanel.setLayout(new BorderLayout());
        tempPanel.setName(currAttr.getName());

        Icon infoIcon = UIManager.getIcon("OptionPane.informationIcon");
        JLabel infoLabel = new JLabel(infoIcon);
        String infoText = "<html>" +
                "Enter a list of strings, separated by commas, with NO SPACES" +
                "<br>e.g. John,David,Mark,Simon,Christopher" +
                "</div></html>";
        infoLabel.setToolTipText(infoText);
        tempPanel.add(infoLabel, BorderLayout.LINE_START);

        JLabel nameLabel = new JLabel("Attribute name: " + currAttr.getName());
        tempPanel.add(nameLabel, BorderLayout.PAGE_START);

        JTextField strTextField = new JTextField();
        tempPanel.add(strTextField, BorderLayout.CENTER);
        // support entering lists e.g. john,david,mark
        return tempPanel;
    }

    public JPanel createIntegerAttributePanel(IntegerAttribute currAttr){
        JPanel tempPanel = new JPanel();
        tempPanel.setName(currAttr.getName());
        tempPanel.setLayout(new BorderLayout());

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.setName("Left panel");
        tempPanel.add(leftPanel, BorderLayout.LINE_START);


        Icon infoIcon = UIManager.getIcon("OptionPane.informationIcon");
        JLabel infoLabel = new JLabel(infoIcon);
        String infoText = "<html>" +
                "For the greater than, less than or equal to options, enter a number e.g. 123" +
                "<br>For the range option, enter two numbers in each of the boxes" +
                "</div></html>";
        infoLabel.setToolTipText(infoText);
        leftPanel.add(infoLabel, BorderLayout.LINE_START);

        JLabel nameLabel = new JLabel("Attribute name: " + currAttr.getName());
        leftPanel.add(nameLabel, BorderLayout.PAGE_START);

        JPanel doubleNumericField = new JPanel();
        integerRangeValueMap.put(currAttr, doubleNumericField);
        doubleNumericField.setName("Double numeric field");
        doubleNumericField.setLayout(new BoxLayout(doubleNumericField, BoxLayout.X_AXIS));
        doubleNumericField.add(Box.createHorizontalGlue());

        NumericTextField lowerBoundField = new NumericTextField();
        lowerBoundField.setName("Lower bound field");
        NumericTextField upperBoundField = new NumericTextField();
        upperBoundField.setName("Upper bound field");

        doubleNumericField.add(lowerBoundField);
        doubleNumericField.add(Box.createHorizontalStrut(10));
        doubleNumericField.add(upperBoundField);
        doubleNumericField.add(Box.createHorizontalGlue());

        tempPanel.add(doubleNumericField, BorderLayout.CENTER);

        return tempPanel;
    }

    public JPanel createDateAttributePanel(DateAttribute currAttr) {
        JPanel tempPanel = new JPanel();
        tempPanel.setName(currAttr.getName());
        tempPanel.setLayout(new BorderLayout());

        JPanel leftPanel = new JPanel();
        leftPanel.setName("Left panel");
        leftPanel.setLayout(new BorderLayout());
        tempPanel.add(leftPanel, BorderLayout.LINE_START);

        Icon infoIcon = UIManager.getIcon("OptionPane.informationIcon");
        JLabel infoLabel = new JLabel(infoIcon);
        String infoText = "";
        infoLabel.setToolTipText(infoText);
        leftPanel.add(infoLabel, BorderLayout.LINE_START);

        JLabel nameLabel = new JLabel("Attribute name: " + currAttr.getName());
        leftPanel.add(nameLabel, BorderLayout.PAGE_START);

        tempPanel.add(createBetweenPanel(currAttr), BorderLayout.CENTER);

        return tempPanel;
    }

    public JPanel createBetweenPanel(DateAttribute attr){
        JPanel betweenPanel = new JPanel();
        dateBetweenValueMap.put(attr, betweenPanel);
        betweenPanel.setName("Between panel");
        betweenPanel.setLayout(new BoxLayout(betweenPanel, BoxLayout.X_AXIS));

        JPanel leftBetweenPanel = new JPanel(new BorderLayout());
        leftBetweenPanel.setName("Left between panel");
        JPanel rightBetweenPanel = new JPanel(new BorderLayout());
        rightBetweenPanel.setName("Right between panel");
        JButton firstBetweenButton = new JButton("Select first date");
        JButton secondBetweenButton = new JButton("Select second date");
        JTextField firstBetweenTextField = new JTextField();
        JTextField secondBetweenTextField = new JTextField();
        firstBetweenTextField.setEditable(false);
        secondBetweenTextField.setEditable(false);

        firstBetweenButton.addActionListener(e -> {
            if(!HelperClass.isFrameOpen) {
                HelperClass.showCalendar(firstBetweenTextField);
            }
        });

        secondBetweenButton.addActionListener(e -> {
            if(!HelperClass.isFrameOpen) {
                HelperClass.showCalendar(secondBetweenTextField);
            }
        });

        leftBetweenPanel.add(firstBetweenTextField, BorderLayout.CENTER);
        leftBetweenPanel.add(firstBetweenButton, BorderLayout.LINE_END);

        rightBetweenPanel.add(secondBetweenTextField, BorderLayout.CENTER);
        rightBetweenPanel.add(secondBetweenButton, BorderLayout.LINE_END);

        betweenPanel.add(Box.createHorizontalGlue());
        betweenPanel.add(leftBetweenPanel);
        betweenPanel.add(Box.createHorizontalStrut(10));
        betweenPanel.add(rightBetweenPanel);
        betweenPanel.add(Box.createHorizontalGlue());
        return betweenPanel;
    }

    public JPanel getPanel(){
        return panel;
    }

}