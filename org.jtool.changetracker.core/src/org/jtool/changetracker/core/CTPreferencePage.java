/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.core;

import org.jtool.changetracker.repository.RepositoryManager;
import org.jtool.changetracker.xml.XmlFileManager;
import org.jtool.changetracker.xml.XmlConverter;
import org.jtool.changetracker.xml.ZipArchiveExporter;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import java.io.File;

/**
 * Manages the preference page.
 * @author Katsuhisa Maruyama
 */
public class CTPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
    
    /**
     * The location of a directory that stores the history files of change operations.
     */
    static final String REPOSITORY_LOCATION = "repository.location";
    
    /**
     * The prefix word that indicates the workspace directory.
     */
    static String WORKSPACE_PATH_PREFIX = "${eclipse.workspace}";
    
    /**
     * The default directory that stores operation history files.
     */
    static String DEFAULT_DIRECTORY_PATH = "#history";
    
    /**
     * The field editor that specifies location of a repository. 
     */
    private CTDirectoryFieldEditor fieldEditor;
    
    /**
     * Creates an object for a preference page.
     */
    public CTPreferencePage() {
        super(GRID);
        IPreferenceStore store = Activator.getPlugin().getPreferenceStore();
        setPreferenceStore(store);
        if (getLocation().length() == 0) {
            store.setValue(REPOSITORY_LOCATION, getDefaultPath());
        }
    }
    
    /**
     * Creates the field editors for preference settings. 
     */
    @Override
    public void createFieldEditors() {
        fieldEditor = new CTDirectoryFieldEditor(REPOSITORY_LOCATION,
                "Repository: ", getFieldEditorParent()) {
            
            /**
             * Stores the preference value from this field editor into the preference store.
             */
            @Override
            protected void doStore() {
                if (!getLocation().equals(fieldEditor.getStringValue())) {
                    String location = CTPreferencePage.getLocation(fieldEditor.getStringValue());
                    if (location != null) {
                        super.doStore();
                        RepositoryManager.getInstance().changeMainRepository(location);
                    }
                }
            }
        };
        fieldEditor.setFilterPath(new File(getLocation()));
        fieldEditor.setEmptyStringAllowed(false);
        addField(fieldEditor);
    }
    
    /**
     * Initializes a preference page for a given workbench.
     */
    @Override
    public void init(IWorkbench workbench) {
    }
    
    /**
     * Creates the control for the customized body of this preference page.
     * @param parent the parent composite
     * @return the new control
     */
    @Override
    protected Control createContents(Composite parent) {
        super.createContents(parent);
        Composite area = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        area.setLayout(layout);
        
        ZipArchiveExporter exporter = new ZipArchiveExporter();
        exporter.createAction(area);
        XmlConverter converter = new XmlConverter();
        converter.createAction(area);
        return area;
    }
    
    /**
     * Returns the location of a directory that contains operation history files.
     * @return the location of the directory
     */
    public static String getLocation() {
        IPreferenceStore store = Activator.getPlugin().getPreferenceStore();
        return store.getString(REPOSITORY_LOCATION);
    }
    
    /**
     * Returns the path of a directory that contains history files.
     * @return the directory path, or <code>null</code> if the directory does not exist
     */
    static String getPath(String location) {
        if (!checkLocation(location)) {
            return null;
        }
        
        IPath workspaceDir = ResourcesPlugin.getWorkspace().getRoot().getLocation();
        String workspacePath = workspaceDir.toOSString();
        if (location.startsWith(workspacePath)) {
            String postfix = location.substring(workspacePath.length());
            return WORKSPACE_PATH_PREFIX + postfix;
        }
        return location;
    }
    
    /**
     * Returns the absolute location of a directory that contains history files.
     * @param the relative path of the directory
     * @return the directory location, or <code>null</code> if the directory does not exist
     */
    static String getLocation(String path) {
        IPath workspaceDir = ResourcesPlugin.getWorkspace().getRoot().getLocation();
        String location;
        if (path.startsWith(WORKSPACE_PATH_PREFIX)) {
            String postfix = path.substring(WORKSPACE_PATH_PREFIX.length());
            location = workspaceDir.append(postfix).toOSString();
        } else {
            location = new Path(path).toOSString();
        }
        
        if (checkLocation(location)) {
            return location;
        }
        return null;
    }
    
    /**
     * Tests if a directory exists
     * @param location the location of the directory
     * @return <code>true</code> if the directory exists, otherwise <code>false</code>
     */
    private static boolean checkLocation(String location) {
        File dir = new File(location);
        return dir.isDirectory();
    }
    
    /**
     * Returns the path of the default directory that contains history files.
     * @return the default directory path.
     */
    static String getDefaultPath() {
        return getPath(getDefaultLoaction());
    }
    
    /**
     * Returns the absolute location of the default directory that contains history files.
     * @return the default directory location.
     */
    static String getDefaultLoaction() {
        String location = getLocation(WORKSPACE_PATH_PREFIX + File.separator + DEFAULT_DIRECTORY_PATH);
        
        File file = new File(location);
        if (!file.exists()) {
            XmlFileManager.makeDir(new File(location));
            return location;
        }
        
        if (file.isFile()) {
            while (file.isFile()) {
                location = location + "#";
                file = new File(location);
            }
        }
        
        return location;
    }
}
