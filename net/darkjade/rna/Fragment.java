package net.darkjade.rna;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Hashtable;



public class Fragment {

	private String id;
	private String type;	
	private ArrayList < Integer > chains;
	private ArrayList < Integer > helices;
	private ArrayList < Base > bases;
	private ArrayList < BaseContact > contacts;	
	
	// hairpin only
	public int getLoopLength() {
		
		return helices.get( 3 ) - helices.get( 2 ) - 1;
		
	}
	
	public ArrayList < Base > getLoopRegion() {
		
		ArrayList < Base > loop = new ArrayList < Base > ();
		
		int start = helices.get( 2 );// - chains.get( 0 );
		int end   = helices.get( 3 );// - chains.get( 0 );
		
		for ( int i = start + 1; i < end; ++i )
			loop.add( bases.get( i ) );
		
		return loop;		
		
	}
	
	public Fragment( 	String id, String type,
						ArrayList < Base > bases, 
						ArrayList < BaseContact > contacts, 
						ArrayList < Integer > chains, 
						ArrayList < Integer > helices,
						int gaplen ) {

		this.id = id;
		this.type = type;
		this.bases = new ArrayList < Base > ();
		this.bases.addAll( bases );
		
		Hashtable < Integer, Integer > map = new Hashtable < Integer, Integer > ();
		
		int ct = 0;
		Iterator< Integer > it = chains.iterator();
		while ( it.hasNext() ) {
			
			int start = it.next();
			
			if ( it.hasNext() ) {
				
				int end = it.next();
				
				for ( int i = start; i <= end; ++i ) {
					
					if ( !map.containsKey( i ) ) { 
					
						map.put( i, ct++ );
						
					}
				}				
			}
			ct += gaplen;
		}
		
		//System.out.println( map.toString() );
		
		this.contacts = new ArrayList < BaseContact > ();
		this.helices = new ArrayList < Integer > ();
		
		for ( BaseContact bc : contacts ) {
			
			int bi1 = ( map.containsKey( bc.getIndex_base1() ) ) 
								? map.get( bc.getIndex_base1() ) 
								: -1;
			int bi2 = ( map.containsKey( bc.getIndex_base2() ) ) 
								? map.get( bc.getIndex_base2() ) 
								: -1;
			
			if ( bi1 != -1 && bi2 != -1 ) 
				this.contacts.add( 
						new BaseContact( 
								bi1, bi2, 
								bc.getIcodeB1(), bc.getIcodeB2(),
								bc.getBase1(), bc.getBase2(), 
								bc.getBase1_mod(), bc.getBase2_mod(),
								StructureFlags.decodeEdge( bc.getEdge1() ), 
								StructureFlags.decodeEdge( bc.getEdge2() ),
								bc.getConfiguration(),
								bc.getDescription(),
								bc.isSynB1(), bc.isSynB2() ) );
				
			
		}
		
		for ( Integer ix : helices ) {
			
			if ( map.containsKey( ix ) )
				this.helices.add( map.get( ix ) );
			
		}
	}



	public ArrayList<Base> getBases() {
		return bases;
	}



	public ArrayList<Integer> getChains() {
		return chains;
	}



	public ArrayList<BaseContact> getContacts() {
		return contacts;
	}



	public ArrayList<Integer> getHelices() {
		return helices;
	}



	public String getId() {
		return id;
	}



	public String getType() {
		return type;
	}
	
}
