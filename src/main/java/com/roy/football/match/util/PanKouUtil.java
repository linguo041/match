package com.roy.football.match.util;

import com.roy.football.match.OFN.response.AsiaPl;

public class PanKouUtil {
	
	public static PKDirection getPKDirection (AsiaPl current, AsiaPl main) {
		float currentPk = getCalculatedPk(current);
		float mainPk = getCalculatedPk(main);
		
		float pkDiff = current.getPanKou() - main.getPanKou();
		currentPk = currentPk + pkDiff *0.25f;
		
		// aomen summed pk is 1.94
		// 1.16 - 0.68
		// 1.12 - 0.72
		// 1.10 - 0.74
		// 1.08 - 0.76
		// 1.02 - 0.82
		// 1.00 - 0.84
		// 0.98 - 0.86
		// 0.96 - 0.88
		// 0.94 - 0.90
		// 0.92 - 0.92
		
		// 0.92 -> 0.68
		if (currentPk - mainPk >= 0.24f
				// 0.6 -> 0.68 or 0.68 -> 0.76
				|| main.gethWin() <= 0.68f && currentPk - mainPk >= -0.08f
				// 0.7 -> 0.74 or 0.72 -> 0.76
				|| main.gethWin() <= 0.72f && currentPk - mainPk >= -0.04f
				// 0.8 -> 0.72
				|| main.gethWin() <= 0.8f && currentPk - mainPk >= 0.08f) {
			return PKDirection.Uper;
		} else if (currentPk - mainPk >= 0.18f
				|| main.gethWin() <= 0.66f && currentPk - mainPk >= -0.16f
				// 0.6 -> 0.72 or 0.72 -> 0.84
				|| main.gethWin() <= 0.72f && currentPk - mainPk >= -0.12f
				// 0.72 -> 0.8 or 0.76 -> 0.84
				|| main.gethWin() <= 0.76f && currentPk - mainPk >= -0.08f
				// 0.74 -> 0.78  or 0.8 -> 0.84
				|| main.gethWin() <= 0.8f && currentPk - mainPk >= -0.04f
				// 0.9 -> 0.82
				|| main.gethWin() <= 0.9f && currentPk - mainPk >= 0.08f) {
			return PKDirection.Up;
		} else if (currentPk - mainPk <= -0.24f
				|| main.gethWin() >= 1.20f && currentPk - mainPk <= 0.14f
				// 1.20 -> 1.1 or 1.16 -> 1.06
				|| main.gethWin() >= 1.16f && currentPk - mainPk <= 0.1f
				// 1.16 -> 1.12 or 1.1 -> 1.06
				|| main.gethWin() >= 1.1f && currentPk - mainPk <= 0.04f
				|| main.gethWin() >= 1.02f && currentPk - mainPk <= -0.06f) {
			return PKDirection.Downer;
		} else if (currentPk - mainPk <= -0.18f
				// 1.20 -> 1.08 or 1.10 -> 0.98
				|| main.gethWin() >= 1.10f && currentPk - mainPk <= 0.12f
				// 1.10 -> 1.02 or 1.06 -> 0.98
				|| main.gethWin() >= 1.06f && currentPk - mainPk <= 0.08f
				// 1.06 -> 1.02 or 1.02 -> 0.98
				|| main.gethWin() >= 1.02f && currentPk - mainPk <= 0.04f
				// 0.92 -> 0.98
				|| main.gethWin() >= 0.92f && currentPk - mainPk <= -0.08f) {
			return PKDirection.Down;
		} else {
			return PKDirection.Middle;
		}
	}
	
	public static float getCalculatedPk (AsiaPl asiaPk) {
		float pankou = asiaPk.getPanKou();
		float winP = asiaPk.gethWin();
		float loseP = asiaPk.getaWin();
		
		return pankou - (winP - loseP)/2;
	}
	
	public static boolean isUpSupport (Float pPk, Float cPk, Float pankou) {
		if (pankou > 1.01f) {
			return cPk > pPk * 0.82f;
		} else if (pankou == 1f) {
			return cPk > pPk * 0.8f;
		} else if (pankou == 0.75f) {
			return cPk > pPk * 0.78f;
		} else if (pankou == 0.5f) {
			return cPk > pPk - 0.2f;
		} else if (pankou == 0.25f) {
			return cPk > pPk - 0.15f;
		} else if (pankou == 0f) {
			return cPk > pPk - 0.12f;
		} else if (pankou == -0.25f) {
			return cPk > pPk - 0.12f;
		} else if (pankou == -0.5f) {
			return cPk > pPk - 0.18f;
		} else if (pankou == -0.75f) {
			return cPk > pPk * 1.2f;
		} else if (pankou == -1f) {
			return cPk > pPk * 1.22f;
		} else /*if (pankou < -1f)*/ {
			return cPk > pPk * 1.2f;
		}
	}
	
	public static boolean isDownSupport (Float pPk, Float cPk, Float pankou) {
		if (pankou > 1.01f) {
			return cPk < pPk * 1.22f;
		} else if (pankou == 1f) {
			return cPk < pPk * 1.22f;
		} else if (pankou == 0.75f) {
			return cPk < pPk * 1.22f;
		} else if (pankou == 0.5f) {
			return cPk < pPk + 0.2f;
		} else if (pankou == 0.25f) {
			return cPk < pPk + 0.15f;
		} else if (pankou == 0f) {
			return cPk < pPk + 0.12f;
		} else if (pankou == -0.25f) {
			return cPk < pPk + 0.12f;
		} else if (pankou == -0.5f) {
			return cPk < pPk + 0.18f;
		} else if (pankou == -0.75f) {
			return cPk < pPk * 0.8f;
		} else if (pankou == -1f) {
			return cPk < pPk * 0.8f;
		} else /*if (pankou < -1f)*/ {
			return cPk < pPk * 0.8f;
		}
	}
	
	public static enum PKDirection {
		Downer, Down, Middle, Up, Uper
	}
	
	public static void main (String args[]) {
		// down
		AsiaPl main1 = new AsiaPl(1.12f, 0.72f, 0.25f);
		AsiaPl current1 = new AsiaPl(1.02f, 0.82f, 0.25f);
		System.out.println(getPKDirection(current1, main1));
		
		// up
		AsiaPl main2 = new AsiaPl(1.02f, 0.82f, 0.25f);
		AsiaPl current2 = new AsiaPl(0.88f, 0.96f, 0.25f);
		System.out.println(getPKDirection(current2, main2));
		
		// up
		AsiaPl main3 = new AsiaPl(0.80f, 1.04f, 0.25f);
		AsiaPl current3 = new AsiaPl(0.70f, 1.14f, 0.25f);
		System.out.println(getPKDirection(current3, main3));
		
		// downer
		AsiaPl main4 = new AsiaPl(1.1f, 0.74f, 0.25f);
		AsiaPl current4 = new AsiaPl(0.7f, 1.14f, 0f);
		System.out.println(getPKDirection(current4, main4));
		
		// upper
		AsiaPl main7 = new AsiaPl(0.7f, 1.14f, 0f);
		AsiaPl current7 = new AsiaPl(1.12f, 0.72f, 0.25f);
		System.out.println(getPKDirection(current7, main7));
	}
}
