// Variables globales para calendario y Gantt
let calendar;
let calendarVisible = false;
let currentGanttDate = new Date();
let ganttVisible = false;

// Función para inicializar el calendario
function initCalendar() {
    const calendarEl = document.getElementById('calendar');

    // Ocultar el Gantt inicialmente
    document.getElementById('ganttSection').style.display = 'none';

    calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: 'dayGridMonth',
        locale: 'en',
        headerToolbar: {
            left: 'prev,next today',
            center: 'title',
            right: 'dayGridMonth,customWeekView,timeGridDay'
        },
        customButtons: {
            customWeekView: {
                text: 'Week (Gantt)',
                click: function() {
                    // Ocultar calendario y mostrar Gantt
                    document.getElementById('calendarSection').style.display = 'none';
                    document.getElementById('ganttSection').style.display = 'block';

                    // Sincronizar fecha con el calendario
                    currentGanttDate = calendar.getDate();
                    renderGantt();

                    // Actualizar título
                    const viewTitle = calendar.view.title;
                    document.querySelector('.gantt-header h3').textContent = `Projects Gantt View - ${viewTitle}`;
                }
            }
        },
        buttonText: {
            today: 'Today',
            month: 'Month',
            day: 'Day'
        },
        events: function(fetchInfo, successCallback, failureCallback) {
            fetch('/albumes/calendar-events')
                .then(response => response.json())
                .then(data => {
                    const events = data.map(album => {
                        let eventColor, className;
                        switch(album.estado) {
                            case 'completado':
                                eventColor = '#6c757d';
                                className = 'completed-event';
                                break;
                            case 'anulado':
                                eventColor = '#f94144';
                                className = 'cancelled-event';
                                break;
                            default:
                                eventColor = '#006fa6';
                                className = 'in-progress-event';
                        }

                        const event = {
                            id: album.id,
                            title: album.nombre,
                            start: album.fechaInicio,
                            backgroundColor: eventColor,
                            borderColor: eventColor,
                            className: className,
                            extendedProps: {
                                description: album.descripcion,
                                location: album.ubicacion,
                                status: album.estado,
                                notes: album.notas,
                                horaFin: album.horaFin
                            }
                        };

                        if (album.horaInicio) {
                            const startDateTime = `${album.fechaInicio}T${album.horaInicio}`;
                            event.start = startDateTime;

                            if (album.horaFin) {
                                const endDate = album.fechaFin ? album.fechaFin : album.fechaInicio;
                                const endDateTime = `${endDate}T${album.horaFin}`;
                                event.end = endDateTime;
                                event.allDay = false;
                            } else {
                                event.allDay = true;
                                if (album.fechaFin) {
                                    const endDate = new Date(album.fechaFin);
                                    endDate.setDate(endDate.getDate() + 1);
                                    event.end = endDate.toISOString().split('T')[0];
                                }
                            }
                        } else {
                            event.allDay = true;
                            if (album.fechaFin) {
                                const endDate = new Date(album.fechaFin);
                                endDate.setDate(endDate.getDate() + 1);
                                event.end = endDate.toISOString().split('T')[0];
                            }
                        }

                        return event;
                    });
                    successCallback(events);
                })
                .catch(error => {
                    console.error('Error al cargar eventos:', error);
                    failureCallback(error);
                });
        },
        eventClick: function(info) {
            showContextMenu(info.jsEvent, info.event);
            info.jsEvent.preventDefault();
        },
        dateClick: function(info) {
            if (canEdit) {
                const fechaInicioInput = document.getElementById('fechaInicio');
                const horaInicioInput = document.getElementById('horaInicio');
                const horaFinInput = document.getElementById('horaFin');
                const fechaSeleccionada = info.dateStr;

                fechaInicioInput.value = fechaSeleccionada;

                if (info.view.type === 'timeGridDay') {
                    const now = new Date();
                    const hours = now.getHours().toString().padStart(2, '0');
                    const minutes = now.getMinutes().toString().padStart(2, '0');
                    horaInicioInput.value = `${hours}:${minutes}`;

                    const endTime = new Date(now.getTime() + 60 * 60 * 1000);
                    const endHours = endTime.getHours().toString().padStart(2, '0');
                    const endMinutes = endTime.getMinutes().toString().padStart(2, '0');
                    horaFinInput.value = `${endHours}:${minutes}`;
                } else {
                    horaInicioInput.value = '';
                    horaFinInput.value = '';
                }

                document.getElementById('fechaFin').value = '';
                projectModal.show();

                document.getElementById('projectModalLabel').innerHTML =
                    '<i class="fas fa-plus-circle"></i> New Project for ' +
                    info.date.toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' });
            }
        },
        views: {
            timeGridDay: {
                dayHeaderFormat: { weekday: 'long', day: 'numeric', month: 'short' },
                slotLabelFormat: {
                    hour: '2-digit',
                    minute: '2-digit',
                    hour12: false
                },
                allDaySlot: false,
                expandRows: true,
                nowIndicator: true,
                slotMinTime: '00:00:00',
                slotMaxTime: '24:00:00',
                slotDuration: '00:30:00',
                eventTimeFormat: {
                    hour: '2-digit',
                    minute: '2-digit',
                    hour12: false
                }
            },
            dayGridMonth: {
                dayHeaderFormat: { weekday: 'short' },
                titleFormat: { year: 'numeric', month: 'long' }
            }
        },
        eventContent: function(arg) {
            const eventEl = document.createElement('div');
            eventEl.className = 'fc-event-content';
            eventEl.style.whiteSpace = 'normal';
            eventEl.style.padding = '4px 6px';
            eventEl.style.borderRadius = '4px';

            const isAllDay = arg.event.allDay ||
                (!arg.event.extendedProps.horaFin &&
                    (!arg.event.end || !arg.event.end.includes('T')));

            if (!isAllDay) {
                const timeEl = document.createElement('div');
                timeEl.className = 'fc-event-time';
                timeEl.style.fontSize = '0.85em';
                timeEl.style.marginBottom = '2px';
                timeEl.style.fontWeight = 'bold';

                const startTime = arg.event.start ?
                    arg.event.start.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit', hour12: false}) : '';

                let timeText = startTime;
                if (arg.event.end) {
                    const endTime = arg.event.end.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit', hour12: false});
                    timeText = `${startTime} - ${endTime}`;
                }

                timeEl.textContent = timeText;
                eventEl.appendChild(timeEl);
            } else {
                const allDayEl = document.createElement('div');
                allDayEl.className = 'fc-event-time';
                allDayEl.style.fontSize = '0.85em';
                allDayEl.style.marginBottom = '2px';
                allDayEl.style.fontWeight = 'bold';
                allDayEl.textContent = 'All day';
                eventEl.appendChild(allDayEl);
            }

            const titleEl = document.createElement('div');
            titleEl.className = 'fc-event-title';
            titleEl.textContent = arg.event.title;
            titleEl.style.fontWeight = 'bold';
            titleEl.style.fontSize = '0.9em';

            if (arg.event.extendedProps.status === 'completado') {
                titleEl.style.textDecoration = 'line-through';
                titleEl.style.opacity = '0.8';
            } else if (arg.event.extendedProps.status === 'anulado') {
                titleEl.style.textDecoration = 'line-through';
            }

            eventEl.appendChild(titleEl);

            if (arg.event.extendedProps.location) {
                const locationEl = document.createElement('div');
                locationEl.className = 'fc-event-location';
                locationEl.style.fontSize = '0.8em';
                locationEl.style.marginTop = '2px';
                locationEl.textContent = arg.event.extendedProps.location;
                eventEl.appendChild(locationEl);
            }

            return { domNodes: [eventEl] };
        },
        height: 'auto',
        nowIndicator: true,
        viewDidMount: function(view) {
            if (view.type !== 'timeGridWeek') {
                document.getElementById('calendarSection').style.display = 'block';
                document.getElementById('ganttSection').style.display = 'none';
            }
        }
    });

    calendar.render();

    // Configurar navegación del Gantt
    document.getElementById('prevWeekBtn').addEventListener('click', function() {
        calendar.prev();
        if (document.getElementById('ganttSection').style.display === 'block') {
            currentGanttDate = calendar.getDate();
            renderGantt();
        }
    });

    document.getElementById('nextWeekBtn').addEventListener('click', function() {
        calendar.next();
        if (document.getElementById('ganttSection').style.display === 'block') {
            currentGanttDate = calendar.getDate();
            renderGantt();
        }
    });

    document.getElementById('currentWeekBtn').addEventListener('click', function() {
        calendar.today();
        if (document.getElementById('ganttSection').style.display === 'block') {
            currentGanttDate = calendar.getDate();
            renderGantt();
        }
    });

    // Inicializar estado del calendario
    calendarVisible = true;
    document.getElementById('calendarSection').classList.add('visible');
    document.getElementById('toggleCalendarBtn').innerHTML = '<i class="fas fa-calendar-times"></i> Hide Calendar';
}

// Función para inicializar el Gantt
function initGantt() {
    // Botón para mostrar/ocultar Gantt
    const toggleGanttBtn = document.createElement('button');
    toggleGanttBtn.id = 'toggleGanttBtn';
    toggleGanttBtn.className = 'btn btn-primary btn-rounded';
    toggleGanttBtn.innerHTML = '<i class="fas fa-chart-gantt"></i> Show Gantt';

    // Insertar el botón junto a los otros botones de control
    const buttonGroup = document.querySelector('.agenda-header-content .button-group');
    if (buttonGroup) {
        buttonGroup.appendChild(toggleGanttBtn);
    }

    // Evento para mostrar/ocultar Gantt
    toggleGanttBtn.addEventListener('click', function() {
        ganttVisible = !ganttVisible;
        const ganttSection = document.getElementById('ganttSection');

        if (ganttVisible) {
            ganttSection.style.display = 'block';
            toggleGanttBtn.innerHTML = '<i class="fas fa-chart-gantt"></i> Hide Gantt';
            renderGantt();
        } else {
            ganttSection.style.display = 'none';
            toggleGanttBtn.innerHTML = '<i class="fas fa-chart-gantt"></i> Show Gantt';
        }
    });

    // Eventos para navegación semanal
    document.getElementById('prevWeekBtn').addEventListener('click', function() {
        currentGanttDate.setDate(currentGanttDate.getDate() - 7);
        renderGantt();
    });

    document.getElementById('nextWeekBtn').addEventListener('click', function() {
        currentGanttDate.setDate(currentGanttDate.getDate() + 7);
        renderGantt();
    });

    document.getElementById('currentWeekBtn').addEventListener('click', function() {
        currentGanttDate = new Date();
        renderGantt();
    });
}

// Función para renderizar el diagrama de Gantt
function renderGantt() {
    // Usar la fecha actual del calendario
    const currentDate = calendar.getDate();
    const weekStart = getWeekStart(currentDate);
    const weekEnd = new Date(weekStart);
    weekEnd.setDate(weekStart.getDate() + 6);

    // Formatear el rango de fechas para que coincida con FullCalendar
    const titleFormat = {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
        weekday: 'short'
    };

    const ganttTitle = `Week of ${weekStart.toLocaleDateString('en-US', titleFormat)}`;
    document.getElementById('ganttWeekRange').textContent = ganttTitle;

    // Obtener datos del servidor
    fetch('/albumes/gantt-data')
        .then(response => response.json())
        .then(projects => {
            // Filtrar proyectos que coincidan con la semana actual
            const filteredProjects = projects.filter(project => {
                if (!project.fechaInicio || !project.fechaFin) return false;

                const projectStart = new Date(project.fechaInicio);
                const projectEnd = new Date(project.fechaFin);

                // Ajustar para eventos de todo el día
                if (!project.horaInicio) {
                    projectStart.setHours(0, 0, 0, 0);
                    projectEnd.setHours(23, 59, 59, 999);
                }

                // Verificar si el proyecto se superpone con la semana actual
                return (projectStart <= weekEnd && projectEnd >= weekStart);
            });

            renderGanttProjectsList(filteredProjects);
            renderGanttTimeline(filteredProjects, weekStart);
        })
        .catch(error => {
            console.error('Error loading Gantt data:', error);
            showToast('Error loading Gantt data');
        });
}

// Función para renderizar la lista de proyectos en el sidebar
function renderGanttProjectsList(projects) {
    const projectsList = document.getElementById('ganttProjectsList');
    projectsList.innerHTML = '';

    projects.forEach(project => {
        const projectItem = document.createElement('div');
        projectItem.className = 'gantt-project-item';
        projectItem.setAttribute('data-id', project.id);

        projectItem.innerHTML = `
        <div class="gantt-project-name" title="${project.nombre}">${project.nombre}</div>
    `;

        projectsList.appendChild(projectItem);
    });
}

// Función para renderizar la línea de tiempo del Gantt (mejorada)
function renderGanttTimeline(projects, weekStart) {
    const daysContainer = document.querySelector('.gantt-timeline-days');
    const barsContainer = document.getElementById('ganttBarsContainer');
    const weekEnd = new Date(weekStart);
    weekEnd.setDate(weekStart.getDate() + 6);

    // Limpiar contenedores
    daysContainer.innerHTML = '';
    barsContainer.innerHTML = '';

    // Crear encabezados de días con horas
    for (let i = 0; i < 7; i++) {
        const day = new Date(weekStart);
        day.setDate(weekStart.getDate() + i);

        const dayHeader = document.createElement('div');
        dayHeader.className = 'gantt-day-header';

        // Mostrar nombre del día y fecha
        dayHeader.innerHTML = `
        <div class="gantt-day-name">${day.toLocaleDateString('en-US', { weekday: 'short' })}</div>
        <div class="gantt-day-date">${day.getDate()}/${day.getMonth() + 1}</div>
    `;

        daysContainer.appendChild(dayHeader);
    }

    // Crear filas y barras para cada proyecto
    projects.forEach((project, index) => {
        const row = document.createElement('div');
        row.className = 'gantt-row';
        row.setAttribute('data-id', project.id);

        // Calcular posición y duración de la barra
        const startDate = new Date(project.fechaInicio);
        const endDate = new Date(project.fechaFin);

        // Ajustar para eventos de todo el día
        if (!project.horaInicio) {
            startDate.setHours(0, 0, 0, 0);
            endDate.setHours(23, 59, 59, 999);
        }

        // Calcular posición y ancho de la barra
        const weekStartTime = weekStart.getTime();
        const weekEndTime = weekEnd.getTime() + 86400000; // Fin del último día

        // Ajustar fechas del proyecto para que no excedan los límites de la semana
        const barStart = Math.max(startDate.getTime(), weekStartTime);
        const barEnd = Math.min(endDate.getTime(), weekEndTime);

        // Calcular porcentajes para posición y ancho
        const totalWeekDuration = weekEndTime - weekStartTime;
        const barOffset = ((barStart - weekStartTime) / totalWeekDuration) * 100;
        const barWidth = ((barEnd - barStart) / totalWeekDuration) * 100;

        // Crear la barra del proyecto
        const bar = document.createElement('div');
        bar.className = `gantt-bar ${project.estado}`;
        bar.style.left = `${barOffset}%`;
        bar.style.width = `${barWidth}%`;

        // Mostrar nombre del proyecto solo si hay espacio suficiente
        if (barWidth > 20) {
            bar.textContent = project.nombre;
        }

        // Tooltip con más información
        bar.title = `${project.nombre}\nInicio: ${formatDateTime(startDate)}\nFin: ${formatDateTime(endDate)}\nEstado: ${project.estado}`;

        // Evento para mostrar detalles al hacer clic
        bar.addEventListener('click', (e) => {
            e.stopPropagation();
            showAlbumDetailsById(project.id);
        });

        row.appendChild(bar);
        barsContainer.appendChild(row);
    });
}

// Función auxiliar para formatear fecha y hora
function formatDateTime(date) {
    if (!date) return '';
    const options = {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    };
    return date.toLocaleDateString('en-US', options);
}

// Función auxiliar para obtener el inicio de la semana (lunes)
function getWeekStart(date) {
    const day = date.getDay();
    const diff = date.getDate() - day + (day === 0 ? -6 : 1); // Ajustar para que la semana empiece en lunes
    return new Date(date.setDate(diff));
}

// Función auxiliar para formatear fechas
function formatDate(date) {
    if (!date) return '';
    return date.toLocaleDateString('en-US', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

// Inicializar Gantt cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', function() {
    initGantt();
});