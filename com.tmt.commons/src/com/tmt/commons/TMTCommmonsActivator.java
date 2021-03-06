package com.tmt.commons;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.mobnut.db.DBActivator;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.MongoClientOptions.Builder;

/**
 * The activator class controls the plug-in life cycle
 */
public class TMTCommmonsActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.tmt.commons"; //$NON-NLS-1$

	// The shared instance
	private static TMTCommmonsActivator plugin;

	private DB db;

	private Set<MongoClient> mongos = new HashSet<MongoClient>();

	/**
	 * The constructor
	 */
	public TMTCommmonsActivator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		loadDB();
	}

	private void loadDB() {
		String folderName = System.getProperty("user.dir") //$NON-NLS-1$
				+ "/configuration/";//$NON-NLS-1$
		File folder = new File(folderName);
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File file, String name) {
				return name.toLowerCase().endsWith(".dbconf");
			}
		};
		String[] files = folder.list(filter );
		for (int i = 0; i < files.length; i++) {
			loadProperties(files[i]);
		}
	}

	private void loadProperties(String name) {
		InputStream is = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(System.getProperty("user.dir") //$NON-NLS-1$
					+ "/configuration/" + name); //$NON-NLS-1$
			is = new BufferedInputStream(fis);
			Properties props = new Properties();
			props.load(is);
			String host = props.getProperty("db.host"); //$NON-NLS-1$
			String _port = props.getProperty("db.port");
			int port = _port==null?10001:Integer.parseInt(_port); //$NON-NLS-1$
			String dbname = props.getProperty("db.name"); //$NON-NLS-1$
			if(dbname==null){
				dbname = name.substring(0, name.indexOf("."));
			}
			

			ArrayList<ServerAddress> serverList = null;
			String replicaSet = props.getProperty("db.replicaSet"); //$NON-NLS-1$
			if (replicaSet!=null&&!replicaSet.isEmpty()) {
				serverList = new ArrayList<ServerAddress>();
				String[] arr = replicaSet.split(" ");
				for (int i = 0; i < arr.length; i++) {
					String[] ari = arr[i].split(":");
					ServerAddress address = new ServerAddress(ari[0],
							Integer.parseInt(ari[1]));
					serverList.add(address);
				}
			}
			Builder builder = MongoClientOptions.builder();
			builder.autoConnectRetry("true".equalsIgnoreCase(props //$NON-NLS-1$
					.getProperty("db.options.autoConnectRetry"))); //$NON-NLS-1$
			builder.connectionsPerHost(Integer.parseInt(props
					.getProperty("db.options.connectionsPerHost"))); //$NON-NLS-1$
			builder.maxWaitTime(Integer.parseInt(props
					.getProperty("db.options.maxWaitTime"))); //$NON-NLS-1$
			builder.socketTimeout(Integer.parseInt(props
					.getProperty("db.options.socketTimeout"))); //$NON-NLS-1$
			builder.connectTimeout(Integer.parseInt(props
					.getProperty("db.options.connectTimeout"))); //$NON-NLS-1$
			builder.threadsAllowedToBlockForConnectionMultiplier(Integer.parseInt(props
					.getProperty("db.options.threadsAllowedToBlockForConnectionMultiplier"))); //$NON-NLS-1$
			ServerAddress address = new ServerAddress(host, port);
			if (serverList != null) {
				MongoClient mongo = new MongoClient(serverList);
				db = mongo.getDB(dbname);
				mongos.add(mongo);
			} else {
				MongoClient mongo = new MongoClient(address, builder.build());
				db = mongo.getDB(dbname);
				mongos.add(mongo);
			}			
			DBActivator.registerDB(dbname, db);

		} catch (Exception e) {
		} finally {
			if (fis != null)
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
				}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		Iterator<MongoClient> iter = mongos.iterator();
		while(iter.hasNext()){
			iter.next().close();
		}
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static TMTCommmonsActivator getDefault() {
		return plugin;
	}

}
