/*-
 * Copyright © 2010 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.scisoft.analysis.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;

public class MetaDataAdapter implements IExtendedMetadata {

	private Collection<String> dataNames;
	private Collection<Object> userObjects;
	private Collection<IMetaData> metaData = new ArrayList<IMetaData>();

	private String creator = null;
	private String filename = null;
	private String owner = null;
	private long filesize = -1;
	private String path;
	private long lastModified = -1;

	public MetaDataAdapter() {

	}

	/**
	 * This constructor will take a reference to a file and populate some of the metadata. 
	 * This should be used in conjunction with populating the rest of the metadata.
	 */
	public MetaDataAdapter(File f) {
		filesize = f.length();
		filename = f.getName();
		lastModified = f.lastModified();
		path = f.getAbsolutePath();
	}

	public MetaDataAdapter(Collection<String> names) {
		this.dataNames = names;
	}

	public MetaDataAdapter(Collection<String> names, final Collection<Object> userObjects) {
		this.dataNames = names;
		this.userObjects = userObjects;
	}

	@Override
	public Collection<String> getDataNames() {
		return dataNames;
	}

	@Override
	public Collection<Object> getUserObjects() {
		return userObjects;
	}

	@Override
	public Map<String, Integer> getDataSizes() {
		return null;
	}

	@Override
	public Map<String, int[]> getDataShapes() {
		return null;
	}

	@Override
	public String getMetaValue(String key) throws Exception {
		return null;
	}

	@Override
	public Collection<String> getMetaNames() throws Exception {
		return null;
	}

	@Override
	public MetaDataAdapter clone() {
		return null;
	}

	@Override
	public Calendar getCreation() {
		return null;
	}

	@Override
	public Calendar getLastModified() {
		Calendar c = null;
		if (lastModified < 0) {
			c = Calendar.getInstance();
			c.setTimeInMillis(lastModified);
		}
		return c;
	}

	@Override
	public String getCreator() {
		return creator;
	}

	@Override
	public String getFileName() {
		return filename;
	}

	@Override
	public String getFileOwner() {
		return owner;
	}

	@Override
	public long getFileSize() {
		return filesize;
	}

	@Override
	public String getFullPath() {
		return path;
	}

	@Override
	public String getScanCommand() {
		return null;
	}
}
