package niko_SR.console

import niko_SR.SR_settings
import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console

class SR_loadSettings: BaseCommand {
    override fun runCommand(
        args: String,
        context: BaseCommand.CommandContext
    ): BaseCommand.CommandResult? {

        SR_settings.loadSettings()

        Console.showMessage("Successfully reloaded settings!")

        return BaseCommand.CommandResult.SUCCESS
    }
}