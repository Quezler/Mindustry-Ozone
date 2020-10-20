package Ozone;


import Atom.Struct.Filter;
import Atom.Utility.Pool;
import Atom.Utility.Random;
import Ozone.Patch.ImprovisedKeybinding;
import arc.Events;
import arc.backend.sdl.jni.SDL;
import arc.struct.ObjectMap;
import mindustry.Vars;
import mindustry.core.GameState;
import mindustry.game.EventType;
import mindustry.world.Tile;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Future;

public class Interface {
    protected static final ObjectMap<String, String> bundle = new ObjectMap<>();
    protected static final ObjectMap<ImprovisedKeybinding, Runnable> keybindings = new ObjectMap<>();

    //on load event show this stupid warning
    public static void warningUI(String title, String description) {
        if (Vars.ui == null)
            Events.on(EventType.ClientLoadEvent.class, s -> Vars.ui.showErrorMessage(title + "\n" + description));
        else
            Vars.ui.showErrorMessage(title + "\n" + description);
    }

    public synchronized static void registerKeybinding(ImprovisedKeybinding b, Runnable r) {
        keybindings.put(b, r);
    }

    public synchronized static void registerWords(String key, String value) {
        bundle.put(key, value);
    }

    public synchronized static void registerWords(String key) {
        bundle.put(key, key);
    }

    public static void restart() {
        SDL.SDL_ShowSimpleMessageBox(64, "Ozone", "You need to restart mindustry");
        //try restart
        try {
            //get JRE or something
            final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
            //get Mindustry Jar
            final File currentJar = new File(Vars.class.getProtectionDomain().getCodeSource().getLocation().toURI());

            //it is a jar ?
            if (!currentJar.getName().endsWith(".jar"))
                throw new RuntimeException(currentJar.getAbsolutePath() + " is not a jar");

            //java -jar path/to/Mindustry.jar
            ArrayList<String> command = new ArrayList<>();
            command.add(javaBin);
            command.add("-jar");
            command.add(currentJar.getPath());

            ProcessBuilder builder = new ProcessBuilder(command);
            builder.start();
        } catch (Throwable ignored) {
            //mmm android
        }
        //exit is priority
        System.exit(0);
    }

    public static Future<Tile> getTile(Filter<Tile> filter) {
        if (!Vars.state.equals(GameState.State.playing)) return null;
        return Pool.submit(() -> {
            ArrayList<Tile> list = new ArrayList<>();
            for (Tile t : Vars.world.tiles) {
                if (!filter.accept(t)) continue;
                list.add(t);
            }
            return Random.getRandom(list);
        });
    }
}





