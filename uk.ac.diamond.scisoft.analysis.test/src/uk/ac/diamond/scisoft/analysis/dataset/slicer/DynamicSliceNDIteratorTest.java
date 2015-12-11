/*-
 * Copyright 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.dataset.slicer;

import static org.junit.Assert.*;

import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.slicer.DynamicSliceNDIterator;
import org.junit.Test;

public class DynamicSliceNDIteratorTest {

	@Test
	public void test() {
		
		int[] shape = new int[]{1,1,100,100};
		Dataset key = DatasetFactory.zeros(new int[]{1, 1}, Dataset.INT32);
		
		DynamicSliceNDIterator gsndi = new DynamicSliceNDIterator(shape, key);
		
		assertFalse(gsndi.hasNext());
		
		shape = new int[]{1,1,100,100};
		key = DatasetFactory.ones(new int[]{1, 1}, Dataset.INT32);
		gsndi.updateShape(shape, key);
		assertTrue(gsndi.hasNext());
		SliceND currentSlice = gsndi.getCurrentSlice();
		
		assertFalse(gsndi.hasNext());
		
		shape = new int[]{1,10,100,100};
		key = DatasetFactory.zeros(new int[]{1, 10}, Dataset.INT32);
		key.setObjectAbs(0, 1);
		key.setObjectAbs(1, 1);
		key.setObjectAbs(2, 1);
		gsndi.updateShape(shape, key);
		assertTrue(gsndi.hasNext());
		assertTrue(gsndi.hasNext());
		currentSlice = gsndi.getCurrentSlice();
		assertFalse(gsndi.hasNext());
		shape = new int[]{1,50,100,100};
		key = DatasetFactory.ones(new int[]{1, 50}, Dataset.INT32);
		gsndi.updateShape(shape, key);
		int count = 0;
		while (gsndi.hasNext()) {
			gsndi.getCurrentSlice();
			count++;
		}
		
		count = 0;
		shape = new int[]{3,50,100,100};
		key = DatasetFactory.ones(new int[]{3, 50}, Dataset.INT32);
		gsndi.updateShape(shape, key);
		while (gsndi.hasNext()) {
			gsndi.getCurrentSlice();
		}
//		gsndi.reset();
//		while (gsndi.hasNext()) System.err.println(gsndi.getCurrentSlice());
	}

}
