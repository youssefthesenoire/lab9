package Model;

import java.util.List;

public class VerificationResult {
    private GameState state;
    private List<LocationOnBoard> duplicateLocations;

    public VerificationResult(GameState state, List<LocationOnBoard> duplicateLocations) {
        this.state = state;
        this.duplicateLocations = duplicateLocations;
    }
    public GameState getState() {
        return state;
    }
    public List<LocationOnBoard> getDuplicateLocations() {
        return duplicateLocations;
    }
}