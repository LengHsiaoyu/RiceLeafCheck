package com.riceleaf.local.ml

object SeverityMapper {

    fun mapLesionRatioToSeverity(ratio: Float, thresholds: List<List<Float>>): Int {
        for (entry in thresholds) {
            if (entry.size >= 2 && ratio <= entry[1]) {
                return entry[0].toInt()
            }
        }
        return 9
    }
}
