package com.vishnu.habittracker.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import javax.inject.Singleton

/**
 * Hilt module providing the Supabase client.
 * Uses the same Supabase instance as the webapp for cross-platform sync.
 */
@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    // Same credentials as the webapp (from supabase.js lines 4-5)
    private const val SUPABASE_URL = "https://plgwxcegcnowqxoggujk.supabase.co"
    private const val SUPABASE_ANON_KEY = "sb_publishable_TlSPeqIQP7ilpp3mLbmE7A_Qw7A0BDt"

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_ANON_KEY
        ) {
            install(Auth) {
                scheme = "com.vishnu.habittracker"
                host = "auth-callback"
            }
            install(Postgrest)
            install(Realtime)
        }
    }
}
