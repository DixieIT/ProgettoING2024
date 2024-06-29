package com.example.progettoing;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Define the initial state
        String q_0 = "q0";

        // Define the set of states
        String[] Q = {"q0"};

        // Define the set of final states
        String[] F = {};

        // Define the alphabet
        String[] Sigma = {};

        // Define the transition function
        String[][] Delta = {
                {},
        };

        // Create an instance of the Automa class
        Automa automaton = new Automa(q_0, Q, F, Sigma, Delta);

        // Load the FXML file for the UI
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("hello-view.fxml"));

        // Set the controller and pass the automaton object to the controller
        MainController controller = new MainController(automaton);
        fxmlLoader.setController(controller);

        Scene scene = new Scene(fxmlLoader.load(), 1750, 1000);
        scene.getStylesheets().add(getClass().getResource("Main.css").toExternalForm());
        stage.setTitle("Automata");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}