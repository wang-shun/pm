package com.sg.business.work.launch;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.mobnut.db.model.IContext;
import com.mobnut.db.model.ModelService;
import com.mobnut.db.model.PrimaryObject;
import com.sg.business.model.Work;
import com.sg.business.model.WorkDefinition;
import com.sg.widgets.MessageUtil;
import com.sg.widgets.Widgets;
import com.sg.widgets.part.CurrentAccountContext;
import com.sg.widgets.part.editor.PrimaryObjectEditorInput;
import com.sg.widgets.part.editor.page.BasicWizardPage;
import com.sg.widgets.registry.config.BasicPageConfigurator;
import com.sg.widgets.registry.config.DataEditorConfigurator;

public class LaunchWorkWizard extends Wizard {

	private static final String PAGE_LAUNCH_WORK_BASICPAGE = "launch.work.basicpage";
	private SelectWorkDefinitionPage selectWorkDefinitionPage;
	private PrimaryObjectEditorInput editorInput;
	private BasicWizardPage basicPage;
	private WorkFlowSettingPage flowSettingPage;
	private boolean startWorkWhenFinish;
	private ConfirmPage comfirmPage;

	public static LaunchWorkWizard OPEN() {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();

		if (window != null) {
			LaunchWorkWizard wiz = new LaunchWorkWizard();
			Shell shell = window.getShell();
			WizardDialog wizardDialog = new WizardDialog(shell, wiz) {
				@Override
				protected Point getInitialSize() {
					Point size = super.getInitialSize();
					return new Point(600, size.y);
				}
			};

			wizardDialog.open();
			return wiz;
		}
		return null;

	}

	public LaunchWorkWizard() {
		setWindowTitle("发起工作");
		initInput();
	}

	private void initInput() {
		Work work = ModelService.createModelObject(Work.class);
		DataEditorConfigurator editor = (DataEditorConfigurator) Widgets
				.getEditorRegistry().getConfigurator("editor.work.launch");
		editorInput = new PrimaryObjectEditorInput(work, editor, null);
		editorInput.setEditable(true);
		editorInput.setNeedHostPartListenSaveEvent(false);
		editorInput.setContext(new CurrentAccountContext());
	}

	@Override
	public void addPages() {
		selectWorkDefinitionPage = new SelectWorkDefinitionPage();

		BasicPageConfigurator conf = (BasicPageConfigurator) Widgets
				.getPageRegistry().getConfigurator(PAGE_LAUNCH_WORK_BASICPAGE);
		basicPage = new BasicWizardPage(conf);
		basicPage.setInput(editorInput);

		addPage(selectWorkDefinitionPage);
		addPage(basicPage);

	}

	@Override
	public boolean performFinish() {
		Work work = (Work) editorInput.getData();
		IContext context = new CurrentAccountContext();
		try {
			work.doSave(context);
			if(startWorkWhenFinish){
				work.doStart(context);
			}
			return true;
		} catch (Exception e) {
			MessageUtil.showToast(e);
			return false;
		}
		
	}

	public void setWorkDefinition(WorkDefinition workd) {
		// 设置流程
		Work work = (Work) editorInput.getData();
		try {
			workd.makeStandloneWork(work, editorInput.getContext());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page == basicPage) {
			// 判断有无流程，如果无流程，则返回空
			Work work = (Work) editorInput.getData();
			if (!work.isExecuteWorkflowActivateAndAvailable()) {
				return getConfirmPage();
			} else {
				return getFlowSettingPage();
			}
		} else if (page == flowSettingPage) {
			return getConfirmPage();
		} else if(page == comfirmPage){
			return null;
		}else if(page == selectWorkDefinitionPage ){
			return basicPage;
		}else {
			return null;
		}
	}

	private IWizardPage getFlowSettingPage() {
		if (flowSettingPage == null) {
			flowSettingPage = new WorkFlowSettingPage();
			flowSettingPage.setInput(editorInput);
			flowSettingPage.setWizard(this);
		}else{
			flowSettingPage.refresh();
		}
		return flowSettingPage;
	}

	private IWizardPage getConfirmPage() {
		if(comfirmPage==null){
			comfirmPage = new ConfirmPage();
			comfirmPage.setInput((Work) editorInput.getData());
			comfirmPage.setWizard(this);
		}else{
			comfirmPage.refresh();
		}
		return comfirmPage;

	}

	public void setStartWorkWhenFinsh(boolean selection) {
		this.startWorkWhenFinish = selection;
	}

	public PrimaryObjectEditorInput getInput(){
		return editorInput;
	}
	
	@Override
	public boolean canFinish() {
		return super.canFinish();
	}
}
