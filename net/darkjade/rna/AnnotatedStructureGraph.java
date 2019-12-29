package net.darkjade.rna;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;



public class AnnotatedStructureGraph extends StructureGraph {
	
	public AnnotatedStructureGraph( ArrayList < Base > v, ArrayList < BaseContact > e, String id ) { super( v, e, id ); }
		
	/**
	 * Calls the processing routines.
	 */
	public void startWorking() {
		
		assembleHelicalRegions();
		mergeHelicalRegions();
		System.out.printf( "%d t-contacts\n", tertiaryContacts.size() );
		assignIntraHelixContacts();
		System.out.printf( "%d t-contacts\n", tertiaryContacts.size() );
		setTertiaryLinks();
		System.out.printf( "%d t-contacts\n", tertiaryContacts.size() );
		assembleSSMotifs();
		assembleTertiaryMotifs2();
		extractLoopMotifs();
		
	}
	
	public void assembleSSMotifs() {
		
		StemMotif h;
		LinkedList < StemMotif > d = new LinkedList < StemMotif > ();
		Stack < StemMotif > s = new Stack < StemMotif > ();
		
		for ( StemMotif ch : rootHelix.children ) s.push( ch );
		while ( !s.isEmpty() ) d.addFirst( s.pop() );
		
		secondaryStructureMotifs 
			= new ArrayList < SecondaryStructureMotif > ();
		
		while ( !d.isEmpty() ) {
		
			h = d.removeFirst(); 
			
			for ( StemMotif ch : h.children ) { s.push( ch ); }
			while ( !s.isEmpty() ) d.addFirst( s.pop() );
						
			secondaryStructureMotifs.add( 
					new SecondaryStructureMotif( h, ( h.children.size() > 2 )
						? 2 : h.children.size() ) );
			
			
		}
		
	}
	
	public void setTertiaryLinks() {
		
		ArrayList < BaseContact > unassignedContacts = new ArrayList < BaseContact > ();
		
		boolean assigned = false;
		
		for ( BaseContact bc : tertiaryContacts ) {
			
			int b1 = bc.getIndex_base1();
			int b2 = bc.getIndex_base2();
			
			StemMotif s1 = rootHelix.getLastEnclosingStem( b1 );
			StemMotif s2 = rootHelix.getLastEnclosingStem( b2 );
			
			if ( s1 != null && s2 != null ) {
				if ( s1.isChild( s2 ) ) { 
					s1.parentContacts.add( bc );
					System.out.printf( "%s contact %s ", "INTER", bc.toString( false ) );
					System.out.println( "in stems: " + s1.toString() + " " + s2.toString() ); 

				} else if ( s2.isChild( s1 ) ) { 
					s2.parentContacts.add( bc );
					System.out.printf( "%s contact %s ", "INTER", bc.toString( false ) );
					System.out.println( "in stems: " + s1.toString() + " " + s2.toString() ); 

				} /*else if ( s1.equals( s2 ) ) {
					s1.noncanonicalContacts.add( bc );
					System.out.printf( "%s contact %s ", "INTRA", bc.toString( false ) );
					System.out.println( "in stems: " + s1.toString() + " " + s2.toString() );
					
				} */ else {
					

					if ( s1.equals( s2 ) ) {
						System.out.printf( "%s contact %s ", "INTRA", bc.toString( false ) );
						System.out.println( "in stems: " + s1.toString() + " " + s2.toString() );						
					}
					
					
					Iterator < TertiaryStructureLink > tsit = tertiaryLinks.iterator();
					assigned = false;
					while ( tsit.hasNext() && !assigned ) {
						
						TertiaryStructureLink tlink = tsit.next();
						if ( tlink.touchesStem( s1 ) && tlink.touchesStem( s2 ) ) {
							tlink.contacts.add( bc );
							assigned = true;
						}						
						
					}
					
					if ( tertiaryLinks.size() == 0 || !assigned ) { 
						tertiaryLinks.add( new TertiaryStructureLink( bc, s1, s2 ) ); 
						System.out.printf( "%s contact %s ", "NEW TSM", bc.toString( false ) );
						System.out.println( "in stems: " + s1.toString() + " " + s2.toString() );						
					} else if ( assigned ) {
						System.out.printf( "%s contact %s ", "MERGE", bc.toString( false ) );
						System.out.println( "in stems: " + s1.toString() + " " + s2.toString() );						
					}	
					
				} 
				
			}
			
		}
		
		tertiaryContacts = unassignedContacts;
		
	}
	
	public void assembleTertiaryMotifs2() {
		
		StemMotif other_stem, first_stem;
		Iterator < TertiaryStructureLink > tsit = tertiaryLinks.iterator();
		
		while ( tsit.hasNext() ) {
			
			TertiaryStructureLink link = tsit.next();
			
			int i, j;
			for ( i = 0; i < secondaryStructureMotifs.size() - 1; ++i ) {
				
				SecondaryStructureMotif ssm1 = secondaryStructureMotifs.get( i );
				
				boolean is_s1 = ssm1.motherStem.equals( link.stem1 );
				boolean is_s2 = ssm1.motherStem.equals( link.stem2 );
				
				if ( is_s1 && is_s2 ) {
					
					ssm1.intraTertiaryContacts.addAll( link.contacts );
					
				} if ( is_s1 || is_s2 ) {
					
					if ( is_s1 ) {
						other_stem = link.stem2;
						first_stem = link.stem1;
					} else {
						other_stem = link.stem1;
						first_stem = link.stem2;
					} 
					
					boolean assigned = false;
					for ( j = i + 1; j < secondaryStructureMotifs.size() && !assigned; ++j ) {
					
						SecondaryStructureMotif ssm2 = secondaryStructureMotifs.get( j );
						
						if ( other_stem.equals( ssm2.motherStem ) ) {
							
							// redundancy check needed! -- happens in setTertiaryLinks()!
							TertiaryStructureMotif tmo = 
								new TertiaryStructureMotif();
							
							int ring_hits_ssm1 = 0, ring_hits_ssm2 = 0;
							for ( BaseContact bc : link.contacts ) {
								if ( ssm1.hitsRing( bc ) ) ++ring_hits_ssm1;
								if ( ssm2.hitsRing( bc ) ) ++ring_hits_ssm2;
							}
							if ( ring_hits_ssm1 == link.contacts.size() )
								tmo.rings.add( ssm1.ring );
							else if ( ring_hits_ssm1 == 0 ) {
								StemMotif h = ssm1.getHelixByBase( link.contacts.get( 0 ).getIndex_base1() );
								if ( h == null ) h = ssm1.getHelixByBase( link.contacts.get( 0 ).getIndex_base2() );
								if ( h != null ) tmo.helices.add( h );
								else tmo.smotifs.add( ssm1 );
							}
							else {
								tmo.smotifs.add( ssm1 );
							}
							if ( ring_hits_ssm2 == link.contacts.size() )
								tmo.rings.add( ssm2.ring );
							else if ( ring_hits_ssm2 == 0 ) {
								StemMotif h = ssm2.getHelixByBase( link.contacts.get( 0 ).getIndex_base1() );
								if ( h == null ) h = ssm2.getHelixByBase( link.contacts.get( 0 ).getIndex_base2() );
								if ( h != null ) tmo.helices.add( h );
								else tmo.smotifs.add( ssm2 );
							}
							else {
								tmo.smotifs.add( ssm2 );
								
							}
							
							for ( BaseContact bc : link.contacts )
								if ( tmo.includesContact( bc ) ) tmo.intraContacts.add( bc ); //tmo.intraContacts.addAll( link.contacts );
							
							tertiaryStructureMotifs.add( tmo );
							assigned = true;
							
						}
						
					}
					
				}
				
			}
			
		}
		
	}
	
	public void mergeHelicalRegions() {
		
		StemMotif h;
		LinkedList < StemMotif > d = new LinkedList < StemMotif > ();
		Stack < StemMotif > s = new Stack < StemMotif > ();
		
		for ( StemMotif ch : rootHelix.children ) s.push( ch );
		while ( !s.isEmpty() ) d.addFirst( s.pop() );
			
		while ( !d.isEmpty() ) {
			
			h = d.removeFirst();
			
			if ( h.children.size() == 1 ) { // bulges and internal loops
				
				StemMotif ch = h.children.get( 0 );
				
				if ( h.isSingle() ) { 
					
					/* this merges a single-bp mother stem with its only child stem, 
					 * thus obtaining a larger stem which includes an interior loop
					 * - stupid idea =P 
					 * */
					
					/*h.b3 = ch.b3;
					h.b4 = ch.b4;
					h.basePairs.addAll( ch.basePairs );
					h.children.addAll( ch.children );
					h.children.remove( 0 );*/
					
					/* new approach: just kick the basepair into tertiary contacts, 
					 * merge the substructure into the main structure*/
					tertiaryContacts.add( h.basePairs.get( 0 ) );
					ch.parent = h.parent;
					int i = 0;
					while ( !h.equals( h.parent.children.get( i ) ) ) ++i;
					h.parent.children.set( i, ch );
					
				}
				
			//deal with stemsize-1 hairpins -> erase from mother stem 
			} else if ( h.children.size() == 0 && h.isSingle() ) {
				
				StemMotif hp = h.parent;
				ArrayList < StemMotif > tmp = new ArrayList < StemMotif > ();
				for ( StemMotif ch : hp.children ) {
					
					if ( !h.equals( ch ) ) { tmp.add( ch ); }
					
				}
				hp.children = tmp;
				tertiaryContacts.add( h.basePairs.get( 0 ) );
				
			} 
			
			for ( StemMotif ch : h.children ) s.push( ch ); 
			while ( !s.isEmpty() ) d.addFirst( s.pop() );
						
		} 
		
	}
			
	public void assignIntraHelixContacts() {
		
		LinkedList < StemMotif > q = new LinkedList < StemMotif > ();
		LinkedList < StemMotif > helices = new LinkedList < StemMotif > ();		
		ArrayList < BaseContact > unassignedContacts = new ArrayList < BaseContact > ();
		
		// gather helices
		for ( StemMotif ch : rootHelix.children ) q.add( ch );		
		while ( !q.isEmpty() ) {
			
			for ( StemMotif ch : q.get( 0 ).children ) q.add( ch );
			helices.add( q.get( 0 ) );
			q.remove( 0 );
			
		}
		
		// now check each contact if it connects two bases within the same stem
		for ( BaseContact bc : tertiaryContacts ) {
			
			boolean contactAssigned = false;
			for ( StemMotif ch : helices ) {
				
				/*if ( ch.includesBase( bc.getIndex_base1() ) != StructureFlags.NO_CONTACT && 
						ch.includesBase( bc.getIndex_base2() ) != StructureFlags.NO_CONTACT ) { */
				if ( ch.includesContact( bc.getIndex_base1(), bc.getIndex_base2() ) ) {
														
					ch.addNonCanonical( bc );
					contactAssigned = true;
					break;					
				} 				
				
			} 
			
			if( !contactAssigned ) unassignedContacts.add( bc );
		}
		
		tertiaryContacts = unassignedContacts;
		
	}
	
	public void assembleHelicalRegions() {
		
		secondaryContacts = new ArrayList < BaseContact >();
		ArrayList < BaseContact > tmpEdges = new ArrayList < BaseContact >();		
		secondaryStructureMotifs = new ArrayList < SecondaryStructureMotif > ();
		
		StemMotif h = null, ch = null;
		int b1 = -1;
		int b2 = -1;
		
		rootHelix = new StemMotif();
		rootHelix.b1 = -666;
		
		// filter non-canonical base-pairs
		for ( BaseContact bc : edges ) {
			if ( !bc.isCanonical() ) 
				tertiaryContacts.add( bc );
			else
				tmpEdges.add( bc ); 
		}
		
		// create helix from first canonical base-pair 
		if ( tmpEdges.size() > 0 ) {
		
			BaseContact bc1 = tmpEdges.get( 0 );
			h = new StemMotif( bc1.getIndex_base1(), bc1.getIndex_base2(), bc1 );
			h.setParent( rootHelix );
			rootHelix.addChild( h );
			tmpEdges.remove( 0 );
			
		}		
		
		// scan & assemble
		for ( BaseContact bc : tmpEdges ) {
				
			b1 = bc.getIndex_base1();
			b2 = bc.getIndex_base2();
				
			boolean isProcessed = false;
								
			while ( !isProcessed ) {
					
				switch( h.checkConnectivity( b1, b2 ) ) {
					
				// shouldn't happen...
				case StructureFlags.BPAIR_IGNORE : 

					bc.markAsCrossing(); // for display reasons
					tertiaryContacts.add( bc );
					isProcessed = true;
					break;
				// current base-pair belongs to current helix
				case ( StructureFlags.BPAIR_TOUCHING | StructureFlags.BPAIR_LBULGE ) :
				case ( StructureFlags.BPAIR_TOUCHING | StructureFlags.BPAIR_RBULGE ) :
				case StructureFlags.BPAIR_TOUCHING :
					h.grow( b1, b2, bc );							
					isProcessed = true;
					
					break;
					
				// current base-pair belongs to next helix &
				// next helix is a child of current helix	
				case StructureFlags.BPAIR_SEPARATE :
						
					// we don't want helices consisting of only one pair
					// (doesn't check the last pair!)
					if ( h.isSingle() ) { // block is new
						bc.markAsCrossing(); // for display reasons
						tertiaryContacts.add( h.basePairs.get( 0 ) );
						ch = h.parent;
						ch.children.remove( ch.children.size() - 1 );										
						h = new StemMotif();
						h = ch;
						continue;
					}
									
					ch = new StemMotif( b1, b2, bc );
					ch.setParent( h );
					h.addChild( ch );									
					h = ch;
					isProcessed = true; 
					break;
					
				// current base-pair belongs to next helix &
				// next helix is a sibling of a former helix (->mloop)
				case StructureFlags.BPAIR_DISJOINT : 
						
					StemMotif tmp = h;
					h = h.parent;
					int cc = -1;
								
					// check if we're back at the root 
					if ( h == null ) {
						ch = new StemMotif( b1, b2, bc );									
						h = tmp;
						ch.setParent( h );
						h.addChild( ch );									
						h = ch;
						isProcessed = true;
									
					} 
					
					// traceback until we find the parent of current base-pair
					while ( !isProcessed ) {
							
						// back at root -> this is no mloop, it's a space station.
						if ( h.parent == null ) {
								
							bc.showme();
							h.showme();
							tmp.showme();
							
							ch = new StemMotif( b1, b2, bc );
							ch.setParent( h );
							h.addChild( ch );
							h = ch;
							isProcessed = true;
						
						// current base-pair does not belong to the next helix
						} else if ( ( cc = h.checkConnectivity( b1, b2 ) ) != StructureFlags.BPAIR_SEPARATE ) {
								
							tmp.showme();
							h.showme();
							
							// if not crossing go on...
							if ( cc != StructureFlags.BPAIR_CROSSING ) {
								
								tmp = h;
								h = h.parent;
							// otherwise -> new tertiary contact
							} else {
											
								tertiaryContacts.add( bc );
								h = tmp;
								isProcessed = true;
									
							} 
						// current base-pair belongs to the next helix	
						} else {
								
							ch = new StemMotif( b1, b2, bc );
							ch.setParent( h );
							h.addChild( ch );										
							h = ch;
							isProcessed = true;
								
						}	
														
					} // while								
								
					break;
				
				// crossing
				// all crossing edges will be classified 
				// as tertiary contact 
				case StructureFlags.BPAIR_CROSSING : 
					
					bc.markAsCrossing(); // for display reasons
					tertiaryContacts.add( bc );
 
						isProcessed = true;
 
					break;
					
				default : break;
					
					
				} // switch		
					
			}
					
		}  // for
		
		ArrayList < StemMotif > helices = getHelices();
		for ( StemMotif hh : helices )			
			secondaryContacts.addAll( hh.basePairs );
			
		allTertiaryContacts = new ArrayList < BaseContact > ();
		allTertiaryContacts.addAll( tertiaryContacts );
		//	System.out.println( "Helix Assembly finished." );
	}
	
	// att: redundant tmotif-formation is already avoided!
	public void assembleTertiaryMotifs() {
		
		
		ArrayList < BaseContact > unassignedContacts = new ArrayList < BaseContact > ();
		
		// begin scanning
		for ( BaseContact bc : tertiaryContacts ) {
			
			boolean contactAssigned = false;		
			if ( bc.getIndex_base1() == 221 && bc.getIndex_base2() == 342 )
				System.out.println("x");
			
			// check each secondary structure motif for intra-motif contacts
			for ( int i = 0; !contactAssigned && i < secondaryStructureMotifs.size(); ++i ) {
				
				SecondaryStructureMotif mo = secondaryStructureMotifs.get( i );
				int bi1Contact = mo.includesBase( bc.getIndex_base1() );
				int bi2Contact = mo.includesBase( bc.getIndex_base2() );
				
				// found intra
				if ( bi1Contact != StructureFlags.NO_CONTACT && bi2Contact != StructureFlags.NO_CONTACT ) {
					
					contactAssigned = true;
					mo.intraTertiaryContacts.add( bc );
					break;
					
				// found first partner
				} else if ( bi1Contact != StructureFlags.NO_CONTACT || bi2Contact != StructureFlags.NO_CONTACT ) {
					
					int secondBase = -1;
					secondBase = ( bi1Contact == StructureFlags.NO_CONTACT ) 
							? bc.getIndex_base1() 
							: bc.getIndex_base2();
								
					// search for second partner
					for ( int j = i + 1; !contactAssigned && j < secondaryStructureMotifs.size(); ++j ) {
							
						SecondaryStructureMotif mo2 = secondaryStructureMotifs.get( j );
						int bjContact = mo2.includesBase( secondBase );
							
						// found a possible partner
						if ( bjContact != StructureFlags.NO_CONTACT ) {
							
							// check for intra-tertiary motif contact 
							// (i.e. exclude creating a motif twice)
							for ( TertiaryStructureMotif tsm : tertiaryStructureMotifs) {
								
								if ( tsm.includesContact( bc ) ) {
									
									tsm.intraContacts.add( bc );
									contactAssigned = true;
									
								} 
								
							}			
							
							// not an intra-motif contact -> new motif
							if ( !contactAssigned ) {

								TertiaryStructureMotif tmo = new TertiaryStructureMotif();
								
								// now check where the contact connects:
								// unpaired-unpaired, paired-paired, paired-unpaired
								int biContact = ( bi1Contact == StructureFlags.UNPAIRED_CONTACT )
									? bi1Contact : bi2Contact;
								
								// first partner is connected in an unpaired region
								if ( biContact == StructureFlags.UNPAIRED_CONTACT )
									tmo.smotifs.add( mo );
								// first partner is connected in a paired region
								else if ( biContact == StructureFlags.PAIRED_CONTACT ) {
									
									StemMotif h = mo.getHelixByBase( bc.getIndex_base1() );
									if ( h == null ) h = mo.getHelixByBase( bc.getIndex_base2() );
									if ( h!= null ) tmo.helices.add( h );
									
								}
								
								// now repeat for second partner
								if ( bjContact == StructureFlags.UNPAIRED_CONTACT ) 
									tmo.smotifs.add( mo2 );
								else if ( bjContact == StructureFlags.PAIRED_CONTACT ) {
									
									StemMotif h = mo2.getHelixByBase( secondBase );
									if ( h!= null ) tmo.helices.add( h );
								
								}
								
								// tertiary motif : 2 secondary motifs ( helix-helix, helix-smotif, smotif-smotif)
								if ( tmo.helices.size() + tmo.smotifs.size() == 2 ) {
									tertiaryStructureMotifs.add( tmo );
									contactAssigned = true;
									break;
								}
								
							} 
							
						}
						
					} 
				
				}
				
			}
			if ( !contactAssigned ) unassignedContacts.add( bc );
			
		}		
		
		for ( TertiaryStructureMotif tmo : tertiaryStructureMotifs ) tmo.showme();
		
	}
	
	
	public void extractLoopMotifs() {
		
		for ( SecondaryStructureMotif ssm : secondaryStructureMotifs ) { ringmotifs.add( ssm.ring ); }
		
		for ( RingMotif ring : ringmotifs ) {
			
			chainmotifs.addAll( ring.nonflanked );
			flankedchainmotifs.addAll( ring.flanked );
			
		}
		
	}
	
}
