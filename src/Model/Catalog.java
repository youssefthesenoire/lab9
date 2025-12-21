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

    public void setHasUnfinished(boolean hasUnfinished) {
        this.hasUnfinished = hasUnfinished;
    }

    public boolean isAllModesExist() {
        return allModesExist;
    }

    public void setAllModesExist(boolean allModesExist) {
        this.allModesExist = allModesExist;
    }
}