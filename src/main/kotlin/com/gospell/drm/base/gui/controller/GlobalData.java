package com.gospell.drm.base.gui.controller;

/**
 * @author 47111
 * @version 1.0
 * @description: TODO
 * @date 2024/11/8 16:40
 */
public class GlobalData {

    public static String getHtmlJson(String jsonString) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>JSON Viewer</title>\n" +
                "    <style>\n" +
                "        body {\n" +
                "            font-family: Arial, sans-serif;\n" +
                "        }\n" +
                "        .collapsible {\n" +
                "            cursor: pointer;\n" +
                "            display: flex;\n" +
                "            align-items: center;\n" +
                "        }\n" +
                "        .caret {\n" +
                "            display: none;\n" +
                "            width: 10px;\n" +
                "            height: 10px;\n" +
                "            margin-left: 5px;\n" +
                "        }\n" +
                "        .down::before {\n" +
                "            content: \"\\25BC\";\n" +
                "        }\n" +
                "        .right::before {\n" +
                "            content: \"\\25BA\";\n" +
                "        }\n" +
                "        .line-number {\n" +
                "            color: #999;\n" +
                "            text-align: right;\n" +
                "            padding-right: 10px;\n" +
                "            display: flex;\n" +
                "            align-items: center;\n" +
                "            border-right: 1px solid #ccc;\n" +
                "            padding-right: 10px;\n" +
                "            padding-left: 10px;\n" +
                "            min-width: 70px; /* 固定行号和三角形图标占用的空间 */\n" +
                "            position: relative;\n" +
                "        }\n" +
                "        .json-container {\n" +
                "            padding: 10px;\n" +
                "        }\n" +
                "        .caret-container {\n" +
                "            display: flex;\n" +
                "            align-items: center;\n" +
                "        }\n" +
                "        .content {\n" +
                "            flex-grow: 1;\n" +
                "            padding-left: 10px;\n" +
                "        }\n" +
                "        .line-number:hover .caret,\n" +
                "        .line-number.caret-visible .caret {\n" +
                "            display: inline-block;\n" +
                "        }\n" +
                "        .line-number .caret.right {\n" +
                "            display: inline-block; /* 折叠时始终显示三角形 */\n" +
                "        }\n" +
                "        .ellipsis {\n" +
                "            display: none;\n" +
                "        }\n" +
                "        .collapsible .ellipsis {\n" +
                "            display: inline-block;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div id=\"json-container\" class=\"json-container\"></div>\n" +
                "\n" +
                "<script>\n" +
                "    function loadJSON(jsonData) {\n" +
                "        const container = document.getElementById('json-container');\n" +
                "        container.innerHTML = ''; // 清空容器\n" +
                "        renderJSON(jsonData, container, 1, '');\n" +
                "    }\n" +
                "\n" +
                "    function renderJSON(data, parent, lineNumber, indent) {\n" +
                "        if (Array.isArray(data)) {\n" +
                "            return renderArray(data, parent, lineNumber, indent);\n" +
                "        } else if (typeof data === 'object') {\n" +
                "            return renderObject(data, parent, lineNumber, indent);\n" +
                "        } else {\n" +
                "            const line = createLine(`${indent}\"${data}\"`, lineNumber); // 添加缩进\n" +
                "            parent.appendChild(line);\n" +
                "            return lineNumber + 1;\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    function renderArray(array, parent, lineNumber, indent) {\n" +
                "        const line = createCollapsibleLine(indent, '[', lineNumber, false); // 初始状态显示倒三角\n" +
                "        line.addEventListener('click', function() {\n" +
                "            toggle(this, array, indent + '&nbsp;&nbsp;', lineNumber + 1);\n" +
                "        });\n" +
                "        parent.appendChild(line);\n" +
                "\n" +
                "        const subContainer = document.createElement('div');\n" +
                "        subContainer.style.display = 'block'; // 初始状态展开\n" +
                "        parent.appendChild(subContainer);\n" +
                "\n" +
                "        let currentLineNumber = lineNumber + 1;\n" +
                "        for (let i = 0; i < array.length; i++) {\n" +
                "            currentLineNumber = renderJSON(array[i], subContainer, currentLineNumber, indent + '&nbsp;&nbsp;'); // 递归调用时增加缩进\n" +
                "        }\n" +
                "\n" +
                "        const closingLine = createLine(`${indent}]`, currentLineNumber); // 添加缩进\n" +
                "        parent.appendChild(closingLine);\n" +
                "        return currentLineNumber + 1;\n" +
                "    }\n" +
                "\n" +
                "    function renderObject(object, parent, lineNumber, indent) {\n" +
                "        const line = createCollapsibleLine(indent, '{', lineNumber, false); // 初始状态显示倒三角\n" +
                "        line.addEventListener('click', function() {\n" +
                "            toggle(this, object, indent + '&nbsp;&nbsp;', lineNumber + 1);\n" +
                "        });\n" +
                "        parent.appendChild(line);\n" +
                "\n" +
                "        const subContainer = document.createElement('div');\n" +
                "        subContainer.style.display = 'block'; // 初始状态展开\n" +
                "        parent.appendChild(subContainer);\n" +
                "\n" +
                "        let currentLineNumber = lineNumber + 1;\n" +
                "        for (let key in object) {\n" +
                "            if (object.hasOwnProperty(key)) {\n" +
                "                const value = object[key];\n" +
                "                const keyValueLine = createLine(`${indent}&nbsp;&nbsp;\"${key}\": ${renderValue(value, indent + '&nbsp;&nbsp;')}`, currentLineNumber); // 添加缩进\n" +
                "                subContainer.appendChild(keyValueLine);\n" +
                "                currentLineNumber++;\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        const closingLine = createLine(`${indent}}`, currentLineNumber); // 添加缩进\n" +
                "        parent.appendChild(closingLine);\n" +
                "        return currentLineNumber + 1;\n" +
                "    }\n" +
                "\n" +
                "    function renderValue(value, indent) {\n" +
                "        if (Array.isArray(value)) {\n" +
                "            return `[${value.map(item => renderValue(item, indent)).join(', ')}]`;\n" +
                "        } else if (typeof value === 'object') {\n" +
                "            return `{${Object.keys(value).map(key => `\"${key}\": ${renderValue(value[key], indent)}`).join(', ')}}`;\n" +
                "        } else {\n" +
                "            return JSON.stringify(value);\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    function createLine(text, lineNumber) {\n" +
                "        const line = document.createElement('div');\n" +
                "        line.style.display = 'flex';\n" +
                "        line.style.alignItems = 'center';\n" +
                "        line.innerHTML = `\n" +
                "      <span class=\"line-number\">${lineNumber}</span>\n" +
                "      <span class=\"content\">${text}</span>\n" +
                "    `;\n" +
                "        return line;\n" +
                "    }\n" +
                "\n" +
                "    function createCollapsibleLine(indent, text, lineNumber, isCollapsed) {\n" +
                "        const line = document.createElement('div');\n" +
                "        line.className = 'collapsible';\n" +
                "        line.innerHTML = `\n" +
                "      <span class=\"line-number\">\n" +
                "        ${lineNumber}\n" +
                "        <span class=\"caret-container\">\n" +
                "          <span class=\"caret ${isCollapsed ? 'right' : 'down'}\"></span>\n" +
                "        </span>\n" +
                "      </span>\n" +
                "      <span class=\"content\">${text}</span>\n" +
                "      <span class=\"ellipsis\">...</span>\n" +
                "    `;\n" +
                "        return line;\n" +
                "    }\n" +
                "\n" +
                "    function toggle(element, data, indent, lineNumber) {\n" +
                "        const caret = element.querySelector('.caret');\n" +
                "        const subContainer = element.nextElementSibling;\n" +
                "        const ellipsis = element.querySelector('.ellipsis');\n" +
                "\n" +
                "        if (subContainer.style.display === 'none') {\n" +
                "            subContainer.style.display = 'block';\n" +
                "            caret.classList.remove('right');\n" +
                "            caret.classList.add('down');\n" +
                "            ellipsis.style.display = 'none'; // 展开时隐藏省略号\n" +
                "        } else {\n" +
                "            subContainer.style.display = 'none';\n" +
                "            caret.classList.remove('down');\n" +
                "            caret.classList.add('right');\n" +
                "            ellipsis.style.display = 'inline-block'; // 折叠时显示省略号\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    // 鼠标悬停时显示所有隐藏的三角形\n" +
                "    document.getElementById('json-container').addEventListener('mouseover', function(event) {\n" +
                "        if (event.target.classList.contains('line-number')) {\n" +
                "            const carets = document.querySelectorAll('.collapsible .caret');\n" +
                "            carets.forEach(caret => {\n" +
                "                caret.parentElement.classList.add('caret-visible');\n" +
                "            });\n" +
                "        }\n" +
                "    });\n" +
                "\n" +
                "    document.getElementById('json-container').addEventListener('mouseout', function(event) {\n" +
                "        if (event.target.classList.contains('line-number')) {\n" +
                "            const carets = document.querySelectorAll('.collapsible .caret');\n" +
                "            carets.forEach(caret => {\n" +
                "                if (!caret.classList.contains('right')) {\n" +
                "                    caret.parentElement.classList.remove('caret-visible');\n" +
                "                }\n" +
                "            });\n" +
                "        }\n" +
                "    });\n" +
                "\n" +
                "    window.onload = function() {\n" +
                "        loadJSON("+jsonString+");\n" +
                "    };\n" +
                "</script>\n" +
                "</body>\n" +
                "</html>";
    }
}
