package Interfaces;

import Exceptions.*;
import java.io.IOException;

public interface Controllable {
    boolean[] getCatalog();
    int[][] getGame(char level) throws NotFoundException;
    void driveGames(String sourcePath) throws SolutionInvalidException;
    boolean[][] verifyGame(int[][] game);
    int[][] solveGame(int[][] game) throws InvalidGameException;
    void logUserAction(UI.UserAction userAction) throws IOException;
}