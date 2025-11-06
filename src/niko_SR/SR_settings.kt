package niko_SR

import com.fs.starfarer.api.Global
import org.lazywizard.lazylib.ext.json.getFloat
import java.awt.Color
import kotlin.collections.set

object SR_settings {

    const val CSV_PATH = "data/config/SR_stationOP.csv"
    const val MODID = "niko_stationRefitting"

    val opCosts = HashMap<String, Int>()

    fun loadOpCosts() {
        val csv = Global.getSettings().getMergedSpreadsheetDataForMod("hullId", CSV_PATH, MODID)

        for (index in 0 until csv.length())
        {
            val row = csv.getJSONObject(index)

            val id = row.getString("hullId")
            if (id == null || id.isEmpty() || id.startsWith("#")) continue
            val op = row.getInt("OP")

            opCosts[id] = op
        }

    }

    fun applyOpCosts() {
        for (entry in opCosts) {
            val spec = Global.getSettings().getHullSpec(entry.key)
            val op = entry.value

            ReflectionUtils.invoke("setOrdnancePoints", spec, op)
        }
    }

}