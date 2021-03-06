package edu.stanford.cs276;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.stanford.cs276.util.Pair;

/**
 * A skeleton for implementing the Smallest Window scorer in Task 3.
 * Note: The class provided in the skeleton code extends BM25Scorer in Task 2. However, you don't necessarily
 * have to use Task 2. (You could also use Task 1, in which case, you'd probably like to extend CosineSimilarityScorer instead.)
 * Also, feel free to modify or add helpers inside this class.
 */
public class SmallestWindowScorer extends CosineSimilarityScorer{

	private double B = 1.2;
	
	public SmallestWindowScorer(Map<String, Double> idfs, Map<Query,Map<String, Document>> queryDict) {
		super(idfs);
//		super(idfs, queryDict);
	}

	/**
	 * get smallest window of one document and query pair.
	 * @param d: document
	 * @param q: query
	 */  
	private int getWindow(Document d, Query q) {
		/*
		 * @//TODO : Your code here
		 */
		Set<String> queryWords = new HashSet<String>(q.processedQueryWords());

		// Smallest URL window
		String decodedUrl = this.decodedUrl(d.url);
		String[] tokenizedUrl = decodedUrl.split("[^A-Za-z0-9\\s ]");
		int smallestUrlWindow = containsAllQueryWords(tokenizedUrl, queryWords) ? smallestWindow(sentenceToTermPositions(tokenizedUrl, queryWords)) : Integer.MAX_VALUE;

		// Smallest title window
		int smallestTitleWindow = Integer.MAX_VALUE;
		if (d.title != null) {
			String[] tokenizedTitle = d.title.split("\\s+");
			if (containsAllQueryWords(tokenizedTitle, queryWords)) {
				smallestTitleWindow = smallestWindow(sentenceToTermPositions(tokenizedTitle, queryWords));
			}
		}
		
		// Smallest header window
		int smallestHeaderWindow = Integer.MAX_VALUE;
		if (d.headers != null){ 
			for (String header : d.headers) {
				String[] tokenizedHeader = header.split("\\s+");
				if (containsAllQueryWords(tokenizedHeader, queryWords)) {
					int headerWindow = smallestWindow(sentenceToTermPositions(tokenizedHeader, queryWords));
					if (headerWindow < smallestHeaderWindow) {
						smallestHeaderWindow = headerWindow;
					}
				}
			}
		}
		
		// Smallest body window
		int smallestBodyWindow = Integer.MAX_VALUE;
		if (d.body_hits != null) {
			if (d.body_hits.keySet().containsAll(queryWords)) {
				Map<String, List<Integer>> body_hits = new HashMap<String, List<Integer>>(d.body_hits);
				body_hits.keySet().retainAll(queryWords); // ehh see if this works
				smallestBodyWindow = smallestWindow(body_hits);
			}
		}
		
		// Smallest anchor window
		int smallestAnchorWindow = Integer.MAX_VALUE;
		if (d.anchors != null) {
			for (String anchor : d.anchors.keySet()) {
				String[] tokenizedAnchor = anchor.split("\\s+");
				if (containsAllQueryWords(tokenizedAnchor, queryWords)) {
					int anchorWindow = smallestWindow(sentenceToTermPositions(tokenizedAnchor, queryWords));
					if (anchorWindow < smallestAnchorWindow) {
						smallestAnchorWindow = anchorWindow;
					}
				}
				
			}
		}

		List<Integer> smallestWindows = new ArrayList<Integer>(Arrays.asList(smallestUrlWindow, smallestTitleWindow, smallestHeaderWindow, smallestBodyWindow, smallestAnchorWindow));
		return Collections.min(smallestWindows);
	}
	
	private boolean containsAllQueryWords(String[] tokenizedSentence, Set<String> queryWords) {
		Set<String> sentenceSet = new HashSet<String>(Arrays.asList(tokenizedSentence));
		return sentenceSet.containsAll(queryWords);
		
	}

	private Map<String, List<Integer>> sentenceToTermPositions(String[] tokenizedSentence, Set<String> queryWords) {
		Map<String, List<Integer>> termPositions = new HashMap<String,List<Integer>>();

		for (int i = 0; i < tokenizedSentence.length; i++) {
			String term = tokenizedSentence[i];
			if (!queryWords.contains(term)) continue;
			List<Integer> positions = termPositions.get(term);
			if (positions != null) {
				positions.add(i);
			} else {
				positions = new ArrayList<Integer>();
				positions.add(i);
			}
			termPositions.put(term, positions);
		}
		return termPositions;
	}

	private int smallestWindow(Map<String, List<Integer>> sentenceTermPositions) {
		int smallestWindow = Integer.MAX_VALUE;
		if (sentenceTermPositions.size() == 0) return Integer.MAX_VALUE;
		Map<String, Integer> termIndexes = new HashMap<String,Integer>();
		String firstTerm = (String) sentenceTermPositions.keySet().toArray()[0];
		Pair<String,Integer> minTermAndPos = new Pair<String,Integer>(firstTerm,Integer.MAX_VALUE);
		Pair<String,Integer> maxTermAndPos = new Pair<String,Integer>(firstTerm,0);

		// initialize values: indexes to 0, min amd max pos
		for (String term : sentenceTermPositions.keySet()) {
			termIndexes.put(term, 0);
		}
		while (true) {
//			Pair<Pair<String,Integer>,Pair<String,Integer>> p = updateMinAndMax(sentenceTermPositions, termIndexes, minTermAndPos, maxTermAndPos);
//			minTermAndPos = p.getFirst();
//			maxTermAndPos = p.getSecond();
			updateMinAndMax(sentenceTermPositions, termIndexes, minTermAndPos, maxTermAndPos);
			int minTermIndex = termIndexes.get(minTermAndPos.getFirst());
			// if the min is already at the end of its array, then we break since window cannot be any smaller
			if (minTermIndex + 1 == sentenceTermPositions.get(minTermAndPos.getFirst()).size()) {
				break;
			}
			// increment the min index
			termIndexes.put(minTermAndPos.getFirst(), minTermIndex + 1);
		}
		smallestWindow = maxTermAndPos.getSecond() - minTermAndPos.getSecond();
//		System.out.println("-------------");
//		System.out.println(sentenceTermPositions);
//		System.out.println(smallestWindow + " " + termIndexes);

		return smallestWindow;
	}
	
	// find the curr min and max
//	private Pair<Pair<String,Integer>,Pair<String,Integer>> updateMinAndMax(Map<String, List<Integer>> sentenceTermPositions,Map<String,Integer> termIndexes, 
	private void updateMinAndMax(Map<String, List<Integer>> sentenceTermPositions,Map<String,Integer> termIndexes, 
			Pair<String, Integer> minTermAndPos, Pair<String,Integer> maxTermAndPos) {
		minTermAndPos.setSecond(Integer.MAX_VALUE);
		maxTermAndPos.setSecond(0);
		for (String term : sentenceTermPositions.keySet()) {

			int termIndex = termIndexes.get(term);
			int termPos = sentenceTermPositions.get(term).get(termIndex);

			// find minimum position here
			if (termPos < minTermAndPos.getSecond()) {
				minTermAndPos.setFirst(term); 
				minTermAndPos.setSecond(termPos);
			}

			// find maximum position here
			if (termPos > maxTermAndPos.getSecond()) {
				maxTermAndPos.setFirst(term);
				maxTermAndPos.setSecond(termPos);
			}
		}
//		return new Pair<Pair<String,Integer>,Pair<String,Integer>>(minTermAndPos, maxTermAndPos);
	}



	/**
	 * get boost score of one document and query pair.
	 * @param d: document
	 * @param q: query
	 */  
	private double getBoostScore (Document d, Query q) {
		int smallestWindow = getWindow(d, q);
//		double boostScore = 1;
		/*
		 * @//TODO : Your code here, calculate the boost score.
		 *
		 */
		double x = smallestWindow - q.processedQueryWords().size();
		if (x == 0) {
			return B;
		} else {
			return 1 + B * (1 / Math.exp(x));
//			return 1 + B * (1 / x);
		}
	}

	@Override
	public double getSimScore(Document d, Query q) {
		Map<String,Map<String, Double>> tfs = this.getDocTermFreqs(d,q);
		this.normalizeTFs(tfs, d, q);
		Map<String,Double> tfQuery = getQueryFreqs(q);
		double boost = getBoostScore(d, q);
		double rawScore = this.getNetScore(tfs, q, tfQuery, d);
		return boost * rawScore;
	}

}
