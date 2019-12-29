package net.darkjade.rna;

import java.util.ArrayList;
import java.util.Iterator;

public class RingMotif {
	
	private ArrayList < StemMotif > basepairs;
	private ArrayList < BaseContact > contacts;
	public ArrayList < ChainMotif > flanked;
	public ArrayList < ChainMotif > nonflanked;
	
	public int size() { return basepairs.size(); }
	
	public RingMotif() {}
	
	public RingMotif( ArrayList < StemMotif > bp ) {
		
		basepairs = new ArrayList<StemMotif> ();
		contacts = new ArrayList<BaseContact> ();
		basepairs.addAll( bp );
		flanked = new ArrayList<ChainMotif> ();
		nonflanked = new ArrayList<ChainMotif> ();
		
		for ( StemMotif stem : basepairs ) contacts.addAll( stem.basePairs );
		
		flanked = getLoopRegions( true );
		nonflanked = getLoopRegions( false );
		
	}
	
	public ArrayList < ChainMotif > getLoopRegions( boolean flanked )	{
		
		ArrayList< ChainMotif > loops = new ArrayList < ChainMotif > ();
		
		int offset = flanked ? 0 : 1;
		ChainMotif chain;
		
		if ( basepairs.size() == 1 ) {
			
			chain = new ChainMotif( basepairs.get( 0 ).b3 + offset, basepairs.get( 0 ).b4 - offset, flanked );
			
			//if ( chain.has_valid_length() ) {
			for ( BaseContact bc : contacts ) {
				if ( chain.includesContact( bc.getIndex_base1(), bc.getIndex_base2() ) )
					chain.getContacts().add( bc );
			}	
			loops.add( chain ); 
			//}
			
		} else if ( basepairs.size() >= 2 ) {
			
			chain = new ChainMotif( basepairs.get( 0 ).b3 + offset, basepairs.get( 1 ).b1 - offset, flanked );
			
			if ( chain.has_valid_length() ) {
				for ( BaseContact bc : contacts ) {
					if ( chain.includesContact( bc.getIndex_base1(), bc.getIndex_base2() ) )
							chain.getContacts().add( bc );
				}
				loops.add( chain ); 
			}
			
			for ( int i = 1; i < basepairs.size() - 1; ++i ) {
				
				chain = new ChainMotif( basepairs.get( i ).b2 + offset, basepairs.get( i + 1 ).b1 - offset, flanked );
				
				if ( chain.has_valid_length() ) {
					for ( BaseContact bc : contacts ) {
						if ( chain.includesContact( bc.getIndex_base1(), bc.getIndex_base2() ) )
							chain.getContacts().add( bc );
					}
					loops.add( chain ); 
				}
				
			}
			
			chain = new ChainMotif( basepairs.get( basepairs.size() - 1 ).b2 + offset, basepairs.get( 0 ).b4 - offset, flanked );
			
			if ( chain.has_valid_length() ) {
				for ( BaseContact bc : contacts ) {
					if ( chain.includesContact( bc.getIndex_base1(), bc.getIndex_base2() ) )
							chain.getContacts().add( bc );					
				}
				loops.add( chain ); 
			}
			
		}	
		
		return loops;
		
	}
	
	public int includesBase( int base ) {
		
		int where = StructureFlags.NO_CONTACT;
		
		Iterator < StemMotif > it = basepairs.iterator();
				
		while ( it.hasNext() && where == StructureFlags.NO_CONTACT ) where = it.next().includesBase( base );

		if ( where == StructureFlags.NO_CONTACT && basepairs.size() > 1 ) {
		
			where = ( ( base > basepairs.get( 0 ).b3 && base < basepairs.get( 1 ).b1 ) ||
					  ( base > basepairs.get( basepairs.size() - 1 ).b2 && base < basepairs.get( 0 ).b4 ) ) 
					  ? StructureFlags.UNPAIRED_CONTACT : where;
			
			int i = 1;
			StemMotif h1, h2;
			
			while (  where == StructureFlags.NO_CONTACT && i < basepairs.size() - 1 ) {
			
				h1 = basepairs.get( i++ );
				h2 = basepairs.get( i );
			
				where = ( base > h1.b2 && base < h2.b1 ) ? StructureFlags.UNPAIRED_CONTACT : where;
			}	
		
		} else if ( where == StructureFlags.NO_CONTACT && basepairs.size() > 0 ) { // hairpin
			
			where = ( base > basepairs.get( 0 ).b3 && base < basepairs.get( 0 ).b4 ) 
						? StructureFlags.UNPAIRED_CONTACT : where;
			
		}
		
		return where; 
		
	}
	
	public ArrayList < Integer > getAnchors( int anchor_size ) {
		
		if ( anchor_size <= 0 ) anchor_size = 1;
		else if ( anchor_size > 3 ) anchor_size = 3;
		
		ArrayList < Integer > anchors = new ArrayList < Integer >();
		
		Iterator < StemMotif > it = basepairs.iterator();
		StemMotif h;
		if ( it.hasNext() ) h = it.next();
		while ( it.hasNext() ) {
			h = it.next();
			anchors.addAll( h.getAnchors( anchor_size ) );
		}
		
		return anchors;
		
	}
	
	public ArrayList < int[] > getChainIndices() {
		
		ArrayList < int[] > chains = new ArrayList < int[] >();
		int ct;
		int[] arr;
		
		if ( basepairs.size() > 1 ) {
			
			arr = new int[ 2 ];
			arr[ 0 ] = basepairs.get( 0 ).b1;
			arr[ 1 ] = basepairs.get( 1 ).b3;
			chains.add( arr );			 
								
			for ( ct = 1; ct < basepairs.size() - 1; ++ct ) {
				
				arr = new int[ 2 ];
				arr[ 0 ] = basepairs.get( ct ).b4;
				arr[ 1 ] = basepairs.get( ct + 1 ).b3;
				chains.add( arr );
				
			} 
			
			arr = new int[ 2 ];
			arr[ 0 ] = basepairs.get( basepairs.size() - 1 ).b4;
			arr[ 1 ] = basepairs.get( 0 ).b2;
			chains.add( arr );
		
		} else if ( basepairs.size() == 1 ){
			
			arr = new int[ 2 ];
			arr[ 0 ] = basepairs.get( 0 ).b1;
			arr[ 1 ] = basepairs.get( 0 ).b2;			
			chains.add( arr );
			
		}
		
		return chains;
		
	}

	public void addContact( BaseContact bc ) { contacts.add( bc ); }
	
	public ArrayList<BaseContact> getContacts() { return contacts; }

	public ArrayList<StemMotif> getBasepairs() { return basepairs; }

}
