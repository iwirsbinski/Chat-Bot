
import java.util.*;
import java.io.*;

public class Chatbot{
    private static String filename = "./WARC201709_wid.txt";
    private static ArrayList<Integer> readCorpus(){
        ArrayList<Integer> corpus = new ArrayList<Integer>();
        try{
            File f = new File(filename);
            Scanner sc = new Scanner(f);
            while(sc.hasNext()){
                if(sc.hasNextInt()){
                    int i = sc.nextInt();
                    corpus.add(i);
                }
                else{
                    sc.next();
                }
            }
        }
        catch(FileNotFoundException ex){
            System.out.println("File Not Found.");
        }
        return corpus;
    }
    
    /**
     * This method returns an ArrayList of each cutoff in the unigram probability distribution between
     * 0 and 1.
     * @param corpus
     * @return
     */
    private static ArrayList<Double> getProbabilitiesUnigram(ArrayList<Integer> corpus) {
        ArrayList<Double> probabilities = new ArrayList<Double>();

//        for (int i = 1; i <= 4699; i++) {
//            int count = 0;
//            for (Integer s: corpus) {
//                if (s.equals(i)) { count++; }
//            }
//            if (i != 1) { probabilities.add(((float)count / 4700) + probabilities.get(i - 2)); }
//            else { probabilities.add(((float)count / 4700)); }
//        }
        int[] tally = new int[4700];
        //count number of occurences of each word
        for (Integer s: corpus) {
            tally[s] = tally[s] + 1;
        }
        for (int i = 0; i < tally.length; i++) {
            if (i == 0) { probabilities.add((double) tally[i] / 228548); }
            else if (i > 0) { probabilities.add(((double) tally[i] / 228548) + probabilities.get(i - 1)); }
        }
        return probabilities;
    }
    
    /**
     * This method generates word index given a number between 0 and 1 and a history word.
     * @param counts
     * @param h
     * @param p
     * @param printFlag
     * @return
     */
    private static int getProbabilitiesBigram(HashMap<Integer, HashMap<Integer, Integer>> counts
            ,int h, double p, boolean printFlag) {
        int summation = 0;
        ArrayList<Double> probabilities = new ArrayList<Double>();
        // calculate summation, total number of times "h" appears in corpus with some word after
        for (Integer s: counts.get(h).keySet()) {
            summation = summation + counts.get(h).get(s);
        }
        int i = 0;
        int wordIndex = 0;
        double prob1 = 0;
        double prob2 = 0;
        Integer[] keys = new Integer[counts.get(h).keySet().size()];
        int j = 0;
        //put key set into an array for sorting
        for (Integer s: counts.get(h).keySet()) {
            keys[j] = s;
            j++;
        }
       Arrays.sort(keys);
       
        for (Integer s: keys) {
            if (probabilities.isEmpty()) {
                probabilities.add((double) counts.get(h).get(s) / summation);
                if (p < probabilities.get(i)) {
                    wordIndex = s;
                    prob1 = 0;
                    prob2 = probabilities.get(i);
                    break;
                }
            }
            else {
                probabilities.add(((double) counts.get(h).get(s) / summation) + (probabilities.get(i-1)));
                if (p <= probabilities.get(i) && p > probabilities.get(i-1)) {
                    wordIndex = s;
                    prob1 = probabilities.get(i-1);
                    prob2 = probabilities.get(i);
                    break;
                }
            }
            i++;
        }
        if (printFlag) {
            System.out.println(wordIndex);
            System.out.println(String.format("%.7f",prob1));
            System.out.println(String.format("%.7f",prob2));
        }
        //System.out.println(p);
        return wordIndex;
    }
    
    /**
     * This method generates a word index given a number between 0 and 1, and two history words.
     * @param counts
     * @param h1
     * @param h2
     * @param p
     * @param printFlag whether or not to print probability intervals
     * @return word index
     */
    public static int getProbabilitiesTrigram(HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> counts, 
            int h1, int h2, double p, boolean printFlag) {
        int summation = 0;
        ArrayList<Double> probabilities = new ArrayList<Double>();
        if (counts.get(h1).get(h2) == null) {
            System.out.print("undefined");
            return -1;
        }
        Set<Integer> keySet = counts.get(h1).get(h2).keySet();
        // calculate summation, total number of times "h1 h2" appears with some word after
        for (Integer s: keySet) {
            summation = summation + counts.get(h1).get(h2).get(s);
        }
        int j = 0;
        Integer[] keys = new Integer[keySet.size()];
        // put key set into an array for sorting
        for (Integer s: keySet) {
            keys[j] = s;
            j++;
        }
        Arrays.sort(keys);
        int i = 0;
        int wordIndex = 0;
        double prob1 = 0;
        double prob2 = 0;
        for (Integer s: keys) {
            if (probabilities.isEmpty()) {
                probabilities.add((double) counts.get(h1).get(h2).get(s) / summation);
                if (p < probabilities.get(i)) {
                    wordIndex = s;
                    prob1 = 0;
                    prob2 = probabilities.get(i);
                    break;
                }
            }
            else {
                probabilities.add(((double) counts.get(h1).get(h2).get(s) / summation) + (probabilities.get(i-1)));
                if (p <= probabilities.get(i) && p > probabilities.get(i-1)) {
                    wordIndex = s;
                    prob1 = probabilities.get(i-1);
                    prob2 = probabilities.get(i);
                    break;
                }
            }
            i++;
        }
        if (printFlag) {
            System.out.println(wordIndex);
            System.out.println(String.format("%.7f",prob1));
            System.out.println(String.format("%.7f",prob2));
        }
        return wordIndex;
    }

    /**
     * This method returns a two dimensional HashMap containing the counts of each two-word
     * instance in the corpus.
     * @param corpus
     * @return
     */
    private static HashMap<Integer, HashMap<Integer, Integer>> getCountsBiGram(ArrayList<Integer> corpus) {
        HashMap<Integer, HashMap<Integer, Integer>> bigram = new HashMap<Integer, HashMap<Integer, Integer>>();
        for (int i = 0; i < corpus.size() - 1; i++) {
            Integer firstWord = corpus.get(i);
            Integer nextWord = corpus.get(i+1);
            //bigram.get(firstWord).put(nextWord, bigram.get(firstWord).get(nextWord) + 1);

            // if there is no instance of first word yet in the corpus
            if (bigram.get(firstWord) == null) { 
                HashMap<Integer, Integer> internal = new HashMap<Integer, Integer>();
                internal.put(nextWord, 1);
                bigram.put(firstWord, internal);
            }
            
            else {
                //if this is the first instance of "firstWord nextWord" in corpus
                if (bigram.get(firstWord).get(nextWord) != null) { 
                    bigram.get(firstWord).put(nextWord, bigram.get(firstWord).get(nextWord) + 1);
                }
                else {
                    bigram.get(firstWord).put(nextWord, 1);
                }
            }
        }
        return bigram;
    }

    /**
     * This method returns a three dimensional HashMap containing the counts of each three-word
     * instance in the corpus.
     * @param corpus
     * @return
     */
    private static HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>
    getCountsTrigram(ArrayList<Integer> corpus) {
        HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> trigram =
                new HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>();

        for (int i = 0; i < corpus.size() - 2; i++) {
            Integer firstWord = corpus.get(i);
            Integer secondWord = corpus.get(i+1);
            Integer thirdWord = corpus.get(i+2);

            // if this is the first instance of firstWord in the corpus
            if (trigram.get(firstWord) == null) {
                HashMap<Integer, HashMap<Integer, Integer>> internal = new HashMap<Integer, HashMap<Integer, Integer>>();
                HashMap<Integer, Integer> internal2 = new HashMap<Integer, Integer>();
                internal2.put(thirdWord, 1);
                internal.put(secondWord, internal2);
                trigram.put(firstWord, internal);
            }
            else {
                // if this is the first instance of "firstWord secondWord" in the corpus
                if (trigram.get(firstWord).get(secondWord) == null) {
                    HashMap<Integer, Integer> internal2 = new HashMap<Integer, Integer>();
                    internal2.put(thirdWord, 1);
                    trigram.get(firstWord).put(secondWord, internal2);
                }
                else {
                    // if this is the first instance of "firstWord secondWord thirdWord"
                    if (trigram.get(firstWord).get(secondWord).get(thirdWord) == null) {
                        trigram.get(firstWord).get(secondWord).put(thirdWord, 1);
                    }
                    else {
                        trigram.get(firstWord).get(secondWord).put(thirdWord,
                                trigram.get(firstWord).get(secondWord).get(thirdWord) + 1);
                    }
                }
            }
        }
        return trigram;
    }
    
    static public void main(String[] args){
        ArrayList<Integer> corpus = readCorpus();
        int flag = Integer.valueOf(args[0]);

        if(flag == 100){
            int w = Integer.valueOf(args[1]);
            int count = 0;
            File file = new File("WARC201709_wid.txt");
            try {
                Scanner scnr = new Scanner(file);
                while (scnr.hasNextLine()) {
                    if (scnr.nextLine().equals(Integer.toString(w))) { count++; }
                }

                System.out.println(count);
                System.out.println(String.format("%.7f",count/(double)corpus.size()));
            } catch (FileNotFoundException e) { System.out.println("File not found"); }
        }
        else if(flag == 200){
            int n1 = Integer.valueOf(args[1]);
            int n2 = Integer.valueOf(args[2]);
            float r = (float)n1 / n2;
            ArrayList<Double> prob = getProbabilitiesUnigram(corpus);
            int index = 0;
            for (int i = 0; i < prob.size(); i++) {
                if (r < prob.get(i)) {
                    index = i - 1;
                    break;
                }
            }
            System.out.println(index + 1);
            if (index < 0) { System.out.println(String.format("%.7f",0.0000000)); }
            else { System.out.println(String.format("%.7f",prob.get(index))); }
            System.out.println(String.format("%.7f",prob.get(index + 1)));
        }
        else if(flag == 300){
            int h = Integer.valueOf(args[1]);
            int w = Integer.valueOf(args[2]);
            int count = 0;
            ArrayList<Integer> words_after_h = new ArrayList<Integer>();
            //TODO
            HashMap<Integer, HashMap<Integer, Integer>> bigram = new HashMap<Integer, HashMap<Integer, Integer>>();
            bigram = getCountsBiGram(corpus);
            if (bigram.get(h).get(w) == null) { count = 0; }
            else { count = bigram.get(h).get(w); }
            int summation = 0;
            for (Integer s: bigram.get(h).keySet()) {
                summation = summation + bigram.get(h).get(s);
            }
            
            //output 
            System.out.println(count);
            System.out.println(summation);
            //System.out.println(String.format("%.7f",count/(double)words_after_h.size()));
            System.out.println(String.format("%.7f",count/(double)summation));
        }
        else if(flag == 400){
            int n1 = Integer.valueOf(args[1]);
            int n2 = Integer.valueOf(args[2]);
            int h = Integer.valueOf(args[3]);
            double r = (double)n1 / n2;
            HashMap<Integer, HashMap<Integer, Integer>> bigram = new HashMap<Integer, HashMap<Integer, Integer>>();
            bigram = getCountsBiGram(corpus);
            getProbabilitiesBigram(bigram, h, r, true);
            
        }
        else if(flag == 500){
            int h1 = Integer.valueOf(args[1]);
            int h2 = Integer.valueOf(args[2]);
            int w = Integer.valueOf(args[3]);
            int count = 0;
            ArrayList<Integer> words_after_h1h2 = new ArrayList<Integer>();
            
            HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> trigram = getCountsTrigram(corpus);

            if (trigram.get(h1).get(h2) == null || trigram.get(h1).get(h2).get(w) == null) { count = 0; }
            else { count = trigram.get(h1).get(h2).get(w); }
            int summation = 0;
            if (count != 0) {
                for (Integer s: trigram.get(h1).get(h2).keySet()) {
                    summation = summation + trigram.get(h1).get(h2).get(s);
                }
            }
            
            //output 
            System.out.println(count);
            System.out.println(summation);
            //words_after_h1h2.size()
            if(summation == 0)
                System.out.println("undefined");
            else
                System.out.println(String.format("%.7f",count/(double)summation));
        }
        else if(flag == 600){
            int n1 = Integer.valueOf(args[1]);
            int n2 = Integer.valueOf(args[2]);
            int h1 = Integer.valueOf(args[3]);
            int h2 = Integer.valueOf(args[4]);
            double r = (double) n1 / n2;
            HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> trigram = getCountsTrigram(corpus);
            getProbabilitiesTrigram(trigram, h1, h2, r, true);
            
        }
        else if(flag == 700){
            int seed = Integer.valueOf(args[1]);
            int t = Integer.valueOf(args[2]);
            int h1=0,h2=0;

            Random rng = new Random();
            if (seed != -1) rng.setSeed(seed);

            if(t == 0){
                // TODO Generate first word using r
                double r = rng.nextDouble();
                ArrayList<Double> prob = getProbabilitiesUnigram(corpus);
                int index = 0;
                for (int i = 0; i < prob.size(); i++) {
                    if (r < prob.get(i)) {
                        h1 = i;
                        break;
                    }
                }
                
                System.out.println(h1);
                if(h1 == 9 || h1 == 10 || h1 == 12){
                    return;
                }
                
                // TODO Generate second word using r
                r = rng.nextDouble();
                HashMap<Integer, HashMap<Integer, Integer>> counts = getCountsBiGram(corpus);
                h2 = getProbabilitiesBigram(counts, h1, r, false);
                System.out.println(h2);
            }
            else if(t == 1){
                h1 = Integer.valueOf(args[3]);
                // TODO Generate second word using r
                
                double r = rng.nextDouble();
                HashMap<Integer, HashMap<Integer, Integer>> counts = getCountsBiGram(corpus);
                h2 = getProbabilitiesBigram(counts, h1, r, false);
                System.out.println(h2);
            }
            else if(t == 2){
                h1 = Integer.valueOf(args[3]);
                h2 = Integer.valueOf(args[4]);
            }

            HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> counts = 
                    getCountsTrigram(corpus);
            while(h2 != 9 && h2 != 10 && h2 != 12){
                double r = rng.nextDouble();
                int w = 0;
                w = getProbabilitiesTrigram(counts, h1, h2, r, false);
                // TODO Generate new word using h1,h2
                System.out.println(w);
                h1 = h2;
                h2 = w;
            }
        }
        return;
    }
}
