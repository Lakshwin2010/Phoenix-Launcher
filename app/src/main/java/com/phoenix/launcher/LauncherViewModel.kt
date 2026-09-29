package com.phoenix.launcher

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.launcher.data.AppRepository
import com.phoenix.launcher.model.AppCategory
import com.phoenix.launcher.model.AppInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class LauncherScreen {
    HOME,
    APP_LIBRARY,
    SPOTLIGHT
}

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application.applicationContext)

    private val _allApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val allApps: StateFlow<List<AppInfo>> = _allApps.asStateFlow()

    private val _visibleApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val visibleApps: StateFlow<List<AppInfo>> = _visibleApps.asStateFlow()

    private val _hiddenApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val hiddenApps: StateFlow<List<AppInfo>> = _hiddenApps.asStateFlow()

    private val _dockApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val dockApps: StateFlow<List<AppInfo>> = _dockApps.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filteredApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val filteredApps: StateFlow<List<AppInfo>> = _filteredApps.asStateFlow()

    private val _currentScreen = MutableStateFlow(LauncherScreen.HOME)
    val currentScreen: StateFlow<LauncherScreen> = _currentScreen.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Hidden App Vault State
    private val _isVaultUnlocked = MutableStateFlow(false)
    val isVaultUnlocked: StateFlow<Boolean> = _isVaultUnlocked.asStateFlow()

    private val _showPasscodeDialog = MutableStateFlow(false)
    val showPasscodeDialog: StateFlow<Boolean> = _showPasscodeDialog.asStateFlow()

    // Selected app for context action menu
    private val _selectedAppForAction = MutableStateFlow<AppInfo?>(null)
    val selectedAppForAction: StateFlow<AppInfo?> = _selectedAppForAction.asStateFlow()

    private val vaultPasscode = "1234"

    init {
        loadInstalledApps()
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            _isLoading.value = true
            val apps = repository.getInstalledApps()
            _allApps.value = apps

            val visible = apps.filter { !it.isHidden }
            val hidden = apps.filter { it.isHidden }

            _visibleApps.value = visible
            _hiddenApps.value = hidden
            _filteredApps.value = visible

            var savedDockPkgs = repository.getDockPackages()
            if (savedDockPkgs.isEmpty() && visible.isNotEmpty()) {
                savedDockPkgs = getDefaultDockPackages(visible)
                repository.setDockPackages(savedDockPkgs)
            }
            val dock = savedDockPkgs.mapNotNull { pkg -> visible.find { it.packageName == pkg } }
            _dockApps.value = dock

            _isLoading.value = false
        }
    }

    private fun getDefaultDockPackages(apps: List<AppInfo>): List<String> {
        val phone = apps.find { it.packageName.contains("dialer") || it.packageName.contains("phone") }?.packageName
        val message = apps.find { (it.packageName.contains("messaging") || it.packageName.contains("mms") || it.packageName.contains("message")) && !it.isHidden }?.packageName
        val browser = apps.find { it.packageName.contains("browser") || it.packageName.contains("chrome") }?.packageName
        val camera = apps.find { it.packageName.contains("camera") }?.packageName

        return listOfNotNull(phone, message, browser, camera).distinct()
    }

    fun isAppInDock(app: AppInfo): Boolean {
        return _dockApps.value.any { it.packageName == app.packageName }
    }

    fun addAppToDock(app: AppInfo) {
        repository.addDockPackage(app.packageName)
        loadInstalledApps()
        dismissAppAction()
    }

    fun removeAppFromDock(app: AppInfo) {
        repository.removeDockPackage(app.packageName)
        loadInstalledApps()
        dismissAppAction()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _filteredApps.value = _visibleApps.value
        } else {
            _filteredApps.value = _visibleApps.value.filter {
                it.label.contains(query, ignoreCase = true) ||
                it.packageName.contains(query, ignoreCase = true)
            }
        }
    }

    fun navigateTo(screen: LauncherScreen) {
        _currentScreen.value = screen
    }

    fun launchApp(app: AppInfo) {
        repository.launchApp(app)
        // If launched from search or library, seamlessly return to home
        if (_currentScreen.value != LauncherScreen.HOME) {
            _currentScreen.value = LauncherScreen.HOME
            _searchQuery.value = ""
        }
    }

    fun getAppsByCategory(category: AppCategory): List<AppInfo> {
        return _visibleApps.value.filter { it.category == category }
    }

    // Vault Authentication & Management
    fun requestOpenVault() {
        if (_isVaultUnlocked.value) {
            // Already unlocked, keep it unlocked
        } else {
            _showPasscodeDialog.value = true
        }
    }

    fun dismissPasscodeDialog() {
        _showPasscodeDialog.value = false
    }

    fun unlockVault(pin: String): Boolean {
        return if (pin == vaultPasscode) {
            _isVaultUnlocked.value = true
            _showPasscodeDialog.value = false
            true
        } else {
            false
        }
    }

    fun lockVault() {
        _isVaultUnlocked.value = false
    }

    fun hideApp(app: AppInfo) {
        repository.setAppHidden(app.packageName, true)
        loadInstalledApps()
        dismissAppAction()
    }

    fun unhideApp(app: AppInfo) {
        repository.setAppHidden(app.packageName, false)
        loadInstalledApps()
        dismissAppAction()
    }

    // App Long-Press Action Sheet
    fun showAppAction(app: AppInfo) {
        _selectedAppForAction.value = app
    }

    fun dismissAppAction() {
        _selectedAppForAction.value = null
    }

    fun openAppInfo(app: AppInfo) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", app.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            getApplication<Application>().startActivity(intent)
        } catch (_: Exception) {}
        dismissAppAction()
    }

    fun uninstallApp(app: AppInfo) {
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.fromParts("package", app.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            getApplication<Application>().startActivity(intent)
        } catch (_: Exception) {}
        dismissAppAction()
    }
}
