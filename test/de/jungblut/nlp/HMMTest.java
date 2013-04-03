package de.jungblut.nlp;

import java.util.Random;

import junit.framework.TestCase;

import org.junit.Test;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;

public class HMMTest extends TestCase {

  DoubleVector[] features = new DenseDoubleVector[1000];
  DenseDoubleVector[] outcome = new DenseDoubleVector[1000];

  @Override
  protected void setUp() throws Exception {
    // we have 70% chance of observing an umbrella
    Random r = new Random(0L);
    for (int i = 0; i < features.length; i++) {
      if (r.nextDouble() > 0.7) {
        // umbrella
        features[i] = new DenseDoubleVector(new double[] { 0d, 1d });
        // in 10% our case we add noisy data
        if (r.nextDouble() > 0.9) {
          outcome[i] = new DenseDoubleVector(new double[] { 0d, 1d });
        } else {
          outcome[i] = new DenseDoubleVector(new double[] { 1d, 0d });
        }
      } else {
        // no umbrella
        features[i] = new DenseDoubleVector(new double[] { 1d, 0d });
        outcome[i] = new DenseDoubleVector(new double[] { 0d, 1d });
      }
    }

  }

  @Test
  public void testUnsupervisedUmbrellaWorld() {
    HMM hmm = new HMM(2, 2, 0L);
    hmm.trainUnsupervised(features, 0.001d, 100, false);
    assertEquals(hmm.getHiddenPriorProbability().getLength(), 2);
    assertEquals(hmm.getHiddenPriorProbability().get(0), 0.01, 0.01);
    assertEquals(hmm.getHiddenPriorProbability().get(1), 0.98, 0.01);
    assertEquals(hmm.getEmissionProbabilitiyMatrix().getRowCount(), 2);
    assertEquals(hmm.getEmissionProbabilitiyMatrix().getColumnCount(), 2);
    assertEquals(hmm.getEmissionProbabilitiyMatrix().get(0, 0), 0.76, 0.01);
    assertEquals(hmm.getEmissionProbabilitiyMatrix().get(0, 1), 0.23, 0.01);
    assertEquals(hmm.getEmissionProbabilitiyMatrix().get(1, 0), 0.45, 0.01);
    assertEquals(hmm.getEmissionProbabilitiyMatrix().get(1, 1), 0.54, 0.01);
    assertEquals(hmm.getTransitionProbabilityMatrix().getRowCount(), 2);
    assertEquals(hmm.getTransitionProbabilityMatrix().getColumnCount(), 2);
    assertEquals(hmm.getTransitionProbabilityMatrix().get(0, 0), 0.79, 0.01);
    assertEquals(hmm.getTransitionProbabilityMatrix().get(0, 1), 0.20, 0.01);
    assertEquals(hmm.getTransitionProbabilityMatrix().get(1, 0), 0.64, 0.01);
    assertEquals(hmm.getTransitionProbabilityMatrix().get(1, 1), 0.35, 0.01);
  }

  @Test
  public void testSupervisedUmbrellaWorld() {
    HMM hmm = new HMM(2, 2, 0L);
    hmm.trainSupervised(features, outcome);
    // note that we added +1 smoothing, so we don't end up exactly with 0.7
    assertEquals(hmm.getHiddenPriorProbability().getLength(), 2);
    assertEquals(hmm.getHiddenPriorProbability().get(0), 0.71, 0.01);
    assertEquals(hmm.getHiddenPriorProbability().get(1), 0.29, 0.01);

    // the transition matrix is equally working
    assertEquals(hmm.getTransitionProbabilityMatrix().getRowCount(), 2);
    assertEquals(hmm.getTransitionProbabilityMatrix().getColumnCount(), 2);
    assertEquals(hmm.getTransitionProbabilityMatrix().get(0, 0), 0.71, 0.01);
    assertEquals(hmm.getTransitionProbabilityMatrix().get(0, 1), 0.28, 0.01);
    assertEquals(hmm.getTransitionProbabilityMatrix().get(1, 0), 0.72, 0.01);
    assertEquals(hmm.getTransitionProbabilityMatrix().get(1, 1), 0.28, 0.01);

    // the emission probability must look quite extreme as we only add 10% noise
    // and otherwise observe the same like the observation
    assertEquals(hmm.getEmissionProbabilitiyMatrix().getRowCount(), 2);
    assertEquals(hmm.getEmissionProbabilitiyMatrix().getColumnCount(), 2);
    assertEquals(hmm.getEmissionProbabilitiyMatrix().get(0, 0), 0.99, 0.01);
    assertEquals(hmm.getEmissionProbabilitiyMatrix().get(0, 1), 0.01, 0.01);
    assertEquals(hmm.getEmissionProbabilitiyMatrix().get(1, 0), 0.08, 0.01);
    assertEquals(hmm.getEmissionProbabilitiyMatrix().get(1, 1), 0.91, 0.01);

    // observe an umbrella
    DoubleVector predict = hmm.predict(new DenseDoubleVector(new double[] { 0d,
        1d }));
    // so it is very likely that it rains ~80%
    assertEquals(1, predict.maxIndex());
    assertEquals(0.82, predict.get(1), 0.01);
    assertEquals(0.18, predict.get(0), 0.1);
  }

}
