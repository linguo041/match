package com.roy.football.match.OFN.statics.matrices;

import java.util.List;

import com.google.common.collect.Lists;

import lombok.Data;

@Data
public class PromoteMatrics {
	private PromoteRatio winRatio;
	private PromoteRatio drawRatio;
	private PromoteRatio loseRatio;
	
	public String toString () {
		return winRatio + "\n" + drawRatio + "\n" + loseRatio;
	}
	
	@Data
	public static class PromoteRatio {
		private Float rBaseDegree;
		private Float rPullNPredict;
		private Float rAomen;
		private Float rJincai;
		private Float rWilliam;
		private Float rAomenPk;
		
		public float getTotal () {
			return (rBaseDegree == null ? 0 : rBaseDegree)
					+ (rPullNPredict == null ? 0 : rPullNPredict)
					+ (rAomen == null ? 0 : rAomen)
					+ (rJincai == null ? 0 : rJincai)
					+ (rWilliam == null ? 0 : rWilliam)
					+ (rAomenPk == null ? 0 : rAomenPk);
		}
		
		public String toString () {
			StringBuilder format = new StringBuilder("Total: %.2f,");
			List<Object> args = Lists.newArrayList(getTotal());
			
			if (rBaseDegree != null) {
				format.append(" Base: %.2f,");
				args.add(rBaseDegree);
			}
			if (rAomenPk != null) {
				format.append(" Pk: %.2f,");
				args.add(rAomenPk);
			}
			if (rPullNPredict != null) {
				format.append(" Pull: %.2f,");
				args.add(rPullNPredict);
			}
			if (rAomen != null) {
				format.append(" Ao: %.2f,");
				args.add(rAomen);
			}
			if (rJincai != null) {
				format.append(" Jc: %.2f,");
				args.add(rJincai);
			}
			if (rWilliam != null) {
				format.append(" Will: %.2f,");
				args.add(rWilliam);
			}
			
			return String.format(format.toString(), args.toArray(new Object[args.size()]));
		}
	}
}
