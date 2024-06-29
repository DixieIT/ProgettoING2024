package com.example.progettoing;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

public class DraggableMaker {

    private double mouseAnchorX;
    private double mouseAnchorY;

    public void makeDraggable(Node node, AnchorPane stateAnchorPane) {

        node.setOnMousePressed(mouseEvent -> {
            mouseAnchorX = mouseEvent.getX();
            mouseAnchorY = mouseEvent.getY();
            node.toFront();
        });

        node.setOnMouseDragged(mouseEvent -> {
            double newX = mouseEvent.getSceneX() - mouseAnchorX;
            double newY = mouseEvent.getSceneY() - mouseAnchorY;

            // Limiti per il trascinamento basati su stateAnchorPane
            double minX = 0;
            double minY = 0;
            double maxX = stateAnchorPane.getWidth() - node.getBoundsInParent().getWidth();
            double maxY = stateAnchorPane.getHeight() - node.getBoundsInParent().getHeight();

            // Applicare i limiti
            newX = Math.max(minX, Math.min(newX, maxX));
            newY = Math.max(minY, Math.min(newY, maxY));

            node.setLayoutX(newX);
            node.setLayoutY(newY);
            node.toFront();
        });

        node.setOnMouseReleased(mouseEvent -> {
            double newX = node.getLayoutX();
            double newY = node.getLayoutY();
            System.out.println("New position: (" + newX + ", " + newY + ")");
        });
    }
}
