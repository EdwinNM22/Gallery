// --- Event Processing ---
// Regular Event Processing
function processEventData(events, cssClass = "event-list-element in-progress") {
  const results = []; // Will store our processed events

  // Process each event in the input array
  events.forEach((ev) => {
    // Process start and end times to ensure they have time components
    let startTime = new Date(ev.start);
    let endTime = new Date(ev.end);
    
    // Check if the original times have specific hours/minutes or are date-only
    const hasTimeInfo = (
      startTime.getHours() !== 0 || 
      startTime.getMinutes() !== 0 || 
      startTime.getSeconds() !== 0
    );
    
    // If no time info is present, add placeholder times (12:00 PM - 1:00 PM)
    if (!hasTimeInfo) {
      // Set start time to 12:00 PM (noon)
      startTime.setHours(12, 0, 0, 0);
      
      // Set end time to 1:00 PM (1 hour duration)
      endTime = new Date(startTime);
      endTime.setHours(13, 0, 0, 0);
    }

    // Add event to results
    results.push({
      id: ev.id, // Use original event ID
      start: startTime.toISOString(), // Use processed start time
      end: endTime.toISOString(), // Use processed end time
      text: ev.nombreCliente,
      barVisible: false,
      backColor: "transparent",
      borderColor: "transparent",
      moveDisabled: true,
      resizeDisabled: true,
      cssClass,
      tags: {
        originalEventId: ev.id,
        estado: cssClass.includes("complete") ? "complete" : "in-progress",
        usuarioNombre: ev.usuarioNombre,
        hasPlaceholderTime: !hasTimeInfo, // Track if we added placeholder time
      },
    });
  });

  return results;
}

// Group Event Processing
function processEventDataAsGroups(eventsInProgress = [], eventsComplete = []) {
  const results = [];
  const eventsByDate = {}; // This will group ALL events by date

  // Combine both in-progress and complete events
  const allEvents = [...eventsInProgress, ...eventsComplete];

  // Group events by their start date
  allEvents.forEach((ev) => {
    const startDate = new Date(ev.start).toISOString().split("T")[0]; // Get YYYY-MM-DD

    if (!eventsByDate[startDate]) {
      eventsByDate[startDate] = {
        count: 0,
        firstEvent: ev, // Keep reference to first event for metadata
        inProgressCount: 0,
        completeCount: 0,
      };
    }
    eventsByDate[startDate].count++;

    // Track status counts for potential styling
    if (
      ev.estado === "complete" ||
      (ev.tags && ev.tags.estado === "complete")
    ) {
      eventsByDate[startDate].completeCount++;
    } else {
      eventsByDate[startDate].inProgressCount++;
    }
  });

  // Create a single event for each date with the total count
  for (const [date, data] of Object.entries(eventsByDate)) {
    // For grouped events, we need to ensure proper time formatting
    let groupStartTime = new Date(data.firstEvent.start);
    let groupEndTime = new Date(data.firstEvent.end);
    
    // Ensure group events have time components for Day view compatibility
    const hasTimeInfo = (
      groupStartTime.getHours() !== 0 || 
      groupStartTime.getMinutes() !== 0 || 
      groupStartTime.getSeconds() !== 0
    );
    
    if (!hasTimeInfo) {
      // Set group time to 12:00 PM - 1:00 PM for consistency
      groupStartTime.setHours(12, 0, 0, 0);
      groupEndTime = new Date(groupStartTime);
      groupEndTime.setHours(13, 0, 0, 0);
    }

    results.push({
      id: `group-${date}`, // Unique ID based on date
      start: groupStartTime.toISOString(), // Use processed start time
      end: groupEndTime.toISOString(), // Use processed end time
      text: `${data.count}`, // Show total count
      barVisible: false,
      backColor: "transparent",
      borderColor: "transparent",
      moveDisabled: true,
      resizeDisabled: true,
      cssClass: `daypilot-event-badge`, // Combined class
      tags: {
        isGroup: true, // Mark this as a grouped event
        eventCount: data.count, // Store the total count
        inProgressCount: data.inProgressCount,
        completeCount: data.completeCount,
        date: date, // Store the date for filtering
        hasPlaceholderTime: !hasTimeInfo, // Track if we added placeholder time
      },
    });
  }

  return results;
}

/**
 * Properly disposes of a DayPilot calendar instance with comprehensive cleanup
 * @param {Object} calendar - The calendar instance to dispose
 * @param {string} elementId - The DOM element ID where the calendar was rendered
 */
function disposeCalendar(calendar, elementId) {
  if (!calendar) return;
  
  try {
    // Call the DayPilot dispose method first
    if (typeof calendar.dispose === 'function') {
      calendar.dispose();
    }
  } catch (error) {
    console.warn('Error during calendar disposal:', error);
  }
  
  // Force clear the DOM container to ensure no remnants
  const container = document.getElementById(elementId);
  if (container) {
    // Clear all content and remove any residual styling
    container.innerHTML = '';
    container.className = container.className.replace(/daypilot[^\\s]*/g, '').trim();
    // Remove any inline styles that might have been added by DayPilot
    container.removeAttribute('style');
  }
  
  // Small delay to ensure DOM cleanup is complete
  return new Promise(resolve => {
    setTimeout(resolve, 10);
  });
}

/**
 * Initializes and configures a DayPilot Month Calendar component
 * @param {Object} config - Configuration object for the calendar
 * @param {string} config.elementId - ID of the DOM element to render the calendar
 * @param {Array} [config.eventsInProgress=[]] - Array of in-progress events to display
 * @param {Array} [config.eventsComplete=[]] - Array of completed events to display
 * @param {boolean} [config.disableSelection=false] - Whether to disable date selection
 * @param {String} [config.viewType="Month"] - The view mode the calendar will have
 * @returns {Promise<DayPilot.Month|DayPilot.Calendar>} - Promise that resolves to initialized DayPilot calendar instance
 */
export async function initializeCalendar({
  elementId,
  eventsInProgress = [],
  eventsComplete = [],
  disableSelection = false,
  viewType = "Month"
}) {
  // Ensure the container is completely clean before initialization
  const container = document.getElementById(elementId);
  if (!container) {
    throw new Error(`Calendar container with id '${elementId}' not found`);
  }
  
  // Clear any existing content and styling
  container.innerHTML = '';
  container.className = '';
  container.removeAttribute('style');

  // Small delay to ensure DOM is ready
  await new Promise(resolve => setTimeout(resolve, 10));

  // 1. Mobile Detection - Check if viewport width is 768px or less
  const isMobile = window.matchMedia("(max-width: 768px)").matches;

  // 2. Set Up Common Config for Both Calendars
  const commonConfig = {
    theme: "calendar",
    showWeekends: true, // Always show weekends

    // Handle date cell clicks (day selection)
    onTimeRangeSelected: async (args) => {
      if (!disableSelection) {
        // Show modal with events for the selected day
        showDayEvents(args.start, calendar);
      }
    },

    // Handle event clicks
    onEventClick: async (args) => {
      // Show modal with events for the clicked event's day
      showDayEvents(args.e.start(), calendar);
    },

    // Disable all interactive modifications
    eventMoveHandling: "Disabled", // No dragging events
    timeRangeDoubleClickHandling: "Disabled", // No double-click actions
    eventRightClickHandling: "Disabled", // No right-click context menu
  };

  // 2. Calendar Core Configuration
  let calendar;
  if (viewType === "Day") {
    calendar = new DayPilot.Calendar(elementId, {
      ...commonConfig,
      viewType: "Day", // Changed from "Week" to "Day"
      startDate: new DayPilot.Date(), // Set to today by default
      heightSpec: "Full",
    });
  } else {
    calendar = new DayPilot.Month(elementId, {
      ...commonConfig,
    });
  }

  // 3. Event Processing and Initialization
  // Save the original events for the modal
  calendar.originalEvents = [
    ...processEventData(eventsInProgress, "event-list-element in-progress"),
    ...processEventData(eventsComplete, "event-list-element completed"),
  ];

  // Different event processing based on view type
  if (viewType === "Day") {
    // For Day view, show individual events (not grouped)
    calendar.events.list = calendar.originalEvents;
    console.log(`Day view: Loading ${calendar.originalEvents.length} individual events`);
  } else {
    // For Month view, use grouped events
    calendar.events.list = processEventDataAsGroups(
      eventsInProgress,
      eventsComplete
    );
    console.log(`Month view: Loading ${calendar.events.list.length} grouped events`);
  }

  // Initialize the calendar with processed events
  calendar.init();

  // Store the elementId for cleanup purposes
  calendar._elementId = elementId;

  // 4. Mobile Optimization
  // Truncate day headers on mobile for better fit
  if (isMobile) truncateDayHeaders();

  // 5. Navigation Controls Setup
  // Previous navigation button handler
  const prevBtn = document.getElementById("prev-month");
  if (prevBtn) {
    prevBtn.addEventListener("click", () => {
      if (viewType === "Day") {
        // Navigate by day for Day view
        calendar.startDate = calendar.startDate.addDays(-1);
        console.log(`Day view: Navigate to previous day - ${calendar.startDate}`);
      } else {
        // Navigate by month for Month view
        calendar.startDate = calendar.startDate.addMonths(-1);
        console.log(`Month view: Navigate to previous month - ${calendar.startDate}`);
      }
      calendar.update(); // Refresh display
      if (isMobile) truncateDayHeaders(); // Re-truncate headers if mobile
    });
  }

  // Today button handler (reset to current date/month)
  const todayBtn = document.getElementById("today");
  if (todayBtn) {
    todayBtn.addEventListener("click", () => {
      calendar.startDate = new DayPilot.Date(); // Set to current date
      console.log(`Navigate to today - ${calendar.startDate}`);
      calendar.update();
      if (isMobile) truncateDayHeaders();
    });
  }

  // Next navigation button handler
  const nextBtn = document.getElementById("next-month");
  if (nextBtn) {
    nextBtn.addEventListener("click", () => {
      if (viewType === "Day") {
        // Navigate by day for Day view
        calendar.startDate = calendar.startDate.addDays(1);
        console.log(`Day view: Navigate to next day - ${calendar.startDate}`);
      } else {
        // Navigate by month for Month view
        calendar.startDate = calendar.startDate.addMonths(1);
        console.log(`Month view: Navigate to next month - ${calendar.startDate}`);
      }
      calendar.update(); // Refresh display
      if (isMobile) truncateDayHeaders();
    });
  }

  // 6. Mobile-Specific UI Adjustments
  if (isMobile) {
    // Hide text labels in navigation buttons (show only icons)
    document.querySelectorAll(".btn-control span").forEach((span) => {
      span.style.display = "none";
    });

    // Add tooltips for icon-only buttons
    const prevMonthBtn = document.getElementById("prev-month");
    const nextMonthBtn = document.getElementById("next-month");
    const todayBtnElement = document.getElementById("today");
    
    if (prevMonthBtn) prevMonthBtn.title = "Previous month";
    if (nextMonthBtn) nextMonthBtn.title = "Next month";
    if (todayBtnElement) todayBtnElement.title = "Go to today";
  }

  // Return the configured calendar instance
  return calendar;
}

/**
 * Enhanced view switching function with proper cleanup
 * @param {string} newViewType - The view type to switch to ('Month' or 'Day')
 * @param {Object} currentCalendar - The current calendar instance
 * @param {Object} config - Configuration object for the new calendar
 * @returns {Promise<Object>} - Promise that resolves to the new calendar instance
 */
export async function switchCalendarView(newViewType, currentCalendar, config) {
  const elementId = config.elementId || currentCalendar._elementId;
  
  // Properly dispose of the current calendar
  await disposeCalendar(currentCalendar, elementId);
  
  // Initialize the new calendar with the specified view type
  const newCalendar = await initializeCalendar({
    ...config,
    viewType: newViewType
  });
  
  return newCalendar;
}

function showDayEvents(date, calendar) {
  // 1. DOM Element References
  // Get modal components - date display, events list container, and create button
  const modalDateDisplay = document.querySelector(
    "#dayEventsModal .date-display"
  );
  const eventsList = document.getElementById("dayEventsList");
  const dayEventsModal = document.getElementById("dayEventsModal"); // Added modal reference

  // Validate all required elements exist
  if (!modalDateDisplay || !eventsList || !dayEventsModal) {
    console.error("Modal elements not found");
    return;
  }

  // 2. Date Formatting and Display
  // Convert date to standardized string format for comparison
  const dateStr = date.toString("yyyy-MM-dd");
  // Display human-readable date in modal header
  modalDateDisplay.textContent = date.toString("MMMM dd, yyyy");

  // 3. Event Filtering
  // Get ALL original events (not the grouped ones)
  const eventsForDate = calendar.originalEvents.filter((e) => {
    const eventStart = new Date(e.start).toISOString().split("T")[0];
    return eventStart === dateStr;
  });

  // 4. Event List Rendering
  eventsList.innerHTML = "";

  if (eventsForDate.length === 0) {
    eventsList.innerHTML =
      '<div class="text-center text-muted py-4">No events scheduled</div>';
  } else {
    // Group events by status
    const groupedEvents = {
      "in-progress": [],
      complete: [],
      cancelled: [],
    };

    eventsForDate.forEach((event) => {
      const status = event.tags?.estado || "in-progress";
      groupedEvents[status].push(event);
    });

    // Render each group
    for (const [status, events] of Object.entries(groupedEvents)) {
      if (events.length > 0) {
        const groupContainer = document.createElement("div");
        groupContainer.className = `event-status-group ${status}-group`;

        groupContainer.innerHTML = `
        <div class="status-group-label ${status}">
          ${window.translations.status[status]} (${events.length})
        </div>
        <div class="events-container"></div>
      `;

        const eventsContainer =
          groupContainer.querySelector(".events-container");

        events.forEach((event) => {
          const eventElement = document.createElement("div");
          eventElement.className = `event-list-element ${status} mb-2`;

          // Format time display
          const startTime = new Date(event.start);
          const endTime = new Date(event.end);
          const timeDisplay = event.tags.hasPlaceholderTime 
            ? `<small class="text-muted">(Time TBD)</small>`
            : `<small class="text-muted">${startTime.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})} - ${endTime.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</small>`;

          eventElement.innerHTML = `
          <div class="d-flex justify-content-between align-items-start">
            <div class="event-content">
              <div class="d-flex align-items-center">
                <h4 class="event-title">${event.text || ''}</h4>
              </div>
              <div class="event-time-display">${timeDisplay}</div>
              ${event.tags && event.tags.usuarioNombre 
                ? `<p class="event-description mt-2">${window.translations?.createdBy || 'Created by'}: ${event.tags.usuarioNombre}</p>`
                : ''}
            </div>

            <div class="action-container">

              ${event.tags.estado !== 'complete'
                ?
                `<a class="custom-btn btn-action-styled btn-primary-styled" 
                  href="/form/${event.tags?.originalEventId || ''}/edit" 
                  title="${window.translations?.finishProject || 'Finish'}">
                  <i class="fa-solid fa-flag-checkered"></i>
                  <span class="btn-text">${window.translations?.finishProject || 'Finish'}</span>
                </a>`
                :
                ``
              }
              
              <button class="custom-btn btn-action-styled btn-primary-styled view-event-btn"
                      data-event-id="${event.tags?.originalEventId || ''}" 
                      data-event-estado="${event.tags?.estado || ''}">

                <i class="fas fa-eye"></i>
                ${window.translations?.view || 'View'}
              </button>

            </div>
          </div>
        `;

          eventsContainer.appendChild(eventElement);
        });

        eventsList.appendChild(groupContainer);
      }
    }
  }

  // 5. Event Button Handlers
  // Add click handlers to all "View" buttons
  document.querySelectorAll(".view-event-btn").forEach((btn) => {
    btn.addEventListener("click", (e) => {
      // Get event metadata from data attributes
      if (!e.target.classList.contains("view-event-btn")) return;

      const eventId = e.target.getAttribute("data-event-id");

      const isComplete =
        e.target.getAttribute("data-event-estado") === "complete";

      // Determine appropriate redirect URL based on completion status
      const redirectUrl = isComplete
        ? `/form/${eventId}` // Completed event URL
        : `/expediente/view-future-project/${eventId}`; // In-progress event URL

      // Navigate to event detail page
      window.location.href = redirectUrl;
    });
  });

  // 6. Modal Display
  // Show the modal using Bootstrap's vanilla JS API
  const modal = new bootstrap.Modal(dayEventsModal);
  modal.show();
}

function truncateDayHeaders() {
  const headerCells = document.querySelectorAll(".calendar_header_inner");
  headerCells.forEach((cell) => {
    const fullText = cell.textContent.trim();
    cell.textContent = fullText.substring(0, 3).toUpperCase();
  });
}