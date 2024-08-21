package com.mensinator.app
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.mensinator.app.database.MensinatorDB
import com.mensinator.app.database.viewmodels.AppSettingViewModel
import com.mensinator.app.database.viewmodels.CycleViewModel
import com.mensinator.app.database.viewmodels.NoteViewModel
import com.mensinator.app.database.viewmodels.OvulationViewModel
import com.mensinator.app.database.viewmodels.PeriodViewModel
import com.mensinator.app.database.viewmodels.PredictionViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val databaseModule = module {
    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = MensinatorDB.Schema,
            context = get(),
            name = "mensinator.db"
        )
    }

    single {
        MensinatorDB(get())
    }
}

val viewModelModule = module {
    viewModelOf(::AppSettingViewModel)
    viewModelOf(::CycleViewModel)
    viewModelOf(::NoteViewModel)
    viewModelOf(::OvulationViewModel)
    viewModelOf(::PeriodViewModel)
    viewModelOf(::PredictionViewModel)
}
