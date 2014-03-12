package com.sg.business.commons.ui.page.flow;

import java.util.ArrayList;
import java.util.List;

import com.mobnut.db.model.DataSet;
import com.mobnut.db.model.PrimaryObject;
import com.sg.bpm.workflow.model.DroolsProcessDefinition;
import com.sg.business.commons.ui.flow.ProcessSettingPanel;
import com.sg.business.model.AbstractRoleAssignment;
import com.sg.business.model.AbstractRoleDefinition;
import com.sg.business.model.IProcessControl;
import com.sg.business.model.Organization;
import com.sg.business.model.Project;
import com.sg.business.model.ProjectRole;
import com.sg.business.model.User;
import com.sg.business.model.toolkit.UserToolkit;
import com.sg.widgets.part.editor.PrimaryObjectEditorInput;

/**
 * ��Ŀ�ύʱ�����ָ�������̣����Զ�������������������Ŀ�ύ<br/>
 * �ö��������Ľ�ɫ��������֯�Ľ�ɫ����������Ŀ��ɫ���塣���ԣ���Ҫ���ǻ�ȡ��ɫ���岿�ֵĴ���
 * 
 * @author zhonghua
 * 
 */
public abstract class AbstractProjectProcessPage extends AbstractProcessPage {

	@Override
	protected IProcessControl getIProcessControl() {
		PrimaryObjectEditorInput input = getInput();
		Project project = (Project) input.getData();
		return (IProcessControl) project.getAdapter(IProcessControl.class);
	}

	@Override
	protected DataSet getRoleDataSet() {
		PrimaryObjectEditorInput input = getInput();
		Project project = (Project) input.getData();
		if (project != null) {
			List<PrimaryObject> rds = project.getProjectRole();
			return new DataSet(rds);
		}
		return null;
	}

	@Override
	protected List<DroolsProcessDefinition> getProcessDefinition() {
		PrimaryObjectEditorInput input = getInput();
		Project project = (Project) input.getData();
		if (project != null) {
			Organization org = project.getFunctionOrganization();
			return org.getDroolsProcessDefinitions();
		}
		return null;
	}

	@Override
	protected DataSet getActorDataSet(AbstractRoleDefinition roled) {
		// �����ɫ���岻Ϊ�գ�ȡ��ɫ�µ��û�
		List<PrimaryObject> result = new ArrayList<PrimaryObject>();
		if (roled instanceof ProjectRole) {
			ProjectRole projectRole = (ProjectRole) roled;
			List<PrimaryObject> assignments = projectRole.getAssignment();
			for (int i = 0; i < assignments.size(); i++) {
				AbstractRoleAssignment ass = (AbstractRoleAssignment) assignments
						.get(i);
				String userid = ass.getUserid();
				User user = UserToolkit.getUserById(userid);
				if (!result.contains(user)) {
					result.add(user);
				}
			}

		} else {
			PrimaryObjectEditorInput input = getInput();
			Project project = (Project) input.getData();
			List<?> useridList = project.getParticipatesIdList();
			if (useridList != null) {
				for (int i = 0; i < useridList.size(); i++) {
					String userid = (String) useridList.get(i);
					User user = UserToolkit.getUserById(userid);
					if (!result.contains(user)) {
						result.add(user);
					}
				}
			}
		}

		// �����ɫ����Ϊ�գ�ȡ��Ŀ�Ĳ�����
		return new DataSet(result);
	}

	@Override
	protected int getProcessSettingControl() {
		return ProcessSettingPanel.PROCESS_SELECTOR
				| ProcessSettingPanel.ROLE_SELECTOR
				| ProcessSettingPanel.ACTOR_SELECTOR;
	}

}