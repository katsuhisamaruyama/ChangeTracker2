/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.dependencyanalyzer;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import java.util.Hashtable;
import java.util.List;

/**
 * Creates the abstract syntax tree (AST) of Java source code.
 * @author Katsuhisa Maruyama
 */
class CTParser {
    
    /**
     * The Java compilier information
     */
    @SuppressWarnings("deprecation")
    private static int JLS_LEVEL = AST.JLS8;
    private static String JC_VERSION = JavaCore.VERSION_1_8;
    
    /**
     * Creates a parser that parses Java source code.
     */
    CTParser() {
    }
    
    /**
     * Parses Java source code its string form.
     * @param contents the contents of the Java source code
     * @return the compilation unit if the parsing succeeds, otherwise <code>null</code>
     */
    CompilationUnit parse(String contents) {
        return parse(contents.toCharArray());
    }
    
    /**
     * Parses Java source code with its array form.
     * @param contents the contents of the Java source code
     * @return the compilation unit if the parsing succeeds, otherwise <code>null</code>
     */
    private CompilationUnit parse(char[] contents) {
        ASTParser parser = ASTParser.newParser(JLS_LEVEL);
        
        Hashtable<String, String> options = (Hashtable<String, String>)JavaCore.getOptions();
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JC_VERSION);
        options.put(JavaCore.COMPILER_SOURCE, JC_VERSION);
        options.put(JavaCore.COMPILER_COMPLIANCE, JC_VERSION);
        parser.setCompilerOptions(options);
        parser.setSource(contents);
        parser.setResolveBindings(false);
        ASTNode node = parser.createAST(null);
        if (node instanceof CompilationUnit) {
            CompilationUnit cu = (CompilationUnit)node;
            // if (!((compilationUnit.getFlags() & ASTNode.MALFORMED) == ASTNode.MALFORMED)) {
            for (IProblem problem : cu.getProblems()) {
                if (problem.isError()) {
                    return null;
                }
            }
            return cu;
        }
        return null;
    }
    
    /**
     * Returns all the Java constructs appearing in the AST.
     * @param cu the compilation unit corresponding to the AST
     * @param time the time of the snapshot for the AST
     * @param finfo file information about the AST
     * @return the collection of the Java constructs
     */
    List<JavaConstruct> getJavaConstructs(CompilationUnit cu) {
        assert cu != null;
        CTVisitor visitor = new CTVisitor(true);
        cu.accept(visitor);
        return visitor.getJavaConstructs();
    }
}
