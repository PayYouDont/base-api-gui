package com.gospell.drm.base.gui.controller

import com.gospell.drm.base.gui.data.ApiData
import com.gospell.drm.base.gui.util.Utils.Companion.getUrl
import com.gospell.drm.base.gui.util.Utils.Companion.loadTabContent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Label
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.text.Text
import java.util.*


class ApiContentTabPanelController {
    @FXML
    lateinit var tabRoot: TabPane

    @FXML
    lateinit var addTab: Tab

    @FXML
    lateinit var moreTab: Tab
    private val apiDataList = mutableListOf<ApiData>()


    @FXML
    fun initialize() {
        ApiListItemController.apiClickListener = {
            addTab(it)
        }
    }

    fun addTab(apiData: ApiData) {
        //新增
        if (apiData.id == -1) {
            apiData.id = apiDataList.size
        }
        if (apiDataList.contains(apiData)) {
            tabRoot.tabs.find { tab -> tab.id == "tab-${apiData.id}" }?.apply {
                tabRoot.selectionModel.select(this)
            }
        } else {
            apiDataList.add(apiData)
            val tab = createCustomTab(apiData)
            tabRoot.tabs.add(tabRoot.tabs.size - 2, tab)
            tabRoot.selectionModel.select(tab)
            tab.graphic.requestFocus()
        }
    }

    private fun createCustomTab(apiData: ApiData): Tab {
        val tab = Tab()
        val hBoxRoot = FXMLLoader("item-api-tab-label.fxml".getUrl())
        val hBox = hBoxRoot.load<AnchorPane>()
        val controller = hBoxRoot.getController<ItemApiTabLabelController>()
        controller.method.text = apiData.method
        controller.method.styleClass.add(apiData.method.lowercase(Locale.getDefault()))
        controller.title.text = apiData.name
        controller.closeBtn.setOnMouseClicked {
            apiDataList.remove(apiData)
            tabRoot.tabs.find { tab -> tab.id == "tab-${apiData.id}" }?.apply {
                tabRoot.tabs.remove(this)
            }
        }
        tab.userData = apiData
        tab.graphic = hBox
        tab.styleClass.add("custom-tab")
        tab.isClosable = false
        tab.id = "tab-${apiData.id}"
        val loader = FXMLLoader("api-tab-content.fxml".getUrl())
        val content = loader.load<BorderPane>()
        loader.getController<ApiContentController>().apply {
            setData(apiData)
        }
        tab.content = content
        return tab
    }

    fun showApiMenu() {
        ApiNewPopupController.showDropdownMenu(addTab.graphic) {
            addTab(it)
        }
    }
}

class TabInfo(
    var name: String,
    var fxml: String,
    var style: String,
    var controller: Any? = null,
    var data: Any? = null
)

class ApiContentController {
    @FXML
    lateinit var tabPane: TabPane

    @FXML
    lateinit var apiContentItem: BorderPane
    private val tabData = mutableListOf<TabInfo>()

    @FXML
    fun initialize() {


    }

    fun setData(apiData: ApiData) {
        tabData.add(TabInfo("设计", "api-tab-content-design.fxml", "-fx-pref-width: 48", null, apiData))
        tabData.add(TabInfo("调试", "api-tab-content-dev.fxml", "-fx-pref-width: 48", null, apiData))
        tabData.add(TabInfo("接口用例", "api-tab-content-example.fxml", "-fx-pref-width: 70;", null, apiData))
        tabData.add(TabInfo("一键压测", "api-tab-content-test.fxml", "-fx-pref-width: 70", null, apiData))
        tabData.add(TabInfo("Mock", "api-tab-content-mock.fxml", "-fx-pref-width: 56", null, apiData))
        tabPane.loadTabContent(tabData, apiContentItem)
        tabPane.selectionModel.select(tabPane.tabs[1])

    }
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
    }
}

