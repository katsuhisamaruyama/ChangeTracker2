/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.core;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Displays a dialog with a message.
 * @author Katsuhisa Maruyama
 */
public class ChangeTrackerDialog {
    
    /**
     * Displays an information dialog presenting the specified message.
     * @param title the title of the dialog, or <code>null</code> if none
     * @param msg the message to be presented
     */
    public static void informationDialog(String title, String msg) {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        MessageDialog.openInformation(window.getShell(), title, msg);
    }
    
    /**
     * Displays an error dialog presenting the specified message.
     * @param title the title of the dialog, or <code>null</code> if none
     * @param msg the message to be presented
     */
    public static void errorDialog(String title, String msg) {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        MessageDialog.openError(window.getShell(), title, msg);
    }
    
    /**
     * Displays a yes/no question dialog presenting the specified message.
     * @param title the title of the dialog, or <code>null</code> if none
     * @param msg the message to be presented
     * @return <code>true</code> if yes button was pushed, otherwise <code>false</code>
     */
    public static boolean yesnoDialog(String title, String msg) {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        return MessageDialog.openQuestion(window.getShell(), title, msg);
    }
    
    /**
     * Obtains a progress monitor dialog for the workbench window.
     * @return the progress monitor dialog
     */
    public static ProgressMonitorDialog getProgressMonitorDialog() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        return new ProgressMonitorDialog(window.getShell());
    }
}
