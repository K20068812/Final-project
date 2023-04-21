import categories.*;
import categories.ResourceAction;
import guipanels.HelperClass;
import guipanels.NumericTextField;
import guipanels.RegistrationForm;
import org.junit.jupiter.api.BeforeEach;
import principal_resource_attributes.*;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HelperClassTest {

    private List<Object> graphNodes;
    private Principal principal1, principal2;
    private PrincipalCategory category1, category2;
    private ResourceAction action1, action2;
    private Resource resource1, resource2;

    @BeforeEach
    public void setUp() {
        graphNodes = new ArrayList<>();
        principal1 = new Principal("Alice");
        principal2 = new Principal("Bob");
        category1 = new PrincipalCategory("Admins");
        category2 = new PrincipalCategory("Users");
        resource1 = new Resource("Book");
        resource2 = new Resource("program");
        action1 = new ResourceAction("read", resource1);
        action2 = new ResourceAction("write", resource2);

        graphNodes.add(principal1);
        graphNodes.add(category1);
        graphNodes.add(principal2);
        graphNodes.add(category2);
        graphNodes.add(action1);
        graphNodes.add(action2);
    }

    @Test
    public void testGetPrincipalIndex() {
        assertEquals(0, HelperClass.getPrincipalIndex(graphNodes, principal1));
        assertEquals(2, HelperClass.getPrincipalIndex(graphNodes, principal2));
        assertEquals(-1, HelperClass.getPrincipalIndex(graphNodes, new Principal("Charlie")));
    }

    @Test
    public void testGetCategoryIndex() {
        assertEquals(1, HelperClass.getCategoryIndex(graphNodes, category1));
        assertEquals(3, HelperClass.getCategoryIndex(graphNodes, category2));
        assertEquals(-1, HelperClass.getCategoryIndex(graphNodes, new PrincipalCategory("Guests")));
    }

    @Test
    public void testGetActionIndex() {
        assertEquals(4, HelperClass.getActionIndex(graphNodes, action1));
        assertEquals(5, HelperClass.getActionIndex(graphNodes, action2));
        assertEquals(-1, HelperClass.getActionIndex(graphNodes, new ResourceAction("execute", resource2)));
    }

    @Test
    public void testGetLeftComponents(){
        RegistrationForm r = new RegistrationForm();
        List<JCheckBox> checkBoxes = new ArrayList<>();
        List<JComboBox<?>> comboBoxes = new ArrayList<>();
        List<JTextField> textFields = new ArrayList<>();
        for(int i = 0; i<r.getLeftPanels().length; i++){
            JPanel leftPanel = r.getLeftPanels()[i];
            checkBoxes.add(HelperClass.getCheckBoxFromComponents(leftPanel.getComponents()));
            comboBoxes.add(HelperClass.getComboBoxFromComponents(leftPanel.getComponents()));
            textFields.add(HelperClass.getTextFieldFromComponents(leftPanel.getComponents()));
        }
        assertEquals(6, checkBoxes.size()); // 6 ATTRIBUTES ALLOWED PER PRINCIPAL, INCLUDING NAME (COMPULSORY)
        assertEquals(6, comboBoxes.size());
        assertEquals(6, textFields.size());
    }

    @Test
    public void testFillFormWithData() {
        RegistrationForm registrationForm = new RegistrationForm();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        int[] attributeTypes = {0, 1, 2}; // 0 for String, 1 for Integer, 2 for Date

        for (int i = 0; i < registrationForm.getNUM_OF_ROWS(); i++) {
            JCheckBox enabledCheckbox = (JCheckBox) registrationForm.getLeftPanels()[i].getComponent(2);
            if (!enabledCheckbox.isSelected()) {
                enabledCheckbox.doClick();
            }

            JComboBox<String> attributeTypeComboBox = (JComboBox<String>) registrationForm.getLeftPanels()[i].getComponent(0);
            int attributeTypeIndex = attributeTypes[Math.min(i, attributeTypes.length - 1)];
            attributeTypeComboBox.setSelectedIndex(attributeTypeIndex);

            JPanel rightPanel = registrationForm.getRightPanels()[i][attributeTypeIndex];
            Component inputComponent = rightPanel.getComponent(0);

            if (inputComponent instanceof NumericTextField numericTextField ) {
                numericTextField.setText(String.valueOf(i * 100));
            } else if (inputComponent instanceof JTextField textField) {
                textField.setText("Sample Text " + (i + 1));
            } else {
                continue;
            }

            String selectedItem = String.valueOf(attributeTypeComboBox.getSelectedItem());
            if (selectedItem.equals("Date attribute")) {
                JTextField dateField = (JTextField) registrationForm.getRightPanels()[i][2].getComponent(0);
                dateField.setText(dateFormatter.format(LocalDate.now()));
            }
        }

        // You can add assertions here to verify the form data is filled correctly.
        // Example:
        for (int i = 0; i < registrationForm.getNUM_OF_ROWS(); i++) {
            JComboBox<String> attributeTypeComboBox = (JComboBox<String>) registrationForm.getLeftPanels()[i].getComponent(0);
            String selectedItem = String.valueOf(attributeTypeComboBox.getSelectedItem());

            JPanel rightPanel = registrationForm.getRightPanels()[i][attributeTypeComboBox.getSelectedIndex()];
            Component inputComponent = rightPanel.getComponent(0);

            if (inputComponent instanceof NumericTextField numericTextField) {
                assertTrue(numericTextField.getText().matches("\\d+"));
            } else if (inputComponent instanceof JTextField textField ) {
                assertTrue(textField.getText().startsWith("Sample Text") || textField.getText().matches("\\d{4}-\\d{2}-\\d{2}"));
            }
        }
    }


    @Test
    public void testAddCorrespondingAttribute() {
        List<StringAttribute> stringAttributeList = new ArrayList<>();
        List<IntegerAttribute> integerAttributeList = new ArrayList<>();
        List<DateAttribute> dateAttributeList = new ArrayList<>();

        // Test adding a string attribute
        boolean errorParsing = HelperClass.addCorrespondingAttribute(stringAttributeList, integerAttributeList, dateAttributeList, 0, "Attribute 1", "Value 1");
        assertFalse(errorParsing);
        assertEquals(stringAttributeList.size(), 1);
        assertEquals(stringAttributeList.get(0).getName(), "attribute 1"); // AS THIS APPLICATION DOES NOT USE UPPERCASE
        assertEquals(stringAttributeList.get(0).getValue(), "value 1");

        // Test adding an integer attribute
        errorParsing = HelperClass.addCorrespondingAttribute(stringAttributeList, integerAttributeList, dateAttributeList, 1, "Attribute 2", "123");
        assertFalse(errorParsing);
        assertEquals(integerAttributeList.size(), 1);
        assertEquals(integerAttributeList.get(0).getName(), "attribute 2".toLowerCase());
        assertEquals(integerAttributeList.get(0).getValue(), 123);

        // Test adding a date attribute
        errorParsing = HelperClass.addCorrespondingAttribute(stringAttributeList, integerAttributeList, dateAttributeList, 2, "Attribute 3", "2022-01-01");
        assertFalse(errorParsing);
        assertEquals(dateAttributeList.size(), 1);
        assertEquals(dateAttributeList.get(0).getName(), "attribute 3".toLowerCase());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        assertEquals(dateFormat.format(dateAttributeList.get(0).getValue()), "2022-01-01");
    }

    @Test
    public void testGetActionByName() {
        List<ResourceAction> actionList = new ArrayList<>();
        Resource resource1 = new Resource("Resource 1");
        Resource resource2 = new Resource("Resource 2");
        actionList.add(new ResourceAction("Action 1", resource1));
        actionList.add(new ResourceAction("Action 2", resource1));
        actionList.add(new ResourceAction("Action 1", resource2));
        actionList.add(new ResourceAction("Action 2", resource2));

        // Test getting an existing action
        ResourceAction action = HelperClass.getActionByName(actionList, "Action 1", "Resource 1");
        assertNotNull(action);
        assertEquals(action.getName(), "Action 1");
        assertEquals(action.getResource().getName(), "Resource 1");

        // Test getting a non-existing action
        action = HelperClass.getActionByName(actionList, "Action 3", "Resource 3");
        assertNull(action);
    }

    @Test
    public void testGetCategoryByName() {
        List<PrincipalCategory> categoryList = new ArrayList<>();
        categoryList.add(new PrincipalCategory("Category 1"));
        categoryList.add(new PrincipalCategory("Category 2"));

        // Test getting an existing category
        PrincipalCategory category = HelperClass.getCategoryByName(categoryList, "Category 1");
        assertNotNull(category);
        assertEquals(category.getName(), "Category 1");

        // Test getting a non-existing category
        category = HelperClass.getCategoryByName(categoryList, "Category 3");
        assertNull(category);
    }

    @Test
    public void testGetResourceByName() {
        List<Resource> resourceList = new ArrayList<>();
        resourceList.add(new Resource("Resource 1"));
        resourceList.add(new Resource("Resource 2"));
        // Test getting an existing resource
        Resource resource = HelperClass.getResourceByName(resourceList, "Resource 1");
        assertNotNull(resource);
        assertEquals(resource.getName(), "Resource 1");

// Test getting a non-existing resource
        resource = HelperClass.getResourceByName(resourceList, "Resource 3");
        assertNull(resource);
    }

    @Test
    public void testGetPrincipalByName() {
        List<Principal> principalList = new ArrayList<>();
        principalList.add(new Principal("Principal 1"));
        principalList.add(new Principal("Principal 2"));
        // Test getting an existing principal
        Principal principal = HelperClass.getPrincipalByName(principalList, "Principal 1");
        assertNotNull(principal);
        assertEquals(principal.getName(), "Principal 1");

// Test getting a non-existing principal
        principal = HelperClass.getPrincipalByName(principalList, "Principal 3");
        assertNull(principal);
    }

    @Test
    public void testFindGridSize() {
// Test with n = 0
        int[] gridSize = HelperClass.findGridSize(0);
        assertNotNull(gridSize);
        assertEquals(gridSize[0], 0);
        assertEquals(gridSize[1], 0);
        // Test with n = 1
        gridSize = HelperClass.findGridSize(1);
        assertNotNull(gridSize);
        assertEquals(gridSize[0], 1);
        assertEquals(gridSize[1], 1);

// Test with n = 4
        gridSize = HelperClass.findGridSize(4);
        assertNotNull(gridSize);
        assertEquals(gridSize[0], 2);
        assertEquals(gridSize[1], 2);

// Test with n = 5
        gridSize = HelperClass.findGridSize(5);
        assertNotNull(gridSize);
        assertEquals(gridSize[0], 3);
        assertEquals(gridSize[1], 2);
    }

    @Test
    public void testGetBlankStringAttributeList2() {
// Create principal list with attributes
        Principal principal1 = new Principal("Principal 1");
        principal1.getStringAttributeList().add(new StringAttribute("Attribute 1", ""));
        principal1.getStringAttributeList().add(new StringAttribute("Attribute 2", ""));
        Principal principal2 = new Principal("Principal 2");
        principal2.getStringAttributeList().add(new StringAttribute("Attribute 1", ""));
        principal2.getStringAttributeList().add(new StringAttribute("Attribute 3", ""));
        List<Principal> principalList = new ArrayList<>();
        principalList.add(principal1);
        principalList.add(principal2);
        // Test getting blank string attribute list
        List<StringAttribute> blankStringAttributeList = HelperClass.getBlankStringAttributeList(principalList);
        assertNotNull(blankStringAttributeList);
        assertEquals(blankStringAttributeList.size(), 3);
        assertTrue(blankStringAttributeList.contains(new StringAttribute("Attribute 1", "")));
        assertTrue(blankStringAttributeList.contains(new StringAttribute("Attribute 2", "")));
        assertTrue(blankStringAttributeList.contains(new StringAttribute("Attribute 3", "")));
    }
        @Test
        public void testGetResourceByName2() {
            List<Resource> resourceList = new ArrayList<>();
            resourceList.add(new Resource("Resource 1"));
            resourceList.add(new Resource("Resource 2"));
// Test getting an existing resource
            Resource resource = HelperClass.getResourceByName(resourceList, "Resource 1");
            assertNotNull(resource);
            assertEquals(resource.getName(), "Resource 1");

// Test getting a non-existing resource
            resource = HelperClass.getResourceByName(resourceList, "Resource 3");
            assertNull(resource);
        }

        @Test
        public void testGetPrincipalByName2() {
            List<Principal> principalList = new ArrayList<>();
            principalList.add(new Principal("Principal 1"));
            principalList.add(new Principal("Principal 2"));

// Test getting an existing principal
            Principal principal = HelperClass.getPrincipalByName(principalList, "Principal 1");
            assertNotNull(principal);
            assertEquals(principal.getName(), "Principal 1");

// Test getting a non-existing principal
            principal = HelperClass.getPrincipalByName(principalList, "Principal 3");
            assertNull(principal);
        }

        @Test
        public void testFindGridSize2() {
// Test with n = 0
            int[] gridSize = HelperClass.findGridSize(0);
            assertNotNull(gridSize);
            assertEquals(gridSize[0], 0);
            assertEquals(gridSize[1], 0);
// Test with n = 1
            gridSize = HelperClass.findGridSize(1);
            assertNotNull(gridSize);
            assertEquals(gridSize[0], 1);
            assertEquals(gridSize[1], 1);

// Test with n = 4
            gridSize = HelperClass.findGridSize(4);
            assertNotNull(gridSize);
            assertEquals(gridSize[0], 2);
            assertEquals(gridSize[1], 2);

// Test with n = 5
            gridSize = HelperClass.findGridSize(5);
            assertNotNull(gridSize);
            assertEquals(gridSize[0], 3);
            assertEquals(gridSize[1], 2);
        }

        @Test
        public void testGetBlankStringAttributeList() {
// Create principal list with attributes
            Principal principal1 = new Principal("Principal 1");
            principal1.getStringAttributeList().add(new StringAttribute("Attribute 1", ""));
            principal1.getStringAttributeList().add(new StringAttribute("Attribute 2", ""));
            Principal principal2 = new Principal("Principal 2");
            principal2.getStringAttributeList().add(new StringAttribute("Attribute 1", ""));
            principal2.getStringAttributeList().add(new StringAttribute("Attribute 3", ""));
            List<Principal> principalList = new ArrayList<>();
            principalList.add(principal1);
            principalList.add(principal2);
// Test getting blank string attribute list
            List<StringAttribute> blankStringAttributeList = HelperClass.getBlankStringAttributeList(principalList);
            assertNotNull(blankStringAttributeList);
            assertEquals(blankStringAttributeList.size(), 3);
            assertTrue(blankStringAttributeList.contains(new StringAttribute("Attribute 1", "")));
            assertTrue(blankStringAttributeList.contains(new StringAttribute("Attribute 2", "")));
            assertTrue(blankStringAttributeList.contains(new StringAttribute("Attribute 3", "")));
        }

        @Test
        public void testGetBlankIntegerAttributeList() {
// Create principal list with attributes
            Principal principal1 = new Principal("Principal 1");
            principal1.getIntegerAttributeList().add(new IntegerAttribute("Attribute 1", 0));
            principal1.getIntegerAttributeList().add(new IntegerAttribute("Attribute 2", 0));
            Principal principal2 = new Principal("Principal 2");
            principal2.getIntegerAttributeList().add(new IntegerAttribute("Attribute 1", 0));
            principal2.getIntegerAttributeList().add(new IntegerAttribute("Attribute 3", 0));
            List<Principal> principalList = new ArrayList<>();
            principalList.add(principal1);
            principalList.add(principal2);
            // Test getting blank integer attribute list
            List<IntegerAttribute> blankIntegerAttributeList = HelperClass.getBlankIntegerAttributeList(principalList);
            assertNotNull(blankIntegerAttributeList);
            assertEquals(blankIntegerAttributeList.size(), 3);
            assertTrue(blankIntegerAttributeList.contains(new IntegerAttribute("Attribute 1", 0)));
            assertTrue(blankIntegerAttributeList.contains(new IntegerAttribute("Attribute 2", 0)));
            assertTrue(blankIntegerAttributeList.contains(new IntegerAttribute("Attribute 3", 0)));
        }

        @Test
        public void testGetBlankDateAttributeList() {
// Create principal list with attributes
            Principal principal1 = new Principal("Principal 1");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = new Date();
            principal1.getDateAttributeList().add(new DateAttribute("Attribute 1", date));
            principal1.getDateAttributeList().add(new DateAttribute("Attribute 2", date));
            Principal principal2 = new Principal("Principal 2");
            principal2.getDateAttributeList().add(new DateAttribute("Attribute 1", date));
            principal2.getDateAttributeList().add(new DateAttribute("Attribute 3", date));
            List<Principal> principalList = new ArrayList<>();
            principalList.add(principal1);
            principalList.add(principal2);

// Test getting blank date attribute list
            List<DateAttribute> blankDateAttributeList = HelperClass.getBlankDateAttributeList(principalList);
            assertNotNull(blankDateAttributeList);
            assertEquals(blankDateAttributeList.size(), 3);
            assertTrue(blankDateAttributeList.contains(new DateAttribute("Attribute 1", new Date())));
            assertTrue(blankDateAttributeList.contains(new DateAttribute("Attribute 2", new Date())));
            assertTrue(blankDateAttributeList.contains(new DateAttribute("Attribute 3", new Date())));
        }


        }