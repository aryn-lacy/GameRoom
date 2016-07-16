package ui.control.button.gamebutton;

import ui.scene.BaseScene;
import data.game.GameEntry;
import javafx.scene.layout.Pane;

import static ui.Main.GENERAL_SETTINGS;

/**
 * Created by LM on 12/07/2016.
 */
public class InfoGameButton extends GameButton {
    private final static double RATIO_PLAYBUTTON_COVER = 1 / 3.0;

    public InfoGameButton(GameEntry entry, BaseScene scene, Pane parent) {
        super(entry, scene, parent);
        COVER_SCALE_EFFECT_FACTOR = 1.03;
        disableInfoButton();
        disablePlayTimeLabel();
        disableTitle();
        setOnMouseExited(eh ->{
            setFocused(false);
        });
        setOnMouseEntered(eh ->{
            setFocused(true);
        });
        scene.heightProperty().addListener(cl ->{
            //initAll();
        });
    }

    @Override
    protected int getCoverHeight() {
        return (int) (GENERAL_SETTINGS.getWindowHeight() *2/3);
    }

    @Override
    protected int getCoverWidth() {
        return (int) (GENERAL_SETTINGS.getWindowHeight() *2/(3* COVER_HEIGHT_WIDTH_RATIO));
    }

    @Override
    protected int getInfoButtonHeight() {
        return 1;
    }

    @Override
    protected int getInfoButtonWidth() {
        return 1;
    }

    @Override
    protected int getPlayButtonHeight() {
        return (int) (getCoverHeight()* RATIO_PLAYBUTTON_COVER);
    }

    @Override
    protected int getPlayButtonWidth() {
        return (int) (getCoverWidth()* RATIO_PLAYBUTTON_COVER);
    }
}