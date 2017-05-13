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
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.DirectoryFieldEditor;
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
public class ChangeTrackerPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
    
    /**
     * The label for indicating the location of a directory that contains operation history files.
     */
    static final String REPOSITORY_LOCATION = "repository.location";
    
    /**
     * The default directory that stores operation history files.
     */
    static String DEFAULT_DIRECTORY_PATH = "#history";
    
    /**
     * The field editor that specifies location of a repository. 
     */
    private DirectoryFieldEditor fieldEditor;
    
    /**
     * Creates an object for a preference page.
     */
    public ChangeTrackerPreferencePage() {
        super(GRID);
        IPreferenceStore store = Activator.getPlugin().getPreferenceStore();
        setPreferenceStore(store);
    }
    
    /**
     * Creates the field editors for preference settings.
     */
    @Override
    public void createFieldEditors() {
        fieldEditor = new DirectoryFieldEditor(REPOSITORY_LOCATION, "Repository: ", getFieldEditorParent()) {
            
            /**
             * Stores the preference value from this field editor into
             * the preference store.
             */
            @Override
            protected void doStore() {
                File dir = new File(fieldEditor.getStringValue());
                if (dir.isDirectory()) {
                    super.doStore();
                    RepositoryManager.getInstance().changeMainRepository(fieldEditor.getStringValue());
                }
            }
        };
        fieldEditor.setFilterPath(new File(getDefaultLoaction()));
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
     * Returns the default location of a directory that contains operation history files.
     * @return the default location of the directory
     */
    public static String getLocation() {
        IPreferenceStore store = Activator.getPlugin().getPreferenceStore();
        return store.getString(REPOSITORY_LOCATION);
    }
    
    /**
     * Returns the default location of a directory that contains operation history files.
     * @return the default location of the directory
     */
    public static void setLocation(String loc) {
        IPreferenceStore store = Activator.getPlugin().getPreferenceStore();
        store.setValue(REPOSITORY_LOCATION, loc);
    }
    
    /**
     * Returns the default path of the directory that contains history files.
     * @return the default directory path.
     */
    static String getDefaultLoaction() {
        IPath workspaceDir = ResourcesPlugin.getWorkspace().getRoot().getLocation();
        String location = workspaceDir.append(File.separator + DEFAULT_DIRECTORY_PATH).toOSString();
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
