package com.gameroom.ui.dialog.selector;

import com.gameroom.data.http.images.ImageUtils;
import com.gameroom.data.game.entry.GameEntry;
import com.gameroom.data.game.scraper.OnDLDoneHandler;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import com.gameroom.ui.Main;
import com.gameroom.ui.GeneralToast;
import com.gameroom.ui.dialog.GameRoomDialog;
import com.gameroom.ui.pane.OnItemSelectedHandler;
import com.gameroom.ui.pane.SelectListPane;

import java.io.File;

import static com.gameroom.ui.Main.MAIN_SCENE;

/**
 * Created by LM on 06/08/2016.
 */
public class IGDBImageSelector extends GameRoomDialog<ButtonType> {
    private ImageList imageList;
    private String selectedImageHash;

    public IGDBImageSelector(GameEntry entry,OnItemSelectedHandler onImageSelected) {
        this(entry.getIgdb_imageHashs(), entry.getIgdb_id(), onImageSelected);
    }

    private IGDBImageSelector(String[] igdbScreenshots, int igdb_id, OnItemSelectedHandler onImageSelected) {
        super();
        Label titleLabel = new Label(Main.getString("select_a_wallpaper"));
        titleLabel.setPadding(new Insets(0 * Main.SCREEN_HEIGHT / 1080
                , 20 * Main.SCREEN_WIDTH / 1920
                , 20 * Main.SCREEN_HEIGHT / 1080
                , 20 * Main.SCREEN_WIDTH / 1920));
        mainPane.setTop(titleLabel);
        mainPane.setPadding(new Insets(30 * Main.SCREEN_HEIGHT / 1080
                , 30 * Main.SCREEN_WIDTH / 1920
                , 20 * Main.SCREEN_HEIGHT / 1080
                , 30 * Main.SCREEN_WIDTH / 1920));
        BorderPane.setAlignment(titleLabel, Pos.CENTER);

        mainPane.setPrefWidth(1.0 / 3 * Main.SCREEN_WIDTH);
        mainPane.setPrefHeight(2.0 / 3 * Main.SCREEN_HEIGHT);

        if(igdbScreenshots != null && igdbScreenshots.length>0) {
            imageList = new ImageList(igdb_id,mainPane.prefWidthProperty(),onImageSelected);
            imageList.addItems(igdbScreenshots);
            mainPane.setCenter(imageList);
            setOnHiding(event -> {
                selectedImageHash = ((String) imageList.getSelectedValue());
            });
        }else{
            mainPane.setCenter(new Label(Main.getString("no_screenshot_for_this_game")));
        }

        getDialogPane().getButtonTypes().addAll(new ButtonType(Main.getString("ok"), ButtonBar.ButtonData.OK_DONE)
                ,new ButtonType(Main.getString("cancel"),ButtonBar.ButtonData.CANCEL_CLOSE));
    }

    public String getSelectedImageHash() {
        return selectedImageHash;
    }

    private static class ImageList<String> extends SelectListPane{
        private ReadOnlyDoubleProperty prefRowWidth;
        private int igdb_id;
        private OnItemSelectedHandler onImageSelected;
        public ImageList(int igdb_id, ReadOnlyDoubleProperty prefRowWidth, OnItemSelectedHandler onImageSelected) {
            super();
            this.prefRowWidth = prefRowWidth;
            this.igdb_id = igdb_id;
            this.onImageSelected = onImageSelected;

            if(MAIN_SCENE != null){
                GeneralToast.displayToast(Main.getString("downloading_images"), MAIN_SCENE.getParentStage(), GeneralToast.DURATION_SHORT);
            }
        }

        @Override
        protected ListItem createListItem(Object value) {
            return new ImageItem(igdb_id, this, value,prefRowWidth);
        }

        @Override
        public void onItemSelected(ListItem item){
            onImageSelected.handle(item);
        }
    }
    private static class ImageItem<String> extends SelectListPane.ListItem{
        private Label loadingLabel = new Label(Main.getString("loading")+"...");
        private StackPane imageViewHolder = new StackPane();
        private ImageView imageView = new ImageView();
        private ReadOnlyDoubleProperty prefRowWidth;
        private int igdb_id;

        public ImageItem(int igdb_id,SelectListPane parentList,String value,ReadOnlyDoubleProperty prefRowWidth) {
            super(value,parentList);
            this.igdb_id = igdb_id;
            this.prefRowWidth = prefRowWidth;
            addContent();
        }

        @Override
        protected void addContent() {
            double prefTileWidth =Main.SCREEN_WIDTH * 1 / 3 * Main.SCREEN_WIDTH / 1920*0.7;
            double prefTileHeight = Main.SCREEN_HEIGHT * (prefTileWidth / Main.SCREEN_WIDTH);
            ImageUtils.downloadIGDBImageToCache(igdb_id
                    , (java.lang.String) getValue()
                    , ImageUtils.IGDB_TYPE_SCREENSHOT
                    , ImageUtils.IGDB_SIZE_MED
                    , new OnDLDoneHandler() {
                        @Override
                        public void run(File outputFile) {
                            Image img = new Image("file:"+ File.separator + File.separator + File.separator + outputFile.getAbsolutePath(), prefTileWidth, prefTileHeight, false, true);
                            ImageUtils.transitionToImage(img, imageView);
                        }
                    });
            prefWidthProperty().bind(prefRowWidth);
            imageViewHolder.getChildren().add(loadingLabel);
            imageViewHolder.getChildren().add(imageView);
            GridPane.setMargin(imageViewHolder, new Insets(10 * Main.SCREEN_HEIGHT / 1080, 0 * Main.SCREEN_WIDTH / 1920, 10 * Main.SCREEN_HEIGHT / 1080, 10 * Main.SCREEN_WIDTH / 1920));
            add(imageViewHolder, columnCount++, 0);
        }
    }

}
