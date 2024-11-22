package com.mco.accessability

import com.mco.accessability.models.MarkerData
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {

    private val _markerDataMap = MutableLiveData<MutableMap<String, MarkerData>>()
    val markerDataMap: MutableMap<String, MarkerData> = mutableMapOf()
    init {
        _markerDataMap.value = mutableMapOf()
    }

    fun addMarkerData(markerId: String, markerData: MarkerData) {
        _markerDataMap.value?.put(markerId, markerData)
        _markerDataMap.postValue(_markerDataMap.value)  // Notify observers
    }

    fun removeMarkerData(markerId: String) {
        _markerDataMap.value?.remove(markerId)
        _markerDataMap.postValue(_markerDataMap.value)
    }

    fun logMarkerData() {
        Log.d("SharedViewModel", "Marker Data Map:")
        _markerDataMap.value?.forEach { (id, data) ->
            Log.d("SharedViewModel", "ID: $id, Name: ${data.nameOfPlace}, Votes: ${data.rating}, Lat: ${data.lat}, Lng: ${data.lng}")
        }
    }
    fun getMarkerData(markerId: String): MarkerData? {
        return _markerDataMap.value?.get(markerId)
    }

    fun getAllMarkerData(): List<MarkerData> {
        return _markerDataMap.value?.values?.toList() ?: emptyList()
    }
}
