package com.sg.business.visualization.editor;

import java.net.URL;
import java.util.Calendar;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.internal.widgets.IWidgetGraphicsAdapter;
import org.eclipse.swt.internal.widgets.MarkupValidator;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import com.sg.business.model.IParameterListener;
import com.sg.business.model.ProjectProvider;
import com.sg.widgets.ImageResource;
import com.sg.widgets.MessageUtil;
import com.sg.widgets.Widgets;
import com.sg.widgets.part.NavigatorControl;
import com.sg.widgets.part.editor.IEditorActionListener;
import com.sg.widgets.part.editor.PrimaryObjectEditorInput;
import com.sg.widgets.part.editor.page.INavigatorPageBodyPartCreater;
import com.sg.widgets.part.editor.page.NavigatorPage;
import com.sg.widgets.viewer.ViewerControl;

@SuppressWarnings("restriction")
public abstract class AbstractProjectPage implements
		INavigatorPageBodyPartCreater, IParameterListener {

	private static final int INFOBANNER_HEIGHT = 68;
	private static final int MARGIN = 4;
	Label filterLabel;
	Composite header;
	protected NavigatorControl navi;
	protected ProjectProvider data;

	public AbstractProjectPage() {

	}

	@Override
	public void parameterChanged(Object[] oldParameters, Object[] newParameters) {
		// doquery
		if (navi != null) {
			ViewerControl viewerControl = navi.getViewerControl();
			if (!viewerControl.getControl().isDisposed()) {
				viewerControl.doReloadData(true);
			}
		}
		if (filterLabel != null && !filterLabel.isDisposed()) {
			filterLabel.setText(getParameterText());
			header.layout();
		}
	}

	@Override
	public void editorAction(IEditorActionListener reciever, int code,
			Object object) {
	}

	@Override
	public void createNavigatorBody(Composite body, NavigatorControl navi,
			PrimaryObjectEditorInput input, NavigatorPage navigatorPage) {
		// ����ȱʡ�Ĳ���
		data = (ProjectProvider) input.getData();
		data.addParameterChangedListener(this);
		this.navi = navi;

		body.setLayout(new FormLayout());
		// ����ҳͷ
		header = createHeader(body);
		FormData fd = new FormData();
		header.setLayoutData(fd);
		fd.top = new FormAttachment(0, 1);
		fd.left = new FormAttachment(0, 1);
		fd.right = new FormAttachment(100, -1);
		fd.height = INFOBANNER_HEIGHT;
		// // �����ָ���
		// Label line = new Label(body, SWT.NONE);
		// fd = new FormData();
		// line.setLayoutData(fd);
		// Color sepColor = Widgets.getColor(body.getDisplay(), 192, 192, 192);
		// line.setBackground(sepColor);
		// fd.top = new FormAttachment(header, 0);
		// fd.left = new FormAttachment(0, 1);
		// fd.right = new FormAttachment(100, -1);
		// fd.height = 1;

		// ����������
		Composite navigator = createContent(body);
		fd = new FormData();
		navigator.setLayoutData(fd);
		fd.top = new FormAttachment(header, 0);
		fd.left = new FormAttachment(0, 1);
		fd.right = new FormAttachment(100, -1);
		fd.bottom = new FormAttachment(100, -1);

		// ����navigator�����¼�
		handleNavigatorTableEvent();

	}

	private void handleNavigatorTableEvent() {
		Table control = (Table) navi.getViewer().getControl();
		control.setData(MarkupValidator.MARKUP_VALIDATION_DISABLED,
				Boolean.TRUE);
		control.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (event.detail == RWT.HYPERLINK) {
					try {
						URL url = new URL(event.text);
						String path = url.getPath();
						String orj = path.substring(1, path.indexOf("/", 1));
						String eventCode = path.substring(path.lastIndexOf("/") + 1);
						call(orj, eventCode);
					} catch (Exception e) {
						MessageUtil.showToast(e);
					}
				}
			}
		});
	}

	protected abstract Composite createContent(Composite body);

	protected void call(String orj, String eventCode) {

	}

	protected Composite createHeader(Composite body) {
		String projectSetCover = data.getProjectSetCoverImage();

		Composite header = new Composite(body, SWT.NONE);
		header.setLayout(new FormLayout());
		setBackgroundGradient(header);

		Label cover = new Label(header, SWT.NONE);
		cover.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);

		StringBuffer sb = new StringBuffer();
		// ������Ŀ���Ϸ���ͼƬ
		if (projectSetCover != null) {
			sb.append("<img src='");
			sb.append(projectSetCover);
			sb.append("' style='float:left;margin-top:" + MARGIN + "' width='"
					+ (INFOBANNER_HEIGHT - MARGIN) + "' height='"
					+ (INFOBANNER_HEIGHT - MARGIN) + "' />");
			cover.setText(sb.toString());
		}

		FormData fd = new FormData();
		cover.setLayoutData(fd);
		fd.left = new FormAttachment();
		fd.top = new FormAttachment();
		fd.height = INFOBANNER_HEIGHT;
		fd.width = INFOBANNER_HEIGHT;

		// ��ʾ���ݹ���
		filterLabel = new Label(header, SWT.NONE);
		filterLabel.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		fd = new FormData();
		filterLabel.setLayoutData(fd);
		fd.left = new FormAttachment(30, -INFOBANNER_HEIGHT);
		fd.top = new FormAttachment(32);

		// ��ʾ�˵�ͼ��
		final CLabel menuButton = new CLabel(header, SWT.NONE);
		menuButton.setImage(Widgets.getImage(ImageResource.DOWN_16));
		fd = new FormData();
		menuButton.setLayoutData(fd);
		fd.left = new FormAttachment(filterLabel, 4);
		fd.top = new FormAttachment(40);
		menuButton.setCursor(Display.getCurrent().getSystemCursor(
				SWT.CURSOR_HAND));
		menuButton.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent e) {
			}

			@Override
			public void mouseDown(MouseEvent e) {
				showFilterMenu(menuButton);
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});

		// ��ʾ��Ŀ��������
		Label projectSetLabel = new Label(header, SWT.NONE);
		projectSetLabel.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		projectSetLabel.setText(getProjectSetPageLabel());

		fd = new FormData();
		projectSetLabel.setLayoutData(fd);
		fd.left = new FormAttachment(menuButton, 4);
		fd.top = new FormAttachment(32);

		filterLabel.setText(getParameterText());

		return header;
	}

	protected abstract String getProjectSetPageLabel();

	protected void showFilterMenu(Control menuButton) {
		final Shell shell = new Shell(menuButton.getShell(), SWT.BORDER);
		shell.setLayout(new FormLayout());
		final Combo yearCombo = new Combo(shell, SWT.READ_ONLY);
		FormData fd = new FormData();
		yearCombo.setLayoutData(fd);
		fd.left = new FormAttachment(0, 4);
		fd.top = new FormAttachment(0, 4);
		fd.width = 100;

		final int startYear = Calendar.getInstance().get(Calendar.YEAR) - 5;
		for (int i = startYear; i < (startYear + 6); i++) {
			yearCombo.add("" + i);
		}
		yearCombo.select(5);

		final Combo quarterCombo = new Combo(shell, SWT.READ_ONLY);
		fd = new FormData();
		quarterCombo.setLayoutData(fd);
		fd.left = new FormAttachment(yearCombo, 4);
		fd.top = new FormAttachment(0, 4);
		fd.width = 80;
		quarterCombo.add("����");
		for (int i = 1; i < 5; i++) {
			quarterCombo.add("" + i + "����");
		}

		final Combo monthCombo = new Combo(shell, SWT.READ_ONLY);
		fd = new FormData();
		monthCombo.setLayoutData(fd);
		fd.left = new FormAttachment(quarterCombo, 4);
		fd.top = new FormAttachment(0, 4);
		fd.right = new FormAttachment(100, -4);

		fd.width = 80;
		monthCombo.add("����");
		for (int i = 1; i < 13; i++) {
			monthCombo.add("" + i + "��");
		}

		Button ok = new Button(shell, SWT.PUSH);
		ok.setText("Ok");
		fd = new FormData();
		ok.setLayoutData(fd);
		fd.left = new FormAttachment(0, 4);
		fd.top = new FormAttachment(monthCombo, 16);
		fd.bottom = new FormAttachment(100, -4);
		ok.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int yearIndex = yearCombo.getSelectionIndex();
				setFilter(startYear + yearIndex,
						quarterCombo.getSelectionIndex(),
						monthCombo.getSelectionIndex());
				shell.dispose();
			}
		});

		Button cancel = new Button(shell, SWT.PUSH);
		cancel.setText("Cancel");
		cancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.dispose();
			}
		});

		fd = new FormData();
		cancel.setLayoutData(fd);
		fd.left = new FormAttachment(ok, 4);
		fd.top = new FormAttachment(monthCombo, 16);
		fd.bottom = new FormAttachment(100, -4);

		shell.pack();
		Point location = menuButton.toDisplay(0, 16);
		Display display = shell.getDisplay();
		if (display.getBounds().width < shell.getBounds().width + location.x) {
			location.x = display.getBounds().width - shell.getBounds().width
					- 10;
		}

		shell.setLocation(location);
		shell.open();
		shell.addShellListener(new ShellListener() {

			@Override
			public void shellDeactivated(ShellEvent e) {
				shell.close();
			}

			@Override
			public void shellClosed(ShellEvent e) {
			}

			@Override
			public void shellActivated(ShellEvent e) {
			}
		});
	}

	protected void setFilter(int yearIndex, int quarterIndex, int monthIndex) {
		if (yearIndex < 0) {
			return;
		}
		Object[] parameters = new Object[2];
		parameters[0] = Calendar.getInstance();
		if (monthIndex > 0) {
			parameters[1] = ProjectProvider.PARAMETER_SUMMARY_BY_MONTH;
			((Calendar) parameters[0]).set(Calendar.MONTH, monthIndex - 1);

		} else if (quarterIndex > 0) {
			parameters[1] = ProjectProvider.PARAMETER_SUMMARY_BY_QUARTER;
			((Calendar) parameters[0]).set(Calendar.MONTH,
					3 * (quarterIndex) - 1);

		} else {
			parameters[1] = ProjectProvider.PARAMETER_SUMMARY_BY_YEAR;
			((Calendar) parameters[0]).set(Calendar.YEAR, yearIndex);

		}
		data.setParameters(parameters);
	}

	protected String getHeadParameterText() {
		StringBuffer sb = new StringBuffer();
		if (ProjectProvider.PARAMETER_SUMMARY_BY_YEAR
				.equals(data.parameters[1])) {
			sb.append(((Calendar) data.parameters[0]).get(Calendar.YEAR) + "��");
		} else if (ProjectProvider.PARAMETER_SUMMARY_BY_QUARTER
				.equals(data.parameters[1])) {
			Calendar calendar = (Calendar) data.parameters[0];
			int month = calendar.get(Calendar.MONTH);
			sb.append(calendar.get(Calendar.YEAR) + "��" + (1 + (1 + month) / 4)
					+ "����");
		} else if (ProjectProvider.PARAMETER_SUMMARY_BY_MONTH
				.equals(data.parameters[1])) {
			Calendar calendar = (Calendar) data.parameters[0];
			int month = calendar.get(Calendar.MONTH);
			sb.append(calendar.get(Calendar.YEAR) + "��" + (1 + month) + "��");
		}

		return sb.toString();
	}

	private void setBackgroundGradient(Composite header) {
		Object adapter = header.getAdapter(IWidgetGraphicsAdapter.class);
		IWidgetGraphicsAdapter gfxAdapter = (IWidgetGraphicsAdapter) adapter;
		int[] percents = new int[] { 0, 50, 100 };
		Display display = header.getDisplay();
		Color[] gradientColors = new Color[] {
				Widgets.getColor(header.getDisplay(), 220, 220, 240),
				Widgets.getColor(header.getDisplay(), 245, 245, 250),
				Widgets.getColor(display, 255, 255, 255) };

		gfxAdapter.setBackgroundGradient(gradientColors, percents, true);
	}

	protected String getParameterText() {
		StringBuffer sb = new StringBuffer();
		sb.append("<span style='FONT-FAMILY:΢���ź�;font-size:13pt'>");
		sb.append(getHeadParameterText());
		sb.append("</span>");
		return sb.toString();
	}
}