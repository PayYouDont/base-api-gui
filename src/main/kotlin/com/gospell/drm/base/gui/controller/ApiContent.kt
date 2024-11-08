package com.gospell.drm.base.gui.controller

import cn.hutool.json.JSONObject
import cn.hutool.json.JSONUtil
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.gospell.drm.base.gui.util.Utils.Companion.findChildren
import com.gospell.drm.base.gui.util.Utils.Companion.findParent
import com.gospell.drm.base.gui.util.Utils.Companion.getImage
import com.gospell.drm.base.gui.util.Utils.Companion.getUrl
import com.gospell.drm.base.gui.util.Utils.Companion.loadTabContent
import com.gospell.drm.base.gui.view.*
import javafx.application.Platform
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.concurrent.Worker
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.control.cell.TreeItemPropertyValueFactory
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.text.Text
import javafx.scene.web.WebView
import javafx.util.Callback
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers
import java.util.*


class ApiContentTabPanelController {
    @FXML
    lateinit var tabRoot: TabPane

    @FXML
    lateinit var addTab: Tab

    @FXML
    lateinit var moreTab: Tab

    @FXML
    fun initialize() {
        // 创建自定义 Tab 并添加到 TabPane 中
        val tab1 = createCustomTab("POST", "新建接口1")
        val tab2 = createCustomTab("GET", "新建接口2")
        tabRoot.tabs.add(0, tab1)
        tabRoot.tabs.add(1, tab2)
        tabRoot.selectionModel.select(tab1) // 选择新添加的 Tab
    }

    private fun createCustomTab(method: String, title: String): Tab {
        val hBoxRoot = FXMLLoader("item-api-tab-label.fxml".getUrl())
        val hBox = hBoxRoot.load<AnchorPane>()
        val controller = hBoxRoot.getController<ItemApiTabLabelController>()
        controller.method.text = method
        controller.method.styleClass.add(method.lowercase(Locale.getDefault()))
        controller.title.text = title
        val tab = Tab()
        tab.graphic = hBox
        tab.styleClass.add("custom-tab")
        tab.isClosable = false
        val root = FXMLLoader("api-tab-content.fxml".getUrl())
        val content = root.load<BorderPane>()
        tab.content = content
        return tab
    }

    fun showApiMenu() {
        println(1)
        ApiNewPopupController.showDropdownMenu(addTab.graphic)
    }
}

class TabInfo(var name: String, var fxml: String, var style: String) {
    var controller: Any? = null
}

class ApiContentController {
    @FXML
    lateinit var tabPane: TabPane

    @FXML
    lateinit var apiContentItem: BorderPane
    private val tabData = mutableListOf<TabInfo>()

    @FXML
    fun initialize() {
        tabData.add(TabInfo("设计", "api-tab-content-design.fxml", "-fx-pref-width: 48"))
        tabData.add(TabInfo("调试", "api-tab-content-dev.fxml", "-fx-pref-width: 48"))
        tabData.add(TabInfo("接口用例", "api-tab-content-example.fxml", "-fx-pref-width: 70;"))
        tabData.add(TabInfo("一键压测", "api-tab-content-test.fxml", "-fx-pref-width: 70"))
        tabData.add(TabInfo("Mock", "api-tab-content-mock.fxml", "-fx-pref-width: 56"))
        tabPane.loadTabContent(tabData, apiContentItem)
        tabPane.selectionModel.select(tabPane.tabs[1])
    }
}

class ApiTabContentDevResResultController {
    @FXML
    lateinit var dataViewTabPane: TabPane

    @FXML
    lateinit var dataViewBorderPane: BorderPane
    private val tabData = mutableListOf<TabInfo>()

    @FXML
    fun initialize() {
        tabData.add(TabInfo("美化", "", "-fx-pref-width: 48"))
        tabData.add(TabInfo("原生", "", "-fx-pref-width: 48"))
        tabData.add(TabInfo("预览", "", "-fx-pref-width: 48;"))
        tabData.add(TabInfo("可视化", "", "-fx-pref-width: 56"))
        //dataViewTabPane.loadTabContent(tabData, dataViewBorderPane)
        val str =
            "{\n" +
                    "  \"name\": \"John Doe\",\n" +
                    "  \"age\": 30,\n" +
                    "  \"address\": {\n" +
                    "    \"street\": \"123 Main St\",\n" +
                    "    \"city\": \"Anytown\",\n" +
                    "    \"state\": \"Anystate\",\n" +
                    "    \"zip\": \"12345\"\n" +
                    "  },\n" +
                    "  \"phoneNumbers\": [\n" +
                    "    { \"type\": \"home\", \"number\": \"555-1234\" },\n" +
                    "    { \"type\": \"mobile\", \"number\": \"555-5678\" }\n" +
                    "  ],\n" +
                    "  \"children\": [\n" +
                    "    { \"name\": \"Alice\", \"age\": 10, \"hobbies\": [\"reading\", \"painting\"] },\n" +
                    "    { \"name\": \"Bob\", \"age\": 8, \"hobbies\": [\"gaming\", \"sports\"] }\n" +
                    "  ],\n" +
                    "  \"married\": true,\n" +
                    "  \"favorites\": {\n" +
                    "    \"colors\": [\"red\", \"blue\", \"green\"],\n" +
                    "    \"foods\": [\"pizza\", \"sushi\", \"pasta\"]\n" +
                    "  }\n" +
                    "}"
        val json = JSONUtil.toJsonPrettyStr(str)
        // 创建美化后的 HTML 内容
        val htmlContent = GlobalData.getHtmlJson(json)
        //println(htmlContent)
        tabData.forEach {
            val title = it.name
            val tab = Tab(title)
            tab.isClosable = false
            tab.style = it.style
            dataViewTabPane.tabs.add(tab)
        }
        dataViewTabPane.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            newValue?.apply {
                loadTab(text, htmlContent)
            }
        }
        dataViewTabPane.selectionModel.select(dataViewTabPane.tabs[0])
        loadTab(tabData[0].name, htmlContent)
    }

    private fun loadTab(text: String, htmlContent: String) {
        val tabContentItemManager = mutableMapOf<String, Node>()
        if (tabContentItemManager.containsKey(text)) {
            dataViewBorderPane.center = tabContentItemManager[text]
        } else {
            tabData.find { it.name == text }?.apply {
                // 创建 WebView 控件
                val webView = WebView()
                webView.engine.apply {
                    loadContent(htmlContent, "text/html")
                    //load("http://localhost:8080")
                    loadWorker.stateProperty().addListener { _, _, newValue ->
                        println("Loading state changed: $newValue")
                        if (newValue == Worker.State.FAILED) {
                            System.err.println("Failed to load content: $location")
                            val exception = loadWorker.exception
                            exception?.printStackTrace()
                        }
                    }

                }
                tabContentItemManager[text] = webView
                dataViewBorderPane.center = tabContentItemManager[text]
            }
        }
    }
}
data class TreeItemData(val key: String, val value: String)
class ApiTabContentDesignController {

}

class ApiHelper {
    companion object {
        var url: String = ""
        var headers = mutableListOf<TableCellParams>()
        var query = mutableListOf<TableCellParams>()

        // 创建 HttpClient 实例
        private val httpClient = HttpClient.newBuilder().build()
        fun post(successListener: (response: HttpResponse<*>) -> Unit, errorListener: (error: Throwable) -> Unit) {
            var queryParams = ""
            query.forEach { queryParams += it.key + "=" + it.value + "&" }
            if (queryParams != "" && queryParams.last() == '&') {
                queryParams = queryParams.substring(0, queryParams.length - 1)
            }
            val request = HttpRequest.newBuilder()
                .uri(URI.create(toUrl()))
                .headers(*getHeader())
                .POST(HttpRequest.BodyPublishers.ofString(queryParams))
                .build()
            println("request: $request")
            httpClient.sendAsync(request, BodyHandlers.ofString())?.apply {
                thenAccept {
                    successListener.invoke(it)
                }
                exceptionally { e ->
                    errorListener.invoke(e)
                    return@exceptionally null
                }
            }
        }

        fun get(successListener: (response: HttpResponse<*>) -> Unit, errorListener: (error: Throwable) -> Unit) {
            val request = HttpRequest.newBuilder()
                .uri(URI.create(toUrl()))
                .headers(*getHeader())
                .GET()
                .build()
            httpClient.sendAsync(request, BodyHandlers.ofString())?.apply {
                thenAccept {
                    successListener.invoke(it)
                }
                exceptionally { e ->
                    errorListener.invoke(e)
                    return@exceptionally null
                }
            }
        }

        fun put(successListener: (response: HttpResponse<*>) -> Unit, errorListener: (error: Throwable) -> Unit) {
            val request = HttpRequest.newBuilder()
                .uri(URI.create(toUrl()))
                .headers(*getHeader())
                .PUT(HttpRequest.BodyPublishers.ofString(""))
                .build()
            httpClient.sendAsync(request, BodyHandlers.ofString())?.apply {
                thenAccept {
                    successListener.invoke(it)
                }
                exceptionally { e ->
                    errorListener.invoke(e)
                    return@exceptionally null
                }
            }
        }

        private fun toUrl(): String {
            if (query.isNotEmpty()) {
                url += "?"
                query.forEach { url += it.key + "=" + it.value + "&" }
                if (url.last() == '&') {
                    url = url.substring(0, url.length - 1)
                }
            }
            return url
        }

        private fun getHeader(): Array<String> {
            val header = arrayListOf<String>()
            if (headers.isNotEmpty()) {
                headers.forEach {
                    if (it.key.isNotEmpty() && it.value.isNotEmpty()) {
                        header.add(it.key)
                        header.add(it.value)
                    }
                }
            }
            return header.toTypedArray()
        }
    }
}

class ApiTabContentDevController {
    @FXML
    lateinit var reqMethodType: DropDownMenuView

    @FXML
    lateinit var httpVersion: DropDownMenuView

    @FXML
    lateinit var urlText: TextField

    @FXML
    lateinit var sendBtn: Button

    @FXML
    lateinit var saveBtn: Button
    private val types = mutableListOf("POST", "GET", "PUT")
    private val httpVersions = mutableListOf("http/1.1", "http/2")

    /************paramTypeViews******************/
    @FXML
    lateinit var paramTypeTabPane: TabPane

    @FXML
    lateinit var devSplitPane: SplitPane

    @FXML
    lateinit var paramBorderPane: BorderPane

    @FXML
    lateinit var responseTabPane: TabPane

    @FXML
    lateinit var responseBorderPane: BorderPane

    private val paramTypeTabData = mutableListOf<TabInfo>()
    private lateinit var paramTypePaneManager: MutableMap<String, Pane>
    private val responseTabData = mutableListOf<TabInfo>()
    private lateinit var responsePaneManager: MutableMap<String, Pane>


    @FXML
    fun initialize() {
        initParamUrlViews()
        initParamTypeViews()
        initResponsePane()
        urlText.apply {
            focusedProperty().addListener { _, _, newValue ->
                if (newValue) {
                    urlText.style = "-fx-border-color: #f78b2b; -fx-background-color: white;"
                } else {
                    urlText.style = "-fx-background-color: transparent"
                }
            }
            textProperty().addListener { _, _, newValue ->
                setQueryData(newValue)
            }
        }
        sendBtn.onMouseClicked = EventHandler {
            request()
        }

    }

    private fun request() {
        urlText.text.apply {
            val url = if (this.contains("?")) {
                this.split("?")[0]
            } else {
                this
            }
            ApiHelper.url = url
        }
        (paramTypeTabData[0].controller as ApiTabContentDevHeaderController).apply {
            ApiHelper.headers = getData()
        }
        (paramTypeTabData[1].controller as ApiTabContentDevQueryController).apply {
            ApiHelper.query = getData()
        }
        when (reqMethodType.selectedItem) {
            types[0] -> {
                ApiHelper.post({ requestSuccess(it) }, { requestError(it) })
            }

            types[1] -> {
                ApiHelper.get({ requestSuccess(it) }, { requestError(it) })
            }

            types[2] -> {
                ApiHelper.put({ requestSuccess(it) }, { requestError(it) })
            }
        }
    }

    private fun requestSuccess(response: HttpResponse<*>) {
        Platform.runLater {

        }
    }

    private fun requestError(throwable: Throwable) {
        Platform.runLater {

        }
    }

    private fun setQueryData(url: String) {
        if (url.isNotEmpty() && url.contains("?")) {
            val queryParams = url.split("?")[1]
            var params = listOf<String>()
            if (queryParams.contains("&")) {
                params = queryParams.split("&")
            } else if (queryParams.contains(",")) {
                params = queryParams.split(",")
            }
            if (params.isNotEmpty()) {
                val cells = mutableListOf<TableCellParams>()
                params.forEach {
                    val param = it.split("=")
                    val cellParams = TableCellParams(param[0], param[1])
                    cellParams.disable = false
                    cells.add(cellParams)
                }
                if (cells.isNotEmpty()) {
                    selectTab(1)
                    (paramTypeTabData[1].controller as ApiTabContentDevQueryController).apply {
                        queryTableView.items.clear()
                        queryTableView.items.addAll(cells)
                    }
                }
            }
        }
    }

    private fun initParamUrlViews() {
        reqMethodType.bind(types, "item-request-type.fxml") { itemData, itemView ->
            itemView.findChildren<Label>("label")?.text = itemData
        }
        reqMethodType.itemSelectedListener = { item ->
            item.findChildren<Label>("label")?.apply {
                setRequestType(text)
            }
        }
        setRequestType(types[0])
        httpVersion.bind(httpVersions, "item-request-type.fxml") { itemData, itemView ->
            itemView.findChildren<Label>("label")?.text = itemData
        }
        httpVersion.itemSelectedListener = { item ->
            item.findChildren<Label>("label")?.apply {
                setHttpVersion(text)
            }
        }
        setHttpVersion(httpVersions[0])
    }

    private fun initParamTypeViews() {
        paramTypeTabData.add(TabInfo("Header", "api-tab-content-dev-header.fxml", "-fx-pref-width: 70"))
        paramTypeTabData.add(TabInfo("Query", "api-tab-content-dev-query.fxml", "-fx-pref-width: 64"))
        paramTypeTabData.add(TabInfo("Path", "api-tab-content-dev-path.fxml", "-fx-pref-width: 58;"))
        paramTypeTabData.add(TabInfo("Body", "api-tab-content-dev-body.fxml", "-fx-pref-width: 58"))
        paramTypeTabData.add(TabInfo("认证", "api-tab-content-dev-auth.fxml", "-fx-pref-width: 58"))
        paramTypeTabData.add(TabInfo("Cookie", "api-tab-content-dev-cookie.fxml", "-fx-pref-width: 70"))
        paramTypeTabData.add(TabInfo("预执行操作", "api-tab-content-dev-pre.fxml", "-fx-pref-width: 90"))
        paramTypeTabData.add(TabInfo("后执行操作", "api-tab-content-dev-after.fxml", "-fx-pref-width: 90"))
        paramTypePaneManager = paramTypeTabPane.loadTabContent(paramTypeTabData, paramBorderPane)
        //初始化
        val loader = FXMLLoader(paramTypeTabData[0].fxml.getUrl())
        paramTypePaneManager[paramTypeTabData[0].name] = loader.load()
        paramTypeTabData[0].controller = loader.getController()
        selectTab(0)
    }

    private fun selectTab(index: Int) {
        paramTypeTabPane.selectionModel.select(index)
        paramBorderPane.center = paramTypePaneManager[paramTypeTabData[index].name]
    }

    private fun setRequestType(type: String) {
        reqMethodType.dropDownMenuController.dropDownBtn.text = type
        types.forEach {
            reqMethodType.dropDownMenuController.dropDownBtn.styleClass.remove(it.lowercase(Locale.getDefault()))
        }
        reqMethodType.dropDownMenuController.dropDownBtn.styleClass.add(type.lowercase(Locale.getDefault()))
    }

    private fun setHttpVersion(version: String) {
        httpVersion.dropDownMenuController.dropDownBtn.text = version
    }

    private fun initResponsePane() {
        //responseTabData
        responseTabData.add(TabInfo("实时响应", "api-tab-content-dev-res-result.fxml", "-fx-pref-width: 90"))
        responseTabData.add(TabInfo("请求头", "api-tab-content-dev-res-header.fxml", "-fx-pref-width: 65"))
        responseTabData.add(TabInfo("响应头", "api-tab-content-dev-res-res.fxml", "-fx-pref-width: 65;"))
        responseTabData.add(TabInfo("Cookie", "api-tab-content-dev-res-cookie.fxml", "-fx-pref-width: 70"))
        responseTabData.add(TabInfo("响应示例", "api-tab-content-dev-res-example.fxml", "-fx-pref-width: 90"))
        responseTabData.add(TabInfo("实际请求", "api-tab-content-dev-res-req.fxml", "-fx-pref-width: 90"))
        responseTabData.add(TabInfo("控制台", "api-tab-content-dev-res-console.fxml", "-fx-pref-width: 65"))
        responsePaneManager = responseTabPane.loadTabContent(responseTabData, responseBorderPane)
        //初始化
        val loader = FXMLLoader(responseTabData[0].fxml.getUrl())
        responsePaneManager[responseTabData[0].name] = loader.load()
        responseTabData[0].controller = loader.getController()
        responseTabPane.selectionModel.select(0)
        responseBorderPane.center = responsePaneManager[responseTabData[0].name]
    }
}

class ApiTabContentDevHeaderController {
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
        initPublicHeaderTableView()
        ApiDevTableView(publicHeaderCustomTableView)
        // 确保在布局完成后修改样式
        Platform.runLater {
            publicHeaderTableView.lookupAll(".column-header-background").forEach { node ->
                if (node.styleClass.contains("column-header-background")) {
                    node.style = "visibility:false;-fx-pref-height: 0;"
                }
            }
        }
    }

    private fun initPublicHeaderTableView() {
        val headers = mutableListOf("key", "value")
        publicHeaderTableView.bindHeaderListener = { index, headerColumn ->
            headerColumn.cellValueFactory = PropertyValueFactory(headers[index])
        }
        publicHeaderTableView.bindCellListener = { item, _, index ->
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
        val data = mutableListOf<TableCellParams>()
        data.add(TableCellParams("Accept", "*/*"))
        data.add(TableCellParams("Accept-Encoding", "gzip, deflate, br"))
        data.add(TableCellParams("User-Agent", "PostmanRuntime-CustomApiRuntime/1.1.0"))
        //data.add(TableCellParams("Connection", "keep-alive"))
        publicHeaderTableView.apply {
            bind(headers)
            items = FXCollections.observableArrayList(data)
        }
    }

    fun getData(): MutableList<TableCellParams> {
        publicHeaderTableView.items.addAll(publicHeaderCustomTableView.items.filter { !it.disable })
        return publicHeaderTableView.items.toMutableList()
    }
}

class ApiTabContentDevQueryController {
    @FXML
    lateinit var queryTableView: CustomTableView<TableCellParams>

    @FXML
    fun initialize() {
        ApiDevTableView(queryTableView)
    }

    fun getData(): MutableList<TableCellParams> {
        val items = queryTableView.items
        return items.filter { !it.disable }.toMutableList()
    }
}

class ApiTabContentExampleController {

}

class ApiTabContentTestController {

}

class ApiTabContentMockController {

}

class RequestTypeController {
    @FXML
    lateinit var label: Label
}

class ItemApiTabLabelController {
    @FXML
    lateinit var hBox: HBox

    @FXML
    lateinit var method: Text

    @FXML
    lateinit var title: Text

    @FXML
    lateinit var closeBtn: ImageView

    @FXML
    fun initialize() {
        method.text = "POST"
        hBox.onMouseExited = EventHandler {
            closeBtn.isVisible = false
        }
        hBox.onMouseEntered = EventHandler {
            closeBtn.isVisible = true
        }
        closeBtn.onMouseClicked = EventHandler {
            closeBtn.findParent(Tab::class.java)?.apply {
                tabPane.tabs.remove(this)
            }
        }
    }
}
