var ticketForwarded=false
$(document).ready(function($){
	debugger
	var userDetails=/*[[${userDetails}]]*/
	console.log($("#prefferedUserName").text())
	console.log($("#isUserActive").text())

	if($("#isUserActive").text()=="true"){
		if(!($(".userActiveCheckBox").is(":checked"))){
			$(".userActiveCheckBox").click()
		}	
		
	}
	$('body').on('click', '.edit-icon', function(e) {
		$("#profile-popup").removeClass("d-none")
	})
	
	$('body').on('click', '.remove-profile-btn', function(e) {
		let userName = $("#prefferedUserName").text()
		var request={
		}
		request['userName']=userName
		$.ajax({
			url:'/usermaster/delete-image',
			type:'DELETE',
			data: JSON.stringify(request),
			contentType : 'application/json',
			success:function(data){
				const img = document.getElementById('profilePreview');
			    img.src = ``;
				
				const modalImg = document.getElementById('modal-profile-image');
				modalImg.src = ``;
				$(".profile-icon").removeClass("d-none")
				$("#profilePreview").addClass("d-none")
				alert("Image removed");
				const imgElement = document.getElementById('modal-profile-image');
						
				if (imgElement && imgElement.src && imgElement.src.trim() !== '' && !imgElement.src.endsWith('default-profile.jpg')) {
				  console.log("Image source is set:", imgElement.src);
				} else {
				  $(".cd-popup-close-profile").click()
				}
			},
			error:function(err){
				alert("Failed to remove image");
			}
		})
	  
	})

	setProfileImage()
	
	function setProfileImage(){
		let userName = $("#prefferedUserName").text()
		var request={
		}
		request['userName']=userName
		$.ajax({
			url:'/usermaster/get',
			type:'POST',
			data: JSON.stringify(request),
			contentType : 'application/json',
			success:function(data){
				if(data){
					if (data.image) {
					    const img = document.getElementById('profilePreview');
					    img.src = `data:image/jpeg;base64,${data.image}`;
						
						const modalImg = document.getElementById('modal-profile-image');
						modalImg.src = `data:image/jpeg;base64,${data.image}`;
					    
						img.style.display = 'block';
						$("#profilePreview").removeClass("d-none")
						$(".profile-icon").addClass("d-none")
					}
				}
			}
			
		})
	}
	
	$('body').on('change', '.userActiveCheckBox', function(e) {
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
	
	
	
	$('body').on('click', '.sidenav .nav-item .nav-link', function(e) {
		if(!$(this).hasClass('cd-popup-trigger')){
			$('.sidenav .nav-item .active').toggleClass('bg-gradient-primary')
			$('.sidenav .nav-item .active').toggleClass('active')
			$(this).toggleClass("active")
			$(this).toggleClass("bg-gradient-primary")
		}
	})

	$('body').on('click', '#iconNavbarSidenav2', function(e) {
		$(".main-content").toggleClass("ms-0")
		$(".g-sidenav-show").toggleClass("g-sidenav-pinned")
		if($(".main-content").hasClass("ms-0"))
			$("#sidenav-main").css("left","-50%")
		else
			$("#sidenav-main").css("left","0%")
	})
	
	$('body').on('click', '.logout-btn', function(e) {
		$.ajax({
			url:'/auth/logout',
			success:function(data){
				location.href="/crmbot/tasks"
			}
		})
	})
	
	//open popup
	$('body').on('click','.cd-popup-trigger-profile', function(event){
		event.preventDefault();
		$('.cd-popup-profile').addClass('is-visible');
		const imgElement = document.getElementById('modal-profile-image');
								
			if (imgElement && imgElement.src && imgElement.src.trim() !== '' && !imgElement.src.endsWith('default-profile.jpg') 
				&& !imgElement.src.includes("localhost") && !imgElement.src.includes("vmedify")) {
			  console.log("Image source is set:", imgElement.src);
			} else {
			  $("#profile-popup").removeClass("d-none")
			}
		
							  
		
	});
	
	$('body').on('click','.cd-popup-trigger', function(event){
			event.preventDefault();
			$('.cd-popup').addClass('is-visible');
			if($(event.target).is(".close-schedule-btn")){
				$(".logout-btn").addClass('complete-schedule')
				$("#logout-popup-title").text('Are you sure you want to close the scheduler')
				$(".logout-btn").removeClass('logout-btn')
			}
			console.log(ticketForwarded+"3")
		});
		$('body').on('click','.cd-popup-profile', function(event){
			if( $(event.target).is('.cd-popup-close-profile') || $(event.target).is('.cd-popup-profile')){
				event.preventDefault();
				$(this).removeClass('is-visible');
				$("#profile-popup").addClass("d-none")
			}
		})
	//close popup
	$('body').on('click','.cd-popup', function(event){
		if( $(event.target).is('.cd-popup-close') || $(event.target).is('.cd-popup') ||  $(event.target).is('.cd-popup-close2')  ) {
			event.preventDefault();
			console.log(ticketForwarded+"2")
			$(this).removeClass('is-visible');
			try{
				
					$(".complete-schedule").addClass('logout-btn')
					$(".complete-schedule").removeClass('complete-schedule')
					$("#logout-popup-title").text('Are you sure you want to logout')
				
			}catch(err){
				console.log(err)
			}
			
		}
	});
	//close popup when clicking the esc keyboard button
	$(document).keyup(function(event){
    	if(event.which=='27'){
    		$('.cd-popup').removeClass('is-visible');
    		$('.cd-popup-automation').removeClass('is-visible');
    		$('.cd-popup2').removeClass('is-visible');
    		console.log(ticketForwarded+"5")
    		if(ticketForwarded){
				ticketForwarded=false
				//location.reload()
			}
			
			try{
				$(".complete-schedule").addClass('logout-btn')
				$(".complete-schedule").removeClass('complete-schedule')
				$("#logout-popup-title").text('Are you sure you want to logout')
			}catch(err){
				console.log(err)
			}
	    }
    });
    
    
    //open popup
	$('body').on('click','.cd-popup-trigger2', function(event){
		event.preventDefault();
		console.log(ticketForwarded+"1")
		ticketForwarded=false
		$("#fileupload-popup").addClass("d-none")
		$(".assign-popup").removeClass('d-none')
		$('.cd-popup2').addClass('is-visible');
	});
	$('body').on('click','.cd-popup-trigger3', function(event){
		event.preventDefault();
		console.log(ticketForwarded+"1")
		ticketForwarded=false
		
		$(".assign-popup").addClass('d-none')
		$(".closing-popup").removeClass('d-none')
		$('.cd-popup2').addClass('is-visible');
	});
	
	//close popup
	$('body').on('click','.cd-popup2', function(event){
		if( $(event.target).is('.cd-popup-close_2') || $(event.target).is('.cd-popup2') || $(event.target).is('.cd-popup-close2_2')) {
			event.preventDefault();
			$(this).removeClass('is-visible');
			$('.cd-popup2').removeClass('is-visible');
			console.log("CLOSE CLIKCED")
			console.log(ticketForwarded)
			if(ticketForwarded || event.target.id=="close-acknowledg-popup"){
				ticketForwarded=false
				$(".overlay").removeClass("d-none")
				$("#search-box").val("")
				location.reload()
			}
		}
	});
	
	$('body').on('click','.profile-upload',function(e){
        e.preventDefault();
		$(".overlay").removeClass("d-none")
        const formData = new FormData();

        formData.append('userName', $("#prefferedUserName").text());
		var input = document.getElementById('profile-upload-input');
		var file = input.files[0];
        if (file) {
            formData.append('file', file);
        }
		$.ajax({

           type:'POST',
           url:'/usermaster/image',
             //contentType: 'application/json',
			data:formData, 
			// enctype: 'multipart/form-data',
			processData : false,
           cache : false,
           contentType : false,
           //cache:false,
           success:function(data){
			$(".overlay").addClass("d-none")
			setProfileImage()
			$(".cd-popup-close-profile").click()
		   },
		   error:function(err){
			
		   }
		 });
        
    });
	
    
    
});