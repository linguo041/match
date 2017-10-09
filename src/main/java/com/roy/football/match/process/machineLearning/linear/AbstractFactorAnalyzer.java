package com.roy.football.match.process.machineLearning.linear;

import java.text.ParseException;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.commons.lang.Pair;
import com.roy.football.match.base.League;
import com.roy.football.match.jpa.entities.calculation.ELeague;
import com.roy.football.match.jpa.repositories.ELeagueRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractFactorAnalyzer implements PredictFactorAnalyzer{
	
	@Autowired
	private ELeagueRepository eLeagueRepository;
	
	public abstract Pair<double[], double[][]> getEvaluatedData(String fromDate, String endDate, League le) throws ParseException;

	@Override
	public void analysis() {
		String startDate = "2016-10-01";
		String endDate = "2017-06-01";
		
		System.out.println(String.format("Checked matches from %s to %s"
				+ "\nwith fomula: score= a * h_att_g_def + b * g_att_h_def"
				+ "\nLeague \tgoalPerMatch \t\t  a \t\t b",
				startDate, endDate));
		
		for (League le : League.values()) {
			try {
				ELeague eLe = eLeagueRepository.findOne(le.getLeagueId());
				
				Pair<double[], double[][]> data = getEvaluatedData(startDate, endDate, le);
				
				if (data != null) {
					RealMatrix coefficients = new Array2DRowRealMatrix(data.getSecond(), false);
//					DecompositionSolver solver = new SingularValueDecomposition(coefficients).getSolver();
					DecompositionSolver solver = new QRDecomposition(coefficients).getSolver();
						
					RealVector constants = new ArrayRealVector(data.getFirst(), false);
					RealVector solution = solver.solve(constants);
					
					System.out.println(String.format("League: %15s   %-5f \t %s",
							le, eLe == null || eLe.getGoalPerMatch() == null ? 0f : eLe.getGoalPerMatch(),
							solution));
				}
			} catch (Exception e) {
				log.error("unable to calculate", e);
			}
		}
	}
}
