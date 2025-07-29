// --- Event Processor ---
function processEventData(
  events,
  cssClass = "daypilot-event-badge in-progress"
) {
  const results = [];

  events.forEach((ev) => {
    const startDate = new Date(ev.start);
    let endDate = ev.end ? new Date(ev.end) : new Date(ev.start);
    endDate.setDate(endDate.getDate() + 1);

    if (startDate.toDateString() !== endDate.toDateString()) {
      const currentDate = new Date(startDate);

      while (currentDate < endDate) {
        const eventDate = new Date(currentDate);
        const eventEndDate = new Date(currentDate);
        eventEndDate.setDate(eventEndDate.getDate() + 1);

        results.push({
          id: `${ev.id}-${eventDate.toISOString().split("T")[0]}`,
          start: eventDate,
          end: eventEndDate,
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

        currentDate.setDate(currentDate.getDate() + 1);
      }
    } else {
      results.push({
        id: ev.id,
        start: ev.start,
        end: ev.end,
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
    }
  });

  return results;
}

// --- Calendar Initializer ---
export function initializeCalendar({
  elementId,
  expedienteId,
  eventsInProgress = [],
  eventsComplete = [],
  disableSelection = false,
}) {
  const calendar = new DayPilot.Month(elementId, {
    theme: "calendar",
    showWeekends: true,
    headerDateFormat: "MMMM yyyy",
    eventArrangement: "SideBySideStack",
    headerClickHandling: "Enabled",
    headerNavigationEnabled: true,
    timeRangeSelectHandling: "Enabled",
    selectMode: "Day",
    heightSpec: "Fixed",
    height: 600,
    width: "100%",
    onTimeRangeSelected: async (args) => {
      if (!disableSelection) {
        const clickedDate = args.start.toString("yyyy-MM-dd");
        window.location.href = `/expediente/create-future-project?expedienteId=${expedienteId}&fecha_evaluacion=${clickedDate}`;
      }
    },
    eventClickHandling: "Enabled",
    onEventClick: async (args) => {
      const eventId = args.e.data.tags.originalEventId;
      const isComplete = args.e.data.tags.estado === "complete";
      const redirectUrl = isComplete
        ? `/form/${eventId}`
        : `/expediente/view-future-project/${eventId}`;
      window.location.href = redirectUrl;
    },
    eventMoveHandling: "Disabled",
    eventResizeHandling: "Disabled",
    timeRangeDoubleClickHandling: "Disabled",
    eventRightClickHandling: "Disabled",
    eventHeight: 35,
    cellHeight: 120,
    eventStackingLineHeight: 24,
  });

  calendar.events.list = [
    ...processEventData(eventsInProgress, "daypilot-event-badge in-progress"),
    ...processEventData(eventsComplete, "daypilot-event-badge completed"),
  ];

  calendar.init();

  // Optional navigation controls
  const prevBtn = document.getElementById("prev-month");
  const todayBtn = document.getElementById("today");
  const nextBtn = document.getElementById("next-month");

  if (prevBtn) {
    prevBtn.addEventListener("click", () => {
      calendar.startDate = calendar.startDate.addMonths(-1);
      calendar.update();
    });
  }
  if (todayBtn) {
    todayBtn.addEventListener("click", () => {
      calendar.startDate = new DayPilot.Date();
      calendar.update();
    });
  }
  if (nextBtn) {
    nextBtn.addEventListener("click", () => {
      calendar.startDate = calendar.startDate.addMonths(1);
      calendar.update();
    });
  }

  return calendar;
}
