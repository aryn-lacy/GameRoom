package system.application;

import javafx.application.Platform;
import system.os.PowerMode;
import ui.Main;

import java.io.*;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;

import static ui.scene.MainScene.MAX_SCALE_FACTOR;
import static ui.scene.MainScene.MIN_SCALE_FACTOR;

/**
 * Created by LM on 03/07/2016.
 */
public class GeneralSettings {
    private String locale = Locale.getDefault().toLanguageTag();
    //private boolean closeOnLaunch = false;
    private OnLaunchAction onLaunchAction = OnLaunchAction.DO_NOTHING;
    private double tileZoom = 0.4;
    private boolean fullScreen = false; //TODO fix fullscreen exiting after changing scene, see https://bugs.openjdk.java.net/browse/JDK-8089209
    private int windowWidth=640;
    private int windowHeight =480;
    private PowerMode gamingPowerMode=PowerMode.getActivePowerMode();
    private boolean enablePowerGamingMode = false;
    private boolean noMoreTrayMessage = false;
    private boolean minimizeOnStart = false;
    private boolean disableAllNotifications = false;

    public GeneralSettings(){
        loadSettings();
    }
    private void loadSettings(){
        Properties prop = new Properties();
        InputStream input = null;

        try {
            File configFile = new File("config.properties");
            if(!configFile.exists()){
                configFile.createNewFile();
                saveSettings();
            }

            input = new FileInputStream("config.properties");

            // load a properties file
            prop.load(input);

            if(prop.getProperty("locale")!=null){
                locale = prop.getProperty("locale");
            }
            if(prop.getProperty("tileZoom")!=null){
                tileZoom = Double.parseDouble(prop.getProperty("tileZoom"));
                if(tileZoom>MAX_SCALE_FACTOR){
                    tileZoom = MAX_SCALE_FACTOR;
                }else if(tileZoom < MIN_SCALE_FACTOR){
                    tileZoom = MIN_SCALE_FACTOR;
                }
            }
            if(prop.getProperty("fullScreen")!= null){
                fullScreen = Boolean.parseBoolean(prop.getProperty("fullScreen"));
            }
            if(prop.getProperty("windowWidth")!=null){
                windowWidth = Integer.parseInt(prop.getProperty("windowWidth"));
            }else{
                windowWidth = (int) Main.SCREEN_WIDTH;
            }
            if(prop.getProperty("windowHeight")!=null){
                windowHeight = Integer.parseInt(prop.getProperty("windowHeight"));
            }else{
                windowHeight = (int) Main.SCREEN_HEIGHT;
            }
            if(prop.getProperty("gamingPowerMode")!=null){
                gamingPowerMode = new PowerMode(UUID.fromString(prop.getProperty("gamingPowerMode")));
            }else{
                gamingPowerMode = PowerMode.getActivePowerMode();
                Main.logger.debug(gamingPowerMode);
            }
            if(prop.getProperty("enablePowerGamingMode")!=null){
                enablePowerGamingMode = Boolean.valueOf(prop.getProperty("enablePowerGamingMode"));
            }
            if(prop.getProperty("noMoreTrayMessage")!=null){
                noMoreTrayMessage = Boolean.valueOf(prop.getProperty("noMoreTrayMessage"));
            }

            if(prop.getProperty("onLaunchAction")!= null){
                onLaunchAction = OnLaunchAction.fromString(prop.getProperty("onLaunchAction"));
            }
            if(prop.getProperty("disableAllNotifications")!=null){
                disableAllNotifications = Boolean.valueOf(prop.getProperty("disableAllNotifications"));
            }
            if(prop.getProperty("minimizeOnStart")!=null){
                minimizeOnStart = Boolean.valueOf(prop.getProperty("minimizeOnStart"));
            }


        } catch (IOException ex) {
            ex.printStackTrace();

        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Main.logger.info("Loaded settings : "
                    +"windowWidth="+windowWidth
                    +", windowHeight="+ windowHeight
                    +", locale="+locale);
        }
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
        saveSettings();
    }

    public OnLaunchAction getOnLaunchAction() {
        return onLaunchAction;
    }

    public void setOnLaunchAction(OnLaunchAction onLaunchAction) {
        this.onLaunchAction = onLaunchAction;
        saveSettings();
    }

    public boolean isFullScreen() {
        return fullScreen;
    }

    public void setFullScreen(boolean fullScreen) {
        this.fullScreen = fullScreen;
        saveSettings();
    }

    public PowerMode getGamingPowerMode() {
        return gamingPowerMode;
    }

    public void setGamingPowerMode(PowerMode gamingPowerMode) {
        this.gamingPowerMode = gamingPowerMode;
        saveSettings();
    }

    public boolean isEnablePowerGamingMode() {
        return enablePowerGamingMode;
    }

    public void setEnablePowerGamingMode(boolean enablePowerGamingMode) {
        this.enablePowerGamingMode = enablePowerGamingMode;
        saveSettings();
    }
    public boolean isNoMoreTrayMessage() {
        return noMoreTrayMessage;
    }

    public void setNoMoreTrayMessage(boolean noMoreTrayMessage) {
        this.noMoreTrayMessage = noMoreTrayMessage;
        saveSettings();
    }

    public boolean isMinimizeOnStart() {
        return minimizeOnStart;
    }

    public void setMinimizeOnStart(boolean minimizeOnStart) {
        this.minimizeOnStart = minimizeOnStart;
        saveSettings();
    }

    public boolean isDisableAllNotifications() {
        return disableAllNotifications;
    }

    public void setDisableAllNotifications(boolean disableAllNotifications) {
        this.disableAllNotifications = disableAllNotifications;
        saveSettings();
    }

    public double getTileZoom() {
        return tileZoom;
    }

    public void setTileZoom(double tileZoom) {
        this.tileZoom = tileZoom;
        saveSettings();
    }

    public void saveSettings(){
        Properties prop = new Properties();
        OutputStream output = null;

        try {

            output = new FileOutputStream("config.properties");

            // set the properties value
            prop.setProperty("locale", locale);
            prop.setProperty("onLaunchAction", onLaunchAction.getRessourceKey());
            prop.setProperty("tileZoom", Double.toString(tileZoom));
            prop.setProperty("fullScreen", Boolean.toString(fullScreen));
            prop.setProperty("windowWidth", Integer.toString(windowWidth));
            prop.setProperty("windowHeight", Integer.toString(windowHeight));
            prop.setProperty("gamingPowerMode", gamingPowerMode.getUuid().toString());
            prop.setProperty("enablePowerGamingMode", Boolean.toString(enablePowerGamingMode));
            prop.setProperty("noMoreTrayMessage", Boolean.toString(noMoreTrayMessage));
            prop.setProperty("disableAllNotifications", Boolean.toString(disableAllNotifications));
            prop.setProperty("minimizeOnStart", Boolean.toString(minimizeOnStart));



            // save properties to project root folder
            prop.store(output, null);

        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(int windowWidth) {
        this.windowWidth = windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public void setWindowHeight(int windowHeight) {
        this.windowHeight = windowHeight;
    }

}