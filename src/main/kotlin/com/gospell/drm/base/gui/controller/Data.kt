package com.gospell.drm.base.gui.controller

import cn.hutool.json.JSONArray
import cn.hutool.json.JSONObject
import javafx.scene.control.TreeItem

class GlobalData2 {
    companion object {
        fun getHtmlJson(jsonString: String): String {
            // 创建美化后的 HTML 内容
            val htmlContent =
                """
                    <!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>JSON Viewer</title>
    <style>
        body {
            font-family: Arial, sans-serif;
        }
        .collapsible {
            cursor: pointer;
            display: flex;
            align-items: center;
        }
        .caret {
            display: inline-block;
            width: 10px;
            height: 10px;
            margin-left: 5px;
        }
        .down::before {
            content: "\25BC";
        }
        .right::before {
            content: "\25BA";
        }
        .line-number {
            color: #999;
            text-align: right;
            padding-right: 10px;
            display: flex;
            align-items: center;
            border-right: 1px solid #ccc;
            padding-right: 10px;
            padding-left: 10px;
            min-width: 40px; /* 固定行号和三角形图标占用的空间 */
        }
        .json-container {
            padding: 10px;
        }
        .caret-container {
            display: flex;
            align-items: center;
        }
        .content {
            flex-grow: 1;
            padding-left: 10px;
        }
    </style>
</head>
<body>
<div id="json-container" class="json-container"></div>

<script>
    function loadJSON(jsonData) {
        const container = document.getElementById('json-container');
        container.innerHTML = ''; // 清空容器
        renderJSON(jsonData, container, 1);
    }

    function renderJSON(data, parent, lineNumber, indent = '') {
        if (Array.isArray(data)) {
            return renderArray(data, parent, lineNumber, indent);
        } else if (typeof data === 'object') {
            return renderObject(data, parent, lineNumber, indent);
        } else {
            const line = createLine(`$${'$'}{indent}${'$'}{data}`, lineNumber);
            parent.appendChild(line);
            return lineNumber + 1;
        }
    }

    function renderArray(array, parent, lineNumber, indent) {
        const line = createCollapsibleLine(indent, '[', lineNumber, false); // 初始状态显示倒三角
        line.onclick = function() {
            toggle(this, array, indent + '  ', lineNumber + 1);
        };
        parent.appendChild(line);

        const subContainer = document.createElement('div');
        subContainer.style.display = 'block'; // 初始状态展开
        parent.appendChild(subContainer);

        let currentLineNumber = lineNumber + 1;
        for (let i = 0; i < array.length; i++) {
            currentLineNumber = renderJSON(array[i], subContainer, currentLineNumber, indent + '  ');
        }

        const closingLine = createLine(`${'$'}{indent}]`, currentLineNumber);
        parent.appendChild(closingLine);
        return currentLineNumber + 1;
    }

    function renderObject(object, parent, lineNumber, indent) {
        const line = createCollapsibleLine(indent, '{', lineNumber, false); // 初始状态显示倒三角
        line.onclick = function() {
            toggle(this, object, indent + '  ', lineNumber + 1);
        };
        parent.appendChild(line);

        const subContainer = document.createElement('div');
        subContainer.style.display = 'block'; // 初始状态展开
        parent.appendChild(subContainer);

        let currentLineNumber = lineNumber + 1;
        for (let key in object) {
            if (object.hasOwnProperty(key)) {
                const value = object[key];
                const keyLine = createLine(`${'$'}{indent}  ${'$'}{key}": `, currentLineNumber++);
                subContainer.appendChild(keyLine);
                currentLineNumber = renderJSON(value, subContainer, currentLineNumber, indent + '  ');
            }
        }

        const closingLine = createLine(`${'$'}{indent}}`, currentLineNumber);
        parent.appendChild(closingLine);
        return currentLineNumber + 1;
    }

    function createLine(text, lineNumber) {
        const line = document.createElement('div');
        line.style.display = 'flex';
        line.style.alignItems = 'center';
        line.innerHTML = `
      <span class="line-number">${'$'}{lineNumber}</span>
      <span class="content">${'$'}{text}</span>
    `;
        return line;
    }

    function createCollapsibleLine(indent, text, lineNumber, isCollapsed) {
        const line = document.createElement('div');
        line.className = 'collapsible';
        line.innerHTML = `
      <span class="line-number">
        ${'$'}{lineNumber}
        <span class="caret-container">
          <span class="caret ${'$'}{isCollapsed ? 'right' : 'down'}"></span>
        </span>
      </span>
      <span class="content">${'$'}{text}</span>
    `;
        return line;
    }

    function toggle(element, data, indent, lineNumber) {
        const caret = element.querySelector('.caret');
        const subContainer = element.nextElementSibling;

        if (subContainer.style.display === 'none') {
            subContainer.style.display = 'block';
            caret.classList.remove('right');
            caret.classList.add('down');
        } else {
            subContainer.style.display = 'none';
            caret.classList.remove('down');
            caret.classList.add('right');
        }
    }

    window.onload = function() {
        const jsonData = {"name": "John", "age": 30, "cars": ["Ford", "BMW", "Fiat"]};
        loadJSON(jsonData);
    };
</script>
</body>
</html>
                """
            return htmlContent
        }

        private fun String.escapeForJavaScript(): String {
            return this.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("'", "\\'")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("\b", "\\b")
                .replace("\u000c", "\\f")
        }

        fun createTreeItem(jsonObject: JSONObject, key: String): TreeItem<String> {
            val item = TreeItem("$key: ${jsonObject.getStr(key)}")
            for (entry in jsonObject.keys) {
                when (val value = jsonObject[entry]) {
                    is JSONObject -> item.children.add(createTreeItem(value, entry))
                    is JSONArray -> item.children.add(createTreeItemFromArray(value, entry))
                    else -> item.children.add(TreeItem("$entry: $value"))
                }
            }
            return item
        }

        fun createTreeItemFromArray(jsonArray: JSONArray, key: String): TreeItem<String> {
            val item = TreeItem("$key: [Array]")
            for (i in 0 until jsonArray.size) {
                when (val value = jsonArray[i]) {
                    is JSONObject -> item.children.add(createTreeItem(value, "[$i]"))
                    is JSONArray -> item.children.add(createTreeItemFromArray(value, "[$i]"))
                    else -> item.children.add(TreeItem("[$i]: $value"))
                }
            }
            return item
        }
    }

}