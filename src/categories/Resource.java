package categories;

import java.util.Objects;

public class Resource {
    private String name;

    public Resource(String name){
        this.name = name;
    }
    public Resource(Resource other) {
        this.name = other.name;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Resource resource = (Resource) o;
        return Objects.equals(name, resource.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

}
