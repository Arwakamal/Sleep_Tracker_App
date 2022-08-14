package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import android.provider.SyncStateContract.Helpers.insert
import android.provider.SyncStateContract.Helpers.update
import androidx.lifecycle.*
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.*

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {

            private var viewModelJob= Job()

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
    /**
     * the scope determines what thread the coroutine will run on , and it also need to about the job */
    private val uiScope= CoroutineScope(Dispatchers.Main+viewModelJob)
    /**tonight->to hold the current night.+we make this live data because we want to be able to observe it
    MutableLiveData ->so that we can change it.**/
    
    private var tonight = MutableLiveData<SleepNight?>()
    private val nights = database.getAllNights()
    val nightsString = Transformations.map(nights) { nights ->
        formatNights(nights, application.resources)
    }

    private val _navigateToSleepQuality = MutableLiveData<SleepNight>()

    val navigateToSleepQuality: LiveData<SleepNight>
        get() = _navigateToSleepQuality
    fun doneNavigating() {
        _navigateToSleepQuality.value = null
    }

    init {
        initializeTonight()
    }

    private fun initializeTonight() {
        /**we are using a coroutines to get tonight form the database.
         * so that we are not blocking the ui while waiting for the result.**/
        //in this scope we creating coroutine
        //creates coroutine without blocking the current thread in the context defined by the scope.
       uiScope.launch {
            tonight.value = getTonightFromDatabase()
        }
    }

    private suspend fun getTonightFromDatabase():  SleepNight?
    {
        return withContext(Dispatchers.IO)
        {
            var night = database.getTonight()

            if (night?.endTimeMilli != night?.startTimeMilli) {
                night = null
            }
            night
        }


    }
    fun onStartTracking()
    {
        uiScope.launch{
            val newNight = SleepNight()
            insert(newNight)
            tonight.value = getTonightFromDatabase()
        }
    }

    private suspend fun insert(night: SleepNight) {
        withContext(Dispatchers.IO){
            database.insert(night)
        }
    }
    
//    fun someWorkNeedsToBeDone(){
//        uiScope.launch {
//            suspendFunction()
//        }
//    }
//
//    private suspend fun suspendFunction() {
//        withContext(Dispatchers.IO){
//            longruningwork()
//        }
//    }
    fun onStopTracking() {
        viewModelScope.launch {
            val oldNight = tonight.value ?: return@launch
            oldNight.endTimeMilli = System.currentTimeMillis()
            update(oldNight)

            _navigateToSleepQuality.value = oldNight
        }


}

    private suspend fun update(night: SleepNight) {
        database.update(night)
    }
    fun onClear() {
        viewModelScope.launch {
            clear()
            tonight.value = null
        }
    }

    suspend fun clear() {
        database.clear()
    }
}

