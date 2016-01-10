package CEP.customEvents;

import java.util.EventObject;

public class BoundaryEvent extends EventObject {
    private String boundary;
    public BoundaryEvent(Object source, String boundary){
        super(source);
        this.boundary=boundary;
    }

    public String getBoundary(){
        return boundary;
    }
}
