document.addEventListener("DOMContentLoaded", function () {
  const deletionTracker = new Map(); // Tracks original state

  document.querySelectorAll(".photo-remove-btn").forEach((btn) => {
    btn.addEventListener("click", function () {
      const photoId = this.getAttribute("data-photo-id");
      const row = this.closest(".row");
      const undoBtn = row.querySelector(".photo-undo-btn");

      // Store original state
      if (!deletionTracker.has(photoId)) {
        deletionTracker.set(photoId, {
          element: row,
          description: row.querySelector("textarea").value,
        });
      }

      // Add to deletion list
      const deleteField = document.getElementById("fotosToDelete");
      let currentDeletions = deleteField.value
        ? new Set(deleteField.value.split(",").filter((id) => id !== ""))
        : new Set();

      currentDeletions.add(photoId);
      deleteField.value = Array.from(currentDeletions).join(",");

      // UI Changes
      row.classList.add("photo-removal-pending");
      this.style.display = "none";
      undoBtn.style.display = "block";
    });
  });

  // Undo functionality
  document.addEventListener("click", function (e) {
    if (e.target.closest(".photo-undo-btn")) {
      const undoBtn = e.target.closest(".photo-undo-btn");
      const photoId = undoBtn.getAttribute("data-photo-id");
      const deleteField = document.getElementById("fotosToDelete");

      // Remove from deletion list
      let currentDeletions = deleteField.value
        ? new Set(deleteField.value.split(",").filter((id) => id !== ""))
        : new Set();

      currentDeletions.delete(photoId);
      deleteField.value = Array.from(currentDeletions).join(",");

      // Restore UI
      const row = undoBtn.closest(".row");
      row.classList.remove("photo-removal-pending");
      undoBtn.style.display = "none";
      row.querySelector(".photo-remove-btn").style.display = "block";

      // Restore original description if needed
      if (deletionTracker.has(photoId)) {
        row.querySelector("textarea").value =
          deletionTracker.get(photoId).description;
      }
    }
  });
});
