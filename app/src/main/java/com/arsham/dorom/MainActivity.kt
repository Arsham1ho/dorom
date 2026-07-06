package com.arsham.dorom

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.arsham.dorom.data.settings.DoromSettings
import com.arsham.dorom.ui.LocalAppContainer
import com.arsham.dorom.ui.navigation.DoromBottomBar
import com.arsham.dorom.ui.navigation.DoromNavHost
import com.arsham.dorom.ui.navigation.Routes
import com.arsham.dorom.ui.theme.DoromTheme

class MainActivity : FragmentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private var pendingRouteState = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        requestExactAlarmPermissionIfNeeded()

        pendingRouteState.value = intent?.getStringExtra("open_route")
        val container = (application as DoromApp).container

        setContent {
            val settings by container.settingsRepository.settings.collectAsStateWithLifecycle(initialValue = DoromSettings())
            DoromTheme(themeMode = settings.themeMode) {
                CompositionLocalProvider(LocalAppContainer provides container) {
                    val navController = rememberNavController()
                    val backStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = backStackEntry?.destination?.route
                    val pendingRoute by pendingRouteState

                    // No-ops when signed out; when signed in, this is the "sync on launch" leg —
                    // the other two are the periodic WorkManager job and right after sign-in.
                    LaunchedEffect(Unit) { container.syncEngine.syncAll() }

                    LaunchedEffect(pendingRoute) {
                        pendingRoute?.let {
                            navController.navigate(it)
                            pendingRouteState.value = null
                        }
                    }

                    Surface(modifier = Modifier.fillMaxSize()) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Box(modifier = Modifier.fillMaxSize().weight(1f).statusBarsPadding().imePadding()) {
                                DoromNavHost(navController = navController, startDestination = Routes.HOME)
                            }
                            DoromBottomBar(
                                currentRoute = currentRoute,
                                onNavigate = { route ->
                                    // Deliberately simple: always collapse back to a single fresh
                                    // entry for the tapped tab. The standard saveState/restoreState
                                    // bottom-nav recipe is built for nested per-tab graphs; in our
                                    // flat single graph it silently no-ops when the tapped tab IS
                                    // the graph's start destination (Home) but isn't currently on
                                    // top of the back stack — which is exactly why "Home" could
                                    // appear to do nothing from a pushed screen like Settings.
                                    navController.navigate(route) {
                                        popUpTo(Routes.HOME) { inclusive = (route == Routes.HOME) }
                                        launchSingleTop = true
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingRouteState.value = intent.getStringExtra("open_route")
    }

    private fun requestExactAlarmPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                runCatching {
                    startActivity(
                        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName"))
                    )
                }
            }
        }
    }
}
