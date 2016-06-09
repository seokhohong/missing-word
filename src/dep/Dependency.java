package dep;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;
import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;


public class Dependency implements Serializable
{
	private String relation;				public String getRelation() { return relation; }
	private String head;					public String getHeadToken() { return head; }
	private String headTag;					public String getHeadTag() { return headTag; }
	private String dep;						public String getDepToken() { return dep; }
	private String depTag;					public String getDepTag() { return depTag; }
	
	//private static TMap<String, Set<Dependency>> pool = new THashMap<>();
	
	public static List<Dependency> from(GrammaticalStructure gs)
	{
		List<Dependency> dependencies = new ArrayList<>();
		for(TypedDependency td : gs.typedDependencies())
		{
			dependencies.add(getInstance(td));
		}
		return dependencies;
	}
	public static Dependency parse(String parse)
	{
		String relation = parse.substring(0, parse.indexOf("("));
		String rest = parse.substring(parse.indexOf("(") + 1, parse.length() - 1);
		String[] split = rest.split(", ");
		String headChunk = split[0];
		String depChunk = split[1];
		
		String head = headChunk.substring(0, headChunk.lastIndexOf("/"));
		
		String headTag = headChunk.substring(headChunk.lastIndexOf("/") + 1);
		if(headTag.equals("null")) headTag = null;
		
		String dep = depChunk.substring(0, depChunk.lastIndexOf("/"));
		
		String depTag = depChunk.substring(depChunk.lastIndexOf("/") + 1);
		if(depTag.equals("null")) depTag = null;
		
		return getInstance(relation, head, headTag, dep, depTag);
	}
	public synchronized static Dependency getInstance(String relation, String headWord, String headTag, String depWord, String depTag)
	{
		/*
		String keyWords = headWord+"_"+depWord;
		if(pool.containsKey(keyWords))
		{
			for(Dependency pooled : pool.get(keyWords))
			{
				if(pooled.getRelation().equals(relation) && pooled.getHeadTag().equals(headTag) && pooled.getDepTag().equals(depTag))
				{
					return pooled;
				}
			}
		}
		*/
		return new Dependency(relation, headWord, headTag, depWord, depTag);
	}
	public static Dependency getInstance(TypedDependency td)
	{
		return getInstance(td.reln().toString(), td.gov().word(), td.gov().tag(), td.dep().word(), td.dep().tag());
	}
	/*
	private void addToPool()
	{
		String keyWords = head+"_"+dep;
		if(!pool.containsKey(keyWords))
		{
			pool.put(keyWords, new HashSet<>());
		}
		pool.get(keyWords).add(this);
	}
	*/
	private Dependency(String relation, String headWord, String headTag, String depWord, String depTag)
	{
		this.relation = relation;
		this.head = headWord;
		this.headTag = headTag;
		this.dep = depWord;
		this.depTag = depTag;
	}
	private Dependency(TypedDependency dependency)
	{
		relation = dependency.reln().toString();
		head = dependency.gov().word();
		headTag = dependency.gov().tag();
		dep = dependency.dep().word();
		depTag = dependency.dep().tag();
		//addToPool();
	}
	public String getHeadChunk()
	{
		return preprocess(head+"_"+headTag);
	}
	public String getDepChunk()
	{
		return preprocess(dep+"_"+depTag);
	}
	private static String preprocess(String combined)
	{
		if(combined.endsWith("_CD"))
		{
			return "#_CD";
		}
		return combined;
	}
	@Override
	public String toString()
	{
		return relation+"("+head+"/"+headTag+", "+dep+"/"+depTag+")";
	}
	@Override
	public int hashCode()
	{
		return relation.hashCode() * head.hashCode() * dep.hashCode();
	}
	@Override
	public boolean equals(Object o)
	{
		if(o instanceof Dependency)
		{
			Dependency other = (Dependency) o;
			if(other.headTag == null && this.headTag != null) return false;
			if(this.headTag == null && other.headTag != null) return false;
			if(other.depTag == null && this.depTag != null) return false;
			if(this.depTag == null && other.depTag != null) return false;
			boolean headEquality = (other.headTag == null && this.headTag == null) || other.headTag.equals(this.headTag);
			boolean depEquality = (other.depTag == null && this.depTag == null) || other.depTag.equals(this.depTag);
			return other.relation.equals(this.relation) && other.head.equals(this.head) && headEquality && other.dep.equals(this.dep) && depEquality;
		}
		return false;
	}
}
