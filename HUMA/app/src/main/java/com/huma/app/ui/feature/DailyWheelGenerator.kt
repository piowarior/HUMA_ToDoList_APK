package com.huma.app.ui.feature

import java.time.LocalDate
import kotlin.random.Random

object DailyWheelGenerator {

    fun generateDailyWheel(): List<String> {

        val seed = LocalDate.now().toEpochDay()
        val random = Random(seed)

        return ChallengeRepository.defaultChallenges
            .shuffled(random)
            .take(10)
    }
}