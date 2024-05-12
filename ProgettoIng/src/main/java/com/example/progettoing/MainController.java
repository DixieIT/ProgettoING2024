package com.example.progettoing;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class MainController {

    private final Automa automaton;

    @FXML
    private TextField currentStateTextField;

    @FXML
    private TextField transitionInputTextField;

    @FXML
    private TextField nextStateTextField;

    public MainController(Automa automaton) {
        this.automaton = automaton;
    }

    @FXML
    private void printStates(ActionEvent event) {
        // Print the set of states of the automaton
        String statesString = automaton.printQ();
        System.out.println("Set of states: " + statesString);
    }

    @FXML
    private void printSize(ActionEvent event) {
        // Print the size of the automaton
        int size = automaton.getStateSize();
        System.out.println("Size of the automaton: " + size);
    }

    @FXML
    private void printFinalStates(ActionEvent event) {
        // Print the set of final states of the automaton
        String statesString = automaton.printF();
        System.out.println("Set of final states: " + statesString);
    }

    @FXML
    private void printDelta(ActionEvent event) {
        // Print the transition function Delta of the automaton
        automaton.printDelta();
    }

    @FXML
    private void AddState(ActionEvent event) {
        // Add a new state to the automaton
        automaton.AddState();
        System.out.println("Your Automaton has now: " + automaton.getStateSize() + " states");
    }

    @FXML
    private void AddFinalState(ActionEvent event) {
        // Add a new final state to the automaton
        automaton.AddFinalState();
        System.out.println("Your Automaton has now: " + automaton.getFinalStateSize() + " states");
    }

    @FXML
    private void addTransition(ActionEvent event) {
        // Retrieve currentState, transitionInput, and nextState from UI text fields
        String currentState = currentStateTextField.getText();
        String transitionInput = transitionInputTextField.getText();
        String nextState = nextStateTextField.getText();

        // Call addTransition with the retrieved values
        automaton.addTransition(currentState, transitionInput, nextState);
    }
}
