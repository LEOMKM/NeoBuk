package com.neobuk.app.data

import com.neobuk.app.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime

/**
 * Singleton Supabase Client for NeoBuk
 * 
 * This client is configured with:
 * - Auth: For user authentication (login, signup, session management)
 * - Postgrest: For database operations (CRUD)
 * - Realtime: For live updates (future use)
 * 
 * Credentials are loaded from BuildConfig (set via gradle.properties)
 * 
 * NOTE: This file is deprecated - we now use Koin DI (see di/AppModule.kt)
 * Keeping for reference/fallback.
 */
object SupabaseClientProvider {
    
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            // Authentication module
            install(Auth) {
                // Session will be persisted automatically
            }
            
            // Database operations
            install(Postgrest)
            
            // Real-time subscriptions (for future use)
            install(Realtime)
        }
    }
    
    // Quick access to auth module
    val auth get() = client.auth
    
    // Quick access to database
    val database get() = client.postgrest
}
