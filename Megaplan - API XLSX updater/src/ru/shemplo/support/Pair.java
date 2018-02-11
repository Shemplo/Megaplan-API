package ru.shemplo.support;


public class Pair <T, D> {

	public final T f;
	public final D s;
	
	public  Pair (T f, D s) {
		this.f = f; this.s = s;
	}
	
	@Override
	public String toString () {
		return f + " " + s;
	}
	
	public static <T, D> Pair <T, D> make (T t, D d) {
		return new Pair <T, D> (t, d);
	}
	
}
