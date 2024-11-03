package com.mensinator.app.database

enum class AppCategory(val value: String) {
    COLORS("Colors"),
    REMINDERS("Reminders"),
    OTHERS("Other settings")
}

enum class NoteCategory(val value: String){
    SYMPTOMS("Symptoms"),
    MOODS("Moods"),
    SEX("Sex")
}

enum class PredictionType(val value: String){
    PERIOD("Period"),
    OVULATION("Ovulation")
}

enum class Colors(val value: String) {
    RED("Red"),
    LIGHT_GRAY("LightGray"),
    DARK_GRAY("DarkGray"),
    YELLOW("Yellow"),
    BLUE("Blue"),
    MAGENTA("Magenta")
}

enum class PredefinedSymptoms(val value: String){
    HEAVY_FLOW("Heavy flow"),
    MEDIUM_FLOW("Medium flow"),
    LIGHT_FLOW("Light flow"),
}