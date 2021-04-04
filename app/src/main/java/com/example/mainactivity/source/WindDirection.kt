package com.example.mainactivity.source

import android.content.Context
import com.example.mainactivity.R


enum class WindDirection {
    // don't change order
    NORTH, NORTH_NORTH_EAST, NORTH_EAST, EAST_NORTH_EAST, EAST, EAST_SOUTH_EAST, SOUTH_EAST, SOUTH_SOUTH_EAST, SOUTH, SOUTH_SOUTH_WEST, SOUTH_WEST, WEST_SOUTH_WEST, WEST, WEST_NORTH_WEST, NORTH_WEST, NORTH_NORTH_WEST;

    fun getLocalizedString(context: Context): String {
        return context.resources.getStringArray(R.array.windDirections)[ordinal]
    }

    fun getArrow(context: Context): String {
        return context.resources.getStringArray(R.array.windDirectionArrows)[ordinal / 2]
    }

    companion object {
        @JvmOverloads
        fun byDegree(
            degree: Double,
            numberOfDirections: Int = values().size
        ): WindDirection {
            val directions: Array<WindDirection> = WindDirection.values()
            val availableNumberOfDirections = directions.size
            val direction = WeatherInfo.windDirectionDegreeToIndex(degree, numberOfDirections)* availableNumberOfDirections / numberOfDirections
            return directions[direction]
        }
    }
}