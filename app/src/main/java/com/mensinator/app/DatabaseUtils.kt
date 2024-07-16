package com.mensinator.app

import android.database.sqlite.SQLiteDatabase

/*
This file is for the database-structure. Take care when changing anything since
we have to keep track of database-version and how stuff effects the onCreate/upgrade.
 */

object DatabaseUtils {

    fun createDatabase(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS periods (
                id INTEGER PRIMARY KEY AUTOINCREMENT, 
                date TEXT, 
                period_id INTEGER
            )
        """)

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS symptoms (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                symptom_name TEXT NOT NULL,
                active INT NOT NULL
            )
        """)

        val predefinedSymptoms = listOf("Heavy Flow", "Medium Flow", "Light Flow")
        predefinedSymptoms.forEach { symptom ->
            db.execSQL(
                """
                INSERT INTO symptoms (symptom_name, active) VALUES (?, 1)
                """, arrayOf(symptom)
            )
        }

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS symptom_date (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                symptom_date TEXT NOT NULL,
                symptom_id INT NOT NULL,
                FOREIGN KEY (symptom_id) REFERENCES symptoms(id)
            )
        """)

        createAppSettingsGroup(db)
        createAppSettings(db)
    }

    fun createAppSettingsGroup(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS app_settings_group (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                group_label TEXT NOT NULL
            )
        """)

        db.execSQL("""
            INSERT INTO app_settings_group (group_label) VALUES
                ('Colors'),
                ('Reminders'),
                ('Other')
        """)
    }

    fun createAppSettings(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS app_settings (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                setting_key TEXT NOT NULL,
                setting_label TEXT NOT NULL,
                setting_value TEXT NOT NULL,
                group_label_id INTEGER NOT NULL,
                FOREIGN KEY (group_label_id) REFERENCES app_settings_group(id)
            )
        """)

        db.execSQL("""
            INSERT INTO app_settings (setting_key, setting_label, setting_value, group_label_id) VALUES
                ('period_color', 'Period Color', 'Red', 1),
                ('selection_color', 'Selection Color', 'Grey', 1),
                ('period_selection_color', 'Period Selection Color', 'DarkGray', 1),
                ('symptom_color', 'Symptom Color', 'Black', 1),
                ('expected_period_color', 'Expected Period Color', 'Yellow', 1),
                ('reminder_days', 'Days Before Reminder', '0', 2)
        """)
    }
}
