module com.gospell.drm.base.gui.controller {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;
    //requires org.kordamp.bootstrapfx.core;
    requires javafx.graphics;
    requires java.net.http;
    requires javafx.web;
    requires cn.hutool.json;
    requires java.sql;
    requires okhttp3;
    requires cn.hutool.core;
    opens com.gospell.drm.base.gui.controller to javafx.fxml;
    opens com.gospell.drm.base.gui.view to javafx.fxml;
    exports com.gospell.drm.base.gui.controller;
    exports com.gospell.drm.base.gui.view;
    exports com.gospell.drm.base.gui to javafx.graphics;

}
