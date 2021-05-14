package models;

/*
 * All files for tree were obtained from:
 *  from http://www.hameister.org/JavaSwingTreeTable.html
 *  Jörn Harmesteir
 *  
 *  Little changes were done to adopt the source to this project
 * 
 */

import java.util.Collections;
import java.util.Date;
import java.util.List;

import renderer_editor.TextButtonCell;
 
public class MyDataNode {
 
    private String name;
    private int extract;
    private int progress;
    private TextButtonCell out;
    private String in;
    
    private List<MyDataNode> children;
 
    public MyDataNode(String name, int extract, TextButtonCell comp, int progress, List<MyDataNode> children, String path_in) {
        this.name = name;
        this.extract = extract;
        this.progress = progress;
        this.out = comp;
        this.children = children;
        this.in = path_in;
 
        if (this.children == null) {
            this.children = Collections.emptyList();
        }
    }
 
    public String getName() {
        return name;
    }
 
    public int getExtract() {
        return extract;
    }
 
    public int getProgress() {
        return progress;
    }
 
    public TextButtonCell getPathOut() {
        return out;
    }
 
    public String getFullPathIn(){
    	return in;
    }
    
    public List<MyDataNode> getChildren() {
        return children;
    }
    
    public int getChildrenCount() {
        return children.size();
    }
 
    public void setExtract(int value) {
        extract = value;
    }
    
    public void setProgress(int value) {
    	progress = value;
    }
    
    public void setPathOut(TextButtonCell value) {
    	out = value;
    }
    
    public void setFullPathIn(String value) {
    	in = value;
    }
     
    /**
     * Knotentext vom JTree.
     */
    public String toString() {
        return name;
    }

}