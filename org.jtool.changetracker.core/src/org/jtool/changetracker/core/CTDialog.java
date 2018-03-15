/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.core;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import java.lang.reflect.InvocationTargetException;


/**
 * Displays a dialog with a message.
 * @author Katsuhisa Maruyama
 */
public class CTDialog {
    
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
     * Displays a progress monitor dialog for the workbench window.
     * @param runnable a runnable task
     */
    public static void getProgressMonitorDialog(IRunnableWithProgress runnable) {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        ProgressMonitorDialog dialog = new ProgressMonitorDialog(window.getShell());
        try {
            dialog.run(true, true, runnable);
        } catch (InvocationTargetException e) {
            CTDialog.errorDialog("InvocationTargetException", e.getCause().getMessage());
        } catch (InterruptedException e) {
            CTDialog.informationDialog("InterruptedException", e.getCause().getMessage());
        }
    }
}
