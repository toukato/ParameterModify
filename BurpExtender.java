package burp;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

public class BurpExtender extends AbstractTableModel implements IBurpExtender, IHttpListener, ITab {

	private IBurpExtenderCallbacks callbacks;
	private IExtensionHelpers helpers;
	private JSplitPane splitPane;
	private JPanel jPanel;
	ArrayList<TableData> arrayList;
	TableData tableData1;
	
	@Override
	public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
		// TODO Auto-generated method stub
		this.callbacks = callbacks;
		helpers = callbacks.getHelpers();
		callbacks.setExtensionName("ParameterModify");
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				jPanel = new JPanel();
				
				Object[] columNames = {"ParameterNames", "URLEncode", "PartModyfy", "ModifyValues", "PartOrgValue", "PartModValue"};
				Object[][] data = {{new String(""), new Boolean(false), new Boolean(false), new String(""), new String(""), new String("")}};
				Object[] object = {new String(""), new Boolean(false), new Boolean(false), new String(""), new String(""), new String("")};
				
				DefaultTableModel defaultTableModel = new MyTableModel(data, columNames);
				
				JTable jTable = new JTable(defaultTableModel);
				JScrollPane jScrollPane = new JScrollPane(jTable);

				JCheckBox jCheckBox1 = new JCheckBox("Repeater");
				JCheckBox jCheckBox2 = new JCheckBox("Intruder");
				JCheckBox jCheckBox3 = new JCheckBox("Scanner");
				
				JButton jButton = new JButton("Add");
				jButton.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						defaultTableModel.addRow(object);
					}
				});
				
				JButton jButton2 = new JButton("Delete");
				jButton2.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						defaultTableModel.removeRow(jTable.getSelectedRow());
					}
				});
				
				jTable.getModel().addTableModelListener(new TableModelListener() {
					
					@Override
					public void tableChanged(TableModelEvent e) {
						// TODO Auto-generated method stub
						arrayList = new ArrayList<TableData>();
						int rowCount = jTable.getRowCount();
						
						tableData1 = new TableData();
						tableData1.setBoolRepeater(jCheckBox1.isSelected());
						tableData1.setBoolIntruder(jCheckBox2.isSelected());
						tableData1.setBoolScanner(jCheckBox3.isSelected());

						for (int i = 0; i < rowCount; i++) {
							try {
								
								TableData tableData = new TableData();
								tableData.setStrParaName(defaultTableModel.getValueAt(i, 0).toString());
								tableData.setBoolURLEncode((Boolean) defaultTableModel.getValueAt(i, 1));
								tableData.setBoolPartMod((Boolean) defaultTableModel.getValueAt(i, 2));
								tableData.setStrModValue(defaultTableModel.getValueAt(i, 3).toString());
								tableData.setStrPartOrgValue(defaultTableModel.getValueAt(i, 4).toString());
								tableData.setStrPartModValue(defaultTableModel.getValueAt(i, 5).toString());
								tableData.setTableCount(rowCount);
								arrayList.add(tableData);
								
							} catch (Exception e2) {
								// TODO: handle exception
								
							}
						}
					}
				});
				
				jPanel.add(jScrollPane);
				jPanel.add(jButton);
				jPanel.add(jButton2);
				
				jPanel.add(jCheckBox1);
				jPanel.add(jCheckBox2);
				jPanel.add(jCheckBox3);
				
				callbacks.customizeUiComponent(jPanel);
				callbacks.addSuiteTab(BurpExtender.this);
				callbacks.registerHttpListener(BurpExtender.this);
				
			}
		});
		
	}
	public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo) {
		
		Boolean blRepeater = tableData1.getBoolRepeater();
		Boolean blIntruder = tableData1.getBoolIntruder();
		Boolean blScanner = tableData1.getBoolScanner();
		
		if ((toolFlag == callbacks.TOOL_REPEATER && blRepeater == true) ||
				(toolFlag == callbacks.TOOL_INTRUDER && blIntruder == true) ||
				(toolFlag == callbacks.TOOL_SCANNER && blScanner == true)) {

			try {

				String searchValue = "";
				int startValue = 0;
				int endValue = 0;
				String setSearchName = "";
				String setReplaceValue = "";
				Boolean urlEncodeCheck = false;
				String getPartOrgValue = "";
				String setPartModValue = "";
				Boolean partModCheck = false;
				StringBuilder stringBuilder;
				String getReplaceValue = "";
				
				String request = new String(messageInfo.getRequest());
				
				for (int i = 0; i < arrayList.get(0).getTableCount(); i++) {
					setSearchName = arrayList.get(i).getStrParaName();
					urlEncodeCheck = arrayList.get(i).getBoolURLEncode();
					setReplaceValue = arrayList.get(i).getStrModValue();
					getPartOrgValue = arrayList.get(i).getStrPartOrgValue();
					setPartModValue = arrayList.get(i).getStrPartModValue();
					partModCheck = arrayList.get(i).getBoolPartMod();

					searchValue = setSearchName + "=";
					String scanLine = "";
					Scanner scanner = new Scanner(request);

					while (scanner.hasNextLine()) {
						scanLine = scanner.nextLine();
						if (scanLine.indexOf(searchValue) != -1) {
							startValue = request.indexOf(searchValue);
							
							StringBuilder cutScanLine = new StringBuilder(scanLine);
							cutScanLine.delete(0, scanLine.indexOf(searchValue));
							String checkIndexOf = new String(cutScanLine);

							int a = -1;
							int b = -1;
							int c = -1;

							if (checkIndexOf.indexOf("&") != -1) {
								a = checkIndexOf.indexOf("&") + scanLine.indexOf(searchValue);
							} else if (checkIndexOf.indexOf(";") != -1) {
								b = checkIndexOf.indexOf(";") + scanLine.indexOf(searchValue);
							} else {
								c = checkIndexOf.length() + scanLine.indexOf(searchValue);
							}

							int[] n = { a, b, c };
							for (int i1 = 0; i1 < n.length; i1++) {
								if (n[i1] > -1) {
									if (endValue < n[i1]) {
										endValue = n[i1] - scanLine.indexOf(searchValue) - searchValue.length();
									}
								}
							}
							startValue = startValue + searchValue.length();
							endValue = endValue + startValue;
						}
					}
					stringBuilder = new StringBuilder(request);
					
					if (partModCheck = true) {
						getReplaceValue = stringBuilder.substring(startValue, endValue);
						if (getReplaceValue.indexOf(getPartOrgValue) != -1) {
							StringBuilder builderReplaceValue = new StringBuilder(getReplaceValue);
							builderReplaceValue.replace(getReplaceValue.indexOf(getPartOrgValue), setPartModValue.length(), setPartModValue);
							setReplaceValue = new String(builderReplaceValue); 
						}
						
					} else {
						if (setReplaceValue.equals("") || setReplaceValue == null) {
							setReplaceValue = request.substring(startValue, endValue);
						}
					}

					if (urlEncodeCheck = true) {
						setReplaceValue = URLEncoder.encode(setReplaceValue, "UTF-8");
					}
					
					stringBuilder.replace(startValue, endValue, setReplaceValue);
					request = new String(stringBuilder);
				}
				messageInfo.setRequest(request.getBytes());
			} catch (Exception e) {
				// TODO: handle exception
				
			}
		}
	}
	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getTabCaption() {
		// TODO Auto-generated method stub
		return "ParameterModify";
	}
	@Override
	public Component getUiComponent() {
		// TODO Auto-generated method stub
		return jPanel;
	}
}