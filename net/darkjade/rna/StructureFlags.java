package net.darkjade.rna;
public class StructureFlags {
	
	// 4 possibilities for a helix - base pair interaction
	public final static int BPAIR_TOUCHING = 0x01;	
	public final static int BPAIR_SEPARATE = 0x02;	
	public final static int BPAIR_DISJOINT = 0x04;
	public final static int BPAIR_CROSSING = 0x08;
	// TOUCHING | SEPARATE = NESTED
	public final static int BPAIR_NESTED = 0x03;
	// 2 bulge possibilities
	public final static int BPAIR_LBULGE = 0x10;
	public final static int BPAIR_RBULGE = 0x20;
	
	public final static int BPAIR_IGNORE = 0x00;
	
	/*
	 * if ( s.contains( "stacked" ) ) conf |= 1;
		if ( s.contains( "syn" ) ) conf |= 2;
		if ( s.contains( "cis" ) ) conf |= 4;
		if ( s.contains( "tran" ) ) conf |= 8;
		if ( s.contains( "!" ) ) conf |= 16;
	 * */
	
	/*
	public final static int CONTACT_STACKED  = 0x01;
	public final static int CONTACT_SYN      = 0x02; // this one is not completely correct
	public final static int CONTACT_CIS	     = 0x04;
	public final static int CONTACT_TRANS	 = 0x08;
	public final static int CONTACT_TERTIARY = 0x10;	
	public final static int CONTACT_ALL      = 0x1F; */
	
	public final static int CONTACT_CIS      = 0x00;
	public final static int CONTACT_TRANS    = 0x01;
	public final static int CONTACT_STACKED  = 0x02;   // 0: non-stacked, 1: stacked
	public final static int CONTACT_SYN5P    = 0x04;   // 0: 5'-base is anti, 1: 5'-base is syn
	public final static int CONTACT_SYN3P    = 0x08;   // 0: 3'-base is anti, 1: 3'-base is syn
	public final static int CONTACT_TERTIARY = 0x10;   // 0: secondary or LW, 1: base-backbone, backbone-backbone contact
	public final static int CONTACT_NOORIENT = 0x20;   // 0: cis|trans, 1: unknown orientation
	
	public final static int CONTACT_ALL      = 0x3F;
	
	public final static int EDGE_WC          = 0x00;
	public final static int EDGE_HOOGSTEEN   = 0x01;
	public final static int EDGE_SUGAR       = 0x02;	
	
	public final static int NO_CONTACT       = 0x00;
	public final static int PAIRED_CONTACT   = 0x01;
	public final static int UNPAIRED_CONTACT = 0x02;
	public final static int IN_MOTHERSTEM    = 0x04;
	
	public final static int UNKNOWN          = -1;
	
	public static int setFlags( String data )
	{
		int flags = 0x000000;
		
		
		if ( !data.contains( "cis" ) && !data.contains( "tran" ) ) flags |= CONTACT_NOORIENT;
		else if ( data.contains( "tran" ) ) flags |= CONTACT_TRANS;		
		
		if ( data.contains( "stacked" ) ) flags |= CONTACT_STACKED;
		if ( data.contains( "isyn" ) ) flags |= CONTACT_SYN5P;
		if ( data.contains( "jsyn" ) ) flags |= CONTACT_SYN3P;
		if ( data.contains( "!" ) ) flags |= StructureFlags.CONTACT_TERTIARY;
		
		return flags;
	}
	
	public static boolean isSet( int flags, int query ) { return ( flags & query ) == query; }
	
	public static char decodeOrientation( int flags ) { return isSet( flags, CONTACT_NOORIENT ) ? '?' : ( isSet( flags, CONTACT_CIS ) ) ? 'C' : 'T'; }
	
	public static int encodeEdge( char e ) 	
	{
		int ee = -1;		
		switch( Character.toUpperCase( e ) ) {		
		case '+' :			
		case '-' :			
		case 'W' :
			ee = EDGE_WC;
			break;
		case 'H' : 
			ee = EDGE_HOOGSTEEN;
			break;
		case 'S' : 
			ee = EDGE_SUGAR;
			break;
		default : 
			ee= UNKNOWN;		
		}
				 
		return ee;		
	} 
	
	public static char decodeEdge( int e )	
	{		
		char ee;
		switch( e ) {		
		case EDGE_WC : 
			ee = 'W';
			break;
		case EDGE_HOOGSTEEN : 
			ee = 'H';
			break;
		case EDGE_SUGAR :
			ee = 'S';
			break;		
		default :
			ee = '?';		
		}
		
		return ee;		
	}	

}