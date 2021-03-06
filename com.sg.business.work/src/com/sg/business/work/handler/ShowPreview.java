package com.sg.business.work.handler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.bson.types.ObjectId;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.UrlLauncher;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.mobnut.commons.util.Office2PDFJob;
import com.mobnut.commons.util.file.FileUtil;
import com.mobnut.commons.util.file.GridFSFilePrevieweUtil;
import com.mobnut.commons.util.file.OSServerFile;
import com.mobnut.db.file.GridServerFile;
import com.mobnut.db.file.RemoteFile;

public class ShowPreview extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		StructuredSelection selection = (StructuredSelection) HandlerUtil
				.getCurrentSelection(event);
		if (selection == null || selection.isEmpty()) {
			return null;
		}
		Object element = selection.getFirstElement();
		Shell parent = HandlerUtil.getActiveShell(event);
		if (element instanceof GridServerFile) {
			createPreview(((GridServerFile) element).getRemoteFile(), parent);
		} else if (element instanceof OSServerFile) {
			createPreview((OSServerFile) element, parent);
		}

		return null;
	}

	private void createPreview(final OSServerFile osfile, final Shell parent) {
		File serverFile = osfile.getServerFile();
		String serverFilePath = serverFile.getPath();
		String previewFilePath = serverFilePath + ".pdf"; //$NON-NLS-1$
		final File previewFile = new File(previewFilePath);
		if (previewFile.isFile()) {
			previewOSFile(previewFile, parent);
			return;
		}

		int fileType = FileUtil.getFileType(osfile.getFileName());
		final Display display = parent.getDisplay();
		if (fileType == FileUtil.FILETYPE_OFFICE_FILE) {

			Office2PDFJob job = new Office2PDFJob();
			job.setSourceFile(serverFile);
			job.setTargetFile(previewFile);
			job.setUser(true);

			job.addJobChangeListener(new JobChangeAdapter() {

				@Override
				public void done(IJobChangeEvent event) {
					display.asyncExec(new Runnable() {

						@Override
						public void run() {
							previewOSFile(previewFile, parent);
						}

					});
					super.done(event);
				}
			});
			job.schedule();

		}

	}

	private void createPreview(final RemoteFile remoteFile, final Shell parent) {
		final GridFSFilePrevieweUtil previewUtil = new GridFSFilePrevieweUtil();
		previewUtil.setRemoteFile(remoteFile);
		if (!previewUtil.isPreviewAvailable()) {
			String pathname = System.getProperty("user.dir") + "/temp"; //$NON-NLS-1$ //$NON-NLS-2$
			File file;
			try {
				file = remoteFile.createServerFile(pathname);
			} catch (IOException e1) {
				e1.printStackTrace();
				return;
			}

			final ObjectId pvOid = new ObjectId();
			String masterfileName = file.getName();
			String previewPath = file.getParent()
					+ "/" //$NON-NLS-1$
					+ masterfileName.substring(0,
							masterfileName.lastIndexOf(".")) + ".pdf"; //$NON-NLS-1$ //$NON-NLS-2$
			final File previewFile = new File(previewPath);
			Job job = previewUtil.createGeneratePDFJob(file, previewPath);
			final Display display = parent.getDisplay();
			job.addJobChangeListener(new JobChangeAdapter() {

				@Override
				public void done(IJobChangeEvent event) {
					display.asyncExec(new Runnable() {

						@Override
						public void run() {
							remoteFile.setPreviewUploaded(previewFile, pvOid);
							try {
								remoteFile.addPreview();
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							}
							previewRemoteFile(previewUtil, parent);
						}

					});
					super.done(event);
				}
			});
			job.schedule();
		} else {
			previewRemoteFile(previewUtil, parent);
		}
	}

	private void previewOSFile(File previewFile, Shell parent) {
		StringBuffer url = new StringBuffer();
		url.append("/pdf/open?"); //$NON-NLS-1$
		String filePath = previewFile.getPath();
		url.append("path="); //$NON-NLS-1$
		url.append(filePath);
		UrlLauncher launcher = RWT.getClient().getService(UrlLauncher.class);
		launcher.openURL(url.toString());
	}

	private void previewRemoteFile(GridFSFilePrevieweUtil previewUtil,
			Shell parent) {
		if (previewUtil.isHTML()) {
			Shell shell = new Shell(parent, SWT.MAX | SWT.CLOSE | SWT.RESIZE);
			Display display = shell.getDisplay();
			shell.setLayout(new FillLayout());
			Browser previewer = new Browser(shell, SWT.NONE);
			previewer.setText(previewUtil.getHTML());
			Rectangle bounds = new Rectangle(
					(display.getBounds().width - 600) / 2,
					(display.getBounds().height - 600) / 2, 600, 600);
			shell.setBounds(bounds);
			shell.open();
		} else {
			UrlLauncher launcher = RWT.getClient()
					.getService(UrlLauncher.class);
			launcher.openURL(previewUtil.getURL());
		}
	}

}
