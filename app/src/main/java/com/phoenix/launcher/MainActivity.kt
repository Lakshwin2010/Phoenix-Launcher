package com.phoenix.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.phoenix.launcher.ui.components.AppActionSheet
import com.phoenix.launcher.ui.components.IosPasscodeDialog
import com.phoenix.launcher.ui.screens.AppLibraryScreen
import com.phoenix.launcher.ui.screens.HomeScreen
import com.phoenix.launcher.ui.screens.SpotlightScreen
import com.phoenix.launcher.ui.theme.PhoenixLauncherTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PhoenixLauncherTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()
                val allApps by viewModel.allApps.collectAsState()
                val visibleApps by viewModel.visibleApps.collectAsState()
                val hiddenApps by viewModel.hiddenApps.collectAsState()
                val dockApps by viewModel.dockApps.collectAsState()
                val filteredApps by viewModel.filteredApps.collectAsState()
                val searchQuery by viewModel.searchQuery.collectAsState()
                val isLoading by viewModel.isLoading.collectAsState()
                val isVaultUnlocked by viewModel.isVaultUnlocked.collectAsState()
                val showPasscodeDialog by viewModel.showPasscodeDialog.collectAsState()
                val selectedAppForAction by viewModel.selectedAppForAction.collectAsState()

                val pagerState = rememberPagerState(pageCount = { 2 })
                val coroutineScope = rememberCoroutineScope()

                // Intercept Android Back Button:
                BackHandler(
                    enabled = currentScreen == LauncherScreen.SPOTLIGHT ||
                              showPasscodeDialog ||
                              selectedAppForAction != null ||
                              pagerState.currentPage != 0
                ) {
                    if (showPasscodeDialog) {
                        viewModel.dismissPasscodeDialog()
                    } else if (selectedAppForAction != null) {
                        viewModel.dismissAppAction()
                    } else if (currentScreen == LauncherScreen.SPOTLIGHT) {
                        viewModel.navigateTo(LauncherScreen.HOME)
                    } else if (pagerState.currentPage != 0) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.35f),
                                    Color.Black.copy(alpha = 0.15f),
                                    Color.Black.copy(alpha = 0.65f)
                                )
                            )
                        )
                        .systemBarsPadding()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.White
                        )
                    } else {
                        // Horizontal Pager: Page 0 = Home, Page 1 = App Library (on the side)
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            when (page) {
                                0 -> {
                                    HomeScreen(
                                        viewModel = viewModel,
                                        apps = visibleApps,
                                        dockApps = dockApps,
                                        onAppClick = { viewModel.launchApp(it) },
                                        onAppLongClick = { viewModel.showAppAction(it) },
                                        onNavigateToLibrary = {
                                             coroutineScope.launch {
                                                pagerState.animateScrollToPage(1)
                                            }
                                        }
                                    )
                                }
                                1 -> {
                                    AppLibraryScreen(
                                        viewModel = viewModel,
                                        visibleApps = visibleApps,
                                        hiddenApps = hiddenApps,
                                        allApps = allApps,
                                        isVaultUnlocked = isVaultUnlocked,
                                        onOpenVault = { viewModel.requestOpenVault() },
                                        onLockVault = { viewModel.lockVault() },
                                        onAppClick = { viewModel.launchApp(it) },
                                        onAppLongClick = { viewModel.showAppAction(it) },
                                        onNavigateToHome = {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(0)
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        // Spotlight Search Overlay
                        AnimatedVisibility(
                            visible = currentScreen == LauncherScreen.SPOTLIGHT,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            SpotlightScreen(
                                viewModel = viewModel,
                                searchQuery = searchQuery,
                                filteredApps = filteredApps,
                                onAppClick = {
                                    viewModel.launchApp(it)
                                    coroutineScope.launch {
                                        pagerState.scrollToPage(0)
                                    }
                                }
                            )
                        }

                        // iOS Passcode Dialog for Hidden Vault
                        IosPasscodeDialog(
                            isOpen = showPasscodeDialog,
                            onDismiss = { viewModel.dismissPasscodeDialog() },
                            onSuccess = { },
                            onValidatePin = { pin -> viewModel.unlockVault(pin) }
                        )

                        // App Context Action Sheet (Remove from Dock / Add to Dock / Hide / App Info / Uninstall)
                        AppActionSheet(
                            app = selectedAppForAction,
                            isInDock = selectedAppForAction?.let { viewModel.isAppInDock(it) } ?: false,
                            onDismiss = { viewModel.dismissAppAction() },
                            onToggleDock = { app ->
                                if (viewModel.isAppInDock(app)) {
                                    viewModel.removeAppFromDock(app)
                                } else {
                                    viewModel.addAppToDock(app)
                                }
                            },
                            onToggleHide = { app ->
                                if (app.isHidden) {
                                    viewModel.unhideApp(app)
                                } else {
                                    viewModel.hideApp(app)
                                }
                            },
                            onAppInfo = { viewModel.openAppInfo(it) },
                            onUninstall = { viewModel.uninstallApp(it) }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh installed apps in case a new app was installed or uninstalled
        viewModel.loadInstalledApps()
    }
}
