/*-
 * Copyright 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.processing.operations.externaldata;

import org.eclipse.january.dataset.IDataset;

import uk.ac.diamond.scisoft.analysis.processing.operations.OperateOnDataAbstractOperation;

public abstract class AbstractExternalDataOperation extends OperateOnDataAbstractOperation<ExternalDataModel> {

	@Override
	protected String getFilePath(IDataset input) {
		return ((ExternalDataModel)model).getFilePath();
	}
}