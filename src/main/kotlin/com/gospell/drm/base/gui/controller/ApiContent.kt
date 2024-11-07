package com.gospell.drm.base.gui.controller

import com.gospell.drm.base.gui.util.Utils.Companion.findChildren
import com.gospell.drm.base.gui.util.Utils.Companion.findParent
import com.gospell.drm.base.gui.util.Utils.Companion.getImage
import com.gospell.drm.base.gui.util.Utils.Companion.getUrl
import com.gospell.drm.base.gui.util.Utils.Companion.loadTabContent
import com.gospell.drm.base.gui.view.*
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.text.Text
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
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

class TabInfo(var name: String, var fxml: String, var style: String)
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

class ApiTabContentDesignController {

}

class ApiParams {
    var url: String = ""
    val headers: MutableList<MutableMap<String, String>> = mutableListOf()
    var method = "POST"

    init {
        // 创建 HttpClient 实例
        val httpClient = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://jsonplaceholder.typicode.com/posts"))
            .headers("Content-Type", "application/json", "Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString("""{"title": "foo", "body": "bar", "userId": 1}"""))
            .build()
        httpClient.send(request, BodyHandlers.ofString())?.apply {
            println(this)
            println(body())
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
    private val tabData = mutableListOf<TabInfo>()

    @FXML
    fun initialize() {
        initParamUrlViews()
        initParamTypeViews()
        urlText.focusedProperty().addListener { _, _, newValue ->
            if (newValue) {
                urlText.style = "-fx-border-color: #f78b2b; -fx-background-color: white;"
            } else {
                urlText.style = "-fx-background-color: transparent"
            }
        }
        sendBtn.onMouseClicked = EventHandler {
            val params = ApiParams()
            params.url = urlText.text
            reqMethodType.findChildren<Label>("label")?.apply {
                params.method = text
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
        tabData.add(TabInfo("Header", "api-tab-content-dev-header.fxml", "-fx-pref-width: 70"))
        tabData.add(TabInfo("Query", "api-tab-content-dev-query.fxml", "-fx-pref-width: 64"))
        tabData.add(TabInfo("Path", "api-tab-content-dev-path.fxml", "-fx-pref-width: 58;"))
        tabData.add(TabInfo("Body", "api-tab-content-dev-body.fxml", "-fx-pref-width: 58"))
        tabData.add(TabInfo("认证", "api-tab-content-dev-auth.fxml", "-fx-pref-width: 58"))
        tabData.add(TabInfo("Cookie", "api-tab-content-dev-cookie.fxml", "-fx-pref-width: 70"))
        tabData.add(TabInfo("预执行操作", "api-tab-content-dev-pre.fxml", "-fx-pref-width: 90"))
        tabData.add(TabInfo("后执行操作", "api-tab-content-dev-after.fxml", "-fx-pref-width: 90"))
        paramTypeTabPane.loadTabContent(tabData, paramBorderPane)
        paramTypeTabPane.selectionModel.select(paramTypeTabPane.tabs[0])
        tabData[0].apply {
            paramBorderPane.center = FXMLLoader(fxml.getUrl()).load()
        }
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
                controller.dropDownMenuView.dropDownMenuController.dropDownBtn.text = "String"
                controller.dropDownMenuView.dropDownMenuController.dropDownBtn.isDisable = true
                controller.dropDownMenuView.dropDownMenuController.root.style = "-fx-background-color:#f5f5f5"
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
        data.add(TableCellParams("Connection", "keep-alive"))
        publicHeaderTableView.bind(headers)
        publicHeaderTableView.items = FXCollections.observableArrayList(data)
    }

}

class ApiTabContentDevQueryController {
    @FXML
    lateinit var queryTableView: CustomTableView<TableCellParams>

    @FXML
    fun initialize() {
        ApiDevTableView(queryTableView)
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
