

package com.example.rollercoaster_physics;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class RollerCoasterUI extends Application {

    private Stage stage;

    private double mass         = 5.0;
    private double initVelocity = 8.0;
    private String objShape     = "Sphere";
    private String objMaterial  = "Metal";

    private static final Color BACKGROUND_DARK  = Color.rgb(8,   18,  40);
    private static final Color BACKGROUND_PANEL = Color.rgb(13,  27,  62);
    private static final Color BACKGROUND_CARD  = Color.rgb(18,  42,  90);
    private static final Color ACCENT_BLUE      = Color.rgb(30,  144, 255);
    private static final Color BORDER_SOFT      = Color.rgb(70,  130, 180);
    private static final Color TEXT_WHITE       = Color.WHITE;
    private static final Color TEXT_LIGHT       = Color.rgb(173, 216, 230);
    private static final Color TEXT_DIM         = Color.rgb(100, 149, 237);
    private static final Color TEXT_CYAN        = Color.rgb(147, 227, 227);
    private static final Color SIMULATION_BG    = Color.rgb(228, 238, 255);

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        stage.setTitle("Roller Coaster Physics Simulator");
        stage.setResizable(false);
        goToWelcome();
        stage.show();
    }

    private void goToWelcome() {

        VBox root = new VBox(28);
        root.setAlignment(Pos.CENTER);
        root.setBackground(new Background(new BackgroundFill(
                BACKGROUND_DARK, CornerRadii.EMPTY, Insets.EMPTY)));
        root.setPadding(new Insets(70));

        Label title = new Label("ROLLER COASTER");
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 58));
        title.setTextFill(ACCENT_BLUE);

        Label subtitle = new Label("Physics Simulator");
        subtitle.setFont(Font.font("Georgia", FontPosture.ITALIC, 28));
        subtitle.setTextFill(TEXT_LIGHT);

        Label edition = new Label("[ Educational Physics Engine v1.0 ]");
        edition.setFont(Font.font("Courier New", 13));
        edition.setTextFill(TEXT_DIM);

        Label authors = new Label(MainMenu.getAuthors());
        authors.setFont(Font.font("Georgia", FontPosture.ITALIC, 14));
        authors.setTextFill(TEXT_LIGHT);

        Button playButton = makeButton("PLAY", 16, 12, 40);
        Button exitButton = makeButton("EXIT", 12, 8, 18);

        HBox buttonRow = new HBox(20, playButton, exitButton);
        buttonRow.setAlignment(Pos.CENTER);

        playButton.setOnAction(e -> goToLevelSelect());
        exitButton.setOnAction(e -> stage.close());

        root.getChildren().addAll(title, subtitle, edition, authors, buttonRow);
        stage.setScene(new Scene(root, 1280, 800));
    }

    private void goToLevelSelect() {

        VBox root = new VBox(30);
        root.setAlignment(Pos.CENTER);
        root.setBackground(new Background(new BackgroundFill(
                BACKGROUND_DARK, CornerRadii.EMPTY, Insets.EMPTY)));
        root.setPadding(new Insets(50));

        Label heading = new Label("SELECT A LEVEL");
        heading.setFont(Font.font("Georgia", FontWeight.BOLD, 36));
        heading.setTextFill(ACCENT_BLUE);

        Label sub = new Label("Each level introduces new physics challenges");
        sub.setFont(Font.font("Courier New", FontWeight.BOLD, 14));
        sub.setTextFill(TEXT_LIGHT);

        HBox levelRow = new HBox(24);
        levelRow.setAlignment(Pos.CENTER);

        String[] names  = MainMenu.getLevelNames();
        String[] descs  = MainMenu.getLevelDescriptions();
        Color[]  colors = { TEXT_CYAN, ACCENT_BLUE, BORDER_SOFT };

        for (int i = 0; i < 3; i++) {
            int levelNumber = i + 1;

            VBox card = new VBox(10);
            card.setAlignment(Pos.CENTER);
            card.setPadding(new Insets(26, 22, 26, 22));
            card.setMinWidth(220);
            card.setBackground(new Background(new BackgroundFill(
                    BACKGROUND_CARD, new CornerRadii(10), Insets.EMPTY)));
            card.setBorder(new Border(new BorderStroke(
                    BORDER_SOFT, BorderStrokeStyle.SOLID,
                    new CornerRadii(10), new BorderWidths(1))));
            card.setCursor(Cursor.HAND);

            Label numLabel = new Label("LEVEL " + levelNumber);
            numLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 11));
            numLabel.setTextFill(colors[i]);

            Label nameLabel = new Label(names[i]);
            nameLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 18));
            nameLabel.setTextFill(TEXT_WHITE);

            Label descLabel = new Label(descs[i]);
            descLabel.setFont(Font.font("Courier New", 12));
            descLabel.setTextFill(TEXT_LIGHT);
            descLabel.setWrapText(true);
            descLabel.setAlignment(Pos.CENTER);

            card.getChildren().addAll(numLabel, nameLabel, descLabel);

            card.setOnMouseEntered(e -> card.setBorder(new Border(new BorderStroke(
                    ACCENT_BLUE, BorderStrokeStyle.SOLID,
                    new CornerRadii(10), new BorderWidths(2)))));
            card.setOnMouseExited(e -> card.setBorder(new Border(new BorderStroke(
                    BORDER_SOFT, BorderStrokeStyle.SOLID,
                    new CornerRadii(10), new BorderWidths(1)))));
            card.setOnMouseClicked(e -> goToSimulator(levelNumber));

            levelRow.getChildren().add(card);
        }

        Button backButton = makeButton("BACK", 12, 8, 18);
        backButton.setOnAction(e -> goToWelcome());

        root.getChildren().addAll(heading, sub, levelRow, backButton);
        stage.setScene(new Scene(root, 1280, 800));
    }

    private void goToSimulator(int level) {

        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(
                BACKGROUND_DARK, CornerRadii.EMPTY, Insets.EMPTY)));

        // build cart and track
        Cart.Material  material     = new Cart.Material(objMaterial, 0.2f);
        Vector2D       position     = new Vector2D(0, 0);
        Vector2D       velocity     = new Vector2D((float) initVelocity, 0f);
        Vector2D       acceleration = new Vector2D(0, 0);
        Cart.cartState state        = new Cart.cartState(position, velocity, acceleration);
        Cart  cart  = new Cart(mass, initVelocity, objShape, material, state);
        Track track = new Track(objMaterial);

        // LevelTest must be created first
        LevelTest levelTest = new LevelTest(cart, track);
        SimPanel  simPanel  = new SimPanel(cart, track, levelTest);

        // top bar
        HBox topBar = new HBox(12);
        topBar.setPadding(new Insets(10, 16, 10, 16));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setBackground(new Background(new BackgroundFill(
                BACKGROUND_PANEL, CornerRadii.EMPTY, Insets.EMPTY)));
        topBar.setBorder(new Border(new BorderStroke(
                ACCENT_BLUE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
                new BorderWidths(0, 0, 3, 0))));

        Button backButton = makeButton("LEVELS", 12, 8, 18);
        backButton.setOnAction(e -> {
            levelTest.stop();
            goToLevelSelect();
        });

        Label levelLabel = new Label("LEVEL " + level);
        levelLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 14));
        levelLabel.setTextFill(TEXT_CYAN);

        String[] levelNames = MainMenu.getLevelNames();
        Label nameLabel = new Label(levelNames[level - 1]);
        nameLabel.setFont(Font.font("Georgia", FontPosture.ITALIC, 13));
        nameLabel.setTextFill(TEXT_LIGHT);

        topBar.getChildren().addAll(backButton, levelLabel, nameLabel);
        root.setTop(topBar);

        // simulation area
        StackPane simulationPane = new StackPane();
        simulationPane.setPrefSize(980, 620);
        simulationPane.setBackground(new Background(new BackgroundFill(
                SIMULATION_BG, CornerRadii.EMPTY, Insets.EMPTY)));
        simulationPane.setBorder(new Border(new BorderStroke(
                BORDER_SOFT, BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY, new BorderWidths(1))));
        simulationPane.getChildren().add(levelTest.getView());
        root.setCenter(simulationPane);

        // left panel from SimPanel
        root.setLeft(simPanel.createPanel());

        // right formula panel
        VBox infoPanel = new VBox(12);
        infoPanel.setPadding(new Insets(16, 14, 16, 14));
        infoPanel.setPrefWidth(190);
        infoPanel.setBackground(new Background(new BackgroundFill(
                BACKGROUND_PANEL, CornerRadii.EMPTY, Insets.EMPTY)));
        infoPanel.setBorder(new Border(new BorderStroke(
                BORDER_SOFT, BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
                new BorderWidths(0, 0, 0, 4))));

        Label formulaTitle = new Label("FORMULAS");
        formulaTitle.setFont(Font.font("Courier New", FontWeight.BOLD, 12));
        formulaTitle.setTextFill(TEXT_CYAN);

        StringBuilder sb = new StringBuilder();
        for (Formula f : Formula.getAllFormulas()) {
            sb.append(f.getDisplay()).append("\n\n");
        }
        Label formulas = new Label(sb.toString().trim());
        formulas.setFont(Font.font("Courier New", 12));
        formulas.setTextFill(TEXT_DIM);
        formulas.setWrapText(true);

        Label vectorKey = new Label(
                "Green  = velocity\n" +
                        "Red    = gravity\n" +
                        "Yellow = normal\n" +
                        "Orange = friction"
        );
        vectorKey.setFont(Font.font("Courier New", 11));
        vectorKey.setTextFill(TEXT_LIGHT);

        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setMaxWidth(Double.MAX_VALUE);
        divider.setBackground(new Background(new BackgroundFill(
                Color.rgb(40, 70, 130), CornerRadii.EMPTY, Insets.EMPTY)));

        infoPanel.getChildren().addAll(formulaTitle, formulas, divider, vectorKey);
        root.setRight(infoPanel);

        stage.setScene(new Scene(root, 1280, 900));
    }

    private Button makeButton(String text, int fontSize, int padV, int padH) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Georgia", FontWeight.BOLD, fontSize));
        btn.setTextFill(TEXT_WHITE);
        btn.setBackground(new Background(new BackgroundFill(
                BACKGROUND_CARD, new CornerRadii(6), Insets.EMPTY)));
        btn.setBorder(new Border(new BorderStroke(
                ACCENT_BLUE, BorderStrokeStyle.SOLID,
                new CornerRadii(6), new BorderWidths(1))));
        btn.setPadding(new Insets(padV, padH, padV, padH));
        btn.setCursor(Cursor.HAND);
        return btn;
    }

    public static void main(String[] args) {
        launch(args);
    }
}