/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.repository;

import org.jtool.changetracker.operation.IChangeOperation;
import org.jtool.changetracker.operation.ChangeOperation;
import org.jtool.changetracker.operation.CodeOperation;
import org.jtool.changetracker.operation.FileOperation;
import org.jtool.changetracker.dependencyanalyzer.DependencyDetector;
import org.jtool.changetracker.dependencyanalyzer.ParseableSnapshot;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

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
    protected List<RepositoryChangedListener> listeners = new ArrayList<RepositoryChangedListener>();
    
    /**
     * Creates an instance that stores information about the repository.
     * @param loc the path of the location of the repository
     */
    public Repository(String loc) {
        this.location = loc;
    }
    
    /**
     * Clears the whole information about this repository.
     */
    public  void clear() {
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
     * Stores change operations related to the same file into this repository.
     * @param ops the collection of the change operations to be stored
     */
    void storeOperationAll(List<? extends IChangeOperation> ops) {
        if (ops == null || ops.size() == 0) {
            return;
        }
        addOperationAll(ops);
    }
    
    /**
     * Add change operations to this repository.
     * @param ops the collection of the change operations to be added
     */
    private void addOperationAll(List<? extends IChangeOperation> ops) {
        IChangeOperation op = ops.get(0);
        CTPath pathinfo = new CTPath(op);
        if (op.isFile()) {
            createResourceInfo((FileOperation)op, pathinfo);
        }
        CTProject projectInfo = createProject(pathinfo);
        CTPackage packageInfo = createPackage(pathinfo, projectInfo);
        CTFile fileInfo = createFile(pathinfo, op,  packageInfo);
        for (int idx = 0; idx < ops.size(); idx++) {
            addOperation(ops.get(idx), projectInfo, packageInfo, fileInfo);
        }
    }
    
    /**
     * Stores a code change operation into this repository.
     * @param op the change operation to be stored
     */
    void storeOperation(IChangeOperation op) {
        if (op == null) {
            return;
        }
        addOperation(op);
    }
    
    /**
     * Adds a change operation to this repository.
     * @param op the code change operation to be added
     */
    private void addOperation(IChangeOperation op) {
        CTPath pathinfo = new CTPath(op);
        if (op.isFile()) {
            createResourceInfo((FileOperation)op, pathinfo);
        }
        CTProject projectInfo = createProject(pathinfo);
        CTPackage packageInfo = createPackage(pathinfo, projectInfo);
        CTFile fileInfo = createFile(pathinfo, op, packageInfo);
        addOperation(op, projectInfo, packageInfo, fileInfo);
    }
    
    /**
     * Creates information about a resource related the a change operation.
     * @param op the change operation to be stored
     * @param pathinfo information about path of the resource
     */
    private void createResourceInfo(FileOperation op, CTPath pathinfo) {
        if (op.isAdded()) {
            CTProject projectInfo = createProject(pathinfo);
            CTPackage packageInfo = createPackage(pathinfo, projectInfo);
            createFile(pathinfo, op, packageInfo);
        } else if (op.isMovedFrom() || op.isRenamedFrom()) {
            eraseFile(pathinfo);
            CTProject projectInfo = createProject(pathinfo);
            CTPackage packageInfo = createPackage(pathinfo, projectInfo);
            CTFile fileInfo = createFile(pathinfo, op, packageInfo);
            
            CTFile fromFileInfo = getFromFile(op.getSrcDstPath());
            if (fromFileInfo != null) {
                fromFileInfo.setFileInfoTo(fileInfo);
                fileInfo.setFileInfoFrom(fromFileInfo);
            }
        } else if (op.isRemoved() || op.isMovedTo() || op.isRenamedTo()) {
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
     * Adds a change operation to the repository.
     * @param op the change operation to be stored
     * @param prjinfo information about a project related to the change operation
     * @param pkginfo information about a package related to the change operation
     * @param finfo information about a file related to the change operation
     */
    private void addOperation(IChangeOperation op,
            CTProject prjinfo, CTPackage pkginfo, CTFile finfo) {
        finfo.addOperation(op);
        if (op instanceof ChangeOperation) {
            ((ChangeOperation)op).setFile(finfo);
        }
        prjinfo.updateTimeRange(op);
        pkginfo.updateTimeRange(op);
        finfo.updateTimeRange(op);
        if (op.isDocumentOrCopy()) {
            detectAffectedJavaConstructs((CodeOperation)op, finfo);
        }
    }
    
    /**
     * Detects Java constructs that a change operation affects.
     * @param op the change operation
     * @param finfo information about a file that contains the Java constructs
     */
    private void detectAffectedJavaConstructs(CodeOperation op, CTFile finfo) {
        int index = finfo.getOperationIndexAt(op.getTime());
        ParseableSnapshot sn = DependencyDetector.parse(finfo, index);
        if (sn != null) {
            ParseableSnapshot psn = finfo.getLastSnapshot();
            finfo.addSnapshot(sn);
            
            List<IChangeOperation> ops;
            if (psn != null) {
                if (psn.getIndex() < 0) {
                    ops = finfo.getOperations();
                } else {
                    ops = finfo.getOperationsAfter(psn.getTime());
                    ops.remove(0);
                }
            } else {
                ops = finfo.getOperations();
            }
            List<CodeOperation> cops = DependencyDetector.getCodeOperations(ops);
            DependencyDetector.detectBackwardChangeEdges(psn, cops);
            DependencyDetector.detectForwardChangeEdges(sn, cops);
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
    public void addEventListener(RepositoryChangedListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Removes the listener that no longer receives repository changed events.
     * @param listener the repository changed listener to be removed
     */
    public void removeEventListener(RepositoryChangedListener listener) {
        if (listener != null && listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }
    
    /**
     * Sends a repository changed event to all the listeners.
     * @param evt the changed event.
     */
    public void fire(RepositoryChangedEvent evt) {
        for (RepositoryChangedListener listener : listeners) {
            listener.notify(evt);
        }
    }
    
    /**
     * Restores the contents of source code on file operations.
     */
    public void restoreCodeOnFileOperation() {
        for (CTFile finfo : getFileHistory()) {
            finfo.getOperationHistory().restoreCodeOnFileOperation();
        }
    }
    
    /**
     * Compacts the history of change operations.
     */
    public void compactOperations() {
        for (CTFile finfo : getFileHistory()) {
            finfo.getOperationHistory().compact();
        }
    }
    
    /**
     * Checks the change operations in this repository were consistently performed.
     */
    public void checkOperationConsistency() {
        for (CTFile finfo : getFileHistory()) {
            finfo.getOperationHistory().checkOperationConsistency();
        }
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
