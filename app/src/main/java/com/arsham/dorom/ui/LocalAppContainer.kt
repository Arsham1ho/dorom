package com.arsham.dorom.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.arsham.dorom.AppContainer

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer not provided")
}
