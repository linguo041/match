package com.roy.football.match.OFN;

import com.roy.football.match.OFN.response.AsiaPl;
import com.roy.football.match.OFN.statics.matrices.OFNCalculateResult;
import com.roy.football.match.OFN.statics.matrices.OFNKillPromoteResult;
import com.roy.football.match.OFN.statics.matrices.PankouMatrices;
import com.roy.football.match.base.ResultGroup;
import com.roy.football.match.process.CalculateResult;
import com.roy.football.match.process.KillPromoter;

public class PankouKillPromoter implements KillPromoter<OFNKillPromoteResult, OFNCalculateResult> {
	
	public OFNKillPromoteResult calculate (OFNCalculateResult calResult) {
		OFNKillPromoteResult killPromoteResult = new OFNKillPromoteResult();
		
		kill(killPromoteResult, calResult);
		promote(killPromoteResult, calResult);
		
		return killPromoteResult;
	}

	@Override
	public void kill(OFNKillPromoteResult killPromoteResult, OFNCalculateResult calResult) {
		if (killPromoteResult != null) {
			PankouMatrices pkMatrices = calResult.getPkMatrices();
			Float predictPk = calResult.getPredictPanKou();
			
			if (pkMatrices != null) {
				killPromoteResult.setKillByPk(killZhuShangPan(pkMatrices, predictPk));
			}
		}
	}

	@Override
	public OFNKillPromoteResult kill(OFNCalculateResult calResult) {
		OFNKillPromoteResult killResult = new OFNKillPromoteResult();
		
		PankouMatrices pkMatrices = calResult.getPkMatrices();
		Float predictPk = calResult.getPredictPanKou();
		
		if (pkMatrices != null) {
			killResult.setKillByPk(killZhuShangPan(pkMatrices, predictPk));
		}
		
		return killResult;
	}
	
	@Override
	public void promote(OFNKillPromoteResult killPromoteResult,
			OFNCalculateResult calResult) {
		if (killPromoteResult != null) {
			PankouMatrices pkMatrices = calResult.getPkMatrices();
			Float predictPk = calResult.getPredictPanKou();
			
			if (pkMatrices != null) {
				killPromoteResult.setPromoteByPk(promoteByPk(pkMatrices, predictPk));
			}
		}
	}

	@Override
	public OFNKillPromoteResult promote(OFNCalculateResult calResult) {
		OFNKillPromoteResult killPromoteResult = new OFNKillPromoteResult();
		
		PankouMatrices pkMatrices = calResult.getPkMatrices();
		Float predictPk = calResult.getPredictPanKou();
		
		if (pkMatrices != null) {
			killPromoteResult.setPromoteByPk(promoteByPk(pkMatrices, predictPk));
		}
		
		return killPromoteResult;
	}
	
	private ResultGroup killZhuShangPan (PankouMatrices pkMatrices, Float predictPk) {
		if (pkMatrices != null && predictPk != null) {
			AsiaPl origin = pkMatrices.getOriginPk();
			AsiaPl main = pkMatrices.getMainPk();
			AsiaPl current = pkMatrices.getCurrentPk();

			if (main.gethWin() > 1.01
					&& (current.gethWin() > 1.01 || origin.gethWin() > 1.01)
					&& main.getPanKou() - predictPk >= 0.12) {
				if (main.getPanKou() <= -0.5) {
					return ResultGroup.ThreeOne;
				} else if (main.getPanKou() < 0.75 && main.getPanKou() != 0) {
					return ResultGroup.Three;
				}
			}
			
			if (main.getaWin() > 1.01
					&& (current.getaWin() > 1.01 || origin.getaWin() > 1.01)
					&& predictPk - main.getPanKou() >= 0.12) {
				if (main.getPanKou() >= 0.5) {
					return ResultGroup.OneZero;
				} else if (main.getPanKou() >= -0.5) {
					return ResultGroup.Zero;
				}
			}
		}

		return null;
	}
	
	private ResultGroup promoteByPk (PankouMatrices pkMatrices, Float predictPk) {
		if (pkMatrices != null && predictPk != null) {
			AsiaPl origin = pkMatrices.getOriginPk();
			AsiaPl main = pkMatrices.getMainPk();
			AsiaPl current = pkMatrices.getCurrentPk();

			if (main.gethWin() <= 0.96
					&& current.gethWin() <= 0.96
					&& predictPk - main.getPanKou() >= 0.12) {
				if (main.getPanKou() >= 0.5) {
					return ResultGroup.Three;
				} else if (main.getPanKou() >= -0.5) {
					return ResultGroup.ThreeOne;
				}
			}
			
			if (main.getaWin() <= 0.96
					&& current.getaWin() <= 0.96
					&& main.getPanKou() - predictPk >= 0.12) {
				if (main.getPanKou() <= -0.5) {
					return ResultGroup.Zero;
				} else if (main.getPanKou() <= 0.5) {
					return ResultGroup.OneZero;
				}
			}
		}

		return null;
	}
}
