/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven;

import java.util.*;
import java.awt.font.TextAttribute;

public class OptWnd extends Window {
    public final Panel main, video, audio, display, map, general;
    public Panel current;

    public void chpanel(Panel p) {
        if (current != null)
            current.hide();
        (current = p).show();
    }

    public class PButton extends Button {
        public final Panel tgt;
        public final int key;

        public PButton(int w, String title, int key, Panel tgt) {
            super(w, title);
            this.tgt = tgt;
            this.key = key;
        }

        public void click() {
            chpanel(tgt);
        }

        public boolean type(char key, java.awt.event.KeyEvent ev) {
            if ((this.key != -1) && (key == this.key)) {
                click();
                return (true);
            }
            return (false);
        }
    }

    public class Panel extends Widget {
        public Panel() {
            visible = false;
            c = Coord.z;
        }
    }

    public class VideoPanel extends Panel {
        public VideoPanel(Panel back) {
            super();
            add(new PButton(200, "Back", 27, back), new Coord(100, 220));
            pack();
        }

        public class CPanel extends Widget {
            public final GLSettings cf;

            public CPanel(GLSettings gcf) {
                this.cf = gcf;
                int y = 0;
                add(new CheckBox("Per-fragment lighting") {
                    {
                        a = cf.flight.val;
                    }

                    public void set(boolean val) {
                        if (val) {
                            try {
                                cf.flight.set(true);
                            } catch (GLSettings.SettingException e) {
                                getparent(GameUI.class).error(e.getMessage());
                                return;
                            }
                        } else {
                            cf.flight.set(false);
                        }
                        a = val;
                        cf.dirty = true;
                    }
                }, new Coord(0, y));
                y += 25;
                add(new CheckBox("Render shadows") {
                    {
                        a = cf.lshadow.val;
                    }

                    public void set(boolean val) {
                        if (val) {
                            try {
                                cf.lshadow.set(true);
                            } catch (GLSettings.SettingException e) {
                                getparent(GameUI.class).error(e.getMessage());
                                return;
                            }
                        } else {
                            cf.lshadow.set(false);
                        }
                        a = val;
                        cf.dirty = true;
                    }
                }, new Coord(0, y));
                y += 25;
                add(new CheckBox("Antialiasing") {
                    {
                        a = cf.fsaa.val;
                    }

                    public void set(boolean val) {
                        try {
                            cf.fsaa.set(val);
                        } catch (GLSettings.SettingException e) {
                            getparent(GameUI.class).error(e.getMessage());
                            return;
                        }
                        a = val;
                        cf.dirty = true;
                    }
                }, new Coord(0, y));
                y += 25;
                add(new Label("Anisotropic filtering"), new Coord(0, y));
                if (cf.anisotex.max() <= 1) {
                    add(new Label("(Not supported)"), new Coord(15, y + 15));
                } else {
                    final Label dpy = add(new Label(""), new Coord(165, y + 15));
                    add(new HSlider(160, (int) (cf.anisotex.min() * 2), (int) (cf.anisotex.max() * 2), (int) (cf.anisotex.val * 2)) {
                        protected void added() {
                            dpy();
                            this.c.y = dpy.c.y + ((dpy.sz.y - this.sz.y) / 2);
                        }

                        void dpy() {
                            if (val < 2)
                                dpy.settext("Off");
                            else
                                dpy.settext(String.format("%.1f\u00d7", (val / 2.0)));
                        }

                        public void changed() {
                            try {
                                cf.anisotex.set(val / 2.0f);
                            } catch (GLSettings.SettingException e) {
                                getparent(GameUI.class).error(e.getMessage());
                                return;
                            }
                            dpy();
                            cf.dirty = true;
                        }
                    }, new Coord(0, y + 15));
                }
                y += 35;
                add(new Button(200, "Reset to defaults") {
                    public void click() {
                        cf.cfg.resetprefs();
                        curcf.destroy();
                        curcf = null;
                    }
                }, new Coord(100, 180));
                pack();
            }
        }

        private CPanel curcf = null;

        public void draw(GOut g) {
            if ((curcf == null) || (g.gc.pref != curcf.cf)) {
                if (curcf != null)
                    curcf.destroy();
                curcf = add(new CPanel(g.gc.pref), Coord.z);
            }
            super.draw(g);
        }
    }

    public OptWnd(boolean gopts) {
        super(new Coord(410, 250), "Options", true);
        main = add(new Panel());
        video = add(new VideoPanel(main));
        audio = add(new Panel());
        display = add(new Panel());
        map = add(new Panel());
        general = add(new Panel());

        int y;

        main.add(new PButton(200, "Video settings", 'v', video), new Coord(0, 0));
        main.add(new PButton(200, "Audio settings", 'a', audio), new Coord(0, 30));
        main.add(new PButton(200, "Display settings", 'd', display), new Coord(0, 60));
        main.add(new PButton(200, "Map settings", 'm', map), new Coord(0, 90));
        main.add(new PButton(200, "General settings", 'g', general), new Coord(210, 0));

        if (gopts) {
            main.add(new Button(200, "Switch character") {
                public void click() {
                    getparent(GameUI.class).act("lo", "cs");
                }
            }, new Coord(100, 160));
            main.add(new Button(200, "Log out") {
                public void click() {
                    getparent(GameUI.class).act("lo");
                }
            }, new Coord(100, 190));
        }
        main.add(new Button(200, "Close") {
            public void click() {
                OptWnd.this.hide();
            }
        }, new Coord(100, 220));
        main.pack();

        // -------------------------------------------- audio
        y = 0;
        audio.add(new Label("Master audio volume"), new Coord(0, y));
        y += 15;
        audio.add(new HSlider(200, 0, 1000, (int) (Audio.volume * 1000)) {
            public void changed() {
                Audio.setvolume(val / 1000.0);
            }
        }, new Coord(0, y));
        y += 30;
        audio.add(new Label("In-game event volume"), new Coord(0, y));
        y += 15;
        audio.add(new HSlider(200, 0, 1000, 0) {
            protected void attach(UI ui) {
                super.attach(ui);
                val = (int) (ui.audio.pos.volume * 1000);
            }

            public void changed() {
                ui.audio.pos.setvolume(val / 1000.0);
            }
        }, new Coord(0, y));
        y += 20;
        audio.add(new Label("Ambient volume"), new Coord(0, y));
        y += 15;
        audio.add(new HSlider(200, 0, 1000, 0) {
            protected void attach(UI ui) {
                super.attach(ui);
                val = (int) (ui.audio.amb.volume * 1000);
            }

            public void changed() {
                ui.audio.amb.setvolume(val / 1000.0);
            }
        }, new Coord(0, y));
        y += 35;
        audio.add(new CheckBox("Alarm on unknown players") {
            {
                a = Config.alarmunknow;
            }

            public void set(boolean val) {
                Utils.setprefb("alarmunknown", val);
                Config.alarmunknow = val;
                a = val;
            }
        }, new Coord(0, y));
        audio.add(new HSlider(100, 0, 1000, 0) {
            protected void attach(UI ui) {
                super.attach(ui);
                val = (int)(Config.alarmunknowvol * 1000);
            }

            public void changed() {
                double vol = val / 1000.0;
                Config.alarmunknowvol = vol;
                Utils.setprefd("alarmunknowvol", vol);
            }
        }, new Coord(170, y));
        y += 35;
        audio.add(new CheckBox("Alarm on red players") {
            {
                a = Config.alarmred;
            }

            public void set(boolean val) {
                Utils.setprefb("alarmred", val);
                Config.alarmred = val;
                a = val;
            }
        }, new Coord(0, y));
        audio.add(new HSlider(100, 0, 1000, 0) {
            protected void attach(UI ui) {
                super.attach(ui);
                val = (int)(Config.alarmredvol * 1000);
            }

            public void changed() {
                double vol = val / 1000.0;
                Config.alarmredvol = vol;
                Utils.setprefd("alarmredvol", vol);
            }
        }, new Coord(170, y));

        audio.add(new PButton(200, "Back", 27, main), new Coord(100, 220));
        audio.pack();

        // -------------------------------------------- display
        y = 0;
        display.add(new CheckBox("Show flavour objects") {
            {
                a = Config.showflo;
            }

            public void set(boolean val) {
                Utils.setprefb("showflo", val);
                Config.showflo = val;
                a = val;
            }
        }, new Coord(0, y));
        y += 35;
        display.add(new CheckBox("Daylight mode") {
            {
                a =  Config.daylight;
            }

            public void set(boolean val) {
                Utils.setprefb("daylight", val);
                Config.daylight = val;
                a = val;
            }
        }, new Coord(0, y));
        y += 35;
        display.add(new CheckBox("Display kin names") {
            {
                a = Config.showkinnames;
            }

            public void set(boolean val) {
                Utils.setprefb("showkinnames", val);
                Config.showkinnames = val;
                a = val;
            }
        }, new Coord(0, y));
        y += 35;
        display.add(new CheckBox("Free camera rotation") {
            {
                a = Config.camfree;
            }

            public void set(boolean val) {
                Utils.setprefb("camfree", val);
                Config.camfree = val;
                a = val;
            }
        }, new Coord(0, y));
        y += 35;
        display.add(new CheckBox("Show item quality") {
            {
                a = Config.showquality;
            }

            public void set(boolean val) {
                Utils.setprefb("showquality", val);
                Config.showquality = val;
                a = val;
            }
        }, new Coord(0, y));

        display.add(new PButton(200, "Back", 27, main), new Coord(100, 220));
        display.pack();

        // -------------------------------------------- map
        y = 0;
        map.add(new CheckBox("Show players on minimap") {
            {
                a = Utils.getprefb("showplayersmmap", false);
            }

            public void set(boolean val) {
                Utils.setprefb("showplayersmmap", val);
                Config.showplayersmmap = val;
                a = val;
            }
        }, new Coord(0, y));
        y += 35;
        map.add(new CheckBox("Save map tiles to disk") {
            {
                a = Utils.getprefb("savemmap", true);
            }

            public void set(boolean val) {
                Utils.setprefb("savemmap", val);
                Config.savemmap = val;
                a = val;
            }
        }, new Coord(0, y));

        map.add(new PButton(200, "Back", 27, main), new Coord(100, 220));
        map.pack();

        // -------------------------------------------- general
        y = 0;
        general.add(new CheckBox("Save chat logs to disk") {
            {
                a = Config.chatsave;
            }

            public void set(boolean val) {
                Utils.setprefb("chatsave", val);
                Config.chatsave = val;
                a = val;
                if (!val && Config.chatlog != null) {
                    try {
                        Config.chatlog.close();
                        Config.chatlog = null;
                    } catch (Exception e) {
                    }
                }
            }
        }, new Coord(0, y));
        y += 35;
        general.add(new CheckBox("Show timestamps in chats") {
            {
                a = Config.chattimestamp;
            }

            public void set(boolean val) {
                Utils.setprefb("chattimestamp", val);
                Config.chattimestamp = val;
                a = val;
            }
        }, new Coord(0, y));

        general.add(new PButton(200, "Back", 27, main), new Coord(100, 220));
        general.pack();


        chpanel(main);
    }

    public OptWnd() {
        this(true);
    }

    public void wdgmsg(Widget sender, String msg, Object... args) {
        if ((sender == this) && (msg == "close")) {
            hide();
        } else {
            super.wdgmsg(sender, msg, args);
        }
    }

    public void show() {
        chpanel(main);
        super.show();
    }
}