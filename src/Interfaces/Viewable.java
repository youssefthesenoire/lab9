package Interfaces;

import Model.*;
import Exceptions.*;

import java.io.IOException;

public interface Viewable {
    Catalog getCatalog();
    Game getGame(Difficulty level) throws NotFoundException;
    void driveGames(Game sourceGame) throws SolutionInvalidException;
    String verifyGame(Game game);
    int[] solveGame(Game game) throws InvalidGameException;
    void logUserAction(String userAction) throws IOException;
    void saveCurrentGame(Game game);
    void deleteCompletedGame(Game game);
}