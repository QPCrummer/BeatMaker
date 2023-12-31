package com.github.qpcrummer.beatmaker.gui;

import com.github.qpcrummer.beatmaker.audio.MusicPlayer;
import com.github.qpcrummer.beatmaker.data.Data;
import com.github.qpcrummer.beatmaker.processing.BeatFile;
import com.github.qpcrummer.beatmaker.processing.BeatManager;
import com.github.qpcrummer.beatmaker.processing.Generator;
import imgui.ImGui;
import imgui.ImVec4;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;

import java.util.concurrent.ThreadLocalRandom;

public class MainGUI {
    private static float PANEL_WIDTH = 600; // Initial width of the left panel (should be about 70% of the whole window size)
    public static final float TOOLBAR_HEIGHT = 20; // Height of the toolbar
    public static boolean isPlayButtonPressed = false; // Flag to toggle Play/Pause button
    private static final float BOX_LENGTH = 50.0f; // Length of each box
    private static float maxPanelWidth; // Maximum width available for the Right Panel
    public static final float CHART_WIDTH = 150.0f;
    public static final ImString time = new ImString();

    static {
        time.set("0.000");
    }

    public static void render() {
        // Calculate panel widths
        PANEL_WIDTH = ImGui.getIO().getDisplaySize().x * 0.7f;

        ImGui.begin("Editor", ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoBackground);

        // Toolbar
        if (ImGui.beginMainMenuBar()) {
            // File Menu
            if (ImGui.beginMenu("File")) {
                if (ImGui.menuItem("Open WAV")) {
                    FileExplorer.setFileExplorerType(false);
                    FileExplorer.enabled = true;
                }
                if (ImGui.menuItem("Open Beat")) {
                    FileExplorer.setFileExplorerType(true);
                    FileExplorer.enabled = true;
                }
                if (ImGui.menuItem("Save All")) {
                    BeatFile.saveAll();
                }
                ImGui.endMenu();
            }

            // Edit Menu
            if (ImGui.beginMenu("Edit")) {
                if (ImGui.menuItem("Add Beat Guide")) {
                    Data.charts.add(new Chart(CHART_WIDTH, ThreadLocalRandom.current().nextInt(), true));
                }
                if (ImGui.menuItem("Remove Beat Guide")) {
                    BeatGuideInteractionGUI.removal = true;
                    BeatGuideInteractionGUI.enable = true;
                }
                if (ImGui.menuItem("Merge Beat Guides")) {
                    BeatGuideInteractionGUI.removal = false;
                    BeatGuideInteractionGUI.enable = true;
                }
                if (ImGui.menuItem("Channel Configuration")) {
                    ChannelInteractionGUI.enable = true;
                }
                ImGui.endMenu();
            }

            // Generate
            if (ImGui.beginMenu("Generate")) {
                if (ImGui.menuItem("Generate Percussion Beat Files")) {
                    Generator.generatePercussionChartsForSong();
                }
                if (ImGui.menuItem("Generate Complex Beat Files")) {
                    Generator.generateComplexChartsForSong();
                }
                if (ImGui.menuItem("Generate Beat Extractor Beat Files")) {
                    Generator.generateWithBeatExtractor();
                }
                if (ImGui.menuItem("Generate Onset Extractor Beat Files")) {
                    Generator.generateWithOnsetExtractor();
                }
                if (ImGui.menuItem("Generator Configuration")) {
                    BeatGenerationGUI.enable = true;
                }
                ImGui.endMenu();
            }

            // Effects

            ImGui.pushStyleColor(ImGuiCol.Button, ImGuiCol.MenuBarBg);
            if (ImGui.button("Effects")) {
                EffectSelectionGUI.primeGUI();
                ImGui.openPopup("Effect Selection");
            }
            ImGui.popStyleColor();

            EffectSelectionGUI.render();

            // Record Menu
            if (ImGui.menuItem("Record")) {
                Recorder.enable = true;
            }

            // Play Menu
            if (ImGui.menuItem(isPlayButtonPressed ? "Pause" : "Play")) {
                isPlayButtonPressed = !isPlayButtonPressed;

                if (isPlayButtonPressed) {
                    MusicPlayer.play();
                } else {
                    MusicPlayer.pause();
                }
            }

            // Rewind
            if (ImGui.menuItem("Rewind")) {
                MusicPlayer.setPosition(0);
                BeatManager.resetBeats();
                isPlayButtonPressed = false;
            }

            // Music Interval
            if (ImGui.checkbox("Interval", Data.doIntervals)) {
                Data.doIntervals = !Data.doIntervals;
            }

            ImGui.pushItemWidth(100);
            ImGui.inputDouble("##SongIntervalInput1", Data.timeIntervals[0], 0, 0, "%.3f");
            ImGui.inputDouble("##SongIntervalInput2", Data.timeIntervals[1], 0, 0, "%.3f");
            ImGui.popItemWidth();

            // Timer Text Box (on the far right)
            ImGui.sameLine(ImGui.getContentRegionMaxX() - ImGui.calcTextSize("000.000").x);
            ImGui.text(time.get());

            ImGui.endMainMenuBar();
        }

        // Adjust vertical position for panels
        float yOffset = TOOLBAR_HEIGHT;

        // Left Panel
        ImGui.setNextWindowSize(PANEL_WIDTH, ImGui.getIO().getDisplaySize().y - yOffset);
        ImGui.setNextWindowPos(0, yOffset);
        ImGui.begin("Left Panel", ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoScrollbar);

        // Temporarily modify padding to remove the gray box
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0, 0);

        ImGui.text("Beat Data");

        // Restore default padding
        ImGui.popStyleVar();

        // Render all charts
        float x = ImGui.getCursorPosX();
        float y = ImGui.getCursorPosY();
        ImGui.beginChild("Charts", PANEL_WIDTH, ImGui.getIO().getDisplaySize().y - 3 * yOffset, false,ImGuiWindowFlags.HorizontalScrollbar);
        for (Chart chart : Data.charts) {
            ImGui.setCursorPosY(y);
            ImGui.setCursorPosX(x);
            chart.render(x);
            x = x + CHART_WIDTH + 25;
        }
        ImGui.endChild();

        ImGui.end();

        // Right Panel
        maxPanelWidth = ImGui.getIO().getDisplaySize().x - (PANEL_WIDTH + 2 * BOX_LENGTH);
        if (maxPanelWidth > BOX_LENGTH) {
            ImGui.setNextWindowSize(ImGui.getIO().getDisplaySize().x - PANEL_WIDTH, ImGui.getIO().getDisplaySize().y - yOffset);
            ImGui.setNextWindowPos(PANEL_WIDTH, yOffset);
            ImGui.begin("Right Panel", ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoNavInputs | ImGuiWindowFlags.NoTitleBar);
            ImGui.text("Light Demos");

            // Calculate the maximum number of boxes per row based on the available width
            int maxBoxesPerRow = (int) (maxPanelWidth / BOX_LENGTH);

            // Calculate the number of columns
            int columns = Math.min(Data.totalChannels, maxBoxesPerRow);

            // Draw the boxes
            for (int i = 0; i < Data.totalChannels; i++) {
                if (i % columns != 0) {
                    ImGui.sameLine();
                }

                ImGui.pushID(i);
                boolean light = false;
                if (Data.blinkBooleans.get(i)) {
                    light = true;
                    ImGui.pushStyleColor(ImGuiCol.Button, 5, 232, 24, 255);
                }
                if (ImGui.button(String.valueOf(i), BOX_LENGTH, BOX_LENGTH)) {
                    Data.blinkBooleans.set(i, !Data.blinkBooleans.get(i));
                }
                if (light) {
                    ImGui.popStyleColor();
                }
                ImGui.popID();
            }

            ImGui.end();
        }

        // End main window
        ImGui.end();
    }
}
