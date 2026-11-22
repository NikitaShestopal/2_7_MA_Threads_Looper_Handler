package com.example.dice

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlin.random.Random

class DiceModel {

    companion object {
        const val DICE_COUNT = 5
        private const val TICK_DELAY_MS = 100L
        private const val MIN_TICKS = 15 // 1.5 сек
        private const val MAX_TICKS = 30 // 3.0 сек
    }
    suspend fun rollSingleDie(onTick: (Int) -> Unit): Int {
        val ticks = Random.nextInt(MIN_TICKS, MAX_TICKS + 1)
        var currentFace = 6

        repeat(ticks) {
            currentFace = Random.nextInt(1, 7)
            onTick(currentFace)
            delay(TICK_DELAY_MS)
        }

        return currentFace
    }
    suspend fun rollAllDice(
        diceCount: Int = DICE_COUNT,
        onDieChanged: (index: Int, value: Int) -> Unit
    ): List<Int> = coroutineScope {
        val deferredRolls = List(diceCount) { index ->
            async {
                rollSingleDie { value ->
                    onDieChanged(index, value)
                }
            }
        }
        deferredRolls.awaitAll()
    }
}