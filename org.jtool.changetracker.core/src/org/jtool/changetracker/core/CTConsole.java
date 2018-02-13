/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.core;

import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * Displays a dialog with a message.
 * @author Katsuhisa Maruyama
 */
public class CTConsole {
    
    /**
     * The name of the dedicated console.
     */
    private static final String CONSOLE_NAME = "ChangeTracker Console";
    
    /**
     * The stream of the dedicated console.
     */
    private static MessageConsoleStream consoleStream = getConsoleStream();
    
    /**
     * Shows the console and creates its stream.
     */
    private static MessageConsoleStream getConsoleStream() {
        ConsolePlugin consolePlugin = ConsolePlugin.getDefault();
        if (consolePlugin == null) {
            return null;
        }
        IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
        IConsole[] consoles = consoleManager.getConsoles();
        
        MessageConsole console = null;
        for (int i = 0; i < consoles.length; i++) {
            if (CONSOLE_NAME.equals(consoles[i].getName())) {
                console = (MessageConsole)consoles[i];
            }
        }
        if (console == null) {
            console = new MessageConsole(CONSOLE_NAME, null);
        }
        
        consoleManager.addConsoles(new MessageConsole[] { console });
        consoleManager.showConsoleView(console);
        return console.newMessageStream();
    }
    
    /**
     * Displays macros on the dedicated console.
     * @param msg the message to be displayed
     */
    public static void print(String msg) {
        if (consoleStream != null) {
            consoleStream.print(msg);
        } else {
            System.err.println(msg);
        }
    }
    
    /**
     * Displays macros on the dedicated console per line.
     * @param msg the message to be displayed
     */
    public static void println(String msg) {
        if (consoleStream != null) {
            consoleStream.println(msg);
        } else {
            System.err.print(msg);
        }
    }
}
