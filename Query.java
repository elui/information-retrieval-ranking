package edu.stanford.cs276;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is used to store a query sequence.
 */
public class Query {
	List<String> queryWords;

	// process the query words ie. lowercase, stem, whatever
	// and return them here
	public List<String> processedQueryWords() {
		return queryWords.stream()
			.map(qw -> qw.toLowerCase())
			.collect(Collectors.toList());
	}

	/**
	 * Constructs a query.
	 * @param query the query String.
	 */
	public Query(String query) {
		queryWords = new ArrayList<String>(Arrays.asList(query.split(" ")));
	}

	/**
	 * Returns a String representation of the Query.
	 * @return the Query as a String
	 */
	public String toString() {
		String str = "";
		for (String word : queryWords) {
			str += word + " ";
		}
		return str.trim();
	}
}
