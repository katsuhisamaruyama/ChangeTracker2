/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.xml;

import org.jtool.changetracker.core.CTDialog;
import org.jtool.changetracker.operation.IChangeOperation;
import org.jtool.changetracker.operation.FileOperation;
import org.jtool.changetracker.repository.Repository;
import org.jtool.changetracker.xml.XmlFileManager;
import org.jtool.changetracker.xml.Xml2Operation;
import org.jtool.changetracker.xml.Operation2Xml;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.progress.UIJob;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.time.ZonedDateTime;

/**
 * Performs the action that converts from operation history in the old format into one in the new format.
 * @author Katsuhisa Maruyama
 */
public class XmlConverter {
    
    /**
     * Creates the action that converts from operation history in the old format into one in the new format.
     * @param parent the parent control
     */
    public void createAction(Composite parent) {
        Button convert = new Button(parent, SWT.NONE);
        convert.setText("Convert Format of Repository...");
        convert.addSelectionListener(new SelectionListener() {
            
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
                
                if (Xml2Operation.isChangeTrackerVersion2(path)) {
                    CTDialog.errorDialog("Convert Format",
                            "No need to convert since the XML format is compatible with ChangeTracker v2");
                    return;
                }
                
                String timeString = String.valueOf(ZonedDateTime.now().toInstant().toEpochMilli());
                String dirpath = path + File.separatorChar + "_converted-" + timeString;
                XmlFileManager.makeDir(new File(dirpath));
                
                convert(dirpath);
                CTDialog.informationDialog("Convert Format", "The converted files are stored in " + dirpath);
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
    
    /**
     * Converts change operations that are stored in the operation history files.
     * @param dirpath the path of the directory that contains the converted files
     */
    private void convert(String dirpath) {
        Repository repo = new Repository(dirpath);
        List<File> files = Xml2Operation.getHistoryFiles(dirpath);
        for (File file : files) {
            String path = file.getAbsolutePath();
            repo.addOperationAll(Xml2Operation.getOperations(path));
        }
        
        repo.restoreCodeOnFileOperation();
        repo.compactOperations();
        
        Map<FileOperation, String> map = storeCodeOnFileOperation(repo.getOperations());
        for (File file : files) {
            List<IChangeOperation> ops = Xml2Operation.getOperations(file.getAbsolutePath());
            restoreCodeOnFileOperation(map, ops);
            int index = file.getName().lastIndexOf(Xml2Operation.XML_FILE_EXTENTION);
            Operation2Xml.storeOperations(ops, dirpath + File.separatorChar + file.getName().substring(0, index));
        }
    }
    
    /**
     * Stores the contents of source code on file operations.
     * @param the collection of all change operations
     * @return the map that contains pairs of a file operation and its contents of source code
     */
    private Map<FileOperation, String> storeCodeOnFileOperation(List<IChangeOperation> ops) {
        Map<FileOperation, String> map = new HashMap<FileOperation, String>();
        for (int idx = 0; idx < ops.size(); idx++) {
            IChangeOperation op = ops.get(idx);
            if (op.isFile()) {
                FileOperation fop = (FileOperation)op;
                map.put(fop, fop.getCode());
            }
        }
        return map;
    }
    
    /**
     * Restores the contents of source code on file operations. 
     * @param map the map that contains pairs of a file operation and its contents of source code
     * @param ops the collection of change operations that contains the file operations with the restored source code
     */
    private void restoreCodeOnFileOperation(Map<FileOperation, String> map, List<IChangeOperation> ops) {
        for (int idx = 0; idx < ops.size(); idx++) {
            IChangeOperation op = ops.get(idx);
            if (op.isFile()) {
                FileOperation fop = (FileOperation)op;
                String code = map.get(fop);
                fop.setCode(code);
            }
        }
    }
}
