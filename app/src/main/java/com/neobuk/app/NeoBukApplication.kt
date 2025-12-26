package com.neobuk.app

import android.app.Application
import com.neobuk.app.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * NeoBuk Application class
 * 
 * Initializes Koin for dependency injection on app startup.
 */
class NeoBukApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Koin
        startKoin {
            // Log Koin events (use Level.ERROR in production)
            androidLogger(Level.DEBUG)
            
            // Provide Android context
            androidContext(this@NeoBukApplication)
            
            // Load modules
            modules(appModule)
        }
    }
}
