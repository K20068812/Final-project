package categoryrules;

import principal_resource_attributes.DateAttribute;

import java.util.Date;
import java.util.Objects;

public class DateRule{
    private Date lowerBound;
    private Date upperBound;
    private DateAttribute attribute;

    public DateAttribute getAttribute() {
        return attribute;
    }

    public void setAttribute(DateAttribute attribute) {
        this.attribute = attribute;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DateRule that = (DateRule) o;
        return Objects.equals(lowerBound, that.lowerBound) &&
                Objects.equals(upperBound, that.upperBound) &&
                Objects.equals(attribute, that.attribute);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lowerBound, upperBound, attribute);
    }


    public DateRule(DateAttribute attribute, Date date1, Date date2){
        this.attribute = attribute;
        lowerBound = date1;
        upperBound = date2;
    }
    public DateRule(DateRule other) {
        this.lowerBound = new Date(other.lowerBound.getTime());
        this.upperBound = new Date(other.upperBound.getTime());
        this.attribute = new DateAttribute(other.attribute);
    }

    public Date getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(Date lowerBound) {
        this.lowerBound = lowerBound;
    }

    public Date getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(Date upperBound) {
        this.upperBound = upperBound;
    }

    @Override
    public String toString(){
        return attribute.getName().toString() +  "[ " + lowerBound.toString() + " - " + upperBound.toString() + " ]";
    }
}
