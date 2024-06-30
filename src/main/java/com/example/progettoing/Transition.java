package com.example.progettoing;

public class Transition {
    private final String currentState;
    private final String input;
    private final String nextState;

    public Transition(String currentState, String input, String nextState) {
        this.currentState = currentState;
        this.input = input;
        this.nextState = nextState;
    }

    public String getCurrentState() {
        return currentState;
    }

    public String getInput() {
        return input;
    }

    public String getNextState() {
        return nextState;
    }
}