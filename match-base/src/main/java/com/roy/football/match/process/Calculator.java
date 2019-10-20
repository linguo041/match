package com.roy.football.match.process;

public interface Calculator <T, E> {
	public T calucate(E matchData);
	
	public void calucate(T Result, E matchData);
}
