package renderer_editor;

/*
 * All files for tree were obtained from:
 *  from http://www.hameister.org/JavaSwingTreeTable.html
 *  Jörn Harmesteir
 *  
 *  Little changes were done to adopt the source to this project
 * 
 */

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import models.MyTreeTable;
 
 
public class MyTreeTableCellRenderer extends JTree implements TableCellRenderer {
    /** Die letzte Zeile, die gerendert wurde. */
    protected int visibleRow;
     
    private MyTreeTable treeTable;
     
    public MyTreeTableCellRenderer(MyTreeTable treeTable, TreeModel model) {
        super(model);
        this.treeTable = treeTable;
        // Setzen der Zeilenhoehe fuer die JTable
        // Muss explizit aufgerufen werden, weil treeTable noch
        // null ist, wenn super(model) setRowHeight aufruft!

        setRowHeight(getRowHeight());
    }
 
    /**
     * Tree und Table muessen die gleiche Hoehe haben.
     */
    public void setRowHeight(int rowHeight) {
        if (rowHeight > 0) {
            super.setRowHeight(rowHeight);
            if (treeTable != null && treeTable.getRowHeight() != rowHeight) {
                treeTable.setRowHeight(getRowHeight());
            }
        }
    }
 
    /**
     * Tree muss die gleiche Hoehe haben wie Table.
     */
    public void setBounds(int x, int y, int w, int h) {
        super.setBounds(x, 0, w, treeTable.getHeight());
    }
 
    /**
     * Sorgt fuer die Einrueckung der Ordner.
     */
    public void paint(Graphics g) {
        g.translate(0, -visibleRow * getRowHeight());
         
        super.paint(g);
    }
     
    /**
     * Liefert den Renderer mit der passenden Hintergrundfarbe zurueck.
     */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected){
            setBackground(table.getSelectionBackground());        	
        }else{        	
            setBackground(table.getBackground());      
        }
        
        visibleRow = row;
/*
        Component field=null;

        switch (column) {
        	case 0:
                //field=new JTree();
        		return this;
        	case 1:
                field=new JCheckBox();
                ((JCheckBox) field).setSelected((value != null && ((Boolean) value).booleanValue()));
            case 2:
                field=new JProgressBar();
                ((JProgressBar) field).setValue(0);
            case 3:
                field=new JTextField();
                ((JTextField) field).setText(value.toString());
            default:
            	break;
        }
        
        return field;*/
        return this;
    }


}