package categories;


import java.util.Objects;

public class ResourceAction {
    private String name;
    private Resource resource;
    public ResourceAction(String name, Resource resource){
        this.name = name;
        this.resource = resource;
    }
    public ResourceAction(ResourceAction other) {
        this.name = other.name;
        this.resource = new Resource(other.resource);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceAction action = (ResourceAction) o;
        return Objects.equals(name, action.name) &&
                Objects.equals(resource, action.resource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, resource);
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    @Override
    public String toString(){
        return "Resource: " + resource.getName() + " action: " + name;
    }

}
