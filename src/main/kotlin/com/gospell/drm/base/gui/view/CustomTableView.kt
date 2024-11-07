package com.gospell.drm.base.gui.view

import javafx.beans.binding.Bindings
import javafx.collections.ListChangeListener
import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.util.Callback

class ItemTableCellController {
    @FXML
    lateinit var checkBox: CheckBox

    @FXML
    lateinit var textField: TextField

    @FXML
    lateinit var dropDownMenuView: DropDownMenuView

    @FXML
    lateinit var refresh: Button

    @FXML
    lateinit var delete: Button

    @FXML
    fun initialize() {
        textField.focusedProperty().addListener { _, _, newValue ->
            if (newValue) {
                textField.parent.style = "-fx-border-color: #f78b2b;-fx-background-color: white;"
            } else {
                textField.parent.style = "-fx-background-color: transparent"
            }
        }
    }
}

class CustomTableView<T> : TableView<T>() {
    var bindHeaderListener: ((index: Int, headerColumn: TableColumn<T, String>) -> Unit)? = null
    var bindCellListener: ((item: String?, data: T, columnIndex: Int) -> Node)? = null
    var bindRowListener: ((row: TableRow<T>) -> Unit)? = null
    var headerCheckBoxSelectedListener: ((selected: Boolean) -> Unit)? = null

    init {
        isEditable = false
        // 设置选择模式
        selectionModel.selectionMode = SelectionMode.SINGLE
    }

    fun bind(headers: MutableList<String>) {
        for ((index, s) in headers.withIndex()) {
            val column = if (index == 0) {
                object : TableColumn<T, String>(s) {
                    init {
                        val hBox = HBox()
                        hBox.padding = Insets(0.0, 10.0, 0.0, 10.0)
                        val checkBox = TriStateCheckBox()
                        checkBox.isSelected = true
                        checkBox.id = "headerTriStateCheckBox"
                        checkBox.selectedProperty().addListener { _, _, newValue ->
                            headerCheckBoxSelectedListener?.invoke(newValue)
                        }
                        hBox.children.add(checkBox)
                        graphic = hBox
                    }
                }
            } else {
                TableColumn<T, String>(s)
            }
            column.isSortable = false
            column.isReorderable = false
            bindHeaderListener?.invoke(index, column)
            column.cellFactory = Callback<TableColumn<T, String>, TableCell<T, String>> {
                object : TableCell<T, String>() {
                    override fun updateItem(item: String?, empty: Boolean) {
                        super.updateItem(item, empty)
                        graphic = if (empty || item == null) {
                            null
                        } else {
                            val view = bindCellListener?.invoke(item, items[getIndex()], index)
                            view
                        }
                    }
                }
            }
            columns.add(column)
        }
        setRowFactory {
            val row = TableRow<T>()
            bindRowListener?.invoke(row)
            row.itemProperty().addListener { _, _, newValue ->
                if (newValue != null) {
                    row.setOnMouseEntered {
                        selectionModel.select(row.index)
                    }
                    row.setOnMouseExited {
                        if (!row.isFocused && !row.isSelected) {
                            selectionModel.clearSelection()
                        }
                    }
                }
            }
            row
        }
        items.addListener { _: ListChangeListener.Change<out T> ->
            prefHeightProperty().bind(
                Bindings.createDoubleBinding(
                    {
                        val itemCount = items.size
                        val headerHeight = 25.0 // 表头高度
                        val rowHeight = 20.0 // 每行高度
                        headerHeight + (itemCount * rowHeight)
                    },
                    items
                )
            )
        }
    }
}
