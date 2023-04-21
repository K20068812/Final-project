package database;

import categories.*;
import categoryrules.DateRule;
import categoryrules.IntegerRule;
import categoryrules.StringRule;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import principal_resource_attributes.DateAttribute;
import principal_resource_attributes.IntegerAttribute;
import principal_resource_attributes.StringAttribute;

import java.util.*;


public class MongoMain {
    private static final String DB_NAME = "MongoDB";
    private MongoClient mongoClient;
    private MongoDatabase database;

    public MongoMain(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
        database = mongoClient.getDatabase(DB_NAME);
    }

    public void createCollectionIfNotExists(String collectionName) {
        boolean collectionExists = database.listCollectionNames()
                .into(new ArrayList<>())
                .stream()
                .anyMatch(name -> name.equalsIgnoreCase(collectionName));

        if (!collectionExists) {
            database.createCollection(collectionName);
        }
    }

    public boolean databaseEmpty() {
        String[] collectionNames = {"principalCategories", "principals", "actions", "resources", "undoClass"};
        boolean isEmpty = true;
        for (String s : collectionNames) {
            boolean collectionExists = database.listCollectionNames()
                    .into(new ArrayList<>())
                    .stream()
                    .anyMatch(name -> name.equalsIgnoreCase(s));
            if (collectionExists && database.getCollection(s).countDocuments() > 0) {
                isEmpty = false;
            }
        }
        return isEmpty;
    }

    public void saveUndoClass(UndoClass undoClass){
        MongoCollection<Document> undoClassCollection = database.getCollection("undoClass");
        List<Document> docs = new ArrayList<>();
        for(List<Object> currEntry : undoClass.getActionTracker()){
            UndoClass.UNDO_TYPE actionType = (UndoClass.UNDO_TYPE) currEntry.get(0);
            Document type = new Document("type", actionType.name());

            if (actionType == UndoClass.UNDO_TYPE.UPDATE_PRINCIPAL) {
                Principal oldPrincipal = (Principal) currEntry.get(1);
                Principal newPrincipal = (Principal) currEntry.get(2);
                type.put("oldPrincipal", convertSinglePrincipalToDocument(oldPrincipal));
                type.put("newPrincipal", convertSinglePrincipalToDocument(newPrincipal));

            } else if (actionType == UndoClass.UNDO_TYPE.CREATE_PRINCIPAL) {
                Principal principal = (Principal) currEntry.get(1);
                type.put("principal", convertSinglePrincipalToDocument(principal));

            } else if (actionType == UndoClass.UNDO_TYPE.REMOVE_PRINCIPAL) {
                Principal principal = (Principal) currEntry.get(1);
                type.put("principal", convertSinglePrincipalToDocument(principal));

            } else if (actionType == UndoClass.UNDO_TYPE.ADD_RESOURCE) {
                Resource resource = (Resource) currEntry.get(1);
                type.put("resource", convertSingleResourceToDocument(resource));

            } else if (actionType == UndoClass.UNDO_TYPE.REMOVE_RESOURCE) {
                Resource toAdd = (Resource) currEntry.get(1);
                type.put("resource", convertSingleResourceToDocument(toAdd));
                Map<ResourceAction, List<PrincipalCategory>> assignedPerms = (Map<ResourceAction, List<PrincipalCategory>>) currEntry.get(2);
                List<Document> docList = new ArrayList<>();
                for(ResourceAction a : assignedPerms.keySet()){
                    List<PrincipalCategory> curr = assignedPerms.get(a);
                    docList.add(convertSingleActionToDocument(a).append("categories", convertJuniorCategoriesToDocuments(curr)));
                }
                type.put("actionMapping", docList);
            } else if (actionType == UndoClass.UNDO_TYPE.ADD_ACTION) {
                ResourceAction a = (ResourceAction) currEntry.get(1);
                type.put("action", convertSingleActionToDocument(a));

            } else if (actionType == UndoClass.UNDO_TYPE.REMOVE_ACTION) {
                ResourceAction a = (ResourceAction) currEntry.get(1);
                List<PrincipalCategory> categories = (List<PrincipalCategory>) currEntry.get(2);
                type.put("action", convertSingleActionToDocument(a));
                type.put("categories", convertJuniorCategoriesToDocuments(categories));

            } else if (actionType == UndoClass.UNDO_TYPE.UPDATE_CATEGORY) {
                PrincipalCategory oldCategory = (PrincipalCategory) currEntry.get(1);
                PrincipalCategory newCategory = (PrincipalCategory) currEntry.get(2);
                type.put("oldCategory", convertSinglePrincipalCategoryToDocument(oldCategory));
                type.put("newCategory", convertSinglePrincipalCategoryToDocument(newCategory));

            } else if (actionType == UndoClass.UNDO_TYPE.CREATE_CATEGORY) {
                PrincipalCategory category = (PrincipalCategory) currEntry.get(1);
                type.put("category", convertSinglePrincipalCategoryToDocument(category));

            } else if (actionType == UndoClass.UNDO_TYPE.REMOVE_CATEGORY) {
                PrincipalCategory oldCategory = (PrincipalCategory) currEntry.get(1);
                List<PrincipalCategory> oldSeniorCategories = (List<PrincipalCategory>) currEntry.get(2);
                type.put("category", convertSinglePrincipalCategoryToDocument(oldCategory));
                type.put("seniorCategories", convertJuniorCategoriesToDocuments(oldSeniorCategories));

            } else if (actionType == UndoClass.UNDO_TYPE.UPDATE_PERMISSIONS) {
                PrincipalCategory curr = (PrincipalCategory) currEntry.get(1);
                List<ResourceAction> oldActions = (List<ResourceAction>) currEntry.get(2);
                type.put("category", convertSinglePrincipalCategoryToDocument(curr));
                type.put("actions", convertActionsToDocuments(oldActions));

            } else if (actionType == UndoClass.UNDO_TYPE.UPDATE_HIERARCHY) {
                PrincipalCategory curr = (PrincipalCategory) currEntry.get(1);
                List<PrincipalCategory> oldJrCategories = (List<PrincipalCategory>) currEntry.get(2);
                type.put("category", convertSinglePrincipalCategoryToDocument(curr));
                type.put("juniorCategories", convertJuniorCategoriesToDocuments(oldJrCategories));

            }
            docs.add(type);
        }
        if(!docs.isEmpty()) {
            undoClassCollection.insertMany(docs);
        }
    }

    public UndoClass getUndoClass(){
        MongoCollection<Document> undoClassCollection = database.getCollection("undoClass");
        UndoClass undoClass = new UndoClass();
        for(Document doc : undoClassCollection.find()){
            UndoClass.UNDO_TYPE actionType = UndoClass.UNDO_TYPE.valueOf(doc.getString("type"));
            if (actionType == UndoClass.UNDO_TYPE.UPDATE_PRINCIPAL) {
                Principal oldPrincipal = convertDocumentToSinglePrincipal((Document) doc.get("oldPrincipal"));
                Principal newPrincipal = convertDocumentToSinglePrincipal((Document) doc.get("newPrincipal"));
                undoClass.addUpdatePrincipal(oldPrincipal, newPrincipal);

            } else if (actionType == UndoClass.UNDO_TYPE.CREATE_PRINCIPAL) {
                Principal principal = convertDocumentToSinglePrincipal((Document) doc.get("principal"));
                undoClass.addCreatePrincipal(principal);

            } else if (actionType == UndoClass.UNDO_TYPE.REMOVE_PRINCIPAL) {
                Principal principal = convertDocumentToSinglePrincipal((Document) doc.get("principal"));
                undoClass.addRemovePrincipal(principal);

            } else if (actionType == UndoClass.UNDO_TYPE.ADD_RESOURCE) {
                Document resourceDoc = (Document) doc.get("resource");
                String resourceName =  (String) resourceDoc.get("name");
                undoClass.addAddResource(new Resource(resourceName));

            } else if (actionType == UndoClass.UNDO_TYPE.REMOVE_RESOURCE) {
                Map<ResourceAction, List<PrincipalCategory>> assignedPerms = new HashMap<>();
                Document resourceDoc = (Document) doc.get("resource");
                Resource resource = new Resource(resourceDoc.getString("name"));
                List<Document> actionMapping = (List<Document>) doc.get("actionMapping");
                for(Document d : actionMapping){
                    ResourceAction fromDb = new ResourceAction(d.getString("name"), resource);
                    List<PrincipalCategory> tempList = new ArrayList<>();
                    List<Document> associatedCategories = (List<Document>) d.get("categories");
                    for(Document categoryDoc : associatedCategories){
                        tempList.add(new PrincipalCategory(categoryDoc.getString("name")));
                    }
                    assignedPerms.put(fromDb, tempList);
                }
                undoClass.addRemoveResource(resource, assignedPerms);

            } else if (actionType == UndoClass.UNDO_TYPE.ADD_ACTION) {
                Document actionDoc = (Document) doc.get("action");
                Document resourceDoc = (Document) actionDoc.get("resource");
                ResourceAction a = new ResourceAction(actionDoc.getString("name"), new Resource(resourceDoc.getString("name")));
                undoClass.addAddAction(a);

            } else if (actionType == UndoClass.UNDO_TYPE.REMOVE_ACTION) {
                Document actionDoc = (Document) doc.get("action");
                Document resourceDoc = (Document) actionDoc.get("resource");
                List<Document> categoryDocs = (List<Document>) doc.get("categories");
                List<PrincipalCategory> associatedCategories = new ArrayList<>();
                for(Document d : categoryDocs){
                    associatedCategories.add(new PrincipalCategory(d.getString("name")));
                }
                ResourceAction action = new ResourceAction(actionDoc.getString("name"), new Resource(resourceDoc.getString("name")));
                undoClass.addRemoveAction(action, associatedCategories);

            } else if (actionType == UndoClass.UNDO_TYPE.UPDATE_CATEGORY) {
                Document newCategoryDoc = (Document) doc.get("newCategory");
                Document oldCategoryDoc = (Document) doc.get("oldCategory");
                PrincipalCategory newCategory = convertDocumentToSinglePrincipalCategory(newCategoryDoc);
                PrincipalCategory oldCategory = convertDocumentToSinglePrincipalCategory(oldCategoryDoc);
                undoClass.addUpdateCategory(oldCategory, newCategory);

            } else if (actionType == UndoClass.UNDO_TYPE.CREATE_CATEGORY) {
                Document categoryDoc = (Document) doc.get("category");
                PrincipalCategory principalCategory = convertDocumentToSinglePrincipalCategory(categoryDoc);
                undoClass.addCreateCategory(principalCategory);

            } else if (actionType == UndoClass.UNDO_TYPE.REMOVE_CATEGORY) {
                Document categoryDoc = (Document) doc.get("category");
                List<Document> seniorCategoryDocs = (List<Document>) doc.get("seniorCategories");
                PrincipalCategory notFixedCategory = convertDocumentToSinglePrincipalCategory(categoryDoc);
                List<PrincipalCategory> seniorCategoryList = new ArrayList<>();
                for(Document d : seniorCategoryDocs){
                    seniorCategoryList.add(new PrincipalCategory(d.getString("name")));
                }
                undoClass.addRemoveCategory(notFixedCategory, seniorCategoryList);

            } else if (actionType == UndoClass.UNDO_TYPE.UPDATE_PERMISSIONS) {
                Document categoryDoc = (Document) doc.get("category");
                List<ResourceAction> actionList = convertDocumentsToActions((List<Document>) doc.get("actions"));
                PrincipalCategory category = convertDocumentToSinglePrincipalCategory(categoryDoc);
                undoClass.addUpdatePermissions(category, actionList);

            } else if (actionType == UndoClass.UNDO_TYPE.UPDATE_HIERARCHY) {
                Document categoryDoc = (Document) doc.get("category");
                PrincipalCategory category = convertDocumentToSinglePrincipalCategory(categoryDoc);
                List<Document> jrCategoryDocs = (List<Document>) doc.get("juniorCategories");
                List<PrincipalCategory> oldJrCategories = new ArrayList<>();
                for(Document d : jrCategoryDocs){
                    oldJrCategories.add(new PrincipalCategory(d.getString("name")));
                }
                undoClass.addUpdateHierarchy(category, oldJrCategories);

            }
        }
        return undoClass;
    }


    public void savePrincipalCategory(PrincipalCategory principalCategory) {
        MongoCollection<Document> principalCategoriesCollection = database.getCollection("principalCategories");
        Document principalCategoryDoc = new Document("name", principalCategory.getName());
        principalCategoryDoc.put("juniorCategories", convertJuniorCategoriesToDocuments(principalCategory.getJuniorCategories()));
        principalCategoryDoc.put("principals", convertPrincipalsToDocuments(principalCategory.getPrincipals()));
        principalCategoryDoc.put("actions", convertActionsToDocuments(principalCategory.getActions()));
        principalCategoryDoc.put("stringRules", convertStringRulesToDocuments(principalCategory.getStringRules()));
        principalCategoryDoc.put("integerRules", convertIntegerRulesToDocuments(principalCategory.getIntegerRules()));
        principalCategoryDoc.put("dateRules", convertDateRulesToDocuments(principalCategory.getDateRules()));

        principalCategoriesCollection.insertOne(principalCategoryDoc);
    }

    public List<Document> convertJuniorCategoriesToDocuments(List<PrincipalCategory> juniorCategories) {
        List<Document> docs = new ArrayList<>();
        for (PrincipalCategory juniorCategory : juniorCategories) {
            Document doc = new Document("name", juniorCategory.getName());
            docs.add(doc);
        }
        return docs;
    }

    public List<Document> convertPrincipalsToDocuments(List<Principal> principals) {
        List<Document> docs = new ArrayList<>();
        for (Principal principal : principals) {
            Document doc = new Document("name", principal.getName());
            doc.put("stringAttributes", convertAttributesToDocuments(principal.getStringAttributeList(), "StringAttribute"));
            doc.put("integerAttributes", convertAttributesToDocuments(principal.getIntegerAttributeList(), "IntegerAttribute"));
            doc.put("dateAttributes", convertAttributesToDocuments(principal.getDateAttributeList(), "DateAttribute"));
            docs.add(doc);
        }
        return docs;
    }

    public List<Document> convertActionsToDocuments(List<ResourceAction> actions) {
        List<Document> docs = new ArrayList<>();
        for (ResourceAction action : actions) {
            Document doc = new Document("name", action.getName());
            doc.put("resource", new Document("name", action.getResource().getName()));
            docs.add(doc);
        }
        return docs;
    }

    public List<Document> convertStringRulesToDocuments(List<StringRule> stringRules) {
        List<Document> docs = new ArrayList<>();
        for (StringRule rule : stringRules) {
            Document doc = new Document("attribute", rule.getAttribute().getName());
            doc.put("attributeValue", rule.getAttribute().getValue());
            doc.put("requirements", rule.getRequirements());
            docs.add(doc);
        }
        return docs;
    }

    public List<Document> convertIntegerRulesToDocuments(List<IntegerRule> integerRules) {
        List<Document> docs = new ArrayList<>();
        for (IntegerRule rule : integerRules) {
            Document doc = new Document("attribute", rule.getAttribute().getName());
            doc.put("attributeValue", rule.getAttribute().getValue());
            doc.put("lowerBound", rule.getLowerBound());
            doc.put("upperBound", rule.getUpperBound());
            docs.add(doc);
        }
        return docs;
    }

    public List<Document> convertDateRulesToDocuments(List<DateRule> dateRules) {
        List<Document> docs = new ArrayList<>();
        for (DateRule rule : dateRules) {
            Document doc = new Document("attribute", rule.getAttribute().getName());
            doc.put("attributeValue", rule.getAttribute().getValue());
            doc.put("lowerBound", rule.getLowerBound());
            doc.put("upperBound", rule.getUpperBound());
            docs.add(doc);
        }
        return docs;
    }



    public <T> List<Document> convertAttributesToDocuments(List<T> attributes, String attributeType) {
        List<Document> docs = new ArrayList<>();
        for (T attribute : attributes) {
            Document doc = new Document();
            if (attributeType.equals("StringAttribute")) {
                StringAttribute stringAttribute = (StringAttribute) attribute;
                doc.put("name", stringAttribute.getName());
                doc.put("value", stringAttribute.getValue());
            } else if (attributeType.equals("IntegerAttribute")) {
                IntegerAttribute integerAttribute = (IntegerAttribute) attribute;
                doc.put("name", integerAttribute.getName());
                doc.put("value", integerAttribute.getValue());
            } else if (attributeType.equals("DateAttribute")) {
                DateAttribute dateAttribute = (DateAttribute) attribute;
                doc.put("name", dateAttribute.getName());
                doc.put("value", dateAttribute.getValue());
            }
            docs.add(doc);
        }
        return docs;
    }

    public PrincipalCategory convertDocumentToSinglePrincipalCategory(Document doc) {
        String name = doc.getString("name");
        List<PrincipalCategory> juniorCategories = convertDocumentsToJuniorCategories((List<Document>) doc.get("juniorCategories"));
        List<Principal> principals = convertDocumentsToPrincipals((List<Document>) doc.get("principals"));
        List<ResourceAction> actions = convertDocumentsToActions((List<Document>) doc.get("actions"));
        List<StringRule> stringRules = convertDocumentsToStringRules((List<Document>) doc.get("stringRules"));
        List<IntegerRule> integerRules = convertDocumentsToIntegerRules((List<Document>) doc.get("integerRules"));
        List<DateRule> dateRules = convertDocumentsToDateRules((List<Document>) doc.get("dateRules"));

        PrincipalCategory principalCategory = new PrincipalCategory(name);
        principalCategory.setJuniorCategories(juniorCategories);
        principalCategory.setPrincipals(principals);
        principalCategory.setActions(actions);
        principalCategory.setStringRules(stringRules);
        principalCategory.setIntegerRules(integerRules);
        principalCategory.setDateRules(dateRules);

        return principalCategory;
    }

    public ResourceAction convertDocumentToSingleAction(Document doc) {
        String name = doc.getString("name");
        Document resourceDoc = (Document) doc.get("resource");
        String resourceName = resourceDoc.getString("name");
        Resource resource = new Resource(resourceName);
        ResourceAction action = new ResourceAction(name, resource);
        return action;
    }

    public Document convertSinglePrincipalCategoryToDocument(PrincipalCategory principalCategory) {
        Document principalCategoryDoc = new Document("name", principalCategory.getName());
        principalCategoryDoc.put("juniorCategories", convertJuniorCategoriesToDocuments(principalCategory.getJuniorCategories()));
        principalCategoryDoc.put("principals", convertPrincipalsToDocuments(principalCategory.getPrincipals()));
        principalCategoryDoc.put("actions", convertActionsToDocuments(principalCategory.getActions()));
        principalCategoryDoc.put("stringRules", convertStringRulesToDocuments(principalCategory.getStringRules()));
        principalCategoryDoc.put("integerRules", convertIntegerRulesToDocuments(principalCategory.getIntegerRules()));
        principalCategoryDoc.put("dateRules", convertDateRulesToDocuments(principalCategory.getDateRules()));

        return principalCategoryDoc;
    }

    public Document convertSingleActionToDocument(ResourceAction action){
        Document doc = new Document("name", action.getName());
        doc.put("resource", new Document("name", action.getResource().getName()));
        return doc;
    }

    public Document convertSingleResourceToDocument(Resource r){
        return new Document("name", r.getName());
    }

    public Document convertSinglePrincipalToDocument(Principal principal) {
        Document doc = new Document("name", principal.getName());
        doc.put("stringAttributes", convertAttributesToDocuments(principal.getStringAttributeList(), "StringAttribute"));
        doc.put("integerAttributes", convertAttributesToDocuments(principal.getIntegerAttributeList(), "IntegerAttribute"));
        doc.put("dateAttributes", convertAttributesToDocuments(principal.getDateAttributeList(), "DateAttribute"));
        return doc;
    }

    public Principal convertDocumentToSinglePrincipal(Document doc){
        String name = doc.getString("name");
        List<StringAttribute> stringAttributes = convertDocumentsToStringAttributes((List<Document>) doc.get("stringAttributes"));
        List<IntegerAttribute> integerAttributes = convertDocumentsToIntegerAttributes((List<Document>) doc.get("integerAttributes"));
        List<DateAttribute> dateAttributes = convertDocumentsToDateAttributes((List<Document>) doc.get("dateAttributes"));

        Principal principal = new Principal(name);
        principal.setStringAttributeList(stringAttributes);
        principal.setIntegerAttributeList(integerAttributes);
        principal.setDateAttributeList(dateAttributes);
        return principal;
    }

    public void saveAssignCategories(AssignCategories assignCategories, UndoClass undoClass) {
        database.getCollection("principalCategories").drop();
        database.getCollection("principals").drop();
        database.getCollection("actions").drop();
        database.getCollection("resources").drop();
        database.getCollection("undoClass").drop();

        createCollectionIfNotExists("principalCategories");
        createCollectionIfNotExists("principals");
        createCollectionIfNotExists("actions");
        createCollectionIfNotExists("resources");
        createCollectionIfNotExists("undoClass");

        saveUndoClass(undoClass);


        for (Principal principal : assignCategories.getPrincipals()) {
            savePrincipal(principal);
        }
        for (PrincipalCategory principalCategory : assignCategories.getPrincipalCategories()) {
            savePrincipalCategory(principalCategory);
        }
        for (ResourceAction action : assignCategories.getResourceActions()) {
            saveAction(action);
        }
        for (Resource resource : assignCategories.getResources()) {
            saveResource(resource);
        }
    }

    public void savePrincipal(Principal principal) {
        MongoCollection<Document> principalsCollection = database.getCollection("principals");
        Document principalDoc = new Document("name", principal.getName());
        principalDoc.put("stringAttributes", convertAttributesToDocuments(principal.getStringAttributeList(), "StringAttribute"));
        principalDoc.put("integerAttributes", convertAttributesToDocuments(principal.getIntegerAttributeList(), "IntegerAttribute"));
        principalDoc.put("dateAttributes", convertAttributesToDocuments(principal.getDateAttributeList(), "DateAttribute"));
        principalsCollection.insertOne(principalDoc);
    }

    public void saveAction(ResourceAction action) {
        MongoCollection<Document> actionsCollection = database.getCollection("actions");
        Document actionDoc = new Document("name", action.getName());
        actionDoc.put("resource", new Document("name", action.getResource().getName()));
        actionsCollection.insertOne(actionDoc);
    }

    public void saveResource(Resource resource) {
        MongoCollection<Document> resourcesCollection = database.getCollection("resources");
        Document resourceDoc = new Document("name", resource.getName());
        resourcesCollection.insertOne(resourceDoc);
    }

    public AssignCategories getAssignCategories() {
        List<Principal> principals = getPrincipals();
        List<PrincipalCategory> principalCategories = getPrincipalCategories();
        List<ResourceAction> resourceActions = getActions();
        List<Resource> resources = getResources();

        AssignCategories toReturn = new AssignCategories(principals, principalCategories);
        toReturn.setResourceActions(resourceActions);
        toReturn.setResources(resources);
        return toReturn;
    }

    public List<Principal> getPrincipals() {
        MongoCollection<Document> principalsCollection = database.getCollection("principals");
        List<Principal> principals = new ArrayList<>();

        for (Document doc : principalsCollection.find()) {
            String name = doc.getString("name");
            List<StringAttribute> stringAttributes = convertDocumentsToStringAttributes((List<Document>) doc.get("stringAttributes"));
            List<IntegerAttribute> integerAttributes = convertDocumentsToIntegerAttributes((List<Document>) doc.get("integerAttributes"));
            List<DateAttribute> dateAttributes = convertDocumentsToDateAttributes((List<Document>) doc.get("dateAttributes"));

            Principal principal = new Principal(name);
            principal.setStringAttributeList(stringAttributes);
            principal.setIntegerAttributeList(integerAttributes);
            principal.setDateAttributeList(dateAttributes);
            principals.add(principal);
        }

        return principals;
    }


    public List<StringAttribute> convertDocumentsToStringAttributes(List<Document> docs) {
        List<StringAttribute> attributes = new ArrayList<>();
        for (Document doc : docs) {
            String name = doc.getString("name");
            String value = doc.getString("value");
            attributes.add(new StringAttribute(name, value));
        }
        return attributes;
    }

    public List<PrincipalCategory> getPrincipalCategories() {
        MongoCollection<Document> principalCategoriesCollection = database.getCollection("principalCategories");
        List<PrincipalCategory> principalCategories = new ArrayList<>();

        for (Document doc : principalCategoriesCollection.find()) {
            String name = doc.getString("name");
            List<PrincipalCategory> juniorCategories = convertDocumentsToJuniorCategories((List<Document>) doc.get("juniorCategories"));
            List<Principal> principals = convertDocumentsToPrincipals((List<Document>) doc.get("principals"));
            List<ResourceAction> actions = convertDocumentsToActions((List<Document>) doc.get("actions"));
            List<StringRule> stringRules = convertDocumentsToStringRules((List<Document>) doc.get("stringRules"));
            List<IntegerRule> integerRules = convertDocumentsToIntegerRules((List<Document>) doc.get("integerRules"));
            List<DateRule> dateRules = convertDocumentsToDateRules((List<Document>) doc.get("dateRules"));

            PrincipalCategory principalCategory = new PrincipalCategory(name);
            principalCategory.setJuniorCategories(juniorCategories);
            principalCategory.setPrincipals(principals);
            principalCategory.setActions(actions);
            principalCategory.setStringRules(stringRules);
            principalCategory.setIntegerRules(integerRules);
            principalCategory.setDateRules(dateRules);
            principalCategories.add(principalCategory);
        }

        return principalCategories;
    }

    public List<ResourceAction> getActions() {
        MongoCollection<Document> actionsCollection = database.getCollection("actions");
        List<ResourceAction> actions = new ArrayList<>();

        for (Document doc : actionsCollection.find()) {
            String name = doc.getString("name");
            Document resourceDoc = (Document) doc.get("resource");
            String resourceName = resourceDoc.getString("name");
            Resource resource = new Resource(resourceName);

            ResourceAction action = new ResourceAction(name, resource);
            actions.add(action);
        }

        return actions;
    }

    public List<Resource> getResources() {
        MongoCollection<Document> resourcesCollection = database.getCollection("resources");
        List<Resource> resources = new ArrayList<>();

        for (Document doc : resourcesCollection.find()) {
            String name = doc.getString("name");
            Resource resource = new Resource(name);
            resources.add(resource);
        }

        return resources;
    }

    public List<IntegerAttribute> convertDocumentsToIntegerAttributes(List<Document> docs) {
        List<IntegerAttribute> attributes = new ArrayList<>();
        for (Document doc : docs) {
            String name = doc.getString("name");
            int value = doc.getInteger("value");
            attributes.add(new IntegerAttribute(name, value));
        }
        return attributes;
    }

    public List<DateAttribute> convertDocumentsToDateAttributes(List<Document> docs) {
        List<DateAttribute> attributes = new ArrayList<>();
        for (Document doc : docs) {
            String name = doc.getString("name");
            Date value = doc.getDate("value");
            attributes.add(new DateAttribute(name, value));
        }
        return attributes;
    }

    public List<PrincipalCategory> convertDocumentsToJuniorCategories(List<Document> docs) {
        List<PrincipalCategory> juniorCategories = new ArrayList<>();
        for (Document doc : docs) {
            String name = doc.getString("name");
            juniorCategories.add(new PrincipalCategory(name));
        }
        return juniorCategories;
    }

    public List<Principal> convertDocumentsToPrincipals(List<Document> docs) {
        List<Principal> principals = new ArrayList<>();
        for (Document doc : docs) {
            String name = doc.getString("name");
            List<StringAttribute> stringAttributes = convertDocumentsToStringAttributes((List<Document>) doc.get("stringAttributes"));
            List<IntegerAttribute> integerAttributes = convertDocumentsToIntegerAttributes((List<Document>) doc.get("integerAttributes"));
            List<DateAttribute> dateAttributes = convertDocumentsToDateAttributes((List<Document>) doc.get("dateAttributes"));

            Principal principal = new Principal(name);
            principal.setStringAttributeList(stringAttributes);
            principal.setIntegerAttributeList(integerAttributes);
            principal.setDateAttributeList(dateAttributes);
            principals.add(principal);
        }
        return principals;
    }

    public List<ResourceAction> convertDocumentsToActions(List<Document> docs) {
        List<ResourceAction> actions = new ArrayList<>();
        for (Document doc : docs) {
            String name = doc.getString("name");
            Document resourceDoc = (Document) doc.get("resource");
            String resourceName = resourceDoc.getString("name");
            Resource resource = new Resource(resourceName);

            ResourceAction action = new ResourceAction(name, resource);
            actions.add(action);
        }
        return actions;
    }

    public List<StringRule> convertDocumentsToStringRules(List<Document> docs) {
        List<StringRule> stringRules = new ArrayList<>();
        for (Document doc : docs) {
            String attributeName = doc.getString("attribute");
            String attributeValue = doc.getString("attributeValue");
            StringAttribute attribute = new StringAttribute(attributeName, attributeValue);
            List<String> requirements = (List<String>) doc.get("requirements");

            StringRule rule = new StringRule(attribute, requirements);
            stringRules.add(rule);
        }
        return stringRules;
    }


    public List<IntegerRule> convertDocumentsToIntegerRules(List<Document> docs) {
        List<IntegerRule> integerRules = new ArrayList<>();
        for (Document doc : docs) {
            String attributeName = doc.getString("attribute");
            int attributeValue = doc.getInteger("attributeValue");
            IntegerAttribute attribute = new IntegerAttribute(attributeName, attributeValue);
            int lowerBound = doc.getInteger("lowerBound");
            int upperBound = doc.getInteger("upperBound");

            IntegerRule rule = new IntegerRule(attribute, lowerBound, upperBound);
            integerRules.add(rule);
        }
        return integerRules;
    }

    public List<DateRule> convertDocumentsToDateRules(List<Document> docs) {
        List<DateRule> dateRules = new ArrayList<>();
        for (Document doc : docs) {
            String attributeName = doc.getString("attribute");
            Date attributeValue = doc.getDate("attributeValue");
            DateAttribute attribute = new DateAttribute(attributeName, attributeValue);
            Date lowerBound = doc.getDate("lowerBound");
            Date upperBound = doc.getDate("upperBound");

            DateRule rule = new DateRule(attribute, lowerBound, upperBound);
            dateRules.add(rule);
        }
        return dateRules;
    }


}

