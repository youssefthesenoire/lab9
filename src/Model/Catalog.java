package Model;

public class Catalog {
    private boolean hasUnfinished;
    private boolean allModesExist;

    public Catalog(boolean hasUnfinished, boolean allModesExist) {
        this.hasUnfinished = hasUnfinished;
        this.allModesExist = allModesExist;
    }

    public boolean isHasUnfinished() {
        return hasUnfinished;
    }

    public boolean isAllModesExist() {
        return allModesExist;
    }
}