package Model;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {
    private final boolean valid;
    private final ArrayList<LocationOnBox> duplicateLocations;

    
    public ValidationResult(boolean valid, ArrayList<LocationOnBox> duplicateLocations) {
        this.valid = valid;
        this.duplicateLocations = duplicateLocations;

    }



    public boolean isValid() {
        return valid;
    }
    
    public ArrayList<LocationOnBox> getDuplicateLocations() {
        return duplicateLocations;
    }
}
