package com.mensinator.app

import android.app.Application
import com.mensinator.app.business.*
import com.mensinator.app.settings.SettingsViewModel
import com.mensinator.app.statistics.StatisticsViewModel
import com.mensinator.app.symptoms.ManageSymptomsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

class App : Application() {

    // Koin dependency injection definitions
    private val appModule = module {
        singleOf(::PeriodDatabaseHelper) { bind<IPeriodDatabaseHelper>() }
        singleOf(::CalculationsHelper) { bind<ICalculationsHelper>() }
        singleOf(::OvulationPrediction) { bind<IOvulationPrediction>() }
        singleOf(::PeriodPrediction) { bind<IPeriodPrediction>() }
        singleOf(::ExportImport) { bind<IExportImport>() }
        singleOf(::NotificationScheduler) { bind<INotificationScheduler>() }

        viewModel { ManageSymptomsViewModel(get(), get()) }
        viewModel { SettingsViewModel(get(), get(), get()) }
        viewModel { StatisticsViewModel(get(), get(), get(), get(), get()) }
    }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(appModule)
        }
    }
}