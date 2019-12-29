package net.darkjade.rna;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

class FragmentLine {
	
	private String id;
	private String content;
	
	public FragmentLine( String id, String content ) {
		
		this.id = id;
		this.content = content;
		
	}

	public String getContent() {
		return content;
	}

	public String getId() {
		return id;
	}
	
}


/*
PDB data file name: 1A1T00011B
BEGIN_base-pair
     1_20, B:   201 G-C   220 B: +/+ cis         XIX
     2_19, B:   202 G-C   219 B: +/+ cis         XIX
      3_4, B:   203 A-C   204 B:      stacked
     3_18, B:   203 A-U   218 B: -/- cis         XX
     4_17, B:   204 C-G   217 B: +/+ cis         XIX
     5_16, B:   205 U-A   216 B: -/- cis         XX
     5_17, B:   205 U-G   217 B: W/W cis         XXVIII
     6_15, B:   206 A-U   215 B: -/- cis         XX
     7_14, B:   207 G-C   214 B: +/+ cis         XIX
     8_13, B:   208 C-G   213 B: +/+ cis         XIX
END_base-pair
  The total base pairs =   9 (from   20 bases)
------------------------------------------------
 Standard  WW--cis  WW-tran  HH--cis  HH-tran  SS--cis  SS-tran
        8        1        0        0        0        0        0
  WH--cis  WH-tran  WS--cis  WS-tran  HS--cis  HS-tran
        0        0        0        0        0        0
------------------------------------------------

PDB data file name: 1A9L000100
BEGIN_base-pair
     1_38,  :     1 G-C    38  : +/+ cis         XIX
     2_37,  :     2 G-C    37  : +/+ cis         XIX
     3_36,  :     3 G-C    36  : +/+ cis         XIX
     4_35,  :     4 U-A    35  : -/- cis         XX
     5_34,  :     5 G-C    34  : +/+ cis         XIX
     6_33,  :     6 A-U    33  : -/- cis         XX
     7_29,  :     7 C-G    29  : +/+ cis         XIX
     8_28,  :     8 U-A    28  : -/- cis         XX
     9_27,  :     9 C-G    27  : +/+ cis         XIX
    10_26,  :    10 C-G    26  : +/+ cis         XIX
    14_25,  :    14 G-C    25  : +/+ cis         XIX
    15_24,  :    15 G-C    24  : +/+ cis         XIX
    16_23,  :    16 U-A    23  : -/- cis         XX
    17_22,  :    17 C-G    22  : +/+ cis         XIX
    18_21,  :    18 G-A    21  :      stacked
    20_21,  :    20 G-A    21  :      stacked
    33_34,  :    33 U-C    34  :      stacked
      6_7,  :     6 A-C     7  : S/S tran        !(s_s)
    11_12,  :    11 A-G    12  : S/S tran        !(s_s)
    14_24,  :    14 G-C    24  : W/W cis         !(s_s)
END_base-pair
  The total base pairs =  14 (from   38 bases)
------------------------------------------------
 Standard  WW--cis  WW-tran  HH--cis  HH-tran  SS--cis  SS-tran
       14        0        0        0        0        0        0
  WH--cis  WH-tran  WS--cis  WS-tran  HS--cis  HS-tran
        0        0        0        0        0        0
------------------------------------------------
*/
public class Reader {
	
	private static BaseDictionary baseDict;
		
	private static BaseContact parseBaseContact( String data ) {
		
		baseDict = BaseDictionary.getInstance();
		
		BaseContact contact = null;
		
		int i1 = 0, i2 = 0, conf = 0x00000;		
		String m1 = "", m2 = "", desc = "";
		char e1 = ' ', e2 = ' ';
		char ic1 = ' '; char ic2 = ' ';	
		
		
		//1_38,  :     1 G-C    38  : +/+ cis         XIX
		//1_20, B:   201 G-C   220 B: +/+ cis         XIX
		//18_21,  :    18 G-A    21  :      stacked
		//3_4, B:   203 A-C   204 B:      stacked
		//1379_1637, 0:  1419 U-A  1678 0: -/- cis    syn    XX
		//1379_1644, 0:  1419 U-A  1685 0: syn    stacked
		int pos = 0;
		
		//new format:    19_22, A:    21L G-A    24L A: S/H tran        XI 
		//             *              
		//1_71, A:     1  U-A    71  A: -/- cis         XX
		String s = data.substring( data.indexOf( ":" ) + 1, data.lastIndexOf( ":" ) - 1 ).trim();
		pos = s.indexOf( " " );
		String sub = s.substring( 0, pos );
		
		// icode exists
		if ( !Character.isDigit( sub.charAt( sub.length() - 1 ) ) ) {
			
			i1 = Integer.parseInt( sub.substring( 0, sub.length() - 1 ) );
			ic1 = sub.charAt( sub.length() - 1 );			
			
		} else {
			
			i1 = Integer.parseInt( sub );
			
		}
		
		s = s.substring( pos + 1 ).trim();
		
		m1 = s.substring( 0, 1 ).toUpperCase();
		m2 = s.substring( 2, 3 ).toUpperCase();
		
		s = s.substring( 3 ).trim();
		
		if ( !Character.isDigit( s.charAt( s.length() - 1 ) ) ) {
			
			i2 = Integer.parseInt( s.substring( 0, s.length() - 1 ) );
			ic2 = s.charAt( s.length() - 1 );
			
		} else {
			
			i2 = Integer.parseInt( s );
			
		}	
		
		s = data.substring( data.lastIndexOf( ":" ) + 1 ).trim();
		/*if ( s.contains( "stacked" ) ) conf |= StructureFlags.CONTACT_STACKED;
		if ( s.contains( "isyn" ) || s.contains( "jsyn" ) ) conf |= StructureFlags.CONTACT_SYN;
		if ( s.contains( "cis" ) ) conf |= StructureFlags.CONTACT_CIS;
		if ( s.contains( "tran" ) ) conf |= StructureFlags.CONTACT_TRANS;
		if ( s.contains( "!" ) ) conf |= StructureFlags.CONTACT_TERTIARY;*/
		conf = StructureFlags.setFlags( s );
				
		if ( s.contains( "/" ) ) {
			
			pos = s.indexOf( "/" );
			e1 = s.charAt( pos - 1 );
			e2 = s.charAt( pos + 1 );
			
		}
		
		pos = s.lastIndexOf( " " );
		if ( pos < s.length() - 1 && ( conf & StructureFlags.CONTACT_STACKED ) == 0 ) 
			desc = s.substring( pos );
		
		contact = new BaseContact( 	i1, i2, ' ', ' ',
									baseDict.getBaseCode( m1.substring( 0, 1 ) ),
									baseDict.getBaseCode( m2.substring( 0, 1 ) ),
									m1, m2, e1, e2, conf, desc, 
									StructureFlags.isSet( conf, StructureFlags.CONTACT_SYN5P ),
									StructureFlags.isSet( conf, StructureFlags.CONTACT_SYN3P ) );	
									//m1.charAt( 0 ), m2.charAt( 0 ), m1, m2, e1, e2, conf, desc, syn1, syn2 );		
		
		return contact;
		
	}
	
	@SuppressWarnings("deprecation")
	public static ArrayList < String > readInformation( String fname ) {
		
		ArrayList < String > info = new ArrayList < String > ();
		File file = new File( fname );
		FileInputStream fis = null;
	    BufferedInputStream bis = null;
	    DataInputStream dis = null;
	    
	    try {
	    	
	    	fis = new FileInputStream( file );
	    	bis = new BufferedInputStream( fis );
	    	dis = new DataInputStream( bis );
	    	
	    	String s;	    	
	    	while ( ( s = dis.readLine() ) != null ) {
	    		
	    		if ( s.startsWith( "HEADER" ) ||  s.startsWith( "TITLE" ) ) {
	    			
	    				String r = "PDBHEAD    " + s.substring( 10, s.length() - 1 ).trim();
	    				info.add( r.trim() );
	    			
	    		} else if ( s.startsWith( "COMPND" ) ) {
	    			
	    			if ( s.indexOf( "MOL_ID:" ) == -1 && s.indexOf( "CHAIN:") == -1 ) {
	    				
	    				String r = "PDBCOMPND  " + s.substring( 10, s.length() - 1 ).trim();
	    				info.add( r.trim() );
	    				
	    			}
	    			
	    		} else if ( s.startsWith( "SOURCE" ) ) {
	    			
	    			if ( s.indexOf( "MOL_ID:" ) == -1 ) { 
	    				
	    				String r = "PDBSOURCE  " + s.substring( 10, s.length() - 1 ).trim(); 
	    				info.add( r.trim() );
	    			}
	    			
	    		}
	    		
	    	}
	    	
	    } catch (FileNotFoundException e) {
	    	e.printStackTrace();	    	
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return info;
		
	}	
	
	@SuppressWarnings("deprecation")
	public static ArrayList < Base > readBases( String fname ) {
		
		baseDict = BaseDictionary.getInstance();
		
		ArrayList < Base > bases = new ArrayList < Base >();
		File file = new File( fname );
		FileInputStream fis = null;
	    BufferedInputStream bis = null;	    
	    BufferedReader dis = null;	    
	    	    
	    int len = 0;
	    int index = -1;
	    char icode = '_';
	    int first_index = -1;
	    Base b = null;
	    int last_index = -1;
	    char first_icode = '_';	    	    
	    char valid_altloc = ' ';
	    
	    try {
	    	
	    	fis = new FileInputStream( file );
	    	bis = new BufferedInputStream( fis );
	    	dis = new BufferedReader( new InputStreamReader( bis ) );
	    	String s;    	
	    	
	    	
	    	while ( ( s = dis.readLine() ) != null ) {	    		
	    		
	    		if ( s.startsWith( "ATOM" ) || s.startsWith( "HETATM" ) ) {
	    			
	    			int ix = Integer.parseInt( s.substring( 22, 26 ).trim() );
	    			char ic = s.charAt( 26 );
	    			String name = s.substring( 17, 20 );	    			
	    			
	    			if ( Character.isLetterOrDigit( s.charAt( 16 ) ) && s.charAt( 16 ) != valid_altloc ) {
	    				
	    				if ( valid_altloc == ' ' )
	    					valid_altloc = s.charAt( 16 );
	    				else 
	    					continue;
	    				
	    			}	    			
	    			
	    			if ( first_index == -1 ) { 
	    				first_index = ix;
	    				first_icode = ic;
	    			}
	    			
	    			
	    			if ( ix != index || ( ix == index && icode != ic ) ) {
	    				
	    				if ( index != -1 ) {
	    					
	    					int d = Math.abs( index - ix );
	    					
	    					if ( d != 1 ) {
	    						System.out.println( "Gap at " + index + " " + ix );
	    						bases.add( new Base( ix - 1, ic, '*', "*" ) );
	    					}   					
	    						    						
	    				}
	    				
	    				index = ix;
	    				icode = ic;
	    				String trName = name.trim();
	    				char base_code = baseDict.getBaseCode( trName );
	    				
	    				if ( base_code != '?' || !baseDict.isAminoAcid( trName ) ) {		
	    					b = new Base( ix, ic, base_code, trName );	    					
	    					bases.add( b ); 
	    				}
	    				
	    			} 
	    			
	    			if ( b != null ) { 
	    			
	    				if ( AtomCoords.isBackboneAtom( s.substring( 12, 16 ).trim() ) ) {
	    			
	    			
	    					b.getBackboneAtoms().add( 
	    							new AtomCoords( s.substring( 12, 16 ).trim(),
	    									Double.parseDouble( s.substring( 30, 38 ) ),
	    									Double.parseDouble( s.substring( 38, 46 ) ),
	    									Double.parseDouble( s.substring( 46, 54 ) ) ) ); 
	    				
	    				} else {
	    					
	    					b.getNonBackboneAtoms().add(
	    							new AtomCoords( s.substring( 12, 16 ).trim(),
	    									Double.parseDouble( s.substring( 30, 38 ) ),
	    									Double.parseDouble( s.substring( 38, 46 ) ),
	    									Double.parseDouble( s.substring( 46, 54 ) ) ) );
	    						    					
	    				}
	    							    			
	    			}
	    			
	    		} else if ( s.startsWith( "TER" ) ) break;	    		
	    	}

	    	dis.close();
	    	bis.close();
	    	fis.close();
	    	
	    } catch (FileNotFoundException e) {
	    	e.printStackTrace();	    	
		} catch (IOException e) {
			e.printStackTrace();
		}	

		return bases;
		
	}
	
	@SuppressWarnings("deprecation")
	public static ArrayList < Base > readBases_old( String fname ) {
		
		baseDict = BaseDictionary.getInstance();
		
		ArrayList < Base > bases = new ArrayList < Base >();
		File file = new File( fname );
		FileInputStream fis = null;
	    BufferedInputStream bis = null;	    
	    BufferedReader dis = null;
	    
	    ArrayList < Integer > map = new ArrayList < Integer > ();
	    ArrayList < String > seq = new ArrayList < String > ();
	    int len = 0;
	    int index = -1;
	    char icode = '_';
	    int first_index = -1;
	    int last_index = -1;
	    char first_icode = '_';
	    Base b = null;
	    
	    char valid_altloc = ' ';
	    
	    try {
	    	
	    	fis = new FileInputStream( file );
	    	bis = new BufferedInputStream( fis );
	    	dis = new BufferedReader( new InputStreamReader( bis ) );
	    	String s;    	
	    	
	    	
	    	while ( ( s = dis.readLine() ) != null ) {
	    		
	    		//System.out.println( s );
	    		
	    		if ( s.startsWith( "SEQRES" ) ) {
	    			
	    			if ( len == 0 ) {
	    			
	    				len = Integer.parseInt( s.substring( 13, 17 ).trim() );
	    				map.ensureCapacity( len ); 
	    					    				
	    			}
	    			
	    			StringTokenizer st = new StringTokenizer( s.substring( 19, 70 ) );
    				while ( st.hasMoreTokens() ) {
    					seq.add( st.nextToken() );
    				}
    				
    				if ( baseDict.isAminoAcid( seq.get( seq.size() - 1 ) ) ) {    					
    					--len;
    				}
    					
 	    			
	    		} else if ( s.startsWith( "ATOM" ) || s.startsWith( "HETATM" ) ) {
	    			
	    			int ix = Integer.parseInt( s.substring( 22, 26 ).trim() );
	    			char ic = s.charAt( 26 );
	    			String name = s.substring( 17, 20 );
	    			
	    			/*if ( s.charAt( 16 ) != ' ' )
	    				if ( valid_altloc == ' ' ) 
	    					valid_altloc = s.charAt( 16 );
	    				else if ( s.charAt( 16 ) != valid_altloc )
	    					continue;*/
	    			
	    			if ( Character.isLetterOrDigit( s.charAt( 16 ) ) && s.charAt( 16 ) != valid_altloc ) {
	    				
	    				if ( valid_altloc == ' ' )
	    					valid_altloc = s.charAt( 16 );
	    				else 
	    					continue;
	    				
	    			}
	    					
	    			
	    			
	    			if ( first_index == -1 ) { 
	    				first_index = ix;
	    				first_icode = ic;
	    			}
	    			
	    			
	    			if ( ix != index || ( ix == index && icode != ic ) ) {
	    				
	    				index = ix;
	    				icode = ic;
	    				String trName = name.trim();
	    				char base_code = baseDict.getBaseCode( trName );
	    				
	    				if ( base_code != '?' || !baseDict.isAminoAcid( trName ) ) {		
	    					b = new Base( ix/* - first_index*/, ic, base_code, trName );	    					
	    					bases.add( b ); 
	    				}
	    				
	    			} 
	    			
	    			if ( b != null ) { 
	    			
	    				if ( AtomCoords.isBackboneAtom( s.substring( 12, 16 ).trim() ) ) {
	    			
	    			
	    					b.getBackboneAtoms().add( 
	    							new AtomCoords( s.substring( 12, 16 ).trim(),
	    									Double.parseDouble( s.substring( 30, 38 ) ),
	    									Double.parseDouble( s.substring( 38, 46 ) ),
	    									Double.parseDouble( s.substring( 46, 54 ) ) ) ); 
	    				
	    				} else {
	    					
	    					b.getNonBackboneAtoms().add(
	    							new AtomCoords( s.substring( 12, 16 ).trim(),
	    									Double.parseDouble( s.substring( 30, 38 ) ),
	    									Double.parseDouble( s.substring( 38, 46 ) ),
	    									Double.parseDouble( s.substring( 46, 54 ) ) ) );
	    						    					
	    				}
	    							    			
	    			}
	    			
	    		} else if ( s.startsWith( "TER" ) ) break;	    		
	    	}
	    	first_index = ( index < first_index ) ? index : first_index;
	    	dis.close();
	    	bis.close();
	    	fis.close();
	    	index = -1;
	    	
	    	if ( bases.size() < len && len != 0 ) {
	    		
	    		bases.clear();
	    		
	    		fis = new FileInputStream( file );
		    	bis = new BufferedInputStream( fis );
	    		dis = new BufferedReader( new InputStreamReader( bis ) );
	    		while ( ( s = dis.readLine() ) != null ) {
		    		
		    		//System.out.println( s ); 
		    		if ( s.startsWith( "ATOM" ) || s.startsWith( "HETATM" ) ) {
		    			
		    			int ix = Integer.parseInt( s.substring( 22, 26 ).trim() );
		    			char ic = s.charAt( 26 );
		    			String name = s.substring( 17, 20 );
		    			
		    			if ( Character.isLetterOrDigit( s.charAt( 16 ) ) && s.charAt( 16 ) != valid_altloc ) {
		    				
		    				//System.out.print( "I found an altLoc: " + s.charAt( 16 ) );
		    				
		    				if ( valid_altloc == ' ' ) {
		    					valid_altloc = s.charAt( 16 ); 
		    					//System.out.println( " Keeping it." );
		    				}
		    				else { 
		    					//System.out.println( " Discarding it." );
		    					continue; 
		    				}
		    				
		    			}
		    			
		    			if ( ix != index || ( ix == index && icode != ic ) ) {
		    				
		    				
		    				if ( index != -1 ) {
		    					
		    					int d = ( index - ix );
		    					
		    					if ( d > 1 ) {
		    						//System.out.println( "Found gap: " + d );
			    					System.out.println( "Inserting @ sequence!" );

		    						for ( int i = d; i > 1; --i )
		    							bases.add( new Base( ( ix /*- first_index*/ - i ), ic, '@', "@" ) ); 
		    					} else if ( d < -1 ) {
		    						//System.out.println( "Found gap: " + d );
			    					System.out.println( "Inserting @ sequence!" );

		    						d = -d;
		    						for ( int i = 1; i < d; ++i ) 
		    							bases.add( new Base( ( ix /* - first_index*/ + i ), ic, '@', "@" ) ); 
		    					} 
		    						
		    				}
		    				
		    				index = ix;
		    				icode = ic;
		    				String trName = name.trim();
		    				char base_code = baseDict.getBaseCode( trName );
		    				
		    				if ( base_code != '?' || !baseDict.isAminoAcid( trName ) ) {		    				
		    					b = new Base( ix /*- first_index*/, ic, base_code, trName );	
		    					bases.add( b );	    					 
		    				} 
		    				
		    			}
		    			
		    			if ( b != null ) {
		    				
		    				if ( AtomCoords.isBackboneAtom( s.substring( 12, 16 ).trim() ) ) {
		    			
			    			
		    					b.getBackboneAtoms().add( 
		    							new AtomCoords( s.substring( 12, 16 ).trim(),
		    									Double.parseDouble( s.substring( 30, 38 ) ),
		    									Double.parseDouble( s.substring( 38, 46 ) ),
		    									Double.parseDouble( s.substring( 46, 54 ) ) ) );
		    			
		    				} else {
	    					
		    					b.getNonBackboneAtoms().add(
		    							new AtomCoords( s.substring( 12, 16 ).trim(),
		    									Double.parseDouble( s.substring( 30, 38 ) ),
		    									Double.parseDouble( s.substring( 38, 46 ) ),
		    									Double.parseDouble( s.substring( 46, 54 ) ) ) );
	    						    					
		    				}
		    			}
		    		}
	    		}	    		
	    	}
	    	
	    } catch (FileNotFoundException e) {
	    	e.printStackTrace();	    	
		} catch (IOException e) {
			e.printStackTrace();
		}	

		return bases;
		
	}
	
	@SuppressWarnings("deprecation")
	public static ArrayList < BaseContact > readBasePairAnnotation( String fname ) {
		
		ArrayList < BaseContact > basepairs = new ArrayList < BaseContact > ();
		File file = new File( fname );
		FileInputStream fis = null;
	    BufferedInputStream bis = null;
	    DataInputStream dis = null;
	    
	    try {
	    	
	    	fis = new FileInputStream( file );
	    	bis = new BufferedInputStream( fis );
	    	dis = new DataInputStream( bis );
	    	
	    	boolean go = true;
	    	boolean begin_found = false;
	    	
	    	while ( dis.available() != 0 ) {
	    		
	    		String s = dis.readLine();
	    		
	    		if ( s.contains( "BEGIN" ) ) {
	    			
	    			begin_found = true;
	    			
	    		} else if ( s.contains( "END" ) ) { 
	    			
	    			go = false;
	    			
	    		} else if ( begin_found && go ) {
	    			
	    			BaseContact bc = parseBaseContact( s );
	    			basepairs.add( bc );
	    			
	    		} 
	    		
	    	}
	    	
	    	dis.close();
	    	bis.close();
	    	fis.close();
	    	
	    } catch (FileNotFoundException e) {
	    	e.printStackTrace();	    	
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return basepairs;
		
	}
	
	@SuppressWarnings("deprecation")
	public static Fragment readFragment( String fname ) {
		
		File file = new File( fname );
		FileInputStream fis = null;
	    BufferedInputStream bis = null;
	    DataInputStream dis = null;
	    String str;	    
	    ArrayList < FragmentLine > lines = new ArrayList < FragmentLine > ();
	    
	     try {
	    	 
	    	 fis = new FileInputStream( file );
	    	 bis = new BufferedInputStream( fis );
	    	 dis = new DataInputStream( bis );

	    	 // scrap header line
	    	 if( dis.available() != 0 ) str = dis.readLine();
	    	 
	    	 while ( dis.available() != 0 ) {
	    		 
	    		 str = dis.readLine();
	    		 lines.add( new FragmentLine( 
	    				 str.substring( 0, str.indexOf( " " ) ), 
	    				 str.substring( 11, str.length() ) ) );
	    		 
	    	 }
	    	 
	    	 dis.close();
	    	 bis.close();
	    	 fis.close();
	    	 
	     } catch (FileNotFoundException e) {
	    	 e.printStackTrace();	    	
	     } catch( IOException e ) { 
	    	 e.printStackTrace(); 
	     }
			    
	     String tmp;
	     String id = "";
	     String type = "";
	     ArrayList < Integer > chains = new ArrayList < Integer > ();
	     ArrayList < Integer > helices = new ArrayList < Integer > ();
	     ArrayList < Base > bases = new ArrayList < Base > ();
	     ArrayList < BaseContact > contacts = new ArrayList < BaseContact > ();
	     int ct;
	     String[] s;
			
	     for ( FragmentLine line : lines ) {
				
	    	 StringTokenizer st = new StringTokenizer( line.getContent() );
				
	    	 if ( line.getId().equals( "FRAGID" ) ) {
	    		 id = line.getContent();				
	    	 } else if ( line.getId().equals( "TYPE" ) ) { 
	    		 type = line.getContent();				
	    	 } else if ( line.getId().equals( "CHAIN") ) {
	    		 tmp = st.nextToken();
	    		 while ( st.hasMoreTokens() ) {
	    			 chains.add( Integer.parseInt( st.nextToken() ) );
	    		 }
	    	 } else if ( line.getId().equals( "PSTRUCTURE" ) ) {
	    		 for ( int i = 0; i < line.getContent().length(); ++i )
	    			 bases.add( new Base( i,' ', line.getContent().charAt( i ), "" ) );
	    	 } else if ( line.getId().equals( "CONTACT" ) ) {
	    		 s = new String[ st.countTokens() ];
	    		 ct = 0;
	    		 while ( st.hasMoreTokens() ) {
	    			 s[ ct++ ] = st.nextToken();
	    		 }
	    		 //CONTACT    0 47 63 c:G W/W C 0
	    		 contacts.add( new BaseContact(  
	    				 Integer.parseInt( s[ 1 ] ), Integer.parseInt( s[ 2 ] ),
	    				 ' ', ' ',
	    				 s[ 3 ].charAt( 0 ), s[ 3 ].charAt( 2 ), 
	    				 "","",
	    				 s[ 4 ].charAt( 0 ), s[ 4 ].charAt( 2 ),
	    				 Integer.parseInt( s[ 6 ] ), "", false, false ) );
							
	    	 } else if ( line.getId().equals( "HELIX" ) ) {
					
	    		 s = new String[ st.countTokens() ];
	    		 ct = 0;
	    		 while ( st.hasMoreTokens() ) {
	    			 s[ ct++ ] = st.nextToken();
	    		 }
	    		 // HELIX      0 47 63 51 59
	    		 for ( ct = 1; ct < s.length; ++ct ) 
	    			 helices.add( Integer.parseInt( s[ ct ] ) );
					
	    	 } else if ( line.getId().equals( "COORD" ) ) {
					
	    		 s = new String[ st.countTokens() ];
	    		 ct = 0;
	    		 while ( st.hasMoreTokens() ) {
	    			 s[ ct++ ] = st.nextToken();
	    		 }
	    		 //COORD      0 C P 12.521 6.541 8.297
	    		 int b = Integer.parseInt( s[ 0 ] );
	    		 bases.get( b ).getBackboneAtoms().add( 
	    				 	new AtomCoords( 
	    				 			s[ 2 ], 
									Double.parseDouble( s[ 3 ] ),
									Double.parseDouble( s[ 4 ] ),
									Double.parseDouble( s[ 5 ] ) ) );
	    	 }
				
	     }
	     
		return new Fragment ( id, type, bases, contacts, chains, helices, 1 );
	}
	
}
