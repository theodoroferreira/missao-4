module com.acme.missao4 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.net.http;
    requires com.google.gson;
    requires javafx.graphics;
    requires org.apache.commons.lang3;

    opens com.acme.missao4 to javafx.fxml;
    exports com.acme.missao4;
}