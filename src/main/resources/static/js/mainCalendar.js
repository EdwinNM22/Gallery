//================LOGICA PARA EXPANDIR SECCIONES===================//
function toggleSection(id) {
    const section = document.getElementById(id);
    section.classList.toggle('active');
}
//================FIN DE LOGICA PARA EXPANDIR SECCIONES============//


//================BOTONES DE EDITAR Y ELIMNAR===================//
// Función para editar el álbum
function editAlbum(event, albumId) {
    event.stopPropagation();
    event.preventDefault();

    if (albumId) {
        window.location.href = `/albumes/edit/${albumId}`;
    }
}
// Función para eliminar un álbum
function deleteAlbum(event, albumId) {
    if (event) {
        event.stopPropagation();
    }

    // Inicializar el modal de Bootstrap
    const confirmModal = new bootstrap.Modal(document.getElementById('confirmDeleteModal'));

    // Configurar el botón de confirmación
    const confirmBtn = document.getElementById('confirmDeleteBtn');
    confirmBtn.onclick = function() {
        fetch(`/albumes/delete/${albumId}`, {
            method: 'GET'
        })
            .then(response => {
                if (response.redirected) {
                    window.location.href = response.url;
                } else {
                    throw new Error('Error al eliminar el proyecto');
                }
            })
            .catch(error => {
                console.error('Error:', error);
            });

        confirmModal.hide();
    };

    // Mostrar el modal
    confirmModal.show();
}

//================FIN DE LOGICA PARA BOTONES DE ACCION===================//