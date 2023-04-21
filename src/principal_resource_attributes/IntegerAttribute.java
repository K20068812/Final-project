package principal_resource_attributes;

import java.util.Objects;

public class IntegerAttribute {
    private int value;
    private String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntegerAttribute that = (IntegerAttribute) o;
        return value == that.value &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, name);
    }

    public IntegerAttribute(String name, int value) {
        this.name = name.toLowerCase();
        this.value = value;
    }
    public IntegerAttribute(IntegerAttribute other) {
        this.name = other.name;
        this.value = other.value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.toLowerCase();
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Type: Integer attribute | " + name.toString() + " | Value: " + value;
    }
}