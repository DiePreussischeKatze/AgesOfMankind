package org.src.components.environment;

public final class WorldCalendar {
	public static final class Date {
		public String month;

		public int day;
		public int year;

		public Date(final String month, final int day, final int year) {
			this.month = month;
			this.day = day;
			this.year = year;
		}
	}

	private int year;
	private int month;
	private int day;
	private int hour;

	public WorldCalendar(final int year, final int month, final int day, final int hour) {
		this.hour = hour;
		this.year = year;
		this.month = month;
		this.day = day;

		// Just for me
		if (month == 0 || day == 0 || year == 0) {
			throw new RuntimeException("Wrong month number for: " + month);
		}
	}

	public void incrementHour() {
		hour++;
		if (hour % 24 == 0) {
			hour = 0;
			incrementDay();
		}
	}

	private void incrementDay() {
		day++;
		if (day == 31 && (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12)) {
			day = 0;
			incrementMonth();
		} else if (day == 30 && (month == 4 || month == 6 || month == 9 || month == 11)) {
			day = 0;
			incrementMonth();
		} else if (((day == 27 && !isLeapYear()) || (day == 28 && isLeapYear())) && month == 2) {
			day = 0;
			incrementMonth();
		}

	}

	private boolean isLeapYear() {
		if (year % 4 == 0) {
			if (year % 100 == 0) {
				if (year % 400 == 0) {
					return true;
				}
				return false;
			}
			return true;
		}
		return false;
	}

	private void incrementMonth() {
		month++;
		if (month % 12 == 0) {
			incrementYear();
		}
	}

	private void incrementYear() {
		year++;
		if (year == 0) {
			year = 1;
		}
	}

	// TODO: Make it support more languages when I add 'em
	private String getMonthName(final int month) {
		return switch (month) {
			case 1 -> "January";
			case 2 -> "February";
			case 3 -> "March";
			case 4 -> "April";
			case 5 -> "May";
			case 6 -> "June";
			case 7 -> "July";
			case 8 -> "August";
			case 9 -> "September";
			case 10 -> "October";
			case 11 -> "November";
			case 12 -> "December";
			default -> throw new RuntimeException("Unable to find month: " + month);
		};
	}

	public Date getDate() {
		return new Date(getMonthName(month), day, year);
	}

	public String toStringHour() {
		return hour + ":00";
	}

	public int getHour() {
		return hour;
	}

}
