package com.example.progettoing;

import java.util.ArrayList;
import java.util.Arrays;

public class Automa {

    private String q_0;
    private ArrayList<String> Q;
    private ArrayList<String> F;
    private ArrayList<String> Sigma;
    private String[][] Delta; // Assuming Delta is a 2D array to represent the transition function

    public Automa(String q_0, String[] Q, String[] F, String[] Sigma, String[][] Delta) {
        this.q_0 = q_0;
        this.Q = new ArrayList<>(Arrays.asList(Q));
        this.F = new ArrayList<>(Arrays.asList(F));
        this.Sigma = new ArrayList<>(Arrays.asList(Sigma));
        this.Delta = Delta;
    }

    public String DeltaFunction(String currentState, String transitionInput) {
        // Find the row corresponding to currentState
        int row = Q.indexOf(currentState);
        if (row == -1) {
            // currentState not found in Q
            return null;
        }

        // Find the column corresponding to transitionInput
        int col = Sigma.indexOf(transitionInput);
        if (col == -1) {
            // transitionInput not found in Sigma
            return null;
        }

        // Get the next state from Delta
        return Delta[row][col];
    }

    public String printQ() {
        StringBuilder sb = new StringBuilder();
        // Append each state in the Q ArrayList to the StringBuilder
        for (String state : Q) {
            sb.append(state).append(", ");
        }
        // Remove the trailing comma and space if the list is not empty
        if (!Q.isEmpty()) {
            sb.delete(sb.length() - 2, sb.length());
        }
        return sb.toString();
    }

    public String printF() {
        StringBuilder sb = new StringBuilder();
        // Append each state in the F ArrayList to the StringBuilder
        for (String state : F) {
            sb.append(state).append(", ");
        }
        // Remove the trailing comma and space if the list is not empty
        if (!F.isEmpty()) {
            sb.delete(sb.length() - 2, sb.length());
        }
        return sb.toString();
    }

    public void printDelta() {
        System.out.println("Transition Function Delta:");
        for (int i = 0; i < Q.size(); i++) {
            for (int j = 0; j < Sigma.size(); j++) {
                System.out.println("Delta(" + Q.get(i) + ", " + Sigma.get(j) + ") = " + Delta[i][j]);
            }
        }
    }

    public void AddState() {
        Q.add("q" + Q.size());
    }

    public void AddFinalState() {
        F.add("q" + Q.size());
        Q.add("q" + Q.size());
    }

    public void addTransition(String currentState, String transitionInput, String nextState) {
        // Find the index of the current state in Q
        if (!Q.contains(nextState)) {
            System.out.println("Error: Next state is not in the set of states.");
            return;
        }

        int currentStateIndex = Q.indexOf(currentState);
        if (currentStateIndex == -1) {
            // currentState not found in Q
            System.out.println("Error: Current state not found.");
            return;
        }

        // Find the index of the transition input in Sigma
        int transitionInputIndex = Sigma.indexOf(transitionInput);
        if (transitionInputIndex == -1) {
            // transitionInput not found in Sigma
            System.out.println("Error: Transition input not found.");
            return;
        }

        // Update the transition in Delta
        Delta[currentStateIndex][transitionInputIndex] = nextState;
    }


    public int getStateSize() {
        return Q.size();
    }

    public int getFinalStateSize() {
        return F.size();
    }

}
