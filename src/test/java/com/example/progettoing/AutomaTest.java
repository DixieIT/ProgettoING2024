package com.example.progettoing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AutomaTest {

    private Automa automaton;

    @BeforeEach
    public void setUp() {
        String q_0 = "q0";
        String[] Q = {"q0"};
        String[] F = {};
        String[] Sigma = {};
        String[][] Delta = {}; // Delta vuoto
        automaton = new Automa(q_0, Q, F, Sigma, Delta);
    }

    @Test
    public void testAddState() {
        automaton.addState();
        assertEquals(2, automaton.getStateSize(), "There should be 2 states after adding one");
        assertTrue(automaton.getQ().contains("q1"), "State 'q1' should be present in the state set");
    }

    @Test
    public void testAddString() {
        automaton.addString("c");
        assertTrue(automaton.getSigma().contains("c"), "Sigma should contain 'c'");
        automaton.addString("c");
        assertEquals(1, automaton.getSigma().size());
    }

    @Test
    public void testAddFinalState() {
        automaton.addFinalState();
        assertEquals(2, automaton.getStateSize(), "There should be 2 states after adding a final state");
        assertEquals(1, automaton.getFinalStateSize(), "There should be 1 final state");
    }

    @Test
    public void testAddTransition() {
        // Adding a transition to a non-existent state should fail
        int result = automaton.addTransition("q0", "a", "q1");
        assertEquals(-1, result, "Transition should not be added because 'q1' does not exist");

        // Add state and string, then add transition
        automaton.addState();
        automaton.addString("a");
        result = automaton.addTransition("q0", "a", "q1");
        assertEquals(1, result, "Transition should be added successfully");

        automaton.printDelta();
    }

    @Test
    public void testClearAll() {
        // Set up the automaton with states, final states, and transitions
        automaton.addString("a");
        automaton.addState();
        automaton.addFinalState();
        automaton.addTransition("q0", "a", "q1");

        // Clear all data and verify
        automaton.clearAll();
        assertEquals(1, automaton.getStateSize(), "Should have only the initial state after clearAll");
        assertEquals(0, automaton.getFinalStateSize(), "Should have no final states after clearAll");
        assertTrue(automaton.getSigma().isEmpty(), "Sigma should be empty after clearAll");
        assertTrue(automaton.getDelta().isEmpty(), "Delta should be empty after clearAll");
    }

    @Test
    public void testIsStringAccepted() {
        // Add states
        automaton.addState(); // q1
        automaton.addState(); // q2
        automaton.addFinalState(); //q3
        automaton.addFinalState(); // q4

        // Add strings to Sigma
        automaton.addString("aab");
        automaton.addString("aba");
        automaton.addString("abbb");
        automaton.addString("abb");
        automaton.addString("bb");
        automaton.addString("a");
        automaton.addString("b");

        // Add transitions
        automaton.addTransition("q0", "aab", "q1");
        automaton.addTransition("q0", "abbb", "q2");

        automaton.addTransition("q1", "b", "q2");
        automaton.addTransition("q1", "a", "q3");

        automaton.addTransition("q2", "aba", "q1");
        automaton.addTransition("q2", "b", "q3");

        automaton.addTransition("q3", "abb", "q3");
        automaton.addTransition("q3", "bb", "q4");

        automaton.addTransition("q4", "a", "q1");
        automaton.addTransition("q4", "b", "q2");

        // Test cases
        // Case 1: aababbbbb (Accepted)
        assertTrue(automaton.isStringAccepted("aababbbb"), "String 'aababbbb' should be accepted (q3 is final)");

        // Case 2: abbbaba (Rejected)
        assertFalse(automaton.isStringAccepted("abbbaba"), "String 'abbbaba' should be rejected (q2 is not final)");

        // Case 3: abbbabaaba (Rejected)
        assertFalse(automaton.isStringAccepted("abbbabaaba"), "String 'abbbabaaba' should be rejected (no path for 'abbbabaaba')");
    }
}
