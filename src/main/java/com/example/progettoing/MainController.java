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
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
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

    private Automa automaton;
    @FXML
    private AnchorPane mainAnchorPane;

    @FXML
    private AnchorPane stateAnchorPane;

    @FXML
    private Button testStringButton;

    @FXML //q0 + arrow
    StackPane groupStackPane = new StackPane();

    private AnchorPane pathAnchorPane;

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

    private boolean isPopup = false;

    private boolean deltaEmpty = true;

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

        testStringTextField.setStyle("-fx-border-color: lightgrey; -fx-background-color: white; -fx-prompt-text-fill: grey; -fx-border-radius: 5; -fx-font-family: Arial;");
        testStringButton.getStylesheets().add(getClass().getResource("/com/example/progettoing/Main.css").toExternalForm());
        testStringButton.getStyleClass().add("testString-button");
        testStringButton.setTranslateY(1);

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
        q0_arrow.setTranslateY(180);

        groupStackPane.getChildren().add(q0_arrow);

        mainAnchorPane.getChildren().add(groupStackPane);
        draggableMaker.makeDraggable(groupStackPane, stateAnchorPane, true);
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
            if(states[0].equals("q0")) {
                fromState = groupStackPane;
            }

            StackPane toState = findStateById(states[1]);
            if(states[1].equals("q0")) {
                toState = groupStackPane;
            }

            if (fromState != null && toState != null && !states[0].equals(states[1])) {
                double startX = fromState.getLayoutX() + fromState.getWidth() / 2;
                double startY = fromState.getLayoutY() + fromState.getHeight() / 2;
                if(fromState.equals(groupStackPane))
                    startY = (fromState.getLayoutY() + fromState.getHeight() / 2) + 180;

                double endX = toState.getLayoutX() + toState.getWidth() / 2;
                double endY = toState.getLayoutY() + toState.getHeight() / 2;
                if(toState.equals(groupStackPane))
                    endY = (toState.getLayoutY() + toState.getHeight() / 2) + 180;

                double[] endPoint = calculateEndPoint(startX, startY, endX, endY, 50);

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
                    updateArrowhead(arrow, endPoint[0], endPoint[1], startX, startY);
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
            if(fromState != null && fromState.equals(q0))
                fromState = groupStackPane;

            if (fromState != null && states[0].equals(states[1])) {
                updateSelfTransitionLoop(curve, fromState, parts[1]);
                curve.toBack();
            }
        }
    }

    private void updateSelfTransitionLoop(CubicCurve curve, StackPane state, String input) {
        double centerX = state.getLayoutX() + state.getWidth() / 2;
        double centerY = state.getLayoutY() + state.getHeight() / 2;

        if(state.equals(groupStackPane)) {
            centerX = q0.getLayoutX() + q0.getWidth() / 2;
            centerY = q0.getLayoutY() + q0.getHeight() / 2;
        }

        double loopRadius = 40;
        double stateRadius = 50;

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
            if(isPopup)
                pathAnchorPane.getChildren().add(labelText);
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
        if(automaton.getSigma().isEmpty()) {
            comboBoxAlphabet.setDisable(true);
        }
        comboBoxAlphabet.setVisibleRowCount(15);

        comboBoxAlphabet.setStyle("-fx-font-family: Arial");
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
        label.setStyle("-fx-font-size: 14; -fx-font-family: Arial;");
        label.setTranslateX(-42.5);
        label.setTranslateY(-18);
        label.setMaxWidth(130);
        label.setPrefHeight(30);

        Button submitNewString = new Button();
        submitNewString.getStylesheets().add(getClass().getResource("/com/example/progettoing/Main.css").toExternalForm());
        submitNewString.getStyleClass().add("submitNewString-button");

        Button removeFocus = new Button();
        removeFocus.setMaxSize(0, 0);
        removeFocus.setTranslateX(-20000);

        submitNewString.disableProperty().bind(Bindings.isEmpty(label.textProperty()));
        submitNewString.setText("Add String");
        submitNewString.setStyle("-fx-font-size: 12; -fx-max-width: 73; -fx-pref-height: 28.3; -fx-font-family: Arial;");
        newStringStackPane.setPrefSize(240, 100);
        submitNewString.setTranslateX(70);
        submitNewString.setTranslateY(-18.5);

        submitNewString.setOnAction(event -> {
            handleAddString(label.getText());
            updateAlphabet();
            if(comboBoxAlphabet.isDisabled())
                comboBoxAlphabet.setDisable(false);
            label.clear();
        });

        comboBoxAlphabet.getStylesheets().add(getClass().getResource("/com/example/progettoing/Main.css").toExternalForm());
        comboBoxAlphabet.getStyleClass().add("alphabet-combobox");

        Button closeWindow = new Button();
        closeWindow.getStylesheets().add(getClass().getResource("/com/example/progettoing/Main.css").toExternalForm());
        closeWindow.getStyleClass().add("newStringCloseWindow-button");
        closeWindow.setText("Close");
        closeWindow.setStyle("-fx-font-family: Arial");
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
        isFinalStateCheckBox.getStylesheets().add(getClass().getResource("/com/example/progettoing/Main.css").toExternalForm());
        isFinalStateCheckBox.getStyleClass().add("finalState-checkbox");
        isFinalStateCheckBox.setTranslateY(-15);
        isFinalStateCheckBox.setTranslateX(-4);

        isFinalStateCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                isFinalStateCheckBox.getStyleClass().add("selected");
            } else {
                isFinalStateCheckBox.getStyleClass().remove("selected");
            }
        });

        Button submitNewState = new Button();
        submitNewState.getStylesheets().add(getClass().getResource("/com/example/progettoing/Main.css").toExternalForm());
        submitNewState.getStyleClass().add("submitNewState-button");

        Button removeFocus = new Button();
        removeFocus.setMaxSize(0, 0);
        removeFocus.setTranslateX(-20000);

        newStateStackPane.getChildren().addAll(removeFocus, isFinalStateCheckBox, submitNewState);

        submitNewState.setText("Add State");
        newStateStackPane.setPrefSize(150, 100);
        submitNewState.setTranslateY(30);
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
        textField.setStyle("-fx-font-weight: bold; -fx-font-size: 15; -fx-font-family: Arial;");
        textField.setAlignment(Pos.CENTER);
        textField.setId("StateText");

        stackPane.setId(stateId);
        stackPanes.add(stackPane);
        stackPane.getChildren().add(textField);

        draggableMaker.makeDraggable(stackPane, stateAnchorPane, false);
        addMovementListeners(stackPane);

        return stackPane;
    }

    @FXML
    private void addTransitionButton(ActionEvent actionEvent) {

        if(automaton.getSigma().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Transition Error");
            alert.setHeaderText("An error has occurred");
            alert.setContentText("Adding at least one string to Σ is required.");
            alert.getDialogPane().getStylesheets().add(getClass().getResource("/com/example/progettoing/Main.css").toExternalForm());
            alert.getDialogPane().getStyleClass().add("transition-alert");

            // Mostra il popup e attendi finché l'utente non lo chiude
            alert.showAndWait();
            return;
        }

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
        from.getStylesheets().add(getClass().getResource("/com/example/progettoing/Main.css").toExternalForm());
        from.getStyleClass().add("combobox");
        from.setVisibleRowCount(3);
        from.setPromptText("From");
        // Creazione di menu items all'interno del menu File
        for(String s: automaton.getQ()) {
            from.getItems().add(s);
        }
        from.setTranslateY(-68);
        from.setPrefSize(135, 30);


        ComboBox<String> to = new ComboBox<>();
        to.getStylesheets().add(getClass().getResource("/com/example/progettoing/Main.css").toExternalForm());
        to.getStyleClass().add("combobox");
        to.setVisibleRowCount(3);
        to.setPromptText("To");

        // Creazione di menu items all'interno del menu File
        for(String s: automaton.getQ()) {
            to.getItems().add(s);
        }
        to.setTranslateY(-23);
        to.setPrefSize(135, 30);

        Button submitNewTransition = new Button();
        submitNewTransition.getStylesheets().add(getClass().getResource("/com/example/progettoing/Main.css").toExternalForm());
        submitNewTransition.getStyleClass().add("submitNewString-button");

        Button removeFocus = new Button();
        removeFocus.setMaxSize(0, 0);
        removeFocus.setTranslateX(-20000);
        newTransitionStackPane.getChildren().addAll(removeFocus, submitNewTransition, label, from, to);

        submitNewTransition.disableProperty().bind(Bindings.isEmpty(label.textProperty()).or(from.valueProperty().isNull()).or(to.valueProperty().isNull()));
        submitNewTransition.setText("Confirm");
        newTransitionStackPane.setPrefSize(200, 200);
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
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Transition Error");
            alert.setHeaderText("An error has occurred");
            alert.setContentText("Invalid transition: label is not a string in Σ");
            alert.getDialogPane().getStylesheets().add(getClass().getResource("/com/example/progettoing/Main.css").toExternalForm());
            alert.getDialogPane().getStyleClass().add("transition-alert");
            // Mostra il popup e attendi finché l'utente non lo chiude
            alert.showAndWait();
            addTransitionButton(new ActionEvent());
        }

        if (automaton.addTransition(currentState, transitionInput, nextState) == -1) {
            System.err.println("Failed to add transition.");
            return;
        }

        System.out.println("Added transition Delta(" + currentState + ", " + transitionInput + ") = " + nextState);

        regenerateTransitions(false, false);
        deltaEmpty = false;
        populateDeltaTable();
    }

    private void clearTransitions() {
        for (QuadCurve curve : transitionCurves.values()) {
            mainAnchorPane.getChildren().remove(curve);
            groupStackPane.getChildren().remove(curve);
        }
        for (CubicCurve curve : selfTransitionCurves.values()) {
            mainAnchorPane.getChildren().remove(curve);
            groupStackPane.getChildren().remove(curve);
        }
        for (Text text : transitionTexts.values()) {
            mainAnchorPane.getChildren().remove(text);
            groupStackPane.getChildren().remove(text);
        }
        for (Polygon arrow : transitionArrows.values()) {
            mainAnchorPane.getChildren().remove(arrow);
            groupStackPane.getChildren().remove(arrow);
        }

        transitionCurves.clear();
        selfTransitionCurves.clear();
        transitionTexts.clear();
        transitionArrows.clear();
    }

    private StackPane copyStackPane(StackPane original) {
        StackPane copy = new StackPane();
        copy.setStyle(original.getStyle()); // Copia lo stile se presente

        for (javafx.scene.Node originalChild : original.getChildren()) {
            javafx.scene.Node copiedChild = cloneNode(originalChild);
            if (copiedChild != null) {
                copy.getChildren().add(copiedChild);
            }
        }

        // Copia la posizione del StackPane originale
        copy.setLayoutX(original.getLayoutX());
        copy.setLayoutY(original.getLayoutY());

        return copy;
    }

    private void generatePath(boolean isAccepted) {
        isPopup = true;
        regenerateTransitionsPopup(isAccepted);

        Map<String, Map<String, String>> path = automaton.getPath();
        Map<String, String> concatenatedTransitions = new HashMap<>();

        for (String currentState : path.keySet()) {
            for (String input : path.get(currentState).keySet()) {
                String nextState = path.get(currentState).get(input);
                String key = currentState + "," + nextState;

                if (concatenatedTransitions.containsKey(key)) {
                    concatenatedTransitions.put(key, concatenatedTransitions.get(key) + ", " + input);
                } else {
                    concatenatedTransitions.put(key, input);
                }
            }
        }
    }

    private javafx.scene.Node cloneNode(javafx.scene.Node original) {
        if (original instanceof Circle) {
            Circle originalCircle = (Circle) original;
            Circle copiedCircle = new Circle(originalCircle.getRadius(), originalCircle.getFill());
            copiedCircle.setStroke(originalCircle.getStroke());
            copiedCircle.setStrokeWidth(originalCircle.getStrokeWidth());

            // Copia la posizione relativa
            copiedCircle.setLayoutX(originalCircle.getLayoutX());
            copiedCircle.setLayoutY(originalCircle.getLayoutY());

            // Puoi copiare altre proprietà necessarie qui
            return copiedCircle;
        } else if (original instanceof Polygon) {
            Polygon originalPolygon = (Polygon) original;
            Double[] points = new Double[originalPolygon.getPoints().size()];
            originalPolygon.getPoints().toArray(points);
            Polygon copiedPolygon = new Polygon();
            copiedPolygon.getPoints().addAll(points);

            // Copia la posizione relativa
            copiedPolygon.setLayoutX(originalPolygon.getLayoutX());
            copiedPolygon.setLayoutY(originalPolygon.getLayoutY());
            copiedPolygon.setTranslateX(5);

            // Puoi copiare altre proprietà necessarie qui
            return copiedPolygon;
        } else if (original instanceof Line) {
            Line originalLine = (Line) original;
            Line copiedLine = new Line(originalLine.getStartX(), originalLine.getStartY(),
                    originalLine.getEndX(), originalLine.getEndY());
            copiedLine.setStroke(originalLine.getStroke());
            copiedLine.setStrokeWidth(originalLine.getStrokeWidth());
            // Copia la posizione relativa
            copiedLine.setLayoutX(originalLine.getLayoutX());
            copiedLine.setLayoutY(originalLine.getLayoutY());

            // Puoi copiare altre proprietà necessarie qui
            return copiedLine;
        } else if (original instanceof Label) {
            Label originalLabel = (Label) original;
            Label copiedLabel = new Label(originalLabel.getText());
            copiedLabel.setFont(originalLabel.getFont());
            copiedLabel.setTextFill(originalLabel.getTextFill());
            copiedLabel.setLayoutX(originalLabel.getLayoutX());
            copiedLabel.setLayoutY(originalLabel.getLayoutY());

            return copiedLabel;
        } else if (original instanceof Group) {
            Group originalGroup = (Group) original;
            Group copiedGroup = new Group();

            // Clona ogni nodo interno del Group
            for (Node child : originalGroup.getChildren()) {
                Node copiedChild = cloneNode(child);
                if (copiedChild != null) {
                    copiedChild.setLayoutX(child.getLayoutX());
                    copiedChild.setLayoutY(child.getLayoutY());
                    if(child instanceof StackPane){
                        copiedChild.setTranslateY(50);
                    }
                    copiedGroup.getChildren().add(copiedChild);
                }
            }
            copiedGroup.setTranslateY(originalGroup.getTranslateY() + 50);

            return copiedGroup;
        } else if (original instanceof StackPane) {
            // Clona lo StackPane
            StackPane originalStackPane = (StackPane) original;
            StackPane copiedStackPane = new StackPane();
            copiedStackPane.setStyle(originalStackPane.getStyle());

            // Clona ogni nodo figlio dello StackPane
            for (javafx.scene.Node child : originalStackPane.getChildren()) {
                javafx.scene.Node copiedChild = cloneNode(child);
                if (copiedChild != null) {
                    copiedStackPane.getChildren().add(copiedChild);
                }
            }

            // Copia la posizione del StackPane originale
            copiedStackPane.setLayoutX(originalStackPane.getLayoutX());
            copiedStackPane.setLayoutY(originalStackPane.getLayoutY());

            return copiedStackPane;
        }
        else if (original instanceof Text) {
            Text originalText = (Text) original;
            Text copiedText = new Text(originalText.getText());
            copiedText.setFont(originalText.getFont());
            copiedText.setFill(originalText.getFill());

            // Copia la posizione relativa rispetto al parent
            copiedText.setLayoutX(originalText.getLayoutX());
            copiedText.setLayoutY(originalText.getLayoutY());

            // Puoi copiare altre proprietà necessarie qui per il Text

            return copiedText;
        }
        else {
            // Gestione di altri tipi di nodi se necessario
            return null;
        }
    }



    private void regenerateTransitionsPopup(boolean isAccepted) {
        AnchorPane backgroundAnchorPane = new AnchorPane();
        backgroundAnchorPane.setPrefSize(2013, 1069);
        Label popupTitle = new Label();
        popupTitle.setText("AUTOMATON TEST");
        popupTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 35; -fx-font-family: Arial;");
        popupTitle.setTranslateY(20);

        Label accepted = new Label();
        if(isAccepted) {
            accepted.setText("The string \"" + testStringTextField.getText() + "\"is accepted." );
            accepted.setTextFill(Color.GREEN);
        }
        else {
            accepted.setText("The string \"" + testStringTextField.getText() + "\" is not accepted." );
            accepted.setTextFill(Color.RED);
        }
        accepted.setStyle("-fx-font-size: 20; -fx-font-family: Arial;");
        accepted.setTranslateY(58);
        StackPane stackPaneTitle = new StackPane();
        stackPaneTitle.getChildren().addAll(popupTitle, accepted);
        stackPaneTitle.setAlignment(Pos.TOP_CENTER);
        stackPaneTitle.setTranslateX(700);
        backgroundAnchorPane.getChildren().add(stackPaneTitle);
        pathAnchorPane = new AnchorPane();
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Test");
        pathAnchorPane.setPrefSize(2013, 869);
        Scene popupScene = new Scene(backgroundAnchorPane);
        popup.setScene(popupScene);
        popup.setMaximized(true);

        regenerateTransitions(false, false);
        regenerateTransitions(true, isAccepted);
        pathAnchorPane.setTranslateY(230);
        isPopup = false;
        regenerateTransitions(false, false);
        backgroundAnchorPane.getChildren().add(pathAnchorPane);
        popup.showAndWait();
    }

    private void regenerateTransitions(boolean hasToBeColored, boolean isAccepted) {
        clearTransitions();

        Map<String, Map<String, String>> map = automaton.getDelta();
        if(hasToBeColored)
            map = automaton.getPath();
        else
            map = automaton.getDelta();

        Map<String, String> concatenatedTransitions = new HashMap<>();

        for (String currentState : map.keySet()) {
            for (String input : map.get(currentState).keySet()) {
                String nextState = map.get(currentState).get(input);
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
                        selfLoop = createSelfTransitionLoop(currentStatePane, inputs, hasToBeColored, isAccepted);

                        mainAnchorPane.getChildren().add(selfLoop);

                        if(currentStatePane.equals(groupStackPane)) {
                            selfLoop.setTranslateX(5);
                            selfLoop.setTranslateY(133);
                            groupStackPane.getChildren().add(selfLoop);
                            selfLoop.toBack();
                        }
                        selfTransitionCurves.put(selfLoopKey, selfLoop);
                    } else {
                        updateSelfTransitionLoop(selfLoop, currentStatePane, inputs);
                    }
                    if(isPopup) {
                        CubicCurve selfLoopCopy = new CubicCurve();
                        selfLoopCopy.setStartX(selfLoop.getStartX());
                        selfLoopCopy.setStartY(selfLoop.getStartY());
                        selfLoopCopy.setControlX1(selfLoop.getControlX1());
                        selfLoopCopy.setControlY1(selfLoop.getControlY1());
                        selfLoopCopy.setControlX2(selfLoop.getControlX2());
                        selfLoopCopy.setControlY2(selfLoop.getControlY2());
                        selfLoopCopy.setEndX(selfLoop.getEndX());
                        selfLoopCopy.setEndY(selfLoop.getEndY());
                        selfLoopCopy.setStroke(selfLoop.getStroke());

                        if(hasToBeColored) {
                            if(isAccepted)
                                selfLoopCopy.setStroke(Color.GREEN);
                            else
                                selfLoopCopy.setStroke(Color.RED);
                        }

                        selfLoopCopy.setStrokeWidth(selfLoop.getStrokeWidth());
                        selfLoopCopy.setFill(selfLoop.getFill());
                        if(currentStatePane.equals(groupStackPane)) {
                            selfLoopCopy.setTranslateY(180);
                            selfLoopCopy.setTranslateX(22);
                        }
                        pathAnchorPane.getChildren().add(selfLoopCopy);
                    }

                } else {
                    String transitionKey = key + "->" + inputs;
                    QuadCurve transitionCurve = transitionCurves.get(transitionKey);
                    Text transitionTextCopy = new Text();
                    Polygon arrowCopy = new Polygon();
                    if (transitionCurve == null) {
                        transitionCurve = createTransitionCurve(currentStatePane, nextStatePane);
                        Text transitionText = createTransitionText(transitionCurve, inputs);
                        Polygon arrow = createArrowhead(transitionCurve);

                        mainAnchorPane.getChildren().addAll(transitionCurve, transitionText, arrow);
                        transitionCurves.put(transitionKey, transitionCurve);
                        transitionTexts.put(transitionKey, transitionText);
                        transitionArrows.put(transitionKey, arrow);

                        transitionTextCopy.setText(transitionText.getText());

                        transitionTextCopy.setX(transitionText.getX());
                        transitionTextCopy.setY(transitionText.getY());
                        transitionTextCopy.setFont(transitionText.getFont());
                        transitionTextCopy.setFill(transitionText.getFill());
                        transitionTextCopy.setStroke(transitionText.getStroke());
                        transitionTextCopy.setStrokeWidth(transitionText.getStrokeWidth());
                        transitionTextCopy.setWrappingWidth(transitionText.getWrappingWidth());
                        transitionTextCopy.setStyle("-fx-font-size: 16.5");

                        if(hasToBeColored) {
                            if(isAccepted)
                                transitionTextCopy.setStroke(Color.GREEN);
                            else
                                transitionTextCopy.setStroke(Color.RED);
                        }

                        ObservableList<Double> points = arrow.getPoints();
                        arrowCopy.getPoints().setAll(points);
                        arrowCopy.setFill(arrow.getFill());
                        arrowCopy.setStroke(arrow.getStroke());
                        arrowCopy.setStrokeWidth(arrow.getStrokeWidth());

                        if(hasToBeColored) {
                            if(isAccepted) {
                                arrowCopy.setStroke(Color.GREEN);
                                arrowCopy.setFill(Color.GREEN);
                            }
                            else {
                                arrowCopy.setStroke(Color.RED);
                                arrowCopy.setFill(Color.RED);
                            }
                        }

                    } else {
                        updateTransitionCurve(transitionCurve, currentStatePane, nextStatePane, inputs);
                    }
                    if(isPopup) {
                        QuadCurve transitionCurveCopy = new QuadCurve();
                        transitionCurveCopy.setStartX(transitionCurve.getStartX());
                        transitionCurveCopy.setStartY(transitionCurve.getStartY());
                        transitionCurveCopy.setControlX(transitionCurve.getControlX());
                        transitionCurveCopy.setControlY(transitionCurve.getControlY());
                        transitionCurveCopy.setEndX(transitionCurve.getEndX());
                        transitionCurveCopy.setEndY(transitionCurve.getEndY());
                        transitionCurveCopy.setStroke(transitionCurve.getStroke());
                        transitionCurveCopy.setStrokeWidth(transitionCurve.getStrokeWidth());
                        transitionCurveCopy.setFill(transitionCurve.getFill());

                        if(hasToBeColored) {
                            if(isAccepted)
                                transitionCurveCopy.setStroke(Color.GREEN);
                            else
                                transitionCurveCopy.setStroke(Color.RED);
                        }

                        pathAnchorPane.getChildren().addAll(transitionCurveCopy, transitionTextCopy, arrowCopy);
                    }
                }
            }
        }

        for (StackPane stackPane : stackPanes) {
            mainAnchorPane.getChildren().remove(stackPane);
            mainAnchorPane.getChildren().add(stackPane);
            if(isPopup)
                pathAnchorPane.getChildren().add(copyStackPane(stackPane));
        }
    }

    private void updateTransitionCurve(QuadCurve curve, StackPane fromState, StackPane toState, String input) {
        double startX = fromState.getLayoutX() + fromState.getWidth() / 2;
        double startY = fromState.getLayoutY() + fromState.getHeight() / 2;
        double endX = toState.getLayoutX() + toState.getWidth() / 2;
        double endY = toState.getLayoutY() + toState.getHeight() / 2;

        double[] endPoint = calculateEndPoint(startX, startY, endX, endY, 50);

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
            text.setY(midY + offsetY * Math.sin(angle) - 20);
        }

        Polygon arrow = transitionArrows.get(textKey);
        if (arrow != null) {
            updateArrowhead(arrow, endPoint[0], endPoint[1], startX, startY);
        }
    }

    private CubicCurve createSelfTransitionLoop(StackPane state, String input, boolean hasToBeColored, boolean isAccepted) {
        double centerX = state.getLayoutX() + state.getWidth() / 2;
        double centerY = state.getLayoutY() + state.getHeight() / 2;


        CubicCurve curve = new CubicCurve();
        double loopRadius = 40;
        double stateRadius = 50;

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
        curve.setStrokeWidth(2);
        curve.setFill(Color.TRANSPARENT);

        Text labelText = new Text(input);
        labelText.setX(centerX);
        labelText.setY(centerY - stateRadius - loopRadius - 5); // Position closer to the loop
        labelText.setFill(Color.BLACK);
        labelText.setTranslateX(-22);
        labelText.setTranslateY(10);
        labelText.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-font-family: Arial;");

        if(isPopup) {
            Text labelTextPopup = new Text(labelText.getText());
            labelTextPopup.setFont(labelText.getFont());
            labelTextPopup.setStyle(labelText.getStyle());
            labelTextPopup.setFill(labelText.getFill());
            labelTextPopup.setX(labelText.getX());
            labelTextPopup.setY(labelText.getY());
            if(state.equals(groupStackPane)) {
                labelTextPopup.setTranslateY(180);
                labelTextPopup.setTranslateX(-15);
            }
            else {
                labelTextPopup.setTranslateY(10);
                labelTextPopup.setTranslateX(-20);
            }
            labelTextPopup.setStyle("-fx-font-size: 16.5; -fx-font-weight: bold; -fx-font-family: Arial;");

            if(hasToBeColored) {
                if(isAccepted)
                    labelTextPopup.setFill(Color.GREEN);
                else
                    labelTextPopup.setFill(Color.RED);
            }

            pathAnchorPane.getChildren().add(labelTextPopup);
        }

        if(!state.equals(groupStackPane)) {
            mainAnchorPane.getChildren().add(labelText);
        }

        if(state.equals(groupStackPane) && !isPopup) {
            labelText.setTranslateY(80);
            labelText.setTranslateX(-10);
            groupStackPane.getChildren().add(labelText);
        }

        transitionTexts.put(state.getId() + "->" + input, labelText);
        if(state.equals(groupStackPane))
            transitionTexts.put("q0" + "->" + input, labelText);

        return curve;
    }

    private QuadCurve createTransitionCurve(StackPane fromState, StackPane toState) {
        double startX = fromState.getLayoutX() + fromState.getWidth() / 2;
        double startY = fromState.getLayoutY() + fromState.getHeight() / 2;
        if(fromState.equals(groupStackPane))
            startY = (fromState.getLayoutY() + fromState.getHeight() / 2) + 180;

        double endX = toState.getLayoutX() + toState.getWidth() / 2;
        double endY = toState.getLayoutY() + toState.getHeight() / 2;
        if(toState.equals(groupStackPane))
            endY = (toState.getLayoutY() + toState.getHeight() / 2) + 180;

        double[] endPoint = calculateEndPoint(startX, startY, endX, endY, 50);

        // Increase the control point distance for a more pronounced curve
        double controlX = (startX + endPoint[0]) / 2;
        double controlY = (startY + endPoint[1]) / 2 + (endX > startX ? -100 : 100);

        QuadCurve curve = new QuadCurve(startX, startY, controlX, controlY, endPoint[0], endPoint[1]);
        curve.setStroke(Color.BLACK);
        curve.setStrokeWidth(2);
        curve.setFill(Color.TRANSPARENT);

        return curve;
    }


    private Text createTransitionText(QuadCurve transitionCurve, String transitionInput) {
        double midX = (transitionCurve.getStartX() + transitionCurve.getEndX()) / 2;
        double midY = (transitionCurve.getStartY() + transitionCurve.getEndY()) / 2;

        Text text = new Text(midX, midY, transitionInput);
        text.setFill(Color.BLACK);
        text.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-font-family: Arial;");

        double offsetX = 10;
        double offsetY = -40; // Slightly negative to move it closer

        double angle = Math.atan2(transitionCurve.getEndY() - transitionCurve.getStartY(),
                transitionCurve.getEndX() - transitionCurve.getStartX());

        text.setX(midX + offsetX * Math.cos(angle));
        text.setY(midY + offsetY * Math.sin(angle) - 20);

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

        double arrowHeadSize = 6;
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

        double arrowHeadSize = 6;
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
        if(deltaEmpty) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("String Test Error");
            alert.setHeaderText("An error has occurred");
            alert.setContentText("Adding at least one transition is required.");
            alert.getDialogPane().getStylesheets().add(getClass().getResource("/com/example/progettoing/Main.css").toExternalForm());
            alert.getDialogPane().getStyleClass().add("transition-alert");
            alert.showAndWait();
            return;
        }

        String testString = testStringTextField.getText().trim();
        if (testString.isEmpty()) {
            resultLabel.setTextFill(Color.RED);
            return;
        }

        boolean isAccepted = automaton.isStringAccepted(testString);

        for (Map.Entry<String, Map<String, String>> stateEntry : automaton.getPath().entrySet()) {
            String state = stateEntry.getKey();
            for (Map.Entry<String, String> transitionEntry : stateEntry.getValue().entrySet()) {
                String inputSymbol = transitionEntry.getKey();
                String nextState = transitionEntry.getValue();
                System.out.println("Delta(" + state + ", " + inputSymbol + ") = " + nextState);
            }
        }

        generatePath(isAccepted);
        testStringTextField.clear();
    }

    @FXML
    private void handleClear() {
        automaton.clearAll();
        deltaEmpty = true;
        regenerateTransitions(false, false);
        populateDeltaTable();
        updateAlphabet();
        List<StackPane> toBeDeleted = new ArrayList<>();
        for(Node node: mainAnchorPane.getChildren()) {
            if(node instanceof StackPane)
                if(!node.equals(groupStackPane))
                    toBeDeleted.add((StackPane)node);
        }
        for(StackPane stackPane: toBeDeleted) {
            mainAnchorPane.getChildren().remove(stackPane);
            stackPanes.remove(stackPane);
        }
    }

}