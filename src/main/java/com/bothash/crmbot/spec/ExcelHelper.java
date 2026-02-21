package com.bothash.crmbot.spec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.bothash.crmbot.dto.CommentStats;
import com.bothash.crmbot.entity.ActiveTask;
import com.bothash.crmbot.entity.CloseTask;
import com.bothash.crmbot.entity.Comments;
import com.bothash.crmbot.entity.CounsellingDetails;
import com.bothash.crmbot.entity.FacebookLeads;
import com.bothash.crmbot.service.ActiveTaskService;
import com.bothash.crmbot.service.CloseTaskService;
import com.bothash.crmbot.service.CommentsService;
import com.bothash.crmbot.service.CounsellingDetailsService;
import com.bothash.crmbot.service.FacebookLeadsService;
import com.bothash.crmbot.service.impl.CloseTaskServiceImpl;
import com.bothash.crmbot.service.impl.CousellingDetailsServiceImpl;

@Service
public class ExcelHelper {

	@Autowired
	private CounsellingDetailsService counsellingDetailsService;

	@Autowired
	private CloseTaskService closeTaskService;
	
	@Autowired
	private CommentsService commentsService;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Value("${keycloak.auth-server-url}")
	private String keycloackUrl;
	
	
	@Value("${keycloack.admin.username}")
	private String adminUserName;
	
	@Value("${keycloack.admin.password}")
	private String adminPassword;
	
	@Value("${keycloak.credentials.secret}")
	private String keycloackClientSecret;
	
	@Value("${crmbot-client-id}")
	private String crmbotClientId;

	public String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	String[] HEADERs = { "Id", "Title", "Description", "Published" };
	String SHEET = "Tutorials";

	public boolean hasExcelFormat(MultipartFile file) {

		if (!TYPE.equals(file.getContentType())) {
			return false;
		}

		return true;
	}

	public List<ActiveTask> excelToTasks(InputStream is) {
	    try {
	        Workbook workbook = new XSSFWorkbook(is);
	        Sheet sheet = workbook.getSheetAt(0);
	        Iterator<Row> rows = sheet.iterator();
	        List<ActiveTask> activeTasks = new ArrayList<>();
	        int rowNumber = 0;

	        while (rows.hasNext()) {
	            Row currentRow = rows.next();
	            // Skip header
	            if (rowNumber == 0) {
	                rowNumber++;
	                continue;
	            }

	            ActiveTask activeTask = new ActiveTask();
	            FacebookLeads facebookLeads = new FacebookLeads();
	            JSONArray jsonArray = new JSONArray();
	            int totalColumns = 8; // Adjust based on the number of columns you expect

	            for (int cellIdx = 0; cellIdx < totalColumns; cellIdx++) {
	                try {
	                    JSONObject jsonObject = new JSONObject();
	                    Cell currentCell = currentRow.getCell(cellIdx, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

	                    switch (cellIdx) {
	                        case 0:
	                            activeTask.setLeadPlatform(currentCell.getStringCellValue());
	                            break;

	                        case 1:
	                            activeTask.setRefferenceName(currentCell.getStringCellValue());
	                            break;

	                        case 2:
	                            activeTask.setArea(currentCell.getStringCellValue());
	                            break;

	                        case 3:
	                            String course = currentCell.getStringCellValue();
	                            if (course != null) {
	                                activeTask.setCourse(course.toUpperCase());
	                            }
	                            break;

	                        case 4:
	                            jsonObject.put("name", "phone_number");
	                            try {
	                                Double phoneNumber = currentCell.getNumericCellValue();
	                                DecimalFormat df = new DecimalFormat("#");
	                                df.setMaximumFractionDigits(11);
	                                String formattedNumber = df.format(phoneNumber);
	                                String values[] = new String[]{formattedNumber};
	                                jsonObject.put("values", values);
	                                activeTask.setPhoneNumber(formattedNumber);
	                            } catch (Exception e) {
	                                String phoneNumber = currentCell.getStringCellValue();
	                                String values[] = new String[]{phoneNumber};
	                                jsonObject.put("values", values);
	                                activeTask.setPhoneNumber(phoneNumber);
	                            }
	                            break;

	                        case 5:
	                            jsonObject.put("name", "full_name");
	                            String fullName = currentCell.getStringCellValue();
	                            String values2[] = new String[]{fullName};
	                            activeTask.setLeadName(fullName);
	                            jsonObject.put("values", values2);
	                            break;

	                        case 6:
	                            String telecallerName = currentCell.getStringCellValue();
	                            activeTask.setAssignee("telecaller");
	                            activeTask.setIsClaimed(false);
	                            activeTask.setAssignedTime(LocalDateTime.now());
	                            activeTask.setStatus("Assigned to " + telecallerName);
	                            activeTask.setTelecallerName(telecallerName);
	                            activeTask.setOwner(telecallerName);
	                            break;

	                        case 7:
	                            String status = "Assigned to " + currentCell.getStringCellValue();
	                            activeTask.setStatus(status);
	                            break;

	                        default:
	                            break;
	                    }

	                    if (!jsonObject.isEmpty()) {
	                        jsonArray.put(jsonObject);
	                    }
	                } catch (Exception e) {
	                    e.printStackTrace();
	                }
	            }

	            facebookLeads.setFieldData(jsonArray.toString());
	            activeTask.setFacebookLeads(facebookLeads);
	            activeTasks.add(activeTask);
	        }

	        workbook.close();
	        return activeTasks;
	    } catch (IOException e) {
	        throw new RuntimeException("Failed to parse Excel file: " + e.getMessage());
	    }
	}
	
	public CommentStats getCommentStats(List<Comments> allComments, List<String> userNamesToMatch) {
	    int count = 0;
	    Comments mostRecent = null;

	    for (Comments comment : allComments) {
	        if (userNamesToMatch.contains(comment.getUserEmail())) {
	            count++;

	            if (mostRecent == null || comment.getCreatedOn().isAfter(mostRecent.getCreatedOn())) {
	                mostRecent = comment;
	            }
	        }
	    }

	    return new CommentStats(count, mostRecent);
	}



	public ByteArrayInputStream taskToExcel(List<ActiveTask> activeTasks) throws IOException {
		String[] COLUMNs = { "Sr. no", "Date & time", "Student name", "Mobile no", "Email", "Area", "Course Interested",
				"Requirement", "College name", "Platform", "Campaign", "Reference name", "Reference contact",
				"Latest remark", "Manager", "Telecaller", "Counsellor", "No. of counselling","Counselling Date","Scheduled date and time",
				"Status", "Converted/ Npt Converted", "Closing remark", "UID","Admission Date","Last Comment","Comment User Name",
				"Lead type","Seat Confirmed","Supervisor Comment","Supervisor Comment Date","Total Comments Of Supervisor","Supervisor Name" };
		try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream();) {
			CreationHelper createHelper = workbook.getCreationHelper();

			Sheet sheet = workbook.createSheet("Task_Details");

			Font headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerFont.setColor(IndexedColors.BLUE.getIndex());

			CellStyle headerCellStyle = workbook.createCellStyle();
			headerCellStyle.setFont(headerFont);
			
			CellStyle dateCellStyle = workbook.createCellStyle();
			CreationHelper creationHelper = workbook.getCreationHelper();
			dateCellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss"));
			// Row for Header
			Row headerRow = sheet.createRow(0);

			// Header
			for (int col = 0; col < COLUMNs.length; col++) {
				Cell cell = headerRow.createCell(col);
				cell.setCellValue(COLUMNs[col]);
				cell.setCellStyle(headerCellStyle);
			}

			// CellStyle for Age
			CellStyle ageCellStyle = workbook.createCellStyle();
			ageCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("#"));
			
			HttpHeaders header=new HttpHeaders();
			MultiValueMap<String,String> body= new  LinkedMultiValueMap<>();
			body.add("grant_type", "password");
			body.add("client_secret", keycloackClientSecret);
			body.add("username", adminUserName);
			body.add("password", adminPassword);
			body.add("client_id", "admin-cli");
			
			HttpEntity<MultiValueMap<String, String>> entity=new HttpEntity<>(body,header);
			
			@SuppressWarnings("rawtypes")
			HashMap response=restTemplate.postForObject(keycloackUrl+"/realms/master/protocol/openid-connect/token",entity, HashMap.class);
			
			String adminAccessToken=response.get("access_token").toString();
			
			HttpHeaders httpHeaders=new HttpHeaders();
			httpHeaders.set("Authorization", "Bearer "+adminAccessToken);
			
			ResponseEntity<Object> userResponse = restTemplate.exchange(
					keycloackUrl + "/admin/realms/crmbot/clients/" + crmbotClientId + "/roles/" + "supervisor" + "/users",
					HttpMethod.GET, new HttpEntity<>(httpHeaders), Object.class);
			
			List<String> supervisors = new ArrayList<>();
			try {
				@SuppressWarnings("unchecked")
				List<LinkedHashMap<String, Object>> userList = (List<LinkedHashMap<String, Object>>) userResponse
						.getBody();
				for (LinkedHashMap<String, Object> userMap : userList) {
					supervisors.add(userMap.get("username").toString());
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
			
			
			
			int rowIdx = 1;
			for (ActiveTask task : activeTasks) {
				
				List<Comments> comments=commentsService.getByActiveTask(task.getId());
				CommentStats stats = getCommentStats(comments, supervisors);
				

				Row row = sheet.createRow(rowIdx++);
				row.createCell(0).setCellValue(rowIdx - 1);
				LocalDateTime createdOn = convertGmtToIst(task.getCreatedOn());
				row.createCell(1)
						.setCellValue(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(createdOn));
				row.createCell(2).setCellValue(task.getLeadName());
				row.createCell(3).setCellValue(task.getPhoneNumber());
				row.createCell(4).setCellValue("");
				row.createCell(5).setCellValue(task.getArea());
				row.createCell(6).setCellValue(task.getCourse());
				row.createCell(7).setCellValue("");
				row.createCell(8).setCellValue(task.getCollege());
				row.createCell(9).setCellValue(task.getLeadPlatform());
				row.createCell(10).setCellValue(task.getCampaign());

				row.createCell(11).setCellValue(task.getRefferenceName());
				row.createCell(12).setCellValue(task.getRerefferenceNo());
				row.createCell(13).setCellValue(task.getRemark());
				try {
					row.createCell(14).setCellValue(task.getManagerName());
					row.createCell(15).setCellValue(task.getTelecallerName());
					row.createCell(16).setCellValue(task.getCounsellorName());
				}catch(Exception e) {
					e.printStackTrace();
				}
				
				/*HttpHeaders header=new HttpHeaders();
				MultiValueMap<String,String> body= new  LinkedMultiValueMap<>();
				body.add("grant_type", "password");
				body.add("client_secret", keycloackClientSecret);
				body.add("username", adminUserName);
				body.add("password", adminPassword);
				body.add("client_id", "admin-cli");
				
				HttpEntity<MultiValueMap<String, String>> entity=new HttpEntity<>(body,header);
				
				@SuppressWarnings("rawtypes")
				HashMap response=restTemplate.postForObject(keycloackUrl+"/realms/master/protocol/openid-connect/token",entity, HashMap.class);
				
				String adminAccessToken=response.get("access_token").toString();
				
				HttpHeaders httpHeaders=new HttpHeaders();
				httpHeaders.set("Authorization", "Bearer "+adminAccessToken);
				row.createCell(14).setCellValue(task.getManagerName());
				row.createCell(15).setCellValue(task.getTelecallerName());
				row.createCell(16).setCellValue(task.getCounsellorName());
				if(task.getManagerName()!=null && task.getManagerName().length()>0) {
					ResponseEntity<List> userResponse=restTemplate.exchange(keycloackUrl+"/admin/realms/crmbot/users?username="+task.getManagerName(),HttpMethod.GET,new HttpEntity<>(httpHeaders),List.class);
					try {
						
						LinkedHashMap<String , Object> map=(LinkedHashMap<String, Object>) userResponse.getBody().get(0);
						row.createCell(14).setCellValue(map.get("firstName")+" "+map.get("lastName"));
					}catch(Exception e) {
						e.printStackTrace();
					}
					
					
				}
				
				if(task.getTelecallerName()!=null && task.getTelecallerName().length()>0) {
					ResponseEntity<List> userResponse=restTemplate.exchange(keycloackUrl+"/admin/realms/crmbot/users?username="+task.getTelecallerName(),HttpMethod.GET,new HttpEntity<>(httpHeaders),List.class);
					try {
						LinkedHashMap<String , Object> map=(LinkedHashMap<String, Object>) userResponse.getBody().get(0);
						row.createCell(15).setCellValue(map.get("firstName")+" "+map.get("lastName"));
					}catch(Exception e) {
						e.printStackTrace();
					}
					
					
				}
				
				if(task.getCounsellorName()!=null && task.getCounsellorName().length()>0) {
					ResponseEntity<List> userResponse=restTemplate.exchange(keycloackUrl+"/admin/realms/crmbot/users?username="+task.getCounsellorName(),HttpMethod.GET,new HttpEntity<>(httpHeaders),List.class);
					try {
						LinkedHashMap<String , Object> map=(LinkedHashMap<String, Object>) userResponse.getBody().get(0);
						row.createCell(16).setCellValue(map.get("firstName")+" "+map.get("lastName"));
					}catch(Exception e) {
						e.printStackTrace();
					}
					
					
				}*/
				

				List<CounsellingDetails> counsellingDetails = counsellingDetailsService.getByActiveTask(task.getId());

				if (counsellingDetails != null) {
					row.createCell(17).setCellValue(counsellingDetails.size());
					if(counsellingDetails.size()>0) {
						row.createCell(18).setCellStyle(dateCellStyle);
						try {
							if(counsellingDetails.get(counsellingDetails.size()-1).getCreatedOn()!=null) {
								LocalDateTime counsellingDate = convertGmtToIst(counsellingDetails.get(counsellingDetails.size()-1).getCreatedOn());
								row.getCell(18).setCellValue(counsellingDate);
							}else {
								row.getCell(18).setCellValue("");
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						
					}
//						Cell cell = row.createCell(18);
//						cell.setCellValue(counsellingDetails.get(counsellingDetails.size()-1).getCreatedOn()); 
						
//						cell.setCellStyle(dateCellStyle);
				}

				if (task.getScheduleTime() != null) {
					//LocalDateTime scheduledTime = convertGmtToIst(task.getScheduleTime());
					row.createCell(19).setCellValue(
							DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(task.getScheduleTime()));
				}
					
				else
					row.createCell(19).setCellValue("");
				row.createCell(20).setCellValue(task.getStatus());

				CloseTask closedTask = closeTaskService.getByActiveTask(task.getId());

				if (closedTask != null) {
					if (closedTask.getIsConverted() != null)
						row.createCell(21).setCellValue(closedTask.getIsConverted()?"Converted":"Not Converted");
					row.createCell(22).setCellValue(closedTask.getRemark());
					row.createCell(23).setCellValue(closedTask.getClosingId()+closedTask.getId());
					row.createCell(24).setCellStyle(dateCellStyle);
					if(closedTask.getCreatedOn()!=null) {
						LocalDateTime admissionDate = convertGmtToIst(closedTask.getCreatedOn());
						row.getCell(24).setCellValue(admissionDate);
					}else {
						row.getCell(24).setCellValue("");
					}
					
				}
				if(comments.size()>0) {
					row.createCell(25).setCellValue(comments.get(comments.size()-1).getComment());
					row.createCell(26).setCellValue(comments.get(comments.size()-1).getUserName());
				}
				row.createCell(27).setCellValue(task.getLeadType());
				if(task.getIsSeatConfirmed()==null) {
					row.createCell(28).setCellValue("Not Confirmed");
				}else
					row.createCell(28).setCellValue(task.getIsSeatConfirmed()?"Confirmed":"Not Confirmed");
				
				if(stats!=null && stats.getMostRecent()!=null && stats.getCount()>0) {
					row.createCell(29).setCellValue(stats.getMostRecent().getComment());
					LocalDateTime commentCreatedOn = convertGmtToIst(stats.getMostRecent().getCreatedOn());
					row.createCell(30)
							.setCellValue(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(commentCreatedOn));
					row.createCell(31).setCellValue(stats.getCount());
					row.createCell(32).setCellValue(stats.getMostRecent().getUserName());
				}
			}
			
			workbook.write(out);
			return new ByteArrayInputStream(out.toByteArray());
		}
	}
	public LocalDateTime convertGmtToIst(LocalDateTime gmtDateTime) {
        // Convert LocalDateTime in GMT to ZonedDateTime
        ZonedDateTime gmtZoned = gmtDateTime.atZone(ZoneId.of("GMT"));

        // Convert to IST
        ZonedDateTime istZoned = gmtZoned.withZoneSameInstant(ZoneId.of("Asia/Kolkata"));

        // Return as LocalDateTime in IST (without zone info)
        return istZoned.toLocalDateTime();
    }
}