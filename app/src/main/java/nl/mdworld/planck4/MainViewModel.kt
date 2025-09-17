package nl.mdworld.planck4

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel : ViewModel() {
    private val _appState = MutableStateFlow<PlanckAppState?>(null)
    val appState: StateFlow<PlanckAppState?> = _appState

    fun setAppState(state: PlanckAppState?) {
        _appState.value = state
    }
}

