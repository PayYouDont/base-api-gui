package com.gospell.drm.base.gui.controller

import com.gospell.drm.base.gui.MainApplication
import com.gospell.drm.base.gui.util.Utils.Companion.getUrl
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Popup


open class ApiListController {
    @FXML
    lateinit var apiListRoot: VBox

    @FXML
    lateinit var textFieldBox: HBox

    @FXML
    lateinit var textField: TextField

    @FXML
    lateinit var listView: ListView<AnchorPane>

    @FXML
    lateinit var searchBtn: Button

    @FXML
    lateinit var addBtnXml: Button


    @FXML
    fun initialize() {
        val anchorPane = AnchorPane()
        val addBtn = Button("+ 新建")
        addBtn.id = "addBtn"
        addBtn.styleClass.add("full-width-button")
        addBtn.onAction = EventHandler {
            ApiNewPopupController.showDropdownMenu(anchorPane)
        }
        AnchorPane.setLeftAnchor(addBtn, 0.0)
        AnchorPane.setRightAnchor(addBtn, 0.0)
        anchorPane.children.add(addBtn)
        listView.items.addAll(anchorPane)
        textFieldBox.widthProperty().addListener { _, _, newWidth ->
            (textFieldBox.children[0] as HBox).prefWidth = newWidth.toDouble() - 40
        }
        textField.focusedProperty().addListener { _, _, isNowFocused ->
            if (isNowFocused) {
                textField.parent.styleClass.add("focused")
            } else {
                textField.parent.styleClass.remove("focused");
            }
        }
        addBtnXml.onAction = EventHandler {
            ApiNewPopupController.showDropdownMenu(addBtnXml)
        }
        addApiMenu()
        addApiMenu()
    }

    private fun addApiMenu() {
        // 加载 api-list.fxml 文件
        val fxmlLoader = FXMLLoader(MainApplication::class.java.getResource("api-list-item.fxml"))
        val anchorPane = fxmlLoader.load<AnchorPane>()
        listView.items.add(0, anchorPane)
    }


}

open class ApiListItemController {
    @FXML
    lateinit var apiListItemRoot: AnchorPane

    @FXML
    lateinit var apiItemHBox: HBox

    @FXML
    lateinit var apiMethod: Label

    @FXML
    lateinit var apiName: Label

    @FXML
    lateinit var tooltipLabel: Label

    @FXML
    fun initialize() {
        apiMethod.text = "GET"
        apiName.text = "获取用户信息"
        // 设置 AnchorPane 的布局参数
        AnchorPane.setLeftAnchor(apiItemHBox, 0.0)
        AnchorPane.setRightAnchor(apiItemHBox, 0.0)
    }
}

open class ApiNewPopupController {
    @FXML
    lateinit var dropdownMenu: VBox

    @FXML
    fun initialize() {

    }

    companion object {
        private var popup = Popup()

        init {
            val root = FXMLLoader("api-new-popup.fxml".getUrl())
            val dialogPane = root.load<AnchorPane>()
            popup.content.add(dialogPane)
            popup.isAutoHide = true
        }

        fun showDropdownMenu(target: Node) {
            if (!popup.isShowing) {
                // 获取按钮的边界
                val boundsInLocal = target.boundsInLocal
                // 转换为场景坐标
                val posInScene = target.localToScene(boundsInLocal.minX, boundsInLocal.maxY)
                // 转换为屏幕坐标
                val posInScreen = target.localToScreen(posInScene)
                (popup.content[0] as AnchorPane).children.find { it.id == "dropdownMenu" }?.apply {
                    (this as VBox).prefWidth = boundsInLocal.width.coerceAtLeast(200.0)
                }
                val x = posInScreen.x - posInScene.x - 10
                val y = posInScreen.y - posInScene.y + boundsInLocal.height - 5
                // 显示 Popup
                popup.show(target.scene.window, x, y)
            } else {
                popup.hide()
            }
        }
    }

    @FXML
    fun addNewInterface() {
        println("1")
    }
}