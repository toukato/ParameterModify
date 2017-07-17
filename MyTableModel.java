package burp;

import javax.swing.table.DefaultTableModel;

public class MyTableModel extends DefaultTableModel {
	
	public MyTableModel(Object[][] data, Object[] columNames) {
		// TODO Auto-generated constructor stub
		super(data, columNames);
	}
	
	public Class getColumnClass(int colum) {
		return getValueAt(0, colum).getClass();
	}

}
