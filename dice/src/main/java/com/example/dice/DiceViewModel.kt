package com.example.dice

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.random.Random

class DiceViewModel : ViewModel() {

    companion object {
        private const val DICE_COUNT = 5
        private const val TICK_DELAY_MS = 100L
    }

    private val _diceValues = MutableLiveData(List(DICE_COUNT) { 6 })
    val diceValues: LiveData<List<Int>> get() = _diceValues

    private val _isRolling = MutableLiveData(false)
    val isRolling: LiveData<Boolean> get() = _isRolling

    private val lock = Any()
    private val currentValues = IntArray(DICE_COUNT) { 6 }
    private val activeThreads = CopyOnWriteArrayList<Thread>()

    @Volatile
    private var isCleared = false

    fun rollDice() {
        if (_isRolling.value == true) return
        _isRolling.value = true

        activeThreads.clear()
        val remainingThreads = AtomicInteger(DICE_COUNT)

        for (index in 0 until DICE_COUNT) {
            val iterations = Random.nextInt(15, 31)

            val worker = thread(start = false, name = "DiceThread-$index") {
                try {
                    repeat(iterations) {
                        if (isCleared || Thread.currentThread().isInterrupted) return@thread

                        val newValue = Random.nextInt(1, 7)
                        val snapshot = synchronized(lock) {
                            currentValues[index] = newValue
                            currentValues.toList()
                        }
                        _diceValues.postValue(snapshot)

                        Thread.sleep(TICK_DELAY_MS)
                    }
                } catch (_: InterruptedException) {
                    Thread.currentThread().interrupt()
                } finally {
                    if (remainingThreads.decrementAndGet() == 0 && !isCleared) {
                        _isRolling.postValue(false)
                    }
                }
            }
            activeThreads.add(worker)
            worker.start()
        }
    }

    override fun onCleared() {
        super.onCleared()
        isCleared = true
        activeThreads.forEach { it.interrupt() }
        activeThreads.clear()
    }
}