/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.core;

import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import java.io.File;

/**
 * A field editor for specifying the path of a directory.
 */
public class CTDirectoryFieldEditor extends StringButtonFieldEditor {
    
    /**
     * The initial path for the directory chooser dialog.
     */
    private File filterPath = null;
    
    /**
     * Creates a directory field editor.
     * @param name the name of the preference this field editor works on
     * @param labelText the label text of the field editor
     * @param parent the parent of the field editor's control
     */
    public CTDirectoryFieldEditor(String name, String labelText, Composite parent) {
        init(name, labelText);
        setErrorMessage(JFaceResources.getString("DirectoryFieldEditor.errorMessage"));
        setChangeButtonText(JFaceResources.getString("openBrowse"));
        setValidateStrategy(VALIDATE_ON_KEY_STROKE);
        createControl(parent);
    }
    
    /**
     * Selects a directory.
     * @return the path of the selected directory
     */
    @Override
    protected String changePressed() {
        String location = CTPreferencePage.getLocation(getTextControl().getText());
        File file = new File(location);
        if (!file.exists()) {
            file = null;
        }
        File dir = getDirectory(file);
        if (dir == null) {
            return null;
        }
        return CTPreferencePage.getPath(dir.getAbsolutePath());
    }
    
    /**
     * Tests if the text input field indicates a valid directory.
     * @return <code>true</code> if the text input field indicates a valid directory, otherwise <code>false</code>
     */
    @Override
    protected boolean doCheckState() {
        String path = getTextControl().getText();
        return CTPreferencePage.getLocation(path.trim()) != null;
    }
    
    /**
     * Sets the initial path for the Browse dialog.
     * @param path initial path for the Browse dialog
     * @since 3.6
     */
    public void setFilterPath(File path) {
        filterPath = path;
    }
    
    /**
     * Opens the directory chooser dialog.
     * @param file the directory the dialog will open in.
     * @return the directory, or <code>null</code> if the selected directory is invalid
     */
    private File getDirectory(File file) {
        DirectoryDialog dirDialog = new DirectoryDialog(getShell(), SWT.OPEN | SWT.SHEET);
        if (file != null) {
            dirDialog.setFilterPath(file.getPath());
        } else if (filterPath != null) {
            dirDialog.setFilterPath(filterPath.getPath());
        }
        
        String dir = dirDialog.open();
        if (dir != null) {
            dir = dir.trim();
            if (dir.length() > 0) {
                return new File(dir);
            }
        }
        return null;
    }
}
