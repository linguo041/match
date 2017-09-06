package com.roy.football.match.util;

import com.roy.football.match.OFN.response.AsiaPl;

public class PanKouUtil {
	
	public static PKDirection getPKDirection (AsiaPl current, AsiaPl main) {
		float currentPk = getCalculatedPk(current);
		float mainPk = getCalculatedPk(main);
		
		// aomen summed pk is 1.94
		// 1.12 - 0.72
		// 1.10 - 0.74
		// 1.08 - 0.76
		// 1.02 - 0.82
		// 1.00 - 0.84
		// 0.98 - 0.86
		// 0.96 - 0.88
		// 0.94 - 0.90
		// 0.92 - 0.92
		
		if (currentPk - mainPk >= -0.04f
				&& (current.gethWin() <= 0.72f || main.gethWin() <= 0.72f) ) {
			return PKDirection.Uper;
		} else if (currentPk - mainPk >= -0.04f
				&& (current.gethWin() <= 0.82f || main.gethWin() <= 0.82f) ) {
			return PKDirection.Up;
		} else if (currentPk - mainPk <= 0.04f
				&& (current.gethWin() >= 1.02f || main.gethWin() >= 1.02f) ) {
			return PKDirection.Down;
		} else if (currentPk - mainPk <= 0.04f
				&& (current.gethWin() >= 1.12f || main.gethWin() >= 1.12f) ) {
			return PKDirection.Downer;
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
		AsiaPl main = new AsiaPl(1.0f, 0.84f, 0.5f);
		AsiaPl current = new AsiaPl(0.96f, 0.88f, 0.5f);
		
		System.out.println(getPKDirection(current, main));
	}
}
