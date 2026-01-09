import javafx.application.Application;
import javafx.scene.*;
import javafx.stage.Stage;
import javafx.scene.control.ComboBox;

import javafx.scene.image.Image;

import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javafx.animation.TranslateTransition;
import javafx.util.Duration;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.SceneAntialiasing;

import javafx.animation.AnimationTimer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class MainApp extends Application {
    private double currentBallMass = 100;
    private BallMaterial currentMaterial = BallMaterial.METAL;

    public static final double WORLD_SIZE = 1000;

    private enum SpawnMode {
        ALL,
        ONE_BY_ONE
    }

    private SpawnMode spawnMode = SpawnMode.ALL;

    private int spawnedCount = 0;
    private double spawnTimer = 0;
    private static final double SPAWN_INTERVAL = 0.6;

    private int ballsToSpawn = 0;
    private int spawnedBalls = 0;

    private final Group root3d = new Group();
    private final List<Ball3D> balls = new ArrayList<>();
    private final Random rnd = new Random();

    private AnimationTimer timer;
    private long lastTimeNano = 0;

    private double mouseOldX, mouseOldY;
    private double cameraRotX = 0;
    private double cameraRotY = 0;

    private int ballCount = 20;
    private Color currentBallColor = Color.RED;

    private double currentBallRadius = 25.0;
    private Label hudMassLabel;


    @Override
    public void start(Stage stage) {

        SoundManager.init();
        stage.setFullScreen(true);

        BorderPane root = new BorderPane();
        StackPane stack = new StackPane(root);
        Scene scene = new Scene(stack, 900, 600);

        final int MENU_WIDTH = 300;

        VBox menu = new VBox(16);
        menu.setPadding(new Insets(120, 25, 25, 25));
        menu.setPrefWidth(MENU_WIDTH);
        menu.setAlignment(Pos.TOP_LEFT);

        menu.setStyle(
                "-fx-background-color: rgba(30,30,30,0.9);" +
                        "-fx-background-radius: 14;"
        );

        String styleMain =
                "-fx-background-color: #2c2c2c;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 16;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #777;" +
                        "-fx-border-width: 1;";

        String styleHover =
                "-fx-background-color: #444;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 16;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: white;" +
                        "-fx-border-width: 1.5;";

        Label lblCount = new Label("Количество шаров");
        lblCount.setTextFill(Color.WHITE);

        Slider countSlider = new Slider(1, 40, 20);
        countSlider.setShowTickLabels(true);
        countSlider.setShowTickMarks(true);
        countSlider.valueProperty().addListener((a,b,c) ->
                ballCount = c.intValue()
        );

        Label lblColor = new Label("Цвет шаров");

        Label lblMass = new Label("Масса шаров");
        lblMass.setTextFill(Color.WHITE);

        Label lblMaterial = new Label("Материал");
        lblMaterial.setTextFill(Color.WHITE);

        ComboBox<BallMaterial> materialBox = new ComboBox<>();
        materialBox.getItems().addAll(BallMaterial.values());
        materialBox.setValue(BallMaterial.METAL);

        materialBox.valueProperty().addListener((obs, oldV, newV) -> {
            currentMaterial = newV;
        });

        Slider massSlider = new Slider(10, 10000.0, 30.0);
        currentBallMass = massSlider.getValue();
        massSlider.setShowTickLabels(true);
        massSlider.setShowTickMarks(true);
        massSlider.setMajorTickUnit(1000);
        massSlider.setMinorTickCount(9);
        massSlider.setSnapToTicks(true);

        lblColor.setTextFill(Color.WHITE);

        ColorPicker colorPicker = new ColorPicker(Color.RED);
        lblMass.setTextFill(Color.WHITE);

        massSlider.setShowTickLabels(true);
        massSlider.setShowTickMarks(true);

        massSlider.valueProperty().addListener((a, b, c) -> {
            currentBallMass = c.doubleValue();
            if (hudMassLabel != null) {
                hudMassLabel.setText(
                        String.format("Масса шаров: %.0f кг", currentBallMass)
                );
            }
        });
        colorPicker.setOnAction(e ->
                currentBallColor = colorPicker.getValue()
        );

        Button btnAll = new Button("Все сразу");
        Button btnOne = new Button("По одному");

        Button btnStart = new Button("Старт");
        Button btnPause = new Button("Пауза");
        Button btnResume = new Button("Продолжить");
        Button btnRestart = new Button("Рестарт");

        Button[] buttons = {
                btnAll, btnOne,
                btnStart, btnPause, btnResume, btnRestart
        };

        for (Button b : buttons) {
            b.setStyle(styleMain);
            b.setOnMouseEntered(e -> {
                b.setStyle(styleHover);
                SoundManager.playHover();
            });
            b.setOnMouseExited(e -> b.setStyle(styleMain));
            b.setOnAction(e -> SoundManager.playClick());
        }

        btnAll.setOnAction(e -> {
            SoundManager.playClick();
            spawnMode = SpawnMode.ALL;
        });

        btnOne.setOnAction(e -> {
            SoundManager.playClick();
            spawnMode = SpawnMode.ONE_BY_ONE;
        });

        Label lblRadius = new Label("Размер шаров");
        lblRadius.setTextFill(Color.WHITE);

        Slider radiusSlider = new Slider(10, 80, 25);
        radiusSlider.setShowTickLabels(true);
        radiusSlider.setShowTickMarks(true);
        radiusSlider.setMajorTickUnit(10);
        radiusSlider.setMinorTickCount(4);
        radiusSlider.setSnapToTicks(true);

        radiusSlider.valueProperty().addListener((obs, oldV, newV) -> {
            currentBallRadius = newV.doubleValue();
        });

        menu.getChildren().addAll(
                lblCount, countSlider,

                btnAll, btnOne,

                lblColor, colorPicker,
                lblMass, massSlider,
                lblMaterial, materialBox,
                lblRadius, radiusSlider,

                btnStart, btnPause, btnResume, btnRestart
        );

        menu.setTranslateX(-MENU_WIDTH);
        root.setLeft(menu);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(6000);
        camera.setTranslateZ(-1600);

        SubScene subScene = new SubScene(
                root3d, 1920, 1080, true, SceneAntialiasing.BALANCED
        );
        subScene.setCamera(camera);
        subScene.setFill(Color.grayRgb(200));
        root.setCenter(subScene);

        hudMassLabel = new Label("Масса шаров: 100 кг");
        hudMassLabel.setTextFill(Color.WHITE);
        hudMassLabel.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-color: rgba(0,0,0,0.6);" +
                        "-fx-padding: 8 14;" +
                        "-fx-background-radius: 10;"
        );

        StackPane.setAlignment(hudMassLabel, Pos.BOTTOM_LEFT);
        StackPane.setMargin(hudMassLabel, new Insets(20));

        stack.getChildren().add(hudMassLabel);

        Button toggle = new Button("≡");
        toggle.setStyle(
                "-fx-background-color: rgba(0,0,0,0.6);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 20px;"
        );
        StackPane.setAlignment(toggle, Pos.TOP_LEFT);
        StackPane.setMargin(toggle, new Insets(20));
        stack.getChildren().add(toggle);

        TranslateTransition show = new TranslateTransition(Duration.millis(250), menu);
        show.setToX(0);
        TranslateTransition hide = new TranslateTransition(Duration.millis(250), menu);
        hide.setToX(-MENU_WIDTH);

        final boolean[] visible = {false};
        toggle.setOnAction(e -> {
            if (visible[0]) hide.play();
            else show.play();
            visible[0] = !visible[0];
        });

        subScene.setOnMousePressed(e -> {
            mouseOldX = e.getSceneX();
            mouseOldY = e.getSceneY();
        });

        subScene.setOnMouseDragged(e -> {
            cameraRotY += (e.getSceneX() - mouseOldX) * 0.2;
            cameraRotX -= (e.getSceneY() - mouseOldY) * 0.2;

            camera.getTransforms().setAll(
                    new Rotate(cameraRotY, Rotate.Y_AXIS),
                    new Rotate(cameraRotX, Rotate.X_AXIS)
            );

            mouseOldX = e.getSceneX();
            mouseOldY = e.getSceneY();
        });

        AmbientLight ambient = new AmbientLight(Color.color(0.4,0.4,0.4));
        PointLight light = new PointLight(Color.WHITE);
        light.setTranslateY(-300);
        light.setTranslateZ(-800);

        Box floor = new Box(WORLD_SIZE, 10, WORLD_SIZE);
        PhongMaterial floorMat = new PhongMaterial();
        floorMat.setDiffuseMap(new Image("file:floor.jpg"));
        floor.setMaterial(floorMat);
        floor.setTranslateY(WORLD_SIZE / 2);

        root3d.getChildren().addAll(ambient, light, floor);

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTimeNano == 0) {
                    lastTimeNano = now;
                    return;
                }

                double dt = (now - lastTimeNano) / 1e9;
                lastTimeNano = now;

                if (spawnMode == SpawnMode.ONE_BY_ONE && spawnedCount < ballCount) {
                    spawnTimer += dt;

                    if (spawnTimer >= SPAWN_INTERVAL) {
                        spawnBall();
                        spawnedCount++;
                        spawnTimer = 0;
                    }
                }

                Physics3D.handleCollisions(balls);
                for (Ball3D b : balls) {
                    b.update(dt);
                }
            }
        };

        btnStart.setOnAction(e -> {
            SoundManager.playClick();
            timer.stop();

            balls.clear();
            root3d.getChildren().removeIf(n -> n instanceof Sphere);

            spawnedCount = 0;
            spawnTimer = 0;

            if (spawnMode == SpawnMode.ALL) {
                for (int i = 0; i < ballCount; i++) {
                    spawnBall();
                }
                spawnedCount = ballCount;
            }

            lastTimeNano = System.nanoTime();
            timer.start();
        });

        btnPause.setOnAction(e -> timer.stop());

        btnResume.setOnAction(e -> {
            lastTimeNano = System.nanoTime();
            timer.start();
        });

        btnRestart.setOnAction(e -> {
            timer.stop();
            balls.clear();
            root3d.getChildren().removeIf(n -> n instanceof Sphere);
            spawnedCount = 0;
            spawnTimer = 0;
            lastTimeNano = System.nanoTime();
            timer.start();
        });

        stage.setScene(scene);
        stage.setTitle("JavaFX 3D Physics");
        stage.show();
    }

    private void spawnBall() {

        Ball3D b = new Ball3D(
                (rnd.nextDouble() - 0.5) * 600,
                -WORLD_SIZE / 2 + rnd.nextDouble() * 300,
                (rnd.nextDouble() - 0.5) * 600,
                (rnd.nextDouble() - 0.5) * 200,
                0,
                (rnd.nextDouble() - 0.5) * 200,
                currentBallRadius,
                currentBallMass,
                currentMaterial,
                currentBallColor
        );

        balls.add(b);
        root3d.getChildren().add(b.sphere);
        hudMassLabel.setText(
                String.format(
                        "Материал: %s | Масса: %.0f кг",
                        currentMaterial.name(),
                        b.mass
                )
        );
    }
    public static void main(String[] args) {
        launch(args);
    }
}