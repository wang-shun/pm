package com.sg.business.management.editor.page;

import java.util.ArrayList;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeColumn;

import com.mobnut.db.model.IPrimaryObjectEventListener;
import com.mobnut.db.model.PrimaryObject;
import com.sg.business.model.AbstractOptionFilterable;
import com.sg.business.model.ProjectTemplate;
import com.sg.business.model.WorkDefinition;
import com.sg.business.resource.nls.Messages;
import com.sg.widgets.part.CurrentAccountContext;
import com.sg.widgets.part.NavigatorControl;
import com.sg.widgets.part.editor.IEditorActionListener;
import com.sg.widgets.part.editor.PrimaryObjectEditorInput;
import com.sg.widgets.part.editor.page.INavigatorPageBodyPartCreater;
import com.sg.widgets.part.editor.page.NavigatorPage;
import com.sg.widgets.viewer.CTreeViewer;

public class DeliverablesBody implements INavigatorPageBodyPartCreater,
		IPrimaryObjectEventListener {

	private static final String IS_OPTION_COLUMN = "isOptionColumn"; //$NON-NLS-1$
	private ProjectTemplate projectTemplate;
	private CTreeViewer viewer;

	@Override
	public void createNavigatorBody(Composite body, NavigatorControl navi,
			PrimaryObjectEditorInput input, NavigatorPage page) {
		navi.createPartContent(body);
		// 创建选项列
		// 读取选择项
		viewer = (CTreeViewer) navi.getViewer();
		if (projectTemplate == null) {
			projectTemplate = (ProjectTemplate) input.getData();
			projectTemplate.addEventListener(this);
		}

		reloadOptionsColumns();

		IToolBarManager manager = page.getToolBarManager();
		manager.add(new PreviewOptionAction(projectTemplate));
		body.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent event) {
				projectTemplate.removeEventListener(DeliverablesBody.this);				
			}
		});
	}

	private void reloadOptionsColumns() {
		TreeColumn[] columns = viewer.getTree().getColumns();
		for (TreeColumn treeColumn : columns) {
			if(Boolean.TRUE.equals(treeColumn.getData(IS_OPTION_COLUMN))){
				treeColumn.dispose();
			}
		}
		
		ArrayList<?> standardSet = (ArrayList<?>) projectTemplate
				.getStandardOptionSet();
		createColumns(standardSet, viewer, Messages.get().DeliverablesBody_1);

		ArrayList<?> productTypeSet = (ArrayList<?>) projectTemplate
				.getProductOptionSet();
		createColumns(productTypeSet, viewer, Messages.get().DeliverablesBody_2);

		ArrayList<?> projectTypeSet = (ArrayList<?>) projectTemplate
				.getProjectOptionSet();
		createColumns(projectTypeSet, viewer, Messages.get().DeliverablesBody_3);
	}

	private void createColumns(ArrayList<?> set, final CTreeViewer viewer,
			final String optionSetName) {
		if (set == null) {
			return;
		}
		TreeViewerColumn col;
		for (int i = 0; i < set.size(); i++) {
			final String optionName = (String) set.get(i);
			col = new TreeViewerColumn(viewer, SWT.CENTER);
			col.getColumn().setData(IS_OPTION_COLUMN, Boolean.TRUE);
			if (i == 0) {
				col.getColumn().setText(optionSetName + "\n" + optionName); //$NON-NLS-1$
			} else {
				col.getColumn().setText("\n" + optionName); //$NON-NLS-1$
			}
			col.getColumn().setWidth(80);
			col.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					if (element instanceof AbstractOptionFilterable) {
						return ((AbstractOptionFilterable) element)
								.getOptionValueSetting(optionSetName,
										optionName);
					}
					return ""; //$NON-NLS-1$
				}
			});
			final CellEditor editor = new ComboBoxCellEditor(viewer.getTree(),
					AbstractOptionFilterable.VALUE_SET, SWT.READ_ONLY);

			col.setEditingSupport(new EditingSupport(viewer) {

				@Override
				protected void setValue(Object element, Object value) {
					if (value instanceof Integer) {
						try {
							String setValue = AbstractOptionFilterable.VALUE_SET[((Integer) value)
									.intValue()];
							AbstractOptionFilterable item = (AbstractOptionFilterable) element;
							item.doSetOptionValueSetting(optionSetName,
									optionName, setValue,
									new CurrentAccountContext());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					viewer.update(element, null);
				}

				@Override
				protected Object getValue(Object element) {
					String value = ((AbstractOptionFilterable) element)
							.getOptionValueSetting(optionSetName, optionName);
					for (int i = 0; i < AbstractOptionFilterable.VALUE_SET.length; i++) {
						if (AbstractOptionFilterable.VALUE_SET[i].equals(value)) {
							return new Integer(i);
						}
					}
					return new Integer(0);
				}

				@Override
				protected CellEditor getCellEditor(Object element) {
					//如果元素为跟工作，则不创建CellEditor
					if(element instanceof WorkDefinition){
						WorkDefinition workd=(WorkDefinition)element;
						if(workd.getParent()==null){
							return null;
						}
					}
					return editor;
				}

				@Override
				protected boolean canEdit(Object element) {
					return projectTemplate.canEdit(new CurrentAccountContext());
				}
			});
		}

	}

	@Override
	public void primaryObjectEvent(String code, PrimaryObject po) {
		if(IPrimaryObjectEventListener.UPDATED.equals(code)){
			reloadOptionsColumns();
			viewer.refresh();
		}
	}

	@Override
	public void editorAction(IEditorActionListener reciever, int code,
			Object object) {
	}

}
