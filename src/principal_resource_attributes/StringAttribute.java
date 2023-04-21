package principal_resource_attributes;


import java.util.Objects;

public class StringAttribute {
    private String value;
    private String name;

    public StringAttribute(String name, String value) {
        this.name = name.toLowerCase();
        this.value = value.toLowerCase();
    }
    public StringAttribute(StringAttribute other) {
        this.name = other.name;
        this.value = other.value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.toLowerCase();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value.toLowerCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringAttribute that = (StringAttribute) o;
        return Objects.equals(value, that.value) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, name);
    }

    @Override
    public String toString() {
        return "Type: String attribute | " + name.toString() + " | Value: " + value;
    }
}
