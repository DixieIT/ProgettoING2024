package com.example.progettoing;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;

public class MainController implements Initializable {

    private final Automa automaton;
    @FXML
    private AnchorPane mainAnchorPane;

    @FXML
    private AnchorPane stateAnchorPane;

    @FXML //q0 + arrow
    StackPane groupStackPane = new StackPane();

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
    private ComboBox<String> comboBoxAlphabet;

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
    private boolean isFinalState;
    private String currentState, nextState, transitionInput;

    public MainController(Automa automaton) {
        this.automaton = automaton;
    }

    @FXML
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        stackPanes = new ArrayList<>();
        comboBoxAlphabet.setDisable(true);

        Platform.runLater(() -> {
            Stage primaryStage = (Stage)mainAnchorPane.getScene().getWindow();
            primaryStage.setMaximized(true);
        });

        double layoutX = q0.getLayoutX();
        double layoutY = q0.getLayoutY();
        System.out.println("Initial State (q0) LayoutX: " + layoutX + ", LayoutY: " + layoutY);


        Group q0_arrow = new Group();

        Line line = new Line();
        line.setStartX(20);
        line.setStartY(290);
        line.setEndX(65); // Lunghezza della freccia
        line.setEndY(290);
        line.setStrokeWidth(2);
        line.setStroke(Color.BLACK);
        line.setTranslateX(0);
        line.setTranslateY(0);

        //unisco in un oggetto Polygon la linea e la punta della freccia
        Polygon arrow = new Polygon();
        arrow.getPoints().addAll(new Double[]{
                line.getEndX(), line.getEndY(),
                line.getEndX() - 10, line.getEndY() - 5,
                line.getEndX() - 10, line.getEndY() + 5
        });
        arrow.setFill(Color.BLACK);
        arrow.setTranslateX(7);
        q0_arrow.getChildren().addAll(arrow, line, q0);

        groupStackPane.getChildren().add(q0_arrow);
        stateAnchorPane.getChildren().add(groupStackPane);
        draggableMaker.makeDraggable(groupStackPane, stateAnchorPane);
        addMovementListeners(groupStackPane);
        stackPanes.add(groupStackPane);

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
            if(states[0].equals("q0"))
                fromState = groupStackPane;
            StackPane toState = findStateById(states[1]);
            if(states[1].equals("q0"))
                toState = groupStackPane;

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
    private void updateAlphabet() {
        while(!comboBoxAlphabet.getItems().isEmpty()) {
            comboBoxAlphabet.getItems().remove(0);
        }
        comboBoxAlphabet.getItems().addAll(automaton.getSigma());
        comboBoxAlphabet.setVisibleRowCount(15);

        //only view ComboBox
        comboBoxAlphabet.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    // Imposta il testo dell'elemento
                    setText(item);
                    // Disabilita il click sugli elementi
                    setDisable(true);
                    // Impedisce il passaggio del mouse sugli elementi
                    setMouseTransparent(true);
                }
            }
        });

        comboBoxAlphabet.showingProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) { // ComboBox è stato appena aperto
                // Rimuovi il focus dagli elementi del ComboBox
                comboBoxAlphabet.getSelectionModel().clearSelection();
                comboBoxAlphabet.requestFocus(); // Rimuove il focus dal ComboBox stesso
            }
        });

        comboBoxAlphabet.setPromptText("View strings in Σ");
    }

    @FXML
    private void addStringButton(ActionEvent actionEvent) {
        StackPane newStringStackPane = new StackPane();
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Add a String to Σ");

        TextField label = new TextField();
        label.setPromptText("String");
        label.setStyle("-fx-font-size: 14");
        label.setTranslateX(-5);
        label.setTranslateY(-18);
        label.setMaxWidth(130);
        label.setPrefHeight(30);

        Button submitNewString = new Button();

        Button removeFocus = new Button();
        removeFocus.setMaxSize(0, 0);
        removeFocus.setTranslateX(-20000);

        submitNewString.disableProperty().bind(Bindings.isEmpty(label.textProperty()));
        submitNewString.setText("+");
        submitNewString.setStyle("-fx-font-size: 14; -fx-font-weight: bold");
        newStringStackPane.setPrefSize(270, 100);
        submitNewString.setTranslateX(80);
        submitNewString.setTranslateY(-18.5);
        submitNewString.setPrefSize(30, 30);

        submitNewString.setOnAction(event -> {
            handleAddString(label.getText());
            updateAlphabet();
            if(comboBoxAlphabet.isDisabled())
                comboBoxAlphabet.setDisable(false);
            label.clear();
        });

        Button closeWindow = new Button();
        closeWindow.setText("Close");
        closeWindow.setTranslateY(30);
        closeWindow.setOnAction(event -> {
            ((Stage)newStringStackPane.getScene().getWindow()).close();
        });

        newStringStackPane.getChildren().addAll(removeFocus, submitNewString, label, closeWindow);
        Scene popupScene = new Scene(newStringStackPane);
        popup.setScene(popupScene);
        popup.showAndWait();
    }

    @FXML
    private void addStateButton(ActionEvent actionEvent) {
        StackPane newStateStackPane = new StackPane();
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Add State");

        CheckBox isFinalStateCheckBox = new CheckBox("Final state");
        isFinalStateCheckBox.setTranslateY(10);
        isFinalStateCheckBox.setTranslateX(-4);
        isFinalStateCheckBox.setStyle("-fx-font-size: 14");


        Button submitNewState = new Button();

        Button removeFocus = new Button();
        removeFocus.setMaxSize(0, 0);
        removeFocus.setTranslateX(-20000);

        newStateStackPane.getChildren().addAll(removeFocus, isFinalStateCheckBox, submitNewState);

        submitNewState.setText("Confirm");
        newStateStackPane.setPrefSize(250, 200);
        submitNewState.setTranslateY(75);
        submitNewState.setOnAction(event -> {
            this.isFinalState = isFinalStateCheckBox.isSelected();
            handleAddState();
            ((Stage)newStateStackPane.getScene().getWindow()).close();
        });
        Scene popupScene = new Scene(newStateStackPane);
        popup.setScene(popupScene);
        popup.showAndWait();
    }

    @FXML
    private void handleAddState() {
        StackPane stackPane = createStatePane(isFinalState);
        mainAnchorPane.getChildren().add(stackPane);
        if(isFinalState)
            automaton.addFinalState();
        else
            automaton.addState();
        System.out.println(automaton.getQ().size() + "STATI");
    }

    private StackPane createStatePane(boolean isFinalState) {
        StackPane stackPane = new StackPane();
        stackPane.setLayoutX(800);
        stackPane.setLayoutY(242);

        Circle outerCircle = new Circle(50, Color.WHITE);
        outerCircle.setStroke(Color.BLACK);
        outerCircle.setStrokeWidth(1.8);

        stackPane.getChildren().add(outerCircle);

        if (isFinalState) {
            Circle innerCircle = new Circle(45, Color.TRANSPARENT);
            innerCircle.setStroke(Color.BLACK);
            innerCircle.setStrokeWidth(1.8);
            innerCircle.setTranslateX(-0.5);
            innerCircle.setTranslateY(-0.4);
            stackPane.getChildren().add(innerCircle);
        }

        String stateId = "q" + automaton.getStateSize();
        Label textField = new Label(stateId);
        textField.setMaxWidth(30);
        textField.setStyle("-fx-font-weight: bold; -fx-font-size: 15");
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
    private void addTransitionButton(ActionEvent actionEvent) {
        StackPane newTransitionStackPane = new StackPane();
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Add Transition");

        TextField label = new TextField();
        label.setPromptText("Label");
        label.setTranslateY(22);
        label.setMaxWidth(130);
        label.setPrefHeight(30);

        ComboBox<String> from = new ComboBox<>();
        from.setVisibleRowCount(3);
        from.setPromptText("From");
        // Creazione di menu items all'interno del menu File
        for(String s: automaton.getQ()) {
            from.getItems().add(s);
        }
        from.setTranslateY(-68);
        from.setPrefSize(135, 30);


        ComboBox<String> to = new ComboBox<>();
        to.setVisibleRowCount(3);
        to.setPromptText("To");

        // Creazione di menu items all'interno del menu File
        for(String s: automaton.getQ()) {
            to.getItems().add(s);
        }
        to.setTranslateY(-23);
        to.setPrefSize(135, 30);

        Button submitNewTransition = new Button();

        Button removeFocus = new Button();
        removeFocus.setMaxSize(0, 0);
        removeFocus.setTranslateX(-20000);
        newTransitionStackPane.getChildren().addAll(removeFocus, submitNewTransition, label, from, to);

        submitNewTransition.disableProperty().bind(Bindings.isEmpty(label.textProperty()).or(from.valueProperty().isNull()).or(to.valueProperty().isNull()));
        submitNewTransition.setText("Confirm");
        newTransitionStackPane.setPrefSize(270, 200);
        submitNewTransition.setTranslateY(78);



        from.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            currentState = newValue;
        });

        to.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            nextState = newValue;
        });

        submitNewTransition.setOnAction(event -> {
            handleAddTransition(currentState, label.getText(), nextState);
            ((Stage)newTransitionStackPane.getScene().getWindow()).close();
        });
        Scene popupScene = new Scene(newTransitionStackPane);
        popup.setScene(popupScene);
        popup.showAndWait();
    }

    @FXML
    private void handleAddTransition(String currentState, String transitionInput, String nextState) {
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
            if(currentState.equals("q0"))
                currentStatePane = groupStackPane;

            StackPane nextStatePane = findStateById(nextState);
            if(nextState.equals("q0"))
                nextStatePane = groupStackPane;

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
    private void handleAddString(String newString) {
        Set<String> sigma = automaton.getSigma();
        if (!newString.isEmpty()) {
            automaton.addString(newString);
        }
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