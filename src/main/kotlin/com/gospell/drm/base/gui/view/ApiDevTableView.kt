package com.gospell.drm.base.gui.view

import com.gospell.drm.base.gui.util.Utils.Companion.findChildren
import com.gospell.drm.base.gui.util.Utils.Companion.getUrl
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.HBox
import javafx.stage.FileChooser
import java.io.File


class TableCellParams(var key: String, var value: String) {
    var description: String = ""
    var keyType: String = "String"
    var selected: Boolean = true
    var disable = true
    override fun toString(): String {
        return "TableCellParams(key='$key', value='$value', description='$description', keyType='$keyType', selected=$selected, disable=$disable)"
    }
}

class ApiDevTableView(private val tableView: CustomTableView<TableCellParams>) {
    val valueTypes = mutableListOf(
        "Array",
        "Boolean",
        "Function",
        "NaN",
        "Number",
        "Float",
        "Integer",
        "Object",
        "RegExp",
        "String",
        "Undefined",
        "Null",
        "Date"
    )

    init {
        initTableView()
    }

    private fun initTableView() {
        val headers = mutableListOf("参数名", "参数值", "描述")
        val headerValues = mutableListOf("key", "value", "description")
        val data = mutableListOf(TableCellParams(" ", " "))
        tableView.apply {
            bindHeaderListener = { index, headerColumn ->
                headerColumn.cellValueFactory = PropertyValueFactory(headerValues[index])
            }
            bindCellListener = { item, itemData, index ->
                val loader = FXMLLoader("item-table-cell.fxml".getUrl())
                val view = loader.load<HBox>()
                view.prefWidth = 250.0
                val controller = loader.getController<ItemTableCellController>()
                controller.textField.text = item
                controller.dropDownMenuView.apply {
                    isDisable = false
                    selectedItem = itemData.keyType
                    bind(valueTypes, "item-request-type.fxml") { itemType, itemView ->
                        itemView.findChildren<Label>("label")?.text = itemType
                    }
                    itemSelectedListener = { itemView ->
                        itemView.findChildren<Label>("label")?.apply {
                            dropDownMenuController.dropDownBtn.text = text
                            itemData.keyType = text
                            if (text == "File") {
                                tableView.refresh()
                            }
                        }
                    }
                }
                when (index) {
                    0 -> {
                        controller.textField.textProperty().addListener { _, _, _ ->
                            if (items.lastIndex == items.indexOf(itemData)) {
                                tableView.items.add(TableCellParams(" ", " "))
                            }
                            itemData.key = controller.textField.text
                            if (itemData.disable) {
                                itemData.disable = false
                                controller.checkBox.isDisable = false
                            }
                        }
                        controller.checkBox.apply {
                            isVisible = true
                            isManaged = true
                            isDisable = itemData.disable
                            isSelected = itemData.selected
                            selectedProperty().addListener { _, _, newValue ->
                                itemData.selected = newValue
                                updateHeaderCheckBox(newValue)
                            }
                        }
                        controller.dropDownMenuView.isVisible = true
                        controller.dropDownMenuView.dropDownMenuController.dropDownBtn.text = itemData.keyType
                    }

                    1 -> {
                        controller.textField.apply {
                            if (itemData.keyType == "File") {
                                isEditable = false
                                text = if (itemData.value.isEmpty()) "请上传文件" else File(itemData.value).name
                                parent.styleClass.add("hand-btn")
                                setOnMouseClicked {
                                    handleFileUpload(itemData, this)
                                }
                            } else {
                                parent.styleClass.remove("hand-btn")
                                isEditable = true
                                textProperty().addListener { _, _, _ ->
                                    itemData.value = text
                                }
                            }
                        }
                    }

                    2 -> {
                        controller.refresh.onMouseClicked = EventHandler {
                            println("refresh")
                        }
                        controller.delete.onMouseClicked = EventHandler {
                            val dataIndex = items.indexOf(itemData)
                            if (!itemData.disable) {
                                items.remove(dataIndex, dataIndex + 1)
                                tableView.refresh()
                            }
                        }
                        controller.textField.textProperty().addListener { _, _, _ ->
                            itemData.description = controller.textField.text
                        }
                    }
                }
                view
            }
            bindRowListener = { row ->
                row.hoverProperty().addListener { _, _, newValue ->
                    row.childrenUnmodifiable[2].findChildren<Button>("refresh")?.apply {
                        isVisible = newValue
                        isManaged = newValue
                    }
                    row.childrenUnmodifiable[2].findChildren<Button>("delete")?.apply {
                        isVisible = newValue
                        isManaged = newValue
                    }
                }
            }
            bind(headers)
            items = FXCollections.observableArrayList(data)
            widthProperty().addListener { _, _, newValue ->
                columns[2].prefWidth = newValue.toDouble() - 537
            }
            headerCheckBoxSelectedListener = { newValue ->
                items.forEach { it.selected = newValue }
                refresh()
            }
        }
    }

    private fun updateHeaderCheckBox(newValue: Boolean) {
        tableView.apply {
            var all = newValue
            for (i in items.indices) {
                if (items[i].selected == all) {
                    continue
                } else if (!items[i].disable) {
                    all = !all
                    break
                }
            }
            columns[0].graphic.findChildren<TriStateCheckBox>("headerTriStateCheckBox")?.apply {
                if (all != newValue) {
                    indeterminate = true
                } else {
                    indeterminate = false
                    isSelected = newValue
                }
            }
        }
    }

    private fun handleFileUpload(cellParams: TableCellParams, textField: TextField) {
        FileChooser().apply {
            extensionFilters.addAll(
                FileChooser.ExtensionFilter("所有文件", "*.*"),
                FileChooser.ExtensionFilter("文本所有", "*.*"),
                FileChooser.ExtensionFilter("图像文件", "*.png", "*.jpg", "*.gif")
            )
            val file = showOpenDialog(null)
            if (file != null) {
                cellParams.value = file.absolutePath
                textField.text = file.name
            }
        }
    }
}