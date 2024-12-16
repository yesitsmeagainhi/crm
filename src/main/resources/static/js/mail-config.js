var editConfigClicked=false
var configId=''
$(document).ready(function(){
	
	function loadInitialTable(){
		$.ajax({
			url:"/admin/mail-configuration-table",
			success:function(data){
				$("#mail-config-table-container").html(data)
			},
			error:function(err){
				console.log(err)
			}
		})
	}
	
	loadInitialTable()
	
	$('body').on('click',".add-mail-config",function(e){
		if( $("#mailId").val().length>0  && $("#password").val().length>0 && $("#isActive").val()!="0"){
			var targetMail={}
			targetMail['mailId']=$("#mailId").val()
			targetMail['appPassword']=$("#password").val()
			targetMail['isActive']=$("#isActive").val()
			if(editConfigClicked){
				targetMail['id']=configId
			}
			$.ajax({
				url:"/admin/savemailConfig",
				type:"PUT",
				contentType:"application/json",
				data:JSON.stringify(targetMail),
				success:function(data){
					$("#configPopupHeader").text("Add Campaign")
					$("#mailId").val("")
					$("#password").val("")
					$("#isActive").val("0")
					$(".cd-popup-close2").click()
					loadInitialTable()
					editConfigClicked=false
				},
				error:function(err){
					console.log(err)
					editConfigClicked=false
				}
				
			})
		}else{
			console.log("Asdad")
		}
	})
	
	$("body").on("click","#pass-visible",function(e){
		if($("#password").attr('type')=="password")
			$("#password").attr('type',"text")
		else
			$("#password").attr('type',"password")
	})
	
	$('body').on('click','#editConfig',function(e){
		editConfigClicked=true
		configId=this.getAttribute('name')
		$("#password").attr('type',"password")
		$("#configPopupHeader").text("Edit Mail Config")
		$("#mailId").val($("#mailId"+configId).text())
		$("#password").val($("#password"+configId).attr("name"))
		$("#isActive").val($("#mailIsActive"+configId).text())
		
	})
	
	$('body').on('click','.cd-popup-trigger-mail', function(event){
		event.preventDefault();
		if(this.id!="editConfig")
			$("#configPopupHeader").text("Add Mail Config")
		$("#password").attr('type',"password")
		$('.cd-popup-mail').addClass('is-visible');
		
	});
	
	
	$('body').on('click','.cd-popup', function(event){
		if( $(event.target).is('.cd-popup-close') || $(event.target).is('.cd-popup') ||  $(event.target).is('.cd-popup-close2')  ) {
			event.preventDefault();
			$(this).removeClass('is-visible');
			
			
		}
	});
	//close popup when clicking the esc keyboard button
	$(document).keyup(function(event){
    	if(event.which=='27'){
    		$('.cd-popup').removeClass('is-visible');
    		
	    }
    });
    
    if($("#isUserActive").text()=="true"){
		debugger;
		if(!($(".userActiveCheckBox").is(":checked"))){
			$(".userActiveCheckBox").click()
		}
		
		
		
	}
	$('body').on('change', '.userActiveCheckBox', function(e) {
		debugger;
		var userMaster={}
		userMaster['isActive']=$(".userActiveCheckBox").is(":checked")
		userMaster['userName']=$("#prefferedUserName").text()
		userMaster['userDisplayName']=$("#userDisplayName").text()
		
		$.ajax({
			url:"/usermaster/save",
			type:'PUT',
			data:JSON.stringify(userMaster),
			contentType:'application/json',
			success:function(data){
				console.log(data)
			},
			error:function(err){
				console.log(err)
			}
		})
	})

})