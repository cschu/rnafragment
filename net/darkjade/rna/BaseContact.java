package net.darkjade.rna;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class BaseContact {

	private int index_base1;
	private int index_base2;
	private char base1;
	private char base2;
	private String base1_mod;
	private String base2_mod;
	private int edge1;
	private int edge2;
	private int configuration;
	private String description;
	private boolean isSynB1;
	private boolean isSynB2;
	private char icodeB1;
	private char icodeB2;
	private boolean isCrossing;
	
	public String toString_old( boolean use_1_numbering ) {
		
		String str = "";
		if ( use_1_numbering ) str += (index_base1+1) + " " + (index_base2+1) + " ";
		else str += (index_base1) + " " + (index_base2) + " ";
		str += base1 + ":" + base2 + " ";
		str += StructureFlags.decodeEdge( edge1 ) + "/" + StructureFlags.decodeEdge( edge2 ) + " ";
		if ( ( configuration & StructureFlags.CONTACT_CIS )  == StructureFlags.CONTACT_CIS )
			str += 'C' + " " 
				+ ( configuration & ~( StructureFlags.CONTACT_CIS | StructureFlags.CONTACT_TRANS ) );
		else if ( ( configuration & StructureFlags.CONTACT_TRANS)  == StructureFlags.CONTACT_TRANS )
			str += 'T' + " " 
				+ ( configuration & ~( StructureFlags.CONTACT_CIS | StructureFlags.CONTACT_TRANS ) );
		else str += '?' + " "
				+ ( configuration & ~( StructureFlags.CONTACT_CIS | StructureFlags.CONTACT_TRANS ) );
		
		return str;
		
	}
	
	public String toString( boolean use_1_numbering ) {
		
		int ix1 = index_base1, ix2 = index_base2;		
		
		if ( use_1_numbering ) { ++ix1; ++ix2; }
		return String.format( "%4d %4d %c:%c %c/%c %c %2d", 
				ix1, ix2, base1, base2, 
				StructureFlags.decodeEdge( edge1 ), StructureFlags.decodeEdge( edge2 ), 
				StructureFlags.decodeOrientation( configuration ), 
				configuration & ~( StructureFlags.CONTACT_CIS | StructureFlags.CONTACT_TRANS | StructureFlags.CONTACT_NOORIENT ) );
		
	}
	
	public void normaliseBasePositions( int offset1, int offset2 ) {
		
		index_base1 += offset1;
		index_base2 += offset2;
		
	}
	
	public BaseContact( int i1, int i2, 
						char ic1, char ic2, 
						char b1, char b2, 
						String m1, String m2, char e1,
						char e2, int conf, String desc, boolean syn1, boolean syn2 ) {
		
		
		index_base1 = i1;
		index_base2 = i2;
		base1 = b1;
		base2 = b2;
		base1_mod = new String( m1 );
		base2_mod = new String( m2 );
		edge1 = StructureFlags.encodeEdge( e1 );
		edge2 = StructureFlags.encodeEdge( e2 );
		configuration = conf;
		description = desc;
		isSynB1 = syn1;
		isSynB2 = syn2;
		icodeB1 = ic1;
		icodeB2 = ic2;
		isCrossing = false;
		
	}
	
	public BaseContact( BaseContact bc ) {
		
		this.index_base1 = bc.index_base1;
		this.index_base2 = bc.index_base2;
		this.base1 = bc.base1;
		this.base2 = bc.base2;
		this.base1_mod = new String( bc.base1_mod );
		this.base2_mod = new String( bc.base2_mod );
		this.edge1 = bc.edge1;
		this.edge2 = bc.edge2;
		this.configuration = bc.configuration;
		this.description = new String( bc.description );
		this.isSynB1 = bc.isSynB1;
		this.isSynB2 = bc.isSynB2;
		this.icodeB1 = bc.icodeB1;
		this.icodeB2 = bc.icodeB2;
		this.isCrossing = bc.isCrossing;
		
	}
	
	
	public boolean isCrossing() { return isCrossing; }
	
	public void markAsCrossing() { isCrossing = true; }
	
	public BaseContact() { this( -1, -1, ' ', ' ', ' ', ' ', "", "", ' ', ' ', -1, "", false, false ); }
	
	public boolean isCanonical() {
		
		boolean isCanonical = ( configuration & StructureFlags.CONTACT_ALL ) == StructureFlags.CONTACT_CIS 
		&& edge1 == 0 && edge2 == 0;
		
		isCanonical = isCanonical 
		&& ( ( base1 == 'G' && ( base2 == 'C' || base2 == 'U' ) ) 
				|| ( base2 == 'G' && ( base1 == 'C' || base1 == 'U' ) ) 
				|| ( base1 == 'A' && base2 == 'U' ) 
				|| ( base2 == 'A' && base1 == 'U' ) );
		
		
		/*if ( ( ( ( configuration & StructureFlags.CONTACT_ALL ) == StructureFlags.CONTACT_CIS ) 
			&& edge1 == 0 && edge2 == 0 ) ) {
				
				if  ( ( base1 == 'G' && ( base2 == 'C' || base2 == 'U' ) ) 
						|| ( base2 == 'G' && ( base1 == 'C' || base1 == 'U' ) ) 
						|| ( base1 == 'A' && base2 == 'U' ) 
						|| ( base2 == 'A' && base1 == 'U' ) ) {
			
					if ( !isCanonical )
					
					System.out.println( "WEIRDNESS: " + base1 + " " + base2 + " " + index_base1 + " " + index_base2 );  
					
				}
				
				//System.out.println( "WEIRDNESS: " + base1 + " " + base2 );  
			}*/
		
		/*if ( !isCanonical )
			
			System.out.println( "WEIRDNESS: " + base1 + " " + base2 + " " + index_base1 + " " + index_base2 );*/  
		
		return isCanonical;
		/*return ( configuration & StructureFlags.CONTACT_ALL ) == StructureFlags.CONTACT_CIS 
			&& edge1 == 0 && edge2 == 0;
			/*&& ( ( base1 == 'G' && ( base2 == 'C' || base2 == 'U' ) ) 
			|| ( base2 == 'G' && ( base1 == 'C' || base1 == 'U' ) ) 
			|| ( base1 == 'A' && base2 == 'U' ) 
			|| ( base2 == 'A' && base1 == 'U' ) );*/
		
	}

	public String createTString2( String pstr, int[] offsets ) {
		
		StringBuilder sbTst = new StringBuilder( pstr );
		for ( int i = 0; i < sbTst.length(); ++i ) {			
			if ( sbTst.charAt( i ) == '|' ) sbTst.setCharAt( i, '|' );
			else sbTst.setCharAt( i, '-' );
		}
		
		int ix = Math.min( getIndex_base1(), getIndex_base2() );
		/*for ( int i = 0; i < offsets.length; ++i ) {
			if ( ix > offsets[ i ] ) --ix;			 
		}*/
		sbTst.setCharAt( ix, '[');
		ix = Math.max( getIndex_base1(), getIndex_base2() );
		/*for ( int i = 0; i < offsets.length; ++i ) {
			if ( ix > offsets[ i ] ) --ix;			 
		}*/			
		sbTst.setCharAt( ix, ']');
		
		return sbTst.toString();		
		
	}

	public String createTString( String pstr, ArrayList < int[] > chains ) {
		
		StringBuilder sbTst = new StringBuilder( pstr );
		for ( int i = 0; i < sbTst.length(); ++i ) {
			
			if ( sbTst.charAt( i ) == '|' ) sbTst.setCharAt( i, '|' );
			else sbTst.setCharAt( i, '-' );
			
		}
		
		int bi1 = getIndex_base1();
		int bi2 = getIndex_base2();
		
		//Collections.sort( chains ); // <- ERROR IN T-FRAGMENTS!
				
		int curpos = 0;
		
		Iterator < int[] > it = chains.iterator();
		int found = 0;
		
		while ( it.hasNext() && found != 3 ) {
			
			int[] arr = it.next();
			int start = arr[ 0 ];
			int end   = arr[ 1 ];
			
			if ( bi1 >= start && bi1 <= end ) {
				
				int pos = curpos + ( bi1 - start );
				
				sbTst.setCharAt( pos, '[' );
				found |= 1;
				
			}
			if ( bi2 >= start && bi2 <= end ) {
				
				int pos = curpos + ( bi2 - start );
				sbTst.setCharAt( pos, ']' );
				found |= 2;
				
			}
			curpos += ( end - start ) + 2;
			
		}		
		
		return sbTst.toString();
	}


	public void showme() {}		
	
	public char getBase1() { return base1; }

	public String getBase1_mod() { return base1_mod; }

	public char getBase2() { return base2; }

	public String getBase2_mod() { return base2_mod; }

	public int getConfiguration() { return configuration; }

	public int getEdge1() { return edge1; }

	public int getEdge2() { return edge2; }

	public int getIndex_base1() { return index_base1; }

	public int getIndex_base2() { return index_base2; }

	public String getDescription() { return description; }	

	public boolean isSynB1() { return isSynB1; }

	public boolean isSynB2() { return isSynB2; }

	public char getIcodeB1() { return icodeB1; }

	public char getIcodeB2() { return icodeB2; }	
	
}
