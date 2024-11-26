package com.gospell.drm.base.gui.controller

import cn.hutool.core.util.XmlUtil
import cn.hutool.json.JSONUtil
import com.gospell.drm.base.gui.data.ApiData
import com.gospell.drm.base.gui.util.Utils.Companion.findChildren
import com.gospell.drm.base.gui.util.Utils.Companion.getUrl
import com.gospell.drm.base.gui.util.Utils.Companion.loadTabContent
import com.gospell.drm.base.gui.view.DropDownMenuView
import com.gospell.drm.base.gui.view.TableCellParams
import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleSetProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.*
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Headers
import okhttp3.Headers.Companion.toHeaders
import okhttp3.Response
import java.io.IOException
import java.util.*

class ApiTabContentDevController {
    @FXML
    lateinit var root: VBox

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
    private var apiData: ApiData? = null
    private var resultData = SimpleObjectProperty<ResultData>()
    private var headerData = SimpleObjectProperty<Headers>()
    private var resHeaderData = SimpleObjectProperty<Headers>()
    @FXML
    fun initialize() {
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
                apiData?.url = newValue
            }
        }
        sendBtn.onMouseClicked = EventHandler {
            request()
        }
        Platform.runLater {
            root.userData?.apply {
                if (this is ApiData) {
                    initData(this)
                }
            }
        }
    }

    private fun request() {
        apiData?.apply {
            val methodType = reqMethodType.selectedItem.toString()
            val headers = mutableMapOf<String, String>()
            paramTypeTabData[0].controller?.apply {
                this as ApiTabContentDevHeaderController
                getData().onEach { headers[it.key] = it.value }
            }
            paramTypeTabData[1].controller?.apply {
                this as ApiTabContentDevQueryController
                query = getData()
                url += if (url.contains("?")) "&" else "?"
                query!!.onEach {
                    if (it.key.isNotEmpty() && it.value.isNotEmpty()) {
                        url += "${it.key}=${it.value}&"
                    }
                    url.substring(0, url.length - 1)
                }
            }
            var contentType = ContentType.APPLICATION_JSON
            val params = mutableMapOf<String, Any?>()
            paramTypeTabData[3].controller?.apply {
                this as ApiTabContentDevBodyController
                when (toggleGroup.selectedToggle) {
                    none -> {
                        contentType = ContentType.NONE
                    }

                    formData -> {
                        contentType = ContentType.MULTIPART_FORM_DATA
                        body.formData.onEach {
                            params[it.key] = it.value
                        }
                    }

                    urlencoded -> {
                        contentType = ContentType.APPLICATION_X_WWW_FORM_URLENCODED
                        body.urlencodedData.onEach {
                            params[it.key] = it.value
                        }
                    }

                    binary -> {
                        contentType = ContentType.APPLICATION_OCTET_STREAM
                        body.binaryData?.get("type").apply {
                            when (this) {
                                "image/jpeg" -> {
                                    contentType = ContentType.IMAGE_JPEG
                                }

                                "image/png" -> {
                                    contentType = ContentType.IMAGE_PNG
                                }
                            }
                        }
                        body.binaryData?.get("data")?.apply {
                            params["file"] = this
                        }
                    }

                    msgpack -> {
                        contentType = ContentType.TEXT_PLAIN
                        body.msgpackData?.apply {
                            params.putAll(this)
                        }
                    }

                    raw -> {
                        body.rawData?.get("type").apply {
                            when (this) {
                                "json" -> {
                                    contentType = ContentType.APPLICATION_JSON
                                    body.rawData?.get("data")?.apply {
                                        params.putAll(JSONUtil.parseObj(this))
                                    }
                                }

                                "xml" -> {
                                    contentType = ContentType.APPLICATION_XML
                                    body.rawData?.get("data")?.apply {
                                        params.putAll(XmlUtil.xmlToMap(this.toString()))
                                    }
                                }

                                "javascript" -> {
                                    contentType = ContentType.APPLICATION_JS
                                    body.rawData?.get("data")?.apply {
                                        params["data"] = this
                                    }
                                }

                                "plain" -> {
                                    contentType = ContentType.TEXT_PLAIN
                                    body.rawData?.get("data")?.apply {
                                        params["data"] = this
                                    }
                                }

                                "html" -> {
                                    contentType = ContentType.APPLICATION_HTML
                                    body.rawData?.get("data")?.apply {
                                        params["data"] = this
                                    }
                                }
                            }
                        }
                    }
                }
            }
            OkHttpUtil.sendRequest(url, methodType, contentType, headers, params, object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Platform.runLater {
                        resultData.set(ResultData(null, e.message))
                        headerData.set(call.request().headers)
                    }
                }
                override fun onResponse(call: Call, response: Response) {
                    Platform.runLater {
                        response.body?.apply {
                            resultData.set(ResultData(string(),null))
                            headerData.set(response.request.headers)
                            resHeaderData.set(response.headers)
                        }
                    }
                }
            })
        }
    }

    private fun setQueryData(url: String) {
        if (url.isNotEmpty() && url.contains("?")) {
            val queryParams = url.split("?")[1]
            val params = if (queryParams.contains("&")) {
                queryParams.split("&")
            } else if (queryParams.contains(",")) {
                queryParams.split(",")
            } else {
                listOf(queryParams)
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

    private fun initData(data: ApiData) {
        this.apiData = data
        apiData?.apply {
            urlText.text = url
            setRequestType(method)
            initParamUrlViews()
            initResponsePane()
            initParamTypeViews()
            if (query != null) {
                selectTab(1)
            } else if (body != null) {
                selectTab(3)
            } else {
                selectTab(0)
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
        paramTypeTabData.add(TabInfo("Header", "api-tab-content-dev-header.fxml", "-fx-pref-width: 70", null, apiData))
        paramTypeTabData.add(TabInfo("Query", "api-tab-content-dev-query.fxml", "-fx-pref-width: 64", null, apiData))
        paramTypeTabData.add(TabInfo("Path", "api-tab-content-dev-path.fxml", "-fx-pref-width: 58;", null, apiData))
        paramTypeTabData.add(TabInfo("Body", "api-tab-content-dev-body.fxml", "-fx-pref-width: 58", null, apiData))
        paramTypeTabData.add(TabInfo("认证", "api-tab-content-dev-auth.fxml", "-fx-pref-width: 58", null, apiData))
        paramTypeTabData.add(TabInfo("Cookie", "api-tab-content-dev-cookie.fxml", "-fx-pref-width: 70", null, apiData))
        paramTypeTabData.add(TabInfo("预执行操作", "api-tab-content-dev-pre.fxml", "-fx-pref-width: 90", null, apiData))
        paramTypeTabData.add(TabInfo("后执行操作", "api-tab-content-dev-after.fxml", "-fx-pref-width: 90", null, apiData))
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
        apiData?.method = type
    }

    private fun setHttpVersion(version: String) {
        httpVersion.dropDownMenuController.dropDownBtn.text = version
    }

    private fun initResponsePane() {
        //responseTabData
        responseTabData.add(TabInfo("实时响应", "api-tab-content-dev-res-result.fxml", "-fx-pref-width: 90",null,resultData))
        responseTabData.add(TabInfo("请求头", "api-tab-content-dev-req-header.fxml", "-fx-pref-width: 65",null,headerData))
        responseTabData.add(TabInfo("响应头", "api-tab-content-dev-res-header.fxml", "-fx-pref-width: 65;", null,resHeaderData))
        responseTabData.add(TabInfo("Cookie", "api-tab-content-dev-res-cookie.fxml", "-fx-pref-width: 70"))
        responseTabData.add(TabInfo("响应示例", "api-tab-content-dev-res-example.fxml", "-fx-pref-width: 90"))
        responseTabData.add(TabInfo("实际请求", "api-tab-content-dev-res-req.fxml", "-fx-pref-width: 90"))
        responseTabData.add(TabInfo("控制台", "api-tab-content-dev-res-console.fxml", "-fx-pref-width: 65"))
        responsePaneManager = responseTabPane.loadTabContent(responseTabData, responseBorderPane)
        //初始化
        val loader = FXMLLoader(responseTabData[0].fxml.getUrl())
        val view = loader.load<VBox>()
        view.userData = resultData
        responsePaneManager[responseTabData[0].name] = view
        responseTabData[0].controller = loader.getController()
        responseTabPane.selectionModel.select(0)
        responseBorderPane.center = responsePaneManager[responseTabData[0].name]
    }
}