/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.xml;

import org.jtool.changetracker.xml.XmlFileManager;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import java.io.File;
import java.time.ZonedDateTime;

/**
 * Performs the action that exports a zip archive file.
 * @author Katsuhisa Maruyama
 */
public class ZipArchiveExporter {
    
    /**
     * Creates the action that exports a zip archive file.
     * @param parent the parent control
     */
    public void createAction(Composite parent) {
        Button export = new Button(parent, SWT.NONE);
        export.setText("Export Repository...");
        export.addSelectionListener(new SelectionListener() {
            
            /**
             * Receives a selection event when selection occurs in the control.
             * @param evt the event containing information about the selection
             */
            @Override
            public void widgetSelected(SelectionEvent evt) {
                DirectoryDialog dialog = new DirectoryDialog(parent.getShell());
                String path = dialog.open();
                if (path == null) {
                    return;
                }
                
                String prefix = new Path(path).removeLastSegments(1).toOSString();
                String timeString = String.valueOf(ZonedDateTime.now().toInstant().toEpochMilli());
                String filename = prefix + File.separator + "history-" + timeString + ".zip";
                XmlFileManager.makeZip(filename, new File(path));
            }
            
            /**
             * Receives a selection event when default selection occurs in the control.
             * @param evt the event containing information about the selection
             */
            @Override
            public void widgetDefaultSelected(SelectionEvent evt) {
            }
        });
    }
}
