package com.mensinator.app.data

enum class ImportSource(val displayName: String) {
    MENSINATOR("Mensinator"),
    CLUE("Clue");

    companion object {
        fun fromString(value: String): ImportSource? {
            return entries.find { it.displayName == value }
        }
    }
}