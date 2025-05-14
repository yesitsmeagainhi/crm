$('body').on('change','.role-select2',function(e){
			
			$(".user-select-label2").removeClass("d-none")
			if(this.value=="0"){
				$(".user-select-label2").addClass("d-none")
			}
			$(".user-select2").addClass("d-none")
			$("#"+this.value+"2").removeClass("d-none")
		})
		
$('body').on('change','.role-select',function(e){
	$("#totalLeads").text("")
})
	
var currLeads = 0
$('body').on('change','.currUserDetails',function(e){
		let currentRole = $('.role-select').val()
		let currentUser = $("#"+$('.role-select').val()).val()
		
		var filterRequest = {}
		filterRequest['role']=$('.role-select').val()
		filterRequest['userName']=currentUser
		filterRequest['courseName']=$("#course").val()
		filterRequest['leadPlatform']=$("#platform").val()
		filterRequest['leadType']=$("#lead_type").val()
		filterRequest['isLeadTransfer']=true
		filterRequest['isAdmin']=false
		filterRequest['isActive']=true
		if($("#taskType").val()=='My Task'){
			filterRequest['isMyTask']=true
		}else{
			filterRequest['isAllTask']=true
		}
		
		if($("#counsellingStatus").val()=='counselled'){
			filterRequest['isCounselled']=true
		}else if($("#counsellingStatus").val()=='not counselled'){
			filterRequest['isCounselled']=false
		}
		
		//filterRequest['isAllTask']=true
		
		if(currentRole =='manager'){
			filterRequest['isManager']=true
		}else if(currentRole =='telecaller'){
			filterRequest['isTeleCaller']=true
		}else if(currentRole =='counsellor'){
			filterRequest['isCounsellor']=true
		}
		filterRequest['userNameForFilterMainTaskPage'] = $("#"+$('.role-select').val()).val()
		
		if(currentRole && currentUser && $("#taskType").val().length>0)
		$.ajax({
			url:'/crmbot/flow/total-leads',
			type:'POST',
			contentType:'application/json',
			data:JSON.stringify(filterRequest),
			success:function(data){
				currLeads = data
				$("#totalLeads").text("Total Leads : "+data)
				},
			error:function(err){
				
			}
		})
	})
	
		
	$('body').on('click','.transfer-leads',function(e){
		let currentRole = $('.role-select').val()
		let currentUser = $("#"+$('.role-select').val()).val()
		
		var filterRequest = {}
		filterRequest['role']=$('.role-select').val()
		filterRequest['userName']=currentUser
		filterRequest['courseName']=$("#course").val()
		filterRequest['leadPlatform']=$("#platform").val()
		filterRequest['leadType']=$("#lead_type").val()
		filterRequest['isLeadTransfer']=true
		filterRequest['isAdmin']=false
		filterRequest['isActive']=true
		
		let nextRole = $('.role-select2').val()
		let nextUser = ""
		if (nextRole)
		 nextUser = $("#"+$('.role-select2').val()+'2').val()
	
		const leadCount = $('#numberOfLeads').val();
		let numberOfLeads =  Number(leadCount);
		
		filterRequest['toRole']=nextRole
		filterRequest['toUserName']=nextUser
		filterRequest['numberOfLeads']=numberOfLeads
		
		if($("#taskType").val()=='My Task'){
			filterRequest['isMyTask']=true
		}else{
			filterRequest['isAllTask']=true
		}
		
		if($("#counsellingStatus").val()=='counselled'){
			filterRequest['isCounselled']=true
		}else if($("#counsellingStatus").val()=='not counselled'){
			filterRequest['isCounselled']=false
		}
		
		//filterRequest['isAllTask']=true
		
		if(currentRole =='manager'){
			filterRequest['isManager']=true
		}else if(currentRole =='telecaller'){
			filterRequest['isTeleCaller']=true
		}else if(currentRole =='counsellor'){
			filterRequest['isCounsellor']=true
		}
		filterRequest['userNameForFilterMainTaskPage'] = $("#"+$('.role-select').val()).val()
		
		if (!leadCount || isNaN(numberOfLeads) || numberOfLeads <= 0 || !Number.isInteger(numberOfLeads)) {
		  alert("Please enter a valid positive number of leads.");
		  return
		}
		if(currentRole && currentUser && $("#taskType").val().length>0){
			
			if(numberOfLeads <= currLeads){
				$.ajax({
					url:'/crmbot/flow/transfer-leads',
					type:'PUT',
					contentType:'application/json',
					data:JSON.stringify(filterRequest),
					success:function(data){
						alert("Leads successfully transfered")
						},
					error:function(err){
						
					}
				})
			}else{
				alert("Enter number less than "+(currLeads+1))
			}
			
		}
		else{
			alert("Enter All Details")
		}
	})