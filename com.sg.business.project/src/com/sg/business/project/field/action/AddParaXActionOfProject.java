package com.sg.business.project.field.action;

import java.util.Iterator;

import org.bson.types.ObjectId;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.TreeItem;

import com.mobnut.db.model.PrimaryObject;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.sg.business.model.IWorkCloneFields;
import com.sg.business.model.Project;
import com.sg.business.model.Work;
import com.sg.business.model.WorkTimeProgram;
import com.sg.widgets.commons.selector.DropdownNavigatorSelector;
import com.sg.widgets.part.editor.fields.AbstractFieldPart;
import com.sg.widgets.part.editor.fields.value.IAddTableItemHandler;
import com.sg.widgets.viewer.CTreeViewer;

public class AddParaXActionOfProject implements IAddTableItemHandler {

	public AddParaXActionOfProject() {
	}

	@Override
	public boolean addItem(final BasicDBList inputData,
			final AbstractFieldPart part) {
		// �����༭����������ݣ�����Ϊ����
		Work data = (Work) part.getInput().getData();
		PrimaryObject master = null;
		if (data.isStandloneWork()) {
			// �˹�������Ϊ������������ʱ��masterֱ��ʹ�ù������壬��Ϊ������������ʹ���˹�ʱ�����ֶΣ������������õĹ�ʱ����
			master = data;
		} else if (data.isGenericWork()) {
			master = data;
		} else {
			// �˹�������Ϊ��Ŀ�еĹ�������ʱ����ʹ����Ŀģ���ȡ��Ŀģ���еĹ�ʱ����
			// ��ȡ��������������Ŀģ��
			Project project = data.getProject();
			master = project.getWorkTimeProgram();
		}

		DropdownNavigatorSelector ns = new DropdownNavigatorSelector(
				"project.selectworktimetype") {
			@Override
			protected boolean isSelectEnabled(IStructuredSelection is) {
				return is != null && !is.isEmpty()
						&& is.getFirstElement() instanceof DBObject;
			}

			@Override
			protected void doOK(IStructuredSelection is) {
				if (is == null || is.isEmpty()) {
				} else {
					Iterator<?> iterator = is.iterator();
					while (iterator.hasNext()) {
						// �˴�valueֻ����DBObject�Ĺ�ʱ����
						DBObject value = (DBObject) iterator.next();
						ObjectId paraXId = (ObjectId) value
								.get(WorkTimeProgram.F__ID);

						CTreeViewer viewer = (CTreeViewer) getNavigator()
								.getViewer();
						TreeItem treeItem = (TreeItem) viewer
								.testFindItem(value);
						WorkTimeProgram workTimeProgram = (WorkTimeProgram) treeItem
								.getParentItem().getData();
						ObjectId workTimeProgramId = workTimeProgram.get_id();
						DBObject key = new BasicDBObject()
								.append(IWorkCloneFields.F_WORKTIME_PARAX_ID,
										paraXId)
								.append(IWorkCloneFields.F_WORKTIME_PARAX_PROGRAM_ID,
										workTimeProgramId);
						// �жϱ����༭�����ֶε�ֵ�Ƿ������ʱ����id�͹�ʱ����id��ɵ��ַ���ʱ���еĴ���
						if (!inputData.contains(key)) {
							inputData.add(key);
						}
					}
					part.setDirty(true);
					part.updateDataValueAndPresent();
					super.doOK(is);
				}
			}
		};
		ns.setMaster(master);
		ns.show(part.getShell(), part.getControl(), true);
		return true;

	}

}