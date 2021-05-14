package renderer_editor;
import java.awt.Component;

import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.TableCellRenderer;
import java.awt.Color;

public class ProgressBarRenderer extends JProgressBar implements TableCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 20;
	
          public ProgressBarRenderer() {

        	  setForeground(Color.BLUE);
          }
          
          public Component getTableCellRendererComponent(JTable table, Object value, 
        		  boolean isSelected, boolean hasFocus, int row, int column) {
        	          	  try {
				UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
					| UnsupportedLookAndFeelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	  SwingUtilities.updateComponentTreeUI(this);
              int progress = 0;
              if (value instanceof Float) {
                  progress = Math.round(((Float) value));

                  if (progress>600){
                	  setForeground(new Color(0, 128, 0));
                  }else if (progress>400.){
                	  setForeground(Color.GREEN);
                  }else if (progress>100.){
                      setMinimum(100);
                      setMaximum(200);
                      setForeground(Color.ORANGE);
                  }else if (progress<100.){
                	  setMinimum(0);
                      setMaximum(100);
                      setForeground(Color.BLUE);
                  }
              } else if (value instanceof Integer) {
                  //progress = Integer.parseInt(value.toString());
            	  progress = (int) value;
                  if (progress>600){
                	  setForeground(new Color(0, 128, 0));
                  }else if (progress>400){
                	  setForeground(Color.GREEN);
                  }else if (progress>100){
                      setMinimum(100);
                      setMaximum(200);
                      setForeground(Color.ORANGE);
                  }else if (progress<100){
                	  setMinimum(0);
                      setMaximum(100);
                      setForeground(Color.BLUE);
                  }
              }      
              
              if (isSelected){
                  setBackground(table.getSelectionBackground());        	
              }else{        	
                  setBackground(table.getBackground());      
              }
              setValue(progress);
  			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
					| UnsupportedLookAndFeelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

              return this;
          }
}