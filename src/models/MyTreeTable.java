package models;

/*
 * All files for tree were obtained from:
 *  from http://www.hameister.org/JavaSwingTreeTable.html
 *  Jörn Harmesteir
 *  
 *  Little changes were done to adopt the source to this project
 * 
 */
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JCheckBox;
import javax.swing.JTable;

import renderer_editor.MyTreeTableCellEditor;
import renderer_editor.MyTreeTableCellRenderer;
 
public class MyTreeTable extends JTable {
 
    private MyTreeTableCellRenderer tree;
     
     
    public MyTreeTable(MyAbstractTreeTableModel treeTableModel) {
        super();
 
        // JTree erstellen.
        tree = new MyTreeTableCellRenderer(this, treeTableModel);
         
        // Modell setzen.
        super.setModel(new MyTreeTableModelAdapter(treeTableModel, tree));        
        
        // Gleichzeitiges Selektieren fuer Tree und Table.
        MyTreeTableSelectionModel selectionModel = new MyTreeTableSelectionModel();
        tree.setSelectionModel(selectionModel); //For the tree
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        
        setSelectionModel(selectionModel.getListSelectionModel()); //For the table

        // Renderer fuer den Tree.
        setDefaultRenderer(MyTreeTableModel.class, tree);
        // Editor fuer die TreeTable
        setDefaultEditor(MyTreeTableModel.class, new MyTreeTableCellEditor(tree, this));
	    
        // Kein Grid anzeigen.
        setShowGrid(false);
 
        // Keine Abstaende.
        setIntercellSpacing(new Dimension(0, 0));
 
    }
    
    public void Change_TreeTableModel(MyAbstractTreeTableModel treeTableModel){
    	tree = null;
        tree = new MyTreeTableCellRenderer(this, treeTableModel);
        
        // Modell setzen.
        super.setModel(new MyTreeTableModelAdapter(treeTableModel, tree));        
        
        // Gleichzeitiges Selektieren fuer Tree und Table.
        MyTreeTableSelectionModel selectionModel = new MyTreeTableSelectionModel();
        tree.setSelectionModel(selectionModel); //For the tree
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        
        setSelectionModel(selectionModel.getListSelectionModel()); //For the table

        // Renderer fuer den Tree.
        setDefaultRenderer(MyTreeTableModel.class, tree);
        // Editor fuer die TreeTable
        setDefaultEditor(MyTreeTableModel.class, new MyTreeTableCellEditor(tree, this));
	    
        // Kein Grid anzeigen.
        setShowGrid(false);
 
        // Keine Abstaende.
        setIntercellSpacing(new Dimension(0, 0));

    }
}