package Model;

import java.util.List;

public class ValidationResult {
    private final boolean valid;
    private final List<LocationOnBox> duplicateLocations;
    
    public ValidationResult(boolean valid, List<LocationOnBox> duplicateLocations) {
        this.valid = valid;
        this.duplicateLocations = duplicateLocations;

    }



    public boolean isValid() {
        return valid;
    }
    
    public List<LocationOnBox> getDuplicateLocations() {
        return duplicateLocations;
    }
}
