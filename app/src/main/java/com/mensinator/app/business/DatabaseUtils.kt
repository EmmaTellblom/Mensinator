package com.mensinator.app.business

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

        val predefinedSymptoms = listOf("Heavy_Flow", "Medium_Flow", "Light_Flow")
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
        createOvulationStructure(db)
        databaseVersion7(db)
        databaseVersion8(db)
        databaseVersion9(db)
        databaseVersion10(db)
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
                ('selection_color', 'Selection Color', 'LightGray', 1),
                ('expected_period_color', 'Expected Period Color', 'Yellow', 1),
                ('reminder_days', 'Days Before Reminder', '0', 2),
                ('luteal_period_calculation', 'Luteal Phase Calculation', '0', 3)
        """)
        // ('period_selection_color', 'Period Selection Color', 'DarkGray', 1), - deprecated
    }

    fun createOvulationStructure(db: SQLiteDatabase){
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS ovulations (
                id INTEGER PRIMARY KEY AUTOINCREMENT, 
                date TEXT
            )
            """
        )

        db.execSQL("""
            INSERT INTO app_settings(setting_key, setting_label, setting_value, group_label_id) VALUES
                ('ovulation_color', 'Ovulation Color', 'Blue', '1'),
                ('expected_ovulation_color', 'Expected Ovulation Color', 'Magenta', '1')
        """)
    }

    fun insertLutealSetting(db: SQLiteDatabase){
        db.execSQL("""
            INSERT INTO APP_SETTINGS(setting_key, setting_label, setting_value, group_label_id) VALUES
                ('luteal_period_calculation', 'Luteal Phase Calculation', '0', 3)
        """)
    }

    fun databaseVersion7 (db: SQLiteDatabase) {

        // Update the app_settings to handle which type of setting it is
        // LI == List
        // NO == Number
        // SW == Switch
        // TX == Text
        db.execSQL("""
            ALTER TABLE app_settings ADD COLUMN setting_type TEXT
        """)
        db.execSQL("""
            UPDATE app_settings SET setting_type = 'LI' WHERE group_label_id = '1'
        """)
        db.execSQL("""
            UPDATE app_settings SET setting_type = 'NO' WHERE group_label_id = '2'
        """)
        db.execSQL("""
            UPDATE app_settings SET setting_type = 'SW' WHERE group_label_id = '3'
        """)

        // Insert new row for cycle history
        // This will allow the user to fine tune how many cycles back should be used for prediction
        // NO because its going to be a number
        // Insert new row for language
        // Insert new row for showing cycle numbers
        db.execSQL("""
            INSERT INTO app_settings (setting_key, setting_label, setting_value, group_label_id, setting_type) 
            VALUES
            ('period_history','Period history','5','3','NO'),
            ('ovulation_history','Ovulation history','5','3','NO'),
            ('lang', 'Language', 'en', '3', 'LI'),
            ('cycle_numbers_show','Show cycle numbers','1','3','SW')
        """)

        // Add color to the symptoms table
        db.execSQL("""
            ALTER TABLE symptoms ADD COLUMN color TEXT DEFAULT 'Black'
        """)
        // Set all colors for the standard symptoms
        db.execSQL("""
            UPDATE symptoms SET color = 'DarkRed' where symptom_name = 'Heavy_Flow'
        """)
        db.execSQL("""
            UPDATE symptoms SET color = 'Red' where symptom_name = 'Medium_Flow'
        """)
        db.execSQL("""
            UPDATE symptoms SET color = 'LightRed' where symptom_name = 'Light_Flow'
        """)

        // Fixed symptom colors, so we can remove setting for symptom indicator
        db.execSQL("""
            DELETE FROM app_settings WHERE setting_key = 'symptom_color'
        """)

        db.execSQL("""
            UPDATE app_settings SET setting_value = 'LightGray' WHERE setting_value = 'Grey'
        """)

    }

    fun databaseVersion8(db: SQLiteDatabase) {
        //Insert new row for screen protection
        db.execSQL("""
            INSERT INTO app_settings(setting_key, setting_label, setting_value, group_label_id, setting_type)
            VALUES
            ('screen_protection', 'Protect screen', '1', '3', 'SW')
        """)
    }
    fun databaseVersion9(db: SQLiteDatabase) {
        //Insert new row for custom period notification message
        db.execSQL(
            """
            INSERT INTO app_settings(setting_key, setting_label, setting_value, group_label_id, setting_type)
            VALUES
            ('period_notification_message', 'Period Notification Message', 'Period_Notification_Message', '2', 'TX')
        """
        )
    }

    fun databaseVersion10(db: SQLiteDatabase) {
        // Remove old setting for period_selection_color
        db.execSQL(
            """
        DELETE FROM app_settings WHERE setting_key = 'period_selection_color';
        """
        )
    }


}
