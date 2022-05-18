package com.aneesh.easyjourney

import android.app.Application
import com.google.android.libraries.places.api.Places
import com.google.maps.GeoApiContext
import com.aneesh.easyjourney.simulator.Simulator

class RideSharingApp : Application() {

    override fun onCreate() {
        super.onCreate()

        //API key -> MAPS_API_KEY
        Places.initialize(applicationContext, MAPS_API_KEY);
        Simulator.geoApiContext = GeoApiContext.Builder()
            .apiKey(MAPS_API_KEY)
            .build()
    }

}