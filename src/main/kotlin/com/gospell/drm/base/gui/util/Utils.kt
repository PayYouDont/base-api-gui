package com.gospell.drm.base.gui.util

import cn.hutool.json.JSONUtil
import com.gospell.drm.base.gui.MainApplication
import com.gospell.drm.base.gui.controller.TabInfo
import com.gospell.drm.base.gui.data.ApiData
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
import javafx.scene.web.WebView
import org.w3c.dom.NodeList
import java.io.InputStream
import java.net.URL
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory

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

        fun TabPane.loadTabContent(
            tabData: MutableList<TabInfo>,
            borderPane: BorderPane
        ): MutableMap<String, Pane> {
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
                    val itemData = tabData.find { it.name == text }
                    if (tabContentItemManager.containsKey(text)) {
                        itemData?.apply {
                            tabContentItemManager[text]?.userData = data
                        }
                        borderPane.center = tabContentItemManager[text]
                    } else {
                        itemData?.apply {
                            val loader = FXMLLoader(fxml.getUrl())
                            tabContentItemManager[text] = loader.load<Pane?>().apply {
                                userData = data
                            }
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

        fun String.isJson(): Boolean {
            return trimStart().startsWith("{") || trimStart().startsWith("[")
        }

        fun String.isHtml(): Boolean {
            return trimStart().lowercase(Locale.getDefault()).startsWith("<!doctype html") || trimStart().lowercase(
                Locale.getDefault()
            ).startsWith("<html")
        }

        fun String.isXml(): Boolean {
            return trimStart().lowercase(Locale.getDefault())
                .startsWith("<?xml") || trimStart().lowercase(Locale.getDefault()).startsWith("<")
        }

        fun String.readXmlContent(): String {
            // 将字符串转换为 InputStream
            val inputStream = this.byteInputStream()

            // 使用 DocumentBuilderFactory 和 DocumentBuilder 解析 XML
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val document = builder.parse(inputStream)

            // 递归遍历所有节点并收集文本内容
            val content = StringBuilder()
            collectTextContent(document.documentElement, content)

            // 返回按空格隔开的字符串
            return content.toString().trim()
        }

        private fun collectTextContent(node: org.w3c.dom.Node, content: StringBuilder) {
            // 如果节点有子节点，递归处理子节点
            if (node.hasChildNodes()) {
                val nodeList: NodeList = node.childNodes
                for (i in 0 until nodeList.length) {
                    collectTextContent(nodeList.item(i), content)
                }
            } else {
                // 如果节点没有子节点且是文本节点，收集文本内容
                if (node.nodeType == org.w3c.dom.Node.TEXT_NODE) {
                    content.append("${node.nodeValue.trim()} ")
                }
            }
        }

        fun WebView.create(data: String, langType: String): WebView {
            val map = mapOf("data" to data, "langType" to langType)
            val json = JSONUtil.toJsonPrettyStr(map)
            return apply {
                isContextMenuEnabled = true
                engine.apply {
                    load("/dist/index.html".getResource())
                    loadWorker.stateProperty().addListener { _, _, _ ->
                        val script =
                            """(function() {var event = new CustomEvent('messageFromJavaFX', { detail: JSON.stringify($json)});window.dispatchEvent(event);})();""".trimIndent()
                        executeScript(script)
                    }
                }
            }
        }

    }
}