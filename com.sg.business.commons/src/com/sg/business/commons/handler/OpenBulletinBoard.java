package com.sg.business.commons.handler;

import org.eclipse.core.commands.ExecutionEvent;

import com.mobnut.db.model.PrimaryObject;
import com.sg.business.model.BulletinBoard;
import com.sg.widgets.command.AbstractNavigatorHandler;
import com.sg.widgets.part.editor.DataObjectDialog;
import com.sg.widgets.part.editor.DataObjectEditor;

/**
 * �򿪰�ť����¼�
 * 
 * @author gdiyang
 * 
 */
public class OpenBulletinBoard extends AbstractNavigatorHandler {

	/**
	 * ����򿪰�ťִ���¼�
	 */
	@Override
	protected void execute(PrimaryObject selected, ExecutionEvent event) {
		// �жϵ�ǰѡ���Ƿ�Ϊ�����
		if (selected instanceof BulletinBoard) {
			// ��ȡ��ǰѡ��Ĺ����
			BulletinBoard bulletinboard = (BulletinBoard) selected;
			if (bulletinboard.getProjectId() != null) {
				try {
					DataObjectDialog.openDialog(bulletinboard,
							BulletinBoard.EDITOR_CREATE, true, null);
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else {
				try {
					DataObjectEditor.open(bulletinboard,
							BulletinBoard.EDITOR_CREATE, false, null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}