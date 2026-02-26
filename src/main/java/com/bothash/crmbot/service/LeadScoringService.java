package com.bothash.crmbot.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LeadScoringService {

	private static final Pattern VALID_EMAIL_PATTERN = Pattern
			.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

	private static final Pattern VALID_PHONE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");

	private static final Set<String> DISPOSABLE_EMAIL_KEYWORDS = new HashSet<>(
			Arrays.asList("test", "fake", "temp", "dummy", "sample", "abc", "xyz", "asdf", "qwerty", "noemail",
					"nomail", "none", "na", "null", "example"));

	private static final Set<String> FAKE_NAME_KEYWORDS = new HashSet<>(
			Arrays.asList("test", "demo", "fake", "sample", "abc", "xyz", "asdf", "qwerty", "na", "none", "null",
					"unknown", "no name", "noname", ".", "-", "a", "b", "x"));

	/**
	 * Scores a lead based on multiple signals and returns the lead type.
	 *
	 * Score breakdown:
	 *   Name present & valid:       +15
	 *   Name has first+last:         +5
	 *   Phone present & valid:      +30
	 *   Phone not fake pattern:     +10
	 *   Email present & valid:      +20
	 *   Email not disposable/fake:  +10
	 *   Not a duplicate:            +10
	 *
	 * Classification:
	 *   >= 70  -> HOT
	 *   >= 45  -> PROSPECT
	 *   >= 20  -> COLD
	 *   < 20   -> DUSTBIN
	 */
	public String classifyLead(String name, String phoneNumber, String email, boolean isDuplicate) {
		int score = 0;

		score += scoreName(name);
		score += scorePhone(phoneNumber);
		score += scoreEmail(email);

		if (!isDuplicate) {
			score += 10;
		}

		String leadType = scoreToLeadType(score);
		log.info("Lead scored: name={}, phone={}, email={}, duplicate={}, score={}, type={}",
				name, maskPhone(phoneNumber), maskEmail(email), isDuplicate, score, leadType);
		return leadType;
	}

	private int scoreName(String name) {
		int score = 0;
		if (name == null || name.trim().isEmpty()) {
			return 0;
		}
		String trimmed = name.trim().toLowerCase();

		if (trimmed.length() < 2 || FAKE_NAME_KEYWORDS.contains(trimmed)) {
			return 0;
		}

		// Name is present and looks real
		score += 15;

		// Bonus if name has first + last (contains space)
		if (trimmed.contains(" ") && trimmed.split("\\s+").length >= 2) {
			score += 5;
		}
		return score;
	}

	private int scorePhone(String phoneNumber) {
		int score = 0;
		if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
			return 0;
		}
		String cleaned = phoneNumber.replaceAll("[^0-9]", "");

		// Remove leading 91 country code if present
		if (cleaned.length() == 12 && cleaned.startsWith("91")) {
			cleaned = cleaned.substring(2);
		}

		if (cleaned.length() != 10) {
			return 0;
		}

		if (VALID_PHONE_PATTERN.matcher(cleaned).matches()) {
			score += 30;
		}

		// Check for fake patterns like 1111111111, 1234567890
		if (!isFakePhonePattern(cleaned)) {
			score += 10;
		}
		return score;
	}

	private boolean isFakePhonePattern(String phone) {
		// All same digits
		if (phone.chars().distinct().count() == 1) {
			return true;
		}
		// Sequential digits
		if (phone.equals("1234567890") || phone.equals("0987654321")
				|| phone.equals("9876543210") || phone.equals("6789012345")) {
			return true;
		}
		return false;
	}

	private int scoreEmail(String email) {
		int score = 0;
		if (email == null || email.trim().isEmpty()) {
			return 0;
		}
		String trimmed = email.trim().toLowerCase();

		if (!VALID_EMAIL_PATTERN.matcher(trimmed).matches()) {
			return 0;
		}

		// Email format is valid
		score += 20;

		// Check if it looks disposable/fake
		String localPart = trimmed.split("@")[0];
		boolean isFake = DISPOSABLE_EMAIL_KEYWORDS.stream()
				.anyMatch(keyword -> localPart.equals(keyword) || localPart.startsWith(keyword + "@"));

		String domain = trimmed.split("@")[1];
		boolean isDisposableDomain = domain.contains("mailinator") || domain.contains("tempmail")
				|| domain.contains("throwaway") || domain.contains("guerrilla") || domain.contains("yopmail");

		if (!isFake && !isDisposableDomain) {
			score += 10;
		}
		return score;
	}

	private String scoreToLeadType(int score) {
		if (score >= 70) {
			return "HOT";
		} else if (score >= 45) {
			return "PROSPECT";
		} else if (score >= 20) {
			return "COLD";
		} else {
			return "DUSTBIN";
		}
	}

	private String maskPhone(String phone) {
		if (phone == null || phone.length() < 4) return "***";
		return "****" + phone.substring(phone.length() - 4);
	}

	private String maskEmail(String email) {
		if (email == null || !email.contains("@")) return "***";
		String[] parts = email.split("@");
		return parts[0].charAt(0) + "***@" + parts[1];
	}
}
