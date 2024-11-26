package com.gospell.drm.base.gui.controller

import cn.hutool.json.JSONUtil
import okhttp3.*
import okhttp3.Headers.Companion.toHeaders
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

object OkHttpUtil {
    private val client = OkHttpClient()

    /**
     * 发送HTTP请求
     *
     * @param url 请求的URL
     * @param methodType 请求方法类型（GET, POST等）
     * @param contentType 内容类型（例如："application/json", "multipart/form-data"等）
     * @param params 合并的参数映射，值可以是String、File、Map或其他类型
     */
    fun sendRequest(
        url: String,
        methodType: String,
        contentType: ContentType,
        headers: MutableMap<String, String>,
        params: Map<String, Any?>,
        callback: Callback
    ) {
        val requestBody: RequestBody = when (contentType) {
            ContentType.APPLICATION_OCTET_STREAM,
            ContentType.MULTIPART_FORM_DATA,
            ContentType.IMAGE_PNG,
            ContentType.IMAGE_JPEG -> {
                val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
                params.forEach { (key, value) ->
                    val file = File(value.toString())
                    builder.addFormDataPart(key, file.name, file.asRequestBody(contentType.value.toMediaTypeOrNull()))
                }
                builder.build()
            }

            ContentType.APPLICATION_X_WWW_FORM_URLENCODED -> {
                val formBuilder = FormBody.Builder()
                params.forEach { (key, value) ->
                    formBuilder.add(key, value.toString())
                }
                formBuilder.build()
            }

            ContentType.APPLICATION_JSON -> {
                val jsonParams = JSONUtil.toJsonStr(params)
                jsonParams.toRequestBody(contentType.value.toMediaTypeOrNull())
            }

            ContentType.APPLICATION_XML -> {
                val xmlParams = params.entries.joinToString("") { (key, value) -> "<$key>$value</$key>" }
                xmlParams.toRequestBody(contentType.value.toMediaTypeOrNull())
            }

            else -> {
                if (params["data"] == null) {
                    "".toRequestBody(contentType.value.toMediaTypeOrNull())
                } else {
                    params["data"].toString().toRequestBody(contentType.value.toMediaTypeOrNull())
                }
            }
        }
        if (contentType != ContentType.NONE) {
            headers["Content-Type"] = contentType.value
        }
        val requestBuilder = Request.Builder()
            .url(url)
            .headers(headers.toHeaders())

        when (methodType.uppercase()) {
            "POST" -> requestBuilder.post(requestBody)
            "PUT" -> requestBuilder.put(requestBody)
            "DELETE" -> requestBuilder.delete(requestBody)
            "GET" -> requestBuilder.get()
            else -> throw IllegalArgumentException("Unsupported HTTP method: $methodType")
        }

        val request = requestBuilder.build()

        client.newCall(request).enqueue(callback)
    }
}

enum class ContentType(val value: String) {
    APPLICATION_JSON("application/json; charset=utf-8"),
    APPLICATION_XML("application/xml; charset=utf-8"),
    APPLICATION_HTML("text/html; charset=utf-8"),
    APPLICATION_JS("application/javascript; charset=utf-8"),
    TEXT_PLAIN("text/html; charset=utf-8"),
    MULTIPART_FORM_DATA("multipart/form-data"),
    APPLICATION_X_WWW_FORM_URLENCODED("application/x-www-form-urlencoded"),
    APPLICATION_OCTET_STREAM("application/octet-stream"),
    IMAGE_JPEG("image/jpeg"),
    IMAGE_PNG("image/png"),
    NONE(""),
}