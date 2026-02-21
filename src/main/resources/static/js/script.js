let currentPage = 0;
let currentPageAllTask = 0;
let currentPageNoComments = 0;
let currentPageTodaysSchedule = 0;
let currentPageWithoutSchedule = 0;
let currentPageCounsellingDone = 0;
let currentPageCounsellingNotDone = 0;
const pageSize = 20;
var messages = []
let api='filterDefault'
	function showLoader() {
    	document.getElementById("loader").classList.remove("hidden");
  	}

  function hideLoader() {
    document.getElementById("loader").classList.add("hidden");
  }
  function setApi(apiToSet){
	$(".tab-filter").removeClass("active")
	$("#"+apiToSet+"Btn").addClass("active")
	api = apiToSet
	
	fetchTasks(currentPageAllTask)
	populateTabCount()
	
  }
  function fetchTasks(page = 0) {
	showLoader();
    const year = $('#yearSelect').val();
    const month = $('#monthSelect').val();
    const day = $('#daySelect').val();
    const course = $('#courseType').val();
    const leadType = $('#leadType').val();
	const today = new Date();

    $.ajax({
      url: '/lead-summary/'+api,
      method: 'GET',
      data: {
        year: year || null,
        month: month || null,
        day: day || null,
        course: course || null,
        leadType: leadType || null,
        page: page,
        size: pageSize
      },
      success: function(response) {
		hideLoader(); 
		const container = $('#taskCardsContainer'); // Make sure this container exists in your HTML
		 container.empty();

		  if (response.content.length === 0 && currentPage > 0) {
		    currentPage--;
		    return;
		  }

		  response.content.forEach(task => {
		      const name = task.leadName || 'Unknown';
		      const phone = task.phoneNumber || 'Not Available';
		      const date = task.scheduleTime ? new Date(task.scheduleTime).toLocaleDateString('en-GB') : 'Not Scheduled';
		      const counsellor = task.counsellorName || 'Not Assigned';
		      const counsellingStatus = task.isCounsellingDone ? 'Done' : 'Not Done';
		      const course = task.course || 'N/A';
		      const leadType = task.leadType || 'N/A';
			  let isComplete = isTaskCompleted(task.id)
			  let classToAdd ="bg-green-100"
			  if(!isComplete){
				classToAdd=""
			  }
			  
			  
			  let messageDropdown = '<select id="whatsappMessageParent_'+task.id+'" class="d-none whatsappMessageParent" style="z-index: 99999999;position: absolute;right: 30%;background: #f4f4f4;padding: 10px;border-radius: 5px;border: 1px solid #f0efef;">'
			 						+'<option value="">select message</option>'
			  for(let i=0;i<messages.length;i++){
				debugger
				messageDropdown+= '<option  value="'+messages[i]["text"]+'">'+messages[i]["messageName"]+'</option>'
			  }
			  messageDropdown+='</select>'
		      const card = `
		      <div class="bg-white rounded-lg p-6 shadow-md flex justify-between items-start mb-4 transition-transform transform hover:scale-105 ${classToAdd}" id="task_${task.id}">
		        <div class="flex flex-col space-y-2">
		          <h2 class="text-xl font-bold text-blue-600 cursor-pointer hover:underline">${name}&nbsp;</h2>
		          <p class="text-gray-600 cursor-pointer" id="phoneNumber_${task.id}" title="Click to copy and go to tasks" onclick="copyNumberAndNavigate(${phone})">${phone}</p>
		          <p class="text-gray-500">Scheduled: ${date}</p>
		          <p class="text-gray-500">Counsellor: ${counsellor}</p>
		          <p class="text-sm font-semibold ${task.isCounsellingDone ? 'text-green-600' : 'text-red-600'}">Counselling: ${counsellingStatus}</p>
		          <span class="text-xs font-bold px-2 py-1 rounded-full"></span>
		          <div class="mt-2 flex items-center">
		            <input type="checkbox" class="mr-2 completeCheck" data-id="${task.id}"><label>Task Completed?</label>
		          </div>
		          <div class="flex gap-2 mt-2" style="position:'relative'">
		            <button class="bg-blue-500 hover:bg-blue-700 text-white py-2 px-4 rounded-md" onclick="openPopup(${task.id})">View</button>
		            <a class="bg-green-500 hover:bg-green-700 text-white py-2 px-4 rounded-md flex items-center justify-center" href="tel:${phone}">
		              <i class="fas fa-phone"></i>
		            </a>
		            <a class="bg-green-500 hover:bg-green-700 text-white py-2 px-4 rounded-md flex items-center justify-center whatsAappBtn " id="whatsAappBtn_${task.id}"  target="_blank"> 
		              <i class="fab fa-whatsapp">
					  </i>
					  </a>
					${messageDropdown}
		          </div>
		        </div>
		        <div class="flex flex-col items-end justify-between space-y-2">
		          <p class="text-gray-500 font-medium">Course: ${course}</p>
		          <p class="text-gray-500 font-medium">Lead Type: ${leadType}</p>
		        </div>
		      </div>
		      `;

		      container.append(card);
		    });
			renderPagination(response.number, response.totalPages);
			$("#leads-counter").text('Total Leads : '+response.totalElements)
		  $('#pageInfo').text(`Page ${response.number + 1} of ${response.totalPages} | Total Tasks: ${response.totalElements}`);
      },
      error: function() {
		hideLoader(); 
        alert('Failed to fetch tasks.');
      }
    });
  }	

$(document).ready(function() {
	getMessages()
	populateTabCount()
	
	
	$('#yearSelect').change(function() {
      var selectedYear = $(this).val();

      if (!selectedYear) {
        $('#monthResults').empty();
        return;
      }

      $.ajax({
        url: '/lead-summary/monthly-count',
        method: 'GET',
        data: { year: selectedYear },
        success: function(response) {
          // response.monthlyCounts is expected to be an object like { "1": 12, "2": 8, ... }
		  var months = response.monthlyCounts;

	      // Start building the select HTML
	      var html = '<select id="monthSelect" name="month"  class="bg-white border border-gray-300 rounded-lg py-2 px-4  appearance-none leading-normal"  autocomplete="off">';
	      html += '<option value=""  selected>All Month</option>';

	      // Iterate through months object and add options
	      for (var month in months) {
	        if (months.hasOwnProperty(month)) {
	          var monthNum = parseInt(month, 10);
	          var monthName = new Date(0, monthNum - 1).toLocaleString('default', { month: 'long' });
	          html += '<option value="' + monthNum + '">' + monthName + ' (' + months[month] + ' tasks)</option>';
	        }
	      }
	      html += '</select>';

	      // Replace the div content with the select dropdown
	      $('#monthResults').html(html);
		  $('#monthSelect').change(function() {
              var selectedMonth = $(this).val();

              if (!selectedMonth) {
                $('#dayResults').empty();
                return;
              }

              $.ajax({
                url: '/lead-summary/daily-count',
                method: 'GET',
                data: { 
                  year: selectedYear,
                  month: selectedMonth
                },
                success: function(response) {
                  var days = response.dailyCounts;
                  var htmlDays = '<select id="daySelect" name="day" class="bg-white border border-gray-300 rounded-lg py-2 px-4  appearance-none leading-normal"  autocomplete="off">';
                  htmlDays += '<option value="" selected>All Day</option>';

                  for (var day in days) {
                    if (days.hasOwnProperty(day)) {
                      htmlDays += '<option value="' + day + '">Day ' + day + ' (' + days[day] + ' tasks)</option>';
                    }
                  }

                  htmlDays += '</select>';

                  $('#dayResults').html(htmlDays);
                },
                error: function() {
                  $('#dayResults').html('<p>Error retrieving daily data.</p>');
                }
              });

            });
        },
        error: function() {
          $('#monthResults').html('<p>Error retrieving months data.</p>');
        }
      });
    });
	
	$('#applyFilter').click(function () {
      const year = $('#yearSelect').val();
      const month = $('#monthSelect').val();
      const day = $('#daySelect').val();
      const course = $('#courseType').val();
      const leadType = $('#leadType').val();
	  populateTabCount()
      $.ajax({
        url: '/lead-summary/filtered-count', // Adjust if your endpoint differs
        method: 'GET',
        data: {
          year: year,
          month: month,
          day: day,
          course: course,
          leadType: leadType
        },
        success: function (response) {
			$("#totalLeads").text(response.totalTasks)
			$("#counsellingDone").text(response.counselledTasks)
			$("#admissionDone").text(response.admissionDoneTasks)
			$("#dPharmAdmissions").text(response.dPharmAdmissions)
			$("#bPharmAdmissions").text(response.bPharmAdmissions)
			$("#gmnAdmissions").text(response.gmnAdmissions)
			$("#otherAdmissions").text(response.otherAdmissions)
          
        },
        error: function () {
          //$('#summaryTable').html('<p>Error fetching task statistics.</p>');
        }
      });
    });
	
   

	  $('#applyFilter').click(function() {
	    currentPage = 0;
		if(api=='filterDefault'){
			currentPageAllTask = currentPage
		}
		else if(api=='without-comments'){
			currentPageNoComments = currentPage
		}
		else if(api=='todaysSchedule'){
			currentPageTodaysSchedule = currentPage
		}
		else if(api=='withoutSchedule'){
			currentPageWithoutSchedule = currentPage
		}
		else if(api=='counsellingDone'){
			currentPageCounsellingDone = currentPage
		}
		else if(api=='counsellingNotDone'){
			currentPageCounsellingNotDone = currentPage
		}
	    fetchTasks(currentPage);
	  });
	
	  $('#prevPage').click(function() {
	    if (currentPage > 0) {
	      currentPage--;
		  if(api=='filterDefault'){
	  			currentPageAllTask = currentPage
	  		}
	  		else if(api=='without-comments'){
	  			currentPageNoComments = currentPage
	  		}
	  		else if(api=='todaysSchedule'){
	  			currentPageTodaysSchedule = currentPage
	  		}
	  		else if(api=='withoutSchedule'){
	  			currentPageWithoutSchedule = currentPage
	  		}
	  		else if(api=='counsellingDone'){
	  			currentPageCounsellingDone = currentPage
	  		}
	  		else if(api=='counsellingNotDone'){
	  			currentPageCounsellingNotDone = currentPage
	  		}
	      fetchTasks(currentPage);
	    }
	  });
	
	  $('#nextPage').click(function() {
	    currentPage++;
		if(api=='filterDefault'){
			currentPageAllTask = currentPage
		}
		else if(api=='without-comments'){
			currentPageNoComments = currentPage
		}
		else if(api=='todaysSchedule'){
			currentPageTodaysSchedule = currentPage
		}
		else if(api=='withoutSchedule'){
			currentPageWithoutSchedule = currentPage
		}
		else if(api=='counsellingDone'){
			currentPageCounsellingDone = currentPage
		}
		else if(api=='counsellingNotDone'){
			currentPageCounsellingNotDone = currentPage
		}
	    fetchTasks(currentPage);
	  });
	
	  // Initialize year options dynamically
	  const now = new Date();
	  for (let y = now.getFullYear(); y >= now.getFullYear() - 5; y--) {
	    $('#yearSelect').append(`<option value="${y}">${y}</option>`);
	  }
	
	  // Add month and day selects
	  for (let m = 1; m <= 12; m++) {
	    $('#monthSelect').append(`<option value="${m}">${m}</option>`);
	  }
	  for (let d = 1; d <= 31; d++) {
	    $('#daySelect').append(`<option value="${d}">${d}</option>`);
	  }
	
	  
	  // First load
	  fetchTasks(currentPage);
	 
	  $("body").on("click","#downloadReport",function(){
		
		showLoader();
	    const year = $('#yearSelect').val();
	    const month = $('#monthSelect').val();
	    const day = $('#daySelect').val();
	    const course = $('#courseType').val();
	    const leadType = $('#leadType').val();
		const today = new Date();
			
		$.ajax({
		  url: '/lead-summary/report',
		  method: 'POST',
		  data: {
		    year: year || null,
		    month: month || null,
		    day: day || null,
		    course: course || null,
		    leadType: leadType || null,
		    tabName: api,
		  },
		  xhrFields: {
  	            responseType: 'blob'
  	        },
		  success: function(data) {
			hideLoader(); 
			try{
				debugger;
				const url = window.URL.createObjectURL(data);
			    const a = document.createElement('a');
			    a.style.display = 'none';
			    a.href = url;
			    // the filename you want
			    a.download = 'lead_summary_report.xlsx';
			    document.body.appendChild(a);
			    a.click();
			    window.URL.revokeObjectURL(url);
			}catch(err){
				console.log(err)
			}
		  }
	  	})
		
	  })
	  
  });
  
  function getMessages(){
	$.ajax({
	      url: '/crmbot/getmessages',
	      method: 'GET',
		  success:function(data){
			console.log(data)
			messages = data
		  },
		  error:function(err){
			console.log(err)
		  }
		  
	})
  }
  
  function populateTabCount(){
	const year = $('#yearSelect').val();
     const month = $('#monthSelect').val();
     const day = $('#daySelect').val();
     const course = $('#courseType').val();
     const leadType = $('#leadType').val();
	$.ajax({
	      url: '/lead-summary/tab-count',
	      method: 'GET',
	      data: {
	        year: year || null,
	        month: month || null,
	        day: day || null,
	        course: course || null,
	        leadType: leadType || null,
	        page: currentPage,
	        size: pageSize
	      },
	      success: function(response) {
			console.log(response)
			if(response){
				$("#totalLeadCount").text(response.totalLeadCount)
				$("#todaysScheduledCount").text(response.todaysScheduledCount)
				$("#notScheduledCount").text(response.notScheduledCount)
				$("#noCommentCount").text(response.noCommentCount)
				$("#counselledCount").text(response.counselledCount)
				$("#notCounselledCount").text(response.notCounselledCount)
			}
	      },
	      error: function() {
	        alert('Failed to fetch tasks.');
	      }
	    });
  }
  
  function openPopup(taskId){
	document.getElementById('popupModal').classList.remove('hidden');
	$.ajax({
	      url: '/lead-summary/taskDetails/'+taskId,
	      method: 'GET',
	      success: function(response) {
			console.log(response)
			$("#popupModal #popupDetails").html(response)
	      },
	      error: function() {
	        alert('Failed to fetch tasks.');
	      }
	    });
  }
  
  $("body").on("click","#searchLeadPopupBtn",function(){
	$("#searchLeadModal").removeClass("hidden")
  })
  
  $("body").on("click","#closePopupModal",function(){
	$('.modalClass').addClass('hidden');
  })
  
  function renderPagination(currentPage, totalPages) {
    const pagination = $('#pagination');
    pagination.empty();

    // Previous Button
    if (currentPage > 0) {
      pagination.append(`<button class="px-3 py-1 bg-gray-200 rounded hover:bg-gray-300" onclick="fetchTasks(${currentPage - 1})">Prev</button>`);
    }

    // Current and next 3 pages
    const maxButtons = 3;
    for (let i = currentPage; i < Math.min(currentPage + maxButtons, totalPages); i++) {
      pagination.append(`
        <button class="px-3 py-1 ${i === currentPage ? 'bg-blue-500 text-white' : 'bg-gray-100'} rounded hover:bg-blue-400 hover:text-white"
                onclick="fetchTasks(${i})">${i + 1}</button>
      `);
    }

    // Next Button
    if (currentPage < totalPages - 1) {
      pagination.append(`<button class="px-3 py-1 bg-gray-200 rounded hover:bg-gray-300" onclick="fetchTasks(${currentPage + 1})">Next</button>`);
    }
  }
  
  function markTaskCompleted(leadId) {
	
    $('#task_'+leadId).addClass('bg-green-100');;
     alert('Task marked as completed.');
     const expiryTime = Date.now() + 12 * 60 * 60 * 1000;
     const taskData = { completed: true, expires: expiryTime };
     try {
         localStorage.setItem(`task-completed-${leadId}`, JSON.stringify(taskData));
         console.log(`Task ${leadId} marked completed and stored in localStorage with expiry: ${new Date(expiryTime)}`);
     } catch (e) {
         console.error(`Error saving task to localStorage: ${e}`);
     }
 }
 
 function isTaskCompleted(leadId) {
     try {
         const taskData = localStorage.getItem(`task-completed-${leadId}`);
         if (taskData) {
             const { completed, expires } = JSON.parse(taskData);
             if (completed && Date.now() < expires) {
                 return true;
             } else {
                 localStorage.removeItem(`task-completed-${leadId}`);
             }
         }
     } catch (e) {
         console.error(`Error reading task from localStorage: ${e}`);
     }
     return false;
 }
 $("body").on('click',".whatsAappBtn",function(e){
	let id = this.id
	id = id.split("_")[1]
	$("#whatsappMessageParent_"+id).removeClass("d-none")
 })
 
 $('body').on("change",'.whatsappMessageParent',function(e){
	let id = this.id
	id = id.split("_")[1]
	if($("#whatsappMessageParent_"+id).val()!=""){
		$("#whatsappMessageParent_"+id).addClass("d-none")
		window.open('//api.whatsapp.com/send?phone=91'+$("#phoneNumber_"+id).text()+'&text='+$("#whatsappMessageParent_"+id).val(), '_blank');
	}
 		
 })
 
 $(document).on('click', function(e) {
   if (!($(e.target).closest('.whatsappMessageParent').length) && !($(e.target).closest('.whatsAappBtn').length)){
     // Clicked outside the #mySelect
     console.log('Clicked outside the select');
 	$(".whatsappMessageParent").addClass("d-none")
     // your logic here
   }
 });
 
 $("body").on('click',".completeCheck",function(e)
  {
             const checkbox = $(this);
			 let leadId = checkbox.data('id')
             if (checkbox.is(':checked')) {
                 if (confirm('Have you applied Schedule in CRM?')) {
                     if (confirm('Have you added comment?')) {
                         markTaskCompleted(leadId);
                     } else {
                         alert('Please go and add a comment first.');
                         checkbox.checked = false;
                     }
                 } else {
                     alert('Please go and add schedule first.');
                     checkbox.checked = false;
                 }
             } else {
                 if (confirm('Are you sure you want to unmark this task as completed?')) {
                     localStorage.removeItem(`task-completed-${leadId}`);
                     $("#task_"+leadId).removeClass('bg-green-100');
                 } else {
                     checkbox.checked = true;
                 }
             }
         })

 function copyNumberAndNavigate(number) {
     navigator.clipboard.writeText(number).then(() => {
         window.location.href = 'https://www.vmedify.com/crmbot/tasks';
		// window.location.href = 'tasks?phoneNumber='+number;
     }).catch(err => {
         console.error('Failed to copy phone number: ', err);
     });
 }