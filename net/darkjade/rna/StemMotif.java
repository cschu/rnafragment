package net.darkjade.rna;
import java.util.ArrayList;
import java.util.Iterator;


public class StemMotif {
	
	int b1, b2, b3, b4;	
	ArrayList < BaseContact > basePairs;
	ArrayList < BaseContact > noncanonicalContacts;	
	ArrayList < BaseContact > parentContacts;
	
	public ArrayList < int[] > getChainIndices() {
		
		ArrayList < int[] > chains = new ArrayList < int[] >();
		int[] arr = { b1, b3 };
		chains.add( arr );
		int[] arr2 = { b4, b2 };
		chains.add( arr2 );
		
		return chains;
		
	}
	
	public boolean equals( StemMotif h ) {
		
		return b1 == h.b1 && b2 == h.b2 && b3 == h.b3 && b4 == h.b4; 
		
	}
	
	public String toString() {
		
		String str = "";
		
		str += b1 + " " + b2 + " " + b3 + " " + b4; 
		
		return str;
		
	}
	
	public StemMotif getLastEnclosingStem( int base ) {
		
		StemMotif stem = null;
		
		Iterator < StemMotif > sit = children.iterator();
		while ( stem == null && sit.hasNext() ) { 
			stem = sit.next().getLastEnclosingStem( base );			
		}		
		
		if ( parent != null && stem == null && base >= b1 && base <= b2 ) stem = this;
		
		return stem;
		
	}
	
	public boolean includesContact( int b1, int b2 ) {
		
		return this.includesBase( b1 ) != StructureFlags.NO_CONTACT 
			&& this.includesBase( b2 ) != StructureFlags.NO_CONTACT;
		
	}
	
	public int includesBase( int base ) {
	
		return ( ( base >= b1 && base <= b3 ) || ( base >= b4 && base <= b2 ) ) ? StructureFlags.PAIRED_CONTACT : StructureFlags.NO_CONTACT;
		/*	
		int where = StructureFlags.NO_CONTACT; // base is not part of the helix
		
		//if ( ( base >= b1 && base <= b2 ) || ( base >= b3 && base <= b4 ) ) {
		if ( ( base >= b1 && base <= b3 ) || ( base >= b4 && base <= b2 ) ) {
			
			where = StructureFlags.UNPAIRED_CONTACT; // base can be any base within the helix		
			
			Iterator < BaseContact > it = basePairs.iterator();
			while ( it.hasNext() && where == StructureFlags.UNPAIRED_CONTACT ) {
			
				BaseContact bc = it.next();
				// if base is paired, stop search
				where = ( bc.getIndex_base1() == base || bc.getIndex_base2() == base ) 
							? StructureFlags.PAIRED_CONTACT 
							: where;
								 
			}
			
		}
		
		return where;*/
		
	}	
	
	boolean crossesParent;
	
	StemMotif parent;
	ArrayList < StemMotif > children;
		
	private StemMotif ( int b1, int b2, int size ) { this( b1, b2, b1 + size, b2 - size, new BaseContact() ); }
	public StemMotif () { this( -1, -1, -1, -1, new BaseContact() ); }
	public StemMotif ( int b1, int b2, BaseContact bc ) { this( b1, b2, b1, b2, bc ); }
	public StemMotif ( int b1, int b2, int b3, int b4, BaseContact bc ) {
		
		this.b1 = b1;
		this.b2 = b2;
		this.b3 = b3;
		this.b4 = b4;		
		basePairs = new ArrayList < BaseContact > ();
		basePairs.add( bc );
		parent = null;
		children = new ArrayList < StemMotif > ();
		crossesParent = false;
		noncanonicalContacts = new ArrayList < BaseContact > ();
		parentContacts = new ArrayList < BaseContact > ();
	}
	
	public boolean isChild( StemMotif parCandidate ) {
		
		return this.parent != null && !equals( this.parent ) && this.parent.equals( parCandidate );
		
	}
	
	public void addNonCanonical( BaseContact bc ) {
		
		noncanonicalContacts.add( bc );
		
	}
	
	public void grow( int b1, int b2, BaseContact bc ) {
		
		this.b3 = b1;
		this.b4 = b2;
		basePairs.add( bc );
		
	}	
	
	public int size() {
		return b3 - b1 + 1;		
	}
	
	public void showme() {
		
//		System.out.println( "HELIX (" + b1 + ", " + b2 + ", " + b3 + ", " + b4 
//				+ ", " + basePairs.size() + " pair(s) )" );	
//		if ( crossesParent ) System.out.println( "Crosses mother" );
		
	}
	
	public boolean isSingle() { return b1 == b3 && b2 == b4; }
	
	
	public void setParent( StemMotif h ) { parent = h; }
	
	public void addChild( StemMotif h ) {
		
		children.add( h );
		
	}
		
	public int checkConnectivity( int b1, int b2 ) {
		
		int cc = StructureFlags.BPAIR_IGNORE;

		if (b1 == this.b3 + 1 && b2 == this.b4 - 1)
			cc = StructureFlags.BPAIR_TOUCHING;
		//else if (b1 == this.b3 + 1 && b2 < this.b4 - 1)
		//	cc = BPFlags.BPAIR_TOUCHING | BPFlags.BPAIR_RBULGE;
		//else if (b1 > this.b3 + 1 && b2 == this.b4 - 1)
		//	cc = BPFlags.BPAIR_TOUCHING | BPFlags.BPAIR_LBULGE;
		else if ( ( b1 >= this.b3 + 1 && b2 < this.b4 - 1 ) || ( b1 > this.b3 + 1 && b2 <= this.b4 - 1 ) )
			cc = StructureFlags.BPAIR_SEPARATE;
		else if (b1 > this.b2 && b2 > this.b2)
			cc = StructureFlags.BPAIR_DISJOINT;
		else if (b1 < this.b4 && b2 > this.b4)
			cc = (b2 > this.b2) ? StructureFlags.BPAIR_CROSSING : StructureFlags.BPAIR_IGNORE;		

		return cc;
		
	}	

	public ArrayList < Integer > getAnchors( int anchor_size ) {
				
		ArrayList < Integer > anchors = new ArrayList < Integer > ();
		
		for ( int i = 1; i <= anchor_size; ++i ) { anchors.add( b3 + i ); }
		anchors.add( -1 );
		for ( int i = 1; i <= anchor_size; ++i ) { anchors.add( b4 - i ); }
		anchors.add( -1 );
		
		return anchors;
		
	}
	
	public StemMotif getCut( int p1, int p2, int size ) {
		
		StemMotif stem = new StemMotif( p1, p2, size - 1 );
		stem.basePairs.remove( 0 );
		
		for ( BaseContact bc : this.basePairs ) { 
			if ( stem.includesContact( bc.getIndex_base1(), bc.getIndex_base2() ) )
				stem.basePairs.add( bc );		
		}
		for ( BaseContact bc : this.noncanonicalContacts ) { 
			if ( stem.includesContact( bc.getIndex_base1(), bc.getIndex_base2() ) )
				stem.noncanonicalContacts.add( bc );		
		}
		
		return stem;
	}
	
	public ArrayList < StemMotif > getAllCuts( int maxsize ) {
		
		ArrayList< StemMotif > res = new ArrayList < StemMotif > ();
		int size = this.size();
		
		/*if ( maxsize < 2 ) maxsize = 2;
		maxsize = Math.min( maxsize, size - 1 );*/		
		
		if ( size > 2 ) {
			
			if ( maxsize < 2 ) maxsize = 2;
			maxsize = Math.min( maxsize, size - 1 );
			
			for ( int i = 2; i <= maxsize; ++i ) {
				
				int lpos = size - i;
				for ( int j = 0; j <= lpos; ++j ) {					
					res.add( this.getCut( b1 + j, b2 - j, i ) );
				}
				
			}
			
		}
		
		return res;
		
	}
	
	
}
