package com.gospell.drm.base.gui.controller

import com.gospell.drm.base.gui.util.Utils.Companion.create
import com.gospell.drm.base.gui.util.Utils.Companion.getUrl
import com.gospell.drm.base.gui.util.Utils.Companion.isHtml
import com.gospell.drm.base.gui.util.Utils.Companion.isJson
import com.gospell.drm.base.gui.util.Utils.Companion.isXml
import com.gospell.drm.base.gui.util.Utils.Companion.readXmlContent
import com.gospell.drm.base.gui.view.ApiDevHeaderTableView
import com.gospell.drm.base.gui.view.CustomTableView
import com.gospell.drm.base.gui.view.TableCellParams
import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.control.TextArea
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.web.WebView
import okhttp3.Headers

class ResultData(var successData: String? = null, var errorData: String? = null)
class ApiTabContentDevResResultController {
    @FXML
    lateinit var root: VBox

    @FXML
    lateinit var dataViewTabPane: TabPane

    @FXML
    lateinit var dataViewBorderPane: BorderPane
    private val tabData = mutableListOf<TabInfo>()
    private lateinit var selectItemData: String

    @FXML
    fun initialize() {
        tabData.add(TabInfo("美化", "", "-fx-pref-width: 48"))
        tabData.add(TabInfo("原生", "", "-fx-pref-width: 48"))
        tabData.add(TabInfo("预览", "", "-fx-pref-width: 48;"))
        tabData.add(TabInfo("可视化", "", "-fx-pref-width: 56"))
        selectItemData = tabData[0].name
        tabData.forEach {
            val title = it.name
            val tab = Tab(title)
            tab.isClosable = false
            tab.style = it.style
            dataViewTabPane.tabs.add(tab)
        }
        dataViewTabPane.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            newValue?.apply {
                selectItemData = text
                loadResult()
            }
        }
        dataViewTabPane.selectionModel.select(dataViewTabPane.tabs[0])
        loadResult()
        Platform.runLater {
            root.userData?.apply {
                if (this is SimpleObjectProperty<*>) {
                    addListener { _, _, newValue ->
                        newValue as ResultData
                        if (newValue.errorData != null) {
                            loadResult(null, newValue.errorData)
                        } else if (newValue.successData != null) {
                            loadResult(newValue.successData, null)
                        }
                    }
                }
            }
        }
    }

    private fun loadResult(resultSuccess: String? = null, resultError: String? = null) {
        if (resultSuccess == null && resultError == null) {
            dataViewBorderPane.center = FXMLLoader("api-tab-content-dev-res-empty.fxml".getUrl()).load()
            return
        }
        val result = resultSuccess ?: resultError
        when (selectItemData) {
            tabData[0].name -> {
                println("result: $result")
                result.toString().apply {
                    val langType = when {
                        isJson() -> "json"
                        isXml() -> "xml"
                        isHtml() -> "html"
                        else -> "text"
                    }
                    loadJSonResult(this, langType)
                }
            }

            tabData[2].name -> {
                result.toString().apply {
                    if (isHtml()) {
                        loadHtmlResult(this)
                    } else if (isXml()) {
                        loadXmlResult(this)
                    } else {
                        loadTextArea(16.0, this)
                    }
                }
            }

            else -> {
                loadTextArea(12.0, result.toString())
            }
        }
    }

    private fun loadJSonResult(result: String, langType: String) {
        dataViewBorderPane.center = WebView().create(result, langType)
    }

    private fun loadHtmlResult(result: String) {
        val webView = WebView()
        webView.engine.loadContent(result)
        dataViewBorderPane.center = webView
    }

    private fun loadXmlResult(result: String?) {
        result?.apply {
            val textArea = TextArea(readXmlContent())
            textArea.font = Font.font(16.0)
            dataViewBorderPane.center = textArea
        }
    }

    private fun loadTextArea(fontSize: Double, result: String) {
        val textArea = TextArea(result)
        textArea.font = Font.font(fontSize)
        dataViewBorderPane.center = textArea
    }
}

class ApiTabContentDevReqHeaderController {
    @FXML
    private lateinit var rootVBox: VBox

    @FXML
    private lateinit var headerTableView: CustomTableView<TableCellParams>
    private lateinit var emptyDataView: VBox

    @FXML
    fun initialize() {
        emptyDataView = FXMLLoader("api-tab-content-dev-res-empty.fxml".getUrl()).load()
        refreshView()
        Platform.runLater {
            rootVBox.userData?.apply {
                if (this is SimpleObjectProperty<*>) {
                    get()?.apply {
                        refreshView(this as Headers)
                    }
                    addListener { _, _, newValue ->
                        refreshView(newValue as Headers)
                    }
                }
            }
        }
    }

    private fun refreshView(headers: Headers? = null) {
        headerTableView.apply {
            isVisible = headers != null
            isManaged = headers != null
            if (headers == null) {
                rootVBox.children.add(emptyDataView)
            } else {
                rootVBox.children.remove(emptyDataView)
                headerTableView.items.clear()
                ApiDevHeaderTableView(headerTableView, getData(headers))
            }
        }
    }

    private fun getData(headersData: Headers): MutableList<TableCellParams> {
        val headers = mutableListOf<TableCellParams>()
        headersData.onEach {
            headers.add(TableCellParams(it.first, it.second))
        }
        return headers
    }
}

class ApiTabContentDevResHeaderController {
    @FXML
    private lateinit var rootVBox: VBox

    @FXML
    private lateinit var headerTableView: CustomTableView<TableCellParams>
    private lateinit var emptyDataView: VBox

    @FXML
    fun initialize() {
        emptyDataView = FXMLLoader("api-tab-content-dev-res-empty.fxml".getUrl()).load()
        refreshView()
        Platform.runLater {
            rootVBox.userData?.apply {
                if (this is SimpleObjectProperty<*>) {
                    get()?.apply {
                        refreshView(this as Headers)
                    }
                    addListener { _, _, newValue ->
                        refreshView(newValue as Headers)
                    }
                }
            }
        }
    }

    private fun refreshView(headers: Headers? = null) {
        if (headers == null) {
            rootVBox.children.add(emptyDataView)
            headerTableView.isVisible = false
            headerTableView.isManaged = false
        } else {
            rootVBox.children.remove(emptyDataView)
            headerTableView.items.clear()
            ApiDevHeaderTableView(headerTableView, getData(headers))
            headerTableView.isVisible = true
            headerTableView.isManaged = true
        }
    }

    private fun getData(headers: Headers): MutableList<TableCellParams> {
        val header = mutableListOf<TableCellParams>()
        headers.onEach {
            header.add(TableCellParams(it.first, it.second))
        }
        return header
    }

}