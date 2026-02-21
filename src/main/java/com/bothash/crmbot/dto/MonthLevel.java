package com.bothash.crmbot.dto;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthLevel {

	private int month;
    private long count;
    private Map<Integer, Long> days = new HashMap<>();
}
