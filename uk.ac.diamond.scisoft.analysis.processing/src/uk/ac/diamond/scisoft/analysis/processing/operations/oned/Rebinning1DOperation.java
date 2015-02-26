/*-
 * Copyright 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.processing.operations.oned;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.dataset.Slice;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.dawnsci.analysis.api.processing.OperationRank;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.IndexIterator;
import org.eclipse.dawnsci.analysis.dataset.impl.Maths;
import org.eclipse.dawnsci.analysis.dataset.metadata.AxesMetadataImpl;
import org.eclipse.dawnsci.analysis.dataset.operations.AbstractOperation;

public class Rebinning1DOperation extends AbstractOperation<Rebinning1DModel, OperationData> {

	private ILazyDataset parent = null;
	private int nBins;
	private double start;
	private double stop;
	private Dataset binEdges;
	
	@Override
	public String getId() {
		return "uk.ac.diamond.scisoft.analysis.processing.operations.oned.Rebinning1DOperation";
	}
	
	protected OperationData process(IDataset input, IMonitor monitor) throws OperationException {
		
		ILazyDataset[] axes = getFirstAxes(input);
		
		if (axes == null || axes[0] == null) throw new OperationException(this, "Cannot rebin if there is no axis");
		
		Dataset axis = (Dataset)axes[0].getSlice();
		
		//TODO this is bs. Should have an init method when the file changes.
		ILazyDataset p = getSliceSeriesMetadata(input).getParent();
		if (parent == null || parent != p) {
			parent = p;


			nBins = model.getNumberOfBins() != null ? model.getNumberOfBins() : axis.getSize(); 

			updateStartStop(axis);
			
		}
			
			double[] edges = new double[]{binEdges.getElementDoubleAbs(0),binEdges.getElementDoubleAbs(nBins)};
			IDataset rebinned = doRebinning(getMinMaxAxisArray(axis), (Dataset)input, nBins, edges);
			AxesMetadataImpl axm = new AxesMetadataImpl(1);
			Dataset ax = Maths.add(binEdges.getSlice(new int[]{1}, null ,null), binEdges.getSlice(null, new int[]{-1},null));
			ax.idivide(2);
			axm.setAxis(0, ax);
			rebinned.setMetadata(axm);
			int[] maxPos = rebinned.maxPos();
			return new OperationData(rebinned);
		
		
//		IDataset r = rebin(axis, input);
		
	}
	
	private Dataset[] getMinMaxAxisArray(Dataset axis) {
		
		DoubleDataset minD = new DoubleDataset(axis.getShape());
		DoubleDataset maxD = new DoubleDataset(axis.getShape());
		
		//TODO handle high to low order
		
		IndexIterator it = axis.getIterator();
		double min = 0;
		double max = 0;
		double val = 0;
		while (it.hasNext()) {
			
			val = axis.getElementDoubleAbs(it.index);
			
			if (it.index == 0) {
				min = axis.getElementDoubleAbs(it.index+1);
				min = val - (min-val);
			} else {
				min = axis.getElementDoubleAbs(it.index-1);
			}
			
			min = val - (val - min)/2;
			
			if (it.index < axis.getSize()-1) {
				max = axis.getElementDoubleAbs(it.index+1);
			} else {
				max = axis.getElementDoubleAbs(it.index-1);
				max = val + (val - max);
			}
			
			max = val + (max- val)/2;
			
			minD.setAbs(it.index, min);
			maxD.setAbs(it.index, max);
			
		}
		
		return new Dataset[]{minD,maxD};
	}
	
	
	private IDataset doRebinning(Dataset[] minMaxAxis, Dataset data, int nbins, double[] edges) {
		
		final double lo = edges[0];
		final double hi = edges[1];
		final double span = (hi - lo)/nbins;
		DoubleDataset histo = new DoubleDataset(nbins);
		DoubleDataset intensity = new DoubleDataset(nbins);
		final double[] h = histo.getData();
		final double[] in = intensity.getData();
		
		//TODO when span <= 0
		
		IndexIterator it = data.getIterator();


		while (it.hasNext()) {
			
			//scale if pixel range not fully covered by bin range
			
			double rangeScale = 1;
			//TODO calcuate scaling
			
			double sig = data.getElementDoubleAbs(it.index);
			double aMin = minMaxAxis[0].getElementDoubleAbs(it.index);
			double aMax = minMaxAxis[1].getElementDoubleAbs(it.index);
			
			//scale if all signal not in bin range
			sig *= rangeScale;
			
			if (aMax < lo || aMin > hi) {
				continue;
			}
			
			double minBinExact = (aMin-lo)/span;
			double maxBinExact = (aMax-lo)/span;

			int minBin = (int)minBinExact;
			int maxBin = (int)maxBinExact;
			
			if (minBin == maxBin) {
				h[minBin]++;
				in[minBin] += sig;
			} else {
				
				double iPerPixel = 1/(maxBinExact-minBinExact);
				double minFrac = 1-(minBinExact-minBin);
				double maxFrac = maxBinExact-maxBin;
				
				if (minBin >= 0 && minBin < h.length) {
					h[minBin]+=(iPerPixel*minFrac);
					in[minBin] += (sig*iPerPixel*minFrac);
				}

				if (maxBin < h.length && maxBin >=0) {
					h[maxBin]+=(iPerPixel*maxFrac);
					in[maxBin] += (sig*iPerPixel*maxFrac);
				}


				for (int i = (minBin+1); i < maxBin; i++) {
					if (i >= h.length || i < 0) continue; 
					h[i]+=iPerPixel;
					in[i] += (sig*iPerPixel);
				}
				
			}
		}
		
		intensity.idivide(histo);
		DatasetUtils.makeFinite(intensity);
		
		intensity.setName(data.getName() + "_integrated");
		
		return intensity;
		
	}
	
//	private IDataset rebin(IDataset axis, IDataset data) {
//		//TODO check axis inc/dec
//		int[] shape = new int[]{nBins};
//		
//		Dataset i = DatasetFactory.zeros(shape, Dataset.FLOAT64);
//		Dataset n = DatasetFactory.zeros(shape, Dataset.FLOAT64);
//		Dataset d = DatasetUtils.convertToDataset(data);
//		Dataset a = DatasetUtils.convertToDataset(axis);
//		
//		final double span = (binEdges.getElementDoubleAbs(0) - lo)/nins;
//		
//		IndexIterator it = d.getIterator();
//		
//		double r0 = 0;
//		double r1 = 0;
//		double aVal = 0;
//		double dVal = 0;
//		
//		while (it.hasNext()) {
//			
//			aVal = a.getElementDoubleAbs(it.index);
//			dVal = d.getElementDoubleAbs(it.index);
//			
//			
//			if (it.index != 0) {
//				r0 = a.getElementDoubleAbs(it.index-1);
//				r0 = aVal - (aVal-r0)/2;
//			}
//			
//			if (it.index < d.getSize()-1) {
//				r1 = a.getElementDoubleAbs(it.index+1);
//				r1 = aVal + (r1-aVal)/2;
//			}
//			
//			
//		}
//		
//		return null;
//	}

	private void updateStartStop(IDataset axis) {

		double st = model.getMin() != null ? model.getMin() : axis.min().doubleValue();
		double sp = model.getMax() != null ? model.getMax() : axis.max().doubleValue();
		
		double shift = (sp- st)/(2*nBins);
		start = st - shift;
		stop = sp + shift;
		binEdges = DatasetUtils.linSpace(start,stop,nBins+1, Dataset.FLOAT64);
	}

	@Override
	public OperationRank getInputRank() {
		return OperationRank.ONE;
	}

	@Override
	public OperationRank getOutputRank() {
		return OperationRank.ONE;
	}

	private IDataset generateBins(IDataset axis, Rebinning1DModel model){
		
		int nBins = axis.getSize();
		double min = axis.min().doubleValue();
		double max = axis.max().doubleValue();
		double offset = (max - min)/(2*nBins);
		max += offset;
		min -= offset;
		
		
		
		if (model.getNumberOfBins() != null) nBins = model.getNumberOfBins();
		if (model.getMax() != null) max = model.getMax();
		if (model.getMin() != null) min = model.getMin();
		
		IDataset left = axis.getSlice(new Slice(1,axis.getSize()));
		IDataset right = axis.getSlice(new Slice(-1));
		Dataset b = (Dataset)right;
		b.isubtract(left);
		double minbin = Math.abs(b.min().doubleValue());
		
		return null;
		
		
	}
}
