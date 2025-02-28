package com.example.progettoing;

import javafx.scene.Node;

public class DraggableMaker {

    private double mouseAnchorX;
    private double mouseAnchorY;

    public void makeDraggable(Node node){

        node.setOnMousePressed(mouseEvent -> {
            mouseAnchorX = mouseEvent.getX();
            mouseAnchorY = mouseEvent.getY();
        });

        node.setOnMouseDragged(mouseEvent -> {
            node.setLayoutX(mouseEvent.getSceneX() - mouseAnchorX);
            node.setLayoutY(mouseEvent.getSceneY() - mouseAnchorY);



        });
        node.setOnMouseReleased(mouseEvent -> {
            double newX = node.getLayoutX();
            double newY = node.getLayoutY();
            System.out.println("New position: (" + newX + ", " + newY + ")");

        });

    }

}