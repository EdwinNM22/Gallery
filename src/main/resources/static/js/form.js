document.addEventListener("DOMContentLoaded", function () {
  const fileUploadArea = document.getElementById("fileUploadArea");
  const fileInput = document.getElementById("fileInput");
  const filePreview = document.getElementById("filePreview");
  let currentFiles = []; // Track all selected files

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

  // Function to add new files to existing selection
  function addFiles(newFiles) {
    // Add new files to our tracking array
    for (let i = 0; i < newFiles.length; i++) {
      if (!newFiles[i].type.match("image.*")) continue;
      currentFiles.push(newFiles[i]);
    }

    // Update the file input with all files
    updateFileInput();

    // Refresh the preview
    refreshPreview();
  }

  // Function to update the actual file input element
  function updateFileInput() {
    const dt = new DataTransfer();
    currentFiles.forEach((file) => dt.items.add(file));
    fileInput.files = dt.files;
  }

  // Function to refresh the entire preview
  function refreshPreview() {
    filePreview.innerHTML = ""; // Clear previous previews

    currentFiles.forEach((file, index) => {
      const reader = new FileReader();

      reader.onload = function (e) {
        const previewContainer = document.createElement("div");
        previewContainer.className = "file-preview-container";
        previewContainer.dataset.index = index;

        previewContainer.innerHTML = `
          <div class="file-preview-item">
              <img src="${e.target.result}" alt="Preview" />
              <button class="file-preview-item-remove" data-index="${index}">
                  <i class="fas fa-times"></i>
              </button>
          </div>
          <div class="file-description-input">
              <textarea name="photoDescriptions" 
                       placeholder="Add description..." 
                       class="form-control"
                       rows="3"></textarea>
          </div>
        `;

        filePreview.appendChild(previewContainer);

        // Add remove button functionality
        previewContainer
          .querySelector(".file-preview-item-remove")
          .addEventListener("click", function () {
            removeFile(index);
          });
      };

      reader.readAsDataURL(file);
    });
  }

  // Function to remove a file
  function removeFile(index) {
    currentFiles.splice(index, 1); // Remove from our array
    updateFileInput(); // Update the file input
    refreshPreview(); // Refresh the display
  }
});
