package net.darkjade.rna;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;


public class StructureGraphWriter {
	
	public final static String version = "#RNA FRAGMENT FILE vX.05052008";
	public final static String fpdbver = "RNA FPDB v0.05052008";
	
	public static void writeAll( String	fname, StructureGraph g ) {
		
		int ct = 0;
		
		writeGraph( fname, g );		
		
		for ( StemMotif h : g.getHelices() ) { 
			writeHelix( fname, g, h, Integer.toString( ct ) );
			ArrayList < StemMotif > cuts = h.getAllCuts( 5 );
			int ct2 = 0;
			for ( StemMotif ch : cuts ) {
				writeHelix( fname, g, ch, Integer.toString( ct ) + "-" + Integer.toString( ct2++ ) );				
			}
			++ct;
		}		
		
		ct = 0;
		for ( SecondaryStructureMotif s : g.getSecondaryStructureMotifs() ) 
			writeSecondaryStructureFragment( fname, g, ct++ );
		ct = 0;
		for ( TertiaryStructureMotif t : g.getTertiaryStructureMotifs() )
			writeTertiaryStructureFragment( fname , g, ct++ );		
		ct = 0;
		for ( RingMotif loo : g.getLoopmotifs() )
			writeRingMotif( fname, g, ct++ );
		ct = 0;
		for ( ChainMotif chain : g.getChainmotifs() ) 
			writeChainMotif( fname, g, chain, ct++ );
		ct = 0;
		for ( ChainMotif chain : g.getFlankedchainmotifs() )
			writeChainMotif( fname, g, chain, ct++ );	
		
	}	
	
	

	public static void writeGraph( String fname, StructureGraph g ) {
		
		try {
			
			PrintWriter fout = 
				new PrintWriter( new BufferedWriter( new FileWriter( fname + ".graph" ) ) );
			
			ArrayList < int[] > chains = new ArrayList < int[] > ();
			int[] ch = { 0, g.getVertices().size() - 1 };
			chains.add( ch ); 
			//chains.add( g.getVertices().size() - 1 );
			ArrayList < BaseContact > contacts = new ArrayList < BaseContact > ();
			contacts.addAll( g.getSecondaryContacts() );
			contacts.addAll( g.getAllTertiaryContacts() );
			
			writeData( fout, 
					   g.getIdString(), 
					   "FULL STRUCTURE", 
					   chains, 
					   g.getSequence(), 
					   g.getSecondaryStructureBracket(),
					   contacts,
					   g.getSecondaryContacts().size(),
					   g.getAllTertiaryContacts().size(),
					   g.getHelices(),
					   g.getVertices(),
					   g.getInfoStrings(),
					   new ArrayList < Base > () );
			
			fout.close();
			
		} catch ( IOException e ) { e.printStackTrace(); }
		
	}
	
	public static void writeHelix( String fname, StructureGraph g, StemMotif h, String index ) {
		
		try {
			
			PrintWriter fout = 
				new PrintWriter( new BufferedWriter( new FileWriter( fname + ".H" + index + ".fragment" ) ) );
			
			ArrayList < int[] > chains = h.getChainIndices();
			ArrayList < BaseContact >contacts = new ArrayList < BaseContact > ();
			contacts.addAll( h.basePairs );
			contacts.addAll( h.noncanonicalContacts );
			
			String strucs = g.getFragmentStructures( chains );
			ArrayList < Base > bases = g.getBaseSubset( chains );			
			
			writeData( fout,
					   g.getIdString() + ".H" + index,
					   "HELIX",
					   chains,
					   strucs.substring( 0, strucs.indexOf( '&' ) ),
					   strucs.substring( strucs.indexOf( '&' ) + 1 ),		
					   contacts,
					   h.basePairs.size(),
					   h.noncanonicalContacts.size(),
					   new ArrayList < StemMotif >(),
					   bases,
					   new ArrayList < String > (),
					   g.getAnchorBaseSubset( h.getAnchors( 1 ) ) );
			
			fout.close();
			
		} catch ( IOException e) { e.printStackTrace(); }
		
	}
	
	
	
	public static void writeSecondaryStructureFragment( String fname, StructureGraph g, int index ) {
		
		try {
			
			PrintWriter fout =
				new PrintWriter( new BufferedWriter( new FileWriter( fname + ".S" + index + ".fragment" ) ) );
			
			SecondaryStructureMotif motif = g.getSecondaryStructureMotifs().get( index );
			ArrayList < int[] > chains = motif.getChainIndices();
			ArrayList < BaseContact > contacts = new ArrayList < BaseContact > ();
			contacts.addAll( motif.motherStem.basePairs );
			contacts.addAll( motif.motherStem.noncanonicalContacts );		
			
			ArrayList < StemMotif > helices = new ArrayList < StemMotif > ();
			helices.add( motif.motherStem );
			helices.addAll( motif.childStems );
			
			int nsstr = motif.motherStem.basePairs.size();
			int ntstr = motif.motherStem.noncanonicalContacts.size();
			for ( StemMotif h : motif.childStems ) { 
				nsstr += h.basePairs.size();
				ntstr += h.noncanonicalContacts.size();
				contacts.addAll( h.basePairs );
				contacts.addAll( h.noncanonicalContacts );
			}
			
			contacts.addAll( motif.intraTertiaryContacts );
			
			String strucs = g.getFragmentStructures( chains );
			ArrayList < Base > bases = g.getBaseSubset( chains );	
						
			writeData( 	fout, 
					   	g.getIdString() + ".S" + index, 
					   	motif.getType(), 
					   	chains, 
					   	strucs.substring( 0, strucs.indexOf( '&' ) ),
					   	strucs.substring( strucs.indexOf( '&' ) + 1 ),					   
					   	contacts, 
					   	nsstr, 
					   	motif.intraTertiaryContacts.size() + ntstr, 
					   	helices,
					   	bases,
					   	new ArrayList < String > (),
					   	new ArrayList < Base > () );
			
			fout.close();
			
		} catch ( IOException e) { e.printStackTrace(); }
		
	}
	
	private static void writeChainMotif( String fname, StructureGraph g, ChainMotif chain, int index ) {		
		
		try {
			
			String identifier = ( chain.is_flanked ) ? ".F" : ".U";
			String longid     = ( chain.is_flanked ) ? "FLANKED UNPAIRED" : "UNPAIRED";
			
			PrintWriter fout =		
				new PrintWriter( new BufferedWriter( new FileWriter( fname + identifier + index + ".fragment" ) ) );			
			
			ArrayList < int[] > chains = chain.getChainIndices();
			String strucs = g.getFragmentStructures( chains );
			
			ArrayList < BaseContact > contacts = new ArrayList < BaseContact > ();			
					
			writeData( 	fout,
						g.getIdString() + identifier + index,
						longid,
						chains,
						strucs.substring( 0, strucs.indexOf( '&' ) ),
						strucs.substring( strucs.indexOf( '&' ) + 1 ),
						contacts,
						chain.size(),
						chain.getContacts().size(),
						new ArrayList < StemMotif > (),
						g.getBaseSubset( chains ), 
						new ArrayList < String > (),
						g.getAnchorBaseSubset( chain.getAnchors( 1 ) ) );
			fout.close();
			
		} catch ( IOException e ) { e.printStackTrace(); }
		
	}
	
	public static void writeRingMotif( String fname, StructureGraph g, int index ) {
		
		try {
			PrintWriter fout =		
				new PrintWriter( new BufferedWriter( new FileWriter( fname + ".R" + index + ".fragment" ) ) );
			
			RingMotif loo = g.getLoopMotifs().get( index );
			ArrayList < int[] > chains = loo.getChainIndices();
			String strucs = g.getFragmentStructures( chains );
			
			ArrayList < BaseContact > contacts = new ArrayList < BaseContact > ();
			/*for ( StemMotif h : loo.getBasepairs() ) {
				contacts.addAll( h.basePairs );				
			}*/
			
					
			writeData( 	fout,
						g.getIdString() + ".R" + index,
						"RING",
						chains,
						strucs.substring( 0, strucs.indexOf( '&' ) ),
						strucs.substring( strucs.indexOf( '&' ) + 1 ),
						loo.getContacts(),
						loo.size(),
						loo.getContacts().size(),
						new ArrayList < StemMotif > (),
						g.getBaseSubset( chains ), 
						new ArrayList < String > (),
						g.getAnchorBaseSubset( loo.getAnchors( 1 ) ) );
			fout.close();
			
		} catch ( IOException e) { e.printStackTrace(); }
				
	}
	
	public static void writeTertiaryStructureFragment( String fname, StructureGraph g, int index ) {
		
		try {
			
			PrintWriter fout =
				new PrintWriter( new BufferedWriter( new FileWriter( fname + ".T" + index + ".fragment" ) ) );
			
			TertiaryStructureMotif tmotif = g.getTertiaryStructureMotifs().get( index );

			ArrayList < int[] > chains = new ArrayList < int[] > ();
			ArrayList < BaseContact > contacts = new ArrayList < BaseContact > ();
			ArrayList < StemMotif > helices = new ArrayList < StemMotif > ();
			int nsstr = 0, ntstr = 0;
			
			String type = "";
			switch( tmotif.smotifs.size() ) {
			
				case 0 : type = "HELIX-HELIX"; break;
				case 1 : type = tmotif.smotifs.get( 0 ).getType() + "-HELIX"; break;
				case 2 : 
					type = tmotif.smotifs.get( 0 ).getType() + "-" + tmotif.smotifs.get( 1 ).getType();
					break;
				default : type = "CANTBE!"; 
			
			}
			
			for ( SecondaryStructureMotif motif : tmotif.smotifs ) {
				chains.addAll( motif.getChainIndices() );
				contacts.addAll( motif.motherStem.basePairs );
				contacts.addAll( motif.motherStem.noncanonicalContacts );				
				helices.add( motif.motherStem );
				helices.addAll( motif.childStems );
				nsstr += motif.motherStem.basePairs.size();
				ntstr += motif.motherStem.noncanonicalContacts.size();
				
				for ( StemMotif h : motif.childStems ) {
					nsstr += h.basePairs.size();
					ntstr += h.noncanonicalContacts.size();
					contacts.addAll( h.basePairs );
					contacts.addAll( h.noncanonicalContacts );					
				}
			}
			for ( StemMotif h : tmotif.helices ) {
				chains.addAll( h.getChainIndices() );
				contacts.addAll( h.basePairs );
				contacts.addAll( h.noncanonicalContacts );
				helices.add( h );
				nsstr += h.basePairs.size();
				ntstr += h.noncanonicalContacts.size();
			}
			for ( RingMotif ring : tmotif.rings ) {
				chains.addAll( ring.getChainIndices() );
				contacts.addAll( ring.getContacts() );				
			}
			
			contacts.addAll( tmotif.intraContacts );
						
			String strucs = g.getFragmentStructures( chains );
			ArrayList < Base > bases = g.getBaseSubset( chains );	
						
			writeData( fout, 
					   g.getIdString() + ".T" + index, 
					   "TERTIARY BIMOTIF: " + type, 
					   chains, 
					   strucs.substring( 0, strucs.indexOf( '&' ) ),
					   strucs.substring( strucs.indexOf( '&' ) + 1 ),					   
					   contacts, 
					   nsstr, 
					   tmotif.intraContacts.size() + ntstr, 
					   helices, 
					   bases,
					   new ArrayList < String > (),
					   new ArrayList < Base > () );
			
			fout.close();
			
		} catch ( IOException e ) { e.printStackTrace(); }
		
		
	}
	
	private static void writeData( PrintWriter fout, 
							 String fragid, 
							 String type,
							 ArrayList < int[] > chains,
							 String pstr,
							 String sstr,
							 ArrayList < BaseContact > contacts,
							 int nsecstr, int ntstr,
							 ArrayList < StemMotif > helices,
							 ArrayList < Base > coords,
							 ArrayList < String > info,
							 ArrayList < Base > anchors ) {
		 
		int ct = 0;
		Iterator < int[] > it = chains.iterator();
		fout.println( version );
		fout.println( "FRAGID     " + fragid );
		
		Iterator < String > sit = info.iterator();
		while ( sit.hasNext() ) {			
			fout.println( sit.next() );
		}		
		
		fout.println( "TYPE       " + type );
		fout.println( "NCHAINS    " + chains.size() );
		while ( it.hasNext() ) {
			
			int[] arr = it.next();
			fout.println( "CHAIN      " + (ct++) + " " + arr[ 0 ] + " " + arr[ 1 ] ); 
			
		}
		
		fout.println( "PSTRUCTURE " + pstr );
		fout.println( "SSTRUCTURE " + sstr );
		
		for ( BaseContact bc : contacts ) {
			
			if ( !bc.isCanonical() || bc.isCrossing() ) {
				
				String tstr = bc.createTString( pstr, chains );
				fout.println( "TSTRUCTURE " + tstr );
				
			}
			
		}
		
		fout.println( "NCONTACTS  " + nsecstr + " " + ntstr );
		ct = 0;
		for ( BaseContact bc : contacts ) {
			
			fout.println( "CONTACT    " + (ct++) + " " + bc.toString(false) ); 
			
		}
		ct = 0;		
		for ( StemMotif h : helices ) {
			
			fout.println( "STEM      " + (ct++) + " " + h.toString() );
			
		}
		
		ct = 0;
		for ( Base b : anchors ) {
			
			for ( AtomCoords co : b.getBackboneAtoms() ) {
				
				fout.println( "ANCHOR     " + (ct++) + " " + b.getBasecode() + " " 
						+ co.type + " " + co.x + " " + co.y + " " + co.z );
				
			}
			
		}
		
		ct = 0;
		for ( Base b : coords ) {
			if ( b.isModified() ) {
				fout.println( "MODBASE    " + ct + " " + b.getName() + " " + b.getBasecode() );				
			}
			++ct;
		}
		
		ct = 0;
		for ( Base b : coords ) {
			
			for ( AtomCoords co : b.getBackboneAtoms() )
				fout.println( "COORD      " + ct + " " 
						+ b.getBasecode() + " " 
						+ co.type + " " + co.x + " " + co.y + " " + co.z );
			
			for ( AtomCoords co : b.getNonBackboneAtoms() )
				fout.println( "RCOORD     " + ct + " " 
						+ b.getBasecode() + " " 
						+ co.type + " " + co.x + " " + co.y + " " + co.z );
						
			++ct;
		}
		
	}

	

}
