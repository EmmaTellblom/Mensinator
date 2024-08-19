package com.mensinator.app

import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.mensinator.app.database.Database
import com.mensinator.app.database.repositories.*
import com.mensinator.app.database.viewmodels.*
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val databaseModule = module {

    single {
        AndroidSqliteDriver(
            schema = Database.Schema,
            context = get(),
            name = "Database.db"
        )
    }

    single {
        Database(get())
    }
}

/*
val repositoryModule = module {
    single {
        AppSettingRepository(get())
    }

    single {
        CycleRepository(get())
    }

    single {
        NoteRepository(get())
    }

    single {
        OvulationRepository(get())
    }

    single {
        PeriodRepository(get())
    }

    single {
        PredictionRepository(get())
    }
}
*/

val viewModelModule = module {
    viewModel {
        AppSettingViewModel(get())
    }

    viewModel {
        CycleViewModel(get())
    }

    viewModel {
        NoteViewModel(get())
    }

    viewModel {
        OvulationViewModel(get())
    }

    viewModel {
        PeriodViewModel(get())
    }

    viewModel {
        PredictionViewModel(get())
    }
}
