package net.minecraft.client.gui;

import net.minecraft.client.resources.I18n;
import net.lax1dude.eaglercraft.v1_8.EagRuntime;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GuiModMenu extends GuiScreen {
    private final GuiScreen parentScreen;
    private final List<ModEntry> allMods = new ArrayList<>();
    private final List<ModEntry> filteredMods = new ArrayList<>();
    private int listScroll = 0;
    private int selectedIndex = -1;
    private GuiTextField searchField;
    private boolean sortAsc = true;

    public GuiModMenu(GuiScreen parent) {
        this.parentScreen = parent;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        int cy = this.height - 28;
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, cy, I18n.format("gui.done", new Object[0])));
        this.buttonList.add(new GuiButton(20, 10, cy, 140, 20, "Open Mods Folder"));
        this.buttonList.add(new GuiButton(21, 10, cy - 24, 140, 20, "Config"));
        // top controls
        this.buttonList.add(new GuiButton(10, 10, 10, 40, 20, "Off"));
        this.buttonList.add(new GuiButton(11, 54, 10, 50, 20, "A-Z"));
        this.buttonList.add(new GuiButton(12, 108, 10, 50, 20, "Z-A"));

        this.searchField = new GuiTextField(0, this.fontRendererObj, 10, this.height - 56, 140, 20);
        this.searchField.setText("");

        loadMods();
        applyFilterAndSort();
    }

    private void loadMods() {
        allMods.clear();
        try {
            File modsDir = new File("mods");
            if (modsDir.exists() && modsDir.isDirectory()) {
                File[] files = modsDir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        String name = f.getName();
                        if (name.endsWith(".jar") || name.endsWith(".zip")) {
                            String nm = name.replaceAll("\\\\.jar$|\\\\.zip$", "");
                            ModEntry e = new ModEntry(nm, "", "", "", "", true);
                            allMods.add(e);
                        }
                    }
                }
            }
        } catch (Throwable t) {
        }
        if (allMods.isEmpty()) {
            // placeholder entries so UI resembles Forge menu when nothing installed
            allMods.add(new ModEntry("Legacy Fabric", "0.18.4", "API for Fabric modifications to work with Minecraft pre-1.14", "Hydos, sheDaniel, Ramidzkh, and BoogieMonster101", "https://legacyfabric.net/", true));
            allMods.add(new ModEntry("Minecraft", "1.13", "Vanilla", "Mojang", "https://minecraft.net/", true));
        }
    }

    private void applyFilterAndSort() {
        filteredMods.clear();
        String q = this.searchField != null ? this.searchField.getText().toLowerCase().trim() : "";
        for (ModEntry e : allMods) {
            if (q.isEmpty() || e.name.toLowerCase().contains(q)) {
                filteredMods.add(e);
            }
        }
        Collections.sort(filteredMods, new Comparator<ModEntry>() {
            @Override
            public int compare(ModEntry o1, ModEntry o2) {
                return sortAsc ? o1.name.compareToIgnoreCase(o2.name) : o2.name.compareToIgnoreCase(o1.name);
            }
        });
        if (selectedIndex >= filteredMods.size()) selectedIndex = filteredMods.size() - 1;
        if (selectedIndex < 0 && !filteredMods.isEmpty()) selectedIndex = 0;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            this.mc.displayGuiScreen(this.parentScreen);
        } else if (button.id == 20) {
            try {
                File mods = new File("mods");
                String path = mods.getAbsolutePath();
                EagRuntime.openLink("file://" + path.replaceAll("\\\\", "/"));
            } catch (Throwable t) {
            }
        } else if (button.id == 21) {
            // Config — open placeholder or no-op unless selected
            if (selectedIndex >= 0 && selectedIndex < filteredMods.size()) {
                // no real config UI available; just open mod homepage if present
                ModEntry me = filteredMods.get(selectedIndex);
                if (me.homepage != null && me.homepage.length() > 0) EagRuntime.openLink(me.homepage);
            }
        } else if (button.id == 11) {
            sortAsc = true; applyFilterAndSort();
        } else if (button.id == 12) {
            sortAsc = false; applyFilterAndSort();
        } else if (button.id == 10) {
            // toggle showing only enabled mods
            List<ModEntry> copy = new ArrayList<>(allMods);
            allMods.clear();
            for (ModEntry e : copy) {
                if (e.enabled) allMods.add(e);
            }
            applyFilterAndSort();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, "Mods", this.width / 2, 16, 16777215);

        int listX = 10;
        int listY = 40;
        int listW = 140;
        int listH = this.height - 100;

        drawRect(listX - 2, listY - 2, listX + listW + 2, listY + listH + 2, 0x88000000);

        int entryH = 24;
        int visible = listH / entryH;
        int maxScroll = Math.max(0, filteredMods.size() - visible);
        if (listScroll < 0) listScroll = 0;
        if (listScroll > maxScroll) listScroll = maxScroll;

        for (int i = 0; i < visible; ++i) {
            int idx = i + listScroll;
            int y = listY + i * entryH;
            if (idx >= filteredMods.size()) break;
            ModEntry me = filteredMods.get(idx);
            if (idx == selectedIndex) drawRect(listX, y, listX + listW, y + entryH - 2, 0xFF555555);
            this.drawString(this.fontRendererObj, me.name, listX + 4, y + 6, 0xFFFFFF);
            if (me.version != null && me.version.length() > 0) this.drawString(this.fontRendererObj, me.version, listX + 4, y + 14, 0xAAAAAA);
        }

        // scrollbar - Forge-like appearance (track + handle with border/highlight)
        int sbX = listX + listW + 2;
        int sbY = listY;
        int sbH = listH;
        // draw track background
        drawRect(sbX - 2, sbY - 1, sbX + 10, sbY + sbH + 1, 0xFF2A2A2A);
        drawRect(sbX - 1, sbY, sbX + 9, sbY + sbH, 0xFF1F1F1F);
        if (filteredMods.size() > 0) {
            int handleH = Math.max(10, (int)((float)sbH * ((float)visible / (float)filteredMods.size())));
            int handleY;
            if (maxScroll <= 0) {
                handleY = sbY;
            } else {
                handleY = sbY + (int)(((float)listScroll / (float)maxScroll) * (sbH - handleH));
            }
            // handle body
            drawRect(sbX - 1, handleY, sbX + 9, handleY + handleH, 0xFF8F8F8F);
            // inner highlight to give 3D look
            drawRect(sbX, handleY + 1, sbX + 8, handleY + handleH - 1, 0xFFDCDCDC);
            // top and bottom border lines
            drawRect(sbX - 1, handleY - 1, sbX + 9, handleY, 0xFF4A4A4A);
            drawRect(sbX - 1, handleY + handleH, sbX + 9, handleY + handleH + 1, 0xFF4A4A4A);
        }

        // right pane
        int paneX = listX + listW + 24;
        int paneW = this.width - paneX - 10;
        int paneY = listY;
        drawRect(paneX - 2, paneY - 2, paneX + paneW + 2, paneY + listH + 2, 0x88000000);
        if (selectedIndex >= 0 && selectedIndex < filteredMods.size()) {
            ModEntry me = filteredMods.get(selectedIndex);
            int tx = paneX + 6;
            int ty = paneY + 6;
            this.drawString(this.fontRendererObj, me.name, tx, ty, 0xFFFFFF);
            ty += 12;
            if (me.version != null && me.version.length() > 0) {
                this.drawString(this.fontRendererObj, "Version: " + me.version, tx, ty, 0xFFFFFF);
                ty += 12;
            }
            if (me.authors != null && me.authors.length() > 0) {
                this.drawString(this.fontRendererObj, "Authors: " + me.authors, tx, ty, 0xFFFFFF);
                ty += 12;
            }
            if (me.homepage != null && me.homepage.length() > 0) {
                this.drawString(this.fontRendererObj, "Homepage: " + me.homepage, tx, ty, 0x3366FF);
                ty += 12;
            }
            ty += 8;
            if (me.description != null && me.description.length() > 0) {
                String[] lines = wrapText(me.description, paneW - 12);
                for (String l : lines) {
                    this.drawString(this.fontRendererObj, l, tx, ty, 0xFFFFFF);
                    ty += 10;
                }
            }
        }

        this.searchField.drawTextBox();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private String[] wrapText(String s, int maxWidth) {
        List<String> out = new ArrayList<>();
        String[] words = s.split(" ");
        StringBuilder cur = new StringBuilder();
        for (String w : words) {
            String test = cur.length() == 0 ? w : cur + " " + w;
            if (this.fontRendererObj.getStringWidth(test) > maxWidth) {
                out.add(cur.toString());
                cur = new StringBuilder(w);
            } else {
                if (cur.length() > 0) cur.append(' ');
                cur.append(w);
            }
        }
        if (cur.length() > 0) out.add(cur.toString());
        return out.toArray(new String[0]);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.searchField.mouseClicked(mouseX, mouseY, mouseButton);
        int listX = 10;
        int listY = 40;
        int listW = 140;
        int listH = this.height - 100;
        if (mouseX >= listX && mouseX <= listX + listW && mouseY >= listY && mouseY <= listY + listH) {
            int entryH = 24;
            int rel = (mouseY - listY) / entryH;
            int idx = listScroll + rel;
            if (idx >= 0 && idx < filteredMods.size()) {
                selectedIndex = idx;
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (this.searchField.textboxKeyTyped(typedChar, keyCode)) {
            applyFilterAndSort();
            return;
        }
        if (keyCode == 200) { // up
            if (selectedIndex > 0) selectedIndex--; if (selectedIndex < listScroll) listScroll--;
        } else if (keyCode == 208) { // down
            if (selectedIndex < filteredMods.size() - 1) selectedIndex++; int visible = (this.height - 100) / 24; if (selectedIndex >= listScroll + visible) listScroll++;
        } else super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    // mouseMovedOrUp is not present in this version's GuiScreen; no-op kept intentionally.

    private static class ModEntry {
        String name;
        String version;
        String description;
        String authors;
        String homepage;
        boolean enabled;

        ModEntry(String n, String v, String d, String a, String h, boolean e) {
            this.name = n; this.version = v; this.description = d; this.authors = a; this.homepage = h; this.enabled = e;
        }
    }
}
