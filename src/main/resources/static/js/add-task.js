var fieldsAdded=0
$(document).ready(function(){
	
	$("body").on('click',".add-field-data-btn",function(e){
		console.log(this)
		fieldsAdded+=1;
		let field='<div class="d-flex mb-3 w-90 justify-content-center position-relative" id="fieldContainer'+fieldsAdded+'">'
		field+='<input class="field-data-label p-2 form-control2 col-3" style="border: 1px solid #d2d6da;">'
		field+='<input type="email" id="formId" class="p-2 ms-1 form-control2 field-data-input col-8" style="border: 1px solid #d2d6da;">'
		field+='<i class="material-icons opacity-10 cursor-pointer deleteField" id="deleteField_'+fieldsAdded+'" style="position: absolute;right: -10px;top: 10px;">close</i></div>'
		$("#leadDataForm").append(field)
	})
	
	$('body').on('click','.deleteField',function(e){
		console.log(this.id)
		try{
			$("#fieldContainer"+this.id.split("_")[1]).remove()
		}catch(err){
			console.log(err)
		}
	})
	
	$('body').on('change','.role-select',function(e){
		
		$(".user-select-label").removeClass("d-none")
		if(this.value=="0"){
			$(".user-select-label").addClass("d-none")
		}
		$(".user-select").addClass("d-none")
		$("#"+this.value).removeClass("d-none")
	})
	
	$('body').on('click','.save-lead',function(e){
		debugger
		if($("#leadName").val().length>0 && $("#phoneNumber").val().length>0 && $("#platform").val().length>0){
			$(".save-lead").attr("disabled",true)
			let createTicketRequest={}
			
			let activeTask={}
			let facebookLeads={}
			let fieldlabels=$(".field-data-label")
			let fieldInputs=$(".field-data-input")
			console.log(fieldlabels)
			let fieldData=[]
			for(let i=0;i<fieldlabels.length;i++){
				let ele={}
				let fielDataLabel=fieldlabels[i].getAttribute('name')
				if(! fieldlabels[i].hasAttribute('name')){
					fielDataLabel=fieldlabels[i].value
				}
				console.log(fielDataLabel)
				ele['name']=fielDataLabel
				ele['values']=[fieldInputs[i].value]
				if(fieldInputs[i].value.length>0)
					fieldData.push(ele)
			}
			facebookLeads['fieldData']=JSON.stringify(fieldData)
			createTicketRequest['facebookLeads']=facebookLeads
			activeTask['college']=$("#college").val()
			activeTask['course']=$("#course").val()
			activeTask['area']=$("#area").val()
			activeTask['refferenceName']=$("#refferenceName").val()
			activeTask['refferenceNo']=$("#refferenceNo").val()
			activeTask['campaign']=$("#campaignName").val()
			activeTask['leadPlatform']=$("#platform").val()
			activeTask['tenthPercent']=$("#10thPercent").val()
			activeTask['twelethPercent']=$("#12thPercent").val()
			activeTask['neetPercent']=$("#neetPercent").val()
			activeTask['phoneNumber2']=$("#phoneNumber2").val()
			activeTask['isActive']=true
			try{
				if($(".role-select").val()!="0"){
					activeTask['taskGroup']=$(".role-select").val()
					activeTask['assignee']=$(".role-select").val()
					if($("#"+$(".role-select").val()).val()!=")")
						activeTask['owner']=$("#"+$(".role-select").val()).val()
						var selectedUserForAdd=$("#"+$(".role-select").val()).val()
						
						var selectedOption2= $("#"+$(".role-select").val()+ " option[value='" + selectedUserForAdd + "']");
						if (selectedOption2.length > 0) {
				            var selectedOptionName2 = selectedOption2.attr("name");
				           
				        } 
						
						createTicketRequest['ownerName']=selectedOptionName2
				}
			}catch(err){
				console.log(err)
			}
			
			if($("#scheduler").val()){
				activeTask['isScheduled']=true
				activeTask['scheduleTime']=$("#scheduler").val()
				activeTask['scheduleComment']=$("#remark").val()
				activeTask['schedulerName']=userName
				activeTask['schedulerEmail']=userEmail
				activeTask['schedulerUserId']=userId
				
			}
			activeTask['status']='Open'
			
			createTicketRequest['activeTask']=activeTask
			createTicketRequest['leadName']=$("#leadName").val()
			createTicketRequest['userName']=userName
			createTicketRequest['userEmail']=userEmail
			createTicketRequest['userId']=userId
			createTicketRequest['phoneNumber']=$("#phoneNumber").val()
			if(cuurentRole!="admin"){
				createTicketRequest['assignToMe']=true
				activeTask['owner']=userEmail
				createTicketRequest['ownerName']=userName
			}else{
				createTicketRequest['assignToMe']=$("#assign_to_me").is(":checked")
			}
			
			$.ajax({
				url:'/crmbot/flow/add',
				type:'POST',
				contentType:'application/json',
				data:JSON.stringify(createTicketRequest),
				success:function(data){
					console.log(data)
					$(".save-lead").removeAttr("disabled")
					debugger;
					if(data){
						$("input").val("");
						$("select").val("0")
						$("select").trigger("change")
						$("#ticket-creation-ack-id").text(data.id)
						$("#closing-popup").removeClass("d-none")
						$(".cd-popup-trigger2").click()
					}else{
						$("input").val("");
						$("select").val("0")
						$("select").trigger("change")
						$("#ticked-creation-ack").text("Duplicate Ticket")
						$("#closing-popup").removeClass("d-none")
						$(".cd-popup-trigger2").click()
					}
					
				},
				error:function(err){
					console.log(err)
					$(".save-lead").removeAttr("disabled")
				}
			})
		}else{
			alert("Enter all mandatory details")
		}
		
	})
	
	$('body').on('click','.cd-popup-trigger4',function(e){
		$("#excel-file-input-p").removeClass('d-none')
		$("#excel-ack").addClass("d-none")
		$("#cd-main-buttons").removeClass("d-none")
		$("#task-creation-ack-buttons").addClass("d-none")
	})
	
	
	$('body').on('click','.upload-excel',function(e){
		$(".upload-excel").attr("disabled",true)
		var formData = new FormData();
		formData.append('multipartFile',document.getElementById("excel-file-input").files[0])
		let createTicketRequest={}
		createTicketRequest['userName']=userName
		createTicketRequest['userEmail']=userEmail
		createTicketRequest['userId']=userId
		
		formData.append("userName",userName)
		formData.append("userEmail",userEmail)
		formData.append("userId",userId)
		$("#excel-file-input-p").addClass('d-none')
		$("#excel-ack").removeClass("d-none")
		$("#cd-main-buttons").addClass("d-none")
		$("#task-creation-ack-buttons").removeClass("d-none")
		$("#excel-ack").text("Lead Upload under process.... it will reflect in sometime ")
		 $.ajax({

            type:'POST',
            url:'/crmbot/flow/upload-excel',
              //contentType: 'application/json',
			data:formData, 
			// enctype: 'multipart/form-data',
			processData : false,
            cache : false,
            contentType : false,
            //cache:false,
            success:function(data){
	
				$("#excel-file-input").val('')
				$(".upload-excel").removeAttr("disabled")
				$("#excel-file-input-p").addClass('d-none')
				$("#excel-ack").removeClass("d-none")
				$("#cd-main-buttons").addClass("d-none")
				$("#task-creation-ack-buttons").removeClass("d-none")
				$("#excel-ack").text("Task Successfully created")
				
			},
			error:function(err){
				$("#excel-ack").text("Unable to created task")
				console.log(err)
			}
				
		})
		
		
		
		
	})

})