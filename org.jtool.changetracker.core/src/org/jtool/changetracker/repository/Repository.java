/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.repository;

import org.jtool.changetracker.operation.IChangeOperation;
import org.jtool.changetracker.operation.ResourceOperation;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.io.File;

/**
 * Stores information about the repository of a workspace, projects, packages, and files.
 * @author Katsuhisa Maruyama
 */
public class Repository {
    
    /**
     * The path name of the location of this repository.
     */
    private String location;
    
    /**
     * The map that stores information about currently existing projects.
     */
    private Map<String, ProjectInfo> projectMap = new HashMap<String, ProjectInfo>();
    
    /**
     * The collection of information about all packages.
     */
    private List<ProjectInfo> projectHistory = new ArrayList<ProjectInfo>();
    
    /**
     * The map that stores information about currently existing packages.
     */
    private Map<String, PackageInfo> packageMap = new HashMap<String, PackageInfo>();
    
    /**
     * The collection of information about all packages.
     */
    private List<PackageInfo> packageHistory = new ArrayList<PackageInfo>();
    
    /**
     * The map that stores information of currently existing files.
     */
    private Map<String, FileInfo> fileMap = new HashMap<String, FileInfo>();
    
    /**
     * The collection of information about all files.
     */
    private List<FileInfo> fileHistory = new ArrayList<FileInfo>();
    
    /**
     * The collection of listeners that receives repository change events.
     */
    protected List<RepositoryChangedListener> listeners = new ArrayList<RepositoryChangedListener>();
    
    /**
     * The character that concatenates a resource name and a branch name.
     */
    private static String CONCATENATION_SYMBOL = "$";
    
    /**
     * The code change operations that are not stored in file information.
     */
    private List<IChangeOperation> nonFileOperations;
    
    /**
     * Creates an instance that stores information about the repository.
     * @param location the path name of the location of the repository
     */
    Repository(String location) {
        this.location = location;
        this.nonFileOperations = new ArrayList<IChangeOperation>();
    }
    
    /**
     * Clears the whole information in this repository.
     */
    public void clear() {
        projectMap.clear();
        projectHistory.clear();
        packageMap.clear();
        packageHistory.clear();
        fileMap.clear();
        fileHistory.clear();
    }
    
    /**
     * Returns the path name of the location of this repository.
     * @return the path name of the location
     */
    public String getLocationPath() {
        return location;
    }
    
    /**
     * Obtains all the code change operations that are not stored in file information.
     * @return the collection of the code change operations
     */
    List<IChangeOperation> getNonFileOperations() {
        return nonFileOperations;
    }
    
    /**
     * Returns all the information on the projects within this repository.
     * @return the collection of the project information
     */
    public List<ProjectInfo> getProjectHistory() {
        return projectHistory;
    }
    
    /**
     * Returns all the information on the packages within this repository.
     * @return the collection of the package information
     */
    public List<PackageInfo> getPackageHistory() {
        return packageHistory;
    }
    
    /**
     * Returns all the information on the files within this repository.
     * @return the collection of the file information
     */
    public List<FileInfo> getFileHistory() {
        return fileHistory;
    }
    
    /**
     * Adds code change operations to the repository.
     * @param operation the code change operation to be added
     */
    public void addOperations(List<IChangeOperation> operations) {
        if (operations == null || operations.size() == 0) {
            return;
        }
        
        storeOperations(operations);
        
        RepositoryChangedEvent evt = new RepositoryChangedEvent(this);
        fire(evt);
    }
    
    /**
     * Adds a code change operation to the repository.
     * @param operation the code change operation to be added
     */
    public void addOperation(IChangeOperation operation) {
        if (operation == null) {
            return;
        }
        
        storeOperation(operation);
        
        RepositoryChangedEvent evt = new RepositoryChangedEvent(this);
        fire(evt);
    }
    
    /**
     * Stores a code change operation into the repository.
     * @param operation the code change operation to be stored
     */
    void storeOperations(List<IChangeOperation> operations) {
        String path = operations.get(0).getPath();
        String branch = operations.get(0).getBranch();
        
        
        
        
        String projectName = getProjectName(path, branch);
        String packageName = getPackageName(path, branch);
        String fileName = getFileName(path, branch);
        
        for (int idx = 0; idx < operations.size(); idx ++) {
            storeOperation(operations.get(idx), projectName, packageName, fileName);
        }
    }
    
    /**
     * Stores a code change operation into the repository.
     * @param operation the code change operation to be stored
     */
    void storeOperation(IChangeOperation operation) {
        String path = operation.getPath();
        
        System.err.println("OP = " + operation + " " + path);
        String branch = operation.getBranch();
        String projectName = getProjectName(path, branch);
        String packageName = getPackageName(path, branch);
        String fileName = getFileName(path, branch);
        
        storeOperation(operation, projectName, packageName, fileName);
    }
    
    /**
     * Stores a code change operation into the repository.
     * @param operation the code change operation to be stored
     * @param projectName the name of the project related to the operation
     * @param packageName the name of the package related to the operation
     * @param fileName the name of the file related to the operation
     */
    private void storeOperation(IChangeOperation operation, String projectName, String packageName, String fileName) {
        if (operation.isResource()) {
            storeResourceOperation((ResourceOperation)operation, projectName, packageName, fileName);
            nonFileOperations.add(operation);
            
        } else if (operation.isGit()) {
            nonFileOperations.add(operation);
            
        } else {
            storeNormalOperation(operation, projectName, packageName, fileName);
        }
    }
    
    /**
     * Obtains the last file information with a path name.
     * @param path the path name of the file information to be retrieved
     * @return the found file information, or <code>null</code> if none
     */
    private FileInfo getFromFileInfo(String path) {
        for (int idx = fileHistory.size() - 1; idx <= 0; idx--) {
            FileInfo info = fileHistory.get(idx);
            if (info.getPath().equals(path)) {
                return info;
            }
        }
        return null;
    }
    
    /**
     * Stores a resource operation in the repository.
     * @param operation the operation to be stored
     * @param projectName the name of the project related to the operation
     * @param packageName the name of the package related to the operation
     * @param fileName the name of the file related to the operation
     */
    private void storeResourceOperation(ResourceOperation operation, String projectName, String packageName, String fileName) {
        String projectKey = getProjectKey(projectName);
        String packageKey = getPackageKey(projectName, packageName);
        String fileKey = getFileKey(projectName, packageName, fileName);
        
        if (operation.isProjectResource()) {
            if (operation.isResourceAdd() || operation.isResourceRenameFrom() || operation.isResourceMoveFrom()) {
                createProjectInfo(projectName, projectKey);
                
            } else if (operation.isResourceRemove() || operation.isResourceRenameTo() || operation.isResourceMoveTo()) {
                eraseProjectInfo(projectKey);
            }
            
        } else if (operation.isPackageResource()) {
            if (operation.isResourceAdd() || operation.isResourceRenameFrom() || operation.isResourceMoveFrom()) {
                ProjectInfo projectInfo = createProjectInfo(projectName, projectKey);
                createPackageInfo(packageName, packageKey, projectInfo);
                
             } else if (operation.isResourceRemove() || operation.isResourceRenameTo() || operation.isResourceMoveTo()) {
                 erasePackageInfo(packageKey);
            }
            
        } else if (operation.isFileResource()) {
            if (operation.isResourceAdd()) {
                ProjectInfo projectInfo = createProjectInfo(projectName, projectKey);
                PackageInfo packageInfo = createPackageInfo(packageName, packageKey, projectInfo);
                createFileInfo(fileName, fileKey, operation.getPath(), packageInfo);
                
            } else if (operation.isResourceRenameFrom() || operation.isResourceMoveFrom()) {
                ProjectInfo projectInfo = createProjectInfo(projectName, projectKey);
                PackageInfo packageInfo = createPackageInfo(packageName, packageKey, projectInfo);
                FileInfo fileInfo = createFileInfo(fileName, fileKey, operation.getPath(), packageInfo);
                
                FileInfo fromFileInfo = getFromFileInfo(operation.getSrcDstPath());
                if (fromFileInfo != null) {
                    fromFileInfo.setFileInfoTo(fileInfo);
                    fileInfo.setFileInfoFrom(fromFileInfo);
                }
                
            } else if (operation.isResourceRemove() || operation.isResourceRenameTo() || operation.isResourceMoveTo()) {
                eraseFileInfo(fileKey);
            }
        }
    }
    
    /**
     * Stores a code change operation except for a resource operation in the repository.
     * @param operation the operation to be stored
     * @param projectName the name of the project related to the operation
     * @param packageName the name of the package related to the operation
     * @param fileName the name of the file related to the operation
     */
    private void storeNormalOperation(IChangeOperation operation, String projectName, String packageName, String fileName) {
        String projectKey = getProjectKey(projectName);
        String packageKey = getPackageKey(projectName, packageName);
        String fileKey = getFileKey(projectName, packageName, fileName);
        
        ProjectInfo projectInfo = createProjectInfo(projectName, projectKey);
        PackageInfo packageInfo = createPackageInfo(packageName, packageKey, projectInfo);
        FileInfo fileInfo = createFileInfo(fileName, fileKey, operation.getPath(), packageInfo);
        
        fileInfo.addOperation(operation);
        
        projectInfo.updateTimeRange(operation);
        packageInfo.updateTimeRange(operation);
        fileInfo.updateTimeRange(operation);
    }
    
    /**
     * Creates information about a project.
     * @param projectName the name of the project
     * @param projectKey the key of the project
     * @return the created or already existing information about the project
     */
    private ProjectInfo createProjectInfo(String projectName, String projectKey) {
        ProjectInfo projectInfo = projectMap.get(projectKey);
        if (projectInfo != null) {
            return projectInfo;
        }
        
        projectInfo = new ProjectInfo(projectName);
        projectMap.put(projectKey, projectInfo);
        projectHistory.add(projectInfo);
        return projectInfo;
    }
    
    /**
     * Removes information about a project.
     * @param projectKey the key of the project
     */
    private void eraseProjectInfo(String projectKey) {
        projectMap.remove(projectKey);
    }
    
    /**
     * Creates information about a package.
     * @param packageName the name of the package
     * @param packageKey the key of the package
     * @param projectInfo the information about a project that contains the package
     * @return the created or already existing information about the package
     */
    private PackageInfo createPackageInfo(String packageName, String packageKey, ProjectInfo projectInfo) {
        PackageInfo packageInfo = packageMap.get(packageKey);
        if (packageInfo != null) {
            return packageInfo;
        }
        
        packageInfo = new PackageInfo(packageName, projectInfo);
        packageMap.put(packageKey, packageInfo);
        packageHistory.add(packageInfo);
        projectInfo.addPackage(packageInfo);
        
        return packageInfo;
    }
    
    /**
     * Removes information about a package.
     * @param packageKey the key of the package
     */
    private void erasePackageInfo(String packageKey) {
        packageMap.remove(packageKey);
    }
    
    /**
     * Creates information about a file.
     * @param fileName the name of the file
     * @param fileKey the key of the file
     * @param path the path name of the file
     * @param packageInfo the information about a package that contains the file
     * @return the created or already existing information about the file
     */
    private FileInfo createFileInfo(String fileName, String fileKey, String path, PackageInfo packageInfo) {
        FileInfo fileInfo = fileMap.get(fileKey);
        if (fileInfo != null) {
            return fileInfo;
        }
        
        fileInfo = new FileInfo(fileName, path, packageInfo);
        fileMap.put(fileKey, fileInfo);
        fileHistory.add(fileInfo);
        packageInfo.addFile(fileInfo);
        
        return fileInfo;
    }
    
    /**
     * Removes information about a file.
     * @param fileKey the key of the file
     */
    private void eraseFileInfo(String fileKey) {
        fileMap.remove(fileKey);
    }
    
    /**
     * Returns the key for retrieval of project information.
     * @param projectName the project name
     * @return the key string for the project
     */
    private String getProjectKey(String projectName) {
        return projectName;
    }
    
    /**
     * Returns the key for retrieval of package information.
     * @param projectName the project name
     * @param packageName the package name
     * @return the key string for the package
     */
    private String getPackageKey(String projectName, String packageName) {
        return projectName + "%" + packageName;
    }
    
    /**
     * Returns the key for retrieval of file information.
     * @param projectName the project name
     * @param packageName the package name
     * @param fileName the file name
     * @return the key string for the file
     */
    private String getFileKey(String projectName, String packageName, String fileName) {
        return projectName + "%" + packageName + "%" + fileName;
    }
    
    /**
     * Extracts the project name from a path.
     * @param the path name of the file
     * @branch the branch name of the file
     * @return the project name
     */
    private String getProjectName(String path, String branch) {
        assert path != null;
        
        int firstIndex = path.indexOf(File.separatorChar, 1);
        if (firstIndex == -1) {
            System.err.println("Unknown project: " + path);
            return "Unknown";
        }
        
        String name = path.substring(1, firstIndex);
        
        if (branch.length() != 0) {
            return name + CONCATENATION_SYMBOL + branch;
        }
        return name;
    }
    /**
     * Extracts the package name from a path.
     * @param path the path name of the file
     * @branch the branch name of the file
     * @return the package name
     */
    private String getPackageName(String path, String branch) {
        assert path != null;
        
        String srcDir = File.separatorChar + "src" + File.separatorChar;
        int firstIndex = path.indexOf(srcDir);
        int lastIndex = path.lastIndexOf(File.separatorChar) + 1;
        if (firstIndex == -1 || lastIndex == -1) {
            System.err.println("Unknown package: " + path);
            return "Unknown";
        }
        
        if (firstIndex + srcDir.length() == lastIndex) {
            return "(default package)";
        }
        
        String name = path.substring(firstIndex + srcDir.length(), lastIndex - 1);
        name = name.replace(File.separatorChar, '.');
        
        if (branch.length() != 0) {
            return name + CONCATENATION_SYMBOL + branch;
        }
        return name;
    }
    
    /**
     * Extracts the file name from a path.
     * @param path the path name of the file
     * @branch the branch name of the file
     * @return the file name without its path information
     */
    private String getFileName(String path, String branch) {
        assert path != null;
        
        int lastIndex = path.lastIndexOf(File.separatorChar) + 1;
        if (lastIndex == -1) {
            System.err.println("Unknown file: " + path);
            return "Unknown";
        }
        
        String name = path.substring(lastIndex);
        if (branch.length() != 0) {
            return name + CONCATENATION_SYMBOL + branch;
        }
        return name;
    }
    
    /**
     * Adds the listener that receives repository changed events.
     * @param listener the repository changed listener to be added
     */
    public void addEventListener(RepositoryChangedListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Removes the listener that no longer receives repository changed events.
     * @param listener the repository changed listener to be removed
     */
    public void removeEventListener(RepositoryChangedListener listener) {
        listeners.remove(listener);
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
}
