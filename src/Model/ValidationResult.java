package Model;

import java.util.ArrayList;

public class ValidationResult {
    private final boolean valid;
    private final ArrayList<LocationOnBoard> duplicateLocations;

    public ValidationResult(boolean valid, ArrayList<LocationOnBoard> duplicateLocations) {
        this.valid = valid;
        this.duplicateLocations = duplicateLocations;

    }

    public boolean isValid() {
        return valid;
    }
    
    public ArrayList<LocationOnBoard> getDuplicateLocations() {
        return duplicateLocations;
    }
}
