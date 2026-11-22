package com.example.dice

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DiceViewModel : ViewModel() {

    private val diceModel = DiceModel()
    private val currentValues = IntArray(DiceModel.DICE_COUNT) { 6 }

    private val _diceValues = MutableLiveData(currentValues.toList())
    val diceValues: LiveData<List<Int>> get() = _diceValues

    private val _isRolling = MutableLiveData(false)
    val isRolling: LiveData<Boolean> get() = _isRolling

    private val _buttonText = MutableLiveData("ROLL")
    val buttonText: LiveData<String> get() = _buttonText

    private var rollJob: Job? = null

    fun rollDice() {
        rollJob?.cancel()

        rollJob = viewModelScope.launch {
            _isRolling.value = true
            _buttonText.value = "RESTART"

            diceModel.rollAllDice(DiceModel.DICE_COUNT) { index, value ->
                currentValues[index] = value
                _diceValues.value = currentValues.toList()
            }

            _isRolling.value = false
            _buttonText.value = "ROLL"
        }
    }
}