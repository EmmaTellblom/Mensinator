package com.mensinator.app
import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MensinatorApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MensinatorApplication)
            androidLogger()
            modules(
                databaseModule,
                dataModule
            )
        }
    }
}