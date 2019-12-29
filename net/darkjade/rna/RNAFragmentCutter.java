package net.darkjade.rna;
import java.util.ArrayList;

public class RNAFragmentCutter {

	public static void main( String[] args ) {
		
		System.out.println( "This is RNAFragmentCutter 05-13-08..." );
		
		if ( args.length >= 1 ) {
			String fname = args[ 0 ];
		
			String id = fname.substring( fname.length() - 12 );  //old format was 10, now 12
			
			System.out.println();
			System.out.println( "Processing " + id + "... " );
		
			ArrayList < BaseContact > basepairs = Reader.readBasePairAnnotation( fname + ".out" );
			ArrayList < Base > bases = Reader.readBases( fname );		
			
			
			if ( args.length >= 1 ) {
				
				AnnotatedStructureGraph g = 
					new AnnotatedStructureGraph( bases, basepairs, id );
				
				g.setInfoStrings( Reader.readInformation ( fname ) );
				
				g.startWorking();				
		
				StructureGraphWriter.writeAll( fname, g ); 
				StructureGraphWriter2.writeAll( fname, g );
				
			} 		
			System.out.println( "FINISHED" ); 
		}
		
		
		
		
		
	}
	
}
