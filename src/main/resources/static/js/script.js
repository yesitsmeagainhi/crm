
function processLeadsByDate(data) {
    const dateMap = {};
    data.forEach(lead => {
        if (lead['status']?.toLowerCase() === 'closed' || lead['managerName'] === 'vikasji7676@gmail.com') {
            return;
        }
        const date = new Date(lead['scheduleTime']);
        if (!isNaN(date.getTime())) {
            const year = date.getFullYear();
            const month = date.getMonth() + 1;
            const day = date.getDate();
            if (!dateMap[year]) dateMap[year] = {};
            if (!dateMap[year][month]) dateMap[year][month] = {};
            if (!dateMap[year][month][day]) dateMap[year][month][day] = [];
            dateMap[year][month][day].push(lead);
        }
    });
    return dateMap;
}


        function populateYearDropdown(dateMap) {
            const yearDropdown = document.getElementById('year-filter');
            yearDropdown.innerHTML = '<option value="">All Years</option>';
            let totalLeads = 0;

            Object.keys(dateMap).forEach(year => {
                const option = document.createElement('option');
                const leadsInYear = countLeadsInYear(dateMap[year]);
                option.value = year;
                option.textContent = `${year} (${leadsInYear})`;
                yearDropdown.appendChild(option);
                totalLeads += leadsInYear;
            });

            yearDropdown.addEventListener('change', () => {
                const selectedYear = parseInt(yearDropdown.value, 10);
                if (selectedYear) {
                    populateMonthDropdown(dateMap[selectedYear]);
                    filterLeads(selectedYear);
                } else {
                    clearDropdowns(['month-filter', 'day-filter']);
                    renderLeads(globalData);
                }
            });
        }

        function countLeadsInYear(yearData) {
            let count = 0;
            for (const month in yearData) {
                count += countLeadsInMonth(yearData[month]);
            }
            return count;
        }

        function populateMonthDropdown(monthData) {
            const monthDropdown = document.getElementById('month-filter');
            monthDropdown.innerHTML = '<option value="">All Months</option>';
            let totalLeads = 0;

            Object.keys(monthData).forEach(month => {
                const option = document.createElement('option');
                const leadsInMonth = countLeadsInMonth(monthData[month]);
                option.value = month;
                option.textContent = `${new Date(0, month - 1).toLocaleString('default', { month: 'long' })} (${leadsInMonth})`;
                monthDropdown.appendChild(option);
                totalLeads += leadsInMonth;
            });

            monthDropdown.addEventListener('change', () => {
                const selectedMonth = parseInt(monthDropdown.value, 10);
                const selectedYear = parseInt(document.getElementById('year-filter').value, 10);
                if (selectedMonth) {
                    populateDayDropdown(monthData[selectedMonth]);
                    filterLeads(selectedYear, selectedMonth);
                } else {
                    clearDropdowns(['day-filter']);
                    filterLeads(selectedYear);
                }
            });
        }

        function countLeadsInMonth(monthData) {
            let count = 0;
            for (const day in monthData) {
                count += monthData[day].length;
            }
            return count;
        }

        function populateDayDropdown(dayData) {
            const dayDropdown = document.getElementById('day-filter');
            dayDropdown.innerHTML = '<option value="">All Days</option>';
            let totalLeads = 0;

            Object.keys(dayData).forEach(day => {
                const option = document.createElement('option');
                option.value = day;
                option.textContent = `${day} (${dayData[day].length})`;
                dayDropdown.appendChild(option);
                totalLeads += dayData[day].length;
            });

            dayDropdown.addEventListener('change', () => {
                const selectedDay = parseInt(dayDropdown.value, 10);
                const selectedYear = parseInt(document.getElementById('year-filter').value, 10);
                const selectedMonth = parseInt(document.getElementById('month-filter').value, 10);
                if (selectedDay) {
                    filterLeads(selectedYear, selectedMonth, selectedDay);
                } else {
                    filterLeads(selectedYear, selectedMonth);
                }
            });
        }

        function filterLeads(year = null, month = null, day = null) {
    let filteredLeads = globalData;

    if (year) {
        filteredLeads = filteredLeads.filter(lead => {
            const scheduledDate = new Date(lead['scheduleTime']);
            return scheduledDate.getFullYear() === year;
        });
    }

    if (month) {
        filteredLeads = filteredLeads.filter(lead => {
            const scheduledDate = new Date(lead['scheduleTime']);
            return scheduledDate.getMonth() + 1 === month;
        });
    }

    if (day) {
        filteredLeads = filteredLeads.filter(lead => {
            const scheduledDate = new Date(lead['scheduleTime']);
            return scheduledDate.getDate() === day;
        });
    }

    filteredLeads = filteredLeads.filter(lead => lead['status']?.trim().toLowerCase() !== 'closed' && lead['managerName'] !== 'vikasji7676@gmail.com');
    renderLeads(filteredLeads);
}


        function clearDropdowns(dropdownIds) {
            dropdownIds.forEach(id => {
                const dropdown = document.getElementById(id);
                dropdown.innerHTML = '<option value="">All</option>';
            });
        }

        function renderLeads(data) {
    const container = document.getElementById('leads-container');
    container.innerHTML = '';

    const filteredData = data.filter(lead => lead['status']?.trim().toLowerCase() !== 'closed' && lead['managerName'] !== 'vikasji7676@gmail.com');

    if (filteredData.length) {
        filteredData.forEach(row => {
            const card = renderLeadCard(row);
            container.appendChild(card);
        });

        document.getElementById('leads-counter').innerText = `Total Leads: ${filteredData.length}`;
    } else {
        container.innerHTML = "<p class='text-gray-500'>No data available</p>";
    }
}

function renderLeadCard(row) {
    const card = document.createElement('div');
    card.classList.add('bg-white', 'rounded-lg', 'p-6', 'shadow-md', 'flex', 'justify-between', 'items-start', 'mb-4', 'transition-transform', 'transform', 'hover:scale-105');

    const leadId = row.id || `lead-${row['phoneNumber'] || 'unknown'}`;
    const name = row['leadName'] || 'Unknown Lead';
    const number = row['phoneNumber'] || 'Unknown Number';
    const counsellor = row['counsellorName'] || 'Not Assigned';
    //const counsellingDone = row['No. of counselling'] && parseInt(row['No. of counselling']) > 0;
    const course = row['course'] || 'Not Specified';
    const leadType = row['leadType'] || 'Not Specified';

    const leftContainer = document.createElement('div');
    leftContainer.classList.add('flex', 'flex-col', 'space-y-2');

    const leadName = document.createElement('h2');
    leadName.classList.add('text-xl', 'font-bold', 'text-blue-600', 'cursor-pointer', 'hover:underline');
    leadName.innerText = name;
    leadName.addEventListener('click', () => displayLeadDetails(row));
    leftContainer.appendChild(leadName);

    const mobileNumber = document.createElement('p');
    mobileNumber.classList.add('text-gray-600', 'cursor-pointer');
    mobileNumber.innerText = number;
    mobileNumber.title = "Click to copy and go to tasks";
    
    // Event listener for clicking directly on the mobile number
    mobileNumber.addEventListener('click', (e) => {
        e.stopPropagation(); // Stop propagation so card click is not triggered
        copyNumberAndNavigate(number);
    });
    leftContainer.appendChild(mobileNumber);

    const scheduledDate = document.createElement('p');
    scheduledDate.classList.add('text-gray-500');
    scheduledDate.innerText = `Scheduled: ${row['scheduleTime'] ? new Date(row['scheduleTime']).toLocaleDateString() : 'N/A'}`;
    leftContainer.appendChild(scheduledDate);

    const counsellorName = document.createElement('p');
    counsellorName.classList.add('text-gray-500');
    counsellorName.innerText = `Counsellor: ${counsellor}`;
    leftContainer.appendChild(counsellorName);

    const counsellingStatus = document.createElement('p');
    counsellingStatus.classList.add('text-sm', 'font-semibold', counsellingDone ? 'text-green-600' : 'text-red-600');
    counsellingStatus.innerText = `Counselling: ${counsellingDone ? 'Done' : 'Not Done'}`;
    leftContainer.appendChild(counsellingStatus);

    const status = row['status'] || '';
    const statusSpan = document.createElement('span');
    statusSpan.classList.add('text-xs', 'font-bold', 'px-2', 'py-1', 'rounded-full');
    if (status.toLowerCase() === 'closed' && row['isConverted']) {
        statusSpan.classList.add('bg-green-100', 'text-green-800');
        statusSpan.textContent = 'Admission Done';
    } else if (status.toLowerCase() === 'closed') {
        statusSpan.classList.add('bg-red-100', 'text-red-800');
        statusSpan.textContent = 'Closed';
    } else if (status.toLowerCase() === 'converted') {
        statusSpan.classList.add('bg-blue-100', 'text-blue-800');
        statusSpan.textContent = 'Converted';
    }
    leftContainer.appendChild(statusSpan);

    const checkboxContainer = document.createElement('div');
    checkboxContainer.classList.add('mt-2', 'flex', 'items-center');
    const checkbox = document.createElement('input');
    checkbox.type = 'checkbox';
    checkbox.classList.add('mr-2');
    checkbox.checked = isTaskCompleted(leadId);
    checkbox.addEventListener('change', () => handleCheckboxChange(leadId, card));
    checkboxContainer.appendChild(checkbox);
    const checkboxLabel = document.createElement('label');
    checkboxLabel.textContent = 'Task Completed?';
    checkboxContainer.appendChild(checkboxLabel);
    leftContainer.appendChild(checkboxContainer);

    const buttonContainer = document.createElement('div');
    buttonContainer.classList.add('flex', 'gap-2', 'mt-2');
    const viewButton = document.createElement('button');
    viewButton.classList.add('bg-blue-500', 'hover:bg-blue-700', 'text-white', 'py-2', 'px-4', 'rounded-md');
    viewButton.innerText = 'View';
    viewButton.addEventListener('click', () => openPopupDetails(row));
    buttonContainer.appendChild(viewButton);
    const callButton = document.createElement('a');
    callButton.classList.add('bg-green-500', 'hover:bg-green-700', 'text-white', 'py-2', 'px-4', 'rounded-md', 'flex', 'items-center', 'justify-center');
    callButton.href = `tel:${number}`;
    callButton.innerHTML = '<i class="fas fa-phone"></i>';
    buttonContainer.appendChild(callButton);
    const whatsappButton = document.createElement('a');
    whatsappButton.classList.add('bg-green-500', 'hover:bg-green-700', 'text-white', 'py-2', 'px-4', 'rounded-md', 'flex', 'items-center', 'justify-center');
    whatsappButton.href = `https://wa.me/${number}`;
    whatsappButton.target = '_blank';
    whatsappButton.innerHTML = '<i class="fab fa-whatsapp"></i>';
    buttonContainer.appendChild(whatsappButton);
    leftContainer.appendChild(buttonContainer);

    if (isTaskCompleted(leadId)) {
        card.classList.add('bg-green-100');
    }

    card.appendChild(leftContainer);

    const rightContainer = document.createElement('div');
    rightContainer.classList.add('flex', 'flex-col', 'items-end', 'justify-between', 'space-y-2');

    const courseInfo = document.createElement('p');
    courseInfo.classList.add('text-gray-500', 'font-medium');
    courseInfo.innerText = `Course: ${course}`;
    rightContainer.appendChild(courseInfo);

    const leadTypeInfo = document.createElement('p');
    leadTypeInfo.classList.add('text-gray-500', 'font-medium');
    leadTypeInfo.innerText = `Lead Type: ${leadType}`;
    rightContainer.appendChild(leadTypeInfo);

    card.appendChild(rightContainer);

    // Add click event listener to the entire card to also copy number and navigate
    card.addEventListener('click', () => {
        copyNumberAndNavigate(number);
    });

    return card;
}

// Helper function to copy the number and navigate to the tasks page
function copyNumberAndNavigate(number) {
    navigator.clipboard.writeText(number).then(() => {
        window.location.href = 'https://www.vmedify.com/crmbot/tasks';
    }).catch(err => {
        console.error('Failed to copy phone number: ', err);
    });
}



        function markTaskCompleted(leadId, card) {
            card.classList.add('bg-green-100');
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

        function handleCheckboxChange(leadId, card) {
            const checkbox = card.querySelector('input[type="checkbox"]');
            if (checkbox.checked) {
                if (confirm('Have you applied Schedule in CRM?')) {
                    if (confirm('Have you added comment?')) {
                        markTaskCompleted(leadId, card);
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
                    card.classList.remove('bg-green-100');
                } else {
                    checkbox.checked = true;
                }
            }
        }

        function displayLeadDetails(leadData) {
            const leadName = document.getElementById('leadName');
            const leadDetailsDiv = document.getElementById('leadDetails');
            leadName.textContent = leadData['leadName'] || 'Unknown Lead';
            leadDetailsDiv.innerHTML = '';
            Object.keys(leadData).forEach(key => {
                const detailDiv = document.createElement('div');
                const detailKey = document.createElement('span');
                const detailValue = document.createElement('span');
                detailKey.classList.add('font-medium', 'mr-2');
                detailKey.textContent = key + ': ';
                detailValue.textContent = leadData[key];
                detailDiv.appendChild(detailKey);
                detailDiv.appendChild(detailValue);
                leadDetailsDiv.appendChild(detailDiv);
            });
            document.getElementById('leadModal').classList.remove('hidden');
        }

        function openPopupDetails(leadData) {
            const popupDetailsDiv = document.getElementById('popupDetails');
            popupDetailsDiv.innerHTML = '';
            Object.keys(leadData).forEach(key => {
                const detailDiv = document.createElement('div');
                const detailKey = document.createElement('span');
                const detailValue = document.createElement('span');
                detailKey.classList.add('font-medium', 'mr-2');
                detailKey.textContent = key + ': ';
                detailValue.textContent = leadData[key];
                detailDiv.appendChild(detailKey);
                detailDiv.appendChild(detailValue);
                popupDetailsDiv.appendChild(detailDiv);
            });
            document.getElementById('popupModal').classList.remove('hidden');
        }

        document.getElementById('closeLeadModal').addEventListener('click', function() {
            document.getElementById('leadModal').classList.add('hidden');
        });

        document.getElementById('closePopupModal').addEventListener('click', function() {
            document.getElementById('popupModal').classList.add('hidden');
        });

        function populateCourseDropdown() {
            const courseDropdown = document.getElementById('course-filter');
            courseDropdown.innerHTML = '<option value="">All Courses</option>';
            const courses = new Set(globalData.map(item => item['course']).filter(Boolean));
            courses.forEach(course => {
                const option = document.createElement('option');
                option.value = course;
                option.textContent = course;
                courseDropdown.appendChild(option);
            });
        }

        function populateLeadTypeDropdown() {
            const leadTypeDropdown = document.getElementById('lead-type-filter');
            leadTypeDropdown.innerHTML = '<option value="">All Types</option>';
            leadTypeDropdown.innerHTML += '<option value="blank">Blank</option>'; // Add this line
            const leadTypes = new Set(globalData.map(item => item['leadType']).filter(Boolean));
            leadTypes.forEach(type => {
                const option = document.createElement('option');
                option.value = type;
                option.textContent = type;
                leadTypeDropdown.appendChild(option);
            });
        }

        function calculateSummaryInfo(data) {
            const totalLeads = data.length;
            const counsellingDone = data.filter(lead => lead['isCounsellingDone']).length;
            const admissionsDone = data.filter(lead => lead['isConverted']);
            const dPharmAdmissions = admissionsDone.filter(lead => lead['course']?.toLowerCase() === 'd.pharm').length;
            const bPharmAdmissions = admissionsDone.filter(lead => lead['course']?.toLowerCase() === 'b.pharm').length;
            const otherAdmissions = admissionsDone.length - (dPharmAdmissions + bPharmAdmissions);
            const totalEarnings = (dPharmAdmissions * 600) + (bPharmAdmissions * 700) + (otherAdmissions * 300);
            document.getElementById('totalLeads').innerText = totalLeads;
            document.getElementById('counsellingDone').innerText = counsellingDone;
            document.getElementById('admissionDone').innerText = admissionsDone.length;
            document.getElementById('dPharmAdmissions').innerText = dPharmAdmissions;
            document.getElementById('bPharmAdmissions').innerText = bPharmAdmissions;
            document.getElementById('otherAdmissions').innerText = otherAdmissions;
            document.getElementById('totalEarnings').innerText = totalEarnings;
        }

        function updateTodaysLeadsCount(data) {
            const today = new Date();
            today.setHours(0, 0, 0, 0);
            const todaysLeads = data.filter(row => {
                if (!row['scheduleTime']) return false;
                const rowDate = new Date(row['scheduleTime']);
                rowDate.setHours(0, 0, 0, 0);
                return rowDate.getTime() === today.getTime();
            }).length;
            document.getElementById('todaysLeadsCount').innerText = todaysLeads;
        }

        function filterByCourse() {
            const selectedCourse = document.getElementById('course-filter').value;
            const filteredData = selectedCourse ? globalData.filter(item => item['course'] === selectedCourse) : globalData;
            renderLeads(filteredData);
        }

        function filterByLeadType() {
            const selectedType = document.getElementById('lead-type-filter').value;
            let filteredData;

            if (selectedType === "blank") {
                filteredData = globalData.filter(item => !item['leadType']);
            } else {
                filteredData = selectedType ? globalData.filter(item => item['leadType'] === selectedType) : globalData;
            }

            renderLeads(filteredData);
        }

        function filterByDate() {
            const selectedDate = new Date(document.getElementById('scheduled-date-picker').value);
            selectedDate.setHours(0, 0, 0, 0);

            const filteredData = globalData.filter(row => {
                if (!row['scheduleTime']) return false;
                const rowDate = new Date(row['scheduleTime']);
                rowDate.setHours(0, 0, 0, 0);
                return rowDate.getTime() === selectedDate.getTime();
            });
            renderLeads(filteredData);
        }

        function filterTodaysLeads() {
            const today = new Date();
            today.setHours(0, 0, 0, 0);

            const todaysLeads = globalData.filter(row => {
                if (!row['scheduleTime']) return false;
                const rowDate = new Date(row['scheduleTime']);
                rowDate.setHours(0, 0, 0, 0);
                return rowDate.getTime() === today.getTime();
            });
            renderLeads(todaysLeads);
        }

        function filterLeadsWithoutSchedule() {
            const leadsWithoutSchedule = globalData.filter(lead => 
                !lead['scheduleTime'] && lead['status']?.trim().toLowerCase() !== 'closed'
            );
            renderLeads(leadsWithoutSchedule);
        }

        function hideClosedLeads() {
            const filteredData = globalData.filter(row => !(row['status']?.trim().toLowerCase() === 'closed'));
            renderLeads(filteredData);
        }

        function updateNoCommentCount() {
            const noCommentLeads = globalData.filter(row =>
                row['status']?.trim().toLowerCase() !== 'closed' &&
                (!row['Last Comment'] || row['Last Comment'].trim() === '')
            );

            document.getElementById('filter-no-comment').innerText = `No Comment - ${noCommentLeads.length}`;
            document.getElementById('filter-no-comment').onclick = () => {
                renderLeads(noCommentLeads);
            };
        }

        function updateCounsellingButtonCounts() {
            const counsellingAssignedNotDone = globalData.filter(lead => 
                lead['counsellorName'] &&
                (!lead['No. of counselling'] || parseInt(lead['No. of counselling']) === 0) &&
                lead['status']?.trim().toLowerCase() !== 'closed'
            );

            const counsellingAssigned = globalData.filter(lead => 
                lead['No. of counselling'] && parseInt(lead['No. of counselling']) > 0 &&
                lead['status']?.trim().toLowerCase() !== 'closed'
            );

            const notDoneSpan = document.getElementById('counselling-assigned-not-done-count');
            notDoneSpan.innerText = counsellingAssignedNotDone.length > 0 ? counsellingAssignedNotDone.length : '';

            const doneSpan = document.getElementById('counselling-assigned-count');
            doneSpan.innerText = counsellingAssigned.length > 0 ? counsellingAssigned.length : '';
        }

        function filterCounsellingAssignedNotDone() {
            const counsellingAssignedNotDone = globalData.filter(lead => 
                lead['counsellorName'] &&
                (!lead['No. of counselling'] || parseInt(lead['No. of counselling']) === 0) &&
                lead['status']?.trim().toLowerCase() !== 'closed'
            );
            renderLeads(counsellingAssignedNotDone);
        }

        function filterCounsellingAssigned() {
            const counsellingAssigned = globalData.filter(lead => 
                lead['No. of counselling'] && parseInt(lead['No. of counselling']) > 0 &&
                lead['status']?.trim().toLowerCase() !== 'closed'
            );
            renderLeads(counsellingAssigned);
        }

        function updateLeadsWithoutScheduleCount() {
            const leadsWithoutSchedule = globalData.filter(lead => !lead['scheduleTime'] && lead['status']?.trim().toLowerCase() !== 'closed');
            const count = leadsWithoutSchedule.length;
            document.getElementById('leads-without-schedule-count').innerText = count > 0 ? count : '';
        }
        
        function searchLeads() {
    const query = document.getElementById('search-bar').value.toLowerCase();
    const filteredLeads = globalData.filter(lead => {
        const name = lead['leadName'] ? lead['leadName'].toLowerCase() : '';
        const phone = lead['phoneNumber'] ? lead['phoneNumber'].toLowerCase() : '';
        const course = lead['course'] ? lead['course'].toLowerCase() : '';
        return name.includes(query) || phone.includes(query) || course.includes(query);
    });
    renderLeads(filteredLeads);
}


        function renderLeadView(leads) {
            const container = document.getElementById('leads-container');
            container.innerHTML = '';

            leads.forEach(lead => {
                const leadElement = renderLeadCard(lead);
                container.appendChild(leadElement);
            });
        }

        initialize(); // Invoke the function to start the application
      
        if ('serviceWorker' in navigator) {
    window.addEventListener('load', function() {
        navigator.serviceWorker.register('/service-worker.js').then(function(registration) {
            console.log('ServiceWorker registration successful with scope: ', registration.scope);
        }, function(err) {
            console.log('ServiceWorker registration failed: ', err);
        });
    });
}
        