/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.core;

import org.jtool.changetracker.repository.RepositoryManager;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 * @author Katsuhisa Maruyama
 */
public class Activator extends AbstractUIPlugin implements IStartup {
    
    /**
     * The plug-in ID.
     */
    public static final String PLUGIN_ID = "org.jtool.changetracker.core";
    
    /**
     * The plug-in instance.
     */
    private static Activator plugin;
    
    /**
     * Performs actions in a separate thread after the workbench initializes.
     */
    @Override
    public void earlyStartup() {
        RepositoryManager.getInstance().initialize();
    }
    
    /**
     * Creates a plug-in instance.
     */
    public Activator() {
    }
    
    /**
     * Performs actions when the plug-in is activated.
     * @param context the bundle context for this plug-in
     * @throws Exception if this plug-in did not start up properly
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }
    
    /**
     * Performs actions when when the plug-in is shut down.
     * @param context the bundle context for this plug-in
     * @throws Exception if this this plug-in fails to stop
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        RepositoryManager.getInstance().terminate();
        
        super.stop(context);
        plugin = null;
    }
    
    /**
     * Returns the default plug-in instance.
     * @return the default plug-in instance
     */
    public static Activator getPlugin() {
        return plugin;
    }
    
    /**
     * Returns the window of the workbench.
     * @return the workbench window
     */
    public static IWorkbenchWindow getWorkbenchWindow() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    }
    
    /**
     * Returns the page of the workbench.
     * @return the workbench page
     */
    public static IWorkbenchPage getWorkbenchPage() {
        return getWorkbenchWindow().getActivePage();
    }
    
    /**
     * Returns the image descriptor for an image file.
     * @param path the image file
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
}
