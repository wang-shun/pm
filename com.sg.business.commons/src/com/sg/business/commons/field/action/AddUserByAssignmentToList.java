package com.sg.business.commons.field.action;

import java.util.Iterator;

import org.eclipse.jface.viewers.IStructuredSelection;

import com.mobnut.db.model.PrimaryObject;
import com.mongodb.BasicDBList;
import com.sg.business.model.User;
import com.sg.widgets.commons.selector.DropdownNavigatorSelector;
import com.sg.widgets.part.editor.fields.AbstractFieldPart;
import com.sg.widgets.part.editor.fields.value.IAddTableItemHandler;

public class AddUserByAssignmentToList implements IAddTableItemHandler {

	public AddUserByAssignmentToList() {
	}

	@Override
	public boolean addItem(final BasicDBList inputData, final AbstractFieldPart part) {
		PrimaryObject master = part.getInput().getData();
		DropdownNavigatorSelector ns = new DropdownNavigatorSelector(
				"organization.user.assignment") { //$NON-NLS-1$
			@Override
			protected void doOK(IStructuredSelection is) {
				if(is==null||is.isEmpty()){
				}else{
					Iterator<?> iter = is.iterator();
					while(iter.hasNext()){
						Object value = iter.next();
						if(value instanceof User){
							String userid = ((User) value).getUserid();
							if(!inputData.contains(userid)){
								inputData.add(userid);
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
