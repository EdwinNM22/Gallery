let selectedFiles = [];
let descriptions = {};

$(document).ready(function () {
    // Maintenance history toggle functionality
    function toggleFechaUltimoMantenimiento() {
        var checkbox = document.getElementById('mantenimientoHistoriaCheckbox');
        var row = document.getElementById('fechaUltimoMantenimientoRow');
        if (checkbox.checked) {
            row.style.display = 'block';
        } else {
            row.style.display = 'none';
            // Optionally clear the date field when hidden
            var input = row.querySelector('input[name="fechaUltimoMantenimiento"]');
            if (input) input.value = '';
        }
    }

    // Initialize maintenance history toggle on page load
    toggleFechaUltimoMantenimiento();

    // Add event listener for maintenance history checkbox
    const mantenimientoCheckbox = document.getElementById('mantenimientoHistoriaCheckbox');
    if (mantenimientoCheckbox) {
        mantenimientoCheckbox.addEventListener('change', toggleFechaUltimoMantenimiento);
    }

    const fileUploadArea = document.getElementById('fileUploadArea');
    const fileInput = document.getElementById('fileInput');
    const filePreview = document.getElementById('filePreview');
    const uploadProgress = document.getElementById('uploadProgress');
    const submitBtn = document.getElementById('submitBtn');

    fileUploadArea.addEventListener('click', () => fileInput.click());

    fileUploadArea.addEventListener('dragover', (e) => {
        e.preventDefault();
        fileUploadArea.classList.add('dragover');
    });

    fileUploadArea.addEventListener('dragleave', () => {
        fileUploadArea.classList.remove('dragover');
    });

    fileUploadArea.addEventListener('drop', (e) => {
        e.preventDefault();
        fileUploadArea.classList.remove('dragover');
        addFiles(e.dataTransfer.files);
    });

    fileInput.addEventListener('change', (e) => {
        const files = e.target.files;
        if (files.length > 10) {
            alert('Maximum 10 images allowed');
            return;
        }
        addFiles(files);
        fileInput.value = '';
    });

    function addFiles(newFiles) {
        Array.from(newFiles).forEach((file) => {
            if (file.type.startsWith('image/')) {
                const exists = selectedFiles.some(f =>
                    f.name === file.name &&
                    f.size === file.size &&
                    f.lastModified === file.lastModified
                );

                if (!exists) {
                    selectedFiles.push(file);
                }
            }
        });

        updateFilePreview();
        updateUploadAreaText();
    }


    function saveDescriptions() {
        const textareas = filePreview.querySelectorAll('textarea[name="descripciones"]');
        textareas.forEach((textarea, i) => {
            if (selectedFiles[i]) {
                const key = getFileKey(selectedFiles[i]);
                descriptions[key] = textarea.value;
            }
        });
    }

    function getFileKey(file) {
        return `${file.name}_${file.size}_${file.lastModified}`;
    }

    function updateFilePreview() {
        while (filePreview.firstChild) {
            filePreview.removeChild(filePreview.firstChild);
        }

        if (selectedFiles.length === 0) {
            filePreview.innerHTML = '<p class="text-muted">No se han seleccionado archivos</p>';
            return;
        }

        const readersPromises = selectedFiles.map(file => {
            return new Promise(resolve => {
                const reader = new FileReader();
                reader.onload = e => resolve({ file, src: e.target.result });
                reader.readAsDataURL(file);
            });
        });

        Promise.all(readersPromises).then(results => {
            results.forEach(({ file, src }) => {
                const container = document.createElement('div');
                container.className = 'file-container';

                const img = document.createElement('img');
                img.src = src;
                img.alt = file.name;
                img.title = file.name;

                const descArea = document.createElement('div');
                descArea.className = 'desc-area';

                const descLabel = document.createElement('label');
                descLabel.textContent = 'Descripción (opcional):';
                descLabel.style.display = 'block';
                descLabel.style.marginBottom = '5px';

                const descTextarea = document.createElement('textarea');
                descTextarea.name = 'descripciones';
                descTextarea.className = 'form-control';
                descTextarea.placeholder = 'Descripción para esta foto';
                descTextarea.style.resize = 'vertical';

                const key = getFileKey(file);
                descTextarea.value = descriptions[key] || '';

                descArea.appendChild(descLabel);
                descArea.appendChild(descTextarea);

                const removeBtn = document.createElement('button');
                removeBtn.className = 'remove-file-btn';
                removeBtn.innerHTML = '<i class="fas fa-times"></i>';

                removeBtn.addEventListener('click', e => {
                    e.preventDefault();
                    e.stopPropagation();
                    saveDescriptions();
                    const indexToRemove = selectedFiles.findIndex(f => getFileKey(f) === key);
                    if (indexToRemove > -1) {
                        removeFile(indexToRemove);
                    }
                });

                container.appendChild(img);
                container.appendChild(descArea);
                container.appendChild(removeBtn);

                filePreview.appendChild(container);
            });
        });
    }

    function removeFile(index) {
        selectedFiles.splice(index, 1);
        updateFilePreview();
        updateUploadAreaText();
    }

    function updateUploadAreaText() {
        const uploadText = fileUploadArea.querySelector('h5');
        uploadText.textContent = selectedFiles.length > 0
            ? `${selectedFiles.length} archivo(s) seleccionado(s)`
            : 'Arrastre las fotos aquí o haga clic para seleccionar';
    }

    $('#mainForm').on('submit', function (e) {
        e.preventDefault();
        saveDescriptions();

        // Validate required fields
        const requiredFields = $(this).find('[required]');
        let isValid = true;
        requiredFields.each(function () {
            if (!$(this).val()) {
                isValid = false;
                $(this).addClass('is-invalid');
            } else {
                $(this).removeClass('is-invalid');
            }
        });

        if (!isValid) {
            alert('Por favor complete todos los campos requeridos.');
            return;
        }

        // Disable submit button and show loading state
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Enviando...';

        // Show progress bar
        uploadProgress.style.display = 'block';
        const progressBar = uploadProgress.querySelector('.progress-bar');
        progressBar.style.width = '0%';

        // Create FormData
        const formData = new FormData(this);

        // Clear and add files one by one
        formData.delete('fotos');
        selectedFiles.forEach((file) => {
            formData.append('fotos', file);
        });

        // Add descriptions
        formData.delete('descripciones');
        selectedFiles.forEach((file, index) => {
            const key = getFileKey(file);
            formData.append(`descripciones[${index}]`, descriptions[key] || '');
        });


        // Add unique timestamp to prevent caching
        const url = $(this).attr('action') + '?' + new Date().getTime();

        // Use XMLHttpRequest for better progress tracking
        const xhr = new XMLHttpRequest();
        xhr.open('POST', url, true);
        xhr.timeout = 300000; // 5 minutes timeout for mobile devices

        // Progress tracking
        xhr.upload.onprogress = function(e) {
            if (e.lengthComputable) {
                const percentComplete = Math.round((e.loaded / e.total) * 100);
                progressBar.style.width = percentComplete + '%';
                progressBar.setAttribute('aria-valuenow', percentComplete);
            }
        };

        xhr.onload = function() {
            if (xhr.status === 200) {
                location.reload();
            } else {
                console.error('Error al enviar formulario:', xhr.statusText);
                alert('Error al enviar el formulario. Intente nuevamente.');
                resetFormState();
            }
        };

        xhr.ontimeout = function() {
            console.error('Tiempo de espera agotado');
            alert('El servidor tardó demasiado en responder. Intente nuevamente.');
            resetFormState();
        };

        xhr.onerror = function() {
            console.error('Error de conexión');
            alert('Error de conexión. Verifique su conexión a internet.');
            resetFormState();
        };

        xhr.send(formData);
    });

    function resetFormState() {
        submitBtn.disabled = false;
        submitBtn.innerHTML = '<i class="fas fa-paper-plane"></i> Soumettre Formulaire';
        uploadProgress.style.display = 'none';
    }
});