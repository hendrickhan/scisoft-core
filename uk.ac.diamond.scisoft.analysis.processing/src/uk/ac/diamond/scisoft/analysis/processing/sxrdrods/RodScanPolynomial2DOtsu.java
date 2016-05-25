/*-
 * Copyright 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.processing.sxrdrods;

import org.dawnsci.boofcv.internal.BoofCVImageFilterImpl;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationRank;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.IndexIterator;
import org.eclipse.dawnsci.analysis.dataset.impl.Maths;
import org.eclipse.dawnsci.analysis.dataset.operations.AbstractOperation;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;

import uk.ac.diamond.scisoft.analysis.fitting.functions.Polynomial2D;
import uk.ac.diamond.scisoft.analysis.optimize.ApacheOptimizer;
import uk.ac.diamond.scisoft.analysis.optimize.IOptimizer;
import uk.ac.diamond.scisoft.analysis.optimize.ApacheOptimizer.Optimizer;

public class RodScanPolynomial2DOtsu extends AbstractOperation<RodScanPolynomial1DModel, OperationData> {

	
	@Override
	public String getId() {
		return "uk.ac.diamond.scisoft.analysis.processing.sxrdrods.RodScanPolynomial2DOtsu";
	}
		
	@Override
	public OperationRank getInputRank() {
		return OperationRank.TWO ;
	}

	@Override
	public OperationRank getOutputRank() {
		return OperationRank.TWO ;
	}
		
	@Override
	protected OperationData process(IDataset input, IMonitor monitor) {
			
		RectangularROI box = model.getBox();
		
		Dataset in1 = BoxSlicerRodScans.rOIBox(input, monitor, box.getIntLengths(), box.getIntPoint());
				
		IOptimizer optimizer = new ApacheOptimizer(Optimizer.SIMPLEX_NM);
		
		Polynomial2D g2 = new Polynomial2D(model.getFitPower());
		
		Dataset[] fittingBackground = BoxSlicerRodScans2D.LeftRightTopBottomBoxes(input, monitor,
				box.getIntLengths(), box.getIntPoint(), model.getBoundaryBox());
		
		try {
			optimizer.optimize(new Dataset[]{fittingBackground[0], fittingBackground[1]}, 
					fittingBackground[2], g2);
		} 
		catch (Exception e) {
		}
		
		DoubleDataset in1Background = g2.getOutputValues(box.getIntLengths(), 
				model.getBoundaryBox(), model.getFitPower());
		
		IndexIterator it = in1Background.getIterator();
		
		while (it.hasNext()) {
			double v = in1Background.getElementDoubleAbs(it.index);
			if (v < 0) in1Background.setObjectAbs(it.index, 0);
		}

//		System.out.println("### in1.getShape()[0]: " + in1.getShape()[0]+ "  in1.getShape()[1]:  "+ in1.getShape()[1] + "  @@@@@@");
//		System.out.println("### in1Background.getShape()[0]: " + in1Background.getShape()[0]+ "  in1Background.getShape()[1]:  "+ in1Background.getShape()[1] + "  @@@@@@");
		
		
		Dataset pBackgroundSubtracted = Maths.subtract(in1, in1Background, null);

		pBackgroundSubtracted.setName("pBackgroundSubtracted");

		Dataset correction = Maths.multiply(RodScanCorrections.lorentz(input), RodScanCorrections.areacor(input
				, model.getBeamCor(), model.getSpecular(),  model.getSampleSize()
				, model.getOutPlaneSlits(), model.getInPlaneSlits(), model.getBeamInPlane()
				, model.getBeamOutPlane(), model.getScalingFactor()));

		correction = Maths.multiply(RodScanCorrections.polarisation(input, model.getInPlanePolarisation()
				, model.getOutPlanePolarisation()), correction);
		
		correction = Maths.multiply(model.getScalingFactor(), correction);
			
		pBackgroundSubtracted = Maths.multiply(pBackgroundSubtracted,correction);
		
		IndexIterator it1 = pBackgroundSubtracted.getIterator();
		
		while (it1.hasNext()) {
			double q = pBackgroundSubtracted.getElementDoubleAbs(it1.index);
			if (q < 0) pBackgroundSubtracted.setObjectAbs(it1.index, 0);
		}
		
		BoofCVImageFilterImpl service = new BoofCVImageFilterImpl(); 
		
//		IDataset otsuTest = service.globalOtsuThreshold(pBackgroundSubtracted, false, true);
		
//		Dataset otsuTestOut = DatasetUtils.cast(otsuTest, Dataset.FLOAT64);
		
		Dataset fhkl = Maths.sqrt(pBackgroundSubtracted);
		
		Dataset in1Sum = DatasetFactory.createFromObject(in1.sum());
		Dataset in1BackgroundSum = DatasetFactory.createFromObject(in1Background.sum());
 		Dataset pBackgroundSubtractedSum = DatasetFactory.createFromObject(pBackgroundSubtracted.sum());
		Dataset fhklSum = DatasetFactory.createFromObject(fhkl.sum());
 	 		
		in1.setName("Region of Interest");
		in1Background.setName("Polynomial background");
		pBackgroundSubtracted.setName("Signal after polynomial background subtracted");
		fhkl.setName("fhkl");
//		otsuTestOut.setName("Otsu test");
		
		
		in1Sum.setName("Region of Interest Summed") ;
		in1BackgroundSum.setName("Polynomial background summed");
		pBackgroundSubtractedSum.setName("Signal after polynomial background subtracted summed");
		fhklSum.setName("fhkl summed");
		
		return new OperationData(in1, in1Background, pBackgroundSubtracted, fhkl, in1Sum, in1BackgroundSum, pBackgroundSubtractedSum, fhklSum);
//, otsuTestOut
	}
//
}