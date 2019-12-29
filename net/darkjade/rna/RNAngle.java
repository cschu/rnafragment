package net.darkjade.rna;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

class vec3 {
	
	double x, y, z;
	vec3() { this.x = this.y = this.z = 0.0; }
	vec3( double x, double y, double z ) { this.x = x; this.y = y; this.z = z; }
	vec3( vec3 v ) { this.x = v.x; this.y = v.y; this.z = v.z; }
	vec3( vec3 u, vec3 v, boolean unit ) { this( v.x - u.x, v.y - u.y, v.z - u.z ); if ( unit ) this.normalise(); }
	
	double length() { return Math.sqrt( this.x * this.x + this.y * this.y + this.z * this.z ); }
	
	vec3 normalise() { return vec3.divide( this, this.length() ); }	
	/**
	 * Subtracts v from u 
	 * */
	static vec3 subtract( vec3 u, vec3 v ) { return new vec3( u.x - v.x, u.y - v.y, u.z - v.z ); }
	static vec3 add( vec3 u, vec3 v ) { return new vec3( v.x + u.x, v.y + u.y, v.z + u.z ); }
	static vec3 multiply( vec3 v, double s ) { return new vec3( v.x * s, v.y * s, v.z * s ); }
	static vec3 divide( vec3 v, double s ) { return new vec3( v.x / s, v.y / s, v.z / s ); }
	static vec3 cross( vec3 v, vec3 u ) { return new vec3( u.y * v.z - u.z *v.y, u.z * v.x - u.x * v.z, u.x * v.y - u.y * v.x ); }
	static double dot( vec3 v, vec3 u ) { return u.x * v.x + u.y * v.y + u.z * v.z; }
	static vec3 translate( vec3 v, vec3 u, double d ) { return vec3.add( v, vec3.multiply( u, d/Math.abs(d) ) ); }
	static vec3 transform( vec3 v, double[][] R, boolean inverse ) {
	
		vec3 res = new vec3();
		if ( inverse ) {
			res = new vec3( R[ 0 ][ 0 ] * v.x + R[ 1 ][ 0 ] * v.y + R[ 2 ][ 0 ] * v.z, 
							R[ 0 ][ 1 ] * v.x + R[ 1 ][ 1 ] * v.y + R[ 2 ][ 1 ] * v.z,
							R[ 0 ][ 2 ] * v.x + R[ 1 ][ 2 ] * v.y + R[ 2 ][ 2 ] * v.z );			
		} else {
			res = new vec3( R[ 0 ][ 0 ] * v.x + R[ 0 ][ 1 ] * v.y + R[ 0 ][ 2 ] * v.z, 
							R[ 1 ][ 0 ] * v.x + R[ 1 ][ 1 ] * v.y + R[ 1 ][ 2 ] * v.z,
							R[ 2 ][ 0 ] * v.x + R[ 2 ][ 1 ] * v.y + R[ 2 ][ 2 ] * v.z );			
		}		
				
		return res;
	}
	
	static double[][] arbitrary_rotation( double theta, vec3 v ) {
		
		double[][] R = new double[ 3 ][ 3 ];
		vec3 u = v.normalise();
		
		double sinth  = Math.sin( theta ), costh = Math.cos( theta ), icos = 1.0 - costh;
		double xy = u.x * u.y * icos, xz = u.x * u.z * icos, yz = u.y * u.z * icos;
		double xsin = u.x * sinth, ysin = u.y * sinth, zsin = u.z * sinth;
		
		R[ 0 ][ 0 ] = u.x * u.x * icos + costh; R[ 0 ][ 1 ] = xy - zsin;                R[ 0 ][ 2 ] = xz + ysin;
		R[ 1 ][ 0 ] = xy + zsin;                R[ 1 ][ 1 ] = u.y * u.y * icos + costh; R[ 1 ][ 2 ] = yz - xsin;
		R[ 2 ][ 0 ] = xz - ysin;                R[ 2 ][ 1 ] = yz + xsin;                R[ 2 ][ 2 ] = u.z * u.z * icos + costh;
		
		return R;
		
	}
	
	static vec3 xrotation( vec3 v, double theta ) {		
		double sinth = Math.sin( theta ), costh = Math.cos( theta );
		return new vec3( v.x, v.y * costh + v.z * -sinth, v.y * sinth + v.z * costh );		
	}
	
	static vec3 yrotation( vec3 v, double theta ) {
		double sinth = Math.sin( theta ), costh = Math.cos( theta );
		return new vec3( v.x * costh + v.z * sinth, v.y, v.x * -sinth + v.z * costh );
	}
	
	static vec3 zrotation( vec3 v, double theta ) {
		double sinth = Math.sin( theta ), costh = Math.cos( theta );
		return new vec3( v.x * costh + v.y * -sinth, v.x * sinth + v.y * costh, v.z );
	}
	
	static vec3 xaxis() { return new vec3( 1.0, 0.0, 0.0 ); }
	static vec3 yaxis() { return new vec3( 0.0, 1.0, 0.0 ); }
	static vec3 zaxis() { return new vec3( 0.0, 0.0, 1.0 ); } 

	
}

public class RNAngle {
	
	public static double compute_dihedral( vec3 a, vec3 b, vec3 c, vec3 d ) {
		
		vec3 AB = new vec3( a, b, false ); 		
		vec3 BC = new vec3( b, c, false );
		vec3 CD = new vec3( c, d, false );		
		
		double arg1 = vec3.dot( vec3.multiply( AB, BC.length() ), vec3.cross( BC, CD ) );
		double arg2 = vec3.dot( vec3.cross( AB, BC ), vec3.cross( BC, CD ) );
		
		return Math.atan2( arg1, arg2 );
		
	}
	

	
	
}
