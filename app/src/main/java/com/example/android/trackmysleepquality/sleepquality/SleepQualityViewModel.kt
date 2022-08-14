package com.example.android.trackmysleepquality.sleepquality

import android.app.Application
import androidx.lifecycle.*
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class SleepQualityViewModel(
        /**
         *Our ViewModel needs access to the data in the database,which is through the interface defined in the DAO.
         *So we pass in an instance of the SleepDatabaseDao.
         *
         **/
        private val sleepNightKey: Long = 0L,
        val database: SleepDatabaseDao):ViewModel()
{
        private var viewModelJob= Job()
        private val uiScope= CoroutineScope(Dispatchers.Main+viewModelJob)

        /**
         * Variable that tells the fragment whether it should navigate to [SleepTrackerFragment].
         *
         * This is `private` because we don't want to expose the ability to set [MutableLiveData] to
         * the [Fragment]
         */
        private val _navigateToSleepTracker = MutableLiveData<Boolean?>()
        /**
         * When true immediately navigate back to the [SleepTrackerFragment]
         */
        val navigateToSleepTracker: LiveData<Boolean?>
                get() = _navigateToSleepTracker
        /**
         * Call this immediately after navigating to [SleepTrackerFragment]
         */
        fun doneNavigating() {
                _navigateToSleepTracker.value = null
        }
        /**
         * Sets the sleep quality and updates the database.
         *
         * Then navigates back to the SleepTrackerFragment.
         */
        fun onSetSleepQuality(quality: Int) {
                viewModelScope.launch withContext@{
                        val tonight = database.get(sleepNightKey) ?: return@withContext
                        tonight.sleepQuality = quality
                        database.update(tonight)
                     // Setting this state variable to true will alert the observer and trigger navigation.
                        _navigateToSleepTracker.value = true
                }
        }
        override fun onCleared() {
                super.onCleared()
                viewModelJob.cancel()
        }

}