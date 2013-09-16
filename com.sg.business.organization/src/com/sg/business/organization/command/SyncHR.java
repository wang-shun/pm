package com.sg.business.organization.command;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.sg.business.organization.OrganizationActivator;

/**
 * <P>
 * 同步HR系统
 * </p>
 * <li>提供了同步HR系统组织的方法
 * 
 * @author yangjun
 *
 */
public class SyncHR extends Job {

	private Set<OrgExchange> insertSet;
	private Set<OrgExchange> removeSet;
	private Set<OrgExchange> renameSet;
	public SyncHR() {
		super("读取HR系统数据");
		setUser(true);
	}

	/**
	 * 与HR组织进行同步
	 * @param insertSet ，存放PM系统比HR系统少的组织，将会作为PM系统需要插入的组织使用
	 * @param removeSet ,存放PM系统比HR系统多的组织，将会作为PM系统需要删除的组织使用
	 * @param renameSet ,存放PM系统和HR系统名称不一致的组织，将会作为PM系统需要修改全称的组织使用
	 */
	public void doSyscHROrganization() throws Exception {
		insertSet = new HashSet<OrgExchange>();
		removeSet = new HashSet<OrgExchange>();
		renameSet = new HashSet<OrgExchange>();
		
		// 获取PM系统和HR系统的顶级组织，构造时会同时构造出完整的组织树形结构
		OrgExchange pmOrg = new OrgExchange(null, true);
		OrgExchange hrOrg = new OrgExchange(null, false);

		// 判断顶级组织是否一致
		if (!hrOrg.equals(pmOrg)) {
			// 顶级组织不一致时，PM系统中的所有组织需要删除，HR的所有组织需要插入
			insertSet.add(hrOrg);
			removeSet.add(pmOrg);
		} else {
			// 获取PM系统和HR系统组织的差异
			getPMDifferentHR(pmOrg, hrOrg, insertSet, removeSet, renameSet);
		}
//		// 插入PM系统比HR系统少的组织
//		for (OrgExchange orgExchange : insertSet) {
//			orgExchange.doAddAllHR();
//		}
//		// 修改PM系统和HR系统名称不一致的组织
//		for (OrgExchange orgExchange : renameSet) {
//			orgExchange.doRenameHR();
//		}
//		// 发邮件通知系统管理员删除PM系统比HR系统多的组织
//		if(removeSet.size() > 0){
//			pmOrg.sendMessage(removeSet);
//		}
	}

	/**
	 * 获取PM系统和HR系统组织的差异
	 * 
	 * @param pmOrg
	 *            : {@link com.sg.business.organization.command.OrgExchange}类型 ,<BR/>
	 *            PM系统中的组织，含子组织，具有完整的组织结构
	 * @param hrOrg
	 *            : {@link com.sg.business.organization.command.OrgExchange}类型 ,<br/>
	 *            HR系统中的组织，含子组织，具有完整的组织结构
	 * @param insertSet
	 *            : {@link HashSet} <br/>
	 *            PM系统比HR系缺少的组织,含子组织
	 * @param removeSet
	 *            : {@link HashSet}, <br/>
	 *            PM系统比HR系统多的组织，含子组织
	 * @param renameSet
	 *            : {@link HashSet}, <br/>
	 *            PM系统和HR系统名称不一致的组织
	 */
	private void getPMDifferentHR(OrgExchange pmOrg, OrgExchange hrOrg,
			Set<OrgExchange> insertSet, Set<OrgExchange> removeSet,
			Set<OrgExchange> renameSet) {
		//获取PM系统比HR系缺少的子组织
		insertSet.addAll(hrOrg.getDifferentChildren(pmOrg));
		//获取PM系统比HR系缺多的子组织
		removeSet.addAll(pmOrg.getDifferentChildren(hrOrg));
		//判断当前两个组织的全称是否一致
		if (!hrOrg.getDifferentName(pmOrg)) {
			hrOrg.setPmId(pmOrg.getPmId());
			renameSet.add(hrOrg);
		}
		//获取PM系统比HR系缺相同的子组织
		Set<OrgExchange> hrSameChildren = hrOrg.getSameChildren(pmOrg);
		//循环获取HR系统中相同的子组织
		for (OrgExchange orgExchange : hrSameChildren) {
			//通过HR的子组织编号构造HR系统和PM系统的子组织
			OrgExchange pmOrgChildren = null;
			try {
				pmOrgChildren = new OrgExchange(orgExchange.getOrgId(),
						true);
			} catch (Exception e) {
				e.printStackTrace();
			}
			OrgExchange hrOrgChildren = null;
			try {
				hrOrgChildren = new OrgExchange(orgExchange.getOrgId(),
						false);
			} catch (Exception e) {
				e.printStackTrace();
			}
			//迭代调用该方法，获取子组织的差异
			getPMDifferentHR(pmOrgChildren, hrOrgChildren, insertSet,
					removeSet, renameSet);
		}
	}

	
	public Set<OrgExchange> getInsertSet() {
		return insertSet;
	}

	public Set<OrgExchange> getRemoveSet() {
		return removeSet;
	}

	public Set<OrgExchange> getRenameSet() {
		return renameSet;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask("正在查询数据", IProgressMonitor.UNKNOWN);
		try {
			doSyscHROrganization();
			return Status.OK_STATUS;
		} catch (Exception e) {
			return new MultiStatus(OrganizationActivator.PLUGIN_ID, IStatus.ERROR, "查询数据", e);
		}
		
	}

}
