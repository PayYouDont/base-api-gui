package com.gospell.drm.base.gui.controller

import cn.hutool.json.JSONUtil
import com.gospell.drm.base.gui.data.ApiData
import com.gospell.drm.base.gui.util.Utils.Companion.create
import com.gospell.drm.base.gui.util.Utils.Companion.findChildren
import com.gospell.drm.base.gui.util.Utils.Companion.getImage
import com.gospell.drm.base.gui.util.Utils.Companion.isJson
import com.gospell.drm.base.gui.view.ApiDevTableView
import com.gospell.drm.base.gui.view.CustomTableView
import com.gospell.drm.base.gui.view.DropDownMenuView
import com.gospell.drm.base.gui.view.TableCellParams
import javafx.application.Platform
import javafx.collections.ListChangeListener
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.scene.web.WebView
import javafx.stage.FileChooser

class ApiTabContentDevBody {
    var noneData: MutableMap<String, Any>? = null
    var formData: MutableList<TableCellParams> = mutableListOf()
    var urlencodedData: MutableList<TableCellParams> = mutableListOf()
    var binaryData: MutableMap<String, Any>? = null
    var msgpackData: MutableMap<String, Any>? = null
    var rawData: MutableMap<String, Any>? = null
}

class ApiTabContentDevBodyController {
    @FXML
    lateinit var root: VBox

    @FXML
    lateinit var none: RadioButton

    @FXML
    lateinit var formData: RadioButton

    @FXML
    lateinit var urlencoded: RadioButton

    @FXML
    lateinit var binary: RadioButton

    @FXML
    lateinit var msgpack: RadioButton

    @FXML
    lateinit var raw: RadioButton

    @FXML
    lateinit var rawType: DropDownMenuView

    lateinit var toggleGroup: ToggleGroup

    @FXML
    private lateinit var bodyContentBorderPane: BorderPane
    private val viewManager = mutableMapOf<RadioButton, Node>()

    var body = ApiTabContentDevBody()

    @FXML
    fun initialize() {
        toggleGroup = ToggleGroup()
        none.toggleGroup = toggleGroup
        formData.toggleGroup = toggleGroup
        urlencoded.toggleGroup = toggleGroup
        binary.toggleGroup = toggleGroup
        msgpack.toggleGroup = toggleGroup
        raw.toggleGroup = toggleGroup
        toggleGroup.selectedToggleProperty().addListener { _, _, newValue ->
            bodyContentBorderPane.center = viewManager[newValue]
        }
        Platform.runLater {
            val data = if (root.userData is ApiData) {
                root.userData as ApiData
            } else {
                null
            }
            data?.body?.also {
                body = it
            }
            initNone()
            initFormData()
            initUrlencoded()
            initBinary()
            initMsgpack()
            initRaw()
            data?.applicationContent?.also {
                when (it) {
                    "application/x-www-form-urlencoded" -> toggleGroup.selectToggle(urlencoded)
                    "multipart/form-data" -> toggleGroup.selectToggle(formData)
                    "application/json" -> toggleGroup.selectToggle(raw)
                    else -> toggleGroup.selectToggle(none)
                }
            }

        }
    }

    private fun initNone() {
        TableView<String>().apply {
            body.noneData = mutableMapOf(Pair("data", items))
            items.addListener(ListChangeListener {
                body.noneData?.put("data", items)
            })
            viewManager[none] = this
        }
    }

    private fun initFormData() {
        CustomTableView<TableCellParams>().apply {
            val apiDevTableView = ApiDevTableView(this)
            apiDevTableView.valueTypes.add(0, "File")
            if (body.formData.isNotEmpty()) {
                items.clear()
                items.addAll(body.formData)
            }
            refresh()
            items.addListener(ListChangeListener {
                println("change:$it")
            })
            items.addListener(ListChangeListener {
                body.formData.clear()
                body.formData.addAll(items)
            })
            viewManager[formData] = this
        }
    }

    private fun initUrlencoded() {
        CustomTableView<TableCellParams>().apply {
            ApiDevTableView(this)
            if (body.urlencodedData.isNotEmpty()) {
                items.clear()
                items.addAll(body.urlencodedData)
                refresh()
            }
            items.addListener(ListChangeListener {
                body.urlencodedData.clear()
                body.urlencodedData.addAll(items)
            })
            viewManager[urlencoded] = this
        }
    }

    private fun initBinary() {
        val upload = Button()
        val vBox = VBox()
        val hBox = HBox()
        val image = ImageView("upload.png".getImage())
        val uploadSelectedImage = ImageView("upload-selected.png".getImage())
        val link = ImageView("link.png".getImage())
        link.isVisible = false
        val text = Label("上传文件")
        text.style = "-fx-fill: #a9acae;"
        val path = Text()
        path.style = "-fx-fill: #a9acae;"
        path.textProperty().addListener { _, _, newValue ->
            link.isVisible = newValue.isNotEmpty()
        }
        hBox.apply {
            spacing = 10.0
            children.add(0, image)
            children.add(text)
            setOnMouseEntered {
                children.remove(image)
                children.add(0, uploadSelectedImage)
            }
            setOnMouseEntered {
                hBox.children.remove(image)
                hBox.children.add(0, uploadSelectedImage)
            }
            setOnMouseExited {
                hBox.children.remove(uploadSelectedImage)
                hBox.children.add(0, image)
            }
        }
        upload.apply {
            graphic = hBox
            styleClass.add("full-width-button")
            setOnMouseClicked {
                handleFileUpload(path)
            }
        }
        val pathHBox = HBox()
        pathHBox.apply {
            spacing = 10.0
            children.add(link)
            children.add(path)
            alignment = javafx.geometry.Pos.CENTER
        }
        vBox.apply {
            children.add(upload)
            children.add(pathHBox)
            alignment = javafx.geometry.Pos.CENTER
            style = "-fx-border-color:#f2f4f7"
        }
        //bodyContentBorderPane.center = vBox
        viewManager[binary] = vBox
    }

    private fun initMsgpack() {
        TextArea().apply {
            if (body.msgpackData == null) {
                body.msgpackData = mutableMapOf(Pair("data", ""))
            } else {
                text = body.msgpackData?.get("data").toString()
            }
            viewManager[msgpack] = this
            textProperty().addListener { _, _, data ->
                body.msgpackData?.put("data", data)
            }
        }
    }

    private fun initRaw() {
        val valueTypes = mutableListOf(
            "json",
            "xml",
            "javascript",
            "plain",
            "html"
        )
        rawType.bind(valueTypes, "item-request-type.fxml") { itemType, itemView ->
            itemView.findChildren<Label>("label")?.text = itemType
        }
        rawType.itemSelectedListener = { itemView ->
            itemView.findChildren<Label>("label")?.apply {
                rawType.dropDownMenuController.dropDownBtn.text = text
                body.rawData?.put("type", text)
            }
        }
        rawType.dropDownMenuController.dropDownBtn.text = "json"
        if (body.rawData == null) {
            body.rawData = mutableMapOf(Pair("data", ""), Pair("type", valueTypes[0]))
        } else {
            body.rawData?.get("type")?.also {
                rawType.selectedItem = it
                rawType.dropDownMenuController.dropDownBtn.text = it.toString()
            }
        }
        raw.selectedProperty().addListener { _, _, newValue ->
            rawType.isVisible = newValue
        }
        var data = "''"
        body.rawData?.get("data")?.also {
            if (it.toString().isJson()) {
                data = "'${JSONUtil.toJsonPrettyStr(it)}'"
            }
        }
        viewManager[raw] = WebView().create(data, body.rawData?.get("type").toString())
    }

    private fun handleFileUpload(path: Text) {
        val fileChooser = FileChooser()
        fileChooser.extensionFilters.addAll(
            FileChooser.ExtensionFilter("所有文件", "*.*"),
            FileChooser.ExtensionFilter("文本所有", "*.*"),
            FileChooser.ExtensionFilter("图像文件", "*.png", "*.jpg", "*.gif")
        )
        val file = fileChooser.showOpenDialog(null)
        if (file != null) {
            path.text = file.absolutePath
            body.binaryData = mutableMapOf(Pair("file", file))
        }
    }
}