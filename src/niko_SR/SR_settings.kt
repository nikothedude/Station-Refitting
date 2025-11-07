package niko_SR

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipVariantAPI
import lunalib.lunaSettings.LunaSettings
import niko_SR.hullmods.SR_stationBlacklister
import org.lazywizard.lazylib.ext.json.getFloat
import java.awt.Color
import kotlin.collections.set

object SR_settings {

    const val OVERRIDE_CSV_PATH = "data/hulls/SR_stationOP.csv"
    const val CSV_PATH = "data/hulls/SR_stationModules.csv"
    const val MODID = "niko_stationRefitting"
    var opMult = 1.15f

    val opCosts = HashMap<String, Int>()

    fun loadSettings() {
        opMult = LunaSettings.getFloat(MODID, "SR_opMult")!!
    }

    fun loadOpCosts() {
        val csv = Global.getSettings().getMergedSpreadsheetDataForMod("hullId", CSV_PATH, MODID)
        val overrideCsv = Global.getSettings().getMergedSpreadsheetDataForMod("hullId", OVERRIDE_CSV_PATH, MODID)

        opCosts.clear()
        for (index in 0 until csv.length()) {
            val row = csv.getJSONObject(index)

            val id = row.getString("hullId")
            if (id == null || id.isEmpty() || id.startsWith("#")) continue

            val variants = digForVariants(id)
            if (variants.isEmpty()) {
                continue
            }
            val totalOpCost = (variants.maxOf { it.computeOPCost(null) } * opMult).toInt()

            opCosts[id] = totalOpCost
        }

        for (index in 0 until overrideCsv.length()) {
            val row = overrideCsv.getJSONObject(index)

            val id = row.getString("hullId")
            if (id == null || id.isEmpty() || id.startsWith("#")) continue

            val op = row.getInt("OP")

            opCosts[id] = op
        }
    }

    private fun digForVariants(hullId: String): ArrayList<ShipVariantAPI> {
        val variants = ArrayList<ShipVariantAPI>()

        // cant use hullidtovariantmap bc modules arent included
        for (variantId in Global.getSettings().allVariantIds) {
            val variant = Global.getSettings().getVariant(variantId)
            if (variant.hullSpec?.hullId != hullId) continue
            variants += variant
        }

        return variants
    }

    fun applyOpCosts() {
        for (entry in opCosts) {
            try {
                val spec = Global.getSettings().getHullSpec(entry.key)
                spec.addBuiltInMod(SR_stationBlacklister.HMOD_ID)
                val op = entry.value

                ReflectionUtils.invoke("setOrdnancePoints", spec, op)

            } catch (ex: RuntimeException) {
                // this is how we ignore bad hullspecs
                continue
            }

        }
    }

}