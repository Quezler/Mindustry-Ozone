/*
 * Copyright 2020 Itzbenz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package Ozone.Desktop.UI;

import Ozone.Desktop.Bootstrap.Dependency;
import Ozone.Desktop.Bootstrap.LibraryLoader;
import Ozone.Desktop.Propertied;
import Ozone.Experimental.Evasion.Identification;
import Ozone.Manifest;
import arc.Core;
import arc.scene.ui.Label;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.util.Interval;
import arc.util.Log;
import io.sentry.Sentry;
import mindustry.Vars;
import mindustry.core.Version;
import mindustry.gen.Icon;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EnvironmentInformation extends OzoneBaseDialog {
    Table table = new Table();
    ScrollPane scrollPane = new ScrollPane(table);
    Interval timer = new Interval();
    boolean b;
    public EnvironmentInformation() {
        super("Environment Information");
        icon = Icon.info;
        buttons.button("Refresh", Icon.refresh, this::generate).size(210f, 64f);

    }

    void setup() {
        table = new Table();
        scrollPane = new ScrollPane(table);
        cont.clear();
        generate();
        cont.add(scrollPane).growX().growY();
    }

    void update() {

    }

    void generate() {
        table.clearChildren();

        ad("Player Name", Vars.player.name);
        ad("UUID", Core.settings.getString("uuid"));
        ad("Current Millis", System.currentTimeMillis());
        ad(Propertied.Manifest);
        ad(Version.h);
        dep();
        uid();
    }

    void dep() {
        try {
            for (URL u : ((LibraryLoader) this.getClass().getClassLoader()).getURLs()) {
                ad("Library", u.toExternalForm());
            }
        } catch (Throwable ignored) {
        }
        for (Dependency d : Dependency.dependencies)
            try {
                ad(d.type.name(), d.getDownload());
            } catch (Throwable i) {
                ad(d.type.name(), i.toString());
            }
        if (!b)
            try {
                Dependency.save();
                b = true;
            } catch (Throwable ignored) {

            }
    }

    void uid() {
        try {
            ObjectMap<String, Object> values = Identification.getValue();
            ArrayList<String> yikes = new ArrayList<>();
            for (String s : values.keys()) yikes.add(s);
            String[] keys = yikes.toArray(new String[0]);
            List<String> key = Arrays.stream(keys).filter(s -> s.startsWith("usid-")).collect(Collectors.toList());
            for (String k : key) {
                ad(k, Core.settings.getString(k));
            }
        }catch (Throwable t) {
            Log.err(t);
            Sentry.captureException(t);
            t.printStackTrace();
        }
        for (Map.Entry<Object, Object> s : System.getProperties().entrySet())
            ad(s.getKey().toString(), s.getValue().toString());
    }

    void ad(Map<String, String> map) {
        for (Map.Entry<String, String> s : map.entrySet())
            ad(s.getKey(), s.getValue());
    }

    void ad(String title, Object value) {
        if (value == null) value = "null";
        Label l = new Label(title + ":");
        table.add(l).growX();
        String finalValue = String.valueOf(value);
        table.row();
        table.field(finalValue, s -> {
            generate();
            Core.app.setClipboardText(finalValue);
            Manifest.toast("Copied");
        }).expandX().disabled(true).growX();
        /*
        table.button(Icon.copy, () -> {
            Core.app.setClipboardText(finalValue);
            Manifest.toast("Copied");
        }).right();

         */
        table.row();
    }
}
