//package org.dawnsci.surfacescatter;
//
//import java.beans.PropertyChangeListener;
//import java.util.ArrayList;
//import java.beans.PropertyChangeListener;
//import java.beans.PropertyChangeSupport;
//
//import org.eclipse.dawnsci.analysis.api.roi.IROI;
//import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
//import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
//import org.eclipse.january.dataset.Dataset;
//import org.eclipse.january.dataset.DatasetFactory;
//import org.eclipse.january.dataset.DoubleDataset;
//import org.eclipse.january.dataset.IDataset;
//import org.eclipse.january.dataset.Maths;
//
//import uk.ac.diamond.scisoft.analysis.io.DataSetProvider;
//
//public class DataModel {
//	
//	private ArrayList<Double> xList;
//	private ArrayList<Double> yList;
//	private ArrayList<Double> yListError;
//	private ArrayList<Double> yListFhkl;
//	private ArrayList<Double> yListFhklError;
//	private ArrayList<Double> yListRaw;
//	private ArrayList<Double> yListRawError;
//	private ArrayList<Double> zList;
//	private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
//	private ArrayList<IDataset> outputDatArray;
//	private ArrayList<IDataset> backgroundDatArray;
//	private IDataset slicerBackground;
//	private RectangularROI backgroundBox;
//	private int[][] backgroundLenPt;
//	private IROI backgroundROI;
//	private String name;
//	private int[][] initialLenPt;
//	private IDataset initialDataset;
//	private IDataset yIDatasetMax;
//	private IDataset yIDatasetFhklMax;
//	private IDataset yIDatasetMin;
//	private IDataset yIDatasetFhklMin;
//	private ArrayList<double[]> locationList; 
//	private double[] seedLocation;
//	
//	
//	public ArrayList<Double> getyListRaw() {
//		return yListRaw;
//	}
//
//	public void setyListRaw(ArrayList<Double> yListRaw) {
//		this.yListRaw = yListRaw;
//	}
//
//	public ArrayList<Double> getyListRawError() {
//		return yListRawError;
//	}
//
//	public void setyListRawError(ArrayList<Double> yListRawError) {
//		this.yListRawError = yListRawError;
//	}
//
//	
//	public IROI getBackgroundROI(){
//		return backgroundROI;
//	}
//	
//	public void setBackgroundROI(IROI backgroundROI){
//		IRectangularROI bounds = backgroundROI.getBounds();
//		int[] len = bounds.getIntLengths();
//		int[] pt = bounds.getIntPoint();
//		int[][] lenpt = new int[2][];
//		lenpt[0]=len;
//		lenpt[1]=pt;
//		firePropertyChange("backgroundROI", this.backgroundROI, this.backgroundROI= backgroundROI);
//		this.setBackgroundLenPt(lenpt);
//		firePropertyChange("backgroundLenPt", this.backgroundLenPt, this.backgroundLenPt= lenpt);
//		
//	}
//	
//	public ArrayList<IDataset> getOutputDatArray() {
//		return outputDatArray;
//	}
//	public void setOutputDatArray(ArrayList<IDataset> outputDatArray) {
//		this.outputDatArray = outputDatArray;
//	}
//	
//	public ArrayList<IDataset> getBackgroundDatArray() {
//		return backgroundDatArray;
//	}
//	public void setBackgroundDatArray(ArrayList<IDataset> backgroundDatArray) {
//		this.backgroundDatArray = backgroundDatArray;
//	}
//	
//	public void addOutputDatArray(IDataset in){
//		if (outputDatArray==null){
//			outputDatArray = new ArrayList<IDataset>();
//		}
//		
//		ArrayList<IDataset> outputDatArray1 = new ArrayList<IDataset>();
//		outputDatArray1 = (ArrayList<IDataset>) outputDatArray.clone();
//		outputDatArray1.add(in);
//		firePropertyChange("outputDatArray", this.outputDatArray,
//				this.outputDatArray= outputDatArray1);
//	}
//	
//	public void addOutputDatArray(int l, int k, IDataset in){
//		if (outputDatArray==null){
//			outputDatArray = new ArrayList<IDataset>();
//			for (int i = 0; i < l; i++) {
//				outputDatArray.add(DatasetFactory.zeros(new int[] {2,2}));
//				}
//		}
//		
//		ArrayList<IDataset> outputDatArray1 = new ArrayList<IDataset>();
//		outputDatArray1 = (ArrayList<IDataset>) outputDatArray.clone();
//		outputDatArray1.set(k,in);
//		firePropertyChange("outputDatArray", this.outputDatArray,
//				this.outputDatArray= outputDatArray1);
//	}
//	
//	public void addBackgroundDatArray(IDataset in){
//		if (backgroundDatArray==null){
//			backgroundDatArray = new ArrayList<IDataset>();
//		}
//		
//		ArrayList<IDataset> backgroundDatArray1 = new ArrayList<IDataset>();
//		backgroundDatArray1 = (ArrayList<IDataset>) backgroundDatArray.clone();
//		backgroundDatArray1.add(in);
//		firePropertyChange("backgroundDatArray", this.backgroundDatArray,
//				this.backgroundDatArray= backgroundDatArray1);
//	}
//
//	public ArrayList<Double> getzList() {
//		return zList;
//		
//	}
//	public void setzList(ArrayList<Double> zList) {
//		this.zList = zList;
//		firePropertyChange("zList", this.zList,
//				this.zList= zList);
//	}
//	public ArrayList<Double> getyList() {
//		return yList;
//	}
//	
//	public void setyList(ArrayList<Double> yList) {
//		this.yList = yList;
//		yListError = new ArrayList<Double>();
//		
//		for(int i = 0; i <yList.size(); i++){
//			if (yList.get(i) >= 0){
//				yListError.add(Math.sqrt(yList.get(i)));
//			}
//			else{
//				yListError.add(0.0001);
//			}
//		}
//		firePropertyChange("yList", this.yList,
//				this.yList= yList);
//	}
//	
//	public ArrayList<Double> getxList() {
//		return xList;
//		
//	}
//	
//	public void setxList(ArrayList<Double> xList) {
//		this.xList = xList;
//		firePropertyChange("xList", this.xList,
//				this.xList= xList);
//	}
//	
//	public void addyList(double y){
//		if (yList==null){
//			yList = new ArrayList<Double>();
//		}
//		if (yListError==null){
//			yListError = new ArrayList<Double>();
//		}
//
//		ArrayList<Double> yList1 = new ArrayList<Double>();
//		yList1 = (ArrayList<Double>) yList.clone();
//		yList1.add(y);
//		
//		ArrayList<Double> yListError2 = new ArrayList<Double>();
//		yListError2 = (ArrayList<Double>) yListError.clone();
//		
//		if(y>=0){
//			yListError2.add(Math.sqrt(y));
//		}
//		else{
//			yListError2.add(0.00001);
//		}
//		yListError = yListError2;
//		
//		firePropertyChange("yList", this.yList,
//				this.yList= yList1);
//	}
//	
//	public void addyList(int l, int k, double y){
//		
//		if (yList==null){
//			yList = new ArrayList<Double>();
//			for (int i = 0; i < l; i++) {
//				  yList.add((double) 1000000000);
//				}
//		}
//		
//		if (yList.isEmpty()){
//			yList = new ArrayList<Double>();
//			for (int i = 0; i < l; i++) {
//				  yList.add((double) 1000000000);
//				}
//		}
//		
//		if (yListError==null){
//			yListError = new ArrayList<Double>();
//			for (int i = 0; i < l; i++) {
//				  yListError.add((double) 1000000000);
//				}
//		}
//		
//		if (yListError.isEmpty()){
//			yListError = new ArrayList<Double>();
//			for (int i = 0; i < l; i++) {
//				  yListError.add((double) 1000000000);
//				}
//		}
//		
//		ArrayList<Double> yList2 = new ArrayList<Double>();
//		
//		yList2 = (ArrayList<Double>) yListError.clone();
//	
//		if(y>=0){
//			yList2.set(k,Math.sqrt(y));
//		}
//		else{
//			yList2.set(k, 0.00001);
//		}
//			
//		yListError = yList2;
//		
//		ArrayList<Double> yList1 = new ArrayList<Double>();
//		yList1 = (ArrayList<Double>) yList.clone();
//		yList1.set(k, y);
//		
//		firePropertyChange("yList", this.yList,
//				this.yList= yList1);
//	}
//	
//	
//	public void addyListFhkl(double y){
//		
//		if (yListFhkl==null){
//			yListFhkl = new ArrayList<Double>();
//		}
//		
//		if (yListFhklError==null){
//			yListFhklError = new ArrayList<Double>();
//		}
//
//		ArrayList<Double> yListFhklError1 = new ArrayList<Double>();
//		yListFhklError1 = (ArrayList<Double>) yListFhklError.clone();
//		
//		if(y>=0){
//			yListFhklError1.add(Math.sqrt(y));
//		}
//		else{
//			yListFhklError1.add(0.00001);
//		}
//		
//		yListFhklError = yListFhklError1;
//		
//		ArrayList<Double> yList1 = new ArrayList<Double>();
//		yList1 = (ArrayList<Double>) yListFhkl.clone();
//		yList1.add(y);
//		
//		firePropertyChange("yListFhkl", this.yListFhkl,
//				this.yListFhkl= yList1);
//	}
//	
//	public void addyListFhkl(int l, int k, double y){
//		
//		if (yListFhkl==null){
//			yListFhkl = new ArrayList<Double>();
//			for (int i = 0; i < l; i++) {
//				  yListFhkl.add((double) 1000000000);
//				}
//		}
//		
//		if (yListFhkl.isEmpty()){
//			yListFhkl = new ArrayList<Double>();
//			for (int i = 0; i < l; i++) {
//				  yListFhkl.add((double) 1000000000);
//				}
//		}
//		
//		
//		if (yListFhklError==null){
//			yListFhklError = new ArrayList<Double>();
//			for (int i = 0; i < l; i++) {
//				  yListFhklError.add((double) 1000000000);
//				}
//		}
//		
//		if (yListFhklError.isEmpty()){
//			yListFhklError = new ArrayList<Double>();
//			for (int i = 0; i < l; i++) {
//				  yListFhklError.add((double) 1000000000);
//				}
//		}
//		
//		ArrayList<Double> yList1 = new ArrayList<Double>();
//		
//		yList1 = (ArrayList<Double>) yListFhkl.clone();
//		yList1.set(k,y);
//		
//		ArrayList<Double> yList2 = new ArrayList<Double>();
//		
//		yList2 = (ArrayList<Double>) yListFhklError.clone();
//		
//		if(y>=0){
//			yList2.set(k, Math.sqrt(y));
//		}
//		else{
//			yList2.set(k,0.00001);
//		}
//		
//		
//		yListFhklError = yList2;
//		
//		firePropertyChange("yListFhkl", this.yListFhkl,
//				this.yListFhkl= yList1);
//	}
//
//	public void addxList(double x){
//		if (xList==null){
//			xList = new ArrayList<Double>();
//		}
//		ArrayList<Double> xList1 = new ArrayList<Double>();
//		xList1 = (ArrayList<Double>) xList.clone();
//		xList1.add(x);
//		firePropertyChange("xList", this.xList,
//				this.xList= xList1);
//	}
//	
//	
//	public void addxList(int l, int k, double x){
//		if (xList==null){
//			xList = new ArrayList<Double>();
//			for (int i = 0; i < l; i++) {
//				  xList.add((double) 1000000000);
//				}
//		}
//		
//		
//		if (xList.isEmpty()){
//			xList = new ArrayList<Double>();
//			for (int i = 0; i < l; i++) {
//				  xList.add((double) 1000000000);
//				}
//		}
//		
//		ArrayList<Double> xList1 = new ArrayList<Double>();
//		xList1 = (ArrayList<Double>) xList.clone();
//		xList1.set(k, x);
//		firePropertyChange("xList", this.xList,
//				this.xList= xList1);
//	}
//	
//	
//
//	public void addzList(double z){
//		if (zList==null){
//			zList = new ArrayList<Double>();
//		}
//		ArrayList<Double> zList1 = new ArrayList<Double>();
//		zList1 = (ArrayList<Double>) zList.clone();
//		zList1.add(z);
//		firePropertyChange("zList", this.zList,
//				this.zList= zList1);
//	}
//	
//	public void resetX(){
//		xList =null;
//	}
//	public void resetY(){
//		yList =null;
//	}
//	
//	public void resetYFhkl(){
//		yListFhkl =null;
//	}
//	
//	public void resetZ(){
//		zList =null;
//	}
//	
//	public void resetAll(){
//		zList =null;
//		xList =null;
//		yList =null;
//		yListFhkl =null;
//		yListFhklError = null;
//		yListError = null;
//		yListRaw =null;
//		yListRawError = null;
//		outputDatArray =null;
//		backgroundDatArray = null;
//		initialDataset = null;
//		initialLenPt = null;
//		locationList = null;
//		
//		
//	}
//
//	public IDataset yIDataset(){
//		if (yList==null){
//			yList = new ArrayList<Double>();
//		}
//
//		ArrayList<Double> yListc = (ArrayList<Double>) yList.clone();
//		
//		ArrayList<Double> zero = new ArrayList<Double>();
//		
//		zero.add((double)1000000000);
//		
//		yListc.removeAll(zero);
//		
//		IDataset yOut = DatasetFactory.ones(new int[] {1});
//		try{
//			yOut = DatasetFactory.createFromList(yListc);
//		}
//		catch(Exception n){
////			IDataset yOut = DatasetFactory.ones(new int[] {1});
//		}
//		
//		Dataset errors = DatasetFactory.zeros(DoubleDataset.class, yOut.getShape());
//		
//		for(int i =0; i< yOut.getSize(); i++){
//			double y = yOut.getDouble(i);
//			if(y >= 0){
//				errors.set(Math.sqrt(y), i);
//			}
//			else{
//				errors.set(0.001, i);
//			}
//		}
//		yOut.setErrors(errors);
//			
//		return yOut;
//	}
//	
//	public IDataset yRawIDataset(){
//		if (yListRaw==null){
//			yListRaw = new ArrayList<Double>();
//		}
//
//		ArrayList<Double> yListRawc = (ArrayList<Double>) yListRaw.clone();
//		
//		ArrayList<Double> zero = new ArrayList<Double>();
//		
//		zero.add((double)1000000000);
//		
//		yListRawc.removeAll(zero);
//		
//		IDataset yOut = DatasetFactory.ones(new int[] {1});
//		try{
//			yOut = DatasetFactory.createFromList(yListRawc);
//		}
//		catch(Exception n){
////			IDataset yOut = DatasetFactory.ones(new int[] {1});
//		}
//		
//		for(int i =0; i< yOut.getSize(); i++){
//			
//			if(yOut.getDouble(i) >= 0){
//				yOut.setErrors(Maths.sqrt(yOut));
//			}
//			else{
//				yOut.setErrors(0.001);
//			}
//		}
//			
//		return yOut;
//	}
//	
//	public IDataset yIDatasetError(){
//		if (yListError==null){
//			yListError = new ArrayList<Double>();
//		}
//
//		ArrayList<Double> yListc = (ArrayList<Double>) yListError.clone();
//		
//		ArrayList<Double> zero = new ArrayList<Double>();
//		
//		zero.add((double) 1000000000);
//		
//		yListc.removeAll(zero);
//		
//		IDataset yOut = DatasetFactory.ones(new int[] {1});
//		
//		try{
//			yOut = DatasetFactory.createFromList(yListc);
//		}
//		catch(Exception x){
//			
//		}
//		
//		return yOut;
//	}
//	
//	public IDataset yRawIDatasetError(){
//		if (yListRawError==null){
//			yListRawError = new ArrayList<Double>();
//		}
//
//		ArrayList<Double> yListc = (ArrayList<Double>) yListRawError.clone();
//		
//		ArrayList<Double> zero = new ArrayList<Double>();
//		
//		zero.add((double) 1000000000);
//		
//		yListc.removeAll(zero);
//		
//		IDataset yOut = DatasetFactory.ones(new int[] {1});
//		
//		try{
//			yOut = DatasetFactory.createFromList(yListc);
//		}
//		catch(Exception x){
//			
//		}
//		
//		return yOut;
//	}
//	
//	public IDataset yIDatasetFhkl(){
//		if (yListFhkl==null){
//			yListFhkl = new ArrayList<Double>();
//		}
//
//		ArrayList<Double> yListFhklc =  (ArrayList<Double>) yListFhkl.clone();
//		
//		ArrayList<Double> zero = new ArrayList<Double>();
//		
//		zero.add((double) 1000000000);
//		
//		yListFhklc.removeAll(zero);
//		IDataset yOut = DatasetFactory.ones(new int[] {1});
//		try{
//			yOut = DatasetFactory.createFromList(yListFhklc);
//		}
//		catch(Exception x){
//			
//		}
//
//		IDataset yOutError = yOut.clone();
//		
//		for(int i =0; i< yOut.getSize(); i++){
//			if(yOutError.getDouble(i) <= 0){
//				yOutError.set((double) 0.001, i);
//			}
//		}
//			
//			yOut.setErrors(Maths.sqrt(yOutError));
//		return yOut;
//	}
//	
//
//	public IDataset yIDatasetFhklError(){
//		if (yListFhklError==null){
//			yListFhklError = new ArrayList<Double>();
//		}
//
//		ArrayList<Double> yListFhklc =  (ArrayList<Double>) yListFhklError.clone();
//		
//		ArrayList<Double> zero = new ArrayList<Double>();
//		
//		zero.add((double) 1000000000);
//		
//		yListFhklc.removeAll(zero);
//		
//		IDataset yOut = DatasetFactory.ones(new int[] {1});
//		try{
//			yOut = DatasetFactory.createFromList(yListFhklc);
//		}
//		catch(Exception n){
//
//		}
//		
//		return yOut;
//	}
//
//	
//	public IDataset xIDataset(){
//		if (xList==null){
//			xList = new ArrayList<Double>();
//		}
//		
//		ArrayList<Double> xListc = (ArrayList<Double>) xList.clone();
//		
//		ArrayList<Double> zero = new ArrayList<Double>();
//		
//		zero.add((double) 1000000000);
//		
//		xListc.removeAll(zero);
//		
//		IDataset xOut = DatasetFactory.ones(new int[] {1});
//		try{
//			xOut = DatasetFactory.createFromList(xListc);
//		}
//		catch(Exception n){
//
//		}
//		
//		return xOut;
//	}
//	
//	public IDataset backupDataset(){
//		
//		IDataset backup = DatasetFactory.createRange(0, 200, 1, Dataset.FLOAT64);
//		return backup;
//	}
//	
//	public ArrayList<Double> backupList(){
//		
//		IDataset backup1 = DatasetFactory.createRange(0, 200, 1, Dataset.FLOAT64);
//		ArrayList<Double> backup = new ArrayList<>();
//		
//		for(int i=0; i<200;i++){
//			backup.add((double) backup1.getInt(i));
//		}
//
//		return backup;
//	}
//	
//	
//	public void addPropertyChangeListener(PropertyChangeListener listener) {
//		propertyChangeSupport.addPropertyChangeListener(listener);
//	}
//
//	public void removePropertyChangeListener(String propertyName,
//			PropertyChangeListener listener) {
//		propertyChangeSupport.removePropertyChangeListener(propertyName,
//				listener);
//	}
//
//	protected void firePropertyChange(String propertyName, Object oldValue,
//			Object newValue) {
//		propertyChangeSupport.firePropertyChange(propertyName, oldValue,
//				newValue);
//	}
//	public IDataset getSlicerBackground() {
//		return slicerBackground;
//	}
//	public void setSlicerBackground(IDataset slicerBackground) {
//		this.slicerBackground = slicerBackground;
//	}
//
//	public int[][] getBackgroundLenPt() {
//		return backgroundLenPt;
//	}
//
//	public void setBackgroundLenPt(int[][] backgroundLenPt) {
//		this.backgroundLenPt = backgroundLenPt;
//	}
//
//	public RectangularROI getBackgroundBox() {
//		return backgroundBox;
//	}
//
//	public void setBackgroundBox(RectangularROI backgroundBox) {
//		this.backgroundBox = backgroundBox;
//	}
//
//	public ArrayList<Double> getyListFhkl() {
//		return yListFhkl;
//	}
//
//	public void setyListFhkl(ArrayList<Double> yListFhkl) {
//		this.yListFhkl = yListFhkl;
//		yListFhklError = new ArrayList<Double>();
//		ArrayList<Double> yListFhklClone  = new ArrayList<Double>();
//
//		yListFhklClone = (ArrayList<Double>) yListFhkl.clone();
//		
//		for(int i =0; i< yListFhklClone.size(); i++){
//			if(yListFhklClone.get(i) <= 0){
//				yListFhklClone.set(i, (double) 0.001);
//			}
//		}
//			
//		for(int i = 0; i <yListFhkl.size(); i++){
//			yListFhklError.add(Math.sqrt(yListFhklClone.get(i)));
//		}
//	}
//
//	public String getName() {
//		return name;
//	}
//
//	public void setName(String name) {
//		this.name = name;
//	}
//
//	public int[][] getInitialLenPt() {
//		return initialLenPt;
//	}
//
//	public void setInitialLenPt(int[][] initialLenPt) {
//		this.initialLenPt = initialLenPt;
//		
//		
//	}
//
//	public IDataset getInitialDataset() {
//		return initialDataset;
//	}
//
//	public void setInitialDataset(IDataset initialDataset) {
//		this.initialDataset = initialDataset;
//	}
//
//	public ArrayList<Double> getyListError() {
//		return yListError;
//	}
//
//	public void setyListError(ArrayList<Double> yListError) {
//		this.yListError = yListError;
//	}
//
//	public ArrayList<Double> getyListFhklError() {
//		return yListFhklError;
//	}
//
//	public void setyListFhklError(ArrayList<Double> yListFhklError) {
//		this.yListFhklError = yListFhklError;
//	}
//	
//	public IDataset getYIDatasetMax(){
//		yIDatasetMax = Maths.add(yIDataset(), yIDatasetError());
//		return yIDatasetMax;
//	}
//	
//	public IDataset getYIDatasetFhklMax(){
//		yIDatasetFhklMax = Maths.add(yIDatasetFhkl(), yIDatasetFhklError());
//		return yIDatasetFhklMax;
//	}
//	
//	public IDataset getYIDatasetMin(){
//		yIDatasetMin = Maths.subtract(yIDataset(), yIDatasetError());
//		return yIDatasetMin;
//	}
//	
//	public IDataset getYIDatasetFhklMin(){
//		yIDatasetFhklMin = Maths.subtract(yIDatasetFhkl(), yIDatasetFhklError());
//		return yIDatasetFhklMin;
//	}
//
//	public ArrayList<double[]> getLocationList() {
//		return locationList;
//	}
//
//	public void setLocationList(ArrayList<double[]> locationList) {
//		this.locationList = locationList;
//	}
//	
//	public void addLocationList(int l , int k, int[] in){
//		double[] inDouble = new double[in.length]; 
//		for (int i = 0; i<in.length; i++){
//			inDouble[i] = (double)in[i];
//		}
//		addLocationList(l, k, inDouble);
//		
//	}
//
//	public void addLocationList(int l, int k, double[] location){
//		
//		if (locationList==null || locationList.isEmpty()){
//			locationList = new ArrayList<double[]>();
//			for (int i = 0; i < l; i++) {
//				locationList.add(new double[]{0,0,0,0,0,0,0,0});
//				}
//		}
//		
//		ArrayList<double[]> locationList1 = new ArrayList<double[]>();
//		locationList1 = (ArrayList<double[]>) locationList.clone();
//		locationList1.set(k, location);
//		firePropertyChange("locationList", this.locationList,
//				this.locationList= locationList1);
//	}
//
//	public double[] getSeedLocation() {
//		return seedLocation;
//	}
//
//	public void setSeedLocation(double[] seedLocation) {
//		this.seedLocation = seedLocation;
//	}
//	
//	
//	public void addYListRaw(int l,
//							int k,
//							double y){
////		
////		dataArrayListManager(yListRaw, 
////							 yListRawError, 
////							 l,
////							 k,
////							 y);
//		
//		firePropertyChange("yListRaw", this.yListRaw,
//				this.yListRaw= yListRaw);
//		
//		
//		if (yListRaw==null){
//			yListRaw = new ArrayList<Double>();
//			for (int i = 0; i < l; i++) {
//				yListRaw.add((double)1000000000);
//			}
//		}
//		
//		if (yListRaw.size() == 0){
//			yListRaw = new ArrayList<Double>();
//			for (int i = 0; i < l; i++) {
//				yListRaw.add((double)1000000000);
//			}
//		}
//	
//	
//		if (yListRawError==null){
//			yListRawError = new ArrayList<Double>();
//			for (int i = 0; i < l; i++) {
//				yListRawError.add((double)1000000000);
//			}
//		}
//	
//		if (yListRawError.size() == 0){
//			yListRawError = new ArrayList<Double>();
//			for (int i = 0; i < l; i++) {
//				yListRawError.add((double)1000000000);
//			}
//		}
//	
//		ArrayList<Double> yList1 = new ArrayList<Double>();
//		
//		yList1 = (ArrayList<Double>) yListRaw.clone();
//		yList1.set(k,y);
//		yListRaw = yList1;
//		
//		ArrayList<Double> yList2 = new ArrayList<Double>();
//		
//		yList2 = (ArrayList<Double>) yListRawError.clone();
//		yList2.set(k,Math.sqrt(y));
//		
//		yListRawError = yList2;
//		
//		firePropertyChange("yListRaw", this.yListRaw,
//				this.yListRaw= yListRaw);
//		
//
//	}	
//		
//	
//	
//	public void addYListRaw(double y){
//		addToDataArray(yListRaw, yListRawError, y);
//		
//		firePropertyChange("yListRaw", this.yListRaw,
//				this.yListRaw= yListRaw);
//		
//	}
//	
//	public void addToDataArray(ArrayList<Double> dataArray,
//							   ArrayList<Double> dataArrayError,
//							   double y){
//
//		if (dataArray==null){
//			dataArray = new ArrayList<Double>();
//		}
//	
//		if (dataArrayError==null){
//			dataArrayError = new ArrayList<Double>();
//		}
//	
//		ArrayList<Double> dataArrayError1 = new ArrayList<Double>();
//		dataArrayError1 = (ArrayList<Double>) dataArrayError.clone();
//		dataArrayError1.add(Math.sqrt(y));
//	
//		dataArrayError = dataArrayError1;
//	
//		ArrayList<Double> yList1 = new ArrayList<Double>();
//		yList1 = (ArrayList<Double>) dataArray.clone();
//		yList1.add(y);
//		
//		dataArray= yList1;
//	}
//	
//	public void dataArrayListManager(ArrayList<Double> dataArray,
//											ArrayList<Double> dataArrayError,
//											int l, 
//											int k, 
//											double y){
//				
//		if (dataArray==null){
//			dataArray = new ArrayList<Double>();
//			for (int i = 0; i < l; i++) {
//				dataArray.add((double)1000000000);
//			}
//		}
//		
//		if (dataArray.size() == 0){
//		dataArray = new ArrayList<Double>();
//			for (int i = 0; i < l; i++) {
//				dataArray.add((double)1000000000);
//			}
//		}
//	
//	
//		if (dataArrayError==null){
//			dataArrayError = new ArrayList<Double>();
//			for (int i = 0; i < l; i++) {
//				dataArrayError.add((double)1000000000);
//			}
//		}
//	
//		if (dataArrayError.size() == 0){
//			dataArrayError = new ArrayList<Double>();
//			for (int i = 0; i < l; i++) {
//				dataArrayError.add((double)1000000000);
//			}
//		}
//	
//		ArrayList<Double> yList1 = new ArrayList<Double>();
//		
//		yList1 = (ArrayList<Double>) dataArray.clone();
//		yList1.set(k,y);
//		dataArray = yList1;
//		
//		ArrayList<Double> yList2 = new ArrayList<Double>();
//		
//		yList2 = (ArrayList<Double>) dataArrayError.clone();
//		yList2.set(k,Math.sqrt(y));
//		
//		dataArrayError = yList2;
//
//	}	
//}
//
