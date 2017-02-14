/*-
 * Copyright (c) 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */


package uk.ac.diamond.scisoft.analysis.processing.operations.saxs;


// Imports from org.eclipse.dawnsci
import org.eclipse.dawnsci.analysis.api.processing.model.FileType;
import org.eclipse.dawnsci.analysis.api.processing.model.RangeType;
import org.eclipse.dawnsci.analysis.api.processing.model.OperationModelField;
import org.eclipse.dawnsci.analysis.api.processing.model.AbstractOperationModel;


// @author Tim Snow


// The model for a DAWN process to perform an absolute intensity calibration
public class AbsoluteIntensityCalibrationModel extends AbstractOperationModel {


	// Get the location of the calibration file
	@OperationModelField(hint="Absolute intensity calibration, e.g. glassy carbon, file path", file = FileType.EXISTING_FILE, label = "Calibration file", fieldPosition = 0)
	private String absoluteScanFilePath = "platform:/plugin/uk.ac.diamond.scisoft.analysis.processing/data/GlassyCarbon_T.dat";
	
	// Set up the getter...
	public String getAbsoluteScanFilePath() {
		return absoluteScanFilePath;
	}

	// and setter.
	public void setAbsoluteScanFilePath(String absoluteScanFilePath) {
		firePropertyChange("absoluteScanFilePath", this.absoluteScanFilePath, this.absoluteScanFilePath = absoluteScanFilePath);
	}

	
	// Get the range over which the calibration is performed
	@OperationModelField(rangevalue = RangeType.XRANGE, label = "Radial range",hint="Two values, start and end, separated by a comma i.e. 2,4.The values should match the axis selected (i.e. q, 2 theta, pixel).If you delete the text, the range is cleared and the whole lineplot is used.")
	double[] radialRange = null;

	// Set up the getter...
	public double[] getRadialRange() {
		return radialRange;
	}	
	
	// and setter.
	public void setRadialRange(double[] radialRange) {
		firePropertyChange("radialRange", this.radialRange, this.radialRange = radialRange);
	}	
}