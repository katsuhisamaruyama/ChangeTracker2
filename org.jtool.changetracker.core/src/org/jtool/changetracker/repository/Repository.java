/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.repository;

import org.jtool.changetracker.operation.IChangeOperation;
import org.jtool.changetracker.operation.ChangeOperation;
import org.jtool.changetracker.operation.CodeOperation;
import org.jtool.changetracker.operation.FileOperation;
import org.jtool.changetracker.core.CTConsole;
import org.jtool.changetracker.dependencyanalyzer.DependencyDetector;
import org.jtool.changetracker.dependencyanalyzer.ParseableSnapshot;
import org.jtool.changetracker.xml.Operation2Xml;
import org.jtool.changetracker.xml.Xml2Operation;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.UIJob;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.io.File;

/**
 * Stores information about the repository for projects, packages, and files.
 * @author Katsuhisa Maruyama
 */
public class Repository {
    
    /**
     * The path of the location of this repository.
     */
    private String location;
    
    /**
     * The map that stores information about currently existing projects.
     */
    private Map<String, CTProject> projectMap = new HashMap<String, CTProject>();
    
    /**
     * The map that stores information about currently existing packages.
     */
    private Map<String, CTPackage> packageMap = new HashMap<String, CTPackage>();
    
    /**
     * The map that stores information of currently existing files.
     */
    private Map<String, CTFile> fileMap = new HashMap<String, CTFile>();
    
    /**
     * The collection of information about all files existing in the past.
     */
    private List<CTFile> fileHistory = new ArrayList<CTFile>();
    
    /**
     * The collection of listeners that receives repository change events.
     */
    protected List<IRepositoryListener> listeners = new ArrayList<IRepositoryListener>();
    
    /**
     * Creates an instance that stores information about the repository.
     * @param location the path of the location of the repository
     */
    public Repository(String location) {
        this.location = location;
    }
    
    /**
     * Clears the whole information about this repository.
     */
    public void clear() {
        projectMap.clear();
        packageMap.clear();
        fileMap.clear();
        fileHistory.clear();
    }
    
    /**
     * Returns the location of this repository.
     * @return the location
     */
    public String getLocation() {
        return location;
    }
    
    /**
     * Returns information about the projects stored in this repository.
     * @return all the file information
     */
    public List<CTProject> getProjectHistory() {
        return new ArrayList<CTProject>(projectMap.values());
    }
    
    /**
     * Returns information about the packages stored in this repository.
     * @return all the file information
     */
    public List<CTPackage> getPackageHistory() {
        return new ArrayList<CTPackage>(packageMap.values());
    }
    
    /**
     * Returns information about the files stored in this repository.
     * @return all the file information
     */
    public List<CTFile> getFileHistory() {
        return fileHistory;
    }
    
    /**
     * Returns the collection of all the change operations in this repository.
     * @return the collection of the change operations
     */
    public List<IChangeOperation> getOperations() {
        List<IChangeOperation> ops = new ArrayList<IChangeOperation>();
        for (CTFile finfo : getFileHistory()) {
            ops.addAll(finfo.getOperations());
        }
        return ops;
    }
    
    /**
     * Stores change operations into the main repository.
     * @param ops the collection of the change operations to be stored
     */
    public void storeChangeOperations(List<IChangeOperation> ops) {
        if (ops == null || ops.size() == 0) {
            return;
        }
        
        ChangeOperation.sort(ops);
        ops = OperationCompactor.compact(ops);
        fireAboutTo(RepositoryEvent.Type.OPERATION_ADD);
        addOperationAll(ops);
        fireChanged(RepositoryEvent.Type.OPERATION_ADD);
        
        storeChangeOperationsIntoHistoryFile(ops);
    }
    
    /**
     * Stores a code change operation into the main repository.
     * @param op the change operation to be stored
     */
    public void storeOperation(IChangeOperation op) {
        if (op == null) {
            return;
        }
        
        fireAboutTo(RepositoryEvent.Type.OPERATION_ADD);
        addOperation(op);
        fireChanged(RepositoryEvent.Type.OPERATION_ADD);
        
        List<IChangeOperation> ops = new ArrayList<IChangeOperation>(1);
        ops.add(op);
        storeChangeOperationsIntoHistoryFile(ops);
    }
    
    /**
     * Stores change operations into a history file.
     * @param ops the collection of the change operations
     */
    public void storeChangeOperationsIntoHistoryFile(List<IChangeOperation> ops) {
        if (ops.size() == 0) {
            return;
        }
        
        long time = ops.get(0).getTimeAsLong();
        String filename = location + File.separatorChar + String.valueOf(time) + Xml2Operation.XML_FILE_EXTENTION;
        boolean result = Operation2Xml.storeOperations(ops, filename);
        if (!result) {
            CTConsole.println("Failed to store change operations into a history file: " + filename);
        }
    }
    
    /**
     * Add change operations to this repository and detects Java constructs that affects the added operations.
     * @param ops the collection of the change operations to be added
     */
    public void addOperationAll(List<? extends IChangeOperation> ops) {
        for (int idx = 0; idx < ops.size(); idx++) {
            addOperation(ops.get(idx));
        }
    }
    
    /**
     * Adds a change operation to this repository.
     * @param op the code change operation to be added
     */
    public void addOperation(IChangeOperation op) {
        CTPath pathinfo = new CTPath(op);
        if (op.isFile()) {
            createResourceInfo((FileOperation)op, pathinfo);
        }
        CTProject projectInfo = createProject(pathinfo);
        CTPackage packageInfo = createPackage(pathinfo, projectInfo);
        CTFile fileInfo = createFile(pathinfo, op, packageInfo);
        
        fileInfo.addOperation(op);
        if (op instanceof ChangeOperation) {
            ((ChangeOperation)op).setFile(fileInfo);
        }
        projectInfo.updateTimeRange(op);
        packageInfo.updateTimeRange(op);
        fileInfo.updateTimeRange(op);
        
        if (op.isFile()) {
            fileInfo.getOperationHistory().restoreCodeOnFileOperation((FileOperation)op);
        }
        
        detectAffectedJavaConstructs(fileInfo, op);
    }
    
    /**
     * Creates information about a resource related the a change operation.
     * @param op the change operation to be stored
     * @param pathinfo information about path of the resource
     */
    private void createResourceInfo(FileOperation op, CTPath pathinfo) {
        if (op.isAdd()) {
            CTProject projectInfo = createProject(pathinfo);
            CTPackage packageInfo = createPackage(pathinfo, projectInfo);
            createFile(pathinfo, op, packageInfo);
        } else if (op.isMoveFrom() || op.isRenameFrom()) {
            eraseFile(pathinfo);
            CTProject projectInfo = createProject(pathinfo);
            CTPackage packageInfo = createPackage(pathinfo, projectInfo);
            CTFile fileInfo = createFile(pathinfo, op, packageInfo);
            
            CTFile fromFileInfo = getFromFile(op.getSrcDstPath());
            if (fromFileInfo != null) {
                fromFileInfo.setFileInfoTo(fileInfo);
                fileInfo.setFileInfoFrom(fromFileInfo);
            }
        } else if (op.isDelete() || op.isMoveTo() || op.isRenameTo()) {
            eraseFile(pathinfo);
        }
    }
    
    /**
     * Obtains the last file information with a path name.
     * @param path the path name of the file information to be retrieved
     * @return the found file information, or <code>null</code> if none
     */
    private CTFile getFromFile(String path) {
        for (int idx = fileHistory.size() - 1; idx >= 0; idx--) {
            CTFile info = fileHistory.get(idx);
            if (info.getPath().equals(path)) {
                return info;
            }
        }
        return null;
    }
    
    /**
     * Detects Java constructs within a snapshot that a change operation affects.
     * @param finfo file information about the change operation
     * @param op the change operation
     */
    private void detectAffectedJavaConstructs(CTFile finfo, IChangeOperation op) {
        int index = finfo.getOperationIndexAt(op.getTime());
        ParseableSnapshot sn = DependencyDetector.parse(finfo, index);
        if (sn == null) {
            return;
        }
        
        finfo.addSnapshot(sn);
        
        ParseableSnapshot lastsn = finfo.getLastSnapshot();
        List<IChangeOperation> ops;
        if (lastsn != null) {
            if (lastsn.getIndex() < 0) {
                ops = finfo.getOperations();
            } else {
                ops = finfo.getOperationsAfter(lastsn.getTime());
                ops.remove(0);
            }
        } else {
            ops = finfo.getOperations();
        }
        List<CodeOperation> cops = DependencyDetector.getCodeOperations(ops);
        DependencyDetector.detectBackwardChangeEdges(lastsn, cops);
        DependencyDetector.detectForwardChangeEdges(sn, cops);
    }
    
    /**
     * Collects change operations from history files and stores them into this repository.
     */
    public void collectFromHistoryFiles() {
        File dir = new File(location);
        if (!dir.isDirectory()) {
            return;
        }
        
        UIJob job = new UIJob("Collect") {
            
            /**
             * Run the job in the UI thread.
             * @param monitor the progress monitor to use to display progress
             */
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                try {
                    List<File> files = Xml2Operation.getHistoryFiles(location);
                    monitor.beginTask("Reading change operations from history files", files.size());
                    readHistoryFiles(files, monitor);
                } catch (InterruptedException e) {
                    clear();
                    return Status.CANCEL_STATUS;
                } finally {
                    monitor.done();
                }
                return Status.OK_STATUS;
            }
        };
        job.setUser(true);
        job.schedule();
    }
    
    /**
     * Reads history files and stores change operations into this repository.
     * @param repo the repository that stores the change operations
     * @param files the collection of the history files
     * @param monitor the progress monitor to use to display progress and receive requests for cancellation
     * @throws Exception if a request to cancel or any failure is detected
     */
    private void readHistoryFiles(List<File> files, IProgressMonitor monitor) throws InterruptedException {
        for (File file : files) {
            String path = file.getAbsolutePath();
            
            List<IChangeOperation> ops = Xml2Operation.getOperations(path);
            if (ops.size() > 0) {
                ops = OperationCompactor.compact(ops);
            }
            
            addOperationAll(ops);
            
            if (monitor.isCanceled()) {
                clear();
                monitor.done();
                throw new InterruptedException("User interrupted");
            }
            monitor.worked(1);
        }
    }
    
    /**
     * Creates information about a project.
     * @param pathinfo information about path of the project
     * @return the created or already existing information about the project
     */
    private CTProject createProject(CTPath pathinfo) {
        String key = pathinfo.getProjectKey();
        CTProject pinfo = projectMap.get(key);
        if (pinfo != null) {
            return pinfo;
        }
        pinfo = new CTProject(pathinfo);
        projectMap.put(key, pinfo);
        return pinfo;
    }
    
    /**
     * Creates information about a package.
     * @param pathinfo information about path of the package
     * @param prjinfo the information about a project that contains the package
     * @return the created or already existing information about the package
     */
    private CTPackage createPackage(CTPath pathinfo, CTProject prjinfo) {
        String key = pathinfo.getPackageKey();
        CTPackage pinfo = packageMap.get(key);
        if (pinfo != null) {
            return pinfo;
        }
        pinfo = new CTPackage(pathinfo, prjinfo);
        packageMap.put(key, pinfo);
        prjinfo.addPackage(pinfo);
        return pinfo;
    }
    
    /**
     * Creates information about a file.
     * @param pathinfo information about path of the file
     * @param op a change operation that was performed on the file
     * @param pckinfo the information about a package that contains the file
     * @return the created or already existing information about the file
     */
    private CTFile createFile(CTPath pathinfo, IChangeOperation op, CTPackage pckinfo) {
        String key = pathinfo.getFileKey();
        CTFile finfo = fileMap.get(key);
        if (finfo != null) {
            return finfo;
        }
        finfo = new CTFile(pathinfo, pckinfo.getProject(), pckinfo);
        if (op.isFile()) {
            FileOperation fop = (FileOperation)op;
            finfo.setInitialCode(fop.getCode());
        }
        fileMap.put(key, finfo);
        fileHistory.add(finfo);
        pckinfo.addFile(finfo);
        return finfo;
    }
    
    /**
     * Removes information about a file.
     * @param pathinfo information about path of the file
     */
    private void eraseFile(CTPath pathinfo) {
        String key = pathinfo.getFileKey();
        fileMap.remove(key);
    }
    
    /**
     * Adds the listener that receives repository changed events.
     * @param listener the repository changed listener to be added
     */
    public void addEventListener(IRepositoryListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Removes the listener that no longer receives repository changed events.
     * @param listener the repository changed listener to be removed
     */
    public void removeEventListener(IRepositoryListener listener) {
        if (listener != null && listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }
    
    /**
     * Sends a repository change event to all the listeners before the event is about to occur.
     * @param type the type of a repository changed event.
     */
    void fireAboutTo(RepositoryEvent.Type type) {
        RepositoryEvent evt = new RepositoryEvent(this, type);
        for (IRepositoryListener listener : listeners) {
            listener.aboutTo(evt);
        }
    }
    
    /**
     * Sends a repository change event to all the listeners after the event occurred.
     * @param type the type of a repository changed event.
     */
    void fireChanged(RepositoryEvent.Type type) {
        RepositoryEvent evt = new RepositoryEvent(this, type);
        for (IRepositoryListener listener : listeners) {
            listener.changed(evt);
        }
    }
    
    /**
     * Checks if change operations are consistent with restored code.
     * @return <code>true</code> if all the change operations are consistent with the restored code, otherwise <code>false</code>
     */
    public boolean checkOperationConsistency() {
        boolean success = true;
        for (CTFile finfo : getFileHistory()) {
            boolean result = finfo.getOperationHistory().checkOperationConsistency();
            if (!result) {
                success = false;
            }
        }
        return success;
    }
    
    /**
     * Returns the string for printing.
     * @return the string for printing
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("Repository=[" + location + "]");
        buf.append(" Project#=" + projectMap.size());
        buf.append(" Package#=" + packageMap.size());
        buf.append(" File#=" + fileHistory.size());
        return buf.toString();
    }
}
