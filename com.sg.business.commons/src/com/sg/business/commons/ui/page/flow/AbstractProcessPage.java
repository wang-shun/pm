package com.sg.business.commons.ui.page.flow;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;

import com.mobnut.db.model.DataSet;
import com.mobnut.db.model.PrimaryObject;
import com.sg.bpm.workflow.model.DroolsProcessDefinition;
import com.sg.bpm.workflow.model.NodeAssignment;
import com.sg.business.commons.ui.flow.ProcessControlSetting;
import com.sg.business.commons.ui.flow.ProcessSettingPanel;
import com.sg.business.model.AbstractRoleDefinition;
import com.sg.business.model.IProcessControl;
import com.sg.business.model.User;
import com.sg.business.model.toolkit.UserToolkit;
import com.sg.widgets.part.editor.PrimaryObjectEditorInput;
import com.sg.widgets.part.editor.page.AbstractFormPageDelegator;
import com.sg.widgets.registry.config.BasicPageConfigurator;

public abstract class AbstractProcessPage extends
		AbstractFormPageDelegator {

	private boolean editable;
	private ProcessSettingPanel psp2;

	@Override
	public ProcessSettingPanel createPageContent(IManagedForm mForm,Composite parent,
			PrimaryObjectEditorInput input, BasicPageConfigurator conf) {
		parent.setBackgroundMode(SWT.INHERIT_DEFAULT);
		setFormInput(input);
		editable = input.isEditable();
		final IProcessControl ipc = getIProcessControl();

		psp2 = new ProcessSettingPanel(parent, getProcessSettingControl()) {

			@Override
			protected AbstractRoleDefinition getRoleDefinition(
					NodeAssignment nodeAssignment) {
				if (nodeAssignment != null) {
					return ipc.getProcessActionAssignment(getProcessKey(),
							nodeAssignment.getNodeActorParameter());
				} else {
					return null;
				}
			}

			@Override
			protected User getActor(NodeAssignment nodeAssignment) {
				if (nodeAssignment == null) {
					return null;
				}
				String userid = ipc.getProcessActionActor(getProcessKey(),
						nodeAssignment.getNodeActorParameter());
				return UserToolkit.getUserById(userid);
			}

			@Override
			public DataSet getActorDataSet() {
				AbstractRoleDefinition roled = getSelectedRole();
				return AbstractProcessPage.this.getActorDataSet(roled);
			}

			@Override
			protected String getRoleNavigatorId() {
				return AbstractProcessPage.this.getRoleNavigatorId();
			}

			@Override
			protected String getActorNavigatorId(AbstractRoleDefinition roled) {
				return AbstractProcessPage.this.getActorNavigatorId(roled);
			}

		};
//		// 设置角色的选择器，项目模板中的角色定义
//		psp2.setRoleNavigatorId(getRoleNavigatorId());
//
//		// 设置用户的选择器
//		psp2.setActorNavigatorId(getActorNavigatorId());

		List<DroolsProcessDefinition> processDefs = getProcessDefinition();
		psp2.setProcessDefinitionChoice(processDefs);
		// 返回当前选中流程
		DroolsProcessDefinition processDef = ipc
				.getProcessDefinition(getProcessKey());
		// 显示当前选中流程的信息
		psp2.setProcessDefinition(processDef);

		// 显示该流程是否激活
		boolean activate = ipc.isWorkflowActivate(getProcessKey());
		psp2.setProcessActivated(activate);

		// 设置角色的数据集
		psp2.setRoleDataSet(getRoleDataSet());

		psp2.createContent();

		// 添加监听
		psp2.addProcessSettingListener(new ProcessControlSetting(ipc,
				getProcessKey()) {
			@Override
			protected void event(int code) {
				setDirty(true);
			}
		});

		psp2.setEditable(editable);
		
		return psp2;
	}

	protected String getRoleNavigatorId() {
		return "commons.generic.tableselector"; //$NON-NLS-1$
	}
	
	protected String getActorNavigatorId(AbstractRoleDefinition roled) {
//		return "commons.generic.tableselector";
		return "commons.workflow.tableselector"; //$NON-NLS-1$
	}

	protected abstract int getProcessSettingControl();

	protected abstract DataSet getRoleDataSet();

	protected IProcessControl getIProcessControl() {
		PrimaryObjectEditorInput input = getInput();
		PrimaryObject po = input.getData();
		return (IProcessControl) po.getAdapter(IProcessControl.class);
	}

	protected abstract List<DroolsProcessDefinition> getProcessDefinition();

	protected abstract String getProcessKey();

	protected abstract DataSet getActorDataSet(AbstractRoleDefinition roled);

	@Override
	public void commit(boolean onSave) {
		setDirty(false);
	}

	@Override
	public void setFocus() {
	}

	@Override
	public void refresh() {
		final IProcessControl ipc = getIProcessControl();

		List<DroolsProcessDefinition> processDefs = getProcessDefinition();
		psp2.setProcessDefinitionChoice(processDefs);
		// 返回当前选中流程
		DroolsProcessDefinition processDef = ipc
				.getProcessDefinition(getProcessKey());
		// 显示当前选中流程的信息
		psp2.setProcessDefinition(processDef);

		// 显示该流程是否激活
		boolean activate = ipc.isWorkflowActivate(getProcessKey());
		psp2.setProcessActivated(activate);

		// 设置角色的数据集
		psp2.setRoleDataSet(getRoleDataSet());
		
		psp2.refresh();
		super.refresh();
	}
	
	@Override
	public boolean canRefresh() {
		return true;
	}
	
	public ProcessSettingPanel getProcessSettingPanel(){
		return psp2;
	}

}
