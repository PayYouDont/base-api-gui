package com.gospell.drm.base.gui.view

import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.CheckBox

class TriStateCheckBox : CheckBox() {

    private val indeterminateProperty: BooleanProperty = SimpleBooleanProperty(this, "indeterminate", false)

    var indeterminate: Boolean
        @JvmName("getIndeterminateProperty") get() = indeterminateProperty.get()
        @JvmName("setIndeterminateProperty") set(value) {
            indeterminateProperty.set(value)
        }

    init {
        indeterminateProperty.addListener { _, _, _ ->
            updateStyle()
        }
    }

    private fun updateStyle() {
        if (indeterminate) {
            styleClass.add("indeterminate")
        } else {
            styleClass.remove("indeterminate")
        }
    }
}