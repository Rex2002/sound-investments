module soundinvestments {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires lombok;

    requires java.compiler;
    requires java.net.http;
    requires java.sql;
    requires java.desktop;

    opens dhbw.si.app.ui to javafx.fxml;
    exports dhbw.si.app.ui to javafx.graphics, javafx.fxml;
}