package net.darkjade.rna;
import java.util.Hashtable;
import java.util.StringTokenizer;


public class BaseDictionary {

	private BaseDictionary() { initialiseDictionary(); }
	
	private static BaseDictionary ref;
	private static Hashtable < String, Character > dict;
	private static Hashtable < String, Character > defdict;
	private static Hashtable < String, Character > aadict;
	
	public static BaseDictionary getInstance() { 
		
		return ( ref == null ) ? new BaseDictionary() : ref; 
		
	}
	
	private static void initialiseDictionary() {
		
		String[] aminoAcids = { "GLY", "ALA", "VAL", "LEU", "ILE", "SER",
								"THR", "TYR", "PRO", "TRP", "ARG", "LYS",
								"ASP", "ASN", "GLU", "GLN", "MET", "CYS", "PHE", "HIS" };		

		String[] modifiedBases = { "A:ADP:ATP:6IA:AET:1MA:2MA:MIA:I:+A:APN:A23:AVC:3DA:MA6:MAD:"
			+ "12A:RIA:AP7:T6A:6HA:PU:PPU:5AA:A2M:A2L:DA:PYO:SUR:AS:PR5:6MZ:LCA:ANZ", 
			"C:CDP:CTP:4AC:5MC:OMC:CPN:CCC:+C:IC:5IC:5PC:SRA:CH:CAR:6HC:CBV:"
			+ "C25:LC:C2L:S4C:P1P:SC:4OC:CSL:10C",
			"M2G:2MG:OMG:G7M:7MG:QUO:1MG:YG:GPN:GDP:GTP:+G:PGP:GMP:GS:"
			+ "G:IG:CB2:5CG:BGM:YYG:MGT:6HG:GAO:G25:LG:G2L:2PR",
		"T:U:PSU:H2U:5MU:4SU:S4U:DHU:OMU:+U:+T:TDP:TTP:RT:2MU:M2U:FMU:P:UMS:"
			+ "UDP:UTP:UPN:T3P:MMT:BRU:5BU:FHU:PDU:SSU:70U:UR3:TPN:TSP:TCP:6HT:U8U:UAR:U25:LHU:"
			+ "MNU:U2L:IU:UD5:TS:ONE:CM0",
			"ROB:N",
			"PQ1:R"};
		
		char[] bases = { 'A', 'C', 'G', 'U', 'N', 'R' };
		
		String[] defaultBases = { "A", "ADE", "C", "CYT", "G", "GUA", "T", "THY", "U", "URA" };
		
		dict = new Hashtable < String, Character > ();
		defdict = new Hashtable < String, Character > ();
		aadict = new Hashtable < String, Character > (); 
		
		for ( int i = 0; i < bases.length; ++i ) {
			
			StringTokenizer to = new StringTokenizer( modifiedBases[ i ], ":" );
			while ( to.hasMoreTokens() ) dict.put( to.nextToken(), bases[ i ] );
			
		}
		
		for ( int i = 0; i < defaultBases.length; ++i ) defdict.put( defaultBases[ i ], defaultBases[ i ].charAt( 0 ) );
		
		for ( int i = 0; i < aminoAcids.length; ++i ) aadict.put( aminoAcids[ i ], '+' );
		
	}
	
	public boolean isDefaultBase( String base ) {
		
		return defdict.containsKey( base ) || base.charAt( 0 ) == '*';
		
	} 
	
	public char getBaseCode( String base ) {
		
		char ret = ( dict.containsKey( base ) ) ? dict.get( base ) : '?';
		if ( ret == '?' && !isAminoAcid( base ) ) {
			
			System.out.println( "BASEDICTIONARY MISSING ENTRY FOR: " + base );
			
		} 
		
		return ret;
		
	}
	
	public boolean isAminoAcid( String id ) {
		
		boolean res = aadict.containsKey( id );
		if ( res ) System.out.println( "RNA contains amino acid " + id + " -> tRNA?" );
		return res;
	}
	
	public static void showDictionary() {
		
		for ( String s : dict.keySet() ) {
			
			System.out.println( s + "->" + dict.get( s ) );
			
		}
		
	}
}
