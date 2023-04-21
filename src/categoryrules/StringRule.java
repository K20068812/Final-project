package categoryrules;

import principal_resource_attributes.StringAttribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StringRule{

    private List<String> requirements;
    private StringAttribute attribute;

    public StringAttribute getAttribute() {
        return attribute;
    }

    public void setAttribute(StringAttribute attribute) {
        this.attribute = attribute;
    }

    public StringRule(StringRule other) {
        this.attribute = new StringAttribute(other.attribute);
        this.requirements = new ArrayList<>(other.requirements);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringRule that = (StringRule) o;
        return Objects.equals(requirements, that.requirements) &&
                Objects.equals(attribute, that.attribute);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requirements, attribute);
    }

    public StringRule(StringAttribute attribute, List<String> requirements){
        this.attribute = attribute;
        this.requirements = requirements;
    }

    public List<String> getRequirements(){
        return requirements;
    }

    public void setRequirements(List<String> requirements){
        this.requirements = requirements;
    }

    public void addRequirement(String s){
        requirements.add(s);
    }

    public void removeRequirement(String s){
        requirements.remove(s);
    }

    @Override
    public String toString(){
        return attribute.getName() + " = " + requirements.toString();
    }
}
