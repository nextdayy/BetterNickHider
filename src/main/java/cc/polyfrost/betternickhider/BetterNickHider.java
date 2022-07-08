package cc.polyfrost.betternickhider;

import cc.polyfrost.betternickhider.command.BNHCommand;
import cc.polyfrost.betternickhider.config.BNHConfig;
import cc.polyfrost.oneconfig.config.profiles.Profiles;
import cc.polyfrost.oneconfig.libs.caffeine.cache.Cache;
import cc.polyfrost.oneconfig.libs.caffeine.cache.Caffeine;
import cc.polyfrost.oneconfig.utils.NetworkUtils;
import cc.polyfrost.oneconfig.utils.commands.CommandManager;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@net.minecraftforge.fml.common.Mod(modid = BetterNickHider.MODID, name = BetterNickHider.NAME, version = BetterNickHider.VERSION)
public class BetterNickHider {
    public static final String MODID = "@ID@";
    public static final String NAME = "@NAME@";
    public static final String VERSION = "@VER@";
    @net.minecraftforge.fml.common.Mod.Instance(MODID)
    public static BetterNickHider INSTANCE;
    public static BNHConfig config;
    public File usernameListFile;
    public File friendsOverrideJsonFile;
    public HashMap<String, String> friendsOverride;
    public String[] usernameArray;
    public Cache<String, String> nickCache;

    @net.minecraftforge.fml.common.Mod.EventHandler
    public void onFMLInitialization(net.minecraftforge.fml.common.event.FMLInitializationEvent event) {
        config = new BNHConfig();
        config.initialize();
        nickCache = Caffeine.newBuilder().expireAfterWrite(600, TimeUnit.SECONDS).maximumSize(config.randomUsernameTableSize).build();
        usernameListFile = new File(Profiles.getProfileDir(), "usernameList.txt");
        friendsOverrideJsonFile = new File(Profiles.getProfileDir(), "friends_override.json");
        try {
            if(friendsOverrideJsonFile.createNewFile()) {
                generateFriendsJson();
            }
            if(usernameListFile.createNewFile()) {
                downloadFileRecursive(usernameListFile, BNHConfig.randomUsernameListFilepath);
            }
            updateUsernameList();
            updateFriendsList();
        } catch (Exception e) {
            e.printStackTrace();
        }


        MinecraftForge.EVENT_BUS.register(this);
        CommandManager.INSTANCE.registerCommand(BNHCommand.class);
    }

    public String getPlayerName(String nameIn) {
        if (nameIn.length() < 2) return nameIn;
        if (BNHConfig.useFriendExclusions) {
            if (friendsOverride.containsKey(nameIn))
                return friendsOverride.get(nameIn);
        }
        if (BNHConfig.useRandomUsernames) {
            return nickCache.asMap().computeIfAbsent(nameIn, (key) -> {
                String random = genNewUsername();
                while (nickCache.asMap().containsValue(random)) {
                    random = genNewUsername();
                }
                return random;
            });
        } else return nameIn;
    }

    public String genNewUsername() {
        return usernameArray[(int) (Math.random() * config.randomUsernameTableSize)];
    }

    public static void downloadFileRecursive(File file, String url) {
        try {
            NetworkUtils.downloadFile(url, file);
        } catch (Exception e) {
            e.printStackTrace();
            downloadFileRecursive(file, url);
        }
    }

    public void updateUsernameList() {
        int i = 0;
        usernameArray = new String[config.randomUsernameTableSize];
        try (BufferedReader reader = new BufferedReader(new FileReader(usernameListFile))) {
            while (i < usernameArray.length) {
                usernameArray[i] = reader.lines().skip((long) (Math.random() * 10)).findFirst().orElse("Steve");
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        nickCache.asMap().clear();
    }

    public void updateFriendsList() throws Exception {
        friendsOverride = new HashMap<>();
        Set<Map.Entry<String, JsonElement>> set = new JsonParser().parse(new BufferedReader(new FileReader(friendsOverrideJsonFile))).getAsJsonObject().getAsJsonObject("friends").entrySet();
        set.forEach(entry -> friendsOverride.put(entry.getKey(), entry.getValue().getAsString()));
    }

    public void generateFriendsJson() {
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(friendsOverrideJsonFile))) {
            writer.write("{\n" +
                    "\t\"friends\": {\n" +
                    "\t\t\"nxtdaydelivery\": \"bobfish\",\n" +
                    "\t\t\"MoonTidez\": \"bobfish2\"\n" +
                    "\t}\n" +
                    "}");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void updatePlayerInfo(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (BetterNickHider.config.enabled) {
                if (Minecraft.getMinecraft().thePlayer == null) return;
                if (Minecraft.getMinecraft().thePlayer.sendQueue == null) return;
                Collection<NetworkPlayerInfo> collection = Minecraft.getMinecraft().thePlayer.sendQueue.getPlayerInfoMap();
                try {
                    collection.removeIf(info -> info.getDisplayName().getFormattedText().startsWith("\u00A78[NPC]") || info.getDisplayName().getFormattedText().startsWith("\u00a7e[NPC]") || info.getDisplayName().getFormattedText().startsWith("\u00a75[NPC]"));
                } catch (Exception ignored) {
                }
                collection.stream().map(NetworkPlayerInfo::getGameProfile).map(GameProfile::getName).forEach(this::getPlayerName);
            }
        }
    }

    @SubscribeEvent
    public void updatePlayerInfoChatBypass(ClientChatReceivedEvent event) {
        if (BetterNickHider.config.enabled) {
            if (BNHConfig.dontShowChat) event.setCanceled(true);
            String message = event.message.getFormattedText();
            if (message.contains(" the lobby!")) {
                try {
                    int index = message.indexOf(']') + 2;
                    getPlayerName(message.substring(index, message.indexOf('\u00a7', index)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
