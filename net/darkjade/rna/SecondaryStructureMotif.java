package net.darkjade.rna;
import java.util.ArrayList;
import java.util.Iterator;

public class SecondaryStructureMotif {
	
	StemMotif motherStem;
	ArrayList < StemMotif > childStems;
	String type;
	int type_int;
	ArrayList < BaseContact > intraTertiaryContacts;
	RingMotif ring;
	
	public ArrayList < int[] > getChainIndices() {
				
		ArrayList < int[] > chains = new ArrayList < int[] > ();
		int ct;
		int[] arr;
		
		if ( childStems.size() > 0 ) {
			
			arr = new int[ 2 ];
			arr[ 0 ] = motherStem.b1;
			arr[ 1 ] = childStems.get( 0 ).b3;
			chains.add( arr );			
						
			for ( ct = 0; ct < childStems.size() - 1; ++ct ) {
			
				arr = new int[ 2 ];
				arr[ 0 ]= childStems.get( ct ).b4;
				arr[ 1 ]= childStems.get( ct + 1 ).b3;
				chains.add( arr );
				
			} 
			
			arr = new int[ 2 ];
			arr[ 0 ] = childStems.get( childStems.size() - 1 ).b4;
			arr[ 1 ] = motherStem.b2;			
			chains.add( arr );
			
		} else {
			
			arr = new int[ 2 ];
			arr[ 0 ]= motherStem.b1;
			arr[ 1 ]= motherStem.b2;			
			chains.add( arr );
			
		}
		
		return chains;
		
	}
	
	public String getType() {
		
		String str = "";
		if ( childStems.size() == 0 ) str = "HAIRPIN";
		else if ( childStems.size() == 1 ) str = "INTERNAL";
		else str = childStems.size() + "-MULTILOOP";
		
		return str;
		
	}
	
	SecondaryStructureMotif( StemMotif mom, int t ) {
		
		motherStem = mom;
		type_int = t;
		childStems = new ArrayList < StemMotif > ();
		childStems.addAll( mom.children );
		intraTertiaryContacts = new ArrayList < BaseContact > ();
		for ( StemMotif ch : childStems ) 
			for ( BaseContact bc : ch.parentContacts ) 
				if ( includesBase( bc.getIndex_base1() ) != StructureFlags.NO_CONTACT && includesBase( bc.getIndex_base2() ) != StructureFlags.NO_CONTACT ) 
					//intraTertiaryContacts.addAll( ch.parentContacts );
					intraTertiaryContacts.add( bc );
		ring = extractLoop();
		
	}
	
	public StemMotif getHelixByBase( int base ) {

		StemMotif h = null;
		if ( motherStem.includesBase( base ) != StructureFlags.NO_CONTACT ) h = motherStem;
		else {
			
			for ( StemMotif ch : childStems ) 				
				if ( ch.includesBase( base ) != StructureFlags.NO_CONTACT ) {
					
					h = ch;
					break;
					
				}
			
		}		
		
		return h;		
	}
	
	public boolean hitsRing( BaseContact bc ) {
		
		return ring.includesBase( bc.getIndex_base1() ) != StructureFlags.NO_CONTACT &&
			ring.includesBase( bc.getIndex_base2() ) != StructureFlags.NO_CONTACT;
		
	}
	
	public int includesBase( int base ) {
		
		int where = StructureFlags.NO_CONTACT;
				
		where = motherStem.includesBase( base );
		//where = ( where > 0 ) ? where | StructureFlags.IN_MOTHERSTEM : where;
		
		
		Iterator < StemMotif > it = childStems.iterator();
		while ( it.hasNext() && where == StructureFlags.NO_CONTACT ) where = it.next().includesBase( base );

		if ( where == StructureFlags.NO_CONTACT && childStems.size() > 0 ) {
		
			where = ( ( base > motherStem.b3 && base < childStems.get( 0 ).b1 ) ||
					  ( base > childStems.get( childStems.size() - 1 ).b2 && base < motherStem.b4 ) ) 
					  ? StructureFlags.UNPAIRED_CONTACT : where;
			
			int i = 1;
			StemMotif h1, h2;
			
			while (  where == StructureFlags.NO_CONTACT && i < childStems.size() - 1 ) {
			
				h1 = childStems.get( i++ );
				h2 = childStems.get( i );
			
				where = ( base > h1.b2 && base < h2.b1 ) ? StructureFlags.UNPAIRED_CONTACT : where;
			}	
		
		} else if ( where == StructureFlags.NO_CONTACT ) { // hairpin
			
			where = ( base > motherStem.b3 && base < motherStem.b4 ) 
						? StructureFlags.UNPAIRED_CONTACT : where;
			
		}
		
		return where; 
		
	}

	public RingMotif extractLoop() {
		
		ArrayList < StemMotif > stems = new ArrayList < StemMotif > ();
		
		if ( motherStem.isSingle() ) { 
			stems.add( new StemMotif( motherStem.b3, motherStem.b4,
					motherStem.basePairs.get( motherStem.basePairs.size() - 1 ) ) );
		} else {
			
			StemMotif stem = new StemMotif( motherStem.b3 - 1, motherStem.b4 + 1, motherStem.b3, motherStem.b4,
					motherStem.basePairs.get( motherStem.basePairs.size() - 1 ) );
			stem.basePairs.add( motherStem.basePairs.get( motherStem.basePairs.size() - 2 ) );			
			stems.add( stem );			
			
		}
		
		
		for ( StemMotif h : childStems ) {
			if ( h.isSingle() ) stems.add( new StemMotif( h.b1, h.b2, h.basePairs.get( 0 ) ) );
			else {
				
				StemMotif stem = new StemMotif( h.b1, h.b2, h.b1 + 1, h.b2 - 1, h.basePairs.get( 0 ) );
				stem.basePairs.add( h.basePairs.get( 1 ) );
				stems.add( stem );
				
			}
		}
		
		RingMotif loo = new RingMotif( stems );		
		
		for ( BaseContact bc : intraTertiaryContacts ) {
			
			if ( ( loo.includesBase( bc.getIndex_base1() ) != StructureFlags.NO_CONTACT ) 
					&& 	( loo.includesBase( bc.getIndex_base2() ) != StructureFlags.NO_CONTACT ) ) {
						
						loo.addContact( bc );
						
					}
			
		}
		
		return loo;		
	}
	
}