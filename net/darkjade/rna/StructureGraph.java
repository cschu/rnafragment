package net.darkjade.rna;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

class TertiaryStructureLink {
	
	public BaseContact contact;
	public StemMotif stem1, stem2;
	public ArrayList < BaseContact > contacts;
	
	public TertiaryStructureLink( BaseContact bc, StemMotif s1, StemMotif s2 ) {
		
		contacts = new ArrayList < BaseContact > ();
		contacts.add( bc );
		stem1 = s1;
		stem2 = s2;
		
	}
	
	public boolean touchesStem( StemMotif stem ) { return stem.equals( stem1 ) || stem.equals( stem2 ); }
	
}

class BaseIdentifier {
	
	private int index;
	private char icode;
	
	public BaseIdentifier( int ix, char ic ) { index = ix; icode = ic; }
	public int hashCode() { return ( index << 8 | icode ); }
	public boolean equals( Object compare ) {
		
		BaseIdentifier bi = (BaseIdentifier)compare;
		return ( icode == bi.getIcode() && index == bi.getIndex() && hashCode() == bi.hashCode() );
		
	}
	public char getIcode() {
		return icode;
	}
	public int getIndex() {
		return index;
	}
	
}

public class StructureGraph {	

	protected ArrayList < SecondaryStructureMotif > secondaryStructureMotifs;
	private ArrayList < Base > vertices;
	protected ArrayList < BaseContact > edges;
	private Hashtable < BaseIdentifier, Integer > baseHash2;
	private Hashtable < Integer, Integer > baseHash;	
	// [ [ WC, H, S, ... ], ... ]
	private ArrayList < ArrayList < Integer > > contactMap;		
	protected StemMotif rootHelix;	
	protected ArrayList < BaseContact > secondaryContacts;
	protected ArrayList < BaseContact > tertiaryContacts;
	protected ArrayList < TertiaryStructureMotif > tertiaryStructureMotifs;
	protected ArrayList < BaseContact > allTertiaryContacts;
	protected ArrayList < PseudoknotMotif > pknots;
	protected ArrayList < String > infostrings;
	protected ArrayList<ChainMotif> chainmotifs;
	protected ArrayList<ChainMotif> flankedchainmotifs;
	
	protected ArrayList< TertiaryStructureLink > tertiaryLinks;
	
	private String idString;
	protected ArrayList<RingMotif> ringmotifs;
	public String getIdString() { return idString; }
	
	public void setInfoStrings( ArrayList < String > info ) {
		
		infostrings = new ArrayList < String > ();
		Iterator < String > it = info.iterator();
		
		while ( it.hasNext() ) {
			
			infostrings.add( it.next() );
			
		}
		
	}
	
	public ArrayList < String > getInfoStrings() { 
		return infostrings;
	}
	
	public ArrayList < Base > getBaseSubset( ArrayList < int[] > chains ) {
		
		ArrayList < Base > bases = new ArrayList< Base > ();
		Iterator < int[] > it = chains.iterator();
		
		while ( it.hasNext() ) {
			
			int[] arr = it.next();
			int start = arr[ 0 ];
			int end = arr[ 1 ];
			for ( int i = start; i <= end; ++i ) bases.add( this.vertices.get( i ) );
			
		}
		
		return bases;
		
	}
	
	public ArrayList < Base > getAnchorBaseSubset( ArrayList < Integer > anchors ) {
		
		ArrayList < Base > bases = new ArrayList< Base > ();
		Iterator < Integer > it = anchors.iterator();
		
		while ( it.hasNext() ) {
		
			int i = it.next();
			if ( i != -1 ) bases.add( this.vertices.get( i ) );
			
		}
		
		return bases;
	}
	
	public String getFragmentStructures( ArrayList < int[] > chains ) {
		
		String seq = getSequence();
		String sst = getSecondaryStructureBracket();
		StringBuilder sbSSt = new StringBuilder();
		StringBuilder sbSeq = new StringBuilder();
		
		//Collections.sort( chains );
		
		Iterator < int[] > it = chains.iterator();
		
		while ( it.hasNext() ) {
			
			int[] arr = it.next();
			int start = arr[ 0 ];
			int end = arr[ 1 ];
			
			if ( start <= end ) {
				sbSSt.append( sst.substring( start , end + 1 ) + "|" );
				sbSeq.append( seq.substring( start, end + 1 ) + "|" ); 
			} else {
				System.out.println( "Error in getFragmentStructures(): " + start + ">=" + end );
				sbSSt = new StringBuilder();
				sbSeq = new StringBuilder();
				break;
			}
			
		}
		
		if ( sbSSt.length() != 0 && sbSSt.charAt( sbSSt.length() - 1 ) == '|' ) 
			sbSSt.deleteCharAt( sbSSt.length() - 1 );
		if ( sbSeq.length() != 0 && sbSeq.charAt( sbSeq.length() - 1 ) == '|' ) 
			sbSeq.deleteCharAt( sbSeq.length() - 1 );
		
		return sbSeq.toString() + "&" + sbSSt.toString();		
		
	}
	
	public ArrayList < StemMotif > getHelices() {
		
		ArrayList < StemMotif > helices = new ArrayList < StemMotif > ();
		
		LinkedList < StemMotif > d = new LinkedList < StemMotif > ();
		Stack < StemMotif > s = new Stack < StemMotif > ();
				
		d.addFirst( rootHelix );
		while ( !d.isEmpty() ) {
			
			StemMotif h = d.removeFirst();
			if ( h.parent != null ) helices.add( h ); 
			
			for ( StemMotif ch : h.children ) s.push( ch );
			while ( !s.isEmpty() ) d.addFirst( s.pop() );			
			
		}
		
		return helices;
		
	}
	
	public String getSecondaryStructureBracket() {
		
		StringBuilder sstruc = new StringBuilder();
		for ( int i = 0; i < vertices.size(); ++i ) sstruc.append('.');
		for ( BaseContact bc : secondaryContacts ) {
			
			if ( bc.isCanonical() ) { 
				// could check here for occupied edges
				sstruc.setCharAt( bc.getIndex_base1(), '(');
				sstruc.setCharAt( bc.getIndex_base2(), ')');
			}
			
		}
		return sstruc.toString();
	}
	
	public String getSequence() {
		
		StringBuffer seq = new StringBuffer( "" );
		for ( Base b : vertices ) seq.append( b.getBasecode() );
		
		return seq.toString();
	}
	
	
	
	StructureGraph() {
		
		this( new ArrayList < Base > (), new ArrayList < BaseContact > (), "" );
		
	}
	
	StructureGraph( ArrayList < Base > v, ArrayList < BaseContact > e, String id ) {
		
		vertices = new ArrayList < Base > ();
		baseHash = new Hashtable < Integer, Integer > ();
		baseHash2 = new Hashtable < BaseIdentifier, Integer > ();
		
		contactMap = new ArrayList < ArrayList < Integer > >();
		edges = new ArrayList < BaseContact > ();
		tertiaryContacts = new ArrayList < BaseContact > ();
		rootHelix = null;
		secondaryStructureMotifs = null;
		tertiaryStructureMotifs = null;
		pknots = new ArrayList < PseudoknotMotif > ();
		allTertiaryContacts = null;
		secondaryContacts = null;
		idString = id;
		infostrings = new ArrayList <String> ();
		ringmotifs = new ArrayList<RingMotif> ();
		chainmotifs = new ArrayList < ChainMotif > ();
		flankedchainmotifs = new ArrayList < ChainMotif > (); 
		tertiaryLinks = new ArrayList < TertiaryStructureLink > ();
		tertiaryStructureMotifs = new ArrayList < TertiaryStructureMotif > ();
		
		insertVertices( v );
		insertEdges( e );		
		
		//System.out.println( "Graph for " + id + " created successfully." );
	}
	
	private void insertVertices( ArrayList < Base > v ) {
		
		int nVertices = 0;
		for ( Base b : v ) addVertex( b, nVertices++ );
				
	}
	
	private void insertEdges( ArrayList< BaseContact > e ) {
		
		for ( BaseContact bc : e ) addEdge( bc );
					
	}
	
	public void addVertex( Base b, int ix ) {
		
		//System.out.println( "Adding base: " + b.getIndex() + " " + ix + " size(contactMap)=" + contactMap.size() ); 
		vertices.add( b ); // basecode/name might still be unprocessed!
		baseHash.put( new Integer( b.getIndex() ), new Integer( ix ) );
		
		BaseIdentifier bi = new BaseIdentifier( b.getIndex(), b.getIcode() );
		baseHash2.put( bi, ix );
		
		ArrayList < Integer > map = new ArrayList < Integer > ();
		map.add( -1 ); // watson-crick edge
		map.add( -1 ); // hoogsteen edge
		map.add( -1 ); // sugar edge
		contactMap.add( map );		
		
	}
	
	public void addEdge( BaseContact bc ) {
		
		//int bi1 = baseHash.get( bc.getIndex_base1() ) == null ? -1 : baseHash.get( bc.getIndex_base1() );
		//int bi2 = baseHash.get( bc.getIndex_base2() ) == null ? -1 : baseHash.get( bc.getIndex_base2() );
		
		BaseIdentifier bid1 = new BaseIdentifier( bc.getIndex_base1(), bc.getIcodeB1() );
		BaseIdentifier bid2 = new BaseIdentifier( bc.getIndex_base2(), bc.getIcodeB2() );
		int bi1 = ( baseHash2.get( bid1 ) == null ) ? -1 : baseHash2.get( bid1 ); 
		int bi2 = ( baseHash2.get( bid2 ) == null ) ? -1 : baseHash2.get( bid2 ); 		
		
		int e1 = bc.getEdge1();
		int e2 = bc.getEdge2();
		char ee1 = ( e1 == 0 ) ? 'W' : ( ( e1 == 1 ) ? 'H' : ( ( e1 == 2) ? 'S' : '?' ) );
		char ee2 = ( e2 == 0 ) ? 'W' : ( ( e2 == 1 ) ? 'H' : ( ( e2 == 2) ? 'S' : '?' ) );
		
		BaseContact bcc = new BaseContact( bi1, bi2, 
				   ' ', ' ',
				   bc.getBase1(), bc.getBase2(), 
				   bc.getBase1_mod(), bc.getBase2_mod(), 
				   ee1, ee2, bc.getConfiguration(), bc.getDescription(), false, false );
		
		//bcc.showme();
		
		boolean bases_exist = ( bi1 > -1 ) && ( bi2 > -1 ) ;
		/* at least one edge unknown
		 * -> pseudo-tertiary contact
		 */
		if ( bases_exist && ( e1 == -1 || e2 == -1 ) ) { 
			
			contactMap.get( bi1 ).add( edges.size() );
			contactMap.get( bi2 ).add( edges.size() );								   
				
			edges.add( bcc ); // <- hashing better?			
			
		} else if ( bases_exist ) {
			
				/*
				 * 	at least one edge is already occupied 
				 */
				if ( ( contactMap.get( bi1 ) ).get( e1 ) != -1 
						|| ( contactMap.get( bi2 ) ).get( e2 ) != -1 ) {
				
//					System.out.println( "Edge conflict:" );				
//					System.out.println( bi1 + " " + e1 + " " + ( contactMap.get( bi1 ) ).get( e1 ) );
//					System.out.println( bi2 + " " + e2 + " " + ( contactMap.get( bi2 ) ).get( e2 ) );
//					System.out.println( "Edge data:" );
//					bc.showme();
				
					// 	decision routine: which contact has priority. 
					// 	suggestion: A-U > G-U, G-U > G-A
					// 
				
				} else { 
					contactMap.get( bi1 ).set( e1, edges.size() );
					contactMap.get( bi2 ).set( e2, edges.size() );
					edges.add( bcc ); 
				
				}				
				
		} else {
				
//			System.out.println( "At least one base is out of chain: ");
//			System.out.println( bc.getIndex_base1() + " " + bc.getIndex_base2() );				
				
		}			
			
	}
	
	public ArrayList<BaseContact> getAllTertiaryContacts() {
		return allTertiaryContacts;
	}


	public Hashtable<Integer, Integer> getBaseHash() {
		return baseHash;
	}


	public ArrayList<ArrayList<Integer>> getContactMap() {
		return contactMap;
	}


	public ArrayList<BaseContact> getEdges() {
		return edges;
	}


	public StemMotif getRootHelix() {
		return rootHelix;
	}


	public ArrayList<SecondaryStructureMotif> getSecondaryStructureMotifs() {
		return secondaryStructureMotifs;
	}


	public ArrayList<BaseContact> getTertiaryContacts() {
		return tertiaryContacts;
	}
	
	public ArrayList<BaseContact> getSecondaryContacts() {
		return secondaryContacts;
	}


	public ArrayList<TertiaryStructureMotif> getTertiaryStructureMotifs() {
		return tertiaryStructureMotifs;
	}


	public ArrayList<Base> getVertices() {
		return vertices;
	}

	public ArrayList<PseudoknotMotif> getPknots() {
		return pknots;
	}

	public ArrayList<RingMotif> getLoopMotifs() {
		
		return ringmotifs;
	}

	public ArrayList<RingMotif> getLoopmotifs() {
		return ringmotifs;
	}

	public ArrayList<ChainMotif> getChainmotifs() {
		return chainmotifs;
	}

	public ArrayList<ChainMotif> getFlankedchainmotifs() {
		return flankedchainmotifs;
	}

}

