package com.roy.football.match.process;

public interface KillPromoter <T, E> {
	public void kill (T killPromoteResult, E calResult);
	
	public T kill (E calResult);
	
	public void promote (T killPromoteResult, E calResult);
	
	public T promote (E calResult);
}
