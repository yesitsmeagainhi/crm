const API_BASE = "/role-access";
	$(document).ready(function(){
		getAccess($("#rbac-role-dropdown").val())
		
		const table = document.getElementById('rbac-access-table');

		table.addEventListener('change', function (event) {
		  const changedCheckbox = event.target;
		  const row = changedCheckbox.closest('tr');
		  const moduleName = row.getAttribute('data-module-name');
		  const isEditCheckbox = changedCheckbox.classList.contains('edit-access');

		  if (moduleName === 'CLOSING INPUT FULL' && isEditCheckbox && changedCheckbox.checked) {
		    // Find the CLOSING INPUT PARTIAL row
		    const partialRow = [...table.rows].find(r => r.getAttribute('data-module-name') === 'CLOSING INPUT PARTIAL');
		    if (partialRow) {
		      const editCheckbox = partialRow.querySelector('.edit-access');
		      if (editCheckbox && editCheckbox.checked) {
		        editCheckbox.checked = false;
		      }
		    }
		  }
		  if (moduleName === 'CLOSING INPUT PARTIAL' && isEditCheckbox && changedCheckbox.checked) {
		  		    // Find the CLOSING INPUT FULL row
		  		    const partialRow = [...table.rows].find(r => r.getAttribute('data-module-name') === 'CLOSING INPUT FULL');
		  		    if (partialRow) {
		  		      const editCheckbox = partialRow.querySelector('.edit-access');
		  		      if (editCheckbox && editCheckbox.checked) {
		  		        editCheckbox.checked = false;
		  		      }
		  		    }
		  		  }
		});
	})
	// 🟡 When role dropdown changes
	$("#rbac-role-dropdown").on("change", function () {
	  const role = $(this).val();
	  getAccess(role)
	  
	});
	
	function getAccess(role){
		$.ajax({
		    url: `${API_BASE}/role/${role}`,
		    type: 'GET',
		    success: function (accessList) {
		      $("#rbac-access-table tr").each(function () {
		        const moduleId = $(this).data("module-id");
		        const viewInput = $(this).find(".view-access");
		        const editInput = $(this).find(".edit-access");
				const adminData = $(this).find(".admin-level-data");
	
		        const access = accessList.find(a => a.modules.id === moduleId);
	
		        viewInput.prop("checked", access?.isView || false);
		        editInput.prop("checked", access?.isEdit || false);
				adminData.prop("checked", access?.isAdminData || false);
		      });
		    },
		    error: function (xhr) {
		      alert("Failed to fetch role access: " + xhr.responseText);
		    }
		  });
	}

	// 🟢 On Save Click
	$(".srbac-btn-save").on("click", function () {
	  const role = $("#rbac-role-dropdown").val();
	  if (!role) return alert("Please select a role");

	  const accessPayload = [];

	  $("#rbac-access-table tr").each(function () {
	    const moduleId = $(this).data("module-id");
	    const view = $(this).find(".view-access").is(":checked");
	    const edit = $(this).find(".edit-access").is(":checked");
		const isAdminData = $(this).find(".admin-level-data").is(":checked");

	    accessPayload.push({
	      role: role,
	      modules: { id: moduleId },
	      isView: view,
	      isEdit: edit,
	      isAdminData: isAdminData // Optional – set if needed
	    });
	  });

	  // Save each access record
	  $.ajax({
	    url: API_BASE + "/bulk",
	    type: "POST",
	    contentType: "application/json",
	    data: JSON.stringify(accessPayload),
	    success: function () {
	      alert("Access saved successfully.");
	    },
	    error: function (xhr) {
	      alert("Failed to save access: " + xhr.responseText);
	    }
	  });
	});