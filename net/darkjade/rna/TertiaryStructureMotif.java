package net.darkjade.rna;
import java.util.ArrayList;
import java.util.Collections;



class TertiaryStructureMotif {
	
	ArrayList < StemMotif > helices;
	ArrayList < SecondaryStructureMotif > smotifs;
	ArrayList < BaseContact > intraContacts;
	ArrayList < RingMotif > rings;
	
	TertiaryStructureMotif() {
		
		helices = new ArrayList < StemMotif > ();
		smotifs = new ArrayList < SecondaryStructureMotif > ();
		intraContacts = new ArrayList < BaseContact > ();
		rings = new ArrayList<RingMotif> ();
	}
	
	public void showme() {
		
		/*if ( helices.size() == 0 ) System.out.println( "Loop-Loop Motif" );
		else if ( smotifs.size() == 0 ) System.out.println( "Stem-Stem Motif" );
		else System.out.println( "Stem-Loop Motif" );*/
		
	}
	
	public boolean includesContact( BaseContact bc ) {
		
		int nBasesIncluded = 0;
		for ( StemMotif h : helices ) {
			if ( h.includesBase( bc.getIndex_base1() ) != StructureFlags.NO_CONTACT ) ++nBasesIncluded; 
			if ( h.includesBase( bc.getIndex_base2() ) != StructureFlags.NO_CONTACT ) ++nBasesIncluded;
		}
		for ( SecondaryStructureMotif ssm : smotifs ) { 
			if ( ssm.includesBase( bc.getIndex_base1() ) != StructureFlags.NO_CONTACT ) ++nBasesIncluded;
			if ( ssm.includesBase( bc.getIndex_base2() ) != StructureFlags.NO_CONTACT ) ++nBasesIncluded;			
		}
		for ( RingMotif r : rings ) {
			if ( r.includesBase( bc.getIndex_base1() ) != StructureFlags.NO_CONTACT ) ++nBasesIncluded;
			if ( r.includesBase( bc.getIndex_base2() ) != StructureFlags.NO_CONTACT ) ++nBasesIncluded;			
		}
			
		return nBasesIncluded >= 2; // ==2;
		
		
	}
	
}

class TertiaryStructureMotif2 {
	
	ArrayList < StemMotif > helices;
	ArrayList < RingMotif > rmotifs;
	ArrayList < BaseContact > intraContacts;
	TertiaryStructureMotif2() {
		
		helices = new ArrayList < StemMotif > ();
		rmotifs = new ArrayList < RingMotif > ();
		intraContacts = new ArrayList < BaseContact > ();
	}
	
	public void showme() {
		
		/*if ( helices.size() == 0 ) System.out.println( "Loop-Loop Motif" );
		else if ( smotifs.size() == 0 ) System.out.println( "Stem-Stem Motif" );
		else System.out.println( "Stem-Loop Motif" );*/
		
	}
	
	public boolean includesContact( BaseContact bc ) {
		
		int nBasesIncluded = StructureFlags.NO_CONTACT;
		for ( StemMotif h : helices ) {
			if ( h.includesBase( bc.getIndex_base1() ) != 0 ) ++nBasesIncluded; 
			if ( h.includesBase( bc.getIndex_base2() ) != 0 ) ++nBasesIncluded;
		}
		for ( RingMotif ring : rmotifs ) { 
			if ( ring.includesBase( bc.getIndex_base1() ) != 0 ) ++nBasesIncluded;
			if ( ring.includesBase( bc.getIndex_base2() ) != 0 ) ++nBasesIncluded;			
		}
			
		return nBasesIncluded >= 2; // ==2;
		
		
	}
	
}

class PseudoknotMotif {
	
	private ArrayList < StemMotif > helices;
	private ArrayList < Integer > chains;
	
	//public PseudoknotMotif() { helices = new ArrayList < Helix > (); }
	
	public PseudoknotMotif( StemMotif h, StemMotif ch ) {
		
		helices = new ArrayList < StemMotif > ();
		helices.add( h );
		helices.add( ch );
		
		chains = new ArrayList < Integer > ();
		chains.add( h.b1 );
		//chains.add( h.b3 );
		//chains.add( ch.b1 );
		//chains.add( ch.b3 );
		//chains.add( h.b4 );
		chains.add( h.b2 );
		chains.add( ch.b4 );
		chains.add( ch.b2 );
		Collections.sort( chains );
		
		
	}
	
	public ArrayList < Integer > getChainIndices() { return chains; }
	public ArrayList < StemMotif > getHelices() { return helices; }
	
	public ArrayList < Integer > getChainIndices2() {
	
		ArrayList < Integer > chains = new ArrayList < Integer > ();
		
		if ( helices.size() == 2 ) {
		
			chains.add( helices.get( 0 ).b1 );
			chains.add( helices.get( 0 ).b3 );
			chains.add( helices.get( 1 ).b1 );
			chains.add( helices.get( 1 ).b3 );
			chains.add( helices.get( 0 ).b4 );
			chains.add( helices.get( 0 ).b2 );
			chains.add( helices.get( 1 ).b4 );
			chains.add( helices.get( 1 ).b2 );			
			
		}
		
		
		return chains;
	}
		
}
