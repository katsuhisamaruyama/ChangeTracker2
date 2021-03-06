/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.recorder;

import org.jtool.changetracker.operation.IChangeOperation;
import org.jtool.changetracker.operation.ICodeOperation;
import org.jtool.changetracker.operation.ChangeOperation;
import org.jtool.changetracker.operation.DocumentOperation;
import org.jtool.changetracker.operation.CopyOperation;
import org.jtool.changetracker.operation.FileOperation;
import org.jtool.changetracker.operation.CommandOperation;
import org.jtool.changetracker.operation.RefactoringOperation;
import org.jtool.changetracker.operation.ResourceOperation;
import org.jtool.changetracker.repository.CTPath;
import org.jtool.changetracker.repository.Repository;
import org.jtool.changetracker.repository.RepositoryManager;
import org.jtool.changetracker.core.CTConsole;
import org.jtool.changetracker.core.CTDialog;
import org.jtool.changetracker.core.CTPreferencePage;
import org.jtool.macrorecorder.macro.Macro;
import org.jtool.macrorecorder.macro.CommandMacro;
import org.jtool.macrorecorder.macro.CompoundMacro;
import org.jtool.macrorecorder.macro.CopyMacro;
import org.jtool.macrorecorder.macro.DocumentMacro;
import org.jtool.macrorecorder.macro.FileMacro;
import org.jtool.macrorecorder.macro.CodeCompletionMacro;
import org.jtool.macrorecorder.macro.RefactoringMacro;
import org.jtool.macrorecorder.macro.ResourceMacro;
import org.jtool.macrorecorder.macro.GitMacro;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Records change operations that were performed on the Eclipse' editor.
 * @author Katsuhisa Maruyama
 */
public class OperationRecorder {
    
    /**
     * The single instance of this operation recorder.
     */
    private static OperationRecorder instance = new OperationRecorder();
    
    /**
     * The macro receiver binding to this change operation recorder.
     */
    private MacroReceiver macroReceiver;
    
    /**
     * The repository that continuously stores recorded change operations.
     */
    private Repository onlineRepository = null;
    
    /**
     * A map that stores the collection of change operations for each file.
     */
    private Map<String, List<IChangeOperation>> operationMap = new HashMap<String, List<IChangeOperation>>();
    
    /**
     * A flag that indicates if recorded change operations are displayed on the console.
     */
    private boolean displayOperations;
    
    /**
     * Creates a singleton operation recorder.
     */
    private OperationRecorder() {
    }
    
    /**
     * Returns the single instance for the operation recorder.
     * @return the operation recorder
     */
    public static OperationRecorder getInstance() {
        return instance;
    }
    
    /**
     * Sets the receiver for change macros.
     * @param receiver the macro receiver
     */
    void setMacroReceiver(MacroReceiver receiver) {
        macroReceiver = receiver;
    }
    
    /**
     * Initializes this change operation recorder.
     */
    void initialize() {
        operationMap.clear();
        displayOperations = OperationRecorderPreferencePage.displayOperations();
        String location = OperationRecorderPreferencePage.getLocation();
        if (location == null || location.length() == 0) {
            OperationRecorderPreferencePage.setLocation(CTPreferencePage.getDefaultLoaction());
        }
        setRepository(OperationRecorderPreferencePage.getLocation());
    }
    
    /**
     * Terminate this change operation recorder.
     */
    void terminate() {
        storeAllChangeOerations();
        macroReceiver.stop();
        operationMap.clear();
    }
    
    /**
     * Sets the location of the repository.
     * @param location the location of the repository
     */
    void setRepository(String location) {
        if (location == null || location.length() == 0) {
            return;
        }
        
        onlineRepository = new Repository(location);
        RepositoryManager.getInstance().addOnlineRepository(onlineRepository);
    }
    
    /**
     * Changes the location of the main repository.
     * @param location the location of the main repository
     */
    void changeRepository(String location) {
        if (onlineRepository == null || location == null || location.length() == 0) {
            return;
        }
        
        boolean result = CTDialog.yesnoDialog("Repository Change", "Are you Ok to close all editors?");
        if (!result) {
            return;
        }
        storeAllChangeOerations();
        closeAllEditors();
        
        RepositoryManager.getInstance().removeOnlineRepository(onlineRepository);
        onlineRepository.clear();
        onlineRepository = new Repository(location);
        onlineRepository.collectFromHistoryFiles();
        RepositoryManager.getInstance().addOnlineRepository(onlineRepository);
    }
    
    /**
     * Closes all the editors that are visible on the page.
     */
    private void closeAllEditors() {
        IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = workbenchWindow.getActivePage();
        for (IEditorReference editorRef : page.getEditorReferences()) {
            IEditorPart editor = editorRef.getEditor(true);
            page.closeEditor(editor, true);
        }
    }
    
    /**
     * Records a macro.
     * @param macro the macro
     */
    void recordMacro(Macro macro) {
        ChangeOperation operation = createChangeOperation(macro);
        if (operation != null) {
            recordChangeOperation(operation);
        }
    }
    
    /**
     * Creates a change operation from a macro.
     * @param macro the received macro
     * @return the created change operation, or <code>null</code> when the creation is not needed
     */
    private ChangeOperation createChangeOperation(Macro macro) {
        if (macro instanceof CompoundMacro) {
            createOperation((CompoundMacro)macro);
        } else if (macro instanceof DocumentMacro) {
            return createOperation((DocumentMacro)macro);
        } else if (macro instanceof CopyMacro) {
            return createOperation((CopyMacro)macro);
        } else if (macro instanceof FileMacro) {
            return createOperation((FileMacro)macro);
        } else if (macro instanceof CommandMacro) {
            return createOperation((CommandMacro)macro);
        } else if (macro instanceof RefactoringMacro) {
            return null;
        } else if (macro instanceof ResourceMacro) {
            return createOperation((ResourceMacro)macro);
        } else if (macro instanceof CodeCompletionMacro) {
            return null;
        } else if (macro instanceof GitMacro) {
            return null;
        }
        return null;
    }
    
    /**
     * Creates change operations from a compound macro.
     * @param macro the compound macro
     */
    private void createOperation(CompoundMacro cmacro) {
        RefactoringOperation rop = null;
        if (cmacro.getCommandMacro() != null && cmacro.getCommandMacro().isRefactoring()) {
            for (Macro macro : cmacro.getMacros()) {
                if (macro instanceof RefactoringMacro) {
                    rop = createOperation((RefactoringMacro)macro);
                    if (rop != null) {
                        rop.setCompoundTime(cmacro.getCommandMacro().getTime());
                        recordChangeOperation(rop);
                    }
                    break;
                }
            }
        }
        
        for (Macro macro : cmacro.getMacros()) {
            ChangeOperation op = createChangeOperation(macro);
            if (op != null) {
                if (rop != null && op.isDocument()) {
                    if (rop.isUndo()) {
                        op.setAction(ICodeOperation.Action.REFACTORING_UNDO.toString());
                    } else if (rop.isRedo()) {
                        op.setAction(ICodeOperation.Action.REFACTORING_REDO.toString());
                    } else {
                        op.setAction(ICodeOperation.Action.REFACTORING.toString());
                    }
                }
                op.setCompoundTime(cmacro.getTime());
                recordChangeOperation(op);
            }
        }
    }
    
    /**
     * Creates a document operation from a document macro.
     * @param macro the document macro
     * @return the created operation, or <code>null</code> when an unknown macro was found
     */
    private ChangeOperation createOperation(DocumentMacro macro) {
        String action = "";
        if (macro.isNormalEdit()) {
            action = DocumentOperation.Action.TYPING.toString();
        } else if (macro.isCut()) {
            action = DocumentOperation.Action.CUT.toString();
        } else if (macro.isPaste()) {
            action = DocumentOperation.Action.PASTE.toString();
        } else if (macro.isUndo()) {
            action = DocumentOperation.Action.UNDO.toString();
        } else if (macro.isRedo()) {
            action = DocumentOperation.Action.REDO.toString();
        } else if (macro.isDiff()) {
            action = DocumentOperation.Action.CONTENT_CHANGE.toString();
        } else {
            return null;
        }
        
        CTPath pathinfo = new CTPath(macro.getProjectName(), macro.getPackageName(), macro.getFileName(),
                macro.getPath(), macro.getBranch());
        DocumentOperation op = new DocumentOperation(macro.getTime(), pathinfo, action);
        op.setStart(macro.getStart());
        op.setInsertedText(macro.getInsertedText());
        op.setDeletedText(macro.getDeletedText());
        return op;
    }
    
    /**
     * Creates a copy operation from a copy macro.
     * @param macro the copy macro
     * @return the created operation
     */
    private ChangeOperation createOperation(CopyMacro macro) {
        CTPath pathinfo = new CTPath(macro.getProjectName(), macro.getPackageName(), macro.getFileName(),
                macro.getPath(), macro.getBranch());
        CopyOperation op = new CopyOperation(macro.getTime(), pathinfo);
        op.setStart(macro.getStart());
        op.setCopiedText(macro.getCopiedText());
        return op;
    }
    
    /**
     * Creates a file operation from a file macro.
     * @param macro the file macro
     * @return the created operation, or <code>null</code> when an unknown macro was found
     */
    private ChangeOperation createOperation(FileMacro macro) {
        String action = "";
        if (macro.isAdd()) {
            action = FileOperation.Action.ADDED.toString();
        } else if (macro.isDelete()) {
            action = FileOperation.Action.REMOVED.toString();
        } else if (macro.isOpen()) {
            action = FileOperation.Action.OPENED.toString();
        } else if (macro.isClose()) {
            action = FileOperation.Action.CLOSED.toString();
        } else if (macro.isSave()) {
            action = FileOperation.Action.SAVED.toString();
        } else if (macro.isActivate()) {
            action = FileOperation.Action.ACTIVATED.toString();
        } else if (macro.isRefactor()) {
            action = FileOperation.Action.REFACTORED.toString();
        } else if (macro.isMoveFrom()) {
            action = FileOperation.Action.MOVED_FROM.toString();
        } else if (macro.isMoveTo()) {
            action = FileOperation.Action.MOVED_TO.toString();
        } else if (macro.isRenameFrom()) {
            action = FileOperation.Action.RENAMED_FROM.toString();
        } else if (macro.isRenameTo()) {
            action = FileOperation.Action.RENAMED_TO.toString();
        } else if (macro.isContentChange()) {
            action = FileOperation.Action.CONTENT_CHANGED.toString();
        } else if (macro.isGitAdded()) {
            action = FileOperation.Action.ADDED_GIT_INDEX_CHANGED.toString();
        } else if (macro.isGitRemoved()) {
            action = FileOperation.Action.REMOVED_GIT_INDEX_CHANGED.toString();
        } else if (macro.isGitModified()) {
            action = FileOperation.Action.MODIFIED_GIT_INDEX_CHANGED.toString();
        } else {
            return null;
        }
        
        CTPath pathinfo = new CTPath(macro.getProjectName(), macro.getPackageName(), macro.getFileName(),
                macro.getPath(), macro.getBranch());
        FileOperation op = new FileOperation(macro.getTime(), pathinfo, action.toString());
        op.setCode(macro.getCode());
        op.setCharset(macro.getCharset());
        op.setSrcDstPath(macro.getSrcDstPath());
        return op;
    }
    
    /**
     * Creates a command operation from a command macro.
     * @param macro the command macro
     * @return the created operation
     */
    private ChangeOperation createOperation(CommandMacro macro) {
        CTPath pathinfo = new CTPath(macro.getProjectName(), macro.getPackageName(), macro.getFileName(),
                macro.getPath(), macro.getBranch());
        CommandOperation op = new CommandOperation(macro.getTime(), pathinfo, CommandOperation.Action.EXECUTE.toString());
        op.setCommandId(macro.getCommandId());
        return op;
    }
    
    /**
     * Creates a refactoring operation from a refactoring macro.
     * @param macro the refactoring macro
     * @return the created operation, or <code>null</code> when an unknown or unneeded macro was found
     */
    private RefactoringOperation createOperation(RefactoringMacro macro) {
        String action = "";
        if (macro.isBegin()) {
            if (macro.isUndo()) {
                action = RefactoringOperation.Action.UNDONE.toString();
            } else if (macro.isRedo()) {
                action = RefactoringOperation.Action.REDONE.toString();
            } else {
                action = RefactoringOperation.Action.PERFORMED.toString();
            }
        } else {
            return null;
        }
        
        CTPath pathinfo = new CTPath(macro.getProjectName(), macro.getPackageName(), macro.getFileName(),
                macro.getPath(), macro.getBranch());
        RefactoringOperation op = new RefactoringOperation(macro.getTime(), pathinfo, action);
        op.setName(macro.getName());
        op.setSelectionStart(macro.getSelectionStart());
        op.setSelectedText(macro.getSelectionText());
        op.setArguments(macro.getArguments());
        return op;
    }
    
    /**
     * Creates a resource operation from a resource macro.
     * @param macro the resource macro
     * @return the created operation, or <code>null</code> when an unknown or unneeded macro was found
     */
    private ResourceOperation createOperation(ResourceMacro macro) {
        String action = "";
        if (macro.isAdd()) {
            action = ResourceOperation.Action.ADDED.toString();
        } else if (macro.isRemove()) {
            action = ResourceOperation.Action.REMOVED.toString();
        } else if (macro.isChange()) {
            action = ResourceOperation.Action.CHANGED.toString();
        } else if (macro.isRenameFrom()) {
            action = ResourceOperation.Action.RENAMED_FROM.toString();
        } else if (macro.isRenameTo()) {
            action = ResourceOperation.Action.RENAMED_TO.toString();
        } else if (macro.isMoveFrom()) {
            action = ResourceOperation.Action.MOVED_FROM.toString();
        } else if (macro.isMoveTo()) {
            action = ResourceOperation.Action.MOVED_TO.toString();
        }  else {
            return null;
        }
        
        String target = ResourceOperation.Target.NONE.toString();
        if (macro.isFileChange()) {
            target = ResourceOperation.Target.FILE.toString();
        } else if  (macro.isPackageChange()) {
            target = ResourceOperation.Target.PACKAGE.toString();
        } else if  (macro.isProjectChange()) {
            target = ResourceOperation.Target.PROJECT.toString();
        }  else {
            return null;
        }
        
        CTPath pathinfo = new CTPath(macro.getProjectName(), macro.getPackageName(), macro.getFileName(),
                macro.getPath(), macro.getBranch());
        ResourceOperation op = new ResourceOperation(macro.getTime(), pathinfo, action, target);
        op.setTarget(target);
        op.setSrcDstPath(macro.getSrcDstPath());
        return op;
    }
    
    /**
     * Records a code change operation into the workspace.
     * @param operation the operation to be stored
     */
    private void recordChangeOperation(ChangeOperation op) {
        if (op == null || op.getPath() == null || op.getFileName().length() == 0) {
            return;
        }
        
        String key = op.getPath() + "#" + op.getBranch();
        List<IChangeOperation> ops = operationMap.get(key);
        if (ops == null) {
            ops = new ArrayList<IChangeOperation>();
            operationMap.put(key, ops);
        }
        ops.add(op);
        print(op);
        if (op.isFile()) {
            FileOperation fop = (FileOperation)op;
            if (fop.isDelete() || fop.isClose() || fop.isSave()) {
                storeChangeOerations(key);
            }
        }
        
    }
    
    /**
     * Stores change operations into the repository.
     * @param key the key that specifies a file related to the change operations to be stored
     */
    private void storeChangeOerations(String key) {
        List<IChangeOperation> ops = operationMap.get(key);
        onlineRepository.storeChangeOperations(ops);
        ops.clear();
    }
    
    /**
     * Stores all the change operations into the repository.
     */
    private void storeAllChangeOerations() {
        for (List<IChangeOperation> ops : operationMap.values()) {
            onlineRepository.storeChangeOperations(ops);
            ops.clear();
        }
    }
    
    /**
     * Sets the flag that indicates if recorded change operations will be displayed.
     * @return <code>true</code> if the displaying is required, otherwise <code>false</code>
     */
    void displayOperations(boolean bool) {
        displayOperations = bool;
    }
    
    /**
     * Sends a code change operation event to all the listeners.
     * @param op the code change operation
     */
    private void print(IChangeOperation op) {
        if (displayOperations) {
            CTConsole.println(op.toString());
        }
    }
}
