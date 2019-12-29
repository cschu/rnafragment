package net.darkjade.rna;

import java.util.ArrayList;

public class ChainMotif {
	
	public int index_base5p, index_base3p;
	public boolean is_flanked;
	private ArrayList < BaseContact > contacts;
	
	public ChainMotif() { this( -1, -1, false ); }
	
	public ChainMotif( int b5p, int b3p, boolean flanked ) {
		
		index_base5p = b5p;
		index_base3p = b3p;
		is_flanked   = flanked;
		contacts = new ArrayList < BaseContact > ();
		
	}
	
	boolean has_valid_length() { return size() - ( 2 * ( is_flanked ? 1 : 0) ) >= 3; }
	
	boolean includesBase( int index ) { return index >= index_base5p && index <= index_base3p; }
	
	boolean includesContact( int ix1, int ix2 ) { return includesBase( ix1 ) && includesBase( ix2 ); }
	
	int size() { return index_base3p - index_base5p + 1; }

	public ArrayList < int[] > getChainIndices() {
		
		ArrayList < int[] > res = new ArrayList < int[] > ();
		int[] arr = { index_base5p, index_base3p };
		res.add( arr );
				
		return res;
	}

	public ArrayList < BaseContact > getContacts() { return contacts; }

	public ArrayList < Integer > getAnchors( int anchor_size ) {
		
		if ( anchor_size <= 0 ) anchor_size = 1;
		else if ( anchor_size > 3 ) anchor_size = 3;
		
		ArrayList < Integer > anchors = new ArrayList < Integer >();
		
		for ( int i = 1; i <= anchor_size; ++i ) anchors.add( index_base5p - i );
		for ( int i = 1; i <= anchor_size; ++i ) anchors.add( index_base3p + i );
				
		return anchors;
		
	}

}
