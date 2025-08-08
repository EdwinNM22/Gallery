document.addEventListener("DOMContentLoaded", function () {
  const fileUploadArea = document.getElementById("fileUploadArea");
  const fileInput = document.getElementById("fileInput");
  const filePreview = document.getElementById("filePreview");
  window.currentFiles = [];

  if (fileUploadArea === null) return;

  // Handle click on upload area
  fileUploadArea.addEventListener("click", function () {
    fileInput.click();
  });

  // Handle file selection
  fileInput.addEventListener("change", function (e) {
    addFiles(e.target.files);
  });

  // Handle drag and drop
  fileUploadArea.addEventListener("dragover", function (e) {
    e.preventDefault();
    fileUploadArea.style.borderColor = "var(--accent-blue)";
    fileUploadArea.style.backgroundColor = "rgba(58, 123, 213, 0.1)";
  });

  fileUploadArea.addEventListener("dragleave", function () {
    fileUploadArea.style.borderColor = "var(--border-color)";
    fileUploadArea.style.backgroundColor = "";
  });

  fileUploadArea.addEventListener("drop", function (e) {
    e.preventDefault();
    fileUploadArea.style.borderColor = "var(--border-color)";
    fileUploadArea.style.backgroundColor = "";

    if (e.dataTransfer.files.length) {
      addFiles(e.dataTransfer.files);
    }
  });


  window.refreshPreview = function() {
    filePreview.innerHTML = "";
    if (window.currentFiles.length === 0) return;

    window.currentFiles.forEach((item) => {
      const reader = new FileReader();
      reader.onload = function(e) {
        const previewContainer = document.createElement("div");
        previewContainer.className = "file-preview-container";
        previewContainer.dataset.fileId = item.id; // Use unique ID instead of index

        previewContainer.innerHTML = `
          <div class="file-preview-item">
            <img src="${e.target.result}" alt="Preview" />
            <button class="file-preview-item-remove" data-file-id="${item.id}">
              <i class="fas fa-times"></i>
            </button>
          </div>
          <div class="file-description-input">
            <textarea name="photoDescriptions" 
                     placeholder="Add description..." 
                     class="form-control photo-description"
                     rows="3">${item.description || ''}</textarea>
          </div>
        `;

        filePreview.appendChild(previewContainer);

        // Update description when changed
        previewContainer.querySelector('.photo-description')
          .addEventListener('input', function() {
            const fileItem = window.currentFiles.find(f => f.id === item.id);
            if (fileItem) fileItem.description = this.value;
          });

        // Handle remove button
        previewContainer.querySelector(".file-preview-item-remove")
          .addEventListener("click", function() {
            removeFile(item.id); // Remove by ID instead of index
          });
      };
      reader.readAsDataURL(item.file);
    });
  };

  function updateFileInput() {
    const dt = new DataTransfer();
    window.currentFiles.forEach(item => dt.items.add(item.file));
    fileInput.files = dt.files;
  }

  function addFiles(newFiles) {
    for (let i = 0; i < newFiles.length; i++) {
      if (!newFiles[i].type.match("image.*")) continue;
      window.currentFiles.push({
        id: Date.now() + Math.random().toString(36).substr(2, 9), // Unique ID
        file: newFiles[i],
        description: ""
      });
    }
    updateFileInput();
    window.refreshPreview();
  }

  function removeFile(fileId) {
    // Find index of file to remove
    const index = window.currentFiles.findIndex(item => item.id === fileId);
    if (index !== -1) {
      window.currentFiles.splice(index, 1);
      updateFileInput();
      window.refreshPreview();
    }
  }
});