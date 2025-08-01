// --- Event Processing ---
// Regular Event Processing
function processEventData(
  events,
  cssClass = "daypilot-event-badge in-progress"
) {
  const results = []; // Will store our processed events

  // Process each event in the input array
  events.forEach((ev) => {
    // Add event to results
    results.push({
      id: ev.id, // Use original event ID
      start: ev.start, // Original start time
      end: ev.end, // Original end time
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
        expedienteId: ev.expedienteId,
      },
    });
  });

  return results;
}

// Group Event Processing
function processEventDataAsGroups(
  events,
  cssClass = "daypilot-event-badge in-progress"
) {
  const results = [];
  const eventsByDate = {}; // This will group events by date

  // Group events by their start date
  events.forEach((ev) => {
    const startDate = new Date(ev.start).toISOString().split("T")[0]; // Get YYYY-MM-DD

    if (!eventsByDate[startDate]) {
      eventsByDate[startDate] = {
        count: 0,
        firstEvent: ev, // Keep reference to first event for metadata
      };
    }
    eventsByDate[startDate].count++;
  });

  // Create a single event for each date with the count
  for (const [date, data] of Object.entries(eventsByDate)) {
    results.push({
      id: `group-${date}`, // Unique ID based on date
      start: data.firstEvent.start,
      end: data.firstEvent.end,
      text: `${data.count}`, // Show just the count
      barVisible: false,
      backColor: "transparent",
      borderColor: "transparent",
      moveDisabled: true,
      resizeDisabled: true,
      cssClass: `${cssClass} event-count-badge`, // Add special class for count badges
      tags: {
        originalEventId: data.firstEvent.id,
        estado: cssClass.includes("complete") ? "complete" : "in-progress",
        expedienteId: data.firstEvent.expedienteId,
        isGroup: true, // Mark this as a grouped event
        eventCount: data.count, // Store the count
        date: date, // Store the date for filtering
      },
    });
  }

  return results;
}

/**
 * Initializes and configures a DayPilot Month Calendar component
 * @param {Object} config - Configuration object for the calendar
 * @param {string} config.elementId - ID of the DOM element to render the calendar
 * @param {int} config.expedienteId - ID of the current Expediente
 * @param {Array} [config.eventsInProgress=[]] - Array of in-progress events to display
 * @param {Array} [config.eventsComplete=[]] - Array of completed events to display
 * @param {boolean} [config.disableSelection=false] - Whether to disable date selection
 * @returns {DayPilot.Month} - Initialized DayPilot calendar instance
 */
export function initializeCalendar({
  elementId,
  expedienteId,
  eventsInProgress = [],
  eventsComplete = [],
  disableSelection = false,
}) {
  // 1. Mobile Detection - Check if viewport width is 768px or less
  const isMobile = window.matchMedia("(max-width: 768px)").matches;

  // 2. Calendar Core Configuration
  const calendar = new DayPilot.Month(elementId, {
    theme: "calendar", // Base theme for styling
    showWeekends: true, // Always show weekends

    // Handle date cell clicks (day selection)
    onTimeRangeSelected: async (args) => {
      if (!disableSelection) {
        // Show modal with events for the selected day
        showDayEvents(args.start, calendar, expedienteId);
      }
    },

    // Handle event clicks
    onEventClick: async (args) => {
      // Show modal with events for the clicked event's day
      showDayEvents(args.e.start(), calendar, expedienteId);
    },

    // Disable all interactive modifications
    eventMoveHandling: "Disabled", // No dragging events
    timeRangeDoubleClickHandling: "Disabled", // No double-click actions
    eventRightClickHandling: "Disabled", // No right-click context menu
  });

  // 3. Event Processing and Initialization
  // Save the original events for the modal
  calendar.originalEvents = [
    ...processEventData(eventsInProgress, "daypilot-event-badge in-progress"),
    ...processEventData(eventsComplete, "daypilot-event-badge completed"),
  ];

  // Combine and process both in-progress and completed events
  calendar.events.list = [
    // Process in-progress events with appropriate CSS class
    ...processEventDataAsGroups(
      eventsInProgress,
      "daypilot-event-badge in-progress"
    ),
    // Process completed events with different CSS class
    ...processEventDataAsGroups(
      eventsComplete,
      "daypilot-event-badge completed"
    ),
  ];

  // Initialize the calendar with processed events
  calendar.init();

  // 4. Mobile Optimization
  // Truncate day headers on mobile for better fit
  if (isMobile) truncateDayHeaders();

  // 5. Navigation Controls Setup
  // Previous month button handler
  const prevBtn = document.getElementById("prev-month");
  if (prevBtn) {
    prevBtn.addEventListener("click", () => {
      calendar.startDate = calendar.startDate.addMonths(-1); // Go back 1 month
      calendar.update(); // Refresh display
      if (isMobile) truncateDayHeaders(); // Re-truncate headers if mobile
    });
  }

  // Today button handler (reset to current month)
  const todayBtn = document.getElementById("today");
  if (todayBtn) {
    todayBtn.addEventListener("click", () => {
      calendar.startDate = new DayPilot.Date(); // Set to current date
      calendar.update();
      if (isMobile) truncateDayHeaders();
    });
  }

  // Next month button handler
  const nextBtn = document.getElementById("next-month");
  if (nextBtn) {
    nextBtn.addEventListener("click", () => {
      calendar.startDate = calendar.startDate.addMonths(1); // Advance 1 month
      calendar.update();
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
    document.getElementById("prev-month").title = "Previous month";
    document.getElementById("next-month").title = "Next month";
    document.getElementById("today").title = "Go to today";
  }

  // Return the configured calendar instance
  return calendar;
}

function showDayEvents(date, calendar, expedienteId) {
  // 1. DOM Element References
  // Get modal components - date display, events list container, and create button
  const modalDateDisplay = document.querySelector(
    "#dayEventsModal .date-display"
  );
  const eventsList = document.getElementById("dayEventsList");
  const createBtn = document.getElementById("createNewEventBtn");
  const dayEventsModal = document.getElementById("dayEventsModal"); // Added modal reference

  // Validate all required elements exist
  if (!modalDateDisplay || !eventsList || !createBtn || !dayEventsModal) {
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

  console.log(eventsForDate);

  // 4. Event List Rendering
  eventsList.innerHTML = "";

  if (eventsForDate.length === 0) {
    eventsList.innerHTML =
      '<div class="text-center text-muted py-3">No events scheduled</div>';
  } else {
    eventsForDate.forEach((event) => {
      const eventElement = document.createElement("div");
      eventElement.className = `event-item mb-2 p-2 rounded ${
        event.tags?.estado || "in-progress"
      }`;
      eventElement.innerHTML = `
        <div class="d-flex justify-content-between align-items-center">
          <div>
            <strong>${event.text}</strong>
          </div>
          <button class="btn btn-sm btn-outline-primary view-event-btn" 
                  data-event-id="${event.id}"
                  data-is-complete="${
                    (event.tags?.estado || "in-progress") === "complete"
                  }">
            View
          </button>
        </div>
      `;
      eventsList.appendChild(eventElement);
    });
  }

  // 5. Event Button Handlers
  // Add click handlers to all "View" buttons
  document.querySelectorAll(".view-event-btn").forEach((btn) => {
    btn.addEventListener("click", (e) => {
      // Get event metadata from data attributes
      const eventId = e.target.getAttribute("data-event-id");
      const isComplete = e.target.getAttribute("data-is-complete") === "true";

      // Determine appropriate redirect URL based on completion status
      const redirectUrl = isComplete
        ? `/form/${eventId}` // Completed event URL
        : `/expediente/view-future-project/${eventId}`; // In-progress event URL

      // Navigate to event detail page
      window.location.href = redirectUrl;
    });
  });

  // 6. Create Button Setup
  // Configure create new event button
  createBtn.onclick = () => {
    // Navigate to event creation page with pre-filled date
    window.location.href = `/expediente/create-future-project?expedienteId=${expedienteId}&fecha_evaluacion=${dateStr}`;
  };

  // 7. Modal Display
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
