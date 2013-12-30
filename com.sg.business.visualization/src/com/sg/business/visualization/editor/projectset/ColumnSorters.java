package com.sg.business.visualization.editor.projectset;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.widgets.MenuItem;

import com.sg.business.resource.BusinessResource;
import com.sg.widgets.ImageResource;
import com.sg.widgets.Widgets;

public class ColumnSorters {

	private String text;
	private MenuItem item;
	private StructuredViewer viewer;
	
	private static final int ASC = 1;
	private static final int DEC = -1;
	private static final int NONE = 0;
	
	private int seq;
//	private String columnLabel;
//	private TableColumn column;

	public ColumnSorters(String text) {
		this.text = text;
		seq = NONE;
	}

	public String getText() {
		return text;
	}

	public void setMenuItem(MenuItem item) {
		this.item = item;
	}



	private void resetMemuItem() {
		switch (seq) {
		case NONE:
			item.setImage(BusinessResource.getImage(BusinessResource.IMAGE_24X24_BLANK));
//			column.setText(columnLabel);
			break;
		case ASC:
			item.setImage(Widgets.getImage(ImageResource.UP_24));
//			column.setText(columnLabel + "["+text+" ��" +"]");
			break;
		case DEC:
			item.setImage(Widgets.getImage(ImageResource.DOWN_24));
//			column.setText(columnLabel + "["+text+" ��" +"]");
			break;
		default:
			break;
		}		
	}

	private void resetSorter() {
		viewer.setComparator(new ViewerComparator(){
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				return seq*doCompare(viewer, e1, e2);
			}
		});
	}

	protected int doCompare(Viewer viewer2, Object e1, Object e2) {
		return 0;
	}

	private void setSeq() {
		switch (seq) {
		case NONE:
			seq = DEC;
			break;
		case ASC:
			seq = NONE;
			break;
		case DEC:
			seq = ASC;
			break;
		default:
			break;
		}
	}

	public void setViewer(StructuredViewer viewer) {
		this.viewer = viewer;
	}

	public void sort() {
		setSeq();
		resetMemuItem();
		resetSorter();
	}

	public void removeSorter() {
		seq = NONE;
		resetMemuItem();
	}

//	public void setColumn(TableColumn tableColumn) {
//		this.column = tableColumn;
//		this.columnLabel = tableColumn.getText();
//	}

}
