package com.example.iucampus.data.dining

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONArray
import java.io.InputStreamReader

class DiningRepository(private val context: Context) {
    fun getFacilities(): Flow<List<DiningFacility>> = flow {
        val facilities = mutableListOf<DiningFacility>()
        try {
            val inputStream = context.assets.open("restaurants.json")
            val jsonText = InputStreamReader(inputStream).readText()
            val jsonArray = JSONArray(jsonText)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val coords = obj.getJSONObject("coordinates")
                val openingArray = obj.optJSONArray("opening")
                val openingList = mutableListOf<String>()
                if (openingArray != null) {
                    for (j in 0 until openingArray.length()) {
                        openingList.add(openingArray.getString(j))
                    }
                }
                facilities.add(
                    DiningFacility(
                        name = obj.optString("name"),
                        cuisine = obj.optString("cuisine"),
                        address = obj.optString("address"),
                        latitude = coords.optDouble("latitude"),
                        longitude = coords.optDouble("longitude"),
                        opening = openingList,
                        phoneNumber = obj.optString("phone_number"),
                        image = obj.optString("image"),
                        rate = obj.optDouble("rate")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        emit(facilities)
    }
}
