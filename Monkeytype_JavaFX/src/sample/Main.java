package sample;

import javafx.application.Application;
import javafx.stage.Stage;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        Model model = new Model();
        Controller controller = new Controller(model);
        View view = new View(primaryStage, model, controller);
        view.setPrimaryStage(primaryStage);
        controller.setView(view);

        controller.setTime(30);
        controller.loadTextForTyping("english.txt");

        view.initializeKeyHandlers(primaryStage.getScene());
        primaryStage.show();
    }



    public static void main(String[] args) {
        launch(args);
    }
}
