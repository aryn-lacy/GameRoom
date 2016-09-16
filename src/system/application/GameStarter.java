package system.application;

import data.game.entry.GameEntry;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import system.application.settings.PredefinedSetting;
import system.os.PowerMode;
import system.os.Terminal;
import ui.Main;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;

import static ui.Main.GENERAL_SETTINGS;
import static ui.Main.MAIN_SCENE;


/**
 * Created by LM on 14/07/2016.
 */
public class GameStarter {
    private static String STEAM_PREFIX = "steam";
    private GameEntry entry;
    private PowerMode originalPowerMode;


    public GameStarter(GameEntry entry) {
        this.entry = entry;
    }

    public void start() {
        Main.LOGGER.info("Starting game : " + entry.getName());
        originalPowerMode = PowerMode.getActivePowerMode();
        if (GENERAL_SETTINGS.getBoolean(PredefinedSetting.ENABLE_GAMING_POWER_MODE) && !entry.isAlreadyStartedInGameRoom() && !entry.isNotInstalled()) {
            GENERAL_SETTINGS.getPowerMode(PredefinedSetting.GAMING_POWER_MODE).activate();
        }
        String logFolder = "Games" + File.separator + entry.getUuid() + File.separator;
        File preLog = new File(logFolder + "pre_" + entry.getProcessName() + ".log");

        entry.setLastPlayedDate(new Date());

        Terminal terminal = new Terminal();
        String cmdBefore = entry.getCmd(GameEntry.CMD_BEFORE_START);
        String commandsBeforeString = GENERAL_SETTINGS.getStrings(PredefinedSetting.CMD)[GameEntry.CMD_BEFORE_START] + (cmdBefore != null ? "\n" + cmdBefore : "");
        String[] commandsBefore = commandsBeforeString.split("\n");
        if (entry.isSteamGame() || entry.getPath().startsWith(STEAM_PREFIX)) {
            terminal.execute(commandsBefore, preLog);
            try {
                String steamUri = entry.isNotInstalled() ? "steam://install/" + entry.getSteam_id() : entry.getPath();
                Desktop.getDesktop().browse(new URI(steamUri));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        } else {
            terminal.execute(commandsBefore, preLog, new File(new File(entry.getPath()).getParent()));
            String[] args = entry.getArgs().split(" ");
            ArrayList<String> commands = new ArrayList<>();
            commands.add('"' + entry.getPath() + '"');
            for (String arg : args) {
                commands.add(arg);
            }
            File gameLog = new File(logFolder + entry.getProcessName() + ".log");
            ProcessBuilder gameProcessBuilder = new ProcessBuilder(commands).inheritIO();
            gameProcessBuilder.redirectOutput(gameLog);
            gameProcessBuilder.redirectError(gameLog);
            gameProcessBuilder.directory(new File(new File(entry.getPath()).getParent()));
            try {
                Process gameProcess = gameProcessBuilder.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        Task<Long> monitor = new Task() {
            @Override
            protected Object call() throws Exception {
                if (GENERAL_SETTINGS.getOnLaunchAction(PredefinedSetting.ON_GAME_LAUNCH_ACTION).equals(OnLaunchAction.CLOSE)) {
                    Main.forceStop(MAIN_SCENE.getParentStage(),"launchAction = OnLaunchAction.CLOSE");
                } else if (GENERAL_SETTINGS.getOnLaunchAction(PredefinedSetting.ON_GAME_LAUNCH_ACTION).equals(OnLaunchAction.HIDE)) {
                    Main.LOGGER.debug("Hiding");
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            MAIN_SCENE.getParentStage().hide();
                        }
                    });
                }
                //This condition means that some thread is already monitoring and waiting for the game to be restarted, no need to monitor
                if (!entry.isAlreadyStartedInGameRoom()) {
                    entry.setAlreadyStartedInGameRoom(true);
                    Monitor timeMonitor = new Monitor(GameStarter.this);
                    return timeMonitor.start(null);
                }
                return new Long(-1);
            }
        };
        monitor.valueProperty().addListener(new ChangeListener<Long>() {
            @Override
            public void changed(ObservableValue<? extends Long> observable, Long oldValue, Long newValue) {
                if (!newValue.equals(new Long(-1))) {
                    Main.LOGGER.debug("Adding " + Math.round(newValue / 1000.0) + "s to game " + entry.getName());

                    String cmdAfter = entry.getCmd(GameEntry.CMD_AFTER_END);
                    String commandsAfterString = GENERAL_SETTINGS.getStrings(PredefinedSetting.CMD)[GameEntry.CMD_AFTER_END] + (cmdAfter != null ? "\n" + cmdAfter : "");
                    String[] commandsAfter = commandsAfterString.split("\n");
                    if (cmdAfter != null) {
                        File postLog = new File(logFolder + "post_" + entry.getProcessName() + ".log");
                        terminal.execute(commandsAfter, postLog, new File(new File(entry.getPath()).getParent()));
                    }
                    entry.setAlreadyStartedInGameRoom(false);
                    MAIN_SCENE.updateGame(entry);
                    if (!GENERAL_SETTINGS.getBoolean(PredefinedSetting.NO_NOTIFICATIONS) && newValue != 0) {
                        Main.TRAY_ICON.displayMessage("GameRoom"
                                , GameEntry.getPlayTimeFormatted(Math.round(newValue / 1000.0), GameEntry.TIME_FORMAT_HMS_CASUAL) + " "
                                        + Main.RESSOURCE_BUNDLE.getString("tray_icon_time_recorded") + " "
                                        + entry.getName(), TrayIcon.MessageType.INFO);
                    }

                } else {
                    //No need to add playtime as if we are here, it means that some thread is already monitoring play time
                }
            }
        });
        Thread th = new Thread(monitor);
        th.setDaemon(true);

        th.start();
    }

    public void onStop() {
        if (GENERAL_SETTINGS.getBoolean(PredefinedSetting.ENABLE_GAMING_POWER_MODE))
            originalPowerMode.activate();
    }

    public GameEntry getGameEntry() {
        return entry;
    }
}
