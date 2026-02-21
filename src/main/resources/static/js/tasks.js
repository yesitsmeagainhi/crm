
var lastElementSorted=''
var currTaskType="myTask"
var currPage=0;
var prevMyTasks=-1
var isDownloadClicked=false
var isMainPage=false
var totalMyTasks=0;
function formatDate(date){
	let d=date
	d=new Date(d)
	let datestring = ("0" + d.getDate()).slice(-2) + "/" + ("0"+(d.getMonth()+1)).slice(-2) + "/" +d.getFullYear() + " " + ("0" + d.getHours()).slice(-2) + ":" + ("0" + d.getMinutes()).slice(-2);
	return datestring
}

$(document).ready(function($){
	navigator.serviceWorker.getRegistrations().then(function(registrations) {
	    registrations.forEach(function(registration) {
	        registration.unregister();
	    });
	});

	if ('serviceWorker' in navigator) {
			    // Register the service worker at the correct path
			    navigator.serviceWorker.register('/sw.js')
			        .then(function(registration) {
			            console.log('Service Worker registered with scope:', registration.scope);
			        })
			        .catch(function(error) {
			            console.log('Service Worker registration failed:', error);
			        });
			}
			
			(async () => {
			  // ----- SERVICE WORKER & PUSH -----
			  const PUBLIC_KEY = "BGwlv_PnnimH8XKXj5MebSIbIgAQqI2Pl-pc1xi9HjQrbHX2rAkfZ_my80140TKJHQKdIpMGi21vwRJjEUeP2kQ";

			  if ('serviceWorker' in navigator) {
			    const reg = await navigator.serviceWorker.register('/sw.js');

			    // ask permission once
			    if (Notification.permission !== 'granted')
			      await Notification.requestPermission();

			    // create or get existing subscription
			    let sub = await reg.pushManager.getSubscription();
				debugger
			    if (!sub) {
			      sub = await reg.pushManager.subscribe({
			        userVisibleOnly: true,
			        applicationServerKey: urlB64ToUint8(PUBLIC_KEY)
			      });
			      await fetch('/api/push/subscribe', {
			        method: 'POST',
			        headers: {'Content-Type':'application/json'},
			        body: JSON.stringify({
			          endpoint: sub.endpoint,
			          p256dh: btoa(String.fromCharCode(...new Uint8Array(sub.getKey('p256dh')))),
			          auth: btoa(String.fromCharCode(...new Uint8Array(sub.getKey('auth'))))
			        })
			      });
			    }
			  }

			})();
			
			function urlB64ToUint8(base64) {
			    const padded = base64.replace(/-/g,'+').replace(/_/g,'/');
			    const raw = atob(padded);
			    return Uint8Array.from([...raw].map(c=>c.charCodeAt(0)));
			  }
	
	$("#search-box").on('input', function (e) {
   // if (e.key === 'Enter' || e.keyCode === 13) {
	        let taskType=["allTask","myTask","completedTask","counselledTask",]
			for(let i=0;i<taskType.length;i++){
				 var filterRequests={}
				  filterRequests['leadPlatform']=$("#leadPlatform").val()
				   filterRequests['assignee']=$("#assignee").val()
				   filterRequests['fromDate']=$("#fromDate").val()
				   filterRequests['toDate']=$("#toDate").val()
				   filterRequests['leadName']=$("#search-box").val()
				   filterRequests['phoneNumber']=$("#search-box").val()
				   
				  $.ajax({
					url:'/crmbot/singletasktable?page='+(0)+"&size=25&sorting=createdOn&desc=true&taskType="+taskType[i],
					type:'PUT',
					contentType:'application/json',
					data:JSON.stringify(filterRequests),
					success:function(data){
						
						$("#"+taskType[i]+"Container_inner").html(data)
					}
				})	
			}
	   // }
	});
	
	$("#search-leadbtn").on('click', function (e) {
	   // if (e.key === 'Enter' || e.keyCode === 13) {
		 if($("#search-lead-box").val()){
			var filterRequests={}
					
		   filterRequests['leadName']=$("#search-lead-box").val()
		   filterRequests['phoneNumber']=$("#search-lead-box").val()
		   
		  $.ajax({
			url:'/crmbot/singletasktable?page='+(0)+"&size=25&sorting=createdOn&desc=true&taskType=searchLead",
			type:'PUT',
			contentType:'application/json',
			data:JSON.stringify(filterRequests),
			success:function(data){
				
				$("#searchLeadContainer_inner").html(data)
			}
		})	
		 }else{
			$("#searchLeadContainer_inner").empty()
		 }
		   // }
		});
	 
	
	
	$('body').on('click','.open-detail',function(e){
		$(".overlay").removeClass("d-none")
		let isClosed=false
		let isSearchLead=false
		isMainPage=false
		let searchLead = this.dataset.searchLead
		if(this.classList.contains("completed-task"))
			isClosed=true
		if(searchLead=='true'){
			isSearchLead=true
		}
		$.ajax({
			url:'/crmbot/detailpage?id='+this.id+"&role="+role+"&isClosed="+isClosed+"&isSearchLead="+isSearchLead,
			success:function(data){
				$(".overlay").addClass("d-none")
				$(".task-list-container").addClass("d-none")
				$(".task-detail-page").removeClass("d-none")
				$(".task-detail-page").html(data)
			}
		})
	})
	
	$("body").on('click','#task-back-btn',function(e){
		isMainPage=true
		$(".task-list-container").removeClass("d-none")
        $(".task-detail-page").addClass("d-none")
        let allInputs=$("input,select,textarea,button,i")
		for(let i=0;i<allInputs.length;i++){
			//if(allInputs[i].id!="iconNavbarSidenav2" && allInputs[i].id!="search-box" && allInputs[i].getAttribute("name")!="tabs")
				allInputs[i].disabled=false
		}
	})
	
	$('body').on('change','.role-select',function(e){
	debugger;
		if(this.value!="0"){
			$(".user-select").addClass("d-none")
			document.querySelectorAll("#"+this.value).forEach((item,index)=>{
				$(item).toggleClass("d-none")
			})
			$("#move-remark").removeClass("d-none")
			$("#move-remark").addClass("d-inline")
		}else{
			$(".user-select").addClass("d-none")
			$("#move-remark").addClass("d-none")
		}
	})
	
	$('body').on('change','.role-select2',function(e){
		debugger;
			if(this.value!="0"){
				$(".user-select2").addClass("d-none")
				document.querySelectorAll("#"+this.value+'2').forEach((item,index)=>{
					$(item).toggleClass("d-none")
				})
				$("#move-remark").removeClass("d-none")
				$("#move-remark").addClass("d-inline")
			}else{
				$(".user-select2").addClass("d-none")
				$("#move-remark").addClass("d-none")
			}
		})
	
	$('body').on('change','.role-select-scheduler',function(e){
		if(this.value!="0"){
			$(".user-select-scheduler").addClass("d-none")
			$("#"+this.value+"-scheduler").toggleClass("d-none")
		}else{
			$(".user-select-scheduler").val("0")
			$(".user-select-scheduler").addClass("d-none")
			$("#scheduler-comment").addClass("d-none")
			$("#scheduler-submit").addClass("d-none")
		}
	})
	$('body').on('change','.user-select-scheduler',function(e){
		if(this.value!="0"){
			$("#scheduler-comment").removeClass("d-none")
			$("#scheduler-submit").removeClass("d-none")
		}else{
			$("#scheduler-comment").addClass("d-none")
			$("#scheduler-submit").addClass("d-none")
		}
	})
	
	$('body').on('click','.move-ticket',function(e){
		ticketForwarded=false
		debugger;
		const selectedRole=document.querySelectorAll(".role-select")[1].value
		//alert(selectedRole)
		if(!this.getAttribute('disabled') && document.querySelectorAll(".role-select")[1].value!="0" && 
				document.querySelectorAll("#"+document.querySelectorAll(".role-select")[1].value)[1].value!="0" && $("#move-remark").val().length>0 && ($("#leadType").val().length>0 && $(".task-schedulerName").text().length>0 || role=="admin")){

			const selectedUser=document.querySelectorAll("#"+selectedRole)[1].value

			var ticketFwdRequest={
				
			}
			var selectedOption2= $("#"+selectedRole+ " option[value='" + selectedUser + "']");
			if (selectedOption2.length > 0) {
	            var selectedOptionName2 = selectedOption2.attr("name");
	        } 
			ticketFwdRequest['userName']=selectedOptionName2
			ticketFwdRequest['userEmail']=selectedUser
			ticketFwdRequest['userGroup']=selectedRole
			ticketFwdRequest['taskId']=taskId
			ticketFwdRequest['forwarderUserName']=userName
			ticketFwdRequest['forwarderUserEmail']=userEmail
			ticketFwdRequest['forwarderUserId']=userId
			ticketFwdRequest['remark']=$("#move-remark").val()
			$.ajax({
				url:'/crmbot/flow/fwd',
				contentType:'application/json',
				type:'PUT',
				data:JSON.stringify(ticketFwdRequest),
				success:function(data){
					$(".move-ticket").attr("disabled",true)
					$(".move-popup-button").attr("disabled",true)
					$(".assign-popup").addClass("d-none")
					$(".ack-popup").removeClass("d-none")
					$("#ticket-acknowledgement").text(data.status)
					ticketForwarded=true
					console.log(ticketForwarded+"4")
					//location.reload()
				}
			})
		}else {
			if($("#leadType").val()=="")
				alert("Please select lead type")
			else if($(".task-schedulerName").text()==""){
				alert("Please add scheduler")
			}
		}
	})
	
	/*$('body').on('change', '.comment-search-dropwown', function(e) {
		let selectedValue = this.value
		$('.comment-dropdown-header').css("bottom","45px")
		$(".comment-textarea").removeClass("d-none")
		if(selectedValue == "Others"){
			$('.comment-dropdown-header').css("bottom","45px")
			$(".comment-textarea").removeClass("d-none")
		}else{
			$('.comment-dropdown-header').css("bottom","8px")
			$(".comment-textarea").addClass("d-none")
		}
	})*/
	
	
	
	var isCommentAlreadyClicked=false
	$('body').on('click', '.comments-send-button', function(e) {
		debugger;
		if((($(".comment-search-dropwown").val().length>0 && $(".comment-search-dropwown").val()!="Others") || ($(".comment-search-dropwown").val()=="Others" && $(".comment-textarea").val().length>0)) && !isCommentAlreadyClicked){
			isCommentAlreadyClicked=true
			let comments={};
		comments['userName']=userName
		comments['userEmail']=userEmail
		comments['userId']=userId
		
		let commentHeader =$(".comment-search-dropwown").val()
		let commentedText=$(".comment-textarea").val()
		
		/*if($(".comment-search-dropwown").val()=="Others"){
			commentedText=$(".comment-textarea").val()
		}*/
		
		comments['comment']=commentedText
		comments['commentHeader']=commentHeader
		$(".comments-send-button").attr("disabled",true)
		$.ajax({
			url:'/crmbot/comments/save/'+taskId,
			contentType:'application/json',
			type:'POST',
			data:JSON.stringify(comments),
			success:function(data){
				isCommentAlreadyClicked=false
				$(".comment-textarea").val("")
				$(".comments-send-button").removeAttr("disabled")
				$(".no-comments-alert").css("display","none")
				let d=data.createdOn
				d=new Date(d)
				let datestring = ("0" + d.getDate()).slice(-2) + "/" + ("0"+(d.getMonth()+1)).slice(-2) + "/" +d.getFullYear() + " " + ("0" + d.getHours()).slice(-2) + ":" + ("0" + d.getMinutes()).slice(-2);
				let li='<li class="list-group-item border-0 d-flex p-4 mb-2 bg-gray-100 border-radius-lg" ><div class="d-flex flex-column w-100">'
				li+='<span class="mb-1 text-xs  ">Name: <span class="text-dark font-weight-bold ms-sm-2 comment_name" >'+data.userName+'</span><span class=" ms-sm-2 font-weight-bold comment_date float-right" >'+datestring+'</span>'
				li+='</span><span class="text-xs mb-1">Comment Type: <span class="text-dark ms-sm-2 font-weight-bold comment_data" >'+data.commentHeader+'</span></span> '
				li+='</span><span class="text-xs">Comment: <span class="text-dark ms-sm-2 font-weight-bold comment_data" >'+data.comment+'</span></span> </div></li>'
				$(".comments-ul-list").append(li)
			}
		})
		}
	})
	
	$('body').on('click','.scheduler',function(e){
		if($(".scheduler").text()=="timer"){
			$(".scheduler").text("timer_off")
			$(".scheduler-card").removeClass("d-none")
			}
		else{
			$(".scheduler").text("timer")
			$(".scheduler-card").addClass("d-none")
		}
	})
	
	$('body').on('click','#scheduler-submit',function(e){
		if($("#scheduler-date").val()){
			let schedulerRequest={}
			schedulerRequest['scheduleTime']=$("#scheduler-date").val()
			schedulerRequest['taskId']=taskId
			schedulerRequest['comment']=$("#scheduler-comment").val()
			schedulerRequest['schedulerName']=userName
			schedulerRequest['schedulerEmail']=userEmail
			schedulerRequest['schedulerUserId']=userId
			$.ajax({
					url:'/crmbot/flow/schedule',
					contentType:'application/json',
					type:'PUT',
					data:JSON.stringify(schedulerRequest),
					success:function(data){
						console.log(data)
						$(".scheduler-title").text("Scheduled")
						$(".close-schedule-btn").removeClass("d-none")
						$(".schedule-card-body").addClass("d-none")
						$(".task-schedulerName").text(data.schedulerName)
						$(".task-scheduleTime").text(formatDate(data.scheduleTime))
						$(".task-scheduleComment").text(data.scheduleComment)
						$(".existing-schedule-card-body").removeClass("d-none")
					}
				})
		}
		
	})
	
	$('body').on('click','.complete-schedule',function(e){
		let schedulerRequest={}
		schedulerRequest['taskId']=taskId
		schedulerRequest['schedulerName']=userName
		schedulerRequest['schedulerEmail']=userEmail
		schedulerRequest['schedulerUserId']=userId
		$.ajax({
				url:'/crmbot/flow/complete-schedule',
				contentType:'application/json',
				type:'PUT',
				data:JSON.stringify(schedulerRequest),
				success:function(data){
					console.log(data)
					$(".scheduler-title").text("Scheduler")
					$(".task-schedulerName").text("")
					$(".close-schedule-btn").addClass("d-none")
					$(".schedule-card-body").removeClass("d-none")
					$(".existing-schedule-card-body").addClass("d-none")
				}
			})
	})
	
	$('body').on('click',".close-ticket",function(e){
		
		let isChecked=$('input[name="converted"]:checked').val();
		let isSeatConfirmed=$('input[name="seatConfirmed"]:checked').val();
		if(isSeatConfirmed == 'on'){
			isSeatConfirmed = true
		}
		console.log($("#closing-remark").val())
		let admissionDone = $(".admission-done-counsellors").val()
		if(admissionDone=='Other'){
			admissionDone = $("#admission-done-others").val()
		}
		if(( isChecked=="true" || isChecked=="false" || isSeatConfirmed) && ($("#closing-remark").val().length>0)){
			if((isChecked == 'true' && admissionDone) || isChecked=="false"|| isSeatConfirmed){
				let closeRequest={}
					closeRequest['taskId']=taskId
					closeRequest['remark']=$("#closing-remark").val()
					closeRequest['userName']=userName
					closeRequest['userEmail']=userEmail
					closeRequest['userId']=userId
					closeRequest['isConverted']=isChecked
					closeRequest['closeTask']=false
					closeRequest['isSeatConfirmed']=isSeatConfirmed
					closeRequest['admissionDoneBy'] = $("#counsellor_admission_done").val()
					console.log(this.id)
					debugger;
					if(this.id!="saveClosingDetails" || isChecked=="true"){
							closeRequest['closeTask']=true
						
					}
						
					$.ajax({
						url:'/crmbot/flow/close-task',
						contentType:'application/json',
						type:'PUT',
						data:JSON.stringify(closeRequest),
						success:function(data){
							ticketForwarded=true
							let alertText=data
							
							
							$(".cd-popup-trigger3").click()	
							$(".cd-popup-trigger2").click()	
							$(".cd-popup-container2").addClass("d-none")
							$("#ticket-acknowledgement").text(alertText)
							$(".ack-popup").removeClass("d-none")
							$(".closing-popup").addClass("d-none")
							let allInputs=$("input,select,textarea,button,i")
								for(let i=0;i<allInputs.length;i++){
									if(allInputs[i].id!="iconNavbarSidenav2" )
										allInputs[i].disabled=true
								}
						}
				})
			}			
			else{
					alert("Enter all details")
				}
			}else{
				alert("Enter all details")
			}
	})
	
	$('body').on('click',"#saveCompleteDetails",function(e){
		if(($("#closing-remark").val().length>0)){
			let closeRequest={}
				closeRequest['taskId']=taskId
				closeRequest['remark']=$("#closing-remark").val()
				closeRequest['userName']=userName
				closeRequest['userEmail']=userEmail
				closeRequest['userId']=userId
				closeRequest['closeTask']=false
				console.log(this.id)
				
					
				$.ajax({
					url:'/crmbot/flow/complete-task',
					contentType:'application/json',
					type:'PUT',
					data:JSON.stringify(closeRequest),
					success:function(data){
						ticketForwarded=true
						let alertText=data
						
						
						$(".cd-popup-trigger3").click()	
						$(".cd-popup-trigger2").click()	
						$(".cd-popup-container2").addClass("d-none")
						$("#ticket-acknowledgement").text(alertText)
						$(".ack-popup").removeClass("d-none")
						$(".closing-popup").addClass("d-none")
						let allInputs=$("input,select,textarea,button,i")
							for(let i=0;i<allInputs.length;i++){
								if(allInputs[i].id!="iconNavbarSidenav2" )
									allInputs[i].disabled=true
							}
					}
				})
			}else{
				alert("Enter all details")
			}
	})
	
	$('body').on('click',"#saveCounsellingDetails",function(e){
		debugger
		let isChecked=$('input[name="counselled"]:checked').val();
		if(( isChecked=="true" || isChecked=="false") ){
				let counsellingDetails={}
				let cousellingRemark =""
				let cousellingDoneBy = $(".counselling-done-counsellors").val()
				let cousellingDoneOthers = $("#counselling-done-others").val()
				if(isChecked == "true"){
					
					if($("#feesPitched").val() && $("#course_counselling").val() && $(".counselling-done-remark").val() && cousellingDoneBy){
						counsellingDetails['feesPitched']=$("#feesPitched").val()
						counsellingDetails['course']=$("#course_counselling").val()
						if($(".counselling-done-remark").val() && $(".counselling-done-remark").val()!="Others"){
							cousellingRemark = $(".counselling-done-remark").val()
						}else{
							cousellingRemark = $("#counselling-done-remark-textbox").val()
						}
					}else{
						alert("Enter all details")
						return
					}
					
						
				}else{
					if($(".counselling-not-done-remark").val() && $(".counselling-not-done-remark").val()!="Others"){
						cousellingRemark = $(".counselling-not-done-remark").val()
					}else if($(".counselling-not-done-remark").val() =="Others"){
						cousellingRemark = $("#counselling-done-remark-textbox").val()
					}else{
						alert("Enter all details")
						return
					}
				}
				
				
				
				
				
				counsellingDetails['remark']=cousellingRemark
				counsellingDetails['userName']=userName
				counsellingDetails['userEmail']=userEmail
				counsellingDetails['isCounselled']=isChecked
				counsellingDetails['counsellingDoneBy'] = cousellingDoneBy
				counsellingDetails['cousellingDoneOthers'] = cousellingDoneOthers
				
				$.ajax({
					url:'/crmbot/flow/counselling/save?activeTaskId='+taskId,
					contentType:'application/json',
					type:'POST',
					data:JSON.stringify(counsellingDetails),
					success:function(data){
						ticketForwarded=true
						let alertText="Details Saved"
						
						
						$(".cd-popup-trigger3").click()	
						$(".cd-popup-trigger2").click()	
						$(".cd-popup-container2").addClass("d-none")
						$("#ticket-acknowledgement").text(alertText)
						$(".ack-popup").removeClass("d-none")
						$(".closing-popup").addClass("d-none")
					}
				})
			}else{
				alert("Enter all details")
			}
	})
	
	$('body').on('click','.sort',function(e){
		console.log(this.id)
		console.log(this.getAttribute("name"))
		let eleName=this.getAttribute("name")
		let order=true
		if(lastElementSorted==eleName){
			order=false
			lastElementSorted=''
		}else
			lastElementSorted=eleName
		let pageToLoad=0
		let parentElement='allTaskContainer'
		if(eleName=="allTask"){
			pageToLoad=0
			parentElement='allTaskContainer_inner'
		  }else if(eleName=="myTask"){
				pageToLoad=0
				parentElement='myTaskContainer_inner'
		  }else if(eleName=="completedTask"){
				pageToLoad=0
				parentElement='completedTaskContainer_inner'
		  }
		  var filterRequests={}
			  filterRequests['leadPlatform']=$("#leadPlatform").val()
			   filterRequests['assignee']=$("#assignee").val()
			   filterRequests['fromDate']=$("#fromDate").val()
			   filterRequests['toDate']=$("#toDate").val()
			   if($("#assignee").val()!=""){
				 filterRequests['userName']=$("#"+$("#assignee").val()).val()
				}
			   	filterRequests['course']=$("#course").val()
		$.ajax({
			url:'/crmbot/singletasktable?page='+currPage+'&size='+$("#pagesize").val()+'&sorting='+this.id+'&desc='+order+'&taskType='+eleName,
			type:'PUT',
			contentType:'application/json',
			data:JSON.stringify(filterRequests),
			success:function(data){
				$("#"+parentElement).html(data)
			}
		})
	})
	
	$("body").on('click','.edit-field-data',function(e){
		var attr = $(this).attr('disabled');
		debugger
		if ( attr !== 'disabled' || typeof attr == 'undefined') {
			$(".edit-field-data-container").addClass("d-none")
			$(".save-field-data-container").removeClass("d-none")
			$(".field-data-value").attr("contentEditable","true")
			$("#course_2").removeAttr("disabled")
			$("#leadType").removeAttr("disabled")
		}
		
	})
	
	$("body").on('click','.save-field-data',function(e){
		var attr = $(this).attr('disabled');
		debugger
		if ( attr !== 'disabled' || typeof attr == 'undefined') {
			$(".save-field-data-container").addClass("d-none")
			$(".edit-field-data-container").removeClass("d-none")
			$("#course").attr("disabled",true)
			let facebookLeadId=$(this).attr("id").replace("save-field-data-","")
			let taskId=$(this).attr("name")
			facebookLeadId=+facebookLeadId
			let createTicketRequest={}
			let activeTask={}
			let facebookLeads={}
			let fieldlabels=$(".field-data-label")
			let fieldInputs=$(".field-data-value")
			let fieldData=[]
			for(let i=0;i<fieldlabels.length;i++){
				let ele={}
				let fielDataLabel=fieldlabels[i].innerText.replace(":","")
				if(fielDataLabel!="state" && fielDataLabel!="city" && fielDataLabel!="area" && fielDataLabel!="college" && fielDataLabel!="course" 
				&& fielDataLabel!="Lead type" && fielDataLabel!="10th Percentage" && fielDataLabel!="12th Percentage"  && fielDataLabel!="Neet Percentage" && fielDataLabel!="phoneNumber2" ){
					console.log(fielDataLabel)
					if(fielDataLabel=="full_name"|| fielDataLabel=="name"|| fielDataLabel=="first_name")
						activeTask['leadName']=fieldInputs[i].innerText
					else if(fielDataLabel=='phone_number'|| fielDataLabel=='number'){
						activeTask['phoneNumber']=fieldInputs[i].innerText
					}
					ele['name']=fielDataLabel
					ele['values']=[fieldInputs[i].innerText]
					if(fieldInputs[i].innerText.length>0)
						fieldData.push(ele)
				}else{
					
				}
				
			}
			debugger;
			facebookLeads['fieldData']=JSON.stringify(fieldData)
			createTicketRequest['facebookLeads']=facebookLeads
			activeTask['college']=$("#college").text()
			activeTask['course']=$("#course_2").val()
			activeTask['area']=$("#area_2").text()
			activeTask['state']=$("#state").text()
			activeTask['city']=$("#city").text()
			activeTask['phoneNumber2']=$("#phoneNumber2").text()
			activeTask['leadType']=$("#leadType").val()
			if($("#10thPercent").text() && $("#10thPercent").text()!=""){
				if(!validateNumber($("#10thPercent").text())){
					alert("Enter 10th percentage as number only")		
				}else{
					activeTask['tenthPercent']=$("#10thPercent").text()
				}
			}
			if($("#12thPercent").text() && $("#12thPercent").text()!=""){
				if(!validateNumber($("#12thPercent").text())){
					alert("Enter 12th percentage as number only")
				}else{
					activeTask['twelethPercent']=$("#12thPercent").text()
				}		
			}
			if($("#neetPercent").text() && $("#neetPercent").text()!=""){
				if(!validateNumber($("#neetPercent").text())){
					alert("Enter neet percentage as number only")
				}else{
					activeTask['neetPercent']=$("#neetPercent").text()
				}		
			}
				
			
			createTicketRequest['activeTask']=activeTask
			createTicketRequest['userName']=userName
			createTicketRequest['userEmail']=userEmail
			createTicketRequest['userId']=userId
			debugger
			$.ajax({
				url:"/crmbot/flow/update-field-data/"+facebookLeadId+"/"+taskId,
				type:"PUT",
				contentType:'application/json',
				data:JSON.stringify(createTicketRequest),
				success:function(data){
					
					$("#whatsapp-btn").attr("href","//api.whatsapp.com/send?phone="+$("#phoneNumber-field").text()+"&text=Hi")
					$("#caller-btn").attr("href","tel:+91"+$("#phoneNumber-field").text())
					
					$("#whatsapp-btn2").attr("href","//api.whatsapp.com/send?phone="+$("#phoneNumber2").text()+"&text=Hi")
					$("#caller-btn2").attr("href","tel:+91"+$("#phoneNumber2").text())
					
				},
				error:function(err){
					console.log(err)
					if(err.status==400){
						alert("The number you entered is duplicate")
						$("#phoneNumber-field").text(err.responseJSON['adId'])
					}
					$("#whatsapp-btn2").attr("href","//api.whatsapp.com/send?phone="+$("#phoneNumber2").text()+"&text=Hi")
					$("#caller-btn2").attr("href","tel:+91"+$("#phoneNumber2").text())
				}
			})
			$(".field-data-value").removeAttr("contentEditable")
		}
	})
	
	function validateNumber(stringToValidate) {
	  if (typeof stringToValidate !== 'string' || stringToValidate.trim() === '') {
	    return false;
	  }
	  const numberPattern = /^\d+(\.\d+)?$/;
	  return numberPattern.test(stringToValidate);
	}
	
	$("body").on('click',".apply-filter",function(e){
     let taskType=["allTask","myTask","completedTask","counselledTask","shceduledTask","meetingTask"]
      $("#remove-filter-btn").removeClass("d-none")
		for(let i=0;i<taskType.length;i++){
			 var filterRequests={}
			  filterRequests['leadPlatform']=$("#leadPlatform").val()
			   filterRequests['assignee']=$("#assignee").val()
			   filterRequests['fromDate']=$("#fromDate").val()
			   filterRequests['toDate']=$("#toDate").val()
			   if($("#assignee").val()!=""){
				 filterRequests['userNameForFilterMainTaskPage']=$("#"+$("#assignee").val()).val()
				}
			   	filterRequests['courseName']=$("#course").val()
			  $.ajax({
				url:'/crmbot/singletasktable?page='+(0)+"&size="+$("#pagesize").val()+"&sorting=createdOn&desc=true&taskType="+taskType[i],
				type:'PUT',
				contentType:'application/json',
				data:JSON.stringify(filterRequests),
				success:function(data){
					
					$("#"+taskType[i]+"Container_inner").html(data)
				}
			})	
		}
	 
	})
	
	
	
})




function showNotification() {
	debugger;
 if(Notification.permission === "granted"){
   
      navigator.serviceWorker.getRegistration()
      .then(function(reg){
			console.log(reg)
			reg.showNotification("Hi, you got a new lead! Please referesh to see")
		})
       
        
      //});
    
  }else{
	console.log("permisiion deim i")
}
}

function notifyMe() {
  if (!("Notification" in window)) {
    // Check if the browser supports notifications
    alert("This browser does not support desktop notification");
  } else if (Notification.permission === "granted") {
    // Check whether notification permissions have already been granted;
    // if so, create a notification
    const notification = new Notification("Hi, you got a new lead! Please referesh to see");
 
    // …
  } else if (Notification.permission !== "denied") {
    // We need to ask the user for permission
    Notification.requestPermission().then((permission) => {
      // If the user accepts, let's create a notification
      if (permission === "granted") {
        const notification = new Notification("Hi, you got a new lead! Please referesh to see");
        // …
      }
    });
  }

  // At last, if the user has denied notifications, and you
  // want to be respectful there is no need to bother them anymore.
}

$("body").on("click","#comment-dropdown-icon",function(e){
	$("#comment-dropdown-select").toggleClass("d-none")
	$("#comment-dropdown-select").attr('size',6);
	$("#comment-dropdown-icon").toggleClass("d-none")
})

$("body").on("change","#comment-dropdown-select",function(e){
	$(".comment-textarea").val($("#comment-dropdown-select").val())
	$("#comment-dropdown-select").val("")
	$("#comment-dropdown-select").toggleClass("d-none")
	$("#comment-dropdown-icon").toggleClass("d-none")
})

  $("body").on("click","input[name$='counselled']",function(e){
	var isCounsellingDone = $(this).val();
	$("#counselling-done-remark-textbox").addClass("d-none")
	if(isCounsellingDone == 'true'){
		$(".counselling-not-done-inputs").addClass("d-none")
		$(".counselling-done-inputs").removeClass("d-none")
	}else{
		$(".counselling-done-inputs").addClass("d-none")
		$(".counselling-not-done-inputs").removeClass("d-none")
	}
	
	
})

 $("body").on("change",".counselling-done-remark,.counselling-not-done-remark",function(e){
	let selectedValue = this.value
	
	if(selectedValue == "Others"){
		$("#counselling-done-remark-textbox").removeClass("d-none")
	}else{
		$("#counselling-done-remark-textbox").addClass("d-none")
	}
	
})
  
    