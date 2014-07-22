/*-
 * Copyright 2012 Diamond Light Source Ltd.
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

package uk.ac.diamond.scisoft.analysis.roi;

import java.io.Serializable;
import java.util.Set;

/**
 * Class for a polygonal ROI is a closed form of a polyline ROI (end point is implicitly the start point)
 */
public class PolygonalROI extends PolylineROI implements Serializable {

	public PolygonalROI() {
		super();
	}

	public PolygonalROI(IPolylineROI poly) {
		PolylineROI c = poly instanceof PolylineROI ? ((PolylineROI) poly).copy() : new PolylineROI(poly);
		name = c.name;
		spt = c.spt;
		plot = c.plot;
		pts = c.pts;
	}

	public PolygonalROI(double[] start) {
		super(start);
	}

	/**
	 * @return number of sides (or points)
	 */
	@Override
	public int getSides() {
		return super.getNumberOfPoints();
	}

	@Override
	public PolygonalROI copy() {
		PolygonalROI c = new PolygonalROI(spt.clone());
		for (int i = 1, imax = pts.size(); i < imax; i++)
			c.insertPoint(pts.get(i).copy());
		c.name = name;
		c.plot = plot;
		return c;
	}

	@Override
	public boolean containsPoint(double x, double y) {
		if (super.containsPoint(x, y))
			return true;

		// after Eric Haines' non-division version of Stuart MacMartin's strategy
		int imax = pts.size();
		double[] pa = pts.get(imax - 1).getPointRef();
		boolean fya = pa[1] >= y; // segment crossing flag
		boolean f = false; // inside flag

		for (int i = 0; i < imax; i++) {
			double[] pb = pts.get(i).getPointRef();
			boolean fyb = pb[1] >= y;
			if (fya != fyb) { // test if in y-range of segment
				boolean fi = (pb[1] - y) * (pa[0] - pb[0]) >= (pb[0] - x) * (pa[1] - pb[1]); 
				if (fi == fyb) { // test if intersect
					f = !f;
				}
			}
			pa = pb;
			fya = fyb;
		}
		return f;
	}

	@Override
	public boolean isNearOutline(double x, double y, double distance) {
		if (super.isNearOutline(x, y, distance))
			return true;

		int imax = pts.size();
		if (imax < 2)
			return true;

		// test last segment that completes circuit
		double[] p = pts.get(imax - 1).getPointRef();
		double ox = p[0];
		double oy = p[1];
		p = pts.get(0).getPointRef();
		return ROIUtils.isNearSegment(p[0] - ox, p[1] - oy, x - ox, y - oy, distance);
	}

	@Override
	public double[] findHorizontalIntersections(double y) {
		if (!intersectHorizontal(y))
			return null;

		int n = pts.size() - 1;
		if (n == 0) {
			return pts.get(0).findHorizontalIntersections(y);
		}

		Set<Double> values = calculateHorizontalIntersections(y);
		double[] xi;
		double[] pta = pts.get(n).getPointRef();
		double[] ptb = spt;
		xi = ROIUtils.findYIntersection(pta, ptb, y);
		if (xi != null) {
			for (double x : xi)
				values.add(x);
		}

		xi = new double[values.size()];
		int i = 0;
		for (Double d : values) {
			xi[i++] = d;
		}
		return xi;
	}
}
