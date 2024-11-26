package com.gospell.drm.base.gui.controller

import com.gospell.drm.base.gui.data.ApiData
import com.gospell.drm.base.gui.view.ApiDevTableView
import com.gospell.drm.base.gui.view.CustomTableView
import com.gospell.drm.base.gui.view.TableCellParams
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.layout.VBox

class ApiTabContentDevQueryController {
    @FXML
    lateinit var queryTableView: CustomTableView<TableCellParams>

    @FXML
    lateinit var root: VBox

    @FXML
    fun initialize() {
        ApiDevTableView(queryTableView)
        Platform.runLater {
            root.userData?.apply {
                if (this is ApiData) {
                    query?.apply {
                        queryTableView.items.addAll(this)
                        queryTableView.refresh()
                    }
                }
            }
        }
    }

    fun getData(): MutableList<TableCellParams> {
        val items = queryTableView.items
        return items.filter { !it.disable }.toMutableList()
    }
}