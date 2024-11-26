package com.gospell.drm.base.gui.controller

import com.gospell.drm.base.gui.data.ApiData
import com.gospell.drm.base.gui.util.Utils.Companion.getImage
import com.gospell.drm.base.gui.view.ApiDevHeaderTableView
import com.gospell.drm.base.gui.view.ApiDevTableView
import com.gospell.drm.base.gui.view.CustomTableView
import com.gospell.drm.base.gui.view.TableCellParams
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.Accordion
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox

class ApiTabContentDevHeaderController {
    @FXML
    lateinit var root: VBox

    @FXML
    lateinit var devAccordion: Accordion

    @FXML
    lateinit var expandImage: ImageView

    @FXML
    lateinit var publicHeaderTableView: CustomTableView<TableCellParams>

    @FXML
    lateinit var publicHeaderCustomTableView: CustomTableView<TableCellParams>

    @FXML
    fun initialize() {
        devAccordion.expandedPaneProperty().addListener { _, _, isNowExpanded ->
            expandImage.image = if (isNowExpanded == null) "eye-close.png".getImage() else "eye-open.png".getImage()
        }
        ApiDevHeaderTableView(
            publicHeaderTableView, mutableListOf(
                TableCellParams("Accept", "*/*"),
                TableCellParams("Accept-Encoding", "gzip, deflate, br"),
                TableCellParams("User-Agent", "PostmanRuntime-CustomApiRuntime/1.1.0")
            )
        )
        ApiDevTableView(publicHeaderCustomTableView)
        // 确保在布局完成后修改样式
        Platform.runLater {
            publicHeaderTableView.lookupAll(".column-header-background").forEach { node ->
                if (node.styleClass.contains("column-header-background")) {
                    node.style = "visibility:false;-fx-pref-height: 0;"
                }
            }
            root.userData?.apply {
                if (this is ApiData) {
                    header?.onEach { (t, u) ->
                        if (u.isNotEmpty()) {
                            publicHeaderCustomTableView.items.add(TableCellParams(t, u))
                            publicHeaderCustomTableView.refresh()
                        }
                    }
                }
            }
        }
    }

    fun getData(): MutableList<TableCellParams> {
        publicHeaderTableView.items.addAll(publicHeaderCustomTableView.items.filter { !it.disable })
        return publicHeaderTableView.items.toMutableList()
    }
}