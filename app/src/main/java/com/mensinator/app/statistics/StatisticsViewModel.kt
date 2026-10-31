package com.mensinator.app.statistics

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mensinator.app.R
import com.mensinator.app.business.ICalculationsHelper
import com.mensinator.app.business.IOvulationPrediction
import com.mensinator.app.business.IPeriodDatabaseHelper
import com.mensinator.app.business.IPeriodPrediction
import com.mensinator.app.extensions.formatToOneDecimalPoint
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class StatisticsViewModel(
    @SuppressLint("StaticFieldLeak") private val appContext: Context,
    private val periodDatabaseHelper: IPeriodDatabaseHelper,
    private val calcHelper: ICalculationsHelper,
    private val ovulationPrediction: IOvulationPrediction,
    private val periodPrediction: IPeriodPrediction,
) : ViewModel() {

    private val dash = "-"
    private val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

    private val _viewState = MutableStateFlow(
        ViewState()
    )
    val viewState: StateFlow<ViewState> = _viewState.asStateFlow()

    data class ViewState(
        val trackedPeriods: String? = null,
        val averageCycleLength: String? = null,
        val averagePeriodLength: String? = null,
        val averageLutealLength: String? = null,
        val follicleGrowthDays: String? = null,
        val ovulationPredictionDate: String? = null,
        val periodPredictionDate: String? = null,
        val ovulationCount: String? = null,
        val completedPeriods: PersistentList<PeriodHistoryRow> = persistentListOf(),
        val predictedPeriods: PersistentList<LocalDate> = persistentListOf(),
    )

    fun refreshData() {
        viewModelScope.launch(Dispatchers.IO) {
            val completedPeriods = periodDatabaseHelper.getAllPeriodSummaries()
                .toCompletedPeriodRows(LocalDate.now())
            val predictedPeriodDate = periodPrediction.getPredictedPeriodDate()
            val averageCycleLength = calcHelper.averageCycleLength()
            val predictedPeriods = predictedPeriodStarts(
                firstPredictedDate = predictedPeriodDate,
                averageCycleLengthDays = averageCycleLength,
            )

            _viewState.update {
                it.copy(
                    trackedPeriods = periodDatabaseHelper.getPeriodCount().toString(),
                    averageCycleLength = formatDays(averageCycleLength.formatToOneDecimalPoint()),
                    averagePeriodLength = formatDays(calcHelper.averagePeriodLength().formatToOneDecimalPoint()),
                    averageLutealLength = formatDays(calcHelper.averageLutealLength().formatToOneDecimalPoint()),
                    follicleGrowthDays = calcHelper.averageFollicalGrowthInDays().formatToOneDecimalPoint(),
                    ovulationPredictionDate = ovulationPrediction.getPredictedOvulationDate()?.format(dateFormatter) ?: dash,
                    periodPredictionDate = predictedPeriodDate?.format(dateFormatter) ?: dash,
                    ovulationCount = periodDatabaseHelper.getOvulationCount().toString(),
                    completedPeriods = completedPeriods.toPersistentList(),
                    predictedPeriods = predictedPeriods.toPersistentList(),
                )
            }
        }
    }

    private fun formatDays(text: String): String {
        val days = appContext.getString(R.string.days)
        return "$text $days"
    }
}
