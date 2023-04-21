package categories;

import categoryrules.*;
import principal_resource_attributes.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AssignCategories {

        List<Principal> principals;
        List<PrincipalCategory> principalCategories;
        List<Action> resourceActions;
        List<Resource> resources;

    public AssignCategories(List<Principal> principals, List<PrincipalCategory> principalCategories) {
        this.principals = principals;
        this.principalCategories = principalCategories;
        resources = new ArrayList<>();
        resourceActions = new ArrayList<>();
        evaluatePrincipalCategories();
    }

    public List<Action> getResourceActions() {
        return resourceActions;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    public void setResourceActions(List<Action> resourceActions) {
        this.resourceActions = resourceActions;
    }

    public void addResource(Resource r) {
        resources.add(r);
    }

    public void removeResource(Resource r) {
        resources.remove(r);
    }

    public void addPrincipalCategory(PrincipalCategory p) {
        principalCategories.add(p);
    }

    public void removePrincipalCategory(PrincipalCategory p) {
        principalCategories.remove(p);
    }

    public void addPrincipal(Principal p) {
        principals.add(p);
    }

    public void removePrincipal(Principal p) {
        principals.remove(p);
    }

    public void evaluatePrincipalCategories() {
        for (PrincipalCategory category : principalCategories) {
            category.getPrincipals().clear();
            for (Principal principal : principals) {
                boolean inCategory = true;
                for (StringRule rule : category.getStringRules()) {
                    boolean noMatch = true;
                    for (StringAttribute principalAttribute : principal.getStringAttributeList()) {
                        if (principalAttribute.getName().strip().equalsIgnoreCase(rule.getAttribute().getName().strip())) {
                            noMatch = false;
                            if (!rule.getRequirements().contains(principalAttribute.getValue())) {
                                inCategory = false;
                            }
                        }
                    }
                    if (noMatch) {
                        inCategory = false;
                    }
                }
                for (IntegerRule rule : category.getIntegerRules()) {
                    boolean noMatch = true;
                    for (IntegerAttribute integerAttribute : principal.getIntegerAttributeList()) {
                        if (integerAttribute.getName().strip().equalsIgnoreCase(rule.getAttribute().getName().strip())) {
                            noMatch = false;
                            if (integerAttribute.getValue() < rule.getLowerBound() || integerAttribute.getValue() > rule.getUpperBound()) {
                                inCategory = false;
                            }
                        }
                    }
                    if (noMatch) {
                        inCategory = false;
                    }
                }
                for (DateRule rule : category.getDateRules()) {
                    boolean noMatch = true;
                    for (DateAttribute dateAttribute : principal.getDateAttributeList()) {
                        if (dateAttribute.getName().strip().equalsIgnoreCase(rule.getAttribute().getName().strip())) {
                            noMatch = false;
                            if (dateAttribute.getValue().before(rule.getLowerBound()) || dateAttribute.getValue().after(rule.getUpperBound())) {
                                inCategory = false;
                            }
                        }
                    }
                    if (noMatch) {
                        inCategory = false;
                    }
                }
                if (inCategory) {
                    category.addPrincipal(principal);
                } else {
                    category.removePrincipal(principal);
                }
            }
        }
    }

    public List<Principal> getPrincipals() {
        return principals;
    }

    public void setPrincipals(List<Principal> principals) {
        this.principals = principals;
    }

    public List<PrincipalCategory> getPrincipalCategories() {
        return principalCategories;
    }

    public void setPrincipalCategories(List<PrincipalCategory> principalCategories) {
        this.principalCategories = principalCategories;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AssignCategories)) return false;
        AssignCategories that = (AssignCategories) o;
        return Objects.equals(principals, that.principals) &&
                Objects.equals(principalCategories, that.principalCategories) &&
                Objects.equals(resourceActions, that.resourceActions) &&
                Objects.equals(resources, that.resources);
    }

    @Override
    public int hashCode() {
        return Objects.hash(principals, principalCategories, resourceActions, resources);
    }


}
