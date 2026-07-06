package com.arsham.dorom.data.remote

import com.arsham.dorom.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Single Supabase client for the app. [BuildConfig.SUPABASE_URL]/[BuildConfig.SUPABASE_ANON_KEY]
 * come from local.properties (git-ignored) and default to empty strings when unset — the client
 * still constructs, requests just fail until real credentials are supplied.
 */
object SupabaseModule {
    val client by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
        ) {
            install(Auth)
            install(Postgrest)
        }
    }
}
