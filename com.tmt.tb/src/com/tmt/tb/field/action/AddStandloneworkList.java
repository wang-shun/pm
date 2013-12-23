package com.tmt.tb.field.action;

import java.util.Iterator;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;

import com.mobnut.db.model.PrimaryObject;
import com.mongodb.BasicDBList;
import com.sg.business.model.WorkDefinition;
import com.sg.widgets.MessageUtil;
import com.sg.widgets.commons.selector.DropdownNavigatorSelector;
import com.sg.widgets.part.editor.fields.AbstractFieldPart;
import com.sg.widgets.part.editor.fields.value.IAddTableItemHandler;
import com.tmt.tb.nls.Messages;

public class AddStandloneworkList implements IAddTableItemHandler {

	public AddStandloneworkList() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean addItem(final BasicDBList inputData, final AbstractFieldPart part) {
		PrimaryObject master = part.getInput().getData();
		DropdownNavigatorSelector ns = new DropdownNavigatorSelector(
				"organization.navigaor.internalusestandlonework") { //$NON-NLS-1$
			@Override
			protected void doOK(IStructuredSelection is) {
				if(is==null||is.isEmpty()){
					MessageUtil.showToast(Messages.get().AddStandloneworkList_1, SWT.ICON_WARNING);
				}else{
					Iterator<?> iter = is.iterator();
					while(iter.hasNext()){
						Object value = iter.next();
						if(value instanceof WorkDefinition){
							String workdid = ((WorkDefinition) value).get_id().toString();
							if(!inputData.contains(workdid)){
								inputData.add(workdid);
							}
						}
					}
					part.setDirty(true);
					part.updateDataValueAndPresent();
					super.doOK(is);
				}
			}
		};
		ns.setMaster(master);
		ns.show(part.getShell(),part.getControl(),true);
		return true;
	}

}
