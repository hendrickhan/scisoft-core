/*-
 * Copyright 2013 Diamond Light Source Ltd.
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

package uk.ac.diamond.scisoft.analysis.dataset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.commons.lang.ArrayUtils;

public class InterpolatorUtils {

	public static AbstractDataset regrid(AbstractDataset data, AbstractDataset x, AbstractDataset y,
			AbstractDataset gridX, AbstractDataset gridY) throws Exception {
		
		DoubleDataset result = new DoubleDataset(gridX.shape[0], gridY.shape[0]);
		
		IndexIterator itx = gridX.getIterator();
		
		while(itx.hasNext()){
			int xindex = itx.index;
			double xPos = gridX.getDouble(xindex);
			
			IndexIterator ity = gridY.getIterator();
			while(ity.hasNext()){
				int yindex = ity.index;
				System.out.println("Testing : "+xindex+","+yindex);
				double yPos = gridX.getDouble(yindex);
				result.set(GetInterpolated(data, x, y, xPos, yPos), yindex, xindex);
				
			}
		}
		return result;
	}

	public static AbstractDataset selectDatasetRegion(AbstractDataset dataset, int x, int y, int xSize, int ySize) {
		int startX = x - xSize;
		int startY = y - ySize;
		int endX = x + xSize + 1;
		int endY = y + ySize +1;
		
		int shapeX = dataset.getShape()[0];
		int shapeY = dataset.getShape()[1];
		
		// Do edge checking
		if (startX < 0) {
			startX = 0;
			endX = 3;
		} 
		
		if (endX > shapeX) {
			endX = shapeX;
			startX = endX-3;
		}
		
		if (startY < 0) {
			startY = 0;
			endY = 3;
		}
		
		if (endY > shapeY) {
			endY = shapeY;
			startY = endY-3;
		}
		
		int[] start = new int[] { startX, startY };
		int[] stop = new int[] { endX, endY };
		
		
		return dataset.getSlice(start, stop, null);
	}
	
	public static double GetInterpolated(AbstractDataset val, AbstractDataset x, AbstractDataset y, double xPos,
			double yPos) throws Exception {
		
		// initial guess
		AbstractDataset xPosDS = x.getSlice(new int[] {0,0}, new int[] {x.getShape()[0],1}, null).isubtract(xPos);
		int xPosMin = xPosDS.minPos()[0];
		AbstractDataset yPosDS = y.getSlice(new int[] {xPosMin,0}, new int[] {xPosMin+1,y.getShape()[1]}, null).isubtract(yPos);
		int yPosMin = yPosDS.minPos()[0];
		
		
		// now search around there 5x5
		
		AbstractDataset xClipped = selectDatasetRegion(x,xPosMin,yPosMin,2,2);
		AbstractDataset yClipped = selectDatasetRegion(y,xPosMin,yPosMin,2,2);
		
		// first find the point in the arrays nearest to the point
		AbstractDataset xSquare = Maths.subtract(xClipped, xPos).ipower(2);
		AbstractDataset ySquare = Maths.subtract(yClipped, yPos).ipower(2);

		AbstractDataset total = Maths.add(xSquare, ySquare);

		int[] pos = total.minPos();

		// now pull out the region around that point, as a 3x3 grid	
		AbstractDataset xReduced = selectDatasetRegion(x, pos[0], pos[1], 1, 1);
		AbstractDataset yReduced = selectDatasetRegion(y, pos[0], pos[1], 1, 1);
		AbstractDataset valReduced = selectDatasetRegion(val, pos[0], pos[1], 1, 1);

		return GetInterpolatedResultFromNinePoints(valReduced, xReduced, yReduced, xPos, yPos);
	}

	public static double GetInterpolatedResultFromNinePoints(AbstractDataset val, AbstractDataset x, AbstractDataset y,
			double xPos, double yPos) throws Exception {
		
		// First build the nine points
		InterpolatedPoint p00 = makePoint(x, y, 0, 0);
		InterpolatedPoint p01 = makePoint(x, y, 0, 1);
		InterpolatedPoint p02 = makePoint(x, y, 0, 2);
		InterpolatedPoint p10 = makePoint(x, y, 1, 0);
		InterpolatedPoint p11 = makePoint(x, y, 1, 1);
		InterpolatedPoint p12 = makePoint(x, y, 1, 2);
		InterpolatedPoint p20 = makePoint(x, y, 2, 0);
		InterpolatedPoint p21 = makePoint(x, y, 2, 1);
		InterpolatedPoint p22 = makePoint(x, y, 2, 2);

		// now try every connection and find points that intersect with the interpolated value
		ArrayList<InterpolatedPoint> points = new ArrayList<InterpolatedPoint>();

		InterpolatedPoint A = get1DInterpolatedPoint(p00, p10, 0, xPos);
		InterpolatedPoint B = get1DInterpolatedPoint(p10, p20, 0, xPos);
		InterpolatedPoint C = get1DInterpolatedPoint(p00, p01, 0, xPos);
		InterpolatedPoint D = get1DInterpolatedPoint(p10, p11, 0, xPos);
		InterpolatedPoint E = get1DInterpolatedPoint(p20, p21, 0, xPos);
		InterpolatedPoint F = get1DInterpolatedPoint(p01, p11, 0, xPos);
		InterpolatedPoint G = get1DInterpolatedPoint(p11, p21, 0, xPos);
		InterpolatedPoint H = get1DInterpolatedPoint(p01, p02, 0, xPos);
		InterpolatedPoint I = get1DInterpolatedPoint(p11, p12, 0, xPos);
		InterpolatedPoint J = get1DInterpolatedPoint(p21, p22, 0, xPos);
		InterpolatedPoint K = get1DInterpolatedPoint(p02, p12, 0, xPos);
		InterpolatedPoint L = get1DInterpolatedPoint(p12, p22, 0, xPos);

		// Now add any to the list which are not null
		if (A != null)
			points.add(A);
		if (B != null)
			points.add(B);
		if (C != null)
			points.add(C);
		if (D != null)
			points.add(D);
		if (E != null)
			points.add(E);
		if (F != null)
			points.add(F);
		if (G != null)
			points.add(G);
		if (H != null)
			points.add(H);
		if (I != null)
			points.add(I);
		if (J != null)
			points.add(J);
		if (K != null)
			points.add(K);
		if (L != null)
			points.add(L);

		// if no intercepts, then retun NaN;
		if (points.size() == 0) return Double.NaN;
		
		InterpolatedPoint bestPoint = null;

		// sort the points by y
		Collections.sort(points, new Comparator<InterpolatedPoint>() {

			@Override
			public int compare(InterpolatedPoint o1, InterpolatedPoint o2) {
				return (int) Math.signum(o1.realPoint.getDouble(1) - o2.realPoint.getDouble(1));
			}
		});
		
		
		// now we have all the points which fit the x criteria, Find the points which fit the y
		for (int a = 1; a < points.size(); a++) {
			InterpolatedPoint testPoint = get1DInterpolatedPoint(points.get(a - 1), points.get(a), 1, yPos);
			if (testPoint != null) {
				bestPoint = testPoint;
				break;
			}
		}

		if (bestPoint == null) {
			return Double.NaN;
		}

		// now we have the best point, we can calculate the weights, and positions
		int xs = (int) Math.floor(bestPoint.getCoordPoint().getDouble(0));
		int ys = (int) Math.floor(bestPoint.getCoordPoint().getDouble(1));
		
		double xoff = bestPoint.getCoordPoint().getDouble(0) - xs;
		double yoff = bestPoint.getCoordPoint().getDouble(1) - ys;

		// check corner cases
		if (xs == 2) {
			xs = 1;
			xoff = 1.0;
		}
		
		if (ys == 2) {
			ys = 1;
			yoff = 1.0;
		}
		
		double w00 = (1 - xoff) * (1 - yoff);
		double w10 = (xoff) * (1 - yoff);
		double w01 = (1 - xoff) * (yoff);
		double w11 = (xoff) * (yoff);
		
		// now using the weights, we can get the final interpolated value
		double result = val.getDouble(xs, ys) * w00;
		result += val.getDouble(xs + 1, ys) * w10;
		result += val.getDouble(xs, ys + 1) * w01;
		result += val.getDouble(xs + 1, ys + 1) * w11;
		
		return result;
	}

	private static InterpolatedPoint makePoint(AbstractDataset x, AbstractDataset y, int i, int j) {
		DoubleDataset realPoint = new DoubleDataset(new double[] { x.getDouble(i, j), y.getDouble(i, j) }, 2);
		DoubleDataset coordPoint = new DoubleDataset(new double[] { i, j }, 2);
		return new InterpolatedPoint(realPoint, coordPoint);
	}

	/**
	 * Gets an interpolated position when only dealing with 1 dimension for the interpolation.
	 * 
	 * @param p1
	 *            Point 1
	 * @param p2
	 *            Point 2
	 * @param interpolationDimention
	 *            The dimension in which the interpolation should be carried out
	 * @param interpolatedValue
	 *            The value at which the interpolated point should be at in the chosen dimension
	 * @return the new interpolated point.
	 * @throws IllegalArgumentException
	 */
	public static InterpolatedPoint get1DInterpolatedPoint(InterpolatedPoint p1, InterpolatedPoint p2,
			int interpolationDimention, double interpolatedValue) throws IllegalArgumentException {
		
		checkPoints(p1, p2);

		if (interpolationDimention >= p1.getRealPoint().shape[0]) {
			throw new IllegalArgumentException("Dimention is too large for these datasets");
		}

		double p1_n = p1.getRealPoint().getDouble(interpolationDimention);
		double p2_n = p2.getRealPoint().getDouble(interpolationDimention);
		double max = Math.max(p1_n, p2_n);
		double min = Math.min(p1_n, p2_n);
		
		if (interpolatedValue < min || interpolatedValue > max || min==max) {
			return null;
		}
		
		double proportion = (interpolatedValue - min) / (max - min);
		
		return getInterpolatedPoint(p1, p2, proportion);
	}

	/**
	 * Gets an interpolated point between 2 points given a certain proportion
	 * 
	 * @param p1
	 *            the initial point
	 * @param p2
	 *            the final point
	 * @param proportion
	 *            how far the new point is along the path between P1(0.0) and P2(1.0)
	 * @return a new point which is the interpolated point
	 */
	private static InterpolatedPoint getInterpolatedPoint(InterpolatedPoint p1, InterpolatedPoint p2, double proportion) {

		checkPoints(p1, p2);

		if (proportion < 0 || proportion > 1.0) {
			throw new IllegalArgumentException("Proportion must be between 0 and 1");
		}

		AbstractDataset p1RealContribution = Maths.multiply(p1.getRealPoint(), (1.0 - proportion));
		AbstractDataset p2RealContribution = Maths.multiply(p2.getRealPoint(), (proportion));

		AbstractDataset realPoint = Maths.add(p1RealContribution, p2RealContribution);

		AbstractDataset p1CoordContribution = Maths.multiply(p1.getCoordPoint(), (1.0 - proportion));
		AbstractDataset p2CoordContribution = Maths.multiply(p2.getCoordPoint(), (proportion));

		AbstractDataset coordPoint = Maths.add(p1CoordContribution, p2CoordContribution);

		return new InterpolatedPoint(realPoint, coordPoint);
	}

	/**
	 * Checks to see if 2 points have the same dimensionality
	 * 
	 * @param p1
	 *            Point 1
	 * @param p2
	 *            Point 2
	 * @throws IllegalArgumentException
	 */
	private static void checkPoints(InterpolatedPoint p1, InterpolatedPoint p2) throws IllegalArgumentException {
		if (!p1.getCoordPoint().isCompatibleWith(p2.getCoordPoint())) {
			throw new IllegalArgumentException("Datasets do not match");
		}
	}

}
