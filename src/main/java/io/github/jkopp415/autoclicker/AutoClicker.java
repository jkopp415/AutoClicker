package io.github.jkopp415.autoclicker;

import com.melloware.jintellitype.JIntellitype;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class AutoClicker {

    private static TrayIcon trayIcon;
    private static Timer timer;
    private static Robot robot;
    private static boolean isClickerActive = false;

    public static void main(String[] args) {
        // Check if system tray is supported
        if (!SystemTray.isSupported()) {
            System.err.println("System tray is not supported.");
        }

        // Create system tray object
        SystemTray tray = SystemTray.getSystemTray();

        // Get the icon asset and set it as the tray icon
        Image clickerIcon;
        try {
            URL clickerIconURL = AutoClicker.class.getClassLoader().getResource("images/clicker-icon.png");
            assert clickerIconURL != null;
            clickerIcon = ImageIO.read(clickerIconURL);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("The image was not found it seems.");
            return;
        }
        trayIcon = new TrayIcon(clickerIcon, "AutoClicker");

        // Initialize popup menu and exit button
        PopupMenu popupMenu = new PopupMenu();
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> {
            systemMessage("Shutting down...", "Goodbye!");
            System.exit(0);
        });
        popupMenu.add(exitItem);
        trayIcon.setPopupMenu(popupMenu);

        // Add the process to the tray
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        }

        // Send a welcome message as a system tray notification
        systemMessage("AutoClicker active!", "Press Shift + Ctrl + Alt + Z to begin");

        // Register Shift + Ctrl + Alt + Z as the start keybind
        JIntellitype.getInstance().registerHotKey(
                1,
                JIntellitype.MOD_SHIFT + JIntellitype.MOD_CONTROL + JIntellitype.MOD_ALT,
                'Z'
        );

        // Register Shift + Ctrl + Alt + X as the stop keybind
        JIntellitype.getInstance().registerHotKey(
                2,
                JIntellitype.MOD_SHIFT + JIntellitype.MOD_CONTROL + JIntellitype.MOD_ALT,
                'X'
        );

        // Register Shift + Ctrl + Alt + C as the emergency quit keybind
        JIntellitype.getInstance().registerHotKey(
                3,
                JIntellitype.MOD_SHIFT + JIntellitype.MOD_CONTROL + JIntellitype.MOD_ALT,
                'C'
        );

        // Keybind listeners
        JIntellitype.getInstance().addHotKeyListener(i -> {
            // Execute this code if the start keybind is pressed
            if (i == 1 && !isClickerActive) {
                isClickerActive = true;
                systemMessage("AutoClicker starting...", "Press Shift + Ctrl + Alt + X to stop it. " +
                        "Press Shift + Ctrl + Alt + C to emergency shutdown.");
                timer = new Timer();
                timer.scheduleAtFixedRate(new TimerTask() {
                        // This code will be run repeatedly
                        @Override
                        public void run() {
                            click();
                        }
                    },
                    0,  // The process will begin after 0 ms
                    10  // The code will run every 10 ms
                );
            }
            // Execute this code if the stop keybind is pressed
            else if (i == 2 && isClickerActive) {
                isClickerActive = false;
                systemMessage("AutoClicker stopping...", "Activate it again with Shift + Ctrl + Alt + Z");
                timer.cancel();
            }

            // Execute this code if an emergency shutdown needs to be performed
            else if (i == 3) {
                systemMessage("Emergency shutdown commencing...", "Goodbye!");
                System.exit(0);
            }
        });

        // Initialize the mouse robot
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

    private static void systemMessage(String caption, String text) {
        trayIcon.displayMessage(caption, text, TrayIcon.MessageType.INFO);
    }

    private static void click() {
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }
}
