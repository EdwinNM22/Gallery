function showImageModal(imgElem) {
    var src = imgElem.getAttribute('src');
    document.getElementById('modalImage').setAttribute('src', src);
    $('#imageModal').modal('show');
}
// Optional: Add a hover effect
window.addEventListener('DOMContentLoaded', function() {
    var thumbs = document.querySelectorAll('.gallery-thumb-square');
    thumbs.forEach(function(thumb) {
        thumb.addEventListener('mouseenter', function() {
            thumb.style.boxShadow = '0 0 10px #007bff';
        });
        thumb.addEventListener('mouseleave', function() {
            thumb.style.boxShadow = '';
        });
    });
});

// Prevent unwanted scroll when opening the image modal (especially on mobile)
$(document).ready(function() {
    $('#imageModal').on('shown.bs.modal', function (e) {
        // Remove focus from any element inside the modal to prevent scroll
        if (document.activeElement && $('#imageModal').has(document.activeElement).length) {
            document.activeElement.blur();
        }
        // Optionally, prevent scroll restoration
        // window.scrollTo({ top: window.scrollY });
    });
});