package com.gospell.drm.base.gui.controller

import com.gospell.drm.base.gui.util.Utils.Companion.getUrl
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.SplitPane
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane

class MainController {
    @FXML
    lateinit var hBox: HBox

    @FXML
    lateinit var rightSplitPanel: SplitPane

    @FXML
    lateinit var leftPanel: Pane

    @FXML
    lateinit var rightSplitPanelLeft: Pane

    @FXML
    lateinit var rightSplitPaneRight: Pane

    private lateinit var apiListController: ApiListController
    private lateinit var apiContentTabPanelController: ApiContentTabPanelController

    @FXML
    fun initialize() {
        // 加载 api-list.fxml 文件
        leftPanel.prefWidth = 100.0
        leftPanel.prefHeight = hBox.prefHeight
        initSplitPanelLeft()
        initSplitPanelRight()
    }

    private fun initSplitPanelLeft() {
        val fxmlLoader = FXMLLoader("api-list.fxml".getUrl())
        val anchorPane = fxmlLoader.load<BorderPane>()
        // 获取 ListView 对象
        apiListController = fxmlLoader.getController()
        rightSplitPanelLeft.children.add(anchorPane)
        rightSplitPanel.dividers[0].position = 250.0 / rightSplitPanel.prefWidth
        rightSplitPanel.dividers[0].positionProperty().addListener { _, _, newPos ->
            val minPosition = 250.0 / rightSplitPanel.prefWidth // 最小位置
            val maxPosition = 350.0 / rightSplitPanel.prefWidth // 最大位置
            if (newPos.toDouble() < minPosition) {
                rightSplitPanel.dividers[0].position = minPosition
            } else if (newPos.toDouble() > maxPosition) {
                rightSplitPanel.dividers[0].position = maxPosition
            }
        }
        hBox.widthProperty().addListener { _, _, _ ->
            rightSplitPanel.prefWidth = hBox.width - leftPanel.prefWidth
        }
        // 监听 rightSplitPanelLeft 的宽度变化，并更新 listView 的宽度
        rightSplitPanelLeft.widthProperty().addListener { _, _, newWidth ->
            apiListController.apiListRoot.prefWidth = newWidth.toDouble()
        }
    }

    private fun initSplitPanelRight() {
        val rightMain = FXMLLoader("api-content-main.fxml".getUrl())
        val rightMainAnchorPane = rightMain.load<BorderPane>()
        apiContentTabPanelController = rightMain.getController()
        rightSplitPaneRight.children.add(rightMainAnchorPane)
        // 监听 rightSplitPanelLeft 的宽度变化，并更新 listView 的宽度
        rightSplitPaneRight.widthProperty().addListener { _, _, newWidth ->
            rightMainAnchorPane.prefWidth = newWidth.toDouble()
        }
        rightSplitPaneRight.heightProperty().addListener { _, _, newHeight ->
            rightMainAnchorPane.prefHeight = newHeight.toDouble()
        }
    }
}