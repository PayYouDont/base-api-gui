package com.gospell.drm.base.gui.view

import com.gospell.drm.base.gui.util.Utils.Companion.getUrl
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ListView
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.stage.Popup

class DropDownMenuController {
    @FXML
    lateinit var root: HBox

    @FXML
    lateinit var image: ImageView

    @FXML
    lateinit var dropDownBtn: Button

    @FXML
    lateinit var dropDownImage: ImageView

    @FXML
    fun initialize() {
        dropDownBtn.onMouseClicked = EventHandler {
            root.fireEvent(it)
        }
        image.onMouseClicked = EventHandler {
            root.fireEvent(it)
        }
        dropDownImage.onMouseClicked
    }

}

class DropDownMenuView : BorderPane() {
    private var popup = Popup()
    var itemSelectedListener: ((item: Node) -> Unit)? = null
    var dropDownMenuController: DropDownMenuController
    var selectedItem: Any? = null

    init {
        val loader = FXMLLoader("drop-down-menu.fxml".getUrl())
        val root = loader.load<HBox>()
        center = root
        dropDownMenuController = loader.getController()
        root.onMouseClicked = EventHandler { showDropdownMenu() }
        popup.isAutoHide = true
    }

    fun <T> bind(list: MutableList<T>, itemFxml: String, bindListener: (itemData: T, itemView: Node) -> Unit) {
        val listView = ListView<Node>()
        listView.prefWidth = prefWidth
        listView.styleClass.add("dropDown-listView")
        popup.content.add(listView)
        list.forEach { itemData ->
            val item = FXMLLoader(itemFxml.getUrl())
            val itemView = item.load<Node>()
            listView.items.add(itemView)
            bindListener.invoke(itemData, itemView)
            itemView.onMouseClicked = EventHandler {
                itemSelectedListener?.invoke(itemView)
                selectedItem = itemData
                popup.hide()
            }
        }
        if(selectedItem == null){
            listView.selectionModel.select(0)
            if(list.isNotEmpty()){
                selectedItem = list[0]
            }
        }else{
            selectedItem.apply {
                this as T
                listView.selectionModel.select(list.indexOf(this))
            }
        }

    }

    private fun showDropdownMenu() {
        if (!popup.isShowing) {
            // 转换为场景坐标
            val posInScene = localToScene(boundsInLocal.minX, boundsInLocal.maxY)
            // 转换为屏幕坐标
            val posInScreen = localToScreen(posInScene)
            val x = posInScreen.x - posInScene.x - 10
            val y = posInScreen.y - posInScene.y + boundsInLocal.height - 5
            // 强制重新布局和应用样式
            popup.content.forEach { it.applyCss() }
            popup.content.forEach { it.parent?.layout() }
            // 显示 Popup
            popup.show(dropDownMenuController.root.scene.window, x, y)
        } else {
            popup.hide()
        }
    }
}


