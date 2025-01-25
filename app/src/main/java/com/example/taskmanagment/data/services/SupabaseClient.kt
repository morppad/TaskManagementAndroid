package com.example.taskmanagment.data.services

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

val supabaseClient = createSupabaseClient(
    supabaseUrl = "https://sddxssylqgmxlrqxaghd.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNkZHhzc3lscWdteGxycXhhZ2hkIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTczNzQwMTg4NCwiZXhwIjoyMDUyOTc3ODg0fQ.4PXE6Z1PTqVLGERG4OJdV1dBEkakgKOi46qYI_vUTGc"
) {
    install(Auth) {
        // Настройки Auth, например, URL для редиректа
        scheme = "http"
    }
    install(Postgrest)
    install(Storage)
}