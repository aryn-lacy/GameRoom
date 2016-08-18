package ui.pane.gamestilepane;

import data.game.entry.GameEntry;
import ui.Main;
import ui.control.button.gamebutton.AddIgnoreGameButton;
import ui.control.button.gamebutton.GameButton;
import ui.scene.MainScene;

/**
 * Created by LM on 17/08/2016.
 */
public class ToAddRowTilePane extends RowCoverTilePane {
    public ToAddRowTilePane(MainScene parentScene) {
        super(parentScene, RowCoverTilePane.TYPE_RECENTLY_ADDED);
        setTitle(Main.RESSOURCE_BUNDLE.getString("to_add"));
        maxColumn = 10;
    }
    @Override
    protected GameButton createGameButton(GameEntry newEntry) {
        //TODO replace by a gameButton with and add and IGNORE BUTTON instead
        return new AddIgnoreGameButton(newEntry, parentScene, tilePane,this);

    }
}
