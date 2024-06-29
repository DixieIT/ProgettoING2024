package com.example.progettoing;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.control.TextField;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;

public class MainController implements Initializable {

    private final Automa automaton;
    @FXML
    private AnchorPane mainAnchorPane;

    @FXML
    private AnchorPane stateAnchorPane;

    @FXML
    private StackPane q0;
    @FXML
    private TextField currentStateTextField;
    @FXML
    private TextField newStringTextField;
    @FXML
    private TextField transitionInputTextField;
    @FXML
    private TextField nextStateTextField;

    @FXML
    private TextField sigmaTextField;

    @FXML
    private TableView<Transition> deltaTableView;
    @FXML
    private TableColumn<Transition, String> currentStateColumn;
    @FXML
    private TableColumn<Transition, String> inputColumn;
    @FXML
    private TableColumn<Transition, String> nextStateColumn;
    @FXML
    private TextField testStringTextField;
    @FXML
    private Label resultLabel;

    private List<StackPane> stackPanes;
    private Map<String, QuadCurve> transitionCurves = new HashMap<>();
    private Map<String, CubicCurve> selfTransitionCurves = new HashMap<>();
    private Map<String, Text> transitionTexts = new HashMap<>();
    private Map<String, Polygon> transitionArrows = new HashMap<>();
    private final DraggableMaker draggableMaker = new DraggableMaker();

    public MainController(Automa automaton) {
        this.automaton = automaton;
    }

    @FXML
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        stackPanes = new ArrayList<>(Collections.singletonList(q0));


        Platform.runLater(() -> {
            Stage primaryStage = (Stage)mainAnchorPane.getScene().getWindow();
            primaryStage.setMaximized(true);
        });

        double layoutX = q0.getLayoutX();
        double layoutY = q0.getLayoutY();
        System.out.println("Initial State (q0) LayoutX: " + layoutX + ", LayoutY: " + layoutY);

        addMovementListeners(q0);

        // Initialize the delta table columns
        currentStateColumn.setCellValueFactory(new PropertyValueFactory<>("currentState"));
        inputColumn.setCellValueFactory(new PropertyValueFactory<>("input"));
        nextStateColumn.setCellValueFactory(new PropertyValueFactory<>("nextState"));
    }

    private void addMovementListeners(StackPane statePane) {
        ChangeListener<Number> listener = (obs, oldVal, newVal) -> updateTransitions();
        statePane.layoutXProperty().addListener(listener);
        statePane.layoutYProperty().addListener(listener);
    }

    private void updateTransitions() {
        for (Map.Entry<String, QuadCurve> entry : transitionCurves.entrySet()) {
            String key = entry.getKey();
            QuadCurve curve = entry.getValue();
            Text text = transitionTexts.get(key);
            Polygon arrow = transitionArrows.get(key);
            String[] parts = key.split("->");
            String[] states = parts[0].split(",");
            StackPane fromState = findStateById(states[0]);
            StackPane toState = findStateById(states[1]);

            if (fromState != null && toState != null && !states[0].equals(states[1])) {
                double startX = fromState.getLayoutX() + fromState.getWidth() / 2;
                double startY = fromState.getLayoutY() + fromState.getHeight() / 2;
                double endX = toState.getLayoutX() + toState.getWidth() / 2;
                double endY = toState.getLayoutY() + toState.getHeight() / 2;

                double[] endPoint = calculateEndPoint(startX, startY, endX, endY, 46);

                // Increase the control point distance for a more pronounced curve
                double controlX = (startX + endPoint[0]) / 2;
                double controlY = (startY + endPoint[1]) / 2 + (endX > startX ? -100 : 100);

                curve.setStartX(startX);
                curve.setStartY(startY);
                curve.setEndX(endPoint[0]);
                curve.setEndY(endPoint[1]);
                curve.setControlX(controlX);
                curve.setControlY(controlY);

                if (text != null) {
                    double offsetX = -10;
                    double offsetY = -40; // Slightly negative to move it closer
                    double midX = (startX + endPoint[0]) / 2;
                    double midY = (startY + endPoint[1]) / 2;
                    double angle = Math.atan2(endPoint[1] - startY, endPoint[0] - startX);
                    text.setX(midX + offsetX * Math.cos(angle));
                    text.setY(midY + offsetY * Math.sin(angle));
                }

                if (arrow != null) {
                    updateArrowhead(arrow, endPoint[0], endPoint[1], startX + 10, startY);
                }

                curve.toBack();
                if (text != null) text.toBack();
                if (arrow != null) arrow.toBack();
            }
        }

        for (Map.Entry<String, CubicCurve> entry : selfTransitionCurves.entrySet()) {
            String key = entry.getKey();
            CubicCurve curve = entry.getValue();
            String[] parts = key.split("->");
            String[] states = parts[0].split(",");
            StackPane fromState = findStateById(states[0]);

            if (fromState != null && states[0].equals(states[1])) {
                updateSelfTransitionLoop(curve, fromState, parts[1]);
                curve.toBack();
            }
        }
    }



    private void updateSelfTransitionLoop(CubicCurve curve, StackPane state, String input) {
        double centerX = state.getLayoutX() + state.getWidth() / 2;
        double centerY = state.getLayoutY() + state.getHeight() / 2;

        double loopRadius = 40;
        double stateRadius = 46;

        curve.setStartX(centerX - stateRadius);
        curve.setStartY(centerY);
        curve.setEndX(centerX + stateRadius);
        curve.setEndY(centerY);

        // Increase the control point distance for a more pronounced curve
        curve.setControlX1(centerX - loopRadius - stateRadius - 60); // Move control point further left
        curve.setControlY1(centerY - loopRadius - stateRadius - 60); // Move control point higher
        curve.setControlX2(centerX + loopRadius - 60); // Move control point further right
        curve.setControlY2(centerY - loopRadius - 60); // Move control point higher

        curve.setStroke(Color.BLACK);
        curve.setFill(Color.TRANSPARENT);

        String textKey = state.getId() + "->" + input;
        Text labelText = transitionTexts.get(textKey);
        if (labelText == null) {
            // Create and add the label if it does not exist
            labelText = new Text(input);
            stateAnchorPane.getChildren().add(labelText);
            transitionTexts.put(textKey, labelText);
        }

        labelText.setText(input);
        labelText.setX(centerX);
        labelText.setY(centerY - stateRadius - loopRadius - 5); // Position closer to the loop
    }

    private double[] calculateEndPoint(double startX, double startY, double endX, double endY, double radius) {
        double angle = Math.atan2(endY - startY, endX - startX);
        double adjustedEndX = endX - radius * Math.cos(angle);
        double adjustedEndY = endY - radius * Math.sin(angle);
        return new double[]{adjustedEndX, adjustedEndY};
    }

    private StackPane findStateById(String stateId) {
        for (StackPane stackPane : stackPanes) {
            if (stateId.equals(stackPane.getId())) {
                return stackPane;
            }
        }
        return null;
    }

    @FXML
    private void handleAddState(ActionEvent event) {
        StackPane stackPane = createStatePane(false);
        mainAnchorPane.getChildren().add(stackPane);
        automaton.addState();
        System.out.println(automaton.getQ().size() + "STATI");
    }

    @FXML
    private void handleAddFinalState(ActionEvent event) {
        StackPane stackPane = createStatePane(true);
        mainAnchorPane.getChildren().add(stackPane);
        automaton.addFinalState();
    }

    private StackPane createStatePane(boolean isFinalState) {
        StackPane stackPane = new StackPane();
        stackPane.setLayoutX(300);
        stackPane.setLayoutY(300);

        Circle outerCircle = new Circle(46, Color.WHITE);
        outerCircle.setStroke(Color.BLACK);
        outerCircle.setStrokeWidth(1);

        stackPane.getChildren().add(outerCircle);

        if (isFinalState) {
            Circle innerCircle = new Circle(40, Color.TRANSPARENT);
            innerCircle.setStroke(Color.BLACK);
            innerCircle.setStrokeWidth(1);
            stackPane.getChildren().add(innerCircle);
        }

        String stateId = "q" + automaton.getStateSize();
        TextField textField = new TextField(stateId);
        textField.setMaxWidth(30);
        textField.setAlignment(Pos.CENTER);
        textField.setId("StateText");

        stackPane.setId(stateId);
        stackPanes.add(stackPane);
        stackPane.getChildren().add(textField);

        draggableMaker.makeDraggable(stackPane, stateAnchorPane);
        addMovementListeners(stackPane);

        return stackPane;
    }


    @FXML
    private void handleAddTransition(ActionEvent event) {
        //!
        String currentState = currentStateTextField.getText().trim();
        String transitionInput = transitionInputTextField.getText().trim();
        String nextState = nextStateTextField.getText().trim();

        if (!automaton.getSigma().contains(transitionInput) ||
                !automaton.getQ().contains(currentState) ||
                !automaton.getQ().contains(nextState)) {
            System.err.println("Invalid transition input or state.");
            return;
        }

        if (automaton.addTransition(currentState, transitionInput, nextState) == -1) {
            System.err.println("Failed to add transition.");
            return;
        }

        System.out.println("Added transition Delta(" + currentState + ", " + transitionInput + ") = " + nextState);

        regenerateTransitions();
        populateDeltaTable();

        currentStateTextField.clear();
        transitionInputTextField.clear();
        nextStateTextField.clear();
    }


    private void clearTransitions() {
        for (QuadCurve curve : transitionCurves.values()) {
            mainAnchorPane.getChildren().remove(curve);
        }
        for (CubicCurve curve : selfTransitionCurves.values()) {
            mainAnchorPane.getChildren().remove(curve);
        }
        for (Text text : transitionTexts.values()) {
            mainAnchorPane.getChildren().remove(text);
        }
        for (Polygon arrow : transitionArrows.values()) {
            mainAnchorPane.getChildren().remove(arrow);
        }
        transitionCurves.clear();
        selfTransitionCurves.clear();
        transitionTexts.clear();
        transitionArrows.clear();
    }


    private void regenerateTransitions() {
        clearTransitions();

        Map<String, Map<String, String>> delta = automaton.getDelta();
        Map<String, String> concatenatedTransitions = new HashMap<>();

        for (String currentState : delta.keySet()) {
            for (String input : delta.get(currentState).keySet()) {
                String nextState = delta.get(currentState).get(input);
                String key = currentState + "," + nextState;

                if (concatenatedTransitions.containsKey(key)) {
                    concatenatedTransitions.put(key, concatenatedTransitions.get(key) + ", " + input);
                } else {
                    concatenatedTransitions.put(key, input);
                }
            }
        }

        for (Map.Entry<String, String> entry : concatenatedTransitions.entrySet()) {
            String key = entry.getKey();
            String[] states = key.split(",");
            String currentState = states[0];
            String nextState = states[1];
            String inputs = entry.getValue();

            StackPane currentStatePane = findStateById(currentState);
            StackPane nextStatePane = findStateById(nextState);

            if (currentStatePane != null && nextStatePane != null) {
                if (currentState.equals(nextState)) {
                    String selfLoopKey = key + "->" + inputs;
                    CubicCurve selfLoop = selfTransitionCurves.get(selfLoopKey);
                    if (selfLoop == null) {
                        selfLoop = createSelfTransitionLoop(currentStatePane, inputs);
                        mainAnchorPane.getChildren().add(selfLoop);
                        selfTransitionCurves.put(selfLoopKey, selfLoop);
                    } else {
                        updateSelfTransitionLoop(selfLoop, currentStatePane, inputs);
                    }
                } else {
                    String transitionKey = key + "->" + inputs;
                    QuadCurve transitionCurve = transitionCurves.get(transitionKey);
                    if (transitionCurve == null) {
                        transitionCurve = createTransitionCurve(currentStatePane, nextStatePane);
                        Text transitionText = createTransitionText(transitionCurve, inputs);
                        Polygon arrow = createArrowhead(transitionCurve);
                        mainAnchorPane.getChildren().addAll(transitionCurve, transitionText, arrow);
                        transitionCurves.put(transitionKey, transitionCurve);
                        transitionTexts.put(transitionKey, transitionText);
                        transitionArrows.put(transitionKey, arrow);
                    } else {
                        updateTransitionCurve(transitionCurve, currentStatePane, nextStatePane, inputs);
                    }
                }
            }
        }

        for (StackPane stackPane : stackPanes) {
            mainAnchorPane.getChildren().remove(stackPane);
            mainAnchorPane.getChildren().add(stackPane);
        }
    }







    private void updateTransitionCurve(QuadCurve curve, StackPane fromState, StackPane toState, String input) {
        double startX = fromState.getLayoutX() + fromState.getWidth() / 2;
        double startY = fromState.getLayoutY() + fromState.getHeight() / 2;
        double endX = toState.getLayoutX() + toState.getWidth() / 2;
        double endY = toState.getLayoutY() + toState.getHeight() / 2;

        double[] endPoint = calculateEndPoint(startX, startY, endX, endY, 46);

        double controlX = (startX + endPoint[0]) / 2;
        double controlY = (startY + endPoint[1]) / 2 + (endX > startX ? -50 : 50);

        curve.setStartX(startX);
        curve.setStartY(startY);
        curve.setEndX(endPoint[0]);
        curve.setEndY(endPoint[1]);
        curve.setControlX(controlX);
        curve.setControlY(controlY);

        String textKey = fromState.getId() + "," + toState.getId() + "->" + input;
        Text text = transitionTexts.get(textKey);
        if (text != null) {
            double offsetX = 10;
            double offsetY = 10;
            double midX = (startX + endPoint[0]) / 2;
            double midY = (startY + endPoint[1]) / 2;
            double angle = Math.atan2(endPoint[1] - startY, endPoint[0] - startX);
            text.setX(midX + offsetX * Math.cos(angle));
            text.setY(midY + offsetY * Math.sin(angle));
        }

        Polygon arrow = transitionArrows.get(textKey);
        if (arrow != null) {
            updateArrowhead(arrow, endPoint[0], endPoint[1], startX, startY);
        }
    }




    private CubicCurve createSelfTransitionLoop(StackPane state, String input) {
        double centerX = state.getLayoutX() + state.getWidth() / 2;
        double centerY = state.getLayoutY() + state.getHeight() / 2;

        CubicCurve curve = new CubicCurve();
        double loopRadius = 40;
        double stateRadius = 46;

        curve.setStartX(centerX - stateRadius);
        curve.setStartY(centerY);
        curve.setEndX(centerX + stateRadius);
        curve.setEndY(centerY);

        // Increase the control point distance for a more pronounced curve
        curve.setControlX1(centerX - loopRadius - stateRadius - 60); // Move control point further left
        curve.setControlY1(centerY - loopRadius - stateRadius - 60); // Move control point higher
        curve.setControlX2(centerX + loopRadius - 60); // Move control point further right
        curve.setControlY2(centerY - loopRadius - 60); // Move control point higher

        curve.setStroke(Color.BLACK);
        curve.setFill(Color.TRANSPARENT);

        Text labelText = new Text(input);
        labelText.setX(centerX);
        labelText.setY(centerY - stateRadius - loopRadius - 5); // Position closer to the loop
        labelText.setFill(Color.RED);

        mainAnchorPane.getChildren().add(labelText);
        transitionTexts.put(state.getId() + "->" + input, labelText);

        return curve;
    }




    private QuadCurve createTransitionCurve(StackPane fromState, StackPane toState) {
        double startX = fromState.getLayoutX() + fromState.getWidth() / 2;
        double startY = fromState.getLayoutY() + fromState.getHeight() / 2;
        double endX = toState.getLayoutX() + toState.getWidth() / 2;
        double endY = toState.getLayoutY() + toState.getHeight() / 2;

        double[] endPoint = calculateEndPoint(startX, startY, endX, endY, 46);

        // Increase the control point distance for a more pronounced curve
        double controlX = (startX + endPoint[0]) / 2;
        double controlY = (startY + endPoint[1]) / 2 + (endX > startX ? -100 : 100);

        QuadCurve curve = new QuadCurve(startX, startY, controlX, controlY, endPoint[0], endPoint[1]);
        curve.setStroke(Color.BLACK);
        curve.setStrokeWidth(1);
        curve.setFill(Color.TRANSPARENT);

        return curve;
    }


    private Text createTransitionText(QuadCurve transitionCurve, String transitionInput) {
        double midX = (transitionCurve.getStartX() + transitionCurve.getEndX()) / 2;
        double midY = (transitionCurve.getStartY() + transitionCurve.getEndY()) / 2;

        Text text = new Text(midX, midY, transitionInput);
        text.setFill(Color.RED);

        double offsetX = 10;
        double offsetY = -40; // Slightly negative to move it closer

        double angle = Math.atan2(transitionCurve.getEndY() - transitionCurve.getStartY(),
                transitionCurve.getEndX() - transitionCurve.getStartX());

        text.setX(midX + offsetX * Math.cos(angle));
        text.setY(midY + offsetY * Math.sin(angle));

        return text;
    }

    private Polygon createArrowhead(QuadCurve transitionCurve) {
        double endX = transitionCurve.getEndX();
        double endY = transitionCurve.getEndY();
        double startX = transitionCurve.getStartX();
        double startY = transitionCurve.getStartY();

        double angle = Math.atan2(endY - startY, endX - startX);

        double sin = Math.sin(angle);
        double cos = Math.cos(angle);

        double arrowHeadSize = 10;
        Polygon arrow = new Polygon();
        arrow.getPoints().addAll(
                endX, endY,
                endX - arrowHeadSize * cos - arrowHeadSize * sin, endY - arrowHeadSize * sin + arrowHeadSize * cos,
                endX - arrowHeadSize * cos + arrowHeadSize * sin, endY - arrowHeadSize * sin - arrowHeadSize * cos
        );

        arrow.setFill(Color.BLACK);

        return arrow;
    }

    private void updateArrowhead(Polygon arrow, double endX, double endY, double startX, double startY) {
        double angle = Math.atan2(endY - startY, endX - startX);

        double sin = Math.sin(angle);
        double cos = Math.cos(angle);

        double arrowHeadSize = 10;
        arrow.getPoints().setAll(
                endX, endY,
                endX - arrowHeadSize * cos - arrowHeadSize * sin, endY - arrowHeadSize * sin + arrowHeadSize * cos,
                endX - arrowHeadSize * cos + arrowHeadSize * sin, endY - arrowHeadSize * sin - arrowHeadSize * cos
        );
    }

    @FXML
    private void handlePrintStates(ActionEvent event) {
        automaton.printQ();
        System.out.println("States: " + automaton.getQ());
    }

    @FXML
    private void handlePrintFinalStates(ActionEvent event){
        automaton.printF();
        System.out.println("Final States: " + automaton.getF());
    }

    @FXML
    private void handlePrintSize(ActionEvent event) {
        System.out.println("Automaton size: " + automaton.getStateSize());
    }

    @FXML
    private void handlePrintDelta(ActionEvent event){
        automaton.printDelta();
        populateDeltaTable();
    }

    @FXML
    private void handleAddString(ActionEvent event) {
        String newString = newStringTextField.getText().trim();
        Set<String> sigma = automaton.getSigma();
        if (!newString.isEmpty()) {
            automaton.addString(newString);
        }
        sigmaTextField.setText(sigma.toString());
    }

    @FXML
    private void handlePrintSigma(ActionEvent event){
        Set<String> sigma = automaton.getSigma();
        sigmaTextField.setText(sigma.toString());
        System.out.println("Sigma: " + sigma);
    }


    @FXML
    private void printStackPanes() {
        System.out.println("Stack Panes:");
        stackPanes.forEach(stackPane -> System.out.println("Stack Pane ID: " + stackPane.getId()));
    }

    private void populateDeltaTable() {
        ObservableList<Transition> data = FXCollections.observableArrayList();
        Map<String, Map<String, String>> delta = automaton.getDelta();

        for (String currentState : delta.keySet()) {
            for (String input : delta.get(currentState).keySet()) {
                String nextState = delta.get(currentState).get(input);
                data.add(new Transition(currentState, input, nextState));
            }
        }

        deltaTableView.setItems(data);
    }

    @FXML
    private void handleTestString(ActionEvent event) {
        String testString = testStringTextField.getText().trim();
        if (testString.isEmpty()) {
            resultLabel.setText("Please enter a string to test.");
            resultLabel.setTextFill(Color.RED);
            return;
        }

        boolean isAccepted = automaton.isStringAccepted(testString);
        if (isAccepted) {
            resultLabel.setText("String is accepted.");
            resultLabel.setTextFill(Color.GREEN);
        } else {
            resultLabel.setText("String is not accepted.");
            resultLabel.setTextFill(Color.RED);
        }
    }

    //!
    public static class Transition {
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
}