package com.sg.business.commons.ui.flow;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.mobnut.db.model.DataSet;
import com.sg.bpm.workflow.model.NodeAssignment;
import com.sg.business.model.AbstractRoleDefinition;
import com.sg.business.model.User;
import com.sg.widgets.commons.selector.NavigatorSelector;
import com.sg.widgets.viewer.ViewerControl;

/**
 * 流程活动编辑器，显示单个活动节点的详细信息
 * @author jinxitao
 *
 */
public class ActivityEditor extends Composite {

	public interface IActivityEditListener {

		void actorChanged(User newActor, User oldActor,
				NodeAssignment nodeAssignment, AbstractRoleDefinition roleDef);

		void roleChanged(AbstractRoleDefinition newRole,
				AbstractRoleDefinition oldRole, NodeAssignment nodeAssignment);

	}

	/**
	 * 活动名称
	 */
	private Text activiteName;
	
	/**
	 * 指派名称
	 */
	private Text assignmentType;
	
	/**
	 * 指派规则
	 */
	private Text assignmentRule;
	
	/**
	 * 角色限定
	 */
	private Text roleText;
	
	/**
	 * 选择角色按钮
	 */
	private Button roleSelectorButton;
	
	/**
	 * 活动执行人
	 */
	private Text actorText;
	
	/**
	 * 选择活动执行人按钮
	 */
	private Button actorSelectorButton;
	
	/**
	 * 是否包含活动执行人字段
	 */
	private boolean hasActorSelector;
	
	/**
	 * 是否被包含角色限定字段
	 */
	private boolean hasRoleSelector;
	
	/**
	 * 活动执行人选择器的navigatorId
	 */
	private String actorNavigatorId;
	
	/**
	 * 活动执行人选择器数据源
	 */
	private DataSet actorDataSet;

	private ListenerList listeners = new ListenerList();
	
	/**
	 * 活动节点角色指派
	 */
	private NodeAssignment nodeAssignment;
	
	/**
	 * 角色定义
	 */
	private AbstractRoleDefinition roleDef;
	
	/**
	 * 活动执行人
	 */
	private User actor;
	
	/**
	 * 选择角色数据集
	 */
	private DataSet roleDataSet;
	
	/**
	 * 选择角色的NavigatorId
	 */
	private String roleNavigatorId;

	/**
	 * 添加监听
	 * @param listener
	 */
	final public void addActiviteEditListener(IActivityEditListener listener) {
		listeners.add(listener);
	}

	/**
	 * 移除监听
	 * @param listener
	 */
	final public void removeActiviteEditListener(IActivityEditListener listener) {
		listeners.remove(listener);
	}

	/**
	 * 构造方法，初始化 hasActorSelector、hasActorSelector字段
	 * @param parent
	 * @param hasRoleSelector
	 * @param hasActorSelector
	 */
	ActivityEditor(Composite parent, boolean hasRoleSelector,
			boolean hasActorSelector) {
		super(parent, SWT.BORDER);
		this.hasActorSelector = hasActorSelector;
		this.hasRoleSelector = hasRoleSelector;
		createContent(this);
	}

	/**
	 * 创建活动节点编辑器
	 * @param parent
	 */
	private void createContent(Composite parent) {
		setLayout(new GridLayout(3, false));
		// 活动名称
		Label label = new Label(parent, SWT.NONE);
		label.setText("活动名称");
		label.setLayoutData(getGridData1());

		activiteName = new Text(parent, SWT.BORDER);
		activiteName.setLayoutData(getGridData2());
		activiteName.setEditable(false);

		// 指派类别
		label = new Label(parent, SWT.NONE);
		label.setText("指派类别");
		label.setLayoutData(getGridData1());
		assignmentType = new Text(parent, SWT.BORDER);
		assignmentType.setLayoutData(getGridData2());
		assignmentType.setEditable(false);

		// 指派规则
		label = new Label(parent, SWT.NONE);
		label.setText("指派规则");
		label.setLayoutData(getGridData1());
		assignmentRule = new Text(parent, SWT.BORDER);
		assignmentRule.setLayoutData(getGridData2());
		assignmentRule.setEditable(false);

		label = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3,
				1));

		// 选择角色
		if (hasRoleSelector) {
			label = new Label(parent, SWT.NONE);
			label.setText("角色限定");
			label.setLayoutData(getGridData1());
			roleText = new Text(parent, SWT.BORDER);
			roleText.setEditable(false);
			roleText.setLayoutData(getGridData3());
			roleSelectorButton = new Button(parent, SWT.PUSH);
			roleSelectorButton.setEnabled(false);
			roleSelectorButton.setText("..");
			roleSelectorButton.setLayoutData(getGridData1());
			roleSelectorButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					showRoleSelectorNavigator();
				}
			});
		}

		// 创建选择用户
		if (hasActorSelector) {
			label = new Label(parent, SWT.NONE);
			label.setText("活动执行人");
			label.setLayoutData(getGridData1());
			actorText = new Text(parent, SWT.BORDER);
			actorText.setEditable(false);
			actorText.setLayoutData(getGridData3());
			actorSelectorButton = new Button(parent, SWT.PUSH);
			actorSelectorButton.setEnabled(false);
			actorSelectorButton.setText("..");
			actorSelectorButton.setLayoutData(getGridData1());
			actorSelectorButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					showActorSelectorNavigator();
				}
			});
		}
	}

	/**
	 * 页面布局
	 * @return GridData
	 */
	private GridData getGridData3() {
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.widthHint = 120;
		return gd;
	}

	/**
	 * 页面布局
	 * @return GridData
	 */
	private GridData getGridData2() {
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd.widthHint = 120;
		return gd;
	}

	/**
	 * 页面布局
	 * @return GridData
	 */
	private GridData getGridData1() {
		return new GridData(SWT.LEFT, SWT.CENTER, false, false);
	}

	/**
	 * 设置活动名称，指派类别，指派规则等字段的显示内容
	 * @param nodeAssignment
	 * @param roleDef
	 * @param actor
	 */
	final public void setInput(NodeAssignment nodeAssignment,
			AbstractRoleDefinition roleDef, User actor) {
		this.nodeAssignment = nodeAssignment;
		this.roleDef = roleDef;
		this.actor = actor;
		if (actorSelectorButton != null) {
			actorSelectorButton.setEnabled(nodeAssignment != null
					&& nodeAssignment.isNeedAssignment());
		}
		if (roleSelectorButton != null) {
			roleSelectorButton.setEnabled(nodeAssignment != null
					&& nodeAssignment.isNeedAssignment());
		}
        
		if (nodeAssignment == null) {
			activiteName.setText("");
			assignmentType.setText("无需指派");
			assignmentRule.setText("");
			roleText.setText("");
			if (actorText != null) {
				actorText.setText(actor.getLabel());
			}
		} else {
			activiteName.setText(nodeAssignment.getNodeName());
			if (nodeAssignment.isDyanmic()) {
				assignmentType.setText("动态指派");
			} else if (nodeAssignment.isRuleAssignment()) {
				assignmentType.setText("规则指派");
			} else if (nodeAssignment.isStaticActor()) {
				assignmentType.setText("静态执行人");
			} else if (nodeAssignment.isStaticGroup()) {
				assignmentType.setText("静态执行组");
			} else {
				assignmentType.setText("");
			}

			String ruleName = nodeAssignment.getRuleAssignmentName();
			if (ruleName == null) {
				assignmentRule.setText("");
			} else {
				assignmentRule.setText(ruleName);
			}

			if (roleText != null) {
				if (roleDef != null) {
					roleText.setText(roleDef.getLabel());
				} else {
					roleText.setText("");
				}
			}

			if (actorText != null) {
				if (actor != null) {
					actorText.setText(actor.getLabel());
				} else {
					actorText.setText("");
				}
			}
		}
	}

	/**
	 * 弹出活动执行人选择器
	 */
	private void showActorSelectorNavigator() {
		NavigatorSelector ns = new NavigatorSelector(actorNavigatorId) {
			@Override
			protected void doOK(IStructuredSelection is) {
				User user = (User) is.getFirstElement();
				setActor(user);
				super.doOK(is);
			}

			@Override
			protected boolean isSelectEnabled(IStructuredSelection is) {

				if (!super.isSelectEnabled(is)) {
					return false;
				} else {
					Object element = is.getFirstElement();
					return element instanceof User;
				}
			}

		};
		ns.show();

		ViewerControl vc = ns.getNavigator().getViewerControl();
		vc.setDataSet(actorDataSet);

	}

	/**
	 * 设置活动执行人
	 * @param user
	 */
	private void setActor(User user) {
		Object[] lis = listeners.getListeners();
		for (int i = 0; i < lis.length; i++) {
			((IActivityEditListener) lis[i]).actorChanged(user, actor,
					nodeAssignment, roleDef);
		}
		actor = user;
		
		actorText.setText(actor.getLabel());
	}

	/**
	 * 设置角色限定
	 * @param role
	 */
	private void setRole(AbstractRoleDefinition role) {
		Object[] lis = listeners.getListeners();
		for (int i = 0; i < lis.length; i++) {
			((IActivityEditListener) lis[i]).roleChanged(role,roleDef, 
					nodeAssignment);
		}
		roleDef = role;
		
		roleText.setText(role.getLabel());
	}

	/**
	 * 弹出角色限定选择器
	 */
	private void showRoleSelectorNavigator() {
		NavigatorSelector ns = new NavigatorSelector(roleNavigatorId) {
			@Override
			protected void doOK(IStructuredSelection is) {
				AbstractRoleDefinition role = (AbstractRoleDefinition) is
						.getFirstElement();
				setRole(role);
				super.doOK(is);
			}

			@Override
			protected boolean isSelectEnabled(IStructuredSelection is) {

				if (!super.isSelectEnabled(is)) {
					return false;
				} else {
					Object element = is.getFirstElement();
					return element instanceof AbstractRoleDefinition;
				}
			}

		};
		ns.show();
		ViewerControl vc = ns.getNavigator().getViewerControl();
		vc.setDataSet(roleDataSet);

	}

	/**
	 * 设置活动执行人选择器navigatorId
	 * @param navigatorId
	 */
	final public void setActorNavigatorId(String navigatorId) {
		this.actorNavigatorId = navigatorId;
	}

	/**
	 * 设置活动执行人选择器dataset
	 * @param dataset
	 */
	final public void setActorDataSet(DataSet dataset) {
		this.actorDataSet = dataset;
	}

	/**
	 * 设置角色限定选择器navigatorId
	 * @param navigatorId
	 */
	final public void setRoleNavigatorId(String navigatorId) {
		this.roleNavigatorId = navigatorId;
	}

	/**
	 * 设置角色限定选择器dataset
	 * @param dataset
	 */
	final public void setRoleDataSet(DataSet dataset) {
		this.roleDataSet = dataset;
	}

}
