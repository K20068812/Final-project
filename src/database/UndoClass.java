package database;

import categories.Action;
import categories.Principal;
import categories.PrincipalCategory;
import categories.Resource;

import java.util.*;

public class UndoClass {
    public enum UNDO_TYPE {
        UPDATE_PRINCIPAL,
        CREATE_PRINCIPAL,
        REMOVE_PRINCIPAL,
        ADD_RESOURCE,
        REMOVE_RESOURCE,
        ADD_ACTION,
        REMOVE_ACTION,
        UPDATE_CATEGORY,
        CREATE_CATEGORY,
        REMOVE_CATEGORY,
        UPDATE_PERMISSIONS,
        UPDATE_HIERARCHY
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UndoClass that = (UndoClass) o;
        return Objects.equals(actionTracker, that.actionTracker);
    }

    @Override
    public int hashCode() {
        return Objects.hash(actionTracker);
    }


    List<List<Object>> actionTracker;
    public UndoClass(){
        actionTracker = new ArrayList<>();
    }
    public List<List<Object>> getActionTracker() {
        return actionTracker;
    }

    public void addUpdatePrincipal(Principal oldPrincipal, Principal newPrincipal){
        List<Object> info = new ArrayList<>();
        info.add(UNDO_TYPE.UPDATE_PRINCIPAL);
        info.add(oldPrincipal);
        info.add(newPrincipal);
        actionTracker.add(info);
    }
    public void addCreatePrincipal(Principal principal){
        List<Object> nameList = new ArrayList<>();
        nameList.add(UNDO_TYPE.CREATE_PRINCIPAL);
        nameList.add(principal);
        actionTracker.add(nameList);
    }
    public void addRemovePrincipal(Principal p){
        List<Object> info = new ArrayList<>();
        info.add(UNDO_TYPE.REMOVE_PRINCIPAL);
        info.add(p);
        actionTracker.add(info);
    }
    public void addAddResource(Resource resource){
        List<Object> nameList = new ArrayList<>();
        nameList.add(UNDO_TYPE.ADD_RESOURCE);
        nameList.add(resource);
        actionTracker.add(nameList);
    }
    public void addRemoveResource(Resource r, Map<Action, List<PrincipalCategory>> assignedPerms){
        List<Object> nameList = new ArrayList<>();
        nameList.add(UNDO_TYPE.REMOVE_RESOURCE);
        nameList.add(r);
        nameList.add(assignedPerms);
        actionTracker.add(nameList);
    }
    public void addAddAction(Action a){
        List<Object> nameList = new ArrayList<>();
        nameList.add(UNDO_TYPE.ADD_ACTION);
        nameList.add(a);
        actionTracker.add(nameList);
    }
    public void addRemoveAction(Action a, List<PrincipalCategory> principalCategories){
        List<Object> nameList = new ArrayList<>();
        nameList.add(UNDO_TYPE.REMOVE_ACTION);
        nameList.add(a);
        nameList.add(principalCategories);
        actionTracker.add(nameList);
    }
    public void addUpdateCategory(PrincipalCategory oldCategory, PrincipalCategory newCategory){
        List<Object> infoList = new ArrayList<>();
        infoList.add(UNDO_TYPE.UPDATE_CATEGORY);
        infoList.add(oldCategory);
        infoList.add(newCategory);
        actionTracker.add(infoList);
    }
    public void addCreateCategory(PrincipalCategory category){
        List<Object> infoList = new ArrayList<>();
        infoList.add(UNDO_TYPE.CREATE_CATEGORY);
        infoList.add(category);
        actionTracker.add(infoList);
    }
    public void addRemoveCategory(PrincipalCategory oldCategory, List<PrincipalCategory> oldSeniorCategories){
        List<Object> infoList = new ArrayList<>();
        infoList.add(UNDO_TYPE.REMOVE_CATEGORY);
        infoList.add(oldCategory);
        infoList.add(oldSeniorCategories);
        actionTracker.add(infoList);
    }
    public void addUpdatePermissions(PrincipalCategory category, List<Action> oldActions){
        List<Object> infoList = new ArrayList<>();
        infoList.add(UNDO_TYPE.UPDATE_PERMISSIONS);
        infoList.add(category);
        infoList.add(oldActions);
        actionTracker.add(infoList);
    }
    public void addUpdateHierarchy(PrincipalCategory pc, List<PrincipalCategory> oldJuniorCategoryNames){
        List<Object> infoList = new ArrayList<>();
        infoList.add(UNDO_TYPE.UPDATE_HIERARCHY);
        infoList.add(pc);
        infoList.add(oldJuniorCategoryNames);
        actionTracker.add(infoList);
    }
}
