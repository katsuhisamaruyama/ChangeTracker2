/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.convert;

import org.jtool.changetracker.repository.CTPath;
import difflib.Chunk;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import java.util.List;
import java.util.Arrays;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Applies an change operation into code.
 * @author Katsuhisa Maruyama
 */
public class DiffGenerator {
    
    public static void generate(ZonedDateTime time, CTPath pathinfo, String otext, String ntext) {
        List<String> oldLines = Arrays.asList(otext.split("\n"));
        List<String> newLines = Arrays.asList(ntext.split("\n"));
        
        int gap = 0;
        Patch patch = DiffUtils.diff(oldLines, newLines);
        for (Delta delta : patch.getDeltas()) {
            time = time.plus(1, ChronoUnit.NANOS);
            
            int start = getStart(oldLines, delta.getOriginal().getPosition());
            String dtext = getText(delta.getOriginal());
            String itext = getText(delta.getRevised());
            
            DiffOperation op = new DiffOperation(time, pathinfo);
            op.setStart(start + gap);
            op.setInsertedText(itext);
            op.setDeletedText(dtext);
            
            gap = gap + itext.length() - dtext.length();
            
            System.err.print(op.toXML());
            /*
            System.out.println("OP = " + op.toString());
            System.out.println(String.format("%d", delta.getOriginal().getPosition() + 1));
            System.out.print(dtext);
            System.out.println("----");
            System.out.println(String.format("%d ", delta.getRevised().getPosition() + 1));
            System.out.print(itext);
            System.out.println();
            */
        }
    }
    
    private static String getText(Chunk chunk) {
        StringBuilder buf = new StringBuilder();
        for (Object line : chunk.getLines()) {
            buf.append(line.toString());
            buf.append("\n");
        }
        return buf.toString();
    }
    
    private static int getStart(List<String> oldLines, int line) {
        int offset = 0;
        for (int index = 0; index < line; index++) {
            offset = offset + oldLines.get(index).length() + 1;
        }
        return offset;
    }
}
