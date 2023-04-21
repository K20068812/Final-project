package guipanels;

import categories.Action;
import categories.*;
import categoryrules.*;
import com.toedter.calendar.JCalendar;
import principal_resource_attributes.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class HelperClass {

    public static boolean isFrameOpen = false;

    public static boolean addCorrespondingAttribute(List<StringAttribute> stringAttributeList, List<IntegerAttribute> integerAttributeList,
                                                    List<DateAttribute> dateAttributeList, int selectedIndex, String attributeName, String attributeValue){
        boolean errorParsing = false;
        if(selectedIndex == 0){
            stringAttributeList.add(new StringAttribute(attributeName, attributeValue));
        } else if(selectedIndex == 1){
            try{
                integerAttributeList.add(new IntegerAttribute(attributeName, Integer.parseInt(attributeValue)));
            } catch(Exception exception){
                exception.printStackTrace();
                errorParsing = true;
            }
        } else if(selectedIndex == 2){
            try{
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                dateAttributeList.add(new DateAttribute(attributeName, dateFormat.parse(attributeValue)));
            } catch(Exception exception){
                exception.printStackTrace();
                errorParsing = true;
            }
        }
        return errorParsing;
    }

    public static int getPrincipalIndex(List<Object> graphNodes, Principal principal){
        for (int i = 0; i < graphNodes.size(); i++) {
            if(graphNodes.get(i) instanceof Principal curr){
                if(curr.equals(principal)){
                    return i;
                }
            }
        }
        return -1;
    }

    public static int getCategoryIndex(List<Object> graphNodes, PrincipalCategory principalCategory){
        for (int i = 0; i < graphNodes.size(); i++) {
            if(graphNodes.get(i) instanceof PrincipalCategory curr){
                if(curr.equals(principalCategory)){
                    return i;
                }
            }
        }
        return -1;
    }

    public static int getActionIndex(List<Object> graphNodes, Action action){
        for (int i = 0; i < graphNodes.size(); i++) {
            if(graphNodes.get(i) instanceof Action){
                Action curr = (Action) graphNodes.get(i);
                if(action.equals(curr)){
                    return i;
                }
            }
        }
        return -1;
    }

    public static Action getActionByName(List<Action> inList, String actionName, String resourceName){
        Action toReturn = null;
        String formattedName = actionName.toLowerCase().strip() + "|" + resourceName.toLowerCase().strip();
        for(Action a: inList){
            String currName = a.getName().toLowerCase().strip() + "|" + a.getResource().getName().toLowerCase().strip();
            if(currName.equalsIgnoreCase(formattedName)){
                toReturn = a;
            }
        }
        return toReturn;
    }

    public static PrincipalCategory getCategoryByName(List<PrincipalCategory> inList, String text){
        PrincipalCategory toReturn = null;
        for (PrincipalCategory c : inList) {
            if (c.getName().strip().equalsIgnoreCase(text.strip())) {
                toReturn = c;
            }
        }
        return toReturn;
    }

    public static Resource getResourceByName(List<Resource> inList, String text){
        Resource toReturn = null;
        for (Resource r : inList) {
            if (r.getName().strip().equalsIgnoreCase(text.strip())) {
                toReturn = r;
            }
        }
        return toReturn;
    }

    public static Principal getPrincipalByName(List<Principal> inList, String text){
        Principal toReturn = null;
        for (Principal p : inList) {
            if (p.getName().strip().equalsIgnoreCase(text.strip())) {
                toReturn = p;
            }
        }
        return toReturn;
    }

    public static int[] findGridSize(int n) {
        if (n <= 0) {
            return new int[] { 0, 0 };
        }
        int rows = (int) Math.ceil(Math.sqrt(n));
        int cols = (int) Math.ceil((double) n / rows);
        return new int[] { rows, cols };
    }

    public static List<StringAttribute> getBlankStringAttributeList(List<Principal> principals) {
        List<StringAttribute> blankPrincipalAttributeList = new ArrayList<>();
        for (Principal p : principals) {
            for (StringAttribute a : p.getStringAttributeList()) {
                boolean exists = false;
                for (StringAttribute innerA : blankPrincipalAttributeList) {
                    if (innerA.getName().strip().equalsIgnoreCase(a.getName().strip())) {
                        exists = true;
                    }
                }
                if (!exists) {
                    blankPrincipalAttributeList.add(new StringAttribute(a.getName(), ""));
                }
            }
        }
        return blankPrincipalAttributeList;
    }
    public static List<IntegerAttribute> getBlankIntegerAttributeList(List<Principal> principals) {
        List<IntegerAttribute> blankPrincipalAttributeList = new ArrayList<>();
        for (Principal p : principals) {
            for (IntegerAttribute a : p.getIntegerAttributeList()) {
                boolean exists = false;
                for (IntegerAttribute innerA : blankPrincipalAttributeList) {
                    if (innerA.getName().strip().equalsIgnoreCase(a.getName().strip())) {
                        exists = true;
                    }
                }
                if (!exists) {
                    blankPrincipalAttributeList.add(new IntegerAttribute(a.getName(), 0));
                }
            }
        }
        return blankPrincipalAttributeList;
    }
    public static List<DateAttribute> getBlankDateAttributeList(List<Principal> principals) {
        List<DateAttribute> blankPrincipalAttributeList = new ArrayList<>();
        for (Principal p : principals) {
            for (DateAttribute a : p.getDateAttributeList()) {
                boolean exists = false;
                for (DateAttribute innerA : blankPrincipalAttributeList) {
                    if (innerA.getName().strip().equalsIgnoreCase(a.getName().strip())) {
                        exists = true;
                    }
                }
                if (!exists) {
                    blankPrincipalAttributeList.add(new DateAttribute(a.getName(), new Date()));
                }
            }
        }
        return blankPrincipalAttributeList;
    }

    public static String getRightPanelAttributeValue(JPanel rightPanel){
        for(Component c: rightPanel.getComponents()){
            if(c instanceof JTextField){
                return ((JTextField) c).getText().toLowerCase().strip();
            }
        }
        return "";
    }

    // evaluate user input from CategoryRulePanel and create corresponding rule object that will be added to the PrincipalCategory object.
    // If an error occurs during any of these processes (e.g. invalid input format), return false
    public static boolean evaluateStringAttr(StringAttribute currAttribute,  CategoryRulePanel selectedRulePanel, PrincipalCategory currPrincipalCategory){
        boolean error = false;
        JTextField currField = selectedRulePanel.getStringAttributeJTextFieldMap().get(currAttribute);
            if (currField != null && currField.getText() != null && !currField.getText().isBlank()){
                String[] values = currField.getText().split(",");
                for (int tempI = 0; tempI < values.length; tempI++) {
                    values[tempI] = values[tempI].toLowerCase().strip();
                }
                Set<String> valuesSet = new HashSet<>();
                Collections.addAll(valuesSet, values);
                List<String> valuesList = new ArrayList<>(valuesSet);
                StringRule tempStrRule = new StringRule(currAttribute, valuesList);
                currPrincipalCategory.getStringRules().add(tempStrRule);
            } else {
                error = true;
            }
            return error;
    }
    public static boolean evaluateIntegerAttr(IntegerAttribute intAttr, CategoryRulePanel selectedRulePanel, PrincipalCategory currPrincipalCategory){
        boolean error = false;
            int lowerBound = Integer.MAX_VALUE;
            int upperBound = 0;
            JPanel currRangePanel = selectedRulePanel.getIntegerRangeValueMap().get(intAttr);
            for (Component c : currRangePanel.getComponents()) {
                if (c instanceof JTextField) {
                    if (((JTextField) c).getText() != null && ! (((JTextField) c).getText().isBlank()) ){
                        try {
                            int currVal = Integer.parseInt(((JTextField) c).getText().strip());
                            if (lowerBound == Integer.MAX_VALUE) {
                                lowerBound = currVal;
                            } else {
                                upperBound = currVal;
                            }
                        } catch (Exception exception) {
                            exception.printStackTrace();
                            error = true;
                        }
                    } else {
                        error = true;
                    }
                }
            }
            if(lowerBound > upperBound){
                error = true;
            } else if(!error) {
                IntegerRule tempIntRule = new IntegerRule(intAttr, lowerBound, upperBound);
                currPrincipalCategory.getIntegerRules().add(tempIntRule);
            }
            return error;
    }
    public static boolean evaluateDateAttr(DateAttribute dateAttr, CategoryRulePanel selectedRulePanel, PrincipalCategory currPrincipalCategory){
        boolean error = false;
        Date lowerBound = null;
        Date upperBound = null;
        JPanel currBetweenPanel = selectedRulePanel.getDateBetweenValueMap().get(dateAttr);
        for(Component c: currBetweenPanel.getComponents()) {
            if (c instanceof JPanel) {
                for(Component innerC: ((JPanel) c).getComponents()){
                    if(innerC instanceof JTextField currField){
                        if (currField.getText() != null && !currField.getText().isBlank()) {
                            try {
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                Date currVal = dateFormat.parse(currField.getText());
                                if(lowerBound == null){
                                    lowerBound = currVal;
                                } else {
                                    upperBound = currVal;
                                }
                            } catch (Exception exception) {
                                error = true;
                            }
                        } else {
                            error = true;
                        }
                    }
                }
            }
        }
        if(lowerBound != null && upperBound != null && !error && upperBound.after(lowerBound)){
            DateRule tempDateRule = new DateRule(dateAttr, lowerBound, upperBound);
            currPrincipalCategory.getDateRules().add(tempDateRule);
        } else {
            error = true;
        }
        return error;
    }

    public static void showCalendar(JTextField inField) {
        SwingUtilities.invokeLater(() -> {
            isFrameOpen = true;
            JCalendar customCalendar = new JCalendar();
            JDialog dialog = new JDialog();
            dialog.setModal(true);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.addWindowListener(new WindowAdapter() {
                public void windowClosed(WindowEvent e) {
                    // reset the flag when the JDialog is closed
                    isFrameOpen = false;
                }
            });
            dialog.setSize(300, 300);

            JButton submitButton = new JButton("Submit");
            submitButton.addActionListener(e -> {
                Date selectedDate = customCalendar.getDate();
                if (selectedDate != null) {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.setTime(selectedDate);
                    Date selectedDateTime = selectedCalendar.getTime();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String dateWithTime = dateFormat.format(selectedDateTime);
                    if(inField.isEnabled()) {
                        inField.setText(dateWithTime);
                    }
                }
                dialog.dispose();
            });
            JPanel calendarPanel = new JPanel();
            calendarPanel.add(customCalendar);
            calendarPanel.add(submitButton);
            dialog.add(calendarPanel);
            dialog.setResizable(false);
            dialog.setVisible(true);
        });
    }

    public static JTextField getTextFieldFromComponents(Component[] components) {
        for (Component c : components) {
            if (c instanceof JTextField) {
                return (JTextField) c;
            }
        }
        return null;
    }

    public static JComboBox<?> getComboBoxFromComponents(Component[] components) {
        for (Component c : components) {
            if (c instanceof JComboBox<?>) {
                return (JComboBox<?>) c;
            }
        }
        return null;
    }

    public static JCheckBox getCheckBoxFromComponents(Component[] components) {
        for (Component c : components) {
            if (c instanceof JCheckBox) {
                return (JCheckBox) c;
            }
        }
        return null;
    }

    public static void showListDialog(JFrame frame, String title, List<String> items) {
        DefaultListModel<String> model = new DefaultListModel<>();
        for (String item : items) {
            model.addElement(item);
        }
        JList<String> list = new JList<>(model);
        JScrollPane scrollPane = new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(400, 400));
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel panel = new JPanel();
        panel.add(scrollPane);
        mainPanel.add(panel, BorderLayout.CENTER);
        JDialog dialog = new JDialog(frame, title, true);
        dialog.add(mainPanel);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> dialog.dispose());
        mainPanel.add(okButton, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }


    public static void showDoubleDialog(Frame frame, String dialogTitle, List<String> list1, List<String> list2, String label1, String label2) {
        DefaultListModel<String> model1 = new DefaultListModel<>();
        for (String s : list1) {
            model1.addElement(s);
        }
        JList<String> jList1 = new JList<>(model1);
        JScrollPane scrollPane1 = new JScrollPane(jList1);

        DefaultListModel<String> model2 = new DefaultListModel<>();
        for (String s : list2) {
            model2.addElement(s);
        }
        JList<String> jList2 = new JList<>(model2);
        JScrollPane scrollPane2 = new JScrollPane(jList2);

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel panel = new JPanel(new GridLayout(1, 2));
        panel.add(scrollPane1);
        panel.add(scrollPane2);
        mainPanel.add(panel, BorderLayout.CENTER);

        JDialog dialog = new JDialog(frame, dialogTitle, true);
        dialog.add(mainPanel);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(acl -> dialog.dispose());
        mainPanel.add(okButton, BorderLayout.SOUTH);

        JPanel labelPanel = new JPanel(new GridLayout(1, 2));
        labelPanel.add(new JLabel(label1));
        labelPanel.add(new JLabel(label2));
        mainPanel.add(labelPanel, BorderLayout.NORTH);

        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    public static String getCurrentTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalTime now = LocalTime.now();
        return now.format(formatter);
    }

    public static boolean hasCycle(PrincipalCategory category) {
        List<PrincipalCategory> visited = new ArrayList<>();
        List<PrincipalCategory> onStack = new ArrayList<>();
        Stack<PrincipalCategory> stack = new Stack<>();
        stack.push(category);

        while (!stack.isEmpty()) {
            PrincipalCategory current = stack.pop();
            visited.add(current);
            onStack.add(current);
            List<PrincipalCategory> juniorCategories = current.getJuniorCategories();
            for (PrincipalCategory juniorCategory : juniorCategories) {
                if (!visited.contains(juniorCategory)) {
                    stack.push(juniorCategory);
                } else if (onStack.contains(juniorCategory)) {
                    return true;
                }
            }
            onStack.remove(current);
        }
        return false;
    }

    public static List<PrincipalCategory> findAllJuniorCategories(PrincipalCategory category) {
        if (hasCycle(category)) {
            JOptionPane.showMessageDialog(null, "HAS CYCLE");
            return new ArrayList<>();
        } else {
            List<PrincipalCategory> juniorCategories = new ArrayList<>();
            Set<PrincipalCategory> visited = new HashSet<>();
            Queue<PrincipalCategory> queue = new LinkedList<>();
            queue.add(category);
            visited.add(category);
            while (!queue.isEmpty()) {
                PrincipalCategory currentCategory = queue.poll();
                juniorCategories.add(currentCategory);
                List<PrincipalCategory> subcategories = currentCategory.getJuniorCategories();
                for (PrincipalCategory subcategory : subcategories) {
                    if (!visited.contains(subcategory)) {
                        queue.add(subcategory);
                        visited.add(subcategory);
                    }
                }
            }
            return juniorCategories;
        }
    }

    public static List<Action> getAllResourceActions(List<Action> actions, Resource r){
        List<Action> toReturn = new ArrayList<>();
        for(Action a: actions){
            if(a.getResource().equals(r)){
                toReturn.add(a);
            }
        }
        return toReturn;
    }

    public static AssignCategories fixAssignCategories(AssignCategories bad) {

        List<Principal> principalList = new ArrayList<>(bad.getPrincipals());
        List<Resource> resourceList = new ArrayList<>(bad.getResources());
        List<Action> resourceActionList = new ArrayList<>();

        for (Action a : bad.getResourceActions()) {
            for (Resource r : resourceList) {
                if (a.getResource().equals(r)) {
                    Action temp = new Action(a.getName(), r);
                    resourceActionList.add(temp);
                }
            }
        }
        List<PrincipalCategory> allPrincipalCategories = bad.getPrincipalCategories();
        List<PrincipalCategory> fixedPrincipalCategories = new ArrayList<>();

        for (PrincipalCategory pc : allPrincipalCategories) {
            PrincipalCategory temp = new PrincipalCategory(pc.getName());

            for (Principal p : pc.getPrincipals()) {
                for (Principal ref : principalList) {
                    if (p.equals(ref)) {
                        temp.addPrincipal(ref);
                    }
                }
            }

            for (Action a : pc.getActions()) {
                for (Action ref : resourceActionList) {
                    if (a.equals(ref)) {
                        temp.addAction(ref);
                    }
                }
            }

            temp.setStringRules(pc.getStringRules());
            temp.setIntegerRules(pc.getIntegerRules());
            temp.setDateRules(pc.getDateRules());
            fixedPrincipalCategories.add(temp);
        }

        for (PrincipalCategory pc : allPrincipalCategories) {
            for (PrincipalCategory ref : fixedPrincipalCategories) {
                if (pc.getName().equals(ref.getName())) {
                    for (PrincipalCategory juniors : pc.getJuniorCategories()) {
                        for (PrincipalCategory temp : fixedPrincipalCategories) {
                            if (juniors.getName().equals(temp.getName())) {
                                ref.addJuniorCategory(temp);
                            }
                        }
                    }
                }
            }
        }

        AssignCategories fixed = new AssignCategories(principalList, fixedPrincipalCategories);
        fixed.setResources(resourceList);
        fixed.setResourceActions(resourceActionList);
        return fixed;
    }


    public static boolean isRuleSubset(PrincipalCategory junior, PrincipalCategory senior) {
        return junior.getPrincipals().containsAll(senior.getPrincipals());
    }



}
