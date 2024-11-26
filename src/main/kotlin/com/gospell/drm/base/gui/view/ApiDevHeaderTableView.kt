package com.gospell.drm.base.gui.view

import com.gospell.drm.base.gui.util.Utils.Companion.getUrl
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.fxml.FXMLLoader
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.HBox


class ApiDevHeaderTableView(
    private val headerTableView: CustomTableView<TableCellParams>,
    private val data: MutableList<TableCellParams>
) {
    init {
        initPublicHeaderTableView()
        Platform.runLater {
            headerTableView.lookupAll(".column-header-background").forEach { node ->
                if (node.styleClass.contains("column-header-background")) {
                    node.style = " -fx-background-color: transparent !important;visibility:false !important;-fx-pref-height: 0 !important;"
                }
            }
        }
    }

    private fun initPublicHeaderTableView() {
        val headers = mutableListOf("key", "value")
        headerTableView.bindHeaderListener = { index, headerColumn ->
            headerColumn.cellValueFactory = PropertyValueFactory(headers[index])
        }
        headerTableView.bindCellListener = { item, _, index ->
            val loader = FXMLLoader("item-table-cell.fxml".getUrl())
            val view = loader.load<HBox>()
            view.prefWidth = 300.0
            val controller = loader.getController<ItemTableCellController>()
            controller.textField.text = item
            controller.textField.isDisable = true
            if (index == 0) {
                controller.dropDownMenuView.dropDownMenuController.dropDownBtn.apply {
                    isVisible = true
                    text = "String"
                    isDisable = true
                }
                controller.dropDownMenuView.dropDownMenuController.root.style = "-fx-background-color:#f5f5f5"
                controller.checkBox.isVisible = true
                controller.checkBox.isDisable = false
            } else {
                controller.dropDownMenuView.isVisible = false
                controller.checkBox.isVisible = false
            }
            view
        }
        headerTableView.apply {
            bind(headers)
            items = FXCollections.observableArrayList(data)
        }
    }
}