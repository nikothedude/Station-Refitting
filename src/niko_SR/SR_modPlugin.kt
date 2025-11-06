package niko_SR

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global

class SR_modPlugin: BaseModPlugin() {

    override fun onApplicationLoad() {
        super.onApplicationLoad()

        SR_settings.loadOpCosts()
        SR_settings.applyOpCosts()
    }

    override fun onGameLoad(newGame: Boolean) {
        super.onGameLoad(newGame)

        Global.getSector().listenerManager.addListener(SR_refitStationOptionAdder(), true)
    }

}