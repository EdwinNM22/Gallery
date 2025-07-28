document.addEventListener("DOMContentLoaded", function () {
  const fileUploadArea = document.getElementById("fileUploadArea");
  const fileInput = document.getElementById("fileInput");
  const filePreview = document.getElementById("filePreview");

  // Handle click on upload area
  fileUploadArea.addEventListener("click", function () {
    fileInput.click();
  });

  // Handle file selection
  fileInput.addEventListener("change", function (e) {
    handleFiles(e.target.files);
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
      handleFiles(e.dataTransfer.files);
    }
  });

  // Function to handle selected files
  function handleFiles(files) {
    filePreview.innerHTML = ""; // Clear previous previews

    for (let i = 0; i < files.length; i++) {
      const file = files[i];

      if (!file.type.match("image.*")) {
        continue; // Skip non-image files
      }

      const reader = new FileReader();

      reader.onload = function (e) {
        const previewContainer = document.createElement("div");
        previewContainer.className = "file-preview-container"; // New wrapper
        previewContainer.dataset.index = i; // Store index

        previewContainer.innerHTML = `
        <div class="file-preview-item">
            <img src="${e.target.result}" alt="Preview" />
            <button class="file-preview-item-remove" data-index="${i}">
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
            removeFile(i);
            previewContainer.remove();
          });
      };

      reader.readAsDataURL(file);
    }
  }

  // Function to remove a file from the input
  function removeFile(index) {
    const dt = new DataTransfer();
    const files = fileInput.files;

    for (let i = 0; i < files.length; i++) {
      if (i !== index) {
        dt.items.add(files[i]);
      }
    }

    fileInput.files = dt.files;
  }
});
