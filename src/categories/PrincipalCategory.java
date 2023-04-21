package categories;

import categoryrules.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PrincipalCategory {
    private String name;
    private List<PrincipalCategory> juniorCategories;
    private List<Principal> principals;
    private List<ResourceAction> actions;
    private List<StringRule> stringRules;
    private List<IntegerRule> integerRules;
    private List<DateRule> dateRules;

    public PrincipalCategory(String name) {
        this.name = name;
        principals = new ArrayList<>();
        stringRules = new ArrayList<>();
        integerRules = new ArrayList<>();
        dateRules = new ArrayList<>();
        juniorCategories = new ArrayList<>();
        actions = new ArrayList<>();
    }
    public PrincipalCategory(PrincipalCategory other) {
        this.name = other.name;
        this.juniorCategories = new ArrayList<>();
        for (PrincipalCategory category : other.getJuniorCategories()) {
            this.juniorCategories.add(new PrincipalCategory(category));
        }
        this.principals = new ArrayList<>();
        for (Principal principal : other.getPrincipals()) {
            this.principals.add(new Principal(principal));
        }
        this.actions = new ArrayList<>();
        for (ResourceAction action : other.getActions()) {
            this.actions.add(new ResourceAction(action));
        }
        this.stringRules = new ArrayList<>();
        for (StringRule rule : other.getStringRules()) {
            this.stringRules.add(new StringRule(rule));
        }
        this.integerRules = new ArrayList<>();
        for (IntegerRule rule : other.getIntegerRules()) {
            this.integerRules.add(new IntegerRule(rule));
        }
        this.dateRules = new ArrayList<>();
        for (DateRule rule : other.getDateRules()) {
            this.dateRules.add(new DateRule(rule));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrincipalCategory that = (PrincipalCategory) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(juniorCategories, that.juniorCategories) &&
                Objects.equals(principals, that.principals) &&
                Objects.equals(actions, that.actions) &&
                Objects.equals(stringRules, that.stringRules) &&
                Objects.equals(integerRules, that.integerRules) &&
                Objects.equals(dateRules, that.dateRules);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, juniorCategories, principals, actions, stringRules, integerRules, dateRules);
    }

    public List<ResourceAction> getActions(){
        return actions;
    }

    public void setActions(List<ResourceAction> actions){
        this.actions = actions;
    }

    public void addAction(ResourceAction a){
        actions.add(a);
    }

    public void removeAction(ResourceAction a){
        actions.remove(a);
    }

    public List<PrincipalCategory> getJuniorCategories(){
        return juniorCategories;
    }

    public void setJuniorCategories(List<PrincipalCategory> juniorCategories){
         this.juniorCategories = juniorCategories;
    }

    public void addJuniorCategory(PrincipalCategory p){
        juniorCategories.add(p);
    }

    public void removeJuniorCategory(PrincipalCategory p){
        juniorCategories.remove(p);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Principal> getPrincipals() {
        return principals;
    }

    public void setPrincipals(List<Principal> principals) {
        this.principals = principals;
    }

    public void addPrincipal(Principal p){
        principals.add(p);
    }

    public void removePrincipal(Principal p){
        principals.remove(p);
    }

    public List<StringRule> getStringRules() {
        return stringRules;
    }

    public void setStringRules(List<StringRule> stringRules) {
        this.stringRules = stringRules;
    }

    public List<IntegerRule> getIntegerRules() {
        return integerRules;
    }

    public void setIntegerRules(List<IntegerRule> integerRules) {
        this.integerRules = integerRules;
    }

    public List<DateRule> getDateRules() {
        return dateRules;
    }

    public void setDateRules(List<DateRule> dateRules) {
        this.dateRules = dateRules;
    }

}
