package cc.polyfrost.betternickhider.config;

import cc.polyfrost.betternickhider.BetterNickHider;
import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.Button;
import cc.polyfrost.oneconfig.config.annotations.Checkbox;
import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import cc.polyfrost.oneconfig.config.data.OptionSize;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class BNHConfig extends Config {

    public ResourceLocation customSkinLocation;

    @Switch(
            name = "Show Self Normally",
            subcategory = "You"
    )
    public static boolean showSelfNormally = true;

    @Checkbox(
            name = "Use Custom Username",
            subcategory = "You"
    )
    public static boolean useCustomUsername = false;

    @Text(
            name = "Custom Username",
            subcategory = "You", size = OptionSize.DUAL
    )
    public static String customUsername = "I support color codes like \u00a7!";

    @Switch(
            name = "Don't Show Chat",
            subcategory = "Chat"
    )
    public static boolean dontShowChat = false;

    @Switch(
            name = "Use Friend Exclusions",
            subcategory = "Friends"
    )
    public static boolean useFriendExclusions = true;

    @Button(
            name = "Open Friends Exclusions JSON",
            text = "Open file", subcategory = "Friends"
    )
    Runnable openFriendsOverrideJSON = () -> {
        try {
            Desktop.getDesktop().open(BetterNickHider.INSTANCE.friendsOverrideJsonFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    };


    @Switch(
            name = "Show Own Skin On All Players",
            category = "Players", subcategory = "Skins"
    )
    public static boolean ownSkin = false;

    @Switch(
            name = "Show Default Skin For All Players",
            category = "Players", subcategory = "Skins"
    )
    public static boolean defaultSkin = true;

    @Switch(
            name = "Use Custom Skin Path",
            category = "Players", subcategory = "Skins"
    )
    public static boolean useCustomSkinPath = false;

    @Button(
            name = "Load Custom Skin", text = "Load",
            category = "Players", subcategory = "Skins"
    )
    Runnable loadSkin = (() -> {
        try {
            DynamicTexture texture = new DynamicTexture(ImageIO.read(new File(customSkinPath)));
            customSkinLocation = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("customSkin", texture);
        } catch (Exception e) {
            customSkinLocation = null;
            System.err.println("Error loading custom skin!");
            e.printStackTrace();
        }
    });

    @Text(
            name = "Custom Skin Path",
            category = "Players", subcategory = "Skins", size = 2
    )
    public static String customSkinPath = "C:\\Path\\To\\File.png";


    @Switch(
            name = "Use Random Usernames",
            category = "Players", subcategory = "Usernames"
    )
    public static boolean useRandomUsernames = true;

    @Button(
            name = "Regenerate Random Usernames",
            category = "Players", subcategory = "Usernames",
            text = "Regenerate"
    )
    Runnable runnable = (() -> {
        BetterNickHider.downloadFileRecursive(BetterNickHider.INSTANCE.usernameListFile, randomUsernameListFilepath);
        BetterNickHider.INSTANCE.updateUsernameList();
    });

    @Text(
            name = "Random Username List URL",
            category = "Players", subcategory = "Usernames", size = OptionSize.DUAL
    )
    public static String randomUsernameListFilepath = "https://raw.githubusercontent.com/dominictarr/random-name/master/first-names.txt";


    @Slider(name = "Cache Size (requires restart)",
            min = 200, max = 2000,
            subcategory = "Cache"
    )
    public int randomUsernameTableSize = 300;

    @Button(
            name = "Clear Cache", text = "Clear",
            subcategory = "Cache"
    )
    Runnable clear = (() -> {
        BetterNickHider.INSTANCE.nickCache.asMap().clear();
    });

    public BNHConfig() {
        super(new Mod("Better Nick Hider", ModType.UTIL_QOL), "betternickhider.json");
        addDependency("useCustomUsername", "customUsername");
        addDependency("useCustomSkinPath", "customSkinPath");
        loadSkin.run();
    }
}

