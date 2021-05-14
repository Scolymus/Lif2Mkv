package main;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.SwingConstants;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import javax.swing.ListSelectionModel;
import java.awt.Dialog.ModalityType;

/*
 * 
 *   	<dependency>
  		<groupId>ome</groupId>
  		<artifactId>pom-bio-formats</artifactId>
  		<type>pom</type>
  		<version>6.3.0</version>
  	</dependency>
  	  	<dependency>
  		<groupId>ome</groupId>
  		<artifactId>bio-formats-tools</artifactId>
  		<version>6.3.0</version>
  	</dependency>
 * 
 * 
 * 
 * 
 */

public class Channels extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTable table;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			Channels dialog = new Channels();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ArrayList<String> getName_channels(){
		ArrayList<String> data = new ArrayList<String>(0);
		for (int i = 0; i<table.getModel().getRowCount(); i++){			
			data.add(table.getValueAt(i, 1).toString());	
		}		
		return data;
	}
	
	public void Load_channels(ArrayList<String> data){
		for (int i = 0; i<data.size(); i++){
			if (!data.get(i).equals(null)){
				table.setValueAt(data.get(i),i, 1);				
			}	
		}		
		table.repaint();
	}
	
	/**
	 * Create the dialog.
	 */
	public Channels() {
		setModalityType(ModalityType.TOOLKIT_MODAL);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);
		Window this_window = this.getWindows()[0];
		setTitle("Name of channels (modify only those needed)");
		setBounds(100, 100, 479, 266);
		getContentPane().setLayout(new BorderLayout());
		FlowLayout fl_contentPanel = new FlowLayout();
		fl_contentPanel.setHgap(0);
		contentPanel.setLayout(fl_contentPanel);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			JScrollPane scrollPane = new JScrollPane();
			contentPanel.add(scrollPane);
			{
				table = new JTable();
				table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				scrollPane.setViewportView(table);
				table.setModel(new DefaultTableModel(
					new Object[][] {
						{"1", "ch1"},
						{"2", "ch2"},
						{"3", "ch3"},
						{"4", "ch4"},
						{"5", "ch5"},
						{"6", "ch6"},
						{"7", "ch7"},
						{"8", "ch8"},
						{"9", "ch9"},
						{"10", "ch10"},
					},
					new String[] {
						"Channel", "Name"
					}
				) {
					Class[] columnTypes = new Class[] {
						String.class, String.class
					};
					public Class getColumnClass(int columnIndex) {
						return columnTypes[columnIndex];
					}
					boolean[] columnEditables = new boolean[] {
						false, true
					};
					public boolean isCellEditable(int row, int column) {
						return columnEditables[column];
					}
				});
				table.getColumnModel().getColumn(0).setResizable(false);
				table.getColumnModel().getColumn(0).setPreferredWidth(51);
				table.getColumnModel().getColumn(0).setMaxWidth(51);
				table.getColumnModel().getColumn(1).setPreferredWidth(269);
				table.getTableHeader().setReorderingAllowed(false);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dispatchEvent(new WindowEvent(this_window, WindowEvent.WINDOW_CLOSING));
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
	}

}
