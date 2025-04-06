package com.mensinator.app

import android.app.AlarmManager
import android.app.Application
import com.mensinator.app.business.*
import com.mensinator.app.business.notifications.AndroidNotificationScheduler
import com.mensinator.app.business.notifications.IAndroidNotificationScheduler
import com.mensinator.app.business.notifications.INotificationScheduler
import com.mensinator.app.business.notifications.NotificationScheduler
import com.mensinator.app.calendar.CalendarViewModel
import com.mensinator.app.settings.SettingsViewModel
import com.mensinator.app.statistics.StatisticsViewModel
import com.mensinator.app.symptoms.ManageSymptomsViewModel
import com.mensinator.app.utils.DefaultDispatcherProvider
import com.mensinator.app.utils.IDispatcherProvider
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
        singleOf(::DefaultDispatcherProvider) { bind<IDispatcherProvider>() }
        singleOf(::AndroidNotificationScheduler) { bind<IAndroidNotificationScheduler>() }
        single { androidContext().getSystemService(ALARM_SERVICE) as AlarmManager }

        viewModel { CalendarViewModel(get(), get(), get(), get()) }
        viewModel { ManageSymptomsViewModel(get()) }
        viewModel { SettingsViewModel(get(), get(), get(), get()) }
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