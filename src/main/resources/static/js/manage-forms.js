// manage-forms.js - JavaScript for the form management page

document.addEventListener('DOMContentLoaded', function() {

    // Auto-hide alerts after 5 seconds
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(function(alert) {
        setTimeout(function() {
            if (alert && alert.parentNode) {
                alert.style.transition = 'opacity 0.5s ease-out';
                alert.style.opacity = '0';
                setTimeout(function() {
                    if (alert && alert.parentNode) {
                        alert.parentNode.removeChild(alert);
                    }
                }, 500);
            }
        }, 5000);
    });

    // Add smooth scrolling for table rows
    const tableRows = document.querySelectorAll('.table tbody tr');
    tableRows.forEach(function(row) {
        row.addEventListener('click', function(e) {
            // Don't trigger if clicking on buttons or links
            if (e.target.tagName === 'BUTTON' || e.target.tagName === 'A' ||
                e.target.closest('button') || e.target.closest('a')) {
                return;
            }

            // Add a subtle highlight effect
            this.style.transform = 'scale(1.01)';
            setTimeout(() => {
                this.style.transform = 'scale(1)';
            }, 150);
        });
    });

    // Enhanced delete confirmation
    const deleteButtons = document.querySelectorAll('button[onclick*="confirm"]');
    deleteButtons.forEach(function(button) {
        button.addEventListener('click', function(e) {
            const confirmed = confirm('Are you sure you want to delete this form? This action cannot be undone.');
            if (!confirmed) {
                e.preventDefault();
                return false;
            }

            // Show loading state
            const originalText = this.innerHTML;
            this.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Deleting...';
            this.disabled = true;

            // Re-enable after a delay (in case of error)
            setTimeout(() => {
                this.innerHTML = originalText;
                this.disabled = false;
            }, 3000);
        });
    });

    // Add keyboard navigation for table
    let currentRowIndex = -1;
    const tableBody = document.querySelector('.table tbody');

    if (tableBody) {
        document.addEventListener('keydown', function(e) {
            const rows = tableBody.querySelectorAll('tr');
            if (rows.length === 0) return;

            switch(e.key) {
                case 'ArrowDown':
                    e.preventDefault();
                    currentRowIndex = Math.min(currentRowIndex + 1, rows.length - 1);
                    highlightRow(rows, currentRowIndex);
                    break;
                case 'ArrowUp':
                    e.preventDefault();
                    currentRowIndex = Math.max(currentRowIndex - 1, 0);
                    highlightRow(rows, currentRowIndex);
                    break;
                case 'Enter':
                    if (currentRowIndex >= 0 && currentRowIndex < rows.length) {
                        const viewButton = rows[currentRowIndex].querySelector('a[href*="/form/"]');
                        if (viewButton) {
                            viewButton.click();
                        }
                    }
                    break;
            }
        });
    }

    function highlightRow(rows, index) {
        rows.forEach((row, i) => {
            if (i === index) {
                row.style.backgroundColor = 'rgba(67, 97, 238, 0.25)';
                row.style.border = '1px solid var(--accent-blue)';
            } else {
                row.style.backgroundColor = '';
                row.style.border = '';
            }
        });
    }

    // Add search functionality (if needed in the future)
    function addSearchFunctionality() {
        const searchInput = document.createElement('input');
        searchInput.type = 'text';
        searchInput.placeholder = 'Search forms...';
        searchInput.className = 'form-control mb-3';
        searchInput.style.background = 'rgba(26, 42, 54, 0.7)';
        searchInput.style.border = '1px solid var(--border-color)';
        searchInput.style.color = 'var(--light-text)';
        searchInput.style.borderRadius = '8px';
        searchInput.style.padding = '8px 12px';

        const cardBody = document.querySelector('.card-body');
        if (cardBody) {
            cardInput.insertBefore(searchInput, cardBody.firstChild);

            searchInput.addEventListener('input', function() {
                const searchTerm = this.value.toLowerCase();
                const rows = tableBody.querySelectorAll('tr');

                rows.forEach(row => {
                    const text = row.textContent.toLowerCase();
                    if (text.includes(searchTerm)) {
                        row.style.display = '';
                    } else {
                        row.style.display = 'none';
                    }
                });
            });
        }
    }

    // Initialize any additional features
    console.log('ManageForms page initialized');
});