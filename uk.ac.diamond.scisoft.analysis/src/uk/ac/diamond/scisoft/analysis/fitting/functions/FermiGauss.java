/*
 * Copyright 2011 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.fitting.functions;

import java.io.Serializable;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;

/**
 * Class that wrappers the Fermi function from Fermi-Dirac distribution
 * y(x) = scale / (exp((x - mu)/kT) + 1) + C
 */
public class FermiGauss extends AFunction implements Serializable{
	
	private static final int GAUSSIAN_ARRAY_SIZE = 21;

	private static String cname = "Fermi * Gaussian";

	private static String[] paramNames = new String[]{"mu", "temperature", "BG_slope", "FE_step_height", "Constant", "FWHM"};

	private double mu, kT, scaleM, scaleC, C, sigma, temperature, fwhm;

	private double[] gaussianArray;

	private Fermi fermi;

	private Gaussian gauss;

	private static String cdescription = "y(x) = (scale / (exp((x - mu)/kT) + 1) + C) * exp(-((x)^2)/(2*sigma^2))";

	private static double[] params = new double[]{0,0,0,0,0,0};

	public FermiGauss(){
		this(params);
	}

	public FermiGauss(double... params) {
		super(params);
		name = cname;
		description = cdescription;
		for(int i =0; i<paramNames.length;i++)
			setParameterName(paramNames[i], i);
	}

	public FermiGauss(IParameter[] params) {
		super(params);
		name = cname;
		description = cdescription;
		for(int i =0; i<paramNames.length;i++)
			setParameterName(paramNames[i], i);
	}

	/**
	 * Construction which allows setting of all the bounds
	 * @param minMu
	 * @param maxMu
	 * @param minkT
	 * @param maxkT
	 * @param minScaleM
	 * @param maxScaleM
	 * @param minScaleC
	 * @param maxScaleC
	 * @param minC
	 * @param maxC
	 * @param minSigma
	 * @param maxSigma
	 */
	public FermiGauss(double minMu, double maxMu, double minkT, double maxkT,
					double minScaleM, double maxScaleM, double minScaleC, double maxScaleC,
					double minC, double maxC, double minSigma, double maxSigma) {

		super(6);

		getParameter(0).setLimits(minMu, maxMu);
		getParameter(0).setValue((minMu + maxMu) / 2.0);

		getParameter(1).setLimits(minkT, maxkT);
		getParameter(1).setValue((minkT + maxkT) / 2.0);
		
		getParameter(2).setLimits(minScaleM, maxScaleM);
		getParameter(2).setValue((minScaleM + maxScaleM) / 2.0);
		
		getParameter(3).setLimits(minScaleC, maxScaleC);
		getParameter(3).setValue((minScaleC + maxScaleC) / 2.0);
		
		getParameter(4).setLimits(minC, maxC);
		getParameter(4).setValue((minC + maxC) / 2.0);
		
		getParameter(5).setLimits(minSigma, maxSigma);
		getParameter(5).setValue((minSigma + maxSigma) / 2.0);

		name = cname;
		description = cdescription;
		for(int i =0; i<paramNames.length;i++)
			setParameterName(paramNames[i], i);
	}

	private void calcCachedParameters() {
		mu = getParameterValue(0);
		temperature = getParameterValue(1);
		scaleM = getParameterValue(2);
		scaleC = getParameterValue(3);
		C = getParameterValue(4);
		fwhm = getParameterValue(5);
		
		markParametersClean();
	}
	
	
	@Override
	public double val(double... values)  {
		if (areParametersDirty())
			calcCachedParameters();
		
		
		// only return the fermi function, not the convolution
		AbstractDataset fermiDS = getFermiDS(new DoubleDataset(values, new int[] {values.length}));
		return fermiDS.getDouble(0);
	}
	
	@Override
	public DoubleDataset makeDataset(IDataset... values) {
		calcCachedParameters();
		
		IDataset xAxis = values[0];
		
		AbstractDataset fermiDS = getFermiDS(xAxis);
		
		if (fwhm == 0.0) return new DoubleDataset(fermiDS);
		
		double localSigma = Math.abs(fwhm/2.35482); // convert to sigma
		
		DoubleDataset conv = DoubleDataset.ones(xAxis.getShape());
		conv.setName("Convolution");
		
		for (int i = 0; i < conv.getShape()[0]; i++) {
			Gaussian gauss = new Gaussian(xAxis.getDouble(i), localSigma, 1.0);
			DoubleDataset gaussDS = gauss.makeDataset(xAxis);
			gaussDS.idivide(gaussDS.sum());
			
			gaussDS.imultiply(fermiDS);
			
			conv.set(gaussDS.sum(), i);
		}
		
		
		return conv;
	}
	
	public AbstractDataset getFermiDS(IDataset xAxis) {
		calcCachedParameters();
		kT = temperature*8.6173324e-5;
		Fermi fermi = new Fermi(mu,kT, 1.0, 0.0);
		StraightLine sl = new StraightLine(new double[] {scaleM, scaleC});
		AbstractDataset fermiDS = fermi.makeDataset(xAxis);
		DoubleDataset slDS = sl.makeDataset(Maths.subtract(xAxis,mu));
		fermiDS.imultiply(slDS);
		fermiDS.iadd(C);
		return fermiDS;
	}
	
	
	// Derived approximate fits for the solution see FermiGaussApproximateFWHM.py
	private static double[] p0Coefficients = { -7.6839838e-11, -2.7999866e-08, -1.4025827e-09 };
	private static double[] p1Coefficients = {  9.6493621e-08,  5.3075971e-06,  0.00084781716 };
	private static double[] p2Coefficients = { -2.1193838e-05,  0.00044909062, -0.017089595 };
	
	/**
	 * Method to approximate a gaussisan FWHM from an appparant temperature,
	 * @param realTemperaure the real temperature the sample is at
	 * @param fittedTemperature the temperature the fit has given
	 * @return the FWHM of the convoluted gaussian assuming the temperature is set to the real temperature
	 */
	public double approximateFWHM(double realTemperaure, double fittedTemperature) {
		
		// first use the real temperature to approximate the paramters for the sigma fit from fitted temperature
		double p0 = p0Coefficients[0]*realTemperaure*realTemperaure +
				p0Coefficients[1]*realTemperaure + p0Coefficients[2];
		double p1 = p1Coefficients[0]*realTemperaure*realTemperaure +
				p1Coefficients[1]*realTemperaure + p1Coefficients[2];
		double p2 = p2Coefficients[0]*realTemperaure*realTemperaure +
				p2Coefficients[1]*realTemperaure + p2Coefficients[2];
		
		// now use the fitted coefficeints to get the sigma value
		double fwhm = p0*fittedTemperature*fittedTemperature +
				p1*fittedTemperature + p2;
		
		return fwhm;
	}
	
}
