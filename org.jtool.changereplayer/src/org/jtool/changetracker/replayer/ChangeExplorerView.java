/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.replayer;

import org.jtool.changetracker.core.Activator;
import org.jtool.changetracker.repository.Repository;
import org.jtool.changetracker.repository.CTProject;
import org.jtool.changetracker.repository.CTPackage;
import org.jtool.changetracker.repository.CTFile;
import org.jtool.changetracker.repository.RepositoryManager;
import org.jtool.changetracker.operation.ChangeOperation;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeNodeContentProvider;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Composite;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * A change explorer view.
 * @author Katsuhisa Maruyama
 */
public class ChangeExplorerView extends ViewPart {
    
    /**
     * The identification string that is used to register this view
     */
    public static String ID = "org.jtool.changetracker.replayer.ui.ChangeExplorerView";
    
    /**
     * The instance that manages the repositories that store change operations.
     */
    protected RepositoryManager repositoryManager;
    
    /**
     * The instance that visualizes change operations.
     */
    protected static OperationVisualizer operationVisualizer2;
    
    /**
     * The current repository that is focused on.
     */
    protected Repository currentRepository;
    
    /**
     * The icon images.
     */
    protected static ImageDescriptor repositoryIcon = Activator.getImageDescriptor("icons/history_rep.gif");
    
    /**
     * The table viewer for project/package/file selection.
     */
    protected TreeViewer viewer;
    
    /**
     * The collection of actions that open each of the repositories.
     */
    protected Map<String, Action> actions = new HashMap<String, Action>();
    
    /**
     * The shell of this view.
     */
    protected Shell shell;
    
    /**
     * The action that opens a new repository.
     */
    protected Action newAction;
    
    /**
     * The string attached to online repositories.
     */
    protected final static String PREFIX_FOR_ONLINE_REPOSITORY = "*";
    
    /**
     * Creates a change explorer view.
     */
    public ChangeExplorerView() {
        repositoryManager = RepositoryManager.getInstance();
        currentRepository = repositoryManager.loadRepository();
        ViewManager.getInstance().setChangeExplorerView(this);
    }
    
    /**
     * Creates the control of this view.
     * @param parent the parent control
     */
    @Override
    public void createPartControl(Composite parent) {
        shell = parent.getShell();
        viewer = new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
        viewer.setContentProvider(new TreeNodeContentProvider());
        viewer.setLabelProvider(new ProjectLabelProvider());
        
        newInToolBarMenu();
        openInToolBarMenu(currentRepository.getLocation());
        
        fileSelectAction();
        update();
    }
    
    /**
     * Add an action that opens a new repository to the tool bar menu.
     */
    private void newInToolBarMenu() {
        newAction = new Action("Open a new repository...") {
            
            /**
             * Runs an action.
             */
            @Override
            public void run() {
                DirectoryDialog dialog = new DirectoryDialog(shell);
                String location = dialog.open();
                Repository repo = repositoryManager.getRepository(location);
                if (repo == null) {
                    currentRepository = repositoryManager.openRepository(location);
                    openInToolBarMenu(currentRepository.getLocation());
                }
            }
        };
        
        newAction.setEnabled(true);
        newAction.setId("OpenRepository");
        IMenuManager manager = getViewSite().getActionBars().getMenuManager();
        manager.add(newAction);
        manager.addMenuListener(new IMenuListener() {
            
            /**
             * Invoked when the menu is about to be shown.
             * @param manager the menu manager
             */
            @Override
            public void menuAboutToShow(IMenuManager m) {
                for (Action action : actions.values()) {
                    action.setImageDescriptor(null);
                    
                    for (Repository repo: repositoryManager.getOnlineRepositories()) {
                        if (action.getId().equals(repo.getLocation())) {
                            action.setText(PREFIX_FOR_ONLINE_REPOSITORY + action.getId());
                        } else {
                            action.setText(action.getId());
                        }
                    }
                }
                
                Action action = actions.get(currentRepository.getLocation());
                if (action != null) {
                    action.setImageDescriptor(repositoryIcon);
                }
            }
        });
    }
    
    /**
     * Opens a repository in the tool bar menu.
     * @param location the location of the repository to be opened
     */
    private void openInToolBarMenu(String location) {
        Action action = new Action(location) {
            
            /**
             * Runs an action.
             */
            @Override
            public void run() {
                currentRepository = repositoryManager.openRepository(getId());
                refresh();
            }
        };
        
        action.setEnabled(true);
        action.setId(location);
        IMenuManager manager = getViewSite().getActionBars().getMenuManager();
        if (repositoryManager.getAllRepositories().size() == 0) {
            manager.add(action);
        } else {
            manager.insertBefore(newAction.getId(), action);
        }
        actions.put(location, action);
        refresh();
    }
    
    /**
     * Sets the focus to this view.
     */
    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }
    
    /**
     * Disposes this view.
     */
    @Override
    public void dispose() {
        ViewManager.getInstance().setChangeExplorerView(null);
        ViewManager.getInstance().close();
        
        viewer.getTree().dispose();
        super.dispose();
    }
    
    /**
     * Updates this view.
     */
    protected void update() {
        currentRepository = repositoryManager.openRepository(currentRepository.getLocation());
        refresh();
    }
    
    /**
     * Refreshes this view.
     */
    protected void refresh() {
        UIJob job = new UIJob("Update") {
            
            /**
             * Run the job in the UI thread.
             * @param monitor the progress monitor to use to display progress
             */
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                if (viewer.getControl().isDisposed()) {
                    return Status.CANCEL_STATUS;
                }
                viewer.setInput(getProjectNodes());
                viewer.refresh();
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }
    
    /**
     * Registers an action when a file is double-clicked.
     */
    private void fileSelectAction() {
        viewer.addDoubleClickListener(new IDoubleClickListener() {
            
            /**
             * Receives an event when a double click is performed.
             * @param evt event describing the double-click
             */
            @Override
            public void doubleClick(DoubleClickEvent evt) {
                if (viewer.getSelection() instanceof IStructuredSelection) {
                    IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
                    Object element = selection.getFirstElement();
                    if (element instanceof TreeNode) {
                        TreeNode node = (TreeNode)element;
                        Object value = node.getValue();
                        if (value instanceof CTFile) {
                            CTFile finfo = (CTFile)value;
                            ViewManager.getInstance().show(finfo);
                        }
                    }
                }
            }
        });
    }
    
    /**
     * Obtains tree nodes that represents all projects within the current repository.
     * @return the collection of the project nodes
     */
    private TreeNode[] getProjectNodes() {
        List<CTProject> projects = currentRepository.getProjectHistory();
        TreeNode[] nodes = new TreeNode[projects.size()];
        for (int i = 0; i < projects.size(); i++) {
            CTProject pinfo = projects.get(i);
            TreeNode node = new TreeNode(pinfo);
            TreeNode[] packageNodes = getPackageNodes(pinfo, node);
            node.setChildren(packageNodes);
            node.setParent(null);
            nodes[i] = node;
        }
        return nodes;
    }
    
    /**
     * Obtains tree nodes that represents all packages within a project.
     * @param prjinfo information about the project
     * @param parent the parent of the created package nodes
     * @return the collection of the package nodes
     */
    private TreeNode[] getPackageNodes(CTProject prjinfo, TreeNode parent) {
        List<CTPackage> packages = prjinfo.getPackages();
        TreeNode[] nodes = new TreeNode[packages.size()];
        for (int i = 0; i < packages.size(); i++) {
            CTPackage pinfo = packages.get(i);
            TreeNode node = new TreeNode(pinfo);
            TreeNode[] fileNodes = getFileNodes(pinfo, node);
            node.setChildren(fileNodes);
            node.setParent(parent);
            nodes[i] = node;
        }
        return nodes;
    }
    
    /**
     * Obtains tree nodes that represents all files within a package.
     * @param pkginfo information about the package
     * @param parent the parent of the created file nodes
     * @return the collection of the file nodes
     */
    private TreeNode[] getFileNodes(CTPackage pkginfo, TreeNode parent) {
        List<CTFile> files = pkginfo.getFiles();
        TreeNode[] nodes = new TreeNode[files.size()];
        for (int i = 0; i < files.size(); i++) {
            CTFile finfo = files.get(i);
            TreeNode node = new TreeNode(finfo);
            node.setChildren(null);
            node.setParent(parent);
            nodes[i] = node;
        }
        return nodes;
    }
}

/**
 * Manages a label provider for the package explorer view.
 */
class ProjectLabelProvider extends LabelProvider {
    
    /**
     * The icon images.
     */
    private static final Image projectImage = Activator.getImageDescriptor("icons/projects.gif").createImage();
    private static final Image packageImage = Activator.getImageDescriptor("icons/package_obj.gif").createImage();
    private static final Image fileImage    = Activator.getImageDescriptor("icons/jcu_obj.gif").createImage();
    private static final Image warningImage = Activator.getImageDescriptor("icons/warning.gif").createImage();
    
    /**
     * Returns the image for a node.
     * @param node the node displayed in the view
     */
    @Override
    public Image getImage(Object node) {
        if (node instanceof TreeNode) {
            Object value = ((TreeNode)node).getValue();
            if (value instanceof CTProject) {
                return projectImage;
            } else if (value instanceof CTPackage) {
                return packageImage;
            } else if (value instanceof CTFile) {
                return fileImage;
            }
        }
        return warningImage;
    }
    
    /**
     * Returns the text string for a node.
     * @param node the node displayed in the view
     */
    @Override
    public String getText(Object node) {
        if (node instanceof TreeNode) {
            Object value = ((TreeNode)node).getValue();
            if (value instanceof CTProject) {
                CTProject prjinfo = (CTProject)value;
                String timeInfo = "(" + ChangeOperation.getFormatedTime(prjinfo.getFromTime()) +
                                  " - " + ChangeOperation.getFormatedTime(prjinfo.getToTime()) + ")";
                return prjinfo.getName() + " " + timeInfo;
            
            } else if (value instanceof CTPackage) {
                CTPackage pkginfo = (CTPackage)value;
                String timeInfo = "(" + ChangeOperation.getFormatedTime(pkginfo.getFromTime()) +
                                  " - " + ChangeOperation.getFormatedTime(pkginfo.getToTime()) + ")";
                return pkginfo.getName() + " " + timeInfo;
            
            } else if (value instanceof CTFile) {
                CTFile finfo = (CTFile)value;
                String timeInfo = "(" + ChangeOperation.getFormatedTime(finfo.getFromTime()) +
                                  " - " + ChangeOperation.getFormatedTime(finfo.getToTime()) + ")";
                return finfo.getName() + " " + timeInfo + " [" + finfo.getNumberOfOprations() + "]";
            }
        }
        return "Unknow Java resource";
    }
}
