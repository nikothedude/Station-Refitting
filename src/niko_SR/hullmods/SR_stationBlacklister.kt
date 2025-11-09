package niko_SR.hullmods

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.loading.VariantSource
import com.fs.starfarer.api.util.Misc
import niko_SR.SR_baseNikoScript
import org.magiclib.util.MagicIncompatibleHullmods

class SR_stationBlacklister: BaseHullMod() {

    companion object {
        const val HMOD_ID = "SR_stationBlacklister"
    }

    fun isHmodIncompatible(hullmodId: String, station: ShipAPI): Boolean {
        val spec = Global.getSettings().getHullModSpec(hullmodId)

        if (station.variant.hasHullMod("supercomputer") || station.variant.hasHullMod(HullMods.ADVANCED_TARGETING_CORE)) {
            if (hullmodId == HullMods.DEDICATED_TARGETING_CORE || hullmodId == HullMods.INTEGRATED_TARGETING_UNIT) {
                return true
            }
        }
        /*if (station.variant.hasHullMod("always_detaches")) {
            if (hullmodId == HullMods.REINFORCEDHULL) return true
        }*/

        if (hullmodId == HullMods.SAFETYOVERRIDES) return true
        if (hullmodId == HullMods.SHROUDED_MANTLE) return true
        if (hullmodId == HullMods.OPERATIONS_CENTER) return true
        if (hullmodId == "escort_package") return true
        if (hullmodId == HullMods.NEURAL_INTERFACE || hullmodId == HullMods.NEURAL_INTEGRATOR) return true

        return false
    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        super.applyEffectsAfterShipCreation(ship, id)

        if (ship == null) return

        for (hmod in ship.variant.hullMods.toList()) {
            if (!isHmodIncompatible(hmod, ship)) continue
            MagicIncompatibleHullmods.removeHullmodWithWarning(
                ship.variant,
                hmod,
                HMOD_ID
            )
        }

        //HmodPropogationScript(ship).start()
    }

    /*class HmodPropogationScript(
        val ship: ShipAPI
    ): SR_baseNikoScript() {
        var timesRan = 0f
        override fun startImpl() {
            Global.getSector().addScript(this)
        }

        override fun stopImpl() {
            Global.getSector().removeScript(this)
        }

        override fun runWhilePaused(): Boolean = true

        override fun advance(amount: Float) {
            timesRan++
            if (timesRan > 3f) {
                delete()
                return
            }

            for (child in ship.childModulesCopy) {
                if (HMOD_ID !in child.variant.permaMods) {
                    child.variant.addPermaMod(HMOD_ID)
                }
            }
        }
    }*/

}