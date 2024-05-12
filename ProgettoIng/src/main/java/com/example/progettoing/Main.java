package com.example.progettoing;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Define the initial state
        String q_0 = "q0";

        // Define the set of states
        String[] Q = {"q0", "q1", "q2"};

        // Define the set of final states
        String[] F = {"q2"};

        // Define the alphabet
        String[] Sigma = {"a", "b"};

        // Define the transition function
        String[][] Delta = {
                {"q1", "q0"},
                {"q2", "q0"},
                {"q2", "q2"}
        };

        // Create an instance of the Automa class
        Automa automaton = new Automa(q_0, Q, F, Sigma, Delta);

        // Load the FXML file for the UI
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("hello-view.fxml"));

        // Set the controller and pass the automaton object to the controller
        MainController controller = new MainController(automaton);
        fxmlLoader.setController(controller);

        Scene scene = new Scene(fxmlLoader.load(), 900, 500);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
