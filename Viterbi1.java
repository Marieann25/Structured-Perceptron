import java.util.*;

public class Viterbi1 {

	private final Index labelIndex;
	private final Index featureIndex;
	private final double[] weight;

	public Viterbi1(Index labelIndex, Index featureIndex, double[] weight) {
		this.labelIndex = labelIndex;
		this.featureIndex = featureIndex;
		this.weight = weight;
	}

	public void decode(List<Datum> data, List<Datum> dataWithMultiplePrevLabels) {
		// load words from the data
		List<String> words = new ArrayList<String>();
		for (Datum datum : data) {
			words.add(datum.word);
		}

		int[][] backpointers = new int[data.size()][numLabels()];
		double[][] scores = new double[data.size()][numLabels()];

		int prevLabel = labelIndex.indexOf(data.get(0).previousLabel);
		double[] localScores = computeScores(data.get(0).features, prevLabel);

		int position = 0;
		for (int currLabel = 0; currLabel < localScores.length; currLabel++) {
			backpointers[position][currLabel] = prevLabel;
			scores[position][currLabel] = localScores[currLabel];
		}
		// for each position in data
		for (position = 1; position< data.size(); position++) {
			// equivalent position in dataWithMultiplePrevLabels
			int i = position * numLabels() - 1; 
			
			// for each previous label 
			for (int j = 0; j < numLabels(); j++) {
				Datum datum = dataWithMultiplePrevLabels.get(i + j);
				String previousLabel = datum.previousLabel;
				prevLabel = labelIndex.indexOf(previousLabel);

				localScores = computeScores(datum.features, prevLabel);
				for (int currLabel = 0; currLabel < localScores.length; currLabel++) {
					double score = localScores[currLabel] + scores[position - 1][prevLabel];
					if (prevLabel == 0 || score > scores[position][currLabel]) {
						backpointers[position][currLabel] = prevLabel;
						scores[position][currLabel] = score;
					}
				}
			}
		}

		int bestLabel = 0;
		double bestScore = scores[data.size() - 1][0];

		for (int label = 1; label < scores[data.size() - 1].length; label++) {
			if (scores[data.size() - 1][label] > bestScore) {
				bestLabel = label;
				bestScore = scores[data.size() - 1][label];
			}
		}

		for (position = data.size() - 1; position >= 0; position--) {
			Datum datum = data.get(position);
			datum.guessLabel = (String) labelIndex.get(bestLabel);
			bestLabel = backpointers[position][bestLabel];
		}

	}

	private double[] computeScores(List<String> features, int prevLabel1) {

		String prevLabel = "";
		if (prevLabel1 == 0) {
			prevLabel = "O";
		} else {
			prevLabel = "PERSON";
		}

		double[] scores = new double[numLabels()];
		for (Object feature : features) {
			int f1 = featureIndex.indexOf("O" + " "  + feature);
			if (f1 >= 0) {
				scores[0] += weight[f1];
			}
			int f2 = featureIndex.indexOf("PERSON" + " " + feature);
                        if (f2 >= 0) {
                        	scores[1] += weight[f2];
			}
		}
	 	int j1 = featureIndex.indexOf(prevLabel + " " + "O");
                if (j1 >= 0) {
                       scores[0] += weight[j1];
                }
		int j2 = featureIndex.indexOf(prevLabel + " " + "PERSON");
                if (j2 >=0 ) {
                       scores[1] += weight[j2];
                }

		return scores;
	}

	private int numLabels() {
		return labelIndex.size();
	}
}
