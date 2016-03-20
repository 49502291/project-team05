package edu.cmu.lti.f14.team05;

public class EvaluationResult {
	private double precision;
	  private double recall;
	  private double fMeasure;
	  private double avgPrecison;
	  private boolean isCorrect;
	  
	  public EvaluationResult(double p, double r, double fm, double avg) {
	    precision = p;
	    recall = r;
	    fMeasure = fm;
	    avgPrecison = avg;
	  }
	  
	  public EvaluationResult(boolean isCorrect) {
	    this.isCorrect = isCorrect;
	  }

	  public double getPrecision() {
	    return precision;
	  }

	  public void setPrecision(double precision) {
	    this.precision = precision;
	  }

	  public double getRecall() {
	    return recall;
	  }

	  public void setRecall(double recall) {
	    this.recall = recall;
	  }

	  public double getfMeasure() {
	    return fMeasure;
	  }

	  public void setfMeasure(double fMeasure) {
	    this.fMeasure = fMeasure;
	  }

	  public double getAvgPrecison() {
	    return avgPrecison;
	  }

	  public void setAvgPrecison(double avgPrecison) {
	    this.avgPrecison = avgPrecison;
	  }

	  public boolean getIsCorrect() {
	    return isCorrect;
	  }

	  public void setIsCorrect(boolean isCorrect) {
	    this.isCorrect = isCorrect;
	  }
}
