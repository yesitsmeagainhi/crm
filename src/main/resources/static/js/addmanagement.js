var editConfigClicked=false
var configId=''

$('body').on('click',".cd-popup-trigger2",function(){
	$("#formId").val("")
	$("#campaignName").val("")
	$("#platform").val("0")
	$("#isActive").val("0")
})

$(document).ready(function(){
	$('body').on('click',".add-campaign",function(e){
		if( $("#platform").val()!="0" && $("#campaignName").val().length>0 && $("#isActive").val()!="0"){
			var facebookLeadConfigs={}
			facebookLeadConfigs['leadId']=$("#formId").val()
			facebookLeadConfigs['campaignName']=$("#campaignName").val()
			facebookLeadConfigs['platform']=$("#platform").val()
			facebookLeadConfigs['isActive']=$("#isActive").val()
			facebookLeadConfigs['message']=$("#message").val()
			if(editConfigClicked){
				facebookLeadConfigs['id']=configId
			}
			$.ajax({
				url:"/admin/savecampaign",
				type:"POST",
				contentType:"application/json",
				data:JSON.stringify(facebookLeadConfigs),
				success:function(data){
					$("#configPopupHeader").text("Add Campaign")
					$("#formId").val("")
					$("#campaignName").val("")
					$("#platform").val("0")
					$("#isActive").val("0")
					$("#message").val("")
					$(".cd-popup-close2_2").click()
					$("#add-management-table-container").html(data)
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
	
	
	$('body').on('click','#editConfig',function(e){
		editConfigClicked=true
		configId=this.getAttribute('name')
		$("#configPopupHeader").text("Edit Campaign")
		$("#formId").val($("#leadFormId"+configId).text())
		$("#campaignName").val($("#leadCampaignName"+configId).text())
		$("#platform").val($("#leadPlatform"+configId).text())
		$("#isActive").val($("#leadIsActive"+configId).text())
		$("#message").val($("#message"+configId).text())
		
	})
	

})