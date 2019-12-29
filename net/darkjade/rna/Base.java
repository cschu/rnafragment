package net.darkjade.rna;

import java.util.ArrayList;

class AtomCoords {
	
	String type;
	double x;
	double y;
	double z;
	
	vec3 coords;
	
	public AtomCoords( String type, double d, double e, double f ) {
		
		this.type = type;
		this.x = d;
		this.y = e;
		this.z = f;
		coords = new vec3( d, e, f );
		
	}

	public static boolean isBackboneAtom( String str ) {

		boolean answer = str.equals( "P" ) 
		|| str.equals( "O5*" ) || str.equals( "C5*" ) || str.equals( "O3*" ) 
		|| str.equals( "C3*" ) || str.equals( "C4*" )
		|| str.equals( "O5\'" ) || str.equals( "C5\'" ) || str.equals( "O3\'" ) 
		|| str.equals( "C3\'" ) || str.equals( "C4\'" );
		
		return answer;
		
	}
	
}

public class Base {
	
	private int index; // index in chain
	private char icode; // pdb insertion code
	private char basecode;
	private String name;
	private ArrayList < AtomCoords > backboneAtoms;
	private ArrayList < AtomCoords > nonbackboneAtoms;
	
	private int hashValue;

	public Base( int ix, char ic, char bc, String n ) {
		
		index = ix;
		icode = ic;
		basecode = bc;
		name = new String( n );
		backboneAtoms = new ArrayList < AtomCoords > ();
		nonbackboneAtoms = new ArrayList < AtomCoords > ();
		
		hashValue = index << 16 | icode << 8 | basecode;  
		
	}
	
	public Base() { this( -1, ' ', ' ', ""); }

	public int hashCode() { return hashValue; }
	
	public boolean equals( Object compare ) {
		
		Base b = compare instanceof Base ? ( Base ) compare : null;
				
		return b != null && index == b.getIndex() 
			&& icode == b.getIcode() 
			&& basecode == b.getBasecode() 
			&& name.equals( b.getName() );  
		
	}
	
	public boolean isModified()
	{
		return !( name.equals( "A" ) || name.equals( "C" ) || name.equals( "G" ) 
				|| name.equals( "T" ) || name.equals( "U" ) 
				|| name.equals( "ADE" ) || name.equals( "CYT" ) || name.equals( "GUA" ) 
				|| name.equals( "THY" ) || name.equals( "URA" ) || name.equals( "*" ) );
		
	}
	
	
	void showme() {}
	
	public char getBasecode() { return basecode; }

	public char getIcode() { return icode; }

	public int getIndex() { return index; }

	public String getName() { return name; }

	public ArrayList<AtomCoords> getBackboneAtoms() { return backboneAtoms; }
	
	public ArrayList<AtomCoords> getNonBackboneAtoms() { return nonbackboneAtoms; }	

}

