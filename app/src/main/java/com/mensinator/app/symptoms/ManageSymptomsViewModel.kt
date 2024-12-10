package com.mensinator.app.symptoms

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import com.mensinator.app.*
import com.mensinator.app.business.ICalculationsHelper
import com.mensinator.app.business.IOvulationPrediction
import com.mensinator.app.business.IPeriodDatabaseHelper
import com.mensinator.app.business.IPeriodPrediction
import com.mensinator.app.extensions.formatToOneDecimalPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class ManageSymptomsViewModel(
    @SuppressLint("StaticFieldLeak") private val appContext: Context,
    private val periodDatabaseHelper: IPeriodDatabaseHelper,
) : ViewModel() {


    private val _viewState = MutableStateFlow(
        ViewState()
    )
    val viewState: StateFlow<ViewState> = _viewState.asStateFlow()

    data class ViewState(
        val trackedPeriods: String? = null,
    )

    fun refreshData() {
        _viewState.update {
            it.copy(
                trackedPeriods = periodDatabaseHelper.getPeriodCount().toString(),
            )
        }
    }
}
