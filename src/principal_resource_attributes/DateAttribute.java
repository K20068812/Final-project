package principal_resource_attributes;


import java.util.Date;
import java.util.Objects;

public class DateAttribute {
    private Date value;
    private String name;

    public DateAttribute(String name, Date value) {
        this.name = name.toLowerCase();
        this.value = value;
    }
    public DateAttribute(DateAttribute other) {
        this.value = new Date(other.getValue().getTime());
        this.name = other.getName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.toLowerCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DateAttribute that = (DateAttribute) o;
        return Objects.equals(value, that.value) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, name);
    }

    public Date getValue() {
        return value;
    }

    public void setValue(Date value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Type: Date attribute | " + name.toString() + " | Value: " + value;
    }
}