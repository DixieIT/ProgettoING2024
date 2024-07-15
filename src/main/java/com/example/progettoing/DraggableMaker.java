package com.example.progettoing;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

public class DraggableMaker {

    private double mouseAnchorX;
    private double mouseAnchorY;

    public void makeDraggable(Node node, AnchorPane stateAnchorPane, boolean isQ0) {

        node.setOnMousePressed(mouseEvent -> {
            mouseAnchorX = mouseEvent.getX();
            mouseAnchorY = mouseEvent.getY();
            node.toFront();
        });

        node.setOnMouseDragged(mouseEvent -> {
            double newX = mouseEvent.getSceneX() - mouseAnchorX - stateAnchorPane.getLayoutX();
            double newY = mouseEvent.getSceneY() - mouseAnchorY - stateAnchorPane.getLayoutY();

            double minX = 2;
            double minY = 4;
            if(isQ0) {
                minY = -227;
            }

            double maxX = stateAnchorPane.getWidth() - node.getBoundsInParent().getWidth();
            double maxY = stateAnchorPane.getHeight() - node.getBoundsInParent().getHeight();

            if(isQ0) {
                newX = Math.max(minX, Math.min(newX, maxX - 0.5));
                newY = Math.max(minY, Math.min(newY, maxY + 46.5));
            }
            else {
                newX = Math.max(minX, Math.min(newX, maxX) - 2);
                newY = Math.max(minY, Math.min(newY, maxY - 1.5) - 1);
            }

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
