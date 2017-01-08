package system.application;

import data.game.entry.AllGameEntries;
import data.game.entry.GameEntry;
import data.game.scraper.SteamOnlineScraper;
import data.http.key.KeyChecker;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import system.application.settings.PredefinedSetting;
import ui.Main;
import ui.control.specific.GeneralToast;
import ui.dialog.GameRoomAlert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static ui.Main.GENERAL_SETTINGS;
import static ui.Main.LOGGER;
import static ui.Main.MAIN_SCENE;

/**
 * Created by LM on 07/01/2017.
 */
public class SupportService {
    private static SupportService INSTANCE;
    private final static long RUN_FREQ = TimeUnit.MINUTES.toMillis(2);
    private final static long DISPLAY_FREQUENCY = TimeUnit.DAYS.toMillis(30);
    private Thread thread;

    private SupportService(){
        thread = new Thread(() ->{
            while(Main.KEEP_THREADS_RUNNING){
                long start = System.currentTimeMillis();
                checkAndDisplaySupportAlert();
                scanSteamGamesTime();

                long elapsedTime = System.currentTimeMillis() - start;
                if(elapsedTime < RUN_FREQ){
                    try {
                        Thread.sleep(RUN_FREQ - elapsedTime);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        });
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.setDaemon(true);
    }

    private static SupportService getInstance(){
        if(INSTANCE == null){
            INSTANCE = new SupportService();
        }
        return INSTANCE;
    }

    public void startOrResume(){
        switch (thread.getState()){
            case NEW:
            case RUNNABLE:
                thread.start();
                break;
            case TIMED_WAITING:
                thread.interrupt();
                break;
            default:break;
        }
    }

    public static void start(){
        getInstance().startOrResume();
    }

    private void checkAndDisplaySupportAlert(){
        if(GENERAL_SETTINGS != null){
            if(!KeyChecker.assumeSupporterMode()){
                LOGGER.info("Checking if have to display support dialog");
                Date lastMessageDate = GENERAL_SETTINGS.getDate(PredefinedSetting.LAST_SUPPORT_MESSAGE);
                Date currentDate = new Date();

                long elapsedTime = currentDate.getTime() - lastMessageDate.getTime();

                if(elapsedTime >= DISPLAY_FREQUENCY){
                    Platform.runLater(() -> displaySupportAlert());
                    GENERAL_SETTINGS.setSettingValue(PredefinedSetting.LAST_SUPPORT_MESSAGE,new Date());
                }
            }
        }
    }

    private static void displaySupportAlert(){
        //TODO display a real alert
        GameRoomAlert alert = new GameRoomAlert(Alert.AlertType.INFORMATION,"Support us");
        alert.showAndWait();
    }

    private void scanSteamGamesTime() {
        try {
            ArrayList<GameEntry> ownedSteamApps = SteamOnlineScraper.getOwnedSteamGames();
            if(MAIN_SCENE!=null){
                GeneralToast.displayToast(Main.getString("scanning_steam_play_time"),MAIN_SCENE.getParentStage(),GeneralToast.DURATION_SHORT,true);
            }
            LOGGER.info("Scanning Steam playtimes online");
            for (GameEntry ownedEntry : ownedSteamApps) {
                if (ownedEntry.getPlayTimeSeconds() != 0) {
                    for (GameEntry storedEntry : AllGameEntries.ENTRIES_LIST) {
                        if (ownedEntry.getSteam_id() == storedEntry.getSteam_id() && ownedEntry.getPlayTimeSeconds() != storedEntry.getPlayTimeSeconds()) {
                            storedEntry.setPlayTimeSeconds(ownedEntry.getPlayTimeSeconds());
                            Platform.runLater(() -> {
                                Main.MAIN_SCENE.updateGame(storedEntry);
                            });
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
