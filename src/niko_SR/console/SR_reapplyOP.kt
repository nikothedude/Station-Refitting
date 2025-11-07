package niko_SR.console

import niko_SR.SR_settings
import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console

class SR_reapplyOP: BaseCommand {
    override fun runCommand(
        args: String,
        context: BaseCommand.CommandContext
    ): BaseCommand.CommandResult {

        SR_settings.loadOpCosts()
        SR_settings.applyOpCosts()
        Console.showMessage("Reapplied OP costs")

        return BaseCommand.CommandResult.SUCCESS
    }
}