package com.gospell.drm.base.gui.data

import cn.hutool.json.JSONObject
import com.gospell.drm.base.gui.controller.ApiTabContentDevBody
import com.gospell.drm.base.gui.view.TableCellParams


class ApiData(
    var id: Int,
    var name: String,
    var url: String,
    var method: String,
    var applicationContent: String,
    var header: MutableMap<String, String>? = null,
    var query: MutableList<TableCellParams>? = null,
    var body: ApiTabContentDevBody? = null
)

class DefaultData {
    companion object {

        val apiDataList = mutableListOf(
            getImportKeyApiData(),
            getImportCrtApiData(),
            getStatisticCrtApiData()
        )

        private fun getImportKeyApiData(): ApiData {
            return ApiData(
                1,
                "导入密钥", "http://192.168.1.102:7004/import/key", "POST", "multipart/form-data",
                null,
                null,
                ApiTabContentDevBody().apply { formData.add(TableCellParams("file", "").apply { keyType = "File" }) }
            )
        }

        private fun getImportCrtApiData(): ApiData {
            return ApiData(
                2,
                "导入授权证书", "http://192.168.1.102:7004/import/crt", "POST", "multipart/form-data",
                null,
                null,
                ApiTabContentDevBody().apply { formData.add(TableCellParams("file", "C:\\Users\\47111\\Downloads\\test_1_drmlicense_202408190947_5 (1).crt").apply { keyType = "File" }) }
            )
        }

        private fun getStatisticCrtApiData(): ApiData {
            return ApiData(
                3,
                "统计授权证书", "http://192.168.1.102:7004/statistic/crt", "POST", "application/json",
                null,
                null,
                ApiTabContentDevBody().apply {
                    val data = """
                        {
                          "deviceModel": "",
                          "batchNumber": "",
                          "startTime": "",
                          "endTime": "",
                          "customerInfoId": 0,
                          "serialNumber": "",
                          "used": true
                        }
                    """.trimIndent()
                    rawData = mutableMapOf(
                        Pair("type", "json"),
                        Pair("data", JSONObject(data))
                    )
                }
            )
        }
    }
}