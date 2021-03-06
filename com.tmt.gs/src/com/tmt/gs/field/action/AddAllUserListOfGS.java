package com.tmt.gs.field.action;

import java.util.Iterator;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import com.tmt.gs.nls.Messages;
import com.mobnut.db.model.PrimaryObject;
import com.mongodb.BasicDBList;
import com.sg.business.model.User;
import com.sg.widgets.MessageUtil;
import com.sg.widgets.commons.selector.DropdownNavigatorSelector;
import com.sg.widgets.part.editor.fields.AbstractFieldPart;
import com.sg.widgets.part.editor.fields.value.IAddTableItemHandler;

/***
 * 同{@link com.sg.business.commons.field.action.AddParticipateFromAllUser}一样,为什么还要重写
 * @author Administrator
 *
 */

@Deprecated
public class AddAllUserListOfGS implements IAddTableItemHandler {

	public AddAllUserListOfGS() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean addItem(final BasicDBList inputData, final AbstractFieldPart part) {
		PrimaryObject master = part.getInput().getData();
		DropdownNavigatorSelector ns = new DropdownNavigatorSelector(
				"organization.user.selector") { //$NON-NLS-1$
			@Override
			protected void doOK(IStructuredSelection is) {
				if(is==null||is.isEmpty()){
					MessageUtil.showToast(Messages.get().AddAllUserList_1, SWT.ICON_WARNING);
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
