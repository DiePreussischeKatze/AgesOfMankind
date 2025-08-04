package org.src.components.province;

import imgui.type.ImString;
import org.lwjgl.system.ThreadLocalUtil;
import org.src.core.main.PerfTimer;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class Ethnicity {
	private ImString name;
	//                    AGE,     AMOUNT_OF_PEOPLE
	private final HashMap<Integer, Integer> demographics;

	public Ethnicity() {
		this.demographics = new HashMap<>();

		// fill age; I don't expect there to be a sizable enough amount of people above the age of 80
		for (int i = 0; i < 80; i++) {
			this.demographics.put(i, 0);
		}
	}

	// double because I need to account for decimal places when "distributing" people
	public void addPeople(final int amount) {
		final int amountPiece = amount / 1000;
		for (int j = 0; j < 1000; j++) {
			int ageToAdd = -50;
			for (int i = 0; i < 3; i++) {
				ageToAdd += ThreadLocalRandom.current().nextInt(0, 50);
			}

			if (ageToAdd < 0) {
				ageToAdd = ThreadLocalRandom.current().nextInt(0, 8);
			}

			if (ageToAdd > 79) {
				ageToAdd = 79;
			}

			demographics.put(ageToAdd, demographics.get(ageToAdd) + (amount / 100));
		}

	}

	public void setName(final ImString name) {
		this.name = name;
	}

	public ImString getName() {
		return this.name;
	}

}
