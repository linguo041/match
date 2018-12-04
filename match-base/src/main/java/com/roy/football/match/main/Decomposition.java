package com.roy.football.match.main;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;

public class Decomposition {
	
	public static void main (String args[]) {
		RealMatrix coefficients = new Array2DRowRealMatrix(
				new double[][] {
					{ 1, 1, 1},
					{ 2, 2, 5 },
					{ 4, 2, 4 },
					{ 1, 2, 1 },
					{ 2, 2, 1 },
					{ 3, 2, 1 }
					}, false);
		DecompositionSolver solver = new SingularValueDecomposition(coefficients).getSolver();
			
		RealVector constants = new ArrayRealVector(new double[] { 10, 35 , 34, 14, 13, 11}, false);
		RealVector solution = solver.solve(constants);
		
		System.out.println(solution);
	}
}
