package niko_SR

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.CoreUITabId
import com.fs.starfarer.api.campaign.FleetInflater
import com.fs.starfarer.api.campaign.econ.Industry
import com.fs.starfarer.api.campaign.listeners.BaseIndustryOptionProvider
import com.fs.starfarer.api.campaign.listeners.DialogCreatorUI
import com.fs.starfarer.api.campaign.listeners.IndustryOptionProvider.IndustryOptionData
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.impl.campaign.econ.impl.OrbitalStation
import com.fs.starfarer.api.impl.campaign.ids.Industries
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class SR_refitStationOptionAdder: BaseIndustryOptionProvider() {

    companion object {
        const val REFIT_OPT_ID = "SR_refitStationOptId"
        const val INFLATER_CACHE = "\$SR_inflaterCache"
        const val HULLID_CACHE = "\$SR_hullIdCache"
        const val MODULE_CACHE = "\$SR_moduleCache"
    }

    override fun isUnsuitable(ind: Industry, allowUnderConstruction: Boolean): Boolean {
        val superResult = super.isUnsuitable(ind, allowUnderConstruction)
        if (superResult) return true

        if (!ind.spec.hasTag(Industries.TAG_STATION) && !ind.spec.hasTag("artillery")) return true
        if (!ind.market.isPlayerOwned && !Global.getSettings().isDevMode) return true

        return false
    }

    override fun createTooltip(opt: IndustryOptionData?, tooltip: TooltipMakerAPI?, width: Float) {
        super.createTooltip(opt, tooltip, width)

        if (tooltip == null) return

        tooltip.addPara(
            "Refit your orbital station. This option is disabled if the station is damaged, or otherwise non-combat capable.",
            0f
        )

        tooltip.addPara(
            "This allows you to mount %s, %s and %s, however your faction will %s.",
            5f,
            Misc.getHighlightColor(),
            "custom weaponry", "custom strikecraft", "custom hullmods", "not be able to cover the costs"
        ).setHighlightColors(
            Misc.getHighlightColor(),
            Misc.getHighlightColor(),
            Misc.getHighlightColor(),
            Misc.getNegativeHighlightColor()
        )

        tooltip.addPara(
            "BE WARNED! Certain hullmods will NOT FUNCTION on a station!",
            5f
        ).color = Misc.getNegativeHighlightColor()

        tooltip.addPara(
            "ALL OP COSTS ARE DETERMINED BY STATION REFITTING! IF YOU HAVE GRIEVANCE, TAKE IT UP WITH NIKO AND NOT THE ORIGINAL AUTHOR!!!!!!!!!!!!!",
            5f
        ).color = Misc.getNegativeHighlightColor()
    }

    override fun getIndustryOptions(ind: Industry?): List<IndustryOptionData>? {
        if (ind == null) return null
        if (isUnsuitable(ind, true)) return null

        val disabledText = if (!ind.isFunctional) " (Station disabled)" else ""
        val data = IndustryOptionData(
            "Refit$disabledText",
            REFIT_OPT_ID,
            ind,
            this
        )
        data.enabled = ind.isFunctional

        return listOf(data)
    }

    override fun optionSelected(opt: IndustryOptionData?, ui: DialogCreatorUI?) {
        if (opt == null || ui == null) return
        if (opt.id != REFIT_OPT_ID) return

        if (!opt.ind.isFunctional) return

        val station: CampaignFleetAPI
        if (opt.ind is OrbitalStation) {
            station = (opt.ind as OrbitalStation).stationFleet
        } else if (ReflectionUtils.hasVariableOfName("stationFleet", opt.ind)) {
            station = ReflectionUtils.get("stationFleet", opt.ind) as? CampaignFleetAPI ?: return
        } else {
            station = Misc.getStationFleet(opt.ind.market) ?: return
        }

        if (!station.isEmpty) {
            val member = station.fleetData.membersListCopy.first()
            val cached = station.memoryWithoutUpdate.getString(HULLID_CACHE)
            if (cached != null && cached != member.hullId) {
                val inflater = station.memoryWithoutUpdate[INFLATER_CACHE] as? FleetInflater
                if (inflater != null) {
                    station.inflater = inflater
                    @Suppress("UsePropertyAccessSyntax")
                    station.setInflated(null)
                }
            }
            station.memoryWithoutUpdate[HULLID_CACHE] = member.hullId
        }
        station.inflateIfNeeded()
        if (station.inflater != null) {
            station.memoryWithoutUpdate[INFLATER_CACHE] = station.inflater
            station.inflater = null

            if (!station.isEmpty) {
                val member = station.fleetData.membersListCopy.first()

                val existingCache = station.memoryWithoutUpdate[MODULE_CACHE] as? HashMap<String, ShipVariantAPI>
                if (existingCache != null && existingCache.isNotEmpty()) {
                    for (entry in existingCache) {
                        val slot = entry.key
                        val variant = entry.value
                        member.variant.setModuleVariant(slot, variant)
                    }
                }
            }
        }
        VariantSaver.init(station)
        val oldPlayerFleet = Global.getSector().playerFleet
        Global.getSector().playerFleet = station
        val ui = Global.getSector().campaignUI
        ui.showCoreUITab(CoreUITabId.REFIT)
        Global.getSector().playerFleet = oldPlayerFleet
    }

    class VariantSaver(
        val station: CampaignFleetAPI
    ): SR_baseNikoScript() {

        companion object {
            fun init(station: CampaignFleetAPI) {
                if (Global.getSector().hasTransientScript(VariantSaver::class.java)) return
                VariantSaver(station).start()
            }
        }

        override fun startImpl() {
            Global.getSector().addTransientScript(this)
        }

        override fun stopImpl() {
            Global.getSector().removeTransientScript(this)
        }

        override fun runWhilePaused(): Boolean = true

        override fun advance(amount: Float) {
            if (station.fleetData.membersListCopy.isEmpty()) {
                delete()
                return
            }
            if (Global.getSector().campaignUI.currentCoreTab != CoreUITabId.REFIT) {
                delete()
                return
            }
            val member = station.fleetData.membersListCopy.first()
            val modules = HashMap<String, ShipVariantAPI>()
            for (slot in member.variant.moduleSlots) {
                val variant = member.variant.getModuleVariant(slot)
                modules[slot] = variant
            }
            station.memoryWithoutUpdate[MODULE_CACHE] = modules
        }
    }

}