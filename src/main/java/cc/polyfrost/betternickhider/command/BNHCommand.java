package cc.polyfrost.betternickhider.command;

import cc.polyfrost.betternickhider.BetterNickHider;
import cc.polyfrost.oneconfig.utils.commands.annotations.Command;
import cc.polyfrost.oneconfig.utils.commands.annotations.Main;

@Command(value = BetterNickHider.MODID, description = "Access the " + BetterNickHider.NAME + " GUI.")
public class BNHCommand {

    @Main
    private static void main() {
        BetterNickHider.config.openGui();
    }
}