The text clusterer works as follows,
1. Load the input file
2. remove the stopwords
3. stem the words
4. Calculate tf*idf value for each record in the input file.
5. Calculate similarity matrix by using the tfidf values of the records.
6. Take most similar records per each record and make them as clusters initially.
7. Use the transitive rule A,B are most similar and B,C are most similar; A and C are likely to be similar. This imply that A, B, C are in the same cluster.
8. Merge the clusters based on the above rule for all the records.
9. Write the final output i.e,; final sets of clusters to the output file.

StopWords:
Words that are insignificant to identify the clusters. This algorithm by defauly uses the list of most popular stopwords. Anyways we can define our own stopword list or even not remove any word.
Stemming:
Stemming is deriving the base word of a word. 
For eg: Identification is stemmed to Identity
We use the famous porter stemmer algorithm, which uses rules given by porter to stem the words. This implementation is taken from Brian Goetz's implementation from the internet.
tf:term frequency
Term frequency defines how frequently a term occur in a document
Idf:Inverse document frequency
Inverse document frequency is the frequency of a word in the whole set of documents.
similarity matrix:
2 dimensional matrix representation of a record's similarity with all other records.

