/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.parser;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import java.util.Hashtable;

/**
 * Creates the abstract syntax tree (AST) of Java source code.
 * @author Katsuhisa Maruyama
 */
public class OpJavaParser {
    
    /**
     * The parser that parses Java source code.
     */
    private ASTParser parser;
    
    /**
     * The Eclipse's compilation unit under of the Java source code to be parsed.
     */
    private CompilationUnit compilationUnit = null;
    
    /**
     * Creates a parser that parses Java source code.
     */
    public OpJavaParser() {
        parser = ASTParser.newParser(AST.JLS8);
    }
    
    /**
     * Parses Java source code its string form.
     * @param contents the contents of the Java source code
     * @return <code>true</code> if the parsing succeeds, otherwise <code>false</code>
     */
    public boolean parse(String contents) {
        return parse(contents.toCharArray());
    }
    
    /**
     * Parses Java source code with its array form.
     * @param contents the contents of the Java source code
     * @return <code>true</code> if the parsing succeeds, otherwise <code>false</code>
     */
    private boolean parse(char[] contents) {
        Hashtable<String, String> options = (Hashtable<String, String>)JavaCore.getOptions();
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
        options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
        parser.setCompilerOptions(options);
        
        parser.setSource(contents);
        parser.setResolveBindings(false);
        
        ASTNode node = parser.createAST(null);
        if (node instanceof CompilationUnit) {
            compilationUnit = (CompilationUnit)node;
            // if (!((compilationUnit.getFlags() & ASTNode.MALFORMED) == ASTNode.MALFORMED)) {
            for (IProblem problem : compilationUnit.getProblems()) {
                if (problem.isError()) {
                    // System.out.println("SYNTAX ERROR " + problem.toString());
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    /**
     * Returns the problems resulting from the parsing.
     * @return the parse problems
     */
    public IProblem[] getProblems() {
        return compilationUnit.getProblems();
    }
     
    /**
     * Returns the compilation unit of the parsed Java source code.
     * @return the compilation unit
     */
    public CompilationUnit getCompilationUnit() {
        return compilationUnit;
    }
}
