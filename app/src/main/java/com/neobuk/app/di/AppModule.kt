package com.neobuk.app.di

import com.neobuk.app.BuildConfig
import com.neobuk.app.data.repositories.AuthRepository
import com.neobuk.app.data.repositories.BusinessRepository
import com.neobuk.app.data.repositories.ExpensesRepository
import com.neobuk.app.data.repositories.ProductsRepository
import com.neobuk.app.data.repositories.SalesRepository
import com.neobuk.app.data.repositories.ReportsRepository
import com.neobuk.app.data.repositories.DayClosureRepository
import com.neobuk.app.data.repositories.DashboardRepository
import com.neobuk.app.data.repositories.ServicesRepository
import com.neobuk.app.data.repositories.SubscriptionRepository
import com.neobuk.app.data.repositories.TasksRepository
import com.neobuk.app.viewmodels.AuthViewModel
import com.neobuk.app.viewmodels.DayClosureViewModel
import com.neobuk.app.viewmodels.DashboardViewModel
import com.neobuk.app.viewmodels.ExpensesViewModel
import com.neobuk.app.viewmodels.InventoryViewModel
import com.neobuk.app.viewmodels.ProductsViewModel
import com.neobuk.app.viewmodels.SalesViewModel
import com.neobuk.app.viewmodels.ReportsViewModel
import com.neobuk.app.viewmodels.ServicesViewModel
import com.neobuk.app.viewmodels.SubscriptionViewModel
import com.neobuk.app.viewmodels.TasksViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Koin DI Module for NeoBuk
 * 
 * This module provides:
 * - Supabase client as a singleton
 * - Repositories as singletons
 * - ViewModels scoped to their lifecycle
 */
val appModule = module {
    
    // ============================================
    // SUPABASE CLIENT (Singleton)
    // ============================================
    single<SupabaseClient> {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            // Authentication module
            install(Auth)
            
            // Database operations
            install(Postgrest)
            
            // Real-time subscriptions
            install(Realtime)
        }
    }
    
    // ============================================
    // REPOSITORIES (Singletons)
    // ============================================
    single { AuthRepository(get(), androidContext()) }
    single { BusinessRepository(get()) }
    single { SubscriptionRepository(get()) }
    single { ServicesRepository(get()) }
    single { ExpensesRepository(get()) }
    single { ProductsRepository(get()) }
    single { SalesRepository(get()) }
    single { ReportsRepository(get()) }
    single { DayClosureRepository(get()) }
    single { DashboardRepository(get()) }
    single { TasksRepository(get()) }
    
    // ============================================
    // VIEWMODELS
    // ============================================
    viewModel { AuthViewModel(get(), get(), get()) }
    viewModel { ServicesViewModel(get()) }
    viewModel { ExpensesViewModel(get()) }
    viewModel { ProductsViewModel(get()) }
    viewModel { InventoryViewModel(get()) } // Legacy - now wraps ProductsRepository
    viewModel { SubscriptionViewModel() }
    viewModel { TasksViewModel(get(), get()) }
    viewModel { SalesViewModel(get()) }
    viewModel { ReportsViewModel(get()) }
    viewModel { DayClosureViewModel(get()) }
    viewModel { DashboardViewModel(get()) }
}
