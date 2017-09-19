/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.core;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

/**
 * Initializes the preference values.
 * @author Katsuhisa Maruyama
 */
public class CTPreferenceInitializer extends AbstractPreferenceInitializer {
    
    /**
     * Stores initial preference values.
     */
    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = Activator.getPlugin().getPreferenceStore();
        String defaultLocation = CTPreferencePage.getDefaultLoaction();
        store.setDefault(CTPreferencePage.REPOSITORY_LOCATION, defaultLocation);
    }
}
