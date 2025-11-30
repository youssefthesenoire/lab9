package Service;

import Model.LocationOnBox;
import Model.Table;
import Model.ValidationResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public interface Interface {

    static ValidationResult validate(Table table) {
        Set<LocationOnBox> duplicateLocations = new HashSet<>();
        boolean isValid = true;
        // You must return a ValidationResult object, for example:
        return new ValidationResult(isValid, new ArrayList<>(duplicateLocations));
    }
}
