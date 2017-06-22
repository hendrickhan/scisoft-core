package org.dawnsci.surfacescatter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.dawnsci.surfacescatter.AnalaysisMethodologies.FitPower;
import org.dawnsci.surfacescatter.AnalaysisMethodologies.Methodology;
import org.dawnsci.surfacescatter.MethodSettingEnum.MethodSetting;
import org.dawnsci.surfacescatter.ProcessingMethodsEnum.ProccessingMethod;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.analysis.dataset.slicer.SourceInformation;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.IndexIterator;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.metadata.Metadata;

public class DummyProcessWithFrames {
	
	
	private static Dataset yValue;
	private static int DEBUG = 0;
	private static OperationData outputOD;
	
	public static IDataset DummyProcess(DirectoryModel drm,  
										GeometricParametersModel gm, 
										int correctionSelector, 
										int k, 
										int trackingMarker,
										int selection,
										double[] locationOverride){		
		
		FrameModel fm = drm.getFms().get(selection);
		
		
		if(locationOverride == null){
			locationOverride = fm.getRoiLocation();
			if(locationOverride == null){
				int[][] lenPt = drm.getInitialLenPt();
				locationOverride = LocationLenPtConverterUtils.lenPtToLocationConverter(lenPt);
			}
		}
		
		IDataset output =null;	
		
		IDataset input = DatasetFactory.createFromObject(0);
		try {
			input = fm.getRawImageData().getSlice(new SliceND(fm.getRawImageData().getShape())).squeeze();
		} catch (DatasetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		switch(fm.getBackgroundMethdology()){
			case TWOD_TRACKING:
				
				AgnosticTrackerWithFrames ath = new AgnosticTrackerWithFrames();
				if(trackingMarker != 3 && trackingMarker != 4 && fm.getProcessingMethodSelection() != ProccessingMethod.MANUAL){
					ath.TwoDTracking3(drm, 
									  trackingMarker, 
									  k, 
									  selection);
				}
				
				locationOverride = pfixer(fm,
										  drm,
										  locationOverride);
				
				if(AnalaysisMethodologies.toInt(fm.getFitPower())<5){
								  outputOD= TwoDFittingIOp(locationOverride,
														   fm.getFitPower(),
														   fm.getBoundaryBox(),
														   drm.getInitialLenPt(),
														   input,
														   k,
														   trackingMarker);
				}
				else if (fm.getFitPower() == AnalaysisMethodologies.FitPower.TWOD_GAUSSIAN){
					outputOD= TwoDGaussianFittingIOp(locationOverride,
							   						 drm.getInitialLenPt(),
							   						 fm.getFitPower(),
							   						 fm.getBoundaryBox(),
													 input,
							   						 selection,	
							   						 trackingMarker);
				}
				else if (fm.getFitPower() == AnalaysisMethodologies.FitPower.TWOD_EXPONENTIAL){
					outputOD= TwoDExponentialFittingIOp(locationOverride,
							   							input,
							   							drm.getInitialLenPt(),
							   							fm.getFitPower(),
							   							fm.getBoundaryBox(),
							   							k,
							   							trackingMarker);
				}
			
				output = outputOD.getData();
				
				IDataset temporaryBackground = DatasetFactory.ones(new int[] {1});
				
				try{
					temporaryBackground =  (IDataset) outputOD.getAuxData()[0];
				}
				catch(Exception f){

				}
				
				drm.setTemporaryBackgroundHolder(temporaryBackground);
				
				break;
				
			case TWOD:
				
				int[] len = drm.getInitialLenPt()[0];
				int[] pt = drm.getInitialLenPt()[1];
				
				if(drm.isTrackerOn() && fm.getProcessingMethodSelection() != ProccessingMethod.MANUAL){
					AgnosticTrackerWithFrames ath1 = new AgnosticTrackerWithFrames();
					
					ath1.TwoDTracking3(drm,
									  trackingMarker, 
									  k, 
									  selection);
					
					locationOverride = pfixer(fm,
							  drm,
							  locationOverride);
				}
				
				else{	
					fm.setRoiLocation(new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]), (double) (pt[1]),
					(double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]), (double) (pt[1] + len[1])});
				}
				
				if(AnalaysisMethodologies.toInt(fm.getFitPower())<5){
					
					locationOverride = pfixer(fm,
							  drm,
							  locationOverride);
					
					int[][] hi   = new int[2][];
					
					if(fm.getRoiLocation() != null){
						hi = LocationLenPtConverterUtils.locationToLenPtConverter(fm.getRoiLocation());
						
					}
					
					else{
						hi = drm.getInitialLenPt();
					}
					
					outputOD= TwoDFittingIOp(locationOverride,
										     fm.getFitPower(),
										     fm.getBoundaryBox(),
										     hi,
										     input,
										     k,
										     trackingMarker);
					
				}
				
				else if (fm.getFitPower() == AnalaysisMethodologies.FitPower.TWOD_GAUSSIAN){
					
					outputOD= TwoDGaussianFittingIOp(locationOverride,
	   						 drm.getInitialLenPt(),
	   						 fm.getFitPower(),
	   						 fm.getBoundaryBox(),
							 input,
	   						 selection,	
	   						 trackingMarker);
				}
				else if (fm.getFitPower() == AnalaysisMethodologies.FitPower.TWOD_EXPONENTIAL){
					outputOD= TwoDExponentialFittingIOp(locationOverride,
   							input,
   							drm.getInitialLenPt(),
   							fm.getFitPower(),
   							fm.getBoundaryBox(),
   							k,
   							trackingMarker);
				}
				
			
				output = outputOD.getData();
				

				IDataset temporaryBackground1 = (IDataset) outputOD.getAuxData()[0];
				
				drm.setTemporaryBackgroundHolder(temporaryBackground1);
				
				break;
				
			case SECOND_BACKGROUND_BOX:
				
				if(drm.isTrackerOn() && fm.getProcessingMethodSelection() != ProccessingMethod.MANUAL){
					AgnosticTrackerWithFrames ath1 = new AgnosticTrackerWithFrames();
					
					ath1.TwoDTracking3(drm, 
									  trackingMarker, 
									  k, 
									  selection);
					
					locationOverride = pfixer(fm,
							  drm,
							  locationOverride);
				}
				
				else{
					
					len = drm.getInitialLenPt()[0];
					pt = drm.getInitialLenPt()[1];
					
					fm.setRoiLocation(new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]), (double) (pt[1]),
					(double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]), (double) (pt[1] + len[1])});
				}
				
				output = secondConstantROIMethod(input,
  						 						 drm,
  						 						 fm.getBackgroundMethdology(), 
  						 						 selection,
  						 						 trackingMarker,
  						 						 k);
	
				break;
				
			case OVERLAPPING_BACKGROUND_BOX:
				
				if(drm.isTrackerOn() && fm.getProcessingMethodSelection() != ProccessingMethod.MANUAL){
					AgnosticTrackerWithFrames ath1 = new AgnosticTrackerWithFrames();
					
					ath1.TwoDTracking3(drm, 
									  trackingMarker, 
									  k, 
									  selection);
					
					locationOverride = pfixer(fm,
							  drm,
							  locationOverride);
				}

				else{
					
					len = drm.getInitialLenPt()[0];
					pt = drm.getInitialLenPt()[1];
					
					fm.setRoiLocation(new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]), (double) (pt[1]),
							(double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]), (double) (pt[1] + len[1])});
				}
				
				

				output = secondConstantROIMethod(input,
						   						 drm,
						   						 fm.getBackgroundMethdology(), 
						   						 selection,
						   						 trackingMarker,
						   						 k);
				
				break;
				
			case X:
				
				
				if(drm.isTrackerOn() && fm.getProcessingMethodSelection() != ProccessingMethod.MANUAL){
					AgnosticTrackerWithFrames ath1 = new AgnosticTrackerWithFrames();
					
					ath1.TwoDTracking3(drm,
							  trackingMarker, 
							  k, 
							  selection);
					
					locationOverride = pfixer(fm,
							  drm,
							  locationOverride);
				}

				else{
					
					len = drm.getInitialLenPt()[0];
					pt = drm.getInitialLenPt()[1];
					
					fm.setRoiLocation(new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]), (double) (pt[1]),
							(double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]), (double) (pt[1] + len[1])});
				}
				
				
				OperationData outputOD2= OneDFittingIOp(LocationLenPtConverterUtils.locationToLenPtConverter(locationOverride),
						   								fm.getFitPower(),
						   								fm.getRoiLocation(),
						   								input,
						   								fm.getBoundaryBox(),
						   								Methodology.X,
						   								trackingMarker,
						   								k);
				output = outputOD2.getData();
				
				IDataset temporaryBackground2 = (IDataset) outputOD2.getAuxData()[1];
				drm.setTemporaryBackgroundHolder(temporaryBackground2);		
								
				break;
				
			case Y:
				
				
				if(drm.isTrackerOn() && fm.getProcessingMethodSelection() != ProccessingMethod.MANUAL){
					AgnosticTrackerWithFrames ath1 = new AgnosticTrackerWithFrames();
					
					ath1.TwoDTracking3(drm,
							  trackingMarker, 
							  k, 
							  selection);
					
					locationOverride = pfixer(fm,
							  drm,
							  locationOverride);
				}

				else{
					
					len = drm.getInitialLenPt()[0];
					pt = drm.getInitialLenPt()[1];
					
					fm.setRoiLocation(new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]), (double) (pt[1]),
							(double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]), (double) (pt[1] + len[1])});
				}
				
				OperationData outputOD3= OneDFittingIOp(LocationLenPtConverterUtils.locationToLenPtConverter(locationOverride),							
														fm.getFitPower(),
														fm.getRoiLocation(),
														input,
														fm.getBoundaryBox(),
														Methodology.Y,
														trackingMarker,
														k);
				output = outputOD3.getData();
				
				IDataset temporaryBackground3 = (IDataset) outputOD3.getAuxData()[1];
				drm.setTemporaryBackgroundHolder(temporaryBackground3);
				
		
				break;
		}
		

		
		if(Arrays.equals(output.getShape(), (new int[] {2,2}))){
			IndexIterator it11 = ((Dataset) output).getIterator();
			
			while (it11.hasNext()) {
				double q = ((Dataset) output).getElementDoubleAbs(it11.index);
				if (q <= 0)
					((Dataset) output).setObjectAbs(it11.index, 0.1);
			}
			return output;
		}
		
		
		yValue = correctionMethod(fm, 
								  output);
		
		
		Double intensity = (Double) DatasetUtils.cast(yValue,Dataset.FLOAT64).sum();
		Double rawIntensity = (Double) DatasetUtils.cast(output,Dataset.FLOAT64).sum();
		
		Double fhkl = (double) 0.001;
			if (intensity >=0){
				fhkl =Math.pow((Double) DatasetUtils.cast(yValue,Dataset.FLOAT64).sum(), 0.5);
		}		
			
		
		if (trackingMarker !=3 ){
			
			OutputCurvesDataPackage ocdp =  drm.getOcdp();
			int noOfFrames = drm.getFms().size();
			
			int a = fm.getDatNo();
			int b = drm.getDatFilepaths().length;
			int c = drm.getNoOfImagesInDatFile(fm.getDatNo());
					
			ocdp.addToYListForEachDat(a,b, c, k ,intensity);
			ocdp.addToYListFhklForEachDat(fm.getDatNo(),drm.getDatFilepaths().length, drm.getNoOfImagesInDatFile(fm.getDatNo()), k ,fhkl);
			ocdp.addToYListRawForEachDat(fm.getDatNo(),drm.getDatFilepaths().length, drm.getNoOfImagesInDatFile(fm.getDatNo()), k ,rawIntensity);
			
			ocdp.addyList(noOfFrames, selection ,intensity);
			ocdp.addyListFhkl(noOfFrames, selection ,fhkl);
			ocdp.addYListRawIntensity(noOfFrames, selection ,rawIntensity);
			ocdp.addOutputDatArray(noOfFrames, selection ,output);
			
			fm.setBackgroundSubtractedImage(output);
			
		}
		
		return output;
	}
	
	public static IDataset DummyProcess0(DirectoryModel drm,
										GeometricParametersModel gm, 
										int correctionSelector, 
										int k, 
										int trackingMarker,
										int selection){		
		
		IDataset output =null;	
		
		FrameModel fm = drm.getFms().get(selection);
		IDataset input = DatasetFactory.createFromObject(0);
		try {
			input = fm.getRawImageData().getSlice(new SliceND(fm.getRawImageData().getShape()));
		} catch (DatasetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		switch(fm.getBackgroundMethdology()){
			case TWOD_TRACKING:
								
				
				AgnosticTrackerWithFrames ath1 = new AgnosticTrackerWithFrames();
				
				ath1.TwoDTracking3(drm,
								  trackingMarker, 
								  k, 
								  selection);
				
				if(AnalaysisMethodologies.toInt(fm.getFitPower())<5){
					
					double[] p = new double[6];
					
					try{
						p =fm.getRoiLocation();
					}
					catch(Exception n){
						System.out.println(n.getMessage());
						p= null;
					}
					
					outputOD= TwoDFittingIOp(p,
						     fm.getFitPower(),
						     fm.getBoundaryBox(),
						     drm.getInitialLenPt(),
						     input,
						     k,
						     trackingMarker);
				}
				else if (fm.getFitPower() == AnalaysisMethodologies.FitPower.TWOD_GAUSSIAN){
					outputOD= TwoDGaussianFittingIOp(fm.getRoiLocation(),
	   						 drm.getInitialLenPt(),
	   						 fm.getFitPower(),
	   						 fm.getBoundaryBox(),
							 input,
	   						 selection,	
	   						 trackingMarker);
				}
				else if (fm.getFitPower() == AnalaysisMethodologies.FitPower.TWOD_EXPONENTIAL){
					outputOD= TwoDExponentialFittingIOp(fm.getRoiLocation(),
   							input,
   							drm.getInitialLenPt(),
   							fm.getFitPower(),
   							fm.getBoundaryBox(),
   							k,
   							trackingMarker);
				}
				
				output = outputOD.getData();
				
			
				IDataset temporaryBackground1 = (IDataset) outputOD.getAuxData()[0];
			

				drm.setTemporaryBackgroundHolder(temporaryBackground1);
				
				
				break;
			case TWOD:
				
				int[] len = drm.getInitialLenPt()[0];
				int[] pt = drm.getInitialLenPt()[1];
				
				if(drm.isTrackerOn() && fm.getProcessingMethodSelection() != ProccessingMethod.MANUAL){
					AgnosticTrackerWithFrames ath = new AgnosticTrackerWithFrames();
					
					ath.TwoDTracking3(drm,
									  trackingMarker, 
									  k, 
									  selection);
				}
				
				else{	
					fm.setRoiLocation(new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]), (double) (pt[1]),
					(double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]), (double) (pt[1] + len[1])});
				}
				
				if(AnalaysisMethodologies.toInt(fm.getFitPower())<5){
					outputOD= TwoDFittingIOp(fm.getRoiLocation(),
							   fm.getFitPower(),
							   fm.getBoundaryBox(),
							   drm.getInitialLenPt(),
							   input,
							   k,
							   trackingMarker);
				}
				else if (fm.getFitPower() == AnalaysisMethodologies.FitPower.TWOD_GAUSSIAN){
					outputOD= TwoDGaussianFittingIOp(fm.getRoiLocation(),
	   						 drm.getInitialLenPt(),
	   						 fm.getFitPower(),
	   						 fm.getBoundaryBox(),
							 input,
	   						 selection,	
	   						 trackingMarker);
				}
				else if (fm.getFitPower() == AnalaysisMethodologies.FitPower.TWOD_EXPONENTIAL){
					outputOD= TwoDExponentialFittingIOp(fm.getRoiLocation(),
   							input,
   							drm.getInitialLenPt(),
   							fm.getFitPower(),
   							fm.getBoundaryBox(),
   							k,
   							trackingMarker);
				}
				
				
				output = outputOD.getData();
				
			
				temporaryBackground1 = (IDataset) outputOD.getAuxData()[0];
				drm.setTemporaryBackgroundHolder(temporaryBackground1);
				
				
				break;
			case SECOND_BACKGROUND_BOX:

				if(drm.isTrackerOn() && fm.getProcessingMethodSelection() != ProccessingMethod.MANUAL){
					AgnosticTrackerWithFrames ath = new AgnosticTrackerWithFrames();
					
					ath.TwoDTracking3(drm,
									  trackingMarker, 
									  k, 
									  selection);
			
				}

				else{
					
					len = drm.getInitialLenPt()[0];
					pt = drm.getInitialLenPt()[1];
					
					fm.setRoiLocation(new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]), (double) (pt[1]),
							(double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]), (double) (pt[1] + len[1])});
				}
				
				input = input.squeeze();

				output = secondConstantROIMethod(input.squeeze(),
						   						 drm,
						   						 fm.getBackgroundMethdology(), 
						   						 selection,
						   						 trackingMarker,
						   						 k);

				break;
				
			case OVERLAPPING_BACKGROUND_BOX:
		
				if(drm.isTrackerOn() && fm.getProcessingMethodSelection() != ProccessingMethod.MANUAL){
					AgnosticTrackerWithFrames ath = new AgnosticTrackerWithFrames();
					
					ath.TwoDTracking3(drm,
									  trackingMarker, 
									  k, 
									  selection);
			
				}

				else{
					
					len = drm.getInitialLenPt()[0];
					pt = drm.getInitialLenPt()[1];
					
					fm.setRoiLocation(new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]), (double) (pt[1]),
							(double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]), (double) (pt[1] + len[1])});
				}

				output = secondConstantROIMethod(input,
						   						 drm,
						   						 fm.getBackgroundMethdology(), 
						   						 selection,
						   						 trackingMarker,
						   						 k);
				
				break;
		
			case X:
				
				
				if(drm.isTrackerOn() && fm.getProcessingMethodSelection() != ProccessingMethod.MANUAL){
					AgnosticTrackerWithFrames ath = new AgnosticTrackerWithFrames();
					
					ath.TwoDTracking3(drm,
									  trackingMarker, 
									  k, 
									  selection);
			
				}
				
				else{
					
					len = drm.getInitialLenPt()[0];
					pt = drm.getInitialLenPt()[1];
					
					fm.setRoiLocation(new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]), (double) (pt[1]),
							(double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]), (double) (pt[1] + len[1])});
				}
				
				OperationData outputOD2= OneDFittingIOp(//drm.getLenPtForEachDat()[k],
							drm.getInitialLenPt(),
							fm.getFitPower(),
							fm.getRoiLocation(),
							input,
							fm.getBoundaryBox(),
							Methodology.X,
							trackingMarker,
							k);
				output = outputOD2.getData();
				
				IDataset temporaryBackground2 = (IDataset) outputOD2.getAuxData()[1];
				drm.setTemporaryBackgroundHolder(temporaryBackground2);

				break;
											
			case Y:
				
				
				if(drm.isTrackerOn() && fm.getProcessingMethodSelection() != ProccessingMethod.MANUAL){
					AgnosticTrackerWithFrames ath2 = new AgnosticTrackerWithFrames();
					
					ath2.TwoDTracking3(drm,
									  trackingMarker, 
									  k, 
									  selection);
			
				}

				else{
					
					len = drm.getInitialLenPt()[0];
					pt = drm.getInitialLenPt()[1];
					
					fm.setRoiLocation(new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]), (double) (pt[1]),
							(double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]), (double) (pt[1] + len[1])});
				}
				
				OperationData outputOD3= OneDFittingIOp(//drm.getLenPtForEachDat()[k],
							drm.getInitialLenPt(),
							fm.getFitPower(),
							fm.getRoiLocation(),
							input,
							fm.getBoundaryBox(),
							Methodology.Y,
							trackingMarker,
							k);
				output = outputOD3.getData();
									
				IDataset temporaryBackground3 = (IDataset) outputOD3.getAuxData()[1];
				drm.setTemporaryBackgroundHolder(temporaryBackground3);
				
				
				break;
		}
	
		yValue = correctionMethod(fm, 
				  output);

		try {
			Thread.sleep(0);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	
		Double intensity = (Double) DatasetUtils.cast(yValue,Dataset.FLOAT64).sum();
		Double rawIntensity = (Double) DatasetUtils.cast(output,Dataset.FLOAT64).sum();
		
		Double fhkl = (double) 0.001;
		if (intensity >=0){
			fhkl =Math.pow((Double) DatasetUtils.cast(yValue,Dataset.FLOAT64).sum(), 0.5);
		}	

		
		if (trackingMarker !=3 ){
			
			
			OutputCurvesDataPackage ocdp =  drm.getOcdp();
			int noOfFrames = drm.getFms().size();
			
			ocdp.addToYListForEachDat(fm.getDatNo(),drm.getDatFilepaths().length, drm.getNoOfImagesInDatFile(fm.getDatNo()), k ,intensity);
			ocdp.addToYListFhklForEachDat(fm.getDatNo(),drm.getDatFilepaths().length, drm.getNoOfImagesInDatFile(fm.getDatNo()), k ,fhkl);
			ocdp.addToYListRawForEachDat(fm.getDatNo(),drm.getDatFilepaths().length, drm.getNoOfImagesInDatFile(fm.getDatNo()), k ,rawIntensity);
			
			ocdp.addyList(noOfFrames, selection ,intensity);
			ocdp.addyListFhkl(noOfFrames, selection ,fhkl);
			ocdp.addYListRawIntensity(noOfFrames, selection ,rawIntensity);
			ocdp.addOutputDatArray(noOfFrames, selection ,output);
			
			fm.setBackgroundSubtractedImage(output);
			
		}

		return output;
	}
	
	public static IDataset DummyProcess1(DirectoryModel drm, 
										GeometricParametersModel gm, 
										int correctionSelector, 
										int k, 
										int trackingMarker,
										int selection){		
		
		IDataset output =null;	
		
		FrameModel fm = drm.getFms().get(selection);
		IDataset input = DatasetFactory.createFromObject(0);
		try {
			input = fm.getRawImageData().getSlice(new SliceND(fm.getRawImageData().getShape()));
		} catch (DatasetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		switch(fm.getBackgroundMethdology()){
			case TWOD_TRACKING:
												
				AgnosticTrackerWithFrames ath = new AgnosticTrackerWithFrames();
				
				ath.TwoDTracking3(drm, 
									  trackingMarker, 
									  k, 
									  selection);
				
				
				if(AnalaysisMethodologies.toInt(fm.getFitPower())<5){
					double[] p = new double[6];
					
					try{
						p =fm.getRoiLocation();
					}
					catch(Exception n){
						System.out.println(n.getMessage());
						p= null;
					}
					
					  outputOD= TwoDFittingIOp(fm.getRoiLocation(),
							   fm.getFitPower(),
							   fm.getBoundaryBox(),
							   drm.getInitialLenPt(),
							   input,
							   k,
							   trackingMarker);
					  
				}
				else if (fm.getFitPower() == AnalaysisMethodologies.FitPower.TWOD_GAUSSIAN){
					outputOD= TwoDGaussianFittingIOp(fm.getRoiLocation(),
	   						 drm.getInitialLenPt(),
	   						 fm.getFitPower(),
	   						 fm.getBoundaryBox(),
							 input,
	   						 selection,	
	   						 trackingMarker);
				}
				else if (fm.getFitPower() == AnalaysisMethodologies.FitPower.TWOD_EXPONENTIAL){
					outputOD= TwoDExponentialFittingIOp(fm.getRoiLocation(),
   							input,
   							drm.getInitialLenPt(),
   							fm.getFitPower(),
   							fm.getBoundaryBox(),
   							k,
   							trackingMarker);
				}
				
				output = outputOD.getData();
				
				IDataset temporaryBackground = (IDataset) outputOD.getAuxData()[0];

				drm.setTemporaryBackgroundHolder(temporaryBackground);
				
				break;
				
			case TWOD:
				
				
				if(drm.isTrackerOn() && fm.getProcessingMethodSelection() != ProccessingMethod.MANUAL){
					
					
					AgnosticTrackerWithFrames ath1 = new AgnosticTrackerWithFrames();
					
					ath1.TwoDTracking3(drm, 
										  trackingMarker, 
										  k, 
										  selection);
					
				}

				else{
					
					int[] len = drm.getInitialLenPt()[0];
					int[] pt = drm.getInitialLenPt()[1];
					
					fm.setRoiLocation(new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]), (double) (pt[1]),
							(double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]), (double) (pt[1] + len[1])});
				}
				
				if(AnalaysisMethodologies.toInt(fm.getFitPower())<5){
					  outputOD= TwoDFittingIOp(fm.getRoiLocation(),
							   fm.getFitPower(),
							   fm.getBoundaryBox(),
							   drm.getInitialLenPt(),
							   input,
							   k,
							   trackingMarker);
				}
				else if (fm.getFitPower() == AnalaysisMethodologies.FitPower.TWOD_GAUSSIAN){
					outputOD= TwoDGaussianFittingIOp(fm.getRoiLocation(),
	   						 drm.getInitialLenPt(),
	   						 fm.getFitPower(),
	   						 fm.getBoundaryBox(),
							 input,
	   						 selection,	
	   						 trackingMarker);
				}
				else if (fm.getFitPower() == AnalaysisMethodologies.FitPower.TWOD_EXPONENTIAL){
					outputOD= TwoDExponentialFittingIOp(fm.getRoiLocation(),
   							input,
   							drm.getInitialLenPt(),
   							fm.getFitPower(),
   							fm.getBoundaryBox(),
   							k,
   							trackingMarker);
				}
				
				output = outputOD.getData();
				
			
				IDataset temporaryBackground1 = (IDataset) outputOD.getAuxData()[0];
				
				drm.setTemporaryBackgroundHolder(temporaryBackground1);
				
				break;
			case SECOND_BACKGROUND_BOX:

				if(drm.isTrackerOn() && fm.getProcessingMethodSelection() != ProccessingMethod.MANUAL){
					
					

					AgnosticTrackerWithFrames ath1 = new AgnosticTrackerWithFrames();
					
					ath1.TwoDTracking3(drm, 
										  trackingMarker, 
										  k, 
										  selection);
					
				}

				else{
					
					int[] len = drm.getInitialLenPt()[0];
					int[]  pt = drm.getInitialLenPt()[1];
					
					fm.setRoiLocation(new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]), (double) (pt[1]),
							(double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]), (double) (pt[1] + len[1])});
				}
				
				output = secondConstantROIMethod(input,
						   						 drm,
						   						 fm.getBackgroundMethdology(), 
						   						 selection,
						   						 trackingMarker,
						   						 k);
				
				
				break;
				
			case OVERLAPPING_BACKGROUND_BOX:
				
				
				if(drm.isTrackerOn() && fm.getProcessingMethodSelection() != ProccessingMethod.MANUAL){

					AgnosticTrackerWithFrames ath1 = new AgnosticTrackerWithFrames();
					
					ath1.TwoDTracking3(drm, 
										  trackingMarker, 
										  k, 
										  selection);
					
				}

				else{
					
					int[] len = drm.getInitialLenPt()[0];
					int[] pt = drm.getInitialLenPt()[1];
					
					fm.setRoiLocation(new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]), (double) (pt[1]),
							(double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]), (double) (pt[1] + len[1])});
				}

				output = secondConstantROIMethod(input,
						   						 drm,
						   						 fm.getBackgroundMethdology(), 
						   						 selection,
						   						 trackingMarker,
						   						 k);
				
				break;
				
			case X:
				
				
				if(drm.isTrackerOn() && fm.getProcessingMethodSelection() != ProccessingMethod.MANUAL){

					AgnosticTrackerWithFrames ath1 = new AgnosticTrackerWithFrames();
					
					ath1.TwoDTracking3(drm, 
										  trackingMarker, 
										  k, 
										  selection);
				}

				else{
					
					int[] len = drm.getInitialLenPt()[0];
					int[] pt = drm.getInitialLenPt()[1];
					
					fm.setRoiLocation(new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]), (double) (pt[1]),
							(double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]), (double) (pt[1] + len[1])});
				}
				
				OperationData outputOD2= OneDFittingIOp(//drm.getLenPtForEachDat()[k],
							drm.getInitialLenPt(),
							fm.getFitPower(),
							fm.getRoiLocation(),
							input,
							fm.getBoundaryBox(),
							Methodology.X,
							trackingMarker,
							k);
				
				output = outputOD2.getData();
				
				IDataset temporaryBackground2 = (IDataset) outputOD2.getAuxData()[1];
				drm.setTemporaryBackgroundHolder(temporaryBackground2);
									
				break;
											
			case Y:
				
				if(drm.isTrackerOn() && fm.getProcessingMethodSelection() != ProccessingMethod.MANUAL){
					
					AgnosticTrackerWithFrames ath1 = new AgnosticTrackerWithFrames();
					
					ath1.TwoDTracking3(drm, 
										  trackingMarker, 
										  k, 
										  selection);
					
				}

				else{
					
					int[] len = drm.getInitialLenPt()[0];
					int[] pt = drm.getInitialLenPt()[1];
					
					fm.setRoiLocation(new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]), (double) (pt[1]),
							(double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]), (double) (pt[1] + len[1])});
				}
				
				OperationData outputOD3= OneDFittingIOp(//drm.getLenPtForEachDat()[k],
							drm.getInitialLenPt(),
							fm.getFitPower(),
							fm.getRoiLocation(),
							input,
							fm.getBoundaryBox(),
							Methodology.Y,
							trackingMarker,
							k);
				
				output = outputOD3.getData();
				
				IDataset temporaryBackground3 = (IDataset) outputOD3.getAuxData()[1];
				drm.setTemporaryBackgroundHolder(temporaryBackground3);
				
									
				break;
		}
				
		yValue = correctionMethod(fm, 
				  output);

		try {
			Thread.sleep(0);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
				
		Double intensity = (Double) DatasetUtils.cast(yValue,Dataset.FLOAT64).sum();
		Double rawIntensity = (Double) DatasetUtils.cast(output,Dataset.FLOAT64).sum();
		
		Double fhkl = (double) 0.001;
		if (intensity >=0){
			fhkl =Math.pow((Double) DatasetUtils.cast(yValue,Dataset.FLOAT64).sum(), 0.5);
		}	
		
		
		if (trackingMarker !=3 ){
			
			OutputCurvesDataPackage ocdp =  drm.getOcdp();
			int noOfFrames = drm.getFms().size();
			
			ocdp.addToYListForEachDat(fm.getDatNo(),drm.getDatFilepaths().length, drm.getNoOfImagesInDatFile(fm.getDatNo()), k ,intensity);
			ocdp.addToYListFhklForEachDat(fm.getDatNo(),drm.getDatFilepaths().length, drm.getNoOfImagesInDatFile(fm.getDatNo()), k ,fhkl);
			ocdp.addToYListRawForEachDat(fm.getDatNo(),drm.getDatFilepaths().length, drm.getNoOfImagesInDatFile(fm.getDatNo()), k ,rawIntensity);
			
			ocdp.addyList(noOfFrames, selection ,intensity);
			ocdp.addyListFhkl(noOfFrames, selection ,fhkl);
			ocdp.addYListRawIntensity(noOfFrames, selection ,rawIntensity);
			ocdp.addOutputDatArray(noOfFrames, selection ,output);
			
			fm.setBackgroundSubtractedImage(output);

			debug("  intensity:  " + intensity + "   k: " + k);
		}
		
		
		debug("intensity added to dm: " + intensity + "   local k: " + k + "   selection: " + selection);
		
		return output;
	}
	
	
	public static IDataset DummyProcess1(DirectoryModel drm,  
										GeometricParametersModel gm, 
										int correctionSelector, 
										int k, 
										int trackingMarker,
										int selection,
										double[]locationList){		
		
		if(locationList == null){
			System.out.println("Warning!!!!!!");
		}
		
		////////////////////////////////NB selection is position in the sorted list of the whole rod k is position in the .dat file
		IDataset output =null;	
		
		FrameModel fm = drm.getFms().get(selection);
		IDataset input = DatasetFactory.createFromObject(0);
		try {
			input = fm.getRawImageData().getSlice(new SliceND(fm.getRawImageData().getShape()));
		} catch (DatasetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		switch(fm.getBackgroundMethdology()){
			case TWOD_TRACKING:
								
				AgnosticTrackerWithFrames ath = new AgnosticTrackerWithFrames();
				
				ath.TwoDTracking1(drm, 
						trackingMarker, 
						k,
						locationList,
						selection);
				

				
				if(AnalaysisMethodologies.toInt(fm.getFitPower())<5){
					  outputOD= TwoDFittingIOp(fm.getRoiLocation(),
							   fm.getFitPower(),
							   fm.getBoundaryBox(),
							   drm.getInitialLenPt(),
							   input,
							   k,
							   trackingMarker);
				}
				else if (fm.getFitPower() == AnalaysisMethodologies.FitPower.TWOD_GAUSSIAN){
					outputOD= TwoDGaussianFittingIOp(fm.getRoiLocation(),
	   						 drm.getInitialLenPt(),
	   						 fm.getFitPower(),
	   						 fm.getBoundaryBox(),
							 input,
	   						 selection,	
	   						 trackingMarker);
				}
				else if (fm.getFitPower() == AnalaysisMethodologies.FitPower.TWOD_EXPONENTIAL){
					outputOD= TwoDExponentialFittingIOp(fm.getRoiLocation(),
   							input,
   							drm.getInitialLenPt(),
   							fm.getFitPower(),
   							fm.getBoundaryBox(),
   							k,
   							trackingMarker);
				}
				
				output = outputOD.getData();
				
			
				IDataset temporaryBackground1 = (IDataset) outputOD.getAuxData()[0];

				
				drm.setTemporaryBackgroundHolder(temporaryBackground1);

				break;
				
			case TWOD:
			
				if(drm.isTrackerOn() && 
				   fm.getProcessingMethodSelection() != ProccessingMethod.MANUAL){
					AgnosticTrackerWithFrames ath2 = new AgnosticTrackerWithFrames();
					
					ath2.TwoDTracking1(drm, 
							trackingMarker, 
							k,
							locationList,
							selection);
				}

				else{
					
					int[] len = drm.getInitialLenPt()[0];
					int[] pt = drm.getInitialLenPt()[1];
					

					fm.setRoiLocation(new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]), (double) (pt[1]),
							(double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]), (double) (pt[1] + len[1])});
				}
				
				if(AnalaysisMethodologies.toInt(fm.getFitPower())<5){
					  outputOD= TwoDFittingIOp(fm.getRoiLocation(),
							   fm.getFitPower(),
							   fm.getBoundaryBox(),
							   drm.getInitialLenPt(),
							   input,
							   k,
							   trackingMarker);
				}
				else if (fm.getFitPower() == AnalaysisMethodologies.FitPower.TWOD_GAUSSIAN){
					outputOD= TwoDGaussianFittingIOp(fm.getRoiLocation(),
	   						 drm.getInitialLenPt(),
	   						 fm.getFitPower(),
	   						 fm.getBoundaryBox(),
							 input,
	   						 selection,	
	   						 trackingMarker);
				}
				else if (fm.getFitPower() == AnalaysisMethodologies.FitPower.TWOD_EXPONENTIAL){
					outputOD= TwoDExponentialFittingIOp(fm.getRoiLocation(),
   							input,
   							drm.getInitialLenPt(),
   							fm.getFitPower(),
   							fm.getBoundaryBox(),
   							k,
   							trackingMarker);
				}
				
				output = outputOD.getData();
				
			
				temporaryBackground1 = (IDataset) outputOD.getAuxData()[0];				
			
				drm.setTemporaryBackgroundHolder(temporaryBackground1);
				
				break;
			case SECOND_BACKGROUND_BOX:

				if(drm.isTrackerOn() && fm.getProcessingMethodSelection() != ProccessingMethod.MANUAL){
					AgnosticTrackerWithFrames ath2 = new AgnosticTrackerWithFrames();
					
					ath2.TwoDTracking1(drm, 
							trackingMarker, 
							k,
							locationList,
							selection);
				}

				else{
					
					int[] len = drm.getInitialLenPt()[0];
					int[] pt = drm.getInitialLenPt()[1];
					

					fm.setRoiLocation(new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]), (double) (pt[1]),
							(double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]), (double) (pt[1] + len[1])});
				}
	
				output = secondConstantROIMethod(input,
 						 drm,
 						 fm.getBackgroundMethdology(), 
 						 selection,
 						 trackingMarker,
 						 k);

				
				break;
			case OVERLAPPING_BACKGROUND_BOX:

				if(drm.isTrackerOn() && fm.getProcessingMethodSelection() != ProccessingMethod.MANUAL){
					AgnosticTrackerWithFrames ath1 = new AgnosticTrackerWithFrames();
					
					ath1.TwoDTracking1(drm, 
							trackingMarker, 
							k,
							locationList,
							selection);
				}

				else{
					
					int[] len = drm.getInitialLenPt()[0];
					int[] pt = drm.getInitialLenPt()[1];
					

					fm.setRoiLocation(new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]), (double) (pt[1]),
							(double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]), (double) (pt[1] + len[1])});
				}
				
				output = secondConstantROIMethod(input,
  						 drm,
  						 fm.getBackgroundMethdology(), 
  						 selection,
  						 trackingMarker,
  						 k);
				
				break;
				
			case X:
				
				if(drm.isTrackerOn() && fm.getProcessingMethodSelection() != ProccessingMethod.MANUAL){
					AgnosticTrackerWithFrames ath1 = new AgnosticTrackerWithFrames();
					
					ath1.TwoDTracking1(drm, 
							trackingMarker, 
							k,
							locationList,
							selection);
					
				}

				else{
					
					int[] len = drm.getInitialLenPt()[0];
					int[] pt = drm.getInitialLenPt()[1];
					

					fm.setRoiLocation(new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]), (double) (pt[1]),
							(double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]), (double) (pt[1] + len[1])});
				}
				
				
				OperationData outputOD2= OneDFittingIOp(//drm.getLenPtForEachDat()[k],
						drm.getInitialLenPt(),
						fm.getFitPower(),
						fm.getRoiLocation(),
						input,
						fm.getBoundaryBox(),
						Methodology.X,
						trackingMarker,
						k);
			
				output = outputOD2.getData();

				IDataset temporaryBackground2 = (IDataset) outputOD2.getAuxData()[1];
				
				drm.setTemporaryBackgroundHolder(temporaryBackground2);
				
											
				break;
											
			case Y:
				
				if(drm.isTrackerOn() && fm.getProcessingMethodSelection() != ProccessingMethod.MANUAL){
					AgnosticTrackerWithFrames ath2 = new AgnosticTrackerWithFrames();
					
					ath2.TwoDTracking1(drm, 
							trackingMarker, 
							k,
							locationList,
							selection);

				}

				else{
					
					int[] len = drm.getInitialLenPt()[0];
					int[] pt = drm.getInitialLenPt()[1];
					

					fm.setRoiLocation(new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]), (double) (pt[1]),
							(double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]), (double) (pt[1] + len[1])});
				}
				
				OperationData outputOD3= OneDFittingIOp(//drm.getLenPtForEachDat()[k],
						drm.getInitialLenPt(),
						fm.getFitPower(),
						fm.getRoiLocation(),
						input,
						fm.getBoundaryBox(),
						Methodology.Y,
						trackingMarker,
						k);
			
			output = outputOD3.getData();

				IDataset temporaryBackground3 = (IDataset) outputOD3.getAuxData()[1];
				drm.setTemporaryBackgroundHolder(temporaryBackground3);
				
				break;
		}
		
		yValue = correctionMethod(fm, 
				  output);

		try {
			Thread.sleep(0);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Double intensity = (Double) DatasetUtils.cast(yValue,Dataset.FLOAT64).sum();
		Double rawIntensity = (Double) DatasetUtils.cast(output,Dataset.FLOAT64).sum();
		
		Double fhkl = (double) 0.001;
		if (intensity >=0){
			fhkl =Math.pow((Double) DatasetUtils.cast(yValue,Dataset.FLOAT64).sum(), 0.5);
		}	
		
		if (trackingMarker !=3 ){
			OutputCurvesDataPackage ocdp =  drm.getOcdp();
			int noOfFrames = drm.getFms().size();
			
			ocdp.addToYListForEachDat(fm.getDatNo(),drm.getDatFilepaths().length, drm.getNoOfImagesInDatFile(fm.getDatNo()), k ,intensity);
			ocdp.addToYListFhklForEachDat(fm.getDatNo(),drm.getDatFilepaths().length, drm.getNoOfImagesInDatFile(fm.getDatNo()), k, fhkl);
			ocdp.addToYListRawForEachDat(fm.getDatNo(),drm.getDatFilepaths().length, drm.getNoOfImagesInDatFile(fm.getDatNo()), k ,rawIntensity);
			
			ocdp.addyList(noOfFrames, selection ,intensity);
			ocdp.addyListFhkl(noOfFrames, selection ,fhkl);
			ocdp.addYListRawIntensity(noOfFrames, selection ,rawIntensity);
			ocdp.addOutputDatArray(noOfFrames, selection ,output);
			
			fm.setBackgroundSubtractedImage(output);
			debug("  intensity:  " + intensity + "   k: " + k);
		}
		debug("intensity added to dm: " + intensity + "   local k: " + k + "   selection: " + selection);
		
		return output;
	}
	
	public static OperationData TwoDFittingIOp(double[] p,   // = sm.getLocationList().get(k)
											   FitPower fp,  //model.getFitPower()
											   int boundaryBox, //model.getBoundaryBox()
											   int[][] initialLenPt,  //sm.getInitialLenPt()
											   IDataset input,
											   int k,
											   int trackingMarker){

		TwoDFittingModel tdfm = new TwoDFittingModel();
	
		input.squeeze();

				int[] pt = new int[]{(int) p[0], (int) p[1]}; 
				int[] len = initialLenPt[0]; 
				int[][] lenPt = new int[][] {len,pt};
				
				if(p[0] != 0 && p[1] != 0){
					tdfm.setLenPt(lenPt);
				}
			
				else{
					tdfm.setLenPt(initialLenPt);
				}	
		
		tdfm.setFitPower(fp);
		tdfm.setBoundaryBox(boundaryBox);
		
		Metadata md = new Metadata();
		Map<String, Integer> dumMap = new HashMap<String, Integer>();
		dumMap.put("one", 1);
		md.initialize(dumMap);
		
		ILazyDataset  ild = null;
		
		SourceInformation  si =new SourceInformation("dummy", "dummy2", ild);
		
		SliceFromSeriesMetadata sfsm = new SliceFromSeriesMetadata(si);
		
		input.setMetadata(sfsm);
		
		input.setMetadata(md);
		
		TwoDFittingUsingIOperation tdfuio = new TwoDFittingUsingIOperation();
		tdfuio.setModel(tdfm);
		
		return tdfuio.execute(input, null);
	
	}
	
	public static OperationData TwoDGaussianFittingIOp(double[] p, // = sm.getLocationList().get(k);,
													   int[][] initialLenPt, //sm.getInitialLenPt() 
													   FitPower fp,
													   int boundaryBox,
													   IDataset input,
													   int k,
													   int trackingMarker){
	
		TwoDFittingModel tdfm = new TwoDFittingModel();
		tdfm.setInitialLenPt(initialLenPt);
		
		if (trackingMarker != 3){
			

			int[] pt = new int[]{(int) p[0], (int) p[1]}; 
			int[] len = initialLenPt[0]; 
			int[][] lenPt = new int[][] {len,pt};
			if(p[0] != 0 && p[1] != 0){
				tdfm.setLenPt(lenPt);
			}
			
			else{
				tdfm.setLenPt(initialLenPt);
			}
		}
		
		else{
			tdfm.setLenPt(initialLenPt);
		}
		
		tdfm.setFitPower(fp);
		tdfm.setBoundaryBox(boundaryBox);
		
		Metadata md = new Metadata();
		Map<String, Integer> dumMap = new HashMap<String, Integer>();
		dumMap.put("one", 1);
		md.initialize(dumMap);
		
		ILazyDataset  ild = null;
		
		SourceInformation  si =new SourceInformation("dummy", "dummy2", ild);
		
		SliceFromSeriesMetadata sfsm = new SliceFromSeriesMetadata(si);
		
		input.setMetadata(sfsm);
		
		input.setMetadata(md);
		
		TwoDGaussianFittingUsingIOperation tdgfuio = new TwoDGaussianFittingUsingIOperation();
		tdgfuio.setModel(tdfm);
		
		return tdgfuio.execute(input, null);

	}
	
	
	public static OperationData TwoDExponentialFittingIOp(double[] p, // = sm.getLocationList().get(k);
														  IDataset input,
														  int[][] initialLenPt, // = sm.getInitialLenPt()[0];
														  FitPower fp,
														  int boundaryBox,
														  int k,
														  int trackingMarker){


		TwoDFittingModel tdfm = new TwoDFittingModel();
		tdfm.setInitialLenPt(initialLenPt);
		
		if (trackingMarker != 3){
			
			int[] pt = new int[]{(int) p[0], (int) p[1]}; 
			int[] len = initialLenPt[0]; 
			int[][] lenPt = new int[][] {len,pt};
			
			if(p[0] != 0 && p[1] != 0){
				tdfm.setLenPt(lenPt);
			}
			
			else{
				tdfm.setLenPt(initialLenPt);
			}
		}
		
		else{
			tdfm.setLenPt(initialLenPt);
		}
		
		tdfm.setFitPower(fp);
		tdfm.setBoundaryBox(boundaryBox);
		
		Metadata md = new Metadata();
		Map<String, Integer> dumMap = new HashMap<String, Integer>();
		dumMap.put("one", 1);
		md.initialize(dumMap);
		
		ILazyDataset  ild = null;
		
		SourceInformation  si =new SourceInformation("dummy", "dummy2", ild);
		
		SliceFromSeriesMetadata sfsm = new SliceFromSeriesMetadata(si);
		
		input.setMetadata(sfsm);
		
		input.setMetadata(md);
		
		RefinedTwoDExponentialFittingUsingIOperation rtdefuio = new RefinedTwoDExponentialFittingUsingIOperation();
		rtdefuio.setModel(tdfm);
		
		return rtdefuio.execute(input, null);
		
	}
	
	
	public static OperationData OneDFittingIOp(//int[][] mLenPt, //model.getLenPt()
											   int[][] initialLenPt, // sm.getInitialLenPt()
											   FitPower fp, // model.getFitPower()
											   double[]location, // 
											   IDataset input, //sm.getLocationList().get(k)
											   int boundaryBox, //model.getBoundaryBpx
									           AnalaysisMethodologies.Methodology am,
									           int trackingMarker,
									           int k){

		OneDFittingModel odfm = new OneDFittingModel();
		
		input.squeeze();
		odfm.setInitialLenPt(initialLenPt);
		
		int[][] mLenPt = LocationLenPtConverterUtils.locationToLenPtConverter(location);
		odfm.setLenPt(mLenPt);
		odfm.setFitPower(fp);
		odfm.setBoundaryBox(boundaryBox);
		odfm.setDirection(am);
		
		if (trackingMarker != 3){
		
			double[] p = location;
			int[] pt = new int[]{(int) p[0], (int) p[1]}; 
			int[] len = initialLenPt[0]; 
			int[][] lenPt = new int[][] {len,pt};
			
			if(p[0] != 0 && p[1] != 0){
				odfm.setLenPt(lenPt);
			}
			
			else{
			odfm.setLenPt(initialLenPt);
			}
	
		}
		
		else{
			odfm.setLenPt(initialLenPt);
		}
		
		
		Metadata md = new Metadata();
		Map<String, Integer> dumMap = new HashMap<String, Integer>();
		dumMap.put("one", 1);
		md.initialize(dumMap);
		
		ILazyDataset  ild = null;
		
		SourceInformation  si =new SourceInformation("dummy", "dummy2", ild);
		
		SliceFromSeriesMetadata sfsm = new SliceFromSeriesMetadata(si);
		
		input.setMetadata(sfsm);
		
		input.setMetadata(md);
		
		OneDFittingUsingIOperation odfuio = new OneDFittingUsingIOperation();
		odfuio.setModel(odfm);
		
		return odfuio.execute(input, null);
	
	}
		
	
	public static OperationData SecondConstantBackgroundROIFittingIOp(IDataset input1,
																	  double[] p, //  = sm.getLocationList().get(k);
																	  int[][] initialLenPt, // sm.getInitialLenPt()
																	  int[][] mLenPt, //model.getLenPt()
																	  int[][] backgroundLenPt,  //sm.getBackgroundLenPt()
																	  FitPower fp,
																	  int boundaryBox,
																	  int trackingMarker,
																	  int k){
		IDataset input = input1.squeeze();
		
		SecondConstantROIBackgroundSubtractionModel scrbm 
		= new SecondConstantROIBackgroundSubtractionModel();
		scrbm.setInitialLenPt(initialLenPt);
		scrbm.setLenPt(mLenPt);
		scrbm.setFitPower(fp);
		scrbm.setBoundaryBox(boundaryBox);
		
		if (backgroundLenPt != null){
			scrbm.setBackgroundLenPt(backgroundLenPt);
		}
		
		if (trackingMarker != 3){
		
			int[] pt = new int[]{(int) p[0], (int) p[1]}; 
			int[] len = initialLenPt[0]; 
			int[][] lenPt = new int[][] {len,pt};
			if(p[0] != 0 && p[1] != 0){
				scrbm.setLenPt(lenPt);
			}
			
			else{
				scrbm.setLenPt(initialLenPt);
			}
		
		}
		else{
			scrbm.setLenPt(initialLenPt);
		}
		
		Metadata md = new Metadata();
		Map<String, Integer> dumMap = new HashMap<String, Integer>();
		dumMap.put("one", 1);
		md.initialize(dumMap);
		
		ILazyDataset  ild = null;
		
		SourceInformation  si =new SourceInformation("dummy", "dummy2", ild);
		
		SliceFromSeriesMetadata sfsm = new SliceFromSeriesMetadata(si);
		
		input.setMetadata(sfsm);
		
		input.setMetadata(md);
		SecondConstantROIUsingIOperation scrbio = new SecondConstantROIUsingIOperation();
		scrbio.setModel(scrbm);
		
		return scrbio.execute(input, null);
	}
	
	public static IDataset secondConstantROIMethod(IDataset input1,
												   DirectoryModel drm,
												   AnalaysisMethodologies.Methodology am, 
												   int selection,
												   int trackingMarker,
												   int k){		
		
		IDataset input = input1.squeeze();
		
		OperationData outputOD4 = null;
		
		int datNo = drm.getFms().get(selection).getDatNo();
		
		if(am == Methodology.SECOND_BACKGROUND_BOX){
		
		
		
			outputOD4 = SecondConstantBackgroundROIFittingIOp(input,
										                      drm.getFms().get(selection).getRoiLocation(),
															  drm.getInitialLenPt(),
															  drm.getLenPtForEachDat()[datNo],
															  drm.getBackgroundLenPt(),
															  drm.getFms().get(k).getFitPower(),
															  drm.getFms().get(k).getBoundaryBox(),
															  trackingMarker,
															  k);
		}
		
		else if(am == Methodology.OVERLAPPING_BACKGROUND_BOX){
		
			 double[] a = drm.getFms().get(selection).getRoiLocation();
			 int[][] b = drm.getInitialLenPt();
			 
			 if(a == null){
				 a = LocationLenPtConverterUtils.lenPtToLocationConverter(b);
				 drm.getFms().get(k).setRoiLocation(a);
			 }
			 
			 int[][] c = drm.getLenPtForEachDat()[datNo];
			 int[][] d = drm.getBackgroundLenPt();
			 int[][] e = drm.getBoxOffsetLenPt();
			 int[][] f = drm.getPermanentBoxOffsetLenPt();
			 boolean g = drm.isTrackerOn();
			 FitPower h = drm.getFms().get(k).getFitPower();
			 int i = drm.getFms().get(k).getBoundaryBox();
			 int j = trackingMarker;			
		
			outputOD4 = OverlappingBackgroundROIFittingIOp(input,
														   drm.getFms().get(selection).getRoiLocation(),
					  									   drm.getInitialLenPt(),
					  									   drm.getLenPtForEachDat()[datNo],
					  									   drm.getBackgroundLenPt(),
					  									   drm.getBoxOffsetLenPt(),
					  									   drm.getPermanentBoxOffsetLenPt(),
					  									   drm.isTrackerOn(),
					  									   drm.getFms().get(k).getFitPower(),
					  									   drm.getFms().get(k).getBoundaryBox(),
					  									   trackingMarker,
					  									   k);
			
			if(outputOD4.getAuxData()[3] != null){
				drm.setBoxOffsetLenPt((int[][]) outputOD4.getAuxData()[3]);
			}
			
		}
		
		IDataset output = outputOD4.getData();
		
		if ((IROI) outputOD4.getAuxData()[1] != null){
			IRectangularROI bounds = ((IROI) outputOD4.getAuxData()[1]).getBounds();
			int[] len = bounds.getIntLengths();
			int[] pt = bounds.getIntPoint();
			
			if (Arrays.equals(len,new int[] {50, 50}) == false || 
					Arrays.equals(pt,new int[] {10, 10}) == false){
			
				drm.setBackgroundROI((IROI) outputOD4.getAuxData()[1]);
				drm.getBackgroundROIForEachDat()[drm.getFms().get(k).getDatNo()]=((IROI) outputOD4.getAuxData()[1]);;
			}
		}
			
		
		drm.setTemporaryBackgroundHolder((IDataset) outputOD4.getAuxData()[2]);
		
		
		return output;
	}
	
	public static OperationData OverlappingBackgroundROIFittingIOp(IDataset input,
																   double[] p, //  = sm.getLocationList().get(k);
																   int[][] initialLenPt, // sm.getInitialLenPt()
																   int[][] mLenPt, //model.getLenPt()
																   int[][] backgroundLenPt,  //sm.getBackgroundLenPt()
																   int[][] boxOffsetLenPt, //sm.getBoxOffsetLenPt()
																   int[][] permanentBoxOffsetLenPt, //sm.getPermanentBoxOffsetLenPt
																   boolean trackerOn,
																   FitPower fp,
																   int boundaryBox,
																   int trackingMarker,
																   int k){
		
		SecondConstantROIBackgroundSubtractionModel scrbm 
				= new SecondConstantROIBackgroundSubtractionModel();
		int[][] a= initialLenPt;
		scrbm.setInitialLenPt(a);
		int[][] b= mLenPt;
		scrbm.setLenPt(b);
		scrbm.setFitPower(fp);
		scrbm.setBoundaryBox(boundaryBox);
		scrbm.setTrackerOn(trackerOn);
		scrbm.setTrackingMarker(trackingMarker);
		
		if(boxOffsetLenPt != null){
			int[][] e = boxOffsetLenPt;
			scrbm.setBoxOffsetLenPt(e);
		}
		
		if (trackingMarker != 3){
		
			int[] pt = new int[]{(int) p[0], (int) p[1]}; 
			int[] len = initialLenPt[0]; 
			int[][] lenPt = new int[][] {len,pt};
			
			if(p[0] != 0 && p[1] != 0){
				scrbm.setLenPt(lenPt);
			}
		
			else{
				scrbm.setLenPt(initialLenPt);
			}
		
			if(permanentBoxOffsetLenPt != null){
				int[][] c = permanentBoxOffsetLenPt;
				scrbm.setBoxOffsetLenPt(c);
			}
		}
		
		else{
				scrbm.setLenPt(initialLenPt);
		}
		
		int[][]  d= backgroundLenPt;
		scrbm.setBackgroundLenPt(d);
		
		Metadata md = new Metadata();
		Map<String, Integer> dumMap = new HashMap<String, Integer>();
		dumMap.put("one", 1);
		md.initialize(dumMap);
		
		ILazyDataset  ild = null;
		
		input.squeeze();
		
		SourceInformation  si =new SourceInformation("dummy", "dummy2", ild);
		
		SliceFromSeriesMetadata sfsm = new SliceFromSeriesMetadata(si);
		
		input.setMetadata(sfsm);
		
		input.setMetadata(md);
		
		OverlappingBgBoxUsingIOperation obbio 
			= new OverlappingBgBoxUsingIOperation();
		
		obbio.setModel(scrbm);
		
		if(input == null || obbio == null){
			System.out.println("obbio or input is null");
		}
		
		
		OperationData out = obbio.execute(input, null);
		
		if(out == null){
			System.out.println("prob in out");
		}
		
		return out;
		
	}
	
	public static Dataset correctionMethod(FrameModel fm,
										   IDataset output){

		double correction = 0.001;
		int correctionSelector = MethodSetting.toInt(fm.getCorrectionSelection());
		
		yValue = DatasetFactory.zeros(new int[] {1}, Dataset.ARRAYFLOAT64);
		
		if (correctionSelector == 0){
		
		
			double lorentz = fm.getLorentzianCorrection();

			double areaCorrection = fm.getAreaCorrection(); 
			
			double polarisation = fm.getPolarisationCorrection();
			

			
			correction = lorentz* polarisation * areaCorrection;
			
			if (correction ==0){
				correction = 0.001;
			}

		
		yValue = Maths.multiply(output, correction);
		
		}
		
		else if (correctionSelector ==1){
		
		try {

			double refAreaCorrection = fm.getReflectivityAreaCorrection(); 
			
			double refFluxCorrection = fm.getReflectivityFluxCorrection(); 
			
			correction = refAreaCorrection* refFluxCorrection; 
			
			if (correction ==0){
				correction = 0.001;
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
		yValue = Maths.multiply(output, correction);
		}
		
		
		else if (correctionSelector ==2){
		
		try {
		
			double refAreaCorrection = fm.getReflectivityAreaCorrection(); 
			
			
			correction = refAreaCorrection;
			
		
			if (correction ==0){
				correction = 0.001;
			}
		} 
		
		catch (Exception e) {
		// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		yValue = Maths.multiply(output, correction);
		
		}
		
		else if (correctionSelector ==3){
		
		yValue = Maths.multiply(output, 1);
		
		}
		
		else{	
		}
		
		
		return yValue;
		
	}

	private static void debug(String output) {
		if (DEBUG == 1) {
			System.out.println(output);
		}
	}
	
	
	private static double[] pfixer(FrameModel fm,
							DirectoryModel drm,
							double[] locationOverride){
		
		double[] p = new double[6];
		
		if(AnalaysisMethodologies.toInt(fm.getFitPower())<5){
			
			try{
				if(locationOverride != null){
					p =locationOverride;
				}
				else{
					p= fm.getRoiLocation();
				}
			}
			catch(Exception n){
				System.out.println(n.getMessage());
				int[][] lenPt = drm.getInitialLenPt();
				p = LocationLenPtConverterUtils.lenPtToLocationConverter(lenPt);
			}
		}
		return p;
	}
	
}
