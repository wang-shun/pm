package com.sg.bpm.workflow.model;

import java.util.ArrayList;
import java.util.List;

import org.drools.definition.process.Node;
import org.drools.definition.process.Process;
import org.drools.definition.process.WorkflowProcess;
import org.jbpm.workflow.core.node.ForEachNode;
import org.jbpm.workflow.core.node.HumanTaskNode;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.sg.bpm.service.BPM;

public class DroolsProcessDefinition {

	private String kbase;
	private String processId;
	private String processName;
	private String processNamespace;
	private String type;
	private String version;
	private Process process;

	public DroolsProcessDefinition(String kbase, Process process) {
		this.kbase = kbase;
		this.processId = process.getId();
		this.processName = process.getName();
		this.processNamespace=process.getPackageName();
		this.type = process.getType();
		this.version = process.getVersion();
		this.process = process;
	}
	
	public DroolsProcessDefinition(DBObject data) {
		this.kbase = (String) data.get("kbase");
		this.processId = (String) data.get("processId");
		this.processName = (String) data.get("processName");
		this.processNamespace=(String) data.get("processNamespace");
		this.type =(String) data.get("type");
		this.version = (String) data.get("version");
	}
	
	public Process getProcess(){
		if(process==null){
			process = BPM.getBPMService().getKnowledgeBase(kbase).getProcess(processId);
		}
		return process;
	}
	
	public DBObject getData(){
		DBObject data = new BasicDBObject();
		data.put("kbase", kbase);
		data.put("processId", processId);
		data.put("processName", processName);
		data.put("processNamespace", processNamespace);
		data.put("type", type);
		data.put("version", version);
		return data;
	}

	public String getKbase() {
		return kbase;
	}

	public String getProcessId() {
		return processId;
	}

	public String getProcessName() {
		return processName;
	}

	public String getProcessNamespace() {
		return processNamespace;
	}

	public String getType() {
		return type;
	}

	public String getVersion() {
		return version;
	}
	
	
	public List<HumanTaskNode> getHumanNodes() {
		List<HumanTaskNode> result = new ArrayList<HumanTaskNode>();
		getProcess();
		if (process instanceof WorkflowProcess) {
			Node[] nodes = ((WorkflowProcess) process).getNodes();
			for (int i = 0; i < nodes.length; i++) {
				Node nodesItem = nodes[i];
				if (nodesItem instanceof HumanTaskNode) {
					result.add((HumanTaskNode) nodesItem);
				} else if (nodesItem instanceof ForEachNode) {
					// 忽略多实例的节点，这可能在多实例中的活动中设置变量参与者时出现问题
				}
			}
		}
		return result;
	}

}
