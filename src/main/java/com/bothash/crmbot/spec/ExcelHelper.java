package com.bothash.crmbot.spec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
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

			List<ActiveTask> activeTasks = new ArrayList<ActiveTask>();

			int rowNumber = 0;
			while (rows.hasNext()) {
				Row currentRow = rows.next();

				// skip header
				if (rowNumber == 0) {
					rowNumber++;
					continue;
				}

				Iterator<Cell> cellsInRow = currentRow.iterator();

				ActiveTask activeTask = new ActiveTask();
				FacebookLeads facebookLeads = new FacebookLeads();

				JSONArray jsonArray = new JSONArray();
				int cellIdx = 0;
				while (cellsInRow.hasNext()) {
					JSONObject jsonObject = new JSONObject();
					Cell currentCell = cellsInRow.next();

					switch (cellIdx) {
					case 0:
						activeTask.setLeadPlatform(currentCell.getStringCellValue());

						break;

					case 1:
						activeTask.setRefferenceName(currentCell.getStringCellValue());
						break;

					case 4:
						activeTask.setArea(currentCell.getStringCellValue());
						// tutorial.setDescription(currentCell.getStringCellValue());
						break;

					case 5:
						activeTask.setCourse(currentCell.getStringCellValue());
						// activeTask.setCreatedOn(currentCell.getBooleanCellValue());
						break;

					case 6:
						jsonObject.put("name", "phone_number");
						try {
							Double phoneNumber = currentCell.getNumericCellValue();
							DecimalFormat df = new DecimalFormat("#");
							df.setMaximumFractionDigits(11);
							System.out.println(df.format(phoneNumber));
							String values[] = new String[] { df.format(phoneNumber) };
							jsonObject.put("values", values);
							activeTask.setPhoneNumber( df.format(phoneNumber));
						}catch(Exception e) {
							try {
								String phoneNumber = currentCell.getStringCellValue();
								String values[] = new String[] { phoneNumber};
								jsonObject.put("values", values);
								activeTask.setPhoneNumber( phoneNumber);
							}catch(Exception e2) {
								e2.printStackTrace();
							}
						}
						
						
						break;

					case 7:
						jsonObject.put("name", "full_name");
						String values2[] = new String[] { currentCell.getStringCellValue() };
						activeTask.setLeadName(currentCell.getStringCellValue());
						jsonObject.put("values", values2);
						break;
					/*
					 * case 8: jsonObject.put("name", "full_name"); String values2[] =new String[]
					 * {currentCell.getStringCellValue()}; jsonObject.put("values", values2); break;
					 */

					default:
						break;
					}
					if (!jsonObject.isEmpty()) {
						jsonArray.put(jsonObject);
					}
					cellIdx++;
				}

				facebookLeads.setFieldData(jsonArray.toString());
				// facebookLeads =this.facebookLeadsService.save(facebookLeads);
				activeTask.setFacebookLeads(facebookLeads);
				activeTasks.add(activeTask);
			}

			workbook.close();
			// activeTaskService.saveAll(activeTasks);
			return activeTasks;
		} catch (IOException e) {
			throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
		}
	}

	public ByteArrayInputStream taskToExcel(List<ActiveTask> activeTasks) throws IOException {
		String[] COLUMNs = { "Sr. no", "Date & time", "Student name", "Mobile no", "Email", "Area", "Course Interested",
				"Requirement", "College name", "Platform", "Campaign", "Reference name", "Reference contact",
				"Latest remark", "Manager", "Telecaller", "Counsellor", "No. of counselling", "Scheduled date and time",
				"Status", "Converted/ Npt Converted", "Closing remark", "UID","Last Comment","Comment User Name","Lead type" };
		try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream();) {
			CreationHelper createHelper = workbook.getCreationHelper();

			Sheet sheet = workbook.createSheet("Task_Details");

			Font headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerFont.setColor(IndexedColors.BLUE.getIndex());

			CellStyle headerCellStyle = workbook.createCellStyle();
			headerCellStyle.setFont(headerFont);
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

			int rowIdx = 1;
			for (ActiveTask task : activeTasks) {
				
				List<Comments> comments=commentsService.getByActiveTask(task.getId());
				//String phoneNumber = "";
				//String email = "";
				//String requirement = "";
				/*try {
					JSONArray array = new JSONArray(task.getFacebookLeads().getFieldData());
					for (int i = 0; i < array.length(); i++) {
						JSONObject jsonObject = array.getJSONObject(i);
						if (jsonObject != null) {
							if (jsonObject.get("name").toString().equalsIgnoreCase("phone_number")
									|| jsonObject.get("name").toString().contains("number")) {
								phoneNumber = jsonObject.getJSONArray("values").getString(0);
							} else if (jsonObject.get("name").toString().contains("email")) {
								email = jsonObject.getJSONArray("values").getString(0);
							} else if (jsonObject.get("name").toString().contains("requirement")) {
								requirement = jsonObject.getJSONArray("values").getString(0);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}*/

				Row row = sheet.createRow(rowIdx++);
				row.createCell(0).setCellValue(rowIdx - 1);
				row.createCell(1)
						.setCellValue(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(task.getCreatedOn()));
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

				if (counsellingDetails != null)
					row.createCell(17).setCellValue(counsellingDetails.size());

				if (task.getScheduleTime() != null)
					row.createCell(18).setCellValue(
							DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(task.getScheduleTime()));
				else
					row.createCell(18).setCellValue("");
				row.createCell(19).setCellValue(task.getStatus());

				CloseTask closedTask = closeTaskService.getByActiveTask(task.getId());

				if (closedTask != null) {
					if (closedTask.getIsConverted() != null)
						row.createCell(20).setCellValue(closedTask.getIsConverted()?"Converted":"Not Converted");
					row.createCell(21).setCellValue(closedTask.getRemark());
					row.createCell(22).setCellValue(closedTask.getClosingId()+closedTask.getId());
				}
				if(comments.size()>0) {
					row.createCell(23).setCellValue(comments.get(comments.size()-1).getComment());
					row.createCell(24).setCellValue(comments.get(comments.size()-1).getUserName());
				}
				row.createCell(25).setCellValue(task.getLeadType());
			}
			
			workbook.write(out);
			return new ByteArrayInputStream(out.toByteArray());
		}
	}

}