package com.mensinator.app.database

import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

class ImportExportManager: KoinComponent {
    private val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()

    private val database: MensinatorDB by inject()

    private fun writeJsonToFile(json: String, filePath: String) {
        val file = File(filePath + "mensinator_export.json")
        file.writeText(json)
    }

    fun exportDatabase(filePath: String) {
        val cycles = database.cycleQueries.getAllCycles().executeAsList()
            .map { cycle ->
                mapOf(
                    "start_date" to cycle.start_date,
                    "end_date" to cycle.end_date,
                    "length" to cycle.length,
                    "luteal_phase_length" to cycle.luteal_phase_length
                )
            }

        val appSettings = database.appSettingQueries.getAllSettings().executeAsList()
            .map { appSetting ->
                 mapOf(
                        "label" to appSetting.label,
                        "value" to appSetting.value_,
                        "category" to appSetting.category
                 )
            }

        val notes = database.noteQueries.getAllNotes().executeAsList()
            .map { note ->
                mapOf(
                    "name" to note.name,
                    "category" to note.category,
                    "is_active" to note.is_active
                )
            }

        val ovulations = database.ovulationQueries.getAllOvulations().executeAsList()
            .map { ovulation ->
                mapOf(
                    "date" to ovulation.date,
                    "cycle_id" to ovulation.cycle_id
                )
            }

        val periods = database.periodQueries.getAllPeriods().executeAsList()
            .map { period ->
                mapOf(
                    "date" to period.date,
                    "cycle_id" to period.cycle_id
                )
            }

        val predictions = database.predictionQueries.getAllPredictions().executeAsList()
            .map { prediction ->
                mapOf(
                    "date" to prediction.date,
                    "type" to prediction.type,
                    "cycle_id" to prediction.cycle_id
                )
            }

        val noteDates = database.noteDateQueries.getAllNoteDates().executeAsList()
            .map { noteDate ->
                mapOf(
                    "date" to noteDate.date
                )
            }

        val noteDateCrossRefs = database.noteDateCrossRefQueries.getAllNoteDateCrossRefs().executeAsList()
            .map { noteDateCrossRef ->
                mapOf(
                    "note_id" to noteDateCrossRef.note_id,
                    "note_date_id" to noteDateCrossRef.note_date_id
                )
            }

        val data = mapOf(
            "cycles" to cycles,
            "app_settings" to appSettings,
            "notes" to notes,
            "note_dates" to noteDates,
            "note_date_cross_refs" to noteDateCrossRefs,
            "ovulations" to ovulations,
            "periods" to periods,
            "predictions" to predictions
        )

        val json = gson.toJson(data)
        writeJsonToFile(json, filePath)
    }

    fun importDatabase(filePath: String) {
        val file = File(filePath)

        require(file.exists())

        val json = file.readText()

        try {

            val data: Map<String, Any> = gson.fromJson(
                json,
                com.google.gson.reflect.TypeToken.getParameterized(Map::class.java, String::class.java, Any::class.java).type
            )

            val cycles = data["cycles"] as? List<Map<String, Any>> ?: emptyList()
            cycles.forEach { cycle ->
                database.cycleQueries.insertCycle(
                    start_date = cycle["start_date"] as String,
                    end_date = cycle["end_date"] as? String,
                    length = (cycle["length"] as? String)?.toLongOrNull() ?: 0L,
                    luteal_phase_length = (cycle["luteal_phase_length"] as? String)?.toLongOrNull() ?: 0L
                )
            }

            val appSettings = data["app_settings"] as? List<Map<String, Any>> ?: emptyList()
            appSettings.forEach { appSetting ->
                database.appSettingQueries.insertAppSetting(
                    label = appSetting["label"] as String,
                    value_ = appSetting["value"] as String,
                    category = appSetting["category"] as String
                )
            }

            val notes = data["notes"] as? List<Map<String, Any>> ?: emptyList()
            notes.forEach { note ->
                database.noteQueries.insertNote(
                    name = note["name"] as String,
                    category = note["category"] as String,
                    is_active = when (val active = note["is_active"]) {
                        is Boolean -> active
                        is String -> active.toBoolean()
                        else -> false
                    }
                )
            }

            val ovulations = data["ovulations"] as? List<Map<String, Any>> ?: emptyList()
            ovulations.forEach { ovulation ->
                database.ovulationQueries.insertOvulation(
                    date = ovulation["date"] as String,
                    cycle_id = (ovulation["cycle_id"] as? String)?.toLongOrNull() ?: 0L
                )
            }

            val periods = data["periods"] as? List<Map<String, Any>> ?: emptyList()
            periods.forEach { period ->
                database.periodQueries.insertPeriod(
                    date = period["date"] as String,
                    cycle_id = (period["cycle_id"] as? String)?.toLongOrNull() ?: 0L
                )
            }

            val predictions = data["predictions"] as? List<Map<String, Any>> ?: emptyList()
            predictions.forEach { prediction ->
                database.predictionQueries.insertPrediction(
                    date = prediction["date"] as String,
                    type = prediction["type"] as String,
                    cycle_id = (prediction["cycle_id"] as? String)?.toLongOrNull() ?: 0L
                )
            }

            val noteDates = data["note_dates"] as? List<Map<String, Any>> ?: emptyList()
            noteDates.forEach { noteDate ->
                database.noteDateQueries.insertNoteDate(
                    date = noteDate["date"] as String
                )
            }

            val noteDateCrossRefs = data["note_date_cross_refs"] as? List<Map<String, Any>> ?: emptyList()
            noteDateCrossRefs.forEach { noteDateCrossRef ->
                database.noteDateCrossRefQueries.insertNoteDateCrossRef(
                    note_id = (noteDateCrossRef["note_id"] as? String)?.toLongOrNull() ?: 0L,
                    note_date_id = (noteDateCrossRef["note_date_id"] as? String)?.toLongOrNull() ?: 0L
                )
            }

        } catch (e: JsonSyntaxException) {
            throw IllegalArgumentException("Failed to parse JSON: ${e.message}")
        }
    }

}