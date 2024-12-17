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
	
	
$('body').on('change','.currUserDetails',function(e){
		let currentRole = $('.role-select').val()
		let currentUser = $("#"+$('.role-select').val()).val()
		let course = $("#course").val()
		let platform = $("#platform").val()
		if(currentRole && currentUser && course && platform)
		$.ajax({
			url:'/crmbot/flow/total-leads?userName='+currentUser+"&role="+currentRole+"&course="+course+"&platform="+platform,
			type:'GET',
			contentType:'application/json',
			success:function(data){
				$("#totalLeads").text("Total Leads : "+data)
				},
			error:function(err){
				
			}
		})
	})
	
		
	$('body').on('click','.transfer-leads',function(e){
			let currentRole = $('.role-select').val()
			let currentUser = ""
			if (currentRole)
				currentUser = $("#"+$('.role-select').val()).val()
			
			let nextRole = $('.role-select2').val()
			let nextUser = ""
			if (nextRole)
			 nextUser = $("#"+$('.role-select2').val()+'2').val()
		debugger
			if(nextUser && currentUser){
				$.ajax({
					url:'/crmbot/flow/transfer-leads?toUserName='+nextUser+"&fromUserName="+currentUser,
					type:'PUT',
					contentType:'application/json',
					success:function(data){
						alert("Leads successfully transfered")
						},
					error:function(err){
						
					}
				})
			}
			else{
				alert("Enter All Details")
			}
		})