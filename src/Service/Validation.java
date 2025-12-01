package Service;

import Model.LocationOnBoard;
import Model.Table;
import Model.ValidationResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public interface Validation {

    static ValidationResult validate(Table table) {
        Set<LocationOnBoard> duplicateLocations = new HashSet<>();
        boolean isValid = true;
        return new ValidationResult(isValid, new ArrayList<>(duplicateLocations));
    }
}
