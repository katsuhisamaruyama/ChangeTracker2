/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.recorder;

import org.jtool.changetracker.core.ChangeTrackerConsole;
import org.jtool.changetracker.operation.IChangeOperation;
import org.jtool.changetracker.operation.ICodeOperation;
import org.jtool.changetracker.operation.ChangeOperation;
import org.jtool.changetracker.operation.DocumentOperation;
import org.jtool.changetracker.operation.CopyOperation;
import org.jtool.changetracker.operation.FileOperation;
import org.jtool.changetracker.operation.CommandOperation;
import org.jtool.changetracker.operation.RefactoringOperation;
import org.jtool.changetracker.repository.RepositoryManager;
import org.jtool.changetracker.repository.RepositoryChangedEvent;
import org.jtool.changetracker.repository.RepositoryChangedListener;
import org.jtool.changetracker.repository.ChangeTrackerPath;
import org.jtool.macrorecorder.recorder.IMacroRecorder;
import org.jtool.macrorecorder.recorder.MacroRecorder;
import org.jtool.macrorecorder.recorder.IMacroListener;
import org.jtool.macrorecorder.recorder.MacroEvent;
import org.jtool.macrorecorder.macro.Macro;
import org.jtool.macrorecorder.macro.CodeCompletionMacro;
import org.jtool.macrorecorder.macro.CommandMacro;
import org.jtool.macrorecorder.macro.CompoundMacro;
import org.jtool.macrorecorder.macro.CopyMacro;
import org.jtool.macrorecorder.macro.DocumentMacro;
import org.jtool.macrorecorder.macro.FileMacro;
import org.jtool.macrorecorder.macro.RefactoringMacro;
import org.jtool.macrorecorder.macro.GitMacro;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Records change operations that were performed on the Eclipse' editor.
 * @author Katsuhisa Maruyama
 */
public class ChangeOperationRecorder implements IMacroListener, RepositoryChangedListener {
    
    /**
     * The single instance that records change operations.
     */
    private static ChangeOperationRecorder instance = new ChangeOperationRecorder();
    
    /**
     * A recorder that records macros.
     */
    private IMacroRecorder macroRecorder;
    
    /**
     * The collection of listeners that receives change operations.
     */
    private List<IChangeOperationListener> listeners = new ArrayList<IChangeOperationListener>();
    
    /**
     * A map that stores the collection of change operations for each file.
     */
    private Map<String, List<IChangeOperation>> operationMap = new HashMap<String, List<IChangeOperation>>();
    
    /**
     * A flag that indicates if this recorder is running.
     */
    private boolean running = false;
    
    /**
     * A flag that indicates if recorded change operations are displayed on the console.
     */
    private boolean displayOperation = false;
    
    /**
     * Creates an object that records change operations.
     */
    private ChangeOperationRecorder() {
        this.macroRecorder = MacroRecorder.getInstance();
    }
    
    /**
     * Returns the single instance that records change operations.
     * @return the change operation record
     */
    public static ChangeOperationRecorder getInstance() {
        return instance;
    }
    
    /**
     * Starts the recording of change operations.
     */
    public void start() {
        operationMap.clear();
        running = true;
        
        macroRecorder.addMacroListener(this);
        macroRecorder.start();
    }
    
    /**
     * Stops the recording of code change operations.
     */
    public void stop() {
        macroRecorder.removeMacroListener(this);
        macroRecorder.stop();
        
        storeAllChangeOerations();
        operationMap.clear();
        running = false;
    }
    
    /**
     * Tests if this recorder is running.
     * @return <code>true</code> if this recorder is running, otherwise <code>false</code>
     */
    public boolean isRunning() {
        return running;
    }
    
    /**
     * Receives a repository changed event.
     * @param evt the received event
     */
    @Override
    public void notify(RepositoryChangedEvent evt) {
        RepositoryChangedEvent.Type type = evt.getType();
        if (type.equals(RepositoryChangedEvent.Type.ABOUT_TO_LOCATION_CHANGE) ||
            type.equals(RepositoryChangedEvent.Type.ABOUT_TO_REFRESH)) {
            storeAllChangeOerations();
        }
    }
    
    /**
     * Receives a macro event when a new macro is added.
     * @param evt the macro event
     */
    @Override
    public void macroAdded(MacroEvent evt) {
        Macro macro = evt.getMacro();
        ChangeOperation operation = createChangeOperation(macro);
        if (operation != null) {
            recordChangeOperation(operation);
        }
    }
    
    /**
     * Receives a macro event when a new raw macro is added.
     * @param evt the raw macro event
     */
    @Override
    public void rawMacroAdded(MacroEvent evt) {
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
        } else if (macro instanceof CodeCompletionMacro) {
            return createOperation((CodeCompletionMacro)macro);
        } else if (macro instanceof FileMacro) {
            return createOperation((FileMacro)macro);
        } else if (macro instanceof CommandMacro) {
            return createOperation((CommandMacro)macro);
        } else if (macro instanceof GitMacro) {
            return createOperation((GitMacro)macro);
        }
        return null;
    }
    
    /**
     * Creates change operations from a compound macro.
     * @param macro the compound macro
     */
    private void createOperation(CompoundMacro cmacro) {
        long compoundId = cmacro.getTimeAsLong();
        
        RefactoringOperation rop = null;
        if (cmacro.getCommandMacro().isRefactoring()) {
            for (Macro macro : cmacro.getMacros()) {
                if (macro instanceof RefactoringMacro) {
                    rop = createOperation((RefactoringMacro)macro);
                    recordChangeOperation(rop);
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
                op.setCompoundId(compoundId);
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
        
        ChangeTrackerPath pathinfo = new ChangeTrackerPath(macro.getProjectName(), macro.getPackageName(), macro.getFileName(),
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
        ChangeTrackerPath pathinfo = new ChangeTrackerPath(macro.getProjectName(), macro.getPackageName(), macro.getFileName(),
                macro.getPath(), macro.getBranch());
        CopyOperation op = new CopyOperation(macro.getTime(), pathinfo);
        op.setStart(macro.getStart());
        op.setCopiedText(macro.getCopiedText());
        return op;
    }
    
    /**
     * Creates a command operation from a code completion macro.
     * @param macro the code completion macro
     * @return the created operation, or <code>null</code> when an unknown macro was found
     */
    private ChangeOperation createOperation(CodeCompletionMacro macro) {
//        String action = "";
//        if (macro.isContentAssistEnd()) {
//            action = CommandOperation.Action.CONTENT_ASSIST.toString();
//        } else if (macro.isQuickAssistEnd()) {
//            action = CommandOperation.Action.QUICK_ASSIST.toString();
//        } else {
//            return null;
//        }
//        ChangeTrackerPath pathinfo = new ChangeTrackerPath(macro.getProjectName(), macro.getPackageName(), macro.getFileName(),
//                macro.getPath(), macro.getBranch());
//        CommandOperation op = new CommandOperation(macro.getTime(), pathinfo, action);
//        op.setName(macro.getName());
//        return op;
        return null;
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
            action = FileOperation.Action.MOVED_FROM.toString();
        } else if (macro.isRenameTo()) {
            action = FileOperation.Action.RENAMED_TO.toString();
        } else if (macro.isContentChange()) {
            action = FileOperation.Action.CONTENT_CHANGED.toString();
        } else {
            return null;
        }
        ChangeTrackerPath pathinfo = new ChangeTrackerPath(macro.getProjectName(), macro.getPackageName(), macro.getFileName(),
                macro.getPath(), macro.getBranch());
        FileOperation op = new FileOperation(macro.getTime(), pathinfo, action.toString());
        op.setCode(macro.getCode());
        return op;
    }
    
    /**
     * Creates a command operation from a command macro.
     * @param macro the command macro
     * @return the created operation
     */
    private ChangeOperation createOperation(CommandMacro macro) {
        ChangeTrackerPath pathinfo = new ChangeTrackerPath(macro.getProjectName(), macro.getPackageName(), macro.getFileName(),
                macro.getPath(), macro.getBranch());
        CommandOperation op = new CommandOperation(macro.getTime(), pathinfo, CommandOperation.Action.EXECUTION.toString());
        op.setName(macro.getCommandId());
        return op;
    }
    
    /**
     * Creates a file operation from agit macro.
     * @param macro the git macro
     * @return the created operation
     */
    private ChangeOperation createOperation(GitMacro macro) {
        //Not implemented yet
        return null;
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
                action = RefactoringOperation.Action.UNDO.toString();
            } else if (macro.isRedo()) {
                action = RefactoringOperation.Action.REDO.toString();
            } else {
                action = RefactoringOperation.Action.EXECUTION.toString();
            }
        } else {
            return null;
        }
        ChangeTrackerPath pathinfo = new ChangeTrackerPath(macro.getProjectName(), macro.getPackageName(), macro.getFileName(),
                macro.getPath(), macro.getBranch());
        RefactoringOperation op = new RefactoringOperation(macro.getTime(), pathinfo, action);
        op.setName(macro.getName());
        op.setArguments(macro.getArguments());
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
        notify(op);
        if (op.isFile()) {
            FileOperation fop = (FileOperation)op;
            if (fop.isRemoved() || fop.isClosed() || fop.isSaved()) {
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
        RepositoryManager.getInstance().storeChangeOperations(ops);
        ops.clear();
    }
    
    /**
     * Stores all the change operations into the repository.
     */
    private void storeAllChangeOerations() {
        for (List<IChangeOperation> ops : operationMap.values()) {
            RepositoryManager.getInstance().storeChangeOperations(ops);
            ops.clear();
        }
    }
    
    /**
     * Adds a listener that receives a code change operation.
     * @param listener the listener to be added
     */
    public void addOperationEventListener(IChangeOperationListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Removes a listener that no longer receives a change operation.
     * @param listener the listener to be removed
     */
    public void removeOperationEventListener(IChangeOperationListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Sets a flag that indicate if change operations are displayed on the console.
     * @param bool <code>true</code> if recorded change operations are displayed, otherwise <code>false</code>
     */
    public void displayOperationsOnConsole(boolean bool) {
        displayOperation = bool;
    }
    
    /**
     * Sends a code change operation event to all the listeners.
     * @param op the code change operation
     */
    protected void notify(IChangeOperation op) {
        if (displayOperation) {
            ChangeTrackerConsole.println(op.toString());
        }
        for (IChangeOperationListener listener : listeners) {
            listener.operationAdded(op);
        }
    }
}
