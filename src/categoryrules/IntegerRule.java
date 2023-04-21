package categoryrules;

import principal_resource_attributes.IntegerAttribute;

import java.util.Objects;

public class IntegerRule {
    private int lowerBound;
    private int upperBound;
    private IntegerAttribute integerAttribute;

    public IntegerRule(IntegerRule other) {
        this.lowerBound = other.lowerBound;
        this.upperBound = other.upperBound;
        this.integerAttribute = new IntegerAttribute(other.integerAttribute);
    }

    public IntegerAttribute getAttribute() {
        return integerAttribute;
    }

    public void setAttribute(IntegerAttribute integerAttribute) {
        this.integerAttribute = integerAttribute;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntegerRule that = (IntegerRule) o;
        return lowerBound == that.lowerBound &&
                upperBound == that.upperBound &&
                Objects.equals(integerAttribute, that.integerAttribute);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lowerBound, upperBound, integerAttribute);
    }

    public IntegerRule(IntegerAttribute attribute, int lowerBound, int upperBound){
        this.integerAttribute = attribute;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public int getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(int lowerBound) {
        this.lowerBound = lowerBound;
    }

    public int getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(int upperBound) {
        this.upperBound = upperBound;
    }

    @Override
    public String toString(){
        return integerAttribute.getName() +  "[ " + getLowerBound() + " - " + getUpperBound() + " ]";
    }
}
