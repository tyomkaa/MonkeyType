package sample;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.File;

public class View {

    private Model model;
    private Controller controller;
    private MenuBar menuBar;
    private Label hotkeysInfo;
    private boolean tabPressed = false;
    private Label timerLabel;
    private TextFlow textFlow;
    private Rectangle cursor;
    private Label languageLabel; // Добавьте это в начале класса View


    public View(Stage primaryStage, Model model, Controller controller){

        this.model = model;
        this.controller = controller;

        // Создаем корневой элемент BorderPane
        BorderPane root = new BorderPane();

        // Создаем MenuBar
        MenuBar menuBar = new MenuBar();

        // Создаем меню для выбора языка
        Menu languageMenu = new Menu("Language");

        // Загружаем список языков из папки 'dictionary'
        File dictionaryDir = new File("src/dictionary");
        File[] languageFiles = dictionaryDir.listFiles((dir, name) -> name.endsWith(".txt"));

        if (languageFiles != null) {
            for (File file : languageFiles) {
                MenuItem languageItem = new MenuItem(file.getName().replace(".txt", ""));
                languageItem.setOnAction(e -> {
                    controller.setCurrentLanguage(file.getName().replace(".txt", ""));
                });
                languageMenu.getItems().add(languageItem);
            }
        }

        ///////////////////// Создаем меню для выбора времени
        Menu timeMenu = new Menu("Time");
        MenuItem time1 = new MenuItem("15 sec");
        MenuItem time2 = new MenuItem("20 sec");
        MenuItem time3 = new MenuItem("45 sec");
        MenuItem time4 = new MenuItem("60 sec");
        MenuItem time5 = new MenuItem("90 sec");
        MenuItem time6 = new MenuItem("120 sec");
        MenuItem time7 = new MenuItem("300 sec");

        // Добавляем пункты меню в меню выбора времени
        timeMenu.getItems().addAll(time1, time2, time3, time4, time5, time6, time7);

        // Добавляем созданные меню в MenuBar
        menuBar.getMenus().addAll(languageMenu, timeMenu);

        // Размещаем MenuBar в верхней части BorderPane
        root.setTop(menuBar);

        ///////////////////// Создаем Label для информации о горячих клавишах
        hotkeysInfo = new Label("Hotkeys:\n" +
                "Tab + Enter - Restart test\n" +
                "Ctrl + Shift + P - Pause\n" +
                "Esc - End test");
        hotkeysInfo.getStyleClass().add("hotkeys-info");

        // Создаем контейнер для хранения информации о горячих клавишах
        VBox bottomContainer = new VBox(hotkeysInfo);
        bottomContainer.getStyleClass().add("bottom-container");

        // Устанавливаем Label в нижнюю часть BorderPane
        root.setBottom(bottomContainer);

        ////////////////////////// Timer
        timerLabel = new Label("Time left: 00:30");
        VBox topContainer = new VBox(menuBar, timerLabel);
        root.setTop(topContainer);

        time1.setOnAction(e -> {
            controller.setTime(15);
            updateInitialTimerLabel(15); // Обновление метки таймера
        });
        time2.setOnAction(e -> {
            controller.setTime(20);
            updateInitialTimerLabel(20);
        });
        time3.setOnAction(e -> {
            controller.setTime(45);
            updateInitialTimerLabel(45);
        });
        time4.setOnAction(e -> {
            controller.setTime(60);
            updateInitialTimerLabel(60);
        });
        time5.setOnAction(e -> {
            controller.setTime(90);
            updateInitialTimerLabel(90);
        });
        time6.setOnAction(e -> {
            controller.setTime(120);
            updateInitialTimerLabel(120);
        });
        time7.setOnAction(e -> {
            controller.setTime(300);
            updateInitialTimerLabel(300);
        });

        ///////////////////////////// Text Area
        textFlow = new TextFlow();
        textFlow.getStyleClass().add("text-flow");

        textFlow.addEventFilter(KeyEvent.KEY_TYPED, keyEvent -> {
            if (!model.isTimerStarted()) { // Проверьте, был ли таймер уже запущен
                controller.startTest(); // Метод для начала теста и таймера
            }
        });

        setTextForTyping("Sample text");

        root.setCenter(textFlow);



        ///////////////////// Создаем сцену, добавляем в нее корневой элемент и отображаем на сцене
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Monkeytype Test");
        primaryStage.setScene(scene);
        primaryStage.show();
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        timerLabel.getStyleClass().add("timer-label");
        initializeCursor();
        setupChart();
    }

    private void updateInitialTimerLabel(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        timerLabel.setText(String.format("Time left: %02d:%02d", minutes, remainingSeconds));
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public MenuBar getMenuBar() {
        return menuBar;
    }

    public void initializeKeyHandlers(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode().isLetterKey() || keyEvent.getCode().isDigitKey() || keyEvent.getCode().isWhitespaceKey()) {

                if (!model.isTimerStarted()) {
                    controller.startTest();
                }

                controller.onUserInput(keyEvent.getText());
                keyEvent.consume();
            }

            if (keyEvent.getCode() == KeyCode.TAB) {
                tabPressed = true;
            } else if (keyEvent.getCode() == KeyCode.ENTER && tabPressed) {
                controller.restartTest();
                tabPressed = false;
                keyEvent.consume();
            } else if (keyEvent.getCode() == KeyCode.P && keyEvent.isControlDown() && keyEvent.isShiftDown()) {
                controller.pauseTest();
                keyEvent.consume();
            } else if (keyEvent.getCode() == KeyCode.ESCAPE) {
                controller.endTest();
                keyEvent.consume();
            } else if (keyEvent.getCode() == KeyCode.BACK_SPACE) {
                controller.onUserInput(KeyCode.BACK_SPACE.toString());
                keyEvent.consume();
            }


        });

        scene.addEventFilter(KeyEvent.KEY_RELEASED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.TAB) {
                tabPressed = false;
            }
        });
    }

    public void updateTimer(int timeSeconds) {
        int minutes = timeSeconds / 60;
        int seconds = timeSeconds % 60;
        timerLabel.setText(String.format("Time left: %02d:%02d", minutes, seconds));
    }

    public void setTextForTyping(String text) {
        textFlow.getChildren().clear();

        String[] words = text.split(" ");
        for (String word : words) {
            Text textNode = new Text(word + " ");
            textNode.setFill(Color.GRAY);
            textFlow.getChildren().add(textNode);
        }
    }

    public void updateTextFlow(String userInput, String expectedText) {
        textFlow.getChildren().clear();

        String[] words = expectedText.split("\\s+");
        String[] userInputs = userInput.split("\\s+", -1);

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            String userWord = i < userInputs.length ? userInputs[i] : "";

            for (int j = 0; j < word.length(); j++) {
                char expectedChar = word.charAt(j);
                char userChar = j < userWord.length() ? userWord.charAt(j) : '\0';

                Text charText = new Text(String.valueOf(expectedChar));

                if (userChar == expectedChar) {
                    charText.setFill(Color.GREEN); // Правильный символ
                } else if (userChar == '\0') {
                    charText.setFill(Color.GRAY); // Не введенный символ
                } else {
                    charText.setFill(Color.RED); // Неправильный символ
                }

                textFlow.getChildren().add(charText);
            }

            if (userWord.length() > word.length()) {
                String extraChars = userWord.substring(word.length());
                paintExtraInputCharacters(extraChars);
            }

            if (i < words.length - 1 || (i == words.length - 1 && userInput.endsWith(" "))) {
                Text space = new Text(" ");
                textFlow.getChildren().add(space);
            }
        }

        textFlow.getChildren().add(cursor);
        updateCursorPosition(userInput.length());
    }




    public void paintExtraInputCharacters(String extraChars) {
        for (char ch : extraChars.toCharArray()) {
            Text extraTextNode = new Text(String.valueOf(ch));
            extraTextNode.setFill(Color.ORANGE);
            textFlow.getChildren().add(extraTextNode);
        }
    }


    private void initializeCursor() {
        cursor = new Rectangle(2, 24);
        cursor.setFill(Color.YELLOW);
        cursor.setVisible(false);
    }

    public void updateCursorPosition(int position) {
        cursor.setVisible(true);

        if (position < textFlow.getChildren().size()) {
            textFlow.getChildren().remove(cursor);
            textFlow.getChildren().add(position, cursor);
        } else {
            textFlow.getChildren().remove(cursor);
            textFlow.getChildren().add(cursor);
        }
    }


    private LineChart<Number, Number> wpmChart;
    private XYChart.Series<Number, Number> wpmSeries;

    private LineChart<Number, Number> setupChart() {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time (seconds)");
        yAxis.setLabel("WPM");

        wpmChart = new LineChart<>(xAxis, yAxis);
        wpmChart.setTitle("Typing Test Results");
        wpmChart.setLegendVisible(false);

        wpmSeries = new XYChart.Series<>();
        wpmSeries.setName("WPM Over Time");

        wpmChart.getData().add(wpmSeries);

        return wpmChart;
    }



    public void updateChart(int time, int wpm) {
        Platform.runLater(() -> {
            if (wpmSeries != null) {
                wpmSeries.getData().add(new XYChart.Data<>(time, wpm));
            }
        });
    }


    public void showChart(int averageWPM, float accuracy, String testLanguage, int testTime, int correctCharacters, int incorrectCharacters, int extraCharacters) {

        VBox resultsBox = new VBox(10);
        resultsBox.setAlignment(Pos.CENTER);

        Label wpmLabel = new Label("Average WPM: " + averageWPM);
        Label accuracyLabel = new Label("Accuracy: " + accuracy + "%");
        Label languageLabel = new Label("Language: " + testLanguage);
        Label timeLabel = new Label("Time: " + testTime + " sec");
        Label correctLabel = new Label("Correct: " + correctCharacters);
        Label incorrectLabel = new Label("Incorrect: " + incorrectCharacters);
        Label extraLabel = new Label("Extra: " + extraCharacters);

        resultsBox.getChildren().addAll(wpmLabel, accuracyLabel, languageLabel, timeLabel, correctLabel, incorrectLabel, extraLabel);

        VBox chartAndResults = new VBox(20);
        chartAndResults.getChildren().addAll(wpmChart, resultsBox);

        Scene scene = new Scene(chartAndResults, 800, 600);
        primaryStage.setScene(scene);

        primaryStage.show();
        scene.getStylesheets().add(getClass().getResource("Result.css").toExternalForm());
    }


    private Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void closeWindow() {
        primaryStage.close();
    }
}
