// Automatically close the uploaded files section after 5 seconds
function closeExpandable() {

    setTimeout(function() {
        if ($(".uploaded-files").is(":visible")) {  // Check if the section is visible
            $(".expandable").trigger("click");  // Simulate a click to toggle and close the section
        }
    }, 3000);  // Wait for 5 seconds
}

// Global function to update the uploaded files section
function updateUploadedFiles(files) {
    console.log("files: ", files);
    var filesListHtml = '';
    files.forEach(function(file) {
        filesListHtml += '<div class="file-item" id="file-' + file + '">' +
                            '<span>' + file + '</span>' +
                            '<button class="delete-file" data-filename="' + file + '">Delete</button>' +
                         '</div>';
    });
    $('#uploadedFilesList').html(filesListHtml); // Update the list with uploaded files

    // Ensure that the uploaded files section is visible if it's hidden
    $(".uploaded-files").show();  // Ensure the uploaded files section is visible
    closeExpandable();
}

// Global function to delete the file
function deleteFile(fileName) {
    $.ajax({
        url: '/file/delete',  // Controller endpoint for handling file deletion
        type: 'POST',
        data: { fileName: fileName },  // Send the file name as a parameter
        success: function(response) {
            alert(response.message);  // Alert success or failure message
            if (response.message === "File deleted successfully") {
                // After successful deletion, fetch the updated file list
                fetchUpdatedFiles();
            }
        },
        error: function(xhr, status, error) {
            alert('File deletion failed. Please try again. Error: ' + error);
        }
    });

    closeExpandable();
}

// Global function to fetch the updated list of files
function fetchUpdatedFiles() {
    $.ajax({
        url: '/file/list', // Endpoint to get the list of uploaded files (you need to create this in your backend)
        type: 'GET',
        success: function(response) {
            if (response.uploadedFiles && response.uploadedFiles.length > 0) {
                updateUploadedFiles(response.uploadedFiles); // Update the UI with the updated file list
            } else {
                $('#uploadedFilesList').html('<p>No uploaded files found.</p>'); // Show message if no files available
            }
        },
        error: function(xhr, status, error) {
            alert('Failed to fetch the updated file list. Please try again. Error: ' + error);
        }
    });

    closeExpandable();
}

// Function to handle file upload
function handleFileUpload() {
    // Handle file upload form submission via AJAX
    $('#fileUploadForm').on('submit', function(e) {
        e.preventDefault(); // Prevent default form submission

        var formData = new FormData(this); // Create FormData object to hold files
        $.ajax({
            url: '/file/upload', // Controller endpoint for handling file upload
            type: 'POST',
            data: formData,
            processData: false,  // Don't process data
            contentType: false,  // Don't set content-type header
            success: function(response) {
                // Check if uploadedFiles is present in the response before calling updateUploadedFiles
                if (response.uploadedFiles && response.uploadedFiles.length > 0) {
                    updateUploadedFiles(response.uploadedFiles); // Update the UI with the updated file list
                } else {
                    console.log('No files found in the response.');
                    $('#uploadedFilesList').html('<p>No uploaded files found.</p>'); // Show a message if no files are available
                }
            },
            error: function(xhr, status, error) {
                alert('File upload failed. Please try again. Error: ' + error);
            }
        });
    });
}

// Function to handle the toggle of the uploaded files section
function handleToggleUploadedFiles() {
    $(".expandable").on("click", function() {
        var $uploadedFiles = $(".uploaded-files");  // Select the .uploaded-files section
        $uploadedFiles.slideToggle();  // Toggle visibility with a slide effect

        // Toggle the icon class between down and up arrow
        $(this).find("i").toggleClass("fa-chevron-down fa-chevron-up");
    });

    closeExpandable();
    // Automatically close the uploaded files section after 5 seconds
//    setTimeout(function() {
//        if ($(".uploaded-files").is(":visible")) {  // Check if the section is visible
//            $(".expandable").trigger("click");  // Simulate a click to toggle and close the section
//        }
//    }, 5000);  // Wait for 5 seconds
}

// Function to handle delete button click events
function handleDeleteFileClick() {
    // Add event listener for the delete button
    $(document).on('click', '.delete-file', function() {
        var fileName = $(this).data('filename'); // Get the filename from the button data attribute
        deleteFile(fileName); // Call the deleteFile function
    });
    closeExpandable();
}

// Function to send the chat prompt to the API
function sendChatPrompt() {
    // Capture the value from the chat input textarea
    const prompt = $("#chatPrompt").val().trim();

    // Clear the response area first
    $("#responseContent").text("Please wait...");

    // Check if the prompt is empty
    if (!prompt) {
        alert("Please enter a prompt.");
        return; // Exit if there's no prompt entered
    }

    // Send the prompt to the server using AJAX
    $.ajax({
        url: '/ai/chat',  // The endpoint for the chat API
        type: 'POST',
        contentType: 'application/json',  // Specify the content type
        data: JSON.stringify({ prompt: prompt }),  // Send the prompt in the request body
        success: function(response) {
            // Handle the response from the API
            $("#responseContent").html(response.message);  // Show the response in the response area
        },
        error: function(xhr, status, error) {
            // Handle error
            $("#responseContent").text("Error communicating with the chatbot.");
            console.error("Error:", error);  // Log the error for debugging
        }
    });
}



// Document ready function
$(document).ready(function() {
    // Initialize the functions
    handleFileUpload();  // Initialize file upload handler
    handleToggleUploadedFiles();  // Initialize file section toggle handler
    handleDeleteFileClick();  // Initialize delete button click handler

    // Send Prompt to API when "Send" button is clicked
    $("#sendPrompt").on("click", function() {
        sendChatPrompt();  // Send the chat prompt to the API
    });

    // Optionally, send prompt when Enter key is pressed (without Shift)
    $("#chatPrompt").on("keypress", function(e) {
        if (e.which === 13 && !e.shiftKey) {  // Enter key without Shift (to prevent new line)
            e.preventDefault();  // Prevent the default action of the Enter key
            sendChatPrompt();  // Send the chat prompt to the API
        }
    });
});

