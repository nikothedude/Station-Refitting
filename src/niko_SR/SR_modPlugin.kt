package niko_SR

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import lunalib.lunaSettings.LunaSettings
import lunalib.lunaSettings.LunaSettingsListener

class SR_modPlugin: BaseModPlugin() {

    override fun onApplicationLoad() {
        super.onApplicationLoad()

        SR_settings.loadSettings()
        SR_settings.loadOpCosts()
        SR_settings.applyOpCosts()

        LunaSettings.addSettingsListener(settingsChangedListener())
    }

    override fun onGameLoad(newGame: Boolean) {
        super.onGameLoad(newGame)

        Global.getSector().listenerManager.addListener(SR_refitStationOptionAdder(), true)
    }

    class settingsChangedListener : LunaSettingsListener {
        override fun settingsChanged(modID: String) {
            SR_settings.loadSettings()
        }
    }

}