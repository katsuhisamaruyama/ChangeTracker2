/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.core;

import org.jtool.changetracker.xml.XmlFileManager;
import org.jtool.changetracker.convert.XmlChecker;
import org.jtool.changetracker.convert.XmlConverter;
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
     * Creates an object for a preference page.
     */
    public CTPreferencePage() {
        super(GRID);
        IPreferenceStore store = Activator.getPlugin().getPreferenceStore();
        setPreferenceStore(store);
        if (getLocation().length() == 0) {
            setLocation(getDefaultLoaction());
        }
    }
    
    /**
     * Creates the field editors for preference settings. 
     */
    @Override
    public void createFieldEditors() {
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
        layout.numColumns = 3;
        area.setLayout(layout);
        
        ZipArchiveExporter exporter = new ZipArchiveExporter();
        exporter.createAction(area);
        
        XmlChecker checker = new XmlChecker();
        checker.createAction(area);
        
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
        return getLocation(store.getString(REPOSITORY_LOCATION));
    }
    
    /**
     * Sets the location of a directory that contains operation history files.
     * @param location the location of the directory
     */
    public static void setLocation(String location) {
        IPreferenceStore store = Activator.getPlugin().getPreferenceStore();
        store.setValue(REPOSITORY_LOCATION, getPath(location));
    }
    
    /**
     * Returns the absolute location of a directory that contains history files.
     * @param the relative path of the directory
     * @return the path of the directory location
     */
    public static String getLocation(String path) {
        IPath workspaceDir = ResourcesPlugin.getWorkspace().getRoot().getLocation();
        String location;
        if (path.startsWith(WORKSPACE_PATH_PREFIX)) {
            String postfix = path.substring(WORKSPACE_PATH_PREFIX.length());
            location = workspaceDir.append(postfix).toOSString();
        } else {
            location = new Path(path).toOSString();
        }
        return location;
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
     * Returns the path of a directory that contains history files.
     * @return the directory path, or <code>null</code> if the directory does not exist
     */
    public static String getPath(String location) {
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
     * Returns the path of the default directory that contains history files.
     * @return the default directory path.
     */
    public static String getDefaultPath() {
        return getPath(getDefaultLoaction());
    }
    
    /**
     * Returns the absolute location of the default directory that contains history files.
     * @return the default directory location.
     */
    public static String getDefaultLoaction() {
        String location = getLocation(WORKSPACE_PATH_PREFIX + File.separatorChar + DEFAULT_DIRECTORY_PATH);
        
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
