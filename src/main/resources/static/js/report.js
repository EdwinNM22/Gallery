document.addEventListener('DOMContentLoaded', () => {
    const albumId = getAlbumId();
    const logsModalId = 'logsModal';

    const elements = {
        logsBtn: document.getElementById('logsBtn')
    };

    /* --------- Utilidades --------- */
    function getAlbumId() {
        return window.location.pathname.split('/')[2];
    }

    function postForm(url, data) {
        return fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams(data)
        });
    }

    function showModal(modalId) {
        const modalEl = document.getElementById(modalId);
        if (modalEl) new bootstrap.Modal(modalEl).show();
    }

    function hideModal(modalId) {
        const modalEl = document.getElementById(modalId);
        if (modalEl) bootstrap.Modal.getOrCreateInstance(modalEl).hide();
    }

    /* --------- Botón: Ver historial --------- */
    elements.logsBtn?.addEventListener('click', () => {
        fetch(`/albumes/${albumId}/reports`)
            .then(res => res.ok ? res.text() : Promise.reject(res.status))
            .then(html => {
                document.getElementById(logsModalId)?.remove();
                document.body.insertAdjacentHTML('beforeend', html);
                setupLogsModalTabs();
                showModal(logsModalId);
            })
            .catch(err => console.error('Could not load logs:', err));
    });

    /* --------- Tabs del modal de historial --------- */
    function setupLogsModalTabs() {
        document.querySelectorAll(`#${logsModalId} [data-bs-toggle="tab"]`).forEach(tab => {
            tab.addEventListener('click', e => {
                e.preventDefault();
                const target = tab.getAttribute('data-bs-target');
                document.querySelectorAll(`#${logsModalId} .tab-pane`).forEach(p => p.classList.remove('show', 'active'));
                document.querySelectorAll(`#${logsModalId} .nav-link`).forEach(n => n.classList.remove('active'));

                tab.classList.add('active');
                document.querySelector(target)?.classList.add('show', 'active');
            });
        });
    }
});

/* --------- Función global para enviar reporte --------- */
function sendReport(contenido, modalToHide = null) {
    const albumId = window.location.pathname.split('/')[2];
    if (!contenido) return alert('Report is empty');

    console.log('Enviando reporte...', { contenido, modalToHide });

    return fetch(`/albumes/${albumId}/reports/send`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({ contenido })
    })
        .then(response => {
            console.log('Respuesta recibida:', response.status);
            if (response.ok) {
                alert('Report sent ✔');
                try {
                    if (modalToHide) {
                        hideModal(modalToHide);
                    }
                    document.getElementById('fillReportBtn')?.setAttribute('disabled', 'true');
                } catch (e) {
                    console.error('Error en operaciones posteriores:', e);
                }
                return response;
            } else if (response.status === 403) {
                alert('You do not have permission to perform this action');
                throw new Error('Forbidden');
            } else if (response.status === 409) {
                alert('A report has already been sent for this album');
                throw new Error('Conflict');
            } else {
                throw new Error('Unexpected status: ' + response.status);
            }
        })
        .catch(error => {
            console.error('Error en sendReport:', error);
            if (error.message !== 'Forbidden' && error.message !== 'Conflict') {
                alert('Error while sending report');
            }
            throw error; // Re-lanzamos el error para que pueda ser capturado por llamadas superiores si es necesario
        });
}

/* --------- Guardar edición inline --------- */
function saveEditedReport(btn) {
    const id = btn.dataset.id;
    const contenido = document.getElementById('txt_' + id)?.value.trim();
    const albumId = window.location.pathname.split('/')[2];

    if (!contenido) return alert('Report is empty');

    fetch(`/albumes/${albumId}/reports/${id}/edit`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({ contenido })
    })
        .then(() => alert('Saved ✔'))
        .catch(() => alert('Error while saving changes'));
}

/* --------- Crear reporte desde modal de historial --------- */
function saveNewReportFromHistory() {
    const contenido = document.getElementById('newReportArea')?.value.trim();
    sendReport(contenido, 'logsModal');
}
