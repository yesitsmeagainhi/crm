package com.bothash.crmbot.dto;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class YearLevel {
	private int year;
    private long count;
    private Map<Integer, MonthLevel> months = new HashMap<>();
}
