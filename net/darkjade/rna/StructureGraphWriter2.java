package net.darkjade.rna;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class StructureGraphWriter2 {
	
	
	//
	public final static String fpdbver = "RNA FPDB v0.05132008";
	
	//
	private final static String[] ftypes = { "FULL STRUCTURE", "BLA" };
	
	//
	private static void writeFPDB( PrintWriter fout, 
			   String fragid, 
			   String type,
			   ArrayList < int[] > subfragments,
			   String pstr,
			   String sstr,
			   ArrayList < BaseContact > original_contacts,
			   int nsecstr,
			   int ntstr,
			   ArrayList < StemMotif > stems,
			   ArrayList < Base > coords,
			   ArrayList < String > info,
			   ArrayList < Base > anchors ) {

		int ct, i, j;
		int[] arr;
		

		Iterator < String > sit = info.iterator();
		Iterator < int[] > iit = subfragments.iterator();
		Iterator < StemMotif > hit = stems.iterator();
		Iterator < BaseContact > bit = original_contacts.iterator();

		ArrayList< BaseContact > contacts = new ArrayList < BaseContact > ();
		while ( bit.hasNext() ) contacts.add( new BaseContact( bit.next() ) );
		
		
		
		
		fout.println( "HEADER " + fragid + " " + fpdbver );

		i = 1;
		j = 1;
		while ( sit.hasNext() ) {			

			String s = sit.next();
			if ( s.startsWith( "PDBH") ) { 
				fout.printf( String.format( "%-6s   %2d %-59s%n", 
						"TITLE", i++, s.substring( 11 ) ) ); 
			} else {
				fout.printf( String.format( "%-6s  %2d %-59s%n", 
						"COMPND", j++,  s.substring( 11 ) ) );
			}	

		}	

		fout.println( "REMARK 300 FRAGMENT TYPE: " + type );

		fout.println( "REMARK 300 NUMBER OF SUBFRAGMENTS: " + subfragments.size() );
		//Collections.sort( subfragments );
		i = 1;
		while ( iit.hasNext() ) {
			
			arr = iit.next();
			fout.printf( String.format( "REMARK 300 SUBFRAGMENT   %4d %4d %4d%n", i++, arr[ 0 ], arr[ 1 ] ) );			
		}

		fout.println( "REMARK 300 NUMBER OF STEMS/ANCHORS: " + stems.size() );
		i = 1;

		while ( hit.hasNext() ) {
			StemMotif st = hit.next(); 
			fout.printf( String.format( "REMARK 300 STEM %4d %4d %4d %4d %4d%n", i++, st.b1, st.b2, st.b3, st.b4 ) );			
		}

		i = 1;
		for ( Base b : coords ) { 			
			if ( b.isModified() )
				fout.printf( String.format( "REMARK 300 MODIFIED BASE %4d %3s %c%n", 
						i, b.getName(), b.getBasecode() ) );
			++i;
		}

		// DISTANCE COMPUTATION FOR INDEXING
		
		if( type.equals( "HELIX" ) || type.equals( "HAIRPIN" ) || type.equals( "RING" ) || type.endsWith( "UNPAIRED" ) ) { 
			AtomCoords O5_ATOM = null, C5_ATOM = null, O3_ATOM = null, C3_ATOM = null;
			for ( AtomCoords co : coords.get( 0 ).getBackboneAtoms() ) {
			
				if ( co.type.startsWith( "C5" ) ) C5_ATOM = co;
				else if ( co.type.startsWith( "O5" ) ) O5_ATOM = co;			
			
			}
			for ( AtomCoords co : coords.get( coords.size() - 1 ).getBackboneAtoms() ) {
				if ( co.type.startsWith( "O3" ) ) O3_ATOM = co;
				else if ( co.type.startsWith( "C3" ) ) C3_ATOM = co;
			}  
		
			if ( O5_ATOM != null && O3_ATOM != null && C5_ATOM != null && C3_ATOM != null ) {			
				fout.printf( String.format( "REMARK 300 STEMWIDTH %8.3f%n", vec3.subtract( O3_ATOM.coords, O5_ATOM.coords ).length() ) );		
				fout.printf( String.format( "REMARK 300 BETA ANGLE  (RAD) %8.3f%n", 
						  	 Math.cos( vec3.dot( vec3.subtract( C3_ATOM.coords, C5_ATOM.coords ), vec3.subtract( C3_ATOM.coords, O3_ATOM.coords ) ) ) ) ); 
				fout.printf( String.format( "REMARK 300 GAMMA ANGLE (RAD) %8.3f%n", 
							 RNAngle.compute_dihedral( O5_ATOM.coords, C5_ATOM.coords, C3_ATOM.coords, O3_ATOM.coords)) );
				
			}
		}
		
		
		
		if ( anchors.size() > 0 ) fout.println( "REMARK 300 IOCOORDS FOR COMPUTING ANGLES OF ADJACENT BASES");
		i = 1;
		for ( Base b : anchors ) {			
			for ( AtomCoords co : b.getBackboneAtoms() ) {				
				fout.printf( String.format( "REMARK 300 IOCOORDS:   %4d %c %3s %8.3f %8.3f %8.3f%n", 
						i++, b.getBasecode(), co.type, co.x, co.y, co.z ) );
			} 			
		}

		fout.printf( String.format( "REMARK 300 PRIMARY STRUCTURE (%d bases):%n", pstr.length() - ( subfragments.size() - 1 ) ) );		
		i = 0;
		while ( i < pstr.length() ) {
			j = i + Math.min( 50, pstr.length() - i );
			fout.printf( String.format( "REMARK 300 PSTR   %-50s%n", pstr.substring( i, j ) ) );
			i += j;
		}

		fout.printf( String.format( "REMARK 300 SECONDARY STRUCTURE:%n" ) );
		i = 0;
		while ( i < sstr.length() ) {
			j = i + Math.min( 50, sstr.length() - i );
			fout.printf( String.format( "REMARK 300 SSTR   %-50s%n", sstr.substring( i, j ) ) );
			i += j; 
		}		

		/* APPLY OFFSET TO CONTACT INDICES */
		
		int max_ix = 0;
		iit = subfragments.iterator();
		
		while ( iit.hasNext() ) {
			
			arr = iit.next();
			int max = Math.max( arr[ 0 ], arr[ 1 ] ); 
			if ( max > max_ix ) max_ix = max;			
			
		}		
		
		int[] offsets = new int[ max_ix + subfragments.size() ];
		int[] offsets_sep = new int[ ( subfragments.size() ) - 1 ];
		iit = subfragments.iterator();
		int start, end, offset;		

		arr    = iit.next();
		start  = arr[ 0 ];
		end    = arr[ 1 ];
		offset = -start;		
		for ( i = start; i <= end; ++i ) offsets[ i ] = offset;
		if ( offsets_sep.length > 0 ) offsets_sep[ 0 ] = -offsets[ end ] + 1;
		j = 0;
		
		while ( iit.hasNext() ) {
			
			arr = iit.next();
			start  = arr[ 0 ];
			offset -= ( start - ( end + 1 ) ) - 1;
			end    = arr[ 1 ];
			for ( i = start; i <= end; ++i ) offsets[ i ] = offset;
			if ( offsets_sep.length > 0 ) offsets_sep[ j++ ] = -offsets[ end ] + 1;
			
		}
			
		for ( BaseContact bc : contacts ) {
			
			if ( bc.getIndex_base1() >= offsets.length || bc.getIndex_base2() >= offsets.length )
				System.out.println( bc.getIndex_base1() + " " + bc.getIndex_base2() + " " + offsets.length );
			//System.out.println( "PRENORM : " + bc.toString(false) );
			bc.normaliseBasePositions( offsets[ bc.getIndex_base1() ], offsets[ bc.getIndex_base2() ] );
			//System.out.println( "POSTNORM: " + bc.toString(false) );
			
		}			
		
		/* */
		
		
		
		fout.printf( String.format( "REMARK 300 TERTIARY STRUCTURE:%n" ) );
		
		for ( BaseContact bc : contacts ) {

			if ( !bc.isCanonical() || bc.isCrossing() ) {				
				//String tstr = bc.createTString( pstr, subfragments );				
				String tstr = bc.createTString2( pstr, offsets_sep );
				i = 0;
				while ( i < tstr.length() ) {
					j = i + Math.min( 50, tstr.length() - i );
					fout.printf( String.format( "REMARK 300 TSTR   %-50s%n", tstr.substring( i, j ) ) );
					i += j; 
				}				
			} 			
		}
		
		
		
		fout.printf( String.format( "REMARK 300 CONTACTS (%d)%n", contacts.size() ) );
		i = 1;
		for ( BaseContact bc : contacts ) {			
			fout.printf( String.format( "REMARK 300 CONTACT %4d %s%n", i++, bc.toString(true) ) );
		}

		ArrayList < String > CONECT_RECORDS = new ArrayList < String > ();

		i = 1; j = 1;
		String cotype;
		for ( Base b : coords ) {

			int P = -1, OP1 = -1, OP2 = -1;

			for ( AtomCoords co : b.getBackboneAtoms() ) {
				fout.printf( 
						String.format( "%-6s%5d %4s%c%3s %c%4d%c   %8.3f%8.3f%8.3f%6.2f%6.2f          %-2s%-2s%n",
								"ATOM", i, co.type, ' ', b.getBasecode(), ' ', j, ' ', co.x, co.y, co.z, 0.0, 0.0, "", "" ) ); 

				cotype = co.type.trim();

				if ( cotype.equals( "P" ) ) P = i++;				 
				else ++i;

			}

			for ( AtomCoords co : b.getNonBackboneAtoms() ) {
				fout.printf( 
						String.format( "%-6s%5d %4s%c%3s %c%4d%c   %8.3f%8.3f%8.3f%6.2f%6.2f          %-2s%-2s%n",
								"ATOM", i, co.type, ' ', b.getBasecode(), ' ', j, ' ', co.x, co.y, co.z, 0.0, 0.0, "", "" ) );

				cotype = co.type.trim();

				if ( cotype.equals( "OP2" ) ) OP2 = i++;
				else if ( cotype.equals( "OP1" ) ) OP1 = i++;
				else ++i;			

			}

			if ( P != -1 ) {

				String CONECT_P = String.format( "CONECT%5d", P );
				int l = CONECT_P.length();

				if ( OP1 != -1 ) 
					CONECT_P += String.format( "%5d", OP1 );
				if ( OP2 != -1 ) 
					CONECT_P += String.format( "%5d", OP2 );

				if ( CONECT_P.length() > l ) CONECT_RECORDS.add( CONECT_P );
			}

			++j;
		}

		sit = CONECT_RECORDS.iterator();
		while ( sit.hasNext() ) fout.println( sit.next() );

	}
	
	//
	private static void writeGraph( String fname, StructureGraph g ) {
		
		try {
			
			PrintWriter fout; 
							
			ArrayList < int[] > chains = new ArrayList < int[] > ();
			int[] arr = { 0, g.getVertices().size() - 1 };
			chains.add( arr );
			/*chains.add( 0 ); 
			chains.add( g.getVertices().size() - 1 );*/
			ArrayList < BaseContact > contacts = new ArrayList < BaseContact > ();
			contacts.addAll( g.getSecondaryContacts() );
			contacts.addAll( g.getAllTertiaryContacts() );			
			
			fout = new PrintWriter( new BufferedWriter( new FileWriter( fname + ".fpdb" ) ) );
			writeFPDB( 
					fout, g.getIdString(), "FULL STRUCTURE", chains, 
					g.getSequence(), g.getSecondaryStructureBracket(), contacts, 
					g.getSecondaryContacts().size(), g.getAllTertiaryContacts().size(), 
					g.getHelices(), g.getVertices(), g.getInfoStrings(), new ArrayList < Base > () );
			
			fout.close();
			
			
		} catch ( IOException e ) { e.printStackTrace(); }
		
	}
	
	public static void writeHelix( String fname, StructureGraph g, StemMotif h, String index ) {
		
		try {
			
			PrintWriter fout = 
				new PrintWriter( new BufferedWriter( new FileWriter( fname + ".H" + index + ".fpdb" ) ) );
			
			ArrayList < int[] > chains = h.getChainIndices();
			ArrayList < BaseContact >contacts = new ArrayList < BaseContact > ();
			contacts.addAll( h.basePairs );
			contacts.addAll( h.noncanonicalContacts );
			
			String strucs = g.getFragmentStructures( chains );
			ArrayList < Base > bases = g.getBaseSubset( chains );			
			
			writeFPDB( fout,
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
				new PrintWriter( new BufferedWriter( new FileWriter( fname + ".S" + index + ".fpdb" ) ) );
			
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
						
			writeFPDB( 	fout, 
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
				new PrintWriter( new BufferedWriter( new FileWriter( fname + identifier + index + ".fpdb" ) ) );			
			
			ArrayList < int[] > chains = chain.getChainIndices();
			String strucs = g.getFragmentStructures( chains );
			
			ArrayList < BaseContact > contacts = new ArrayList < BaseContact > ();			
					
			writeFPDB( 	fout,
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
				new PrintWriter( new BufferedWriter( new FileWriter( fname + ".R" + index + ".fpdb" ) ) );
			
			RingMotif loo = g.getLoopMotifs().get( index );
			ArrayList < int[] > chains = loo.getChainIndices();
			String strucs = g.getFragmentStructures( chains );
			
			ArrayList < BaseContact > contacts = new ArrayList < BaseContact > ();
			/*for ( StemMotif h : loo.getBasepairs() ) {
				contacts.addAll( h.basePairs );				
			}*/
			
					
			writeFPDB( 	fout,
						g.getIdString() + ".R" + index,
						"RING",
						chains,
						strucs.substring( 0, strucs.indexOf( '&' ) ),
						strucs.substring( strucs.indexOf( '&' ) + 1 ),
						loo.getContacts(),
						loo.size(),
						loo.getContacts().size(),
						loo.getBasepairs(),
						g.getBaseSubset( chains ), 
						new ArrayList < String > (),
						g.getAnchorBaseSubset( loo.getAnchors( 1 ) ) );
			fout.close();
			
		} catch ( IOException e) { e.printStackTrace(); }
		
		
	}
	
	public static void writeTertiaryStructureFragment( String fname, StructureGraph g, int index ) {
		
		try {
			
			System.out.println( index );
			
			PrintWriter fout =
				new PrintWriter( new BufferedWriter( new FileWriter( fname + ".T" + index + ".fpdb" ) ) );
			
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
						
			writeFPDB( fout, 
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
	
	
	
	//
	public static void writeAll( String	fname, StructureGraph g ) {
		
		int ct = 0;
		
		System.out.println( "Writing Graph...");
		writeGraph( fname, g );
		
		System.out.println( "Writing Stems...");
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
		System.out.println( "Writing SSMs...");
		for ( SecondaryStructureMotif s : g.getSecondaryStructureMotifs() ) 
			writeSecondaryStructureFragment( fname, g, ct++ ); 
		ct = 0;
		System.out.println( "Writing TSMs...");
		for ( TertiaryStructureMotif t : g.getTertiaryStructureMotifs() )
			writeTertiaryStructureFragment( fname , g, ct++ ); 		
		ct = 0;
		System.out.println( "Writing Rings...");
		for ( RingMotif loo : g.getLoopmotifs() )
			writeRingMotif( fname, g, ct++ );
		ct = 0;
		System.out.println( "Writing Chains...");
		for ( ChainMotif chain : g.getChainmotifs() ) 
			writeChainMotif( fname, g, chain, ct++ );
		ct = 0;
		for ( ChainMotif chain : g.getFlankedchainmotifs() )
			writeChainMotif( fname, g, chain, ct++ );
		
	}
	

}
