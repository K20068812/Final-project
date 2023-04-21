package guipanels;
import categories.*;
import categories.Action;
import database.SaveAssignCategoriesWorker;
import database.UndoClass;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.geom.Point2;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.swing_viewer.DefaultView;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.swing_viewer.util.DefaultMouseManager;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.camera.Camera;
import org.graphstream.ui.view.util.InteractiveElement;
import org.graphstream.ui.view.util.MouseManager;
import principal_resource_attributes.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

public class GraphDisplay {
    Graph graph;
    JPanel MAIN_PANEL;
    JFrame frame;
    CardLayout cl;
    AssignCategories assignCategories;
    Stack<AssignCategories> PREVIOUS_STATE;
    ArrayList<String> ADMIN_LOG;
    DefaultView DEFAULT_VIEW;
    UndoClass undoClass;

    public GraphDisplay(AssignCategories assignCategories, UndoClass undoClass, JPanel MAIN_PANEL, JFrame frame, CardLayout cl) {
        this.MAIN_PANEL = MAIN_PANEL;
        this.frame = frame;
        this.cl = cl;
        this.assignCategories = assignCategories;
        this.undoClass = undoClass;
        PREVIOUS_STATE = new Stack<>();
        ADMIN_LOG = new ArrayList<>();
        MAIN_PANEL.add(createGraph(), "Graph");
        cl.show(MAIN_PANEL, "Graph");
    }

    public JPanel createGraph() {
        JPanel graphPanel = new JPanel();

        //GRAPH CODE
        System.setProperty("org.graphstream.ui", "swing");

        graph = new SingleGraph("embedded");
        SwingViewer viewer = new SwingViewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);

        DefaultView view = (DefaultView) viewer.addDefaultView(false);
        DEFAULT_VIEW = view;
        view.setPreferredSize(new Dimension(500, 500));
        viewer.enableAutoLayout();
        graph.setAttribute("ui.quality");
        graph.setAttribute("ui.antialias");

        // Add custom MouseManager for panning
        MouseManager mouseManager = new DefaultMouseManager() {
            private double lastX;
            private double lastY;
            private Node draggedNode;

            @Override
            public void mousePressed(MouseEvent event) {
                lastX = event.getX();
                lastY = event.getY();

                GraphicElement element = view.findGraphicElementAt(EnumSet.of(InteractiveElement.NODE), event.getX(), event.getY());
                if (element instanceof Node) {
                    draggedNode = (Node) element;
                } else {
                    draggedNode = null;
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                double x = e.getX();
                double y = e.getY();

                GraphicElement element = view.findGraphicElementAt(EnumSet.of(InteractiveElement.NODE), x, y);
                if (element instanceof Node node && ((Node) element).getId() != null) {
                    if (node.getId().startsWith("Principal category")) {
                        String catName = node.getId().substring(node.getId().indexOf(':') + 2);
                        PrincipalCategory curr = HelperClass.getCategoryByName(assignCategories.getPrincipalCategories(), catName);
                        if(curr != null){
                            if(e.getButton() == MouseEvent.BUTTON1) {
                                Set<Principal> categoryPrincipals = new HashSet<>();
                                for(PrincipalCategory pc: assignCategories.getPrincipalCategories()){
                                    if(HelperClass.findAllJuniorCategories(pc).contains(curr)){
                                        categoryPrincipals.addAll(pc.getPrincipals());
                                    }
                                }
                                List<String> categoryMembers = new ArrayList<>();
                                for (Principal p: categoryPrincipals) {
                                    categoryMembers.add(p.getName());
                                }
                                List<String> categoryRules = new ArrayList<>();
                                curr.getStringRules().forEach(rule -> categoryRules.add(rule.toString()));
                                curr.getIntegerRules().forEach(rule -> categoryRules.add(rule.toString()));
                                curr.getDateRules().forEach(rule -> categoryRules.add(rule.toString()));

                                HelperClass.showDoubleDialog(frame, "Info", categoryRules, categoryMembers, "Rules", "Members");
                            } else if(e.getButton() == MouseEvent.BUTTON3) {
                                Set<Action> categoryActions = new HashSet<>();
                                List<String> juniorCategories = new ArrayList<>();
                                for(PrincipalCategory pc: HelperClass.findAllJuniorCategories(curr)){
                                    categoryActions.addAll(pc.getActions());
                                    if(!pc.equals(curr)){
                                        juniorCategories.add(pc.getName());
                                    }
                                }
                                List<String> actions = new ArrayList<>();
                                for (Action a: categoryActions) {
                                    actions.add(a.toString());
                                }
                                HelperClass.showDoubleDialog(frame, "Info", actions, juniorCategories, "Permissions", "Junior categories");
                            }
                        }
                    } else if(node.getId().startsWith("Principal")){
                        String principalName = node.getId().substring(node.getId().indexOf(':') + 2);
                        Principal curr = HelperClass.getPrincipalByName(assignCategories.getPrincipals(), principalName);
                        if(curr != null){
                            if(e.getButton() == MouseEvent.BUTTON1) {
                                List<String> attributes = new ArrayList<>();
                                curr.getStringAttributeList().forEach(c -> attributes.add(c.toString()));
                                curr.getIntegerAttributeList().forEach(c -> attributes.add(c.toString()));
                                curr.getDateAttributeList().forEach(c -> attributes.add(c.toString()));
                                HelperClass.showListDialog(frame, "Attributes ", attributes);
                            } else if(e.getButton() == MouseEvent.BUTTON3){
                                Set<PrincipalCategory> associatedCategories = new HashSet<>();
                                Set<Action> associatedPermissions = new HashSet<>();
                                for(PrincipalCategory pc: assignCategories.getPrincipalCategories()){
                                    if(pc.getPrincipals().contains(curr)){
                                        associatedCategories.add(pc);
                                        associatedCategories.addAll(HelperClass.findAllJuniorCategories(pc));
                                    }
                                }
                                for(PrincipalCategory pc: associatedCategories){
                                    associatedPermissions.addAll(pc.getActions());
                                }
                                List<String> categoryList = new ArrayList<>();
                                for (PrincipalCategory pc: associatedCategories) {
                                    categoryList.add(pc.getName());
                                }
                                List<String> permissionList = new ArrayList<>();
                                for (Action a: associatedPermissions) {
                                    permissionList.add(a.toString());
                                }
                                HelperClass.showDoubleDialog(frame, "Info", categoryList, permissionList, "Categories", "Actions");
                            }
                        }
                    }  else if(node.getId().startsWith("Action")){
                        String resourceName = "";
                        Action curr = null;
                        Node[] neighborNodeArray = node.neighborNodes().toArray(Node[]::new);
                        for(Node neighborNode : neighborNodeArray){
                            if(neighborNode.getId().startsWith("Resource")){
                                String nodeId = node.getId() + neighborNode.getId();
                                if(graph.getEdge(nodeId) != null){
                                    resourceName = neighborNode.getId().substring(neighborNode.getId().indexOf(':') + 2);
                                }
                            }
                        }
                        String actionName = node.getId().substring(node.getId().indexOf(':') + 2).replace(resourceName, "").trim();
                        curr = HelperClass.getActionByName(assignCategories.getResourceActions(), actionName, resourceName);
                        if(curr != null){
                                Set<Principal> authorisedPrincipals = new HashSet<>();
                                for(PrincipalCategory pc: assignCategories.getPrincipalCategories()){
                                    for(PrincipalCategory junior: HelperClass.findAllJuniorCategories(pc)){
                                        if(junior.getActions().contains(curr)){
                                            authorisedPrincipals.addAll(pc.getPrincipals());
                                        }
                                    }
                                }
                                List<String> tempList = new ArrayList<>();
                                for (Principal p: authorisedPrincipals) {
                                    tempList.add(p.getName());
                                }
                                HelperClass.showListDialog(frame, "Principals ", tempList);
                        }
                    }else if(node.getId().startsWith("Resource")){
                        String resourceName = node.getId().substring(node.getId().indexOf(':') + 2);
                        Resource curr = HelperClass.getResourceByName(assignCategories.getResources(), resourceName);
                        if(curr != null){
                            Set<Principal> authorisedPrincipals = new HashSet<>();
                            for(Action a : HelperClass.getAllResourceActions(assignCategories.getResourceActions(), curr)) {
                                for (PrincipalCategory pc : assignCategories.getPrincipalCategories()) {
                                    for (PrincipalCategory junior : HelperClass.findAllJuniorCategories(pc)) {
                                        if (junior.getActions().contains(a)) {
                                            authorisedPrincipals.addAll(pc.getPrincipals());
                                        }
                                    }
                                }
                            }
                            List<String> tempList = new ArrayList<>();
                            for (Principal p: authorisedPrincipals) {
                                tempList.add(p.getName());
                            }
                            HelperClass.showListDialog(frame, "Principals ", tempList);
                        }
                    }
                }
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                double currentX = event.getX();
                double currentY = event.getY();
                double dx = (currentX - lastX) * 0.01; // reduce panning speed by a factor of 10*10
                double dy = (currentY - lastY) * 0.01;
                if (draggedNode == null) {

                    view.getCamera().setViewCenter(
                            view.getCamera().getViewCenter().x - dx / view.getCamera().getViewPercent(),
                            view.getCamera().getViewCenter().y + dy / view.getCamera().getViewPercent(),
                            0);

                }
                lastX = currentX;
                lastY = currentY;
            }

        };
        view.setMouseManager(mouseManager);

        view.addMouseWheelListener(e -> {
            e.consume();
            int i = e.getWheelRotation();
            double factor = Math.pow(1.25, i);
            Camera cam = view.getCamera();
            double zoom = cam.getViewPercent() * factor;
            Point2 pxCenter = cam.transformGuToPx(cam.getViewCenter().x, cam.getViewCenter().y, 0);
            Point3 guClicked = cam.transformPxToGu(e.getX(), e.getY());
            double newRatioPx2Gu = cam.getMetrics().ratioPx2Gu / factor;
            double x = guClicked.x + (pxCenter.x - e.getX()) / newRatioPx2Gu;
            double y = guClicked.y - (pxCenter.y - e.getY()) / newRatioPx2Gu;
            cam.setViewCenter(x, y, 0);
            cam.setViewPercent(zoom);
        });

        graphPanel.setLayout(new BorderLayout());
        graphPanel.add(view, BorderLayout.CENTER);

        JButton addPrincipalButton = new JButton("<html>Update/Create<br>principal</html>");
        addPrincipalButton.addActionListener(e -> addPrincipal());
        JButton removePrincipalButton = new JButton("<html>Remove<br>principal</html>");
        removePrincipalButton.addActionListener(e -> removePrincipal());
        JButton updateCategoryRulesButton = new JButton("<html>Update/Create<br>category</html>");
        updateCategoryRulesButton.addActionListener(e -> updateCategoryRules());
        JButton deleteCategoryButton = new JButton("<html>Delete<br>category</html>");
        deleteCategoryButton.addActionListener(e -> deleteCategory());
        JButton refreshGraphButton = new JButton("<html>Refresh<br>graph</html>");
        refreshGraphButton.addActionListener(e -> updateGraph());
        JButton centerGraphButton = new JButton("<html>Center<br>graph");
        centerGraphButton.addActionListener(e -> centerGraph());
        JButton addResourceButton = new JButton("<html>Add<br>resource</html>");
        addResourceButton.addActionListener(e -> addResource());
        JButton removeResourceButton = new JButton("<html>Remove<br>resource</html>");
        removeResourceButton.addActionListener(e -> removeResource());
        JButton addActionButton = new JButton("<html>Add<br>action</html>");
        addActionButton.addActionListener(e -> addAction());
        JButton removeActionButton = new JButton("<html>Remove<br>action</html>");
        removeActionButton.addActionListener(e -> removeAction());
        JButton checkCategoryRulesButton = new JButton("<html>Configure<br>permissions</html>");
        checkCategoryRulesButton.addActionListener(e -> configurePermissions());
        JButton updateHierarchyButton = new JButton("<html>Update<br>hierarchy</html>");
        updateHierarchyButton.addActionListener(e -> updateHierarchy());
        JButton saveToDBButton = new JButton("<html>Save to<br>database</html>");
        saveToDBButton.addActionListener(e -> saveToDB(saveToDBButton));
        JButton checkPARButton = new JButton("<html>Check<br>PAR</html>");
        checkPARButton.addActionListener(e -> checkPar());
        JButton infoIcon = new JButton("<html>Query<br>Information</html>");
        infoIcon.addActionListener(e -> showInfoPanel());
        JButton undoActionButton = new JButton("<html>Undo<br>action</html>");
        undoActionButton.addActionListener(e -> undoAction());
        JButton viewLogButton = new JButton("<html>View<br>log</html>");
        viewLogButton.addActionListener(e -> viewLog());

        String[] graphViewOptions = {"Normal view", "PCA", "Unassigned", "Hide unassigned"};
        JComboBox<String> graphViewDropdown = new JComboBox<>(graphViewOptions);
        graphViewDropdown.setMaximumSize(new Dimension(150, 30));
        graphViewDropdown.addActionListener(e -> filterGraphView(graphViewDropdown.getSelectedIndex()));

        JButton policy2 = new JButton("2nd query");
        policy2.addActionListener(e -> secondQuery());
        JButton policy3 = new JButton("3rd query");
        policy3.addActionListener(e -> thirdQuery());
        JButton policy4 = new JButton("4th query");
        policy4.addActionListener(e -> fourthQuery());
        JButton policy5 = new JButton("5th query");
        policy5.addActionListener(e -> fifthQuery());
        JButton policy6 = new JButton("6th query");
        policy6.addActionListener(e -> sixthQuery());

        updateGraph();

        JPanel topButtonPanel = new JPanel();
        JPanel leftButtonPanel = new JPanel();
        leftButtonPanel.setLayout(new BoxLayout(leftButtonPanel, BoxLayout.Y_AXIS));
        leftButtonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add spacing to the left panel

        JPanel rightButtonPanel = new JPanel();
        rightButtonPanel.setLayout(new BoxLayout(rightButtonPanel, BoxLayout.Y_AXIS));
        rightButtonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add spacing to the left panel

        graphPanel.add(topButtonPanel, BorderLayout.NORTH);
        graphPanel.add(rightButtonPanel, BorderLayout.EAST);
        graphPanel.add(leftButtonPanel, BorderLayout.WEST);

        JButton[] topButtons = {
                addPrincipalButton, removePrincipalButton, addResourceButton, removeResourceButton, addActionButton,
                removeActionButton, updateCategoryRulesButton, deleteCategoryButton, checkCategoryRulesButton, updateHierarchyButton
        };
        for(JButton jButton : topButtons){
            topButtonPanel.add(jButton);
        }
        JButton[] leftButtons = {
                refreshGraphButton, centerGraphButton
        };
        for(JButton jButton : leftButtons){
            leftButtonPanel.add(jButton);
            leftButtonPanel.add(Box.createVerticalStrut(10));
        }
        leftButtonPanel.add(graphViewDropdown);
        graphViewDropdown.setMaximumSize(new Dimension(150, 30));

        JButton[] rightButtons = {
                saveToDBButton, checkPARButton, policy2, policy3, policy4, policy5, policy6, viewLogButton, undoActionButton, infoIcon
        };
        for(JButton jButton : rightButtons){
            rightButtonPanel.add(jButton);
            rightButtonPanel.add(Box.createVerticalStrut(10));
        }
        return graphPanel;
    }


    public void secondQuery() { // "Can all resources be accessed by at least one user?"
            boolean res = true;
            List<String> names = new ArrayList<>();
            for(Resource r : assignCategories.getResources()){
                Set<Principal> authorisedPrincipals = new HashSet<>();
                List<Action> correspondingActions = HelperClass.getAllResourceActions(assignCategories.getResourceActions(), r);
                for(Action curr : correspondingActions){
                    for(PrincipalCategory pc: assignCategories.getPrincipalCategories()){
                        for(PrincipalCategory junior: HelperClass.findAllJuniorCategories(pc)){
                            if(junior.getActions().contains(curr)){
                                authorisedPrincipals.addAll(pc.getPrincipals());
                            }
                        }
                    }
                }
                if(authorisedPrincipals.isEmpty()){
                    names.add(r.getName());
                    res = false;
                }
            }
            String output = (res) ? "True" : "False" + " -> " + names;
            JOptionPane.showMessageDialog(frame, output);
    }

    public void thirdQuery() { // "Can all users access at least some of the resources?"
            boolean res = true;
            List<String> names = new ArrayList<>();
            for (Principal curr : assignCategories.getPrincipals()) {
                Set<PrincipalCategory> associatedCategories = new HashSet<>();
                Set<Action> associatedPermissions = new HashSet<>();
                for (PrincipalCategory pc : assignCategories.getPrincipalCategories()) {
                    if (pc.getPrincipals().contains(curr)) {
                        associatedCategories.add(pc);
                        associatedCategories.addAll(HelperClass.findAllJuniorCategories(pc));
                    }
                }
                for (PrincipalCategory pc : associatedCategories) {
                    associatedPermissions.addAll(pc.getActions());
                }
                if (associatedPermissions.isEmpty()) {
                    names.add(curr.getName());
                    res = false;
                }
            }
            String output = (res) ? "True" : "False" + " -> " + names;
            JOptionPane.showMessageDialog(frame, output);
    }

    public void fourthQuery(){ // "Are all the principals associated with at least one category?"
            boolean res = true;
            List<String> names = new ArrayList<>();
            for (Principal curr : assignCategories.getPrincipals()) {
                Set<PrincipalCategory> associatedCategories = new HashSet<>();
                for (PrincipalCategory pc : assignCategories.getPrincipalCategories()) {
                    if (pc.getPrincipals().contains(curr)) {
                        associatedCategories.add(pc);
                        associatedCategories.addAll(HelperClass.findAllJuniorCategories(pc));
                    }
                }
                if(associatedCategories.isEmpty()){
                    names.add(curr.getName());
                    res = false;
                }
            }
            String output = (res) ? "True" : "False" + " -> " + names;
            JOptionPane.showMessageDialog(frame, output);
    }

    public void fifthQuery(){ // "Are there permissions associated with each category?"
            boolean res = true;
            List<String> names = new ArrayList<>();
            for(PrincipalCategory curr : assignCategories.getPrincipalCategories()) {
                Set<Action> categoryActions = new HashSet<>();
                for (PrincipalCategory pc : HelperClass.findAllJuniorCategories(curr)) {
                    categoryActions.addAll(pc.getActions());
                }
                if(categoryActions.isEmpty()){
                    names.add(curr.getName());
                    res = false;
                }
            }
            String output = (res) ? "True" : "False" + " -> " + names;
            JOptionPane.showMessageDialog(frame, output);
    }

    public void sixthQuery() { // "Are rule definitions compatible with axiom c0 in paper Specification and Analysis of ABAC Policies via the Category-Based Metamodel"
            List<String> outputList = new ArrayList<>();
            for (PrincipalCategory pc : assignCategories.getPrincipalCategories()) {
                for (PrincipalCategory jr : pc.getJuniorCategories()) {
                    if (!HelperClass.isRuleSubset(jr, pc)) {
                        outputList.add(pc.getName() + " -> " + jr.getName());
                    }
                }
            }
            if (outputList.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "true");
            } else {
                HelperClass.showListDialog(frame, "Categories", outputList);
            }
    }

    public void showInfoPanel(){
        String infoString = "<html>" +
                "<br>2nd query - Can all resources be accessed by at least one user?" +
                "<br>3rd query - Can all users access at least some of the resources?" +
                "<br>4th query - Are all the principals associated with at least one category?" +
                "<br>5th query - Are there permissions associated with each category? "
                +"<br>6th query - If a category is senior, does it's defining condition <br>imply membership to it's junior categories?"
               + "</html>";
        JOptionPane.showMessageDialog(frame, infoString);
    }

    public void filterGraphView(int index){
        updateGraph();
        SwingUtilities.invokeLater(() -> {
            if(index == 1) {
                graph.nodes().forEach(node -> {
                        if(! (node.getId().startsWith("Principal") || node.getId().startsWith("Principal category")) ||
                                (node.neighborNodes().toArray(Node[]::new)).length == 0){
                            node.setAttribute("ui.hide");
                            node.edges().forEach(edge ->{
                                edge.setAttribute("ui.hide");
                            });
                        }
                });
            } else if(index == 2){
                graph.nodes().forEach(node -> {
                    Node[] nodeArray = node.neighborNodes().toArray(Node[]::new);
                    if(nodeArray.length > 0){
                        node.setAttribute("ui.hide");
                        node.edges().forEach(edge -> edge.setAttribute("ui.hide"));
                    }
                });
            } else if(index == 3){
                graph.nodes().forEach(node -> {
                    Node[] nodeArray = node.neighborNodes().toArray(Node[]::new);
                    if(nodeArray.length == 0){
                        node.setAttribute("ui.hide");
                    }
                });
            }
        });
        centerGraph();
    }

    public void centerGraph() {
        SwingUtilities.invokeLater(() -> DEFAULT_VIEW.getCamera().resetView());
    }

    public void addAction(){
        JDialog dialog = new JDialog();
        JPanel panel = new JPanel();
        dialog.add(panel);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel label1 = new JLabel("Resource name");
        JTextField field1 = new JTextField(10);
        panel.add(label1);
        panel.add(field1);

        JLabel label2 = new JLabel("Action name");
        JTextField field2 = new JTextField(10);
        panel.add(label2);
        panel.add(field2);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            String formattedResourceText = field1.getText().toLowerCase().strip();
            String formattedActionText = field2.getText().toLowerCase().strip();
            Resource r = HelperClass.getResourceByName(assignCategories.getResources(), formattedResourceText);
            boolean error = false;
            if(r != null){
                for(Action a : assignCategories.getResourceActions()){
                    if(a.getResource().equals(r) && a.getName().strip().equalsIgnoreCase(formattedActionText)){
                        error = true;
                    }
                }
            } else {
                error = true;
            }
            if(!error){
                Action newAction = new Action(formattedActionText, r);
                undoClass.addAddAction(newAction);
                assignCategories.getResourceActions().add(newAction);
                updateGraph();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(frame, "Resource not found or action already exists");
            }
        });
        panel.add(okButton);
        dialog.setTitle("Add action");
        panel.setPreferredSize(new Dimension(300, 300));
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    public void removeAction(){
        JDialog dialog = new JDialog();
        JPanel panel = new JPanel();
        dialog.add(panel);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel label1 = new JLabel("Resource name");
        JTextField field1 = new JTextField(10);
        panel.add(label1);
        panel.add(field1);

        JLabel label2 = new JLabel("Action name");
        JTextField field2 = new JTextField(10);
        panel.add(label2);
        panel.add(field2);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            String formattedResourceText = field1.getText().toLowerCase().strip();
            String formattedActionText = field2.getText().toLowerCase().strip();
            Action toRemove = HelperClass.getActionByName(assignCategories.getResourceActions(),formattedActionText, formattedResourceText);
            if(toRemove != null  && HelperClass.getAllResourceActions(assignCategories.getResourceActions(), toRemove.getResource()).size() > 1){
                List<PrincipalCategory> nameList = new ArrayList<>();
                assignCategories.getPrincipalCategories().forEach(principalCategory -> {
                            if (principalCategory.getActions().contains(toRemove)) {
                                nameList.add(principalCategory);
                            }
                            principalCategory.removeAction(toRemove);
                        });
                undoClass.addRemoveAction(toRemove, nameList);
                assignCategories.getResourceActions().remove(toRemove);
                updateGraph();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(frame, "<html>Not found or only one action<br>on resource remaining</html>");
            }
        });
        panel.add(okButton);
        dialog.setTitle("Remove action");
        panel.setPreferredSize(new Dimension(300, 300));
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    public void undoAction() {
        if (!undoClass.getActionTracker().isEmpty()) {
            int responseInt = JOptionPane.showConfirmDialog(frame, "Are you sure you want to undo?");
            if (responseInt == JOptionPane.YES_OPTION) {
                List<Object> lastEntry = undoClass.getActionTracker().get(undoClass.getActionTracker().size() - 1);
                try {
                    UndoClass.UNDO_TYPE actionType = (UndoClass.UNDO_TYPE) lastEntry.get(0);
                    if (actionType == UndoClass.UNDO_TYPE.UPDATE_PRINCIPAL) {
                        Principal oldPrincipal = (Principal) lastEntry.get(1);
                        Principal newPrincipal = (Principal) lastEntry.get(2);
                        assignCategories.removePrincipal(newPrincipal);
                        assignCategories.addPrincipal(oldPrincipal);
                        assignCategories.evaluatePrincipalCategories();

                    } else if (actionType == UndoClass.UNDO_TYPE.CREATE_PRINCIPAL) {
                        Principal principal = (Principal) lastEntry.get(1);
                        assignCategories.removePrincipal(HelperClass.getPrincipalByName(assignCategories.getPrincipals(), principal.getName()));
                        assignCategories.evaluatePrincipalCategories();

                    } else if (actionType == UndoClass.UNDO_TYPE.REMOVE_PRINCIPAL) {
                        Principal oldPrincipal = (Principal) lastEntry.get(1);
                        assignCategories.addPrincipal(oldPrincipal);
                        assignCategories.evaluatePrincipalCategories();

                    } else if (actionType == UndoClass.UNDO_TYPE.ADD_RESOURCE) {
                        Resource resource = (Resource) lastEntry.get(1);
                        Resource fixedRes = HelperClass.getResourceByName(assignCategories.getResources(), resource.getName());
                        List<Action> resourceActions = HelperClass.getAllResourceActions(assignCategories.getResourceActions(), fixedRes);
                        assignCategories.getPrincipalCategories().forEach(principalCategory -> principalCategory.getActions().removeAll(resourceActions));
                        assignCategories.getResourceActions().removeAll(resourceActions);
                        assignCategories.getResources().remove(fixedRes);
                        assignCategories.evaluatePrincipalCategories();

                    } else if (actionType == UndoClass.UNDO_TYPE.REMOVE_RESOURCE) {
                        Resource toAdd = (Resource) lastEntry.get(1);
                        Map<Action, List<PrincipalCategory>> assignedPerms = (Map<Action, List<PrincipalCategory>>) lastEntry.get(2);
                        assignCategories.addResource(toAdd);
                        for (Action a : assignedPerms.keySet()) {
                            Action fixedAction = new Action(a.getName(), toAdd);
                            assignCategories.getResourceActions().add(fixedAction);
                            List<PrincipalCategory> categories = assignedPerms.get(a);
                            for (PrincipalCategory currentPrincipalCategory : categories) {
                                PrincipalCategory fixedRef = HelperClass.getCategoryByName(assignCategories.getPrincipalCategories(),
                                        currentPrincipalCategory.getName());
                                fixedRef.addAction(a);
                            }
                        }

                    } else if (actionType == UndoClass.UNDO_TYPE.ADD_ACTION) {
                        Action a = (Action) lastEntry.get(1);
                        Action ref = HelperClass.getActionByName(assignCategories.getResourceActions(), a.getName(), a.getResource().getName());
                        assignCategories.getPrincipalCategories().forEach(principalCategory -> principalCategory.removeAction(ref));
                        assignCategories.getResourceActions().remove(ref);

                    } else if (actionType == UndoClass.UNDO_TYPE.REMOVE_ACTION) {
                        Action a = (Action) lastEntry.get(1);
                        Resource resourceRef = HelperClass.getResourceByName(assignCategories.getResources(), a.getResource().getName());
                        Action actionRef = new Action(a.getName(), resourceRef);
                        assignCategories.getResourceActions().add(actionRef);
                        List<PrincipalCategory> categories = (List<PrincipalCategory>) lastEntry.get(2);
                        for (PrincipalCategory category : categories) {
                            PrincipalCategory fixedRef = HelperClass.getCategoryByName(assignCategories.getPrincipalCategories(), category.getName());
                            fixedRef.addAction(actionRef);
                        }

                    } else if (actionType == UndoClass.UNDO_TYPE.UPDATE_CATEGORY) {
                        PrincipalCategory oldCategory = (PrincipalCategory) lastEntry.get(1);
                        PrincipalCategory newCategory = (PrincipalCategory) lastEntry.get(2);
                        newCategory.setStringRules(oldCategory.getStringRules());
                        newCategory.setIntegerRules(oldCategory.getIntegerRules());
                        newCategory.setDateRules(oldCategory.getDateRules());
                        newCategory.getPrincipals().clear();
                        newCategory.setName(oldCategory.getName());
                        assignCategories.evaluatePrincipalCategories();

                    } else if (actionType == UndoClass.UNDO_TYPE.CREATE_CATEGORY) {
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
                        List<Action> fixedActions = new ArrayList<>();
                        for (Action oldAction : oldCategory.getActions()) {
                            Action actionRef = HelperClass.getActionByName(assignCategories.getResourceActions(), oldAction.getName(), oldAction.getResource().getName());
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

                    } else if (actionType == UndoClass.UNDO_TYPE.UPDATE_PERMISSIONS) {
                        PrincipalCategory curr = (PrincipalCategory) lastEntry.get(1);
                        List<Action> oldActions = (List<Action>) lastEntry.get(2);
                        curr.setActions(oldActions);

                    } else if (actionType == UndoClass.UNDO_TYPE.UPDATE_HIERARCHY) {
                        PrincipalCategory curr = (PrincipalCategory) lastEntry.get(1);
                        PrincipalCategory categoryRef = HelperClass.getCategoryByName(assignCategories.getPrincipalCategories(), curr.getName());
                        categoryRef.getJuniorCategories().clear();
                        List<PrincipalCategory> oldJrCategories = (List<PrincipalCategory>) lastEntry.get(2);
                        for (PrincipalCategory oldJr : oldJrCategories) {
                            PrincipalCategory jrRef = HelperClass.getCategoryByName(assignCategories.getPrincipalCategories(), oldJr.getName());
                            categoryRef.addJuniorCategory(jrRef);
                        }

                    }
                } catch(Exception err){
                    err.printStackTrace();
                    JOptionPane.showMessageDialog(frame, JOptionPane.ERROR_MESSAGE);
                }
                undoClass.getActionTracker().remove(lastEntry);
                if(!ADMIN_LOG.isEmpty()) {
                    ADMIN_LOG.remove(ADMIN_LOG.get(ADMIN_LOG.size() - 1));
                }
                updateGraph();
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Empty");
        }
    }

    public void checkPar(){
        JDialog dialog = new JDialog();
        JPanel panel = new JPanel();
        dialog.add(panel);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel label1 = new JLabel("Principal name");
        JTextField field1 = new JTextField(10);
        panel.add(label1);
        panel.add(field1);

        JLabel label2 = new JLabel("Action name");
        JTextField field2 = new JTextField(10);
        panel.add(label2);
        panel.add(field2);

        JLabel label3 = new JLabel("Resource name");
        JTextField field3 = new JTextField(10);
        panel.add(label3);
        panel.add(field3);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            Principal principal = HelperClass.getPrincipalByName(assignCategories.getPrincipals(), field1.getText());
            Action action = HelperClass.getActionByName(assignCategories.getResourceActions(), field2.getText(), field3.getText());
            if(principal != null && action != null){
                Set<Action> perms = new HashSet<>();
                for(PrincipalCategory pc : assignCategories.getPrincipalCategories()){
                    List<PrincipalCategory> allCategories = HelperClass.findAllJuniorCategories(pc);
                    if(pc.getPrincipals().contains(principal)) {
                        allCategories.forEach(category -> perms.addAll(category.getActions()));
                    }
                }
                String isAuthorised = (perms.contains(action)) ? "Authorised " : " Not authorised";
                JOptionPane.showMessageDialog(frame, isAuthorised);
            } else {
                JOptionPane.showMessageDialog(frame, "Does not exist");
            }
        });
        panel.add(okButton);
        dialog.setTitle("Check PAR");
        panel.setPreferredSize(new Dimension(300, 300));
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    public void saveToDB(JButton button){
        SaveAssignCategoriesWorker saveAssignCategoriesWorker = new SaveAssignCategoriesWorker(frame, assignCategories, undoClass, button);
        saveAssignCategoriesWorker.execute();
    }

    public void viewLog() {
        try {
            List<String> logList = new ArrayList<>();
            for (List<Object> innerList : undoClass.getActionTracker()) {
                UndoClass.UNDO_TYPE firstElem = (UndoClass.UNDO_TYPE) innerList.get(0);
                if (firstElem == UndoClass.UNDO_TYPE.UPDATE_PRINCIPAL) {
                    logList.add(firstElem + " " + ((Principal) innerList.get(2)).getName());
                } else if (firstElem == UndoClass.UNDO_TYPE.CREATE_PRINCIPAL) {
                    logList.add(firstElem + " " +  ((Principal) innerList.get(1)).getName());
                } else if (firstElem == UndoClass.UNDO_TYPE.REMOVE_PRINCIPAL) {
                    logList.add(firstElem + " " +  ((Principal) innerList.get(1)).getName());
                } else if (firstElem == UndoClass.UNDO_TYPE.ADD_RESOURCE) {
                    logList.add(firstElem + " " +  ((Resource) innerList.get(1)).getName());
                } else if (firstElem == UndoClass.UNDO_TYPE.REMOVE_RESOURCE) {
                    logList.add(firstElem + " " +  ((Resource) innerList.get(1)).getName());
                } else if (firstElem == UndoClass.UNDO_TYPE.ADD_ACTION) {
                    logList.add(firstElem + " " +  "Action: " + ((Action) innerList.get(1)).getName() +
                            " Resource: " + ((Action) innerList.get(1)).getResource().getName());
                } else if (firstElem == UndoClass.UNDO_TYPE.REMOVE_ACTION) {
                    logList.add(firstElem + " " +  "Action: " + ((Action) innerList.get(1)).getName() +
                            " Resource: " + ((Action) innerList.get(1)).getResource().getName());
                } else if (firstElem == UndoClass.UNDO_TYPE.UPDATE_CATEGORY) {
                    logList.add(firstElem + " " +  ((PrincipalCategory) innerList.get(2)).getName());
                } else if (firstElem == UndoClass.UNDO_TYPE.CREATE_CATEGORY) {
                    logList.add(firstElem + " " +  ((PrincipalCategory) innerList.get(1)).getName());
                } else if (firstElem == UndoClass.UNDO_TYPE.REMOVE_CATEGORY) {
                    logList.add(firstElem + " " +  ((PrincipalCategory) innerList.get(1)).getName());
                } else if (firstElem == UndoClass.UNDO_TYPE.UPDATE_PERMISSIONS) {
                    logList.add(firstElem + " " +  ((PrincipalCategory) innerList.get(1)).getName());
                } else if (firstElem == UndoClass.UNDO_TYPE.UPDATE_HIERARCHY) {
                    logList.add(firstElem + " " +  ((PrincipalCategory) innerList.get(1)).getName());
                }
            }
            HelperClass.showListDialog(frame, "Admin log", logList);
        } catch(Exception err){
            err.printStackTrace();
            JOptionPane.showMessageDialog(frame, JOptionPane.ERROR_MESSAGE);
        }
    }

    public void deleteCategory() {
        String text = JOptionPane.showInputDialog("Enter category name");
        if (text != null && !text.isBlank()) {
            PrincipalCategory toDelete = HelperClass.getCategoryByName(assignCategories.getPrincipalCategories(), text);
            if (toDelete != null) {
                List<PrincipalCategory> superiorCategories = new ArrayList<>();
                for(PrincipalCategory pc: assignCategories.getPrincipalCategories()){
                    if(!pc.equals(toDelete) && HelperClass.findAllJuniorCategories(pc).contains(toDelete)){
                        superiorCategories.add(pc);
                    }
                }
                undoClass.addRemoveCategory(toDelete, superiorCategories);
                assignCategories.removePrincipalCategory(toDelete);
                for(PrincipalCategory pc: assignCategories.getPrincipalCategories()){
                    pc.getJuniorCategories().remove(toDelete);
                }
                updateGraph();
            }
        } //helloageag
    }

    public void updateHierarchy() {
        String text = JOptionPane.showInputDialog("Enter category name");
        if (text != null && !text.isBlank()) {
            PrincipalCategory principalCategory = HelperClass.getCategoryByName(assignCategories.getPrincipalCategories(), text);
            if (principalCategory != null) {
                JDialog dialog = new JDialog();
                JPanel mainPanel = new JPanel(new BorderLayout());
                mainPanel.setPreferredSize(new Dimension(500, 700));
                dialog.add(mainPanel);
                JPanel gridPanel = new JPanel(new GridLayout(assignCategories.getPrincipalCategories().size(), 1));
                Map<PrincipalCategory, JCheckBox> selectedCatMap = new HashMap<>();
                for(PrincipalCategory pc: assignCategories.getPrincipalCategories()){
                    if(!pc.equals(principalCategory)) {
                        boolean isJuniorOf = false;
                        String deleteThisString = "";
                        List<PrincipalCategory> allJunior = HelperClass.findAllJuniorCategories(pc);
                        if(allJunior.contains(principalCategory)){
                            isJuniorOf = true;
                            deleteThisString = pc.getName();
                        }
                        if(!isJuniorOf) {
                            JCheckBox curr = new JCheckBox(pc.getName());
                            selectedCatMap.put(pc, curr);
                            gridPanel.add(curr);
                        } else {
                            JLabel textLabel = new JLabel("<html>senior<br>category<br>" +deleteThisString + "</html>");
                            gridPanel.add(textLabel);
                        }
                    }
                }
                mainPanel.add(gridPanel, BorderLayout.CENTER);
                JButton submitButton = new JButton("Submit");
                mainPanel.add(submitButton, BorderLayout.NORTH);
                submitButton.addActionListener(e -> {
                    List<PrincipalCategory> oldJrCategories = new ArrayList<>(principalCategory.getJuniorCategories());
                    principalCategory.getJuniorCategories().clear();
                    for(PrincipalCategory pc: assignCategories.getPrincipalCategories()){
                        JCheckBox currBox = selectedCatMap.get(pc);
                        if(currBox != null){
                            if(currBox.isSelected()){
                                principalCategory.addJuniorCategory(pc);
                            } else {
                                principalCategory.removeJuniorCategory(pc);
                            }
                        }
                    }
                    undoClass.addUpdateHierarchy(principalCategory, oldJrCategories);
                    updateGraph();
                    dialog.dispose();
                });

                dialog.pack();
                dialog.setLocationRelativeTo(frame);
                dialog.setModal(true);
                dialog.setVisible(true);
            }
        }
    }

    public void configurePermissions() {
        String text = JOptionPane.showInputDialog("Enter category name");
        if (text != null && !text.isBlank()) {
            PrincipalCategory principalCategory = HelperClass.getCategoryByName(assignCategories.getPrincipalCategories(), text);
            if (principalCategory != null) {
                PrincipalRulePanel prp = new PrincipalRulePanel(assignCategories);
                Set<Action> juniorActions = new HashSet<>();
                for(PrincipalCategory jr: HelperClass.findAllJuniorCategories(principalCategory)){
                    if(!jr.equals(principalCategory)) {
                        juniorActions.addAll(jr.getActions());
                    }
                }
                for (Action a : juniorActions) {
                    JCheckBox curr = prp.getActionJCheckBoxMap().get(a);
                    if (curr != null) {
                        curr.setText("Inherited permission");
                    }
                }
                for(Action a :  principalCategory.getActions()){
                    JCheckBox curr = prp.getActionJCheckBoxMap().get(a);
                    if(curr != null){
                        if(juniorActions.contains(a)){
                            curr.setText("<html>Inherited permission<br>and explicitly assigned</html>");
                        } else {
                            curr.setText("Explicitly assigned");
                        }
                    }
                }
                JDialog dialog = new JDialog();
                JPanel tempPanel = new JPanel(new BorderLayout());
                JButton submitButton = new JButton("Submit");
                dialog.add(tempPanel);
                prp.getPanel().setPreferredSize(new Dimension(400, 400));
                tempPanel.add(prp.getPanel(), BorderLayout.CENTER);
                tempPanel.add(submitButton, BorderLayout.NORTH);
                submitButton.addActionListener(e -> {
                    List<Action> oldActionList = new ArrayList<>(principalCategory.getActions());
                    principalCategory.getActions().clear();
                    for (Action a : assignCategories.getResourceActions()) {
                        JCheckBox currCheckBox = prp.getActionJCheckBoxMap().get(a);
                        if (currCheckBox != null) {
                            if (currCheckBox.isSelected()) {
                                principalCategory.addAction(a);
                            } else {
                                principalCategory.removeAction(a);
                            }
                        }
                    }
                    undoClass.addUpdatePermissions(principalCategory, oldActionList);
                    updateGraph();
                    dialog.dispose();
                });

                dialog.pack();
                dialog.setLocationRelativeTo(frame);
                dialog.setModal(true);
                dialog.setResizable(true);
                dialog.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(frame, "not found");
            }
        }
    }

    public void addResource() {
        JDialog dialog = new JDialog();
        JPanel mainPanel = new JPanel(new BorderLayout());
        ResourceRulePanel rrp = new ResourceRulePanel();
        mainPanel.add(rrp.getPanel(), BorderLayout.CENTER);
        dialog.add(mainPanel);
        rrp.getPanel().setPreferredSize(new Dimension(500, 500));

        JButton submitButton = new JButton("Submit");
        mainPanel.add(submitButton, BorderLayout.NORTH);
        submitButton.addActionListener(e -> {
            boolean error = false;
            List<String> uniqueActionNames = new ArrayList<>();
            List<Action> resourceActions = new ArrayList<>();
            Resource resource = null;
            JTextField resourceName = rrp.getResourceNameField();
            if (resourceName.getText() != null && !resourceName.getText().isBlank() && HelperClass.getResourceByName(assignCategories.getResources(), resourceName.getText()) == null) {
                String fieldText = resourceName.getText().toLowerCase().strip();
                resource = new Resource(fieldText);

                // add to assigncat
                for (int i = 0; i < rrp.getTextFieldArray().length; i++) {
                    JTextField currField = rrp.getTextFieldArray()[i];
                    if (currField.getText() != null && !currField.getText().isBlank()) {
                        if (!uniqueActionNames.contains(currField.getText().toLowerCase().strip())) {
                            String currActionName = currField.getText().toLowerCase().strip();
                            uniqueActionNames.add(currField.getText().toLowerCase().strip());
                            resourceActions.add(new Action(currActionName, resource));
                        } else {
                            error = true;
                        }
                    }
                }
            } else {
                error = true;
            }
            if (!error) {
                undoClass.addAddResource(resource);
                assignCategories.addResource(resource);
                assignCategories.getResourceActions().addAll(resourceActions);
                updateGraph();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(frame, "error");
            }
        });

        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setModal(true);
        dialog.setResizable(true);
        dialog.setVisible(true);
    }

    public void removeResource() {
        String text = JOptionPane.showInputDialog("Enter resource name:");
        if (text != null && !text.isBlank()) {
            Resource toRemove = HelperClass.getResourceByName(assignCategories.getResources(), text);
            if (toRemove != null) {
                List<Action> resourceActions = HelperClass.getAllResourceActions(assignCategories.getResourceActions(), toRemove);
                Map<Action, List<PrincipalCategory>> actionCategoryNames = new HashMap<>();
                for(PrincipalCategory pc : assignCategories.getPrincipalCategories()){
                    List<Action> categoryActions = pc.getActions();
                    for(Action a : resourceActions){
                        if(categoryActions.contains(a)){
                            List<PrincipalCategory> categoryList;
                            if(actionCategoryNames.containsKey(a)){
                                categoryList = actionCategoryNames.get(a);
                            } else {
                                categoryList = new ArrayList<>();
                            }
                            categoryList.add(pc);
                            actionCategoryNames.put(a, categoryList);
                        }
                    }
                }
                undoClass.addRemoveResource(toRemove, actionCategoryNames);
                ADMIN_LOG.add("resource " + toRemove.getName() + " deleted "  + HelperClass.getCurrentTime());

                assignCategories.getPrincipalCategories().forEach(principalCategory ->  principalCategory.getActions().removeAll(resourceActions));
                assignCategories.getResourceActions().removeAll(resourceActions);
                assignCategories.getResources().remove(toRemove);
                updateGraph();
            } else {
                JOptionPane.showMessageDialog(frame, "Resource not found!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void addPrincipal() {
        String text = JOptionPane.showInputDialog("Enter principal name:");
        if (text != null && !text.isBlank()) {
            Principal currPrincipal = HelperClass.getPrincipalByName(assignCategories.getPrincipals(), text);
            boolean IS_NEW_PRINCIPAL = (currPrincipal == null);
            JDialog dialog = new JDialog();
            JPanel newPrincipalPanel = new JPanel(new BorderLayout());
            newPrincipalPanel.setPreferredSize(new Dimension(1000, 700));
            JButton submitButton = new JButton("Submit");
            newPrincipalPanel.add(submitButton, BorderLayout.SOUTH);

            RegistrationForm currForm = new RegistrationForm();
            JPanel firstPanel = currForm.getRightPanels()[0][0];
            for(Component c: firstPanel.getComponents()){
                if(c instanceof JTextField textField){
                    textField.setText(text.toLowerCase().strip()); // set first text field to input text for convenience
                    if(!IS_NEW_PRINCIPAL){
                        textField.setEnabled(false);
                    }
                }
            }
            JPanel regPanel = currForm.getPanel();
            newPrincipalPanel.add(regPanel, BorderLayout.CENTER);
            if(currPrincipal != null){
                newPrincipalPanel.add(new JLabel("Editing " + currPrincipal.getName() ), BorderLayout.NORTH);
            }

            dialog.add(newPrincipalPanel);

            submitButton.addActionListener(e1 -> {
                boolean isValid = validate(currForm, IS_NEW_PRINCIPAL);
                if (isValid) {
                    boolean error = false;
                    List<StringAttribute> stringAttributeList = new ArrayList<>();
                    List<IntegerAttribute> integerAttributeList = new ArrayList<>();
                    List<DateAttribute> dateAttributeList = new ArrayList<>();
                    for (int i = 0; i < currForm.getNUM_OF_ROWS(); i++) {
                        JPanel leftPanel = currForm.getLeftPanels()[i];
                        JCheckBox checkBox = HelperClass.getCheckBoxFromComponents(leftPanel.getComponents());
                        JComboBox<?> comboBox = HelperClass.getComboBoxFromComponents(leftPanel.getComponents());
                        JTextField textField = HelperClass.getTextFieldFromComponents(leftPanel.getComponents());
                        if (checkBox != null && comboBox != null && textField != null) {
                            int selectedIndex = comboBox.getSelectedIndex();
                            JPanel rightPanel = currForm.getRightPanels()[i][selectedIndex];
                            if (checkBox.isSelected()) {
                                String attributeName = textField.getText().toLowerCase().strip();
                                String attributeValue = HelperClass.getRightPanelAttributeValue(rightPanel);
                                error = HelperClass.addCorrespondingAttribute(stringAttributeList, integerAttributeList, dateAttributeList,
                                        selectedIndex, attributeName, attributeValue);
                            }
                        }
                    }

                    String name = stringAttributeList.get(0).getValue();
                    Principal principal = null;
                    if(currPrincipal != null){
                        principal = currPrincipal;
                        principal.setName(name);
                    } else {
                        principal = new Principal(name);
                    }

                    if (!error) {
                        ADMIN_LOG.add("Principal " + principal.getName() + " created "  + HelperClass.getCurrentTime());
                        Principal oldPrincipal = new Principal(principal);
                        principal.setStringAttributeList(stringAttributeList);
                        principal.setIntegerAttributeList(integerAttributeList);
                        principal.setDateAttributeList(dateAttributeList);
                        if(IS_NEW_PRINCIPAL) {
                            undoClass.addCreatePrincipal(principal);
                            assignCategories.addPrincipal(principal);
                        } else {
                            undoClass.addUpdatePrincipal(oldPrincipal, principal);
                        }
                        assignCategories.evaluatePrincipalCategories();
                        updateGraph();
                        dialog.dispose();
                    } else {
                        JOptionPane.showMessageDialog(frame, "Error parsing");
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Invalid");
                }
            });

            dialog.pack();
            dialog.setLocationRelativeTo(frame);
            dialog.setModal(true);
            dialog.setResizable(true);
            dialog.setVisible(true);
        }
    }

    public void removePrincipal() {
        String text = JOptionPane.showInputDialog("Enter principal name:");
        if (text != null && !text.isBlank()) {
            Principal toRemove = HelperClass.getPrincipalByName(assignCategories.getPrincipals(), text);
            if (toRemove != null) {
                undoClass.addRemovePrincipal(toRemove);
                ADMIN_LOG.add("Principal " + toRemove.getName() + " deleted "  + HelperClass.getCurrentTime());

                assignCategories.removePrincipal(toRemove);
                for(PrincipalCategory pc: assignCategories.getPrincipalCategories()){
                    pc.getPrincipals().remove(toRemove);
                }
//                assignCategories.evaluatePrincipalCategories();
                updateGraph();
            } else {
                JOptionPane.showMessageDialog(frame, "Principal not found!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void updateCategoryRules() {
        String text = JOptionPane.showInputDialog("Enter principal category name:");
        if (text != null && !text.isBlank()) {
            PrincipalCategory currCat = HelperClass.getCategoryByName(assignCategories.getPrincipalCategories(), text);
            boolean IS_NEW_CATEGORY = (currCat == null);

            List<String> principalsBefore = new ArrayList<>();
            if(currCat != null){
                for(Principal p : currCat.getPrincipals()){
                    principalsBefore.add(p.getName());
                }
            }
            List<String> principalsAfter = new ArrayList<>();

            JDialog dialog = new JDialog();
            JPanel tempPanel = new JPanel(new BorderLayout());
            tempPanel.setPreferredSize(new Dimension(1000, 700));

            JButton submitButton = new JButton("Submit");
            tempPanel.add(submitButton, BorderLayout.SOUTH);
            List<StringAttribute> bl1 = HelperClass.getBlankStringAttributeList(assignCategories.getPrincipals());
            List<IntegerAttribute> bl2 = HelperClass.getBlankIntegerAttributeList(assignCategories.getPrincipals());
            List<DateAttribute> bl3 = HelperClass.getBlankDateAttributeList(assignCategories.getPrincipals());

            CategoryRulePanel currRulePanel = new CategoryRulePanel(bl1, bl2, bl3);
            JPanel cPanel = currRulePanel.getPanel();
            tempPanel.add(cPanel, BorderLayout.CENTER);

            String updatedName = (currCat != null) ? currCat.getName() : text.toLowerCase().strip();
            currRulePanel.getCategoryNameField().setText(updatedName);
            if(!IS_NEW_CATEGORY) {
                currRulePanel.getCategoryNameField().setEnabled(false);
            }

            String infoLabelText = (currCat != null) ? "Modifying category " + currCat.getName() : "Creating new category";
            tempPanel.add(new JLabel(infoLabelText), BorderLayout.NORTH);

            submitButton.addActionListener(al -> {
                PrincipalCategory currModifiedCategory = currCat;

                boolean error = false;
                PrincipalCategory oldCategory = (!IS_NEW_CATEGORY) ? new PrincipalCategory(currModifiedCategory) : null;
                if (currRulePanel.getCategoryNameField().getText() != null) {
                    String proposedName = currRulePanel.getCategoryNameField().getText().strip().toLowerCase();
                    if (!proposedName.isBlank()) {
                        if ((currModifiedCategory != null && proposedName.strip().equalsIgnoreCase(currModifiedCategory.getName().strip())) ||
                                HelperClass.getCategoryByName(assignCategories.getPrincipalCategories(), proposedName) == null) {
                            if (IS_NEW_CATEGORY) {
                                currModifiedCategory = new PrincipalCategory(proposedName);
                            } else {
                                currModifiedCategory.setName(proposedName);
                            }
                            currModifiedCategory.getStringRules().clear();
                            currModifiedCategory.getIntegerRules().clear();
                            currModifiedCategory.getDateRules().clear();
                            for (int i = 0; i < currRulePanel.getPanelArray().length; i++) {
                                JCheckBox currentRowBox = currRulePanel.getIsEnabledMap().get(i);
                                if (currentRowBox != null && currentRowBox.isSelected()) {
                                    if (i < bl1.size()) {
                                        StringAttribute currAttribute = bl1.get(i);
                                        error = error || HelperClass.evaluateStringAttr(currAttribute, currRulePanel, currModifiedCategory);
                                    } else {
                                        int fixedIndex = i - bl1.size();
                                        if (fixedIndex < bl2.size()) {
                                            IntegerAttribute currAttribute = bl2.get(fixedIndex);
                                            error = error || HelperClass.evaluateIntegerAttr(currAttribute, currRulePanel, currModifiedCategory);
                                        } else {
                                            int finalFixedIndex = i - bl1.size() - bl2.size();
                                            if(finalFixedIndex < bl3.size()) {
                                                DateAttribute currAttribute = bl3.get(finalFixedIndex);
                                                error = error || HelperClass.evaluateDateAttr(currAttribute, currRulePanel, currModifiedCategory);
                                            }
                                        }
                                    }
                                }
                            }
                            if (!error) {

                                currModifiedCategory.getPrincipals().clear();
                                if(IS_NEW_CATEGORY) {
                                    assignCategories.addPrincipalCategory(currModifiedCategory);
                                    undoClass.addCreateCategory(currModifiedCategory);
                                } else {
                                    undoClass.addUpdateCategory(oldCategory, currModifiedCategory);
                                }
                                assignCategories.evaluatePrincipalCategories();
                                for(Principal prin : currModifiedCategory.getPrincipals()){
                                    principalsAfter.add(prin.getName());
                                }

                                String currentLogAction = (IS_NEW_CATEGORY) ? "Created new category " + currModifiedCategory.getName() : " Updated category " + proposedName;
                                ADMIN_LOG.add(currentLogAction + " " + HelperClass.getCurrentTime());
                                HelperClass.showDoubleDialog(frame, "Category " + currModifiedCategory.getName(), principalsBefore, principalsAfter, "Principals before", "Principals after");

                                updateGraph();
                                dialog.dispose();
                            } else {
                                JOptionPane.showMessageDialog(frame, "Error parsing");
                            }
                        } else {
                            JOptionPane.showMessageDialog(frame, "attempting to overwrite existing.. not possible");
                        }
                    }
                }
            });
            dialog.add(tempPanel);
            dialog.pack();
            dialog.setLocationRelativeTo(frame);
            dialog.setModal(true);
            dialog.setResizable(true);
            dialog.setVisible(true);
        }
    }

    public boolean isRedundant_PC_Edge(Principal principal, PrincipalCategory category, List<Object> graphNodes, boolean[][] adjacencyMatrix) {
        List<PrincipalCategory> superiorCategories = new ArrayList<>();
        for(PrincipalCategory pc: assignCategories.getPrincipalCategories()){
            if(!pc.equals(category) && HelperClass.findAllJuniorCategories(pc).contains(category)){
                superiorCategories.add(pc);
            }
        }
        int principalIndex = HelperClass.getPrincipalIndex(graphNodes, principal);
        for (PrincipalCategory superiorCategory : superiorCategories) {
            int superiorCategoryIndex = HelperClass.getCategoryIndex(graphNodes, superiorCategory);
            if (!superiorCategory.equals(category) && adjacencyMatrix[principalIndex][superiorCategoryIndex]) {
                return true;
            }
        }
        return false;
    }

    public boolean isRedundant_CC_Edge(PrincipalCategory category1, PrincipalCategory category2, List<Object> graphNodes, boolean[][] adjacencyMatrix) {
        int category1Index = HelperClass.getCategoryIndex(graphNodes, category1);

        List<PrincipalCategory> superiorCategories = new ArrayList<>();
        for(PrincipalCategory pc: assignCategories.getPrincipalCategories()){
            if(!pc.equals(category2) && HelperClass.findAllJuniorCategories(pc).contains(category2)){
                superiorCategories.add(pc);
            }
        }
        for (PrincipalCategory superiorCategory : superiorCategories) {
            int superiorCategoryIndex = HelperClass.getCategoryIndex(graphNodes, superiorCategory);
            if (!superiorCategory.equals(category2) && adjacencyMatrix[category1Index][superiorCategoryIndex]) {
                return true;
            }
        }
        return false;
    }

    public boolean isRedundant_CA_Edge(PrincipalCategory category, Action action, List<Object> graphNodes, boolean[][] adjacencyMatrix) {
        int actionIndex = HelperClass.getActionIndex(graphNodes, action);

        List<PrincipalCategory> juniorCategories = HelperClass.findAllJuniorCategories(category);
        for (PrincipalCategory juniorCategory : juniorCategories) {
            int juniorCategoryIndex = HelperClass.getCategoryIndex(graphNodes, juniorCategory);
            if (!juniorCategory.equals(category) && adjacencyMatrix[juniorCategoryIndex][actionIndex]) {
                return true;
            }
        }
        return false;
    }

    public void updateGraph() {
        SwingUtilities.invokeLater(() -> {
            // Clear the existing graph
            graph.clear();
            List<Object> graphNodes = new ArrayList<>();

            for (Principal p : assignCategories.getPrincipals()) {
                addNode("Principal: " + p.getName(), "grey");
                graphNodes.add(p);
            }
            for (PrincipalCategory p : assignCategories.getPrincipalCategories()) {
                addNode("Principal category: " + p.getName(), "green");
                graphNodes.add(p);
            }
            for (Action a : assignCategories.getResourceActions()) {
                addNode("Action: " + a.getName() + " " + a.getResource().getName(), "pink");
                graphNodes.add(a);
            }
            for(Resource r: assignCategories.getResources()){
                addNode("Resource: " + r.getName(), "red");
            }

            // Create the adjacency matrix
            boolean[][] adjacencyMatrix = new boolean[graphNodes.size()][graphNodes.size()];
            for(int i = 0; i < adjacencyMatrix.length; i++){
                for (int j = 0; j < adjacencyMatrix[i].length; j++) {
                    Object firstNode = graphNodes.get(i);
                    Object secondNode = graphNodes.get(j);

                    // Check if there is an edge between the two nodes
                    boolean hasEdge = false;
                    if (firstNode instanceof Principal && secondNode instanceof PrincipalCategory) {
                        hasEdge = HelperClass.getPrincipalByName(((PrincipalCategory) secondNode).getPrincipals(), ((Principal) firstNode).getName()) != null;
                    } else if (firstNode instanceof PrincipalCategory && secondNode instanceof Principal) {
                        hasEdge = HelperClass.getPrincipalByName(((PrincipalCategory) firstNode).getPrincipals(), ((Principal) secondNode).getName()) != null;
                    } else if (firstNode instanceof PrincipalCategory && secondNode instanceof PrincipalCategory) {
                        hasEdge = HelperClass.getCategoryByName(((PrincipalCategory) firstNode).getJuniorCategories(), ((PrincipalCategory) secondNode).getName()) != null;
                    } else if (firstNode instanceof PrincipalCategory && secondNode instanceof Action) {
                        hasEdge = HelperClass.getActionByName(((PrincipalCategory) firstNode).getActions(), ((Action) secondNode).getName(), ((Action) secondNode).getResource().getName()) != null;
                    }
                    // Store the relationship between the two nodes in the matrix
                    adjacencyMatrix[i][j] = hasEdge;
                }
            }


            for (PrincipalCategory p : assignCategories.getPrincipalCategories()) {
                String id1 = "Principal category: " + p.getName();
                for (Principal pr : p.getPrincipals()) {
                    String id2 = "Principal: " + pr.getName();
                    String edgeId = id1 + id2;

                    // Check if the edge is redundant
                    if (!isRedundant_PC_Edge(pr, p, graphNodes, adjacencyMatrix) && graph.getEdge(edgeId) == null) {
                        graph.addEdge(edgeId, id1, id2);
                    }
                }
            }

            for (Action a : assignCategories.getResourceActions()) {
                String id1 = "Action: " + a.getName() + " " + a.getResource().getName();
                String id2 = "Resource: " + a.getResource().getName();
                String edgeId = id1 + id2;
                if (graph.getEdge(edgeId) == null) {
                    graph.addEdge(edgeId, id1, id2);
                }
            }

            for (PrincipalCategory p : assignCategories.getPrincipalCategories()) {
                String id1 = "Principal category: " + p.getName();
                for (Action a : p.getActions()) {
                    String id2 = "Action: " + a.getName() + " " + a.getResource().getName();
                    String edgeId = id1 + id2;

                    // Check if the edge is redundant
                    if (!isRedundant_CA_Edge(p, a, graphNodes, adjacencyMatrix) && graph.getEdge(edgeId) == null) {
                        graph.addEdge(edgeId, id1, id2);
                    }
                }
            }

            for (PrincipalCategory pc : assignCategories.getPrincipalCategories()) {
                String id1 = "Principal category: " + pc.getName();
                for (PrincipalCategory juniorPC : pc.getJuniorCategories()) {
                    String id2 = "Principal category: " + juniorPC.getName();
                    String edgeId = id1 + id2;

                    // Check if the edge is redundant
                    if (!isRedundant_CC_Edge(pc, juniorPC, graphNodes, adjacencyMatrix) && graph.getEdge(edgeId) == null) {
                        Edge edge = graph.addEdge(edgeId, id1, id2, true);
                        edge.setAttribute("ui.style", "arrow-shape: arrow; arrow-size: 10px;");
                    }
                }
            }
        });
        centerGraph();
    }

    public boolean validate(RegistrationForm currForm, boolean IS_NEW_PRINCIPAL) {
        boolean isValid = true;
        String principalName = "";
        List<String> attributeNameList = new ArrayList<>();
        for (int i = 0; i < currForm.getNUM_OF_ROWS(); i++) {
            JPanel leftPanel = currForm.getLeftPanels()[i];
            JCheckBox checkBox = HelperClass.getCheckBoxFromComponents(leftPanel.getComponents());
            JComboBox<?> comboBox = HelperClass.getComboBoxFromComponents(leftPanel.getComponents());
            JTextField textField = HelperClass.getTextFieldFromComponents(leftPanel.getComponents());
            if (checkBox != null && comboBox != null && textField != null) {
                int selectedIndex = comboBox.getSelectedIndex();
                JPanel rightPanel = currForm.getRightPanels()[i][selectedIndex];
                if (checkBox.isSelected()) {
                    if (textField.getText() != null && !textField.getText().isBlank() && !(attributeNameList.contains(textField.getText().toLowerCase().strip()))) {
                        String attributeName = textField.getText().toLowerCase().strip();
                        for (Component c : rightPanel.getComponents()) {
                            if (c instanceof JTextField) {
                                if (((JTextField) c).getText() == null || ((JTextField) c).getText().isBlank()) {
                                    isValid = false;
                                } else if (i == 0) {
                                    principalName = ((JTextField) c).getText().toLowerCase().strip();
                                }
                            }
                        }
                        attributeNameList.add(attributeName);
                    } else {
                        isValid = false;
                    }
                }
            } else {
                isValid = false;
            }
        }
        if(IS_NEW_PRINCIPAL && HelperClass.getPrincipalByName(assignCategories.getPrincipals(), principalName) != null){
            isValid = false; // principal exists
        }
        return isValid;
    }

    private void addNode(String nodeName, String color) {
        if (graph.getNode(nodeName) == null) {
            Node n = graph.addNode(nodeName);
            n.setAttribute("ui.style", "shape: circle;");
            n.setAttribute("ui.style", "size: 40px,25px;");
            n.setAttribute("ui.style", "fill-color: " + color + ";");
            n.setAttribute("ui.style", "text-size: 16px;");
            String nodeLabel = nodeName;
            if(nodeLabel.startsWith("Principal category")){
                nodeLabel = nodeLabel.replace("Principal category: ", "");
            } else if(nodeLabel.startsWith("Resource")){
                nodeLabel = nodeLabel.replace("Resource: ", "");
            } else if(nodeLabel.startsWith("Principal")){
                nodeLabel = nodeLabel.replace("Principal: ", "");
            } else if(nodeLabel.startsWith("Action")){
                String myString = nodeLabel.replace("Action: ", "");
                String[] words = myString.split("\\s"); // split the string by whitespace
                if(words.length >= 1 ) {
                    nodeLabel = words[0];
                }
            }
            n.setAttribute("ui.label", nodeLabel);
            n.setAttribute("layout.weight", 8.0);
        }
    }
}