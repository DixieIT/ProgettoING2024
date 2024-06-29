module com.example.progettoing {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.progettoing to javafx.fxml;
    exports com.example.progettoing;
}