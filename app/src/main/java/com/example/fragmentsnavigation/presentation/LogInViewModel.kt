package com.example.fragmentsnavigation.presentation

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.fragmentsnavigation.App
import com.example.fragmentsnavigation.domain.RequestResult
import com.example.fragmentsnavigation.domain.UploadRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


sealed class UiEvent{
    data class NavigateTo(val dest: String): UiEvent()
    data class ShowToast(val message: String): UiEvent()
    object Back: UiEvent()
}


class LogInViewModel(
    private val uploadRepository: UploadRepository,
    private val prefs: SharedPreferences,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    // TODO: Implement the ViewModel

    private val TAG = this::class.java.simpleName

    private val _showProgressBar = MutableStateFlow(false)
    val showProgressBar: StateFlow<Boolean> = _showProgressBar

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()


    fun login(username: String, password: String){
        viewModelScope.launch{
            _showProgressBar.value = true
            val res = withContext(Dispatchers.IO){
                uploadRepository.login(username, password)
            }
            _showProgressBar.value = false
            when(res){
                is RequestResult.Authorized ->{
                    Log.e(TAG, "got new token ${res.data}")
                    prefs.edit().putString("token", res.data).apply()
                    _uiEvent.emit(UiEvent.NavigateTo("list"))
                }
                is RequestResult.Unauthorized -> {
                    Log.e(TAG, "unauthorized")
                    _uiEvent.emit(UiEvent.ShowToast("Unauthorized"))
                }
                is RequestResult.UnknownError -> {
                    Log.e(TAG, "unknown error")
                    _uiEvent.emit(UiEvent.ShowToast("Unknown error"))
                }
            }
        }
    }

    override fun onCleared() {
        Log.e(TAG, "View Model cleared")
        super.onCleared()

    }

    companion object {
        //using kotlin DSL
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                val myRepository = (this[APPLICATION_KEY] as App).uploadRepository
                val prefs = (this[APPLICATION_KEY] as App).prefs

                LogInViewModel(
                    uploadRepository = myRepository,
                    prefs = prefs,
                    savedStateHandle = savedStateHandle
                )
            }
        }

//        //Classic variant
//        val Factory1: ViewModelProvider.Factory = object: ViewModelProvider.Factory {
//            @Suppress("UNCHECKED_CAST")
//            override fun <T : ViewModel> create(
//                modelClass: Class<T>,
//                extras: CreationExtras
//            ): T {
//                val application = checkNotNull(extras[APPLICATION_KEY])
//                val savedStateHandle = extras.createSavedStateHandle()
//
//                return LogInViewModel(
//                    myRepository = (application as App).uploadRepository,
//                    savedStateHandle = savedStateHandle
//                ) as T
//            }
//        }
    }
}