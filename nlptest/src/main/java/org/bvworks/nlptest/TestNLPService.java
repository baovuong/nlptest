package org.bvworks.nlptest;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

@Path("/testnlp")
public class TestNLPService {

	@POST
	@Path("/pos")
	@Produces("application/json")
	public TaggedWord[] tagInput(@FormParam("input") String input) {
		
		// pos tags
		Map<String,String> tagMap = new HashMap<String,String>();
		tagMap.put("CC", "Coordinating conjunction");
		tagMap.put("CD", "Cardinal number");
		tagMap.put("DT", "Determiner");
		tagMap.put("EX", "Existential there");
		tagMap.put("FW", "Foreign word");
		tagMap.put("IN", "Preposition or subordinating conjunction");
		tagMap.put("JJ", "Adjective");
		tagMap.put("JJR", "Adjective, comparative");
		tagMap.put("JJS", "Adjective, superlative");
		tagMap.put("LS", "List item marker");
		tagMap.put("MD", "Modal");
		tagMap.put("NN", "Noun, singular or mass");
		tagMap.put("NNS", "Noun, plural");
		tagMap.put("NNP", "Proper noun, singular");
		tagMap.put("NNPS", "Proper noun, plural");
		tagMap.put("PDT", "Predeterminer");
		tagMap.put("POS", "Possessive pronoun");
		tagMap.put("PRP", "Personal pronoun");
		tagMap.put("PRP$", "Possessive pronoun");
		tagMap.put("RB", "Adverb");
		tagMap.put("RBR", "Adverb, comparative");
		tagMap.put("RBS", "Adverb, superlative");
		tagMap.put("RP", "Particle");
		tagMap.put("SYM", "Symbol");
		tagMap.put("TO", "to");
		tagMap.put("UH", "Interjection");
		tagMap.put("VB", "Verb, base form");
		tagMap.put("VBD", "Verb, past tense");
		tagMap.put("VBG", "Verb, gerund or present participle");
		tagMap.put("VBN", "Verb, past participle");
		tagMap.put("VBP", "Verb, non-3rd person singular present");
		tagMap.put("VBZ", "Verb, 3rd person singular present");
		tagMap.put("WDT", "Wh-determiner");
		tagMap.put("WP", "Wh-pronoun");
		tagMap.put("WP$", "Possessive wh-pronoun");
		tagMap.put("WRB", "Wh-adverb");
		tagMap.put(".", ".");
		TaggedWord[] words = tagWords(input);
		
		for (int i=0; i<words.length; i++) {
			words[i].setTag(tagMap.get(words[i].getTag()));
		}
		
		return words;

	}
	
	@GET
	@Path("/ridley")
	public Response ridleyfy(@QueryParam("input") String input) {
		Map<String,Integer> order = new HashMap<String,Integer>();
		order.put("NNP", 1);
		order.put("PRP", 2);
		order.put("NN", 3);
		TaggedWord[] words = tagWords(input);
		
		// find preferred word
		double[] weight = new double[words.length];
		for (int i=0; i<weight.length; i++) {
			int o = 100;
			
			try {
				o = order.get(words[i].getTag());
			} catch (Exception e) {
				
			}
			weight[i] = words[i].getConfidence()/(Math.pow(o, 2));
		}
		
		int best = 0;
		for (int i=0; i<weight.length; i++) {
			System.out.println(weight[i]);
			if (weight[best] < weight[i])
				best = i;
		}
		
		String bestWord = words[best].getWord();
		System.out.println(bestWord);
		String phrase = "";
		for (int i=0; i<words.length; i++) {
			if (!(words[i].getTag().equals("POS") || words[i].getTag().equals(null)))
				phrase += " ";
			if (words[i].getWord().equals(bestWord))
				phrase += "Ridley";
			else
				phrase += words[i].getWord();
			
		}
		
		return Response.status(200).entity(phrase).build();
		
	}
	
	
	private TaggedWord[] tagWords(String words) {

		
		
		InputStream posModelIn = this.getClass()
				.getResourceAsStream("/nlp/en-pos-maxent.bin");
		InputStream tokModelIn = this.getClass()
				.getResourceAsStream("/nlp/en-token.bin");
		
		// create models from files
		POSModel posModel = null;
		TokenizerModel tokModel = null;
		
		
		try {
			posModel = new POSModel(posModelIn);
			tokModel = new TokenizerModel(tokModelIn);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// create tools from models
		POSTaggerME tagger = new POSTaggerME(posModel);
		Tokenizer tokenizer = new TokenizerME(tokModel);
		
		String[] tokens = tokenizer.tokenize(words);
		String[] tags = tagger.tag(tokens);
		double[] probs = tagger.probs();
		TaggedWord[] result = new TaggedWord[tokens.length];
		
		for (int i=0; i<result.length; i++) {
			result[i] = new TaggedWord();
			result[i].setWord(tokens[i]);
			result[i].setTag(tags[i]);
			result[i].setConfidence(probs[i]);
		}
		
		
		return result;
	}
}
