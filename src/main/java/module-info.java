module soundinvestments {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;

    requires lombok;

    requires java.compiler;
    requires java.net.http;
    requires java.sql;
    requires transitive java.desktop;

    opens dhbw.si.app.ui to javafx.fxml;
    exports dhbw.si.app.ui to javafx.graphics, javafx.fxml;
    exports dhbw.si.app.communication;
    exports dhbw.si.dataRepo;
    exports dhbw.si.audio.playback to dhbw.si.app.communication;
}