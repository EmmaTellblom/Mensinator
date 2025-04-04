package com.mensinator.app.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mensinator.app.R

//Maps Database keys to res/strings.xml for multilanguage support
object ResourceMapper {
    //maps res strings xml file to db keys
    private val resourceMap = mapOf(
        //settings
        "app_settings" to R.string.app_settings,
        "period_color" to R.string.period_color,
        "selection_color" to R.string.selection_color,
        "period_selection_color" to R.string.period_selection_color,
        "expected_period_color" to R.string.expected_period_color,
        "ovulation_color" to R.string.ovulation_color,
        "expected_ovulation_color" to R.string.expected_ovulation_color,
        "Period_Notification_Message" to R.string.period_notification_message,
        "reminders" to R.string.reminders,
        "reminder_days" to R.string.days_before_reminder,
        "other_settings" to R.string.other_settings,
        "luteal_period_calculation" to R.string.luteal_phase_calculation,
        "period_history" to R.string.period_history,
        "ovulation_history" to R.string.ovulation_history,
        "lang" to R.string.language,
        "cycle_numbers_show" to R.string.cycle_numbers_show,
        "close" to R.string.close,
        "save" to R.string.save,
        "Heavy_Flow" to R.string.heavy,
        "Medium_Flow" to R.string.medium,
        "Light_Flow" to R.string.light,
        "screen_protection" to R.string.screen_protection,
        // colors
//        "Red" to R.string.color_red,
//        "Green" to R.string.color_green,
//        "Blue" to R.string.color_blue,
//        "Yellow" to R.string.color_yellow,
//        "Cyan" to R.string.color_cyan,
//        "Magenta" to R.string.color_magenta,
//        "Black" to R.string.color_black,
//        "White" to R.string.color_white,
//        "DarkGray" to R.string.color_darkgray,
//        "LightGray" to R.string.color_gray,
    )

    fun getStringResourceId(key: String): Int? {
        return resourceMap[key]
    }

    fun getPeriodReminderMessage(key: String, context: Context): String {
        // If we can't retrieve a resource ID via the key, we know that the user has changed the text.
        val userHasChangedMessage = getStringResourceId(key) == null

        val appDefaultText = context.getString(R.string.period_notification_message)
        val userSetValue = key.takeIf { userHasChangedMessage }

        return if (userSetValue.isNullOrBlank()) {
            appDefaultText
        } else {
            userSetValue
        }
    }

    @Composable
    fun getStringResourceOrCustom(key: String): String {
        /**
         * - If key is unchanged, return the stringResource value
         * - If key has changed (null), return user-set value
         */
        val id = getStringResourceId(key)
        val text = id?.let { stringResource(id = id) } ?: key
        return text
    }
}