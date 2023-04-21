package categories;

import principal_resource_attributes.DateAttribute;
import principal_resource_attributes.IntegerAttribute;
import principal_resource_attributes.StringAttribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Principal {

    private List<StringAttribute> stringAttributeList;
    private List<IntegerAttribute> integerAttributeList;
    private List<DateAttribute> dateAttributeList;
    private String name;

    public Principal(String name){
        this.name = name;
        stringAttributeList = new ArrayList<>();
        integerAttributeList = new ArrayList<>();
        dateAttributeList = new ArrayList<>();
    }
    public Principal(Principal other) {
        this.name = other.name;
        this.stringAttributeList = new ArrayList<>();
        for (StringAttribute attribute : other.stringAttributeList) {
            this.stringAttributeList.add(new StringAttribute(attribute));
        }
        this.integerAttributeList = new ArrayList<>();
        for (IntegerAttribute attribute : other.integerAttributeList) {
            this.integerAttributeList.add(new IntegerAttribute(attribute));
        }
        this.dateAttributeList = new ArrayList<>();
        for (DateAttribute attribute : other.dateAttributeList) {
            this.dateAttributeList.add(new DateAttribute(attribute));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Principal principal = (Principal) o;
        return Objects.equals(name, principal.name) &&
                Objects.equals(stringAttributeList, principal.stringAttributeList) &&
                Objects.equals(integerAttributeList, principal.integerAttributeList) &&
                Objects.equals(dateAttributeList, principal.dateAttributeList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, stringAttributeList, integerAttributeList, dateAttributeList);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<StringAttribute> getStringAttributeList() {
        return stringAttributeList;
    }

    public void setStringAttributeList(List<StringAttribute> stringAttributeList) {
        this.stringAttributeList = stringAttributeList;
    }

    public List<IntegerAttribute> getIntegerAttributeList() {
        return integerAttributeList;
    }

    public void setIntegerAttributeList(List<IntegerAttribute> integerAttributeList) {
        this.integerAttributeList = integerAttributeList;
    }

    public List<DateAttribute> getDateAttributeList() {
        return dateAttributeList;
    }

    public void setDateAttributeList(List<DateAttribute> dateAttributeList) {
        this.dateAttributeList = dateAttributeList;
    }

}
