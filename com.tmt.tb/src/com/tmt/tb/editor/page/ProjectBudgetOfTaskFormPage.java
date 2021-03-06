package com.tmt.tb.editor.page;

import java.text.DecimalFormat;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;

import com.mobnut.commons.util.Utils;
import com.sg.business.commons.ui.viewer.ProjectBudgetTreeContentProvider;
import com.sg.business.model.Project;
import com.sg.business.model.ProjectBudget;
import com.sg.widgets.part.CurrentAccountContext;
import com.sg.widgets.part.editor.PrimaryObjectEditorInput;
import com.sg.widgets.part.editor.page.IEditorPageLayoutProvider;
import com.sg.widgets.registry.config.BasicPageConfigurator;
import com.sg.widgets.registry.config.IPageDelegator;
import com.tmt.tb.nls.Messages;

public class ProjectBudgetOfTaskFormPage implements IPageDelegator, IFormPart {
	private TreeViewer viewer;
	private ProjectBudget root;
	private boolean isDirty;
	private IManagedForm form;
	
	//TODO ��Ҫ�޸�
	
	public ProjectBudgetOfTaskFormPage() {
	}

	@Override
	public IEditorPageLayoutProvider getPageLayout() {
		return null;
	}
	
	@Override
	public Composite createPageContent(IManagedForm mForm,Composite parent,
			PrimaryObjectEditorInput input, BasicPageConfigurator conf) {
		Project project = (Project) input.getData();

		root = project.getBudget();

		viewer = new TreeViewer(parent, SWT.FULL_SELECTION);
		viewer.getTree().setHeaderVisible(true);
		viewer.getTree().setLinesVisible(true);
		viewer.setContentProvider(new ProjectBudgetTreeContentProvider());

		TreeViewerColumn column = new TreeViewerColumn(viewer, SWT.LEFT);
		column.getColumn().setText(Messages.get().ProjectBudgetOfTaskFormPage_0);
		column.getColumn().setWidth(280);
		column.setLabelProvider(new ColumnLabelProvider());
		
		column = new TreeViewerColumn(viewer, SWT.RIGHT);
		column.getColumn().setText(Messages.get().ProjectBudgetOfTaskFormPage_1);
		column.getColumn().setWidth(120);
		column.setLabelProvider(new ColumnLabelProvider(){
			@Override
			public String getText(Object element) {
				ProjectBudget budget = (ProjectBudget)element;
				Double value = budget.getBudgetValue();
				if(value!=null){
					DecimalFormat df = new DecimalFormat(Utils.NF_RMB_MONEY);
					return df.format(value);
				}else{
					return ""; //$NON-NLS-1$
				}
			}
		});
		
		if(input.isEditable()){
			EditingSupport es = new EditingSupport(viewer) {
				
				@Override
				protected void setValue(Object element, Object value) {
					ProjectBudget budget = (ProjectBudget)element;
					Double val = Utils.getDoubleValue(value);
					
					budget.inputBudgetValue(val);
					setDirty(true);
					viewer.refresh();
				}
				
				@Override
				protected Object getValue(Object element) {
					ProjectBudget budget = (ProjectBudget)element;
					Double value = budget.getBudgetValue();
					if(value == null){
						return ""; //$NON-NLS-1$
					}else{
						return ""+value; //$NON-NLS-1$
					}
				}
				
				@Override
				protected CellEditor getCellEditor(Object element) {
					return new TextCellEditor(viewer.getTree());
				}
				
				@Override
				protected boolean canEdit(Object element) {
					ProjectBudget budget = (ProjectBudget)element;
					return !budget.hasChildren();
				}
			};
			column.setEditingSupport(es);
			
		}
		
		
		viewer.setInput(root.getChildren());
		viewer.expandAll();
		return (Composite) viewer.getControl();
	}


	@Override
	public IFormPart getFormPart() {
		return this;
	}

	@Override
	public void initialize(IManagedForm form) {
		this.form = form;
	}

	@Override
	public void dispose() {

	}

	@Override
	public boolean isDirty() {
		return isDirty;
	}
	
	private void setDirty(boolean b){
		isDirty = b;
		form.dirtyStateChanged();
	}

	@Override
	public void commit(boolean onSave) {
		if (!onSave) {
			try {
				root.doSave(new CurrentAccountContext());
			} catch (Exception e) {
			}
		} else {
			setDirty(false);
		}
	}


	@Override
	public boolean setFormInput(Object input) {
		return false;
	}

	@Override
	public void setFocus() {
		viewer.getTree().setFocus();
	}

	@Override
	public boolean isStale() {
		return false;
	}

	@Override
	public void refresh() {
	}

	@Override
	public boolean canRefresh() {
		return false;
	}
	@Override
	public boolean createBody() {
		return false;
	}
}
