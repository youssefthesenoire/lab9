package Utility;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class MusicPlayer {
    private static final String MUSIC_DIR = "music/";
    private static final String START_MUSIC = "Sudoku_StartingGame.wav";
    private static final String INGAME_MUSIC = "Sudoku_Ingame.wav";
    private static final String SUCCESS_MUSIC = "Sudoku_FinishGameSuccessfully.wav";
    private Clip currentClip;

    public MusicPlayer() {
        // Constructor - no auto start
    }
    public boolean isPlaying() {
        return currentClip != null && currentClip.isRunning();
    }
    public static void checkMusicFiles() throws IOException {
        String[] files = {START_MUSIC, INGAME_MUSIC, SUCCESS_MUSIC};
        for (String fileName : files) {
            File musicFile = new File(MUSIC_DIR + fileName);
            if (!musicFile.exists()) {
                throw new IOException("Music file not found: " + MUSIC_DIR + fileName);
            }
        }
    }
    public void playStartingMusic() {
        playMusic(START_MUSIC, false);
    }
    public void playInGameMusic() {
        playMusic(INGAME_MUSIC, true);
    }
    public void playSuccessMusic() {
        playMusic(SUCCESS_MUSIC, false);
    }
    public void stopMusic() {
        if (currentClip != null && currentClip.isRunning()) {
            currentClip.stop();
            currentClip.close();
            currentClip = null;
        }
    }
    private void playMusic(String fileName, boolean loop) {
        try {
            stopMusic();
            File musicFile = new File(MUSIC_DIR + fileName);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);
            currentClip = AudioSystem.getClip();
            currentClip.open(audioStream);







































































































































































































            //idk idk
            if (loop) {
                currentClip.loop(Clip.LOOP_CONTINUOUSLY);
            }
            currentClip.start();
        } catch (Exception e) {
            System.out.println("Could not play music: " + fileName + " - " + e.getMessage());
        }
    }
}