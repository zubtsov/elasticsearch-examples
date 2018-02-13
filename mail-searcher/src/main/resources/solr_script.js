var tableHeaders = ["Order", "Folder", "From", "Subject", "Sent date", "Received date", "Recipients", "Reply to"];
var fieldNames = ["folder", "from", "subject", "sent_date", "received_date", "recipients", "reply_to"];

function loadDocs() {
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
            var response = JSON.parse(this.responseText);

            var table = document.createElement("TABLE");
            var header = table.createTHead();
            var row = header.insertRow(0);
            for (i = 0; i<tableHeaders.length; i++) {
                row.insertCell().innerHTML=tableHeaders[i];
            }

            var hit;
            for (i = 0; i < response.response.docs.length; i++) {

                hit = response.response.docs[i];

                var row = table.insertRow();
                row.insertCell().innerHTML=i;

                for (j = 0; j<fieldNames.length; j++) {
                    row.insertCell().innerHTML=escapeHtml(hit[fieldNames[j]].toString());
                }
            }

            document.getElementById("results").innerHTML="";
            document.getElementById("results").append(table);
        }
    };

    xhttp.open("GET", document.getElementById("url").value, true);
    xhttp.send();
}

function escapeHtml(unsafe) {
    return unsafe
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
};