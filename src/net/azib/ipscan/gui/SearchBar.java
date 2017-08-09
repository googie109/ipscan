package net.azib.ipscan.gui;

import static net.azib.ipscan.gui.util.LayoutHelper.formData;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

public class SearchBar {
	
	private static SearchBar instance;
	
	private Text searchField;
	private Button searchPrevious; 
	private Button searchNext;
	
	private SearchBar() { }
	
	private SearchBar(Composite composite, Control parent){
		searchField = new Text(composite, SWT.BORDER);
		searchField.setLayoutData(formData(new FormAttachment(parent), new FormAttachment(94), new FormAttachment(0), new FormAttachment(100)));
		searchField.setText("");
		searchField.setToolTipText("Search here...");
		
		searchPrevious = new Button(composite, SWT.BORDER);
		searchPrevious.setLayoutData(formData(new FormAttachment(searchField), new FormAttachment(97), new FormAttachment(0), new FormAttachment(100)));
		searchPrevious.setText("<");
		
		searchNext = new Button(composite, SWT.BORDER);
		searchNext.setLayoutData(formData(new FormAttachment(searchPrevious), new FormAttachment(100), new FormAttachment(0), new FormAttachment(100)));
		searchNext.setText(">");
	}
	
	public static SearchBar create(Composite composite, Control parent){
		SearchBar searchBar = new SearchBar(composite, parent);
		instance = searchBar;
		return searchBar;
	}
	
	public static String getText() {
		return getInstance().searchField.getText();
	}
	
	public static SearchBar getInstance(){
		if(instance == null){
			throw new NullPointerException("You must call create() first! There is no instance of SearchBar!");
		}
		return instance;
	}
	
	
}
