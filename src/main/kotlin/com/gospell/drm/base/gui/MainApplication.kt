package com.gospell.drm.base.gui

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage

class MainApplication : Application() {
    override fun start(stage: Stage) {
        val fxmlLoader = FXMLLoader(MainApplication::class.java.getResource("main.fxml"))
        val scene = Scene(fxmlLoader.load(), 1200.0, 800.0)
        // 引用自定义CSS文件
        scene.stylesheets.add(MainApplication::class.java.getResource("/css/styles.css")?.toExternalForm())
        // 引用bootstrapFX css
        //scene.stylesheets.add(BootstrapFX.bootstrapFXStylesheet())
        stage.title = "Base Gui"
        stage.scene = scene
        stage.minWidth = 1200.0
        stage.minHeight = 800.0
        stage.show()
    }
}

fun main() {
    Application.launch(MainApplication::class.java)
}