package com.bothash.crmbot.dto;

import java.util.List;

import lombok.Data;

@Data
public class FaceBookLeadsResponseDto {

	private List<FacebookLeadsDto> data;
	
	private FacebookLeadsPagingDto paging;
	
	
}
