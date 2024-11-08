package com.gospell.drm.base.gui.util

import com.gospell.drm.base.gui.MainApplication
import com.gospell.drm.base.gui.controller.TabInfo
import com.gospell.drm.base.gui.util.Utils.Companion.getImage
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.control.TableRow
import javafx.scene.control.TableView
import javafx.scene.image.Image
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Pane
import java.io.InputStream
import java.net.URL

class Utils {
    companion object {
        fun String.getResource(): String {
            val resourceUrl = Utils::class.java.getResource(this)?.toExternalForm()
                ?: throw RuntimeException("Resource not found: $this")
            return resourceUrl
        }
        fun String.getImage(): Image {
            val resourceUrl = Utils::class.java.getResource("/icons/$this")
                ?: throw RuntimeException("Resource not found: /icons/$this")
            return Image(resourceUrl.toExternalForm())
        }

        fun String.getStream(): InputStream? {
            return MainApplication::class.java.getResourceAsStream(this)
        }

        fun String.getUrl(): URL? {
            return MainApplication::class.java.getResource(this)
        }

        fun <T> Node.findParent(parentType: Class<T>): T? {
            var currentNode: Parent? = parent
            while (currentNode != null) {
                if (parentType.isInstance(currentNode)) {
                    return parentType.cast(currentNode)
                }
                currentNode = currentNode.parent
            }
            return null
        }

        fun <T> Node.findChildren(id: String): T? {
            val children = this.lookupAll("#$id")
            for (child in children) {
                if (child.id == id) {
                    return child as T
                }
            }
            return null
        }

        fun TabPane.loadTabContent(tabData: MutableList<TabInfo>, borderPane: BorderPane):MutableMap<String, Pane> {
            val tabContentItemManager = mutableMapOf<String, Pane>()
            tabData.forEach {
                val title = it.name
                val tab = Tab(title)
                tab.isClosable = false
                tab.style = it.style
                tabs.add(tab)
            }
            selectionModel.selectedItemProperty().addListener { _, _, newValue ->
                newValue?.apply {
                    if (tabContentItemManager.containsKey(text)) {
                        borderPane.center = tabContentItemManager[text]
                    } else {
                        tabData.find { it.name == text }?.apply {
                            val loader = FXMLLoader(fxml.getUrl())
                            tabContentItemManager[text] = loader.load()
                            borderPane.center = tabContentItemManager[text]
                            controller = loader.getController()
                        }
                    }
                }
            }
            return tabContentItemManager
        }

        fun <T> TableView<T>.getTableRow(rowIndex: Int): TableRow<T>? {
            if (rowIndex < 0 || rowIndex >= items.size) {
                return null
            }
            // 获取所有的 TableRow 对象
            val rows = lookupAll(".table-row-cell").filterIsInstance<TableRow<T>>()
            // 查找指定索引的 TableRow
            return rows.firstOrNull { it.index == rowIndex }
        }
    }
}