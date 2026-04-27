package com.swordfish.lemuroid.app.tv.game

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.game.BaseGameActivity
import com.swordfish.lemuroid.app.shared.game.BaseGameScreenViewModel
import com.swordfish.lemuroid.app.tv.gamemenu.TVGameMenuActivity
import com.swordfish.lemuroid.common.coroutines.launchOnState
import com.swordfish.lemuroid.common.coroutines.safeCollect
import com.swordfish.lemuroid.common.displayToast
import kotlinx.coroutines.flow.filter

class TVGameActivity : BaseGameActivity() {
    override fun getDialogClass() = TVGameMenuActivity::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeFlows()
    }

    @Composable
    override fun GameScreen(viewModel: BaseGameScreenViewModel) {
        TVGameScreen(viewModel)
    }

    private fun initializeFlows() {
        launchOnState(Lifecycle.State.CREATED) {
            initializeShortcutToastFlow()
        }
    }

    private suspend fun initializeShortcutToastFlow() {
        kotlinx.coroutines.flow.combine(
            inputDeviceManager.getDistinctGamePadsObservable(),
            inputDeviceManager.getEnabledInputsObservable(),
            inputDeviceManager.getInputBindingsObservable()
        ) { allPads, enabledPads, bindingsLookup ->
            Triple(allPads, enabledPads, bindingsLookup)
        }.safeCollect { (allPads, enabledPads, bindingsLookup) ->
            if (allPads.isNotEmpty() && enabledPads.isEmpty()) {
                displayToast(R.string.tv_game_message_gamepad_disabled)
            } else if (allPads.isEmpty()) {
                displayToast(R.string.tv_game_message_missing_gamepad)
            } else {
                val hasBrokenBindings = enabledPads.any { pad ->
                    val bindings = bindingsLookup(pad)
                    val mappedRetroKeys = bindings.values.map { it.keyCode }
                    val hasA = mappedRetroKeys.contains(android.view.KeyEvent.KEYCODE_BUTTON_A)
                    val hasB = mappedRetroKeys.contains(android.view.KeyEvent.KEYCODE_BUTTON_B)
                    !hasA || !hasB
                }
                if (hasBrokenBindings) {
                    displayToast(R.string.tv_game_message_unmapped_gamepad)
                }
            }
        }
    }
}
