var tableHeaders = ["Order", "Folder", "From", "Subject", "Sent date", "Received Date", "Recipients", "Reply to"];
var fieldNames = ["Folder", "From", "Subject", "Sent date", "Received Date", "Recipients", "Reply to"];

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
            for (i = 0; i < response.hits.hits.length; i++) {
                hit = response.hits.hits[i];

                var row = table.insertRow();
                row.insertCell().innerHTML=i;
                for (j = 0; j < fieldNames.length; j++) {
                    row.insertCell().innerHTML=escapeHtml(hit._source[fieldNames[j]].toString());
                }
            }

            document.getElementById("results").innerHTML="";
            document.getElementById("results").append(table);
        }
    };
    xhttp.open("POST", document.getElementById("url").value, true);
    xhttp.setRequestHeader("Content-Type", "application/json");
    xhttp.send(document.getElementById("query").value);
}

function escapeHtml(unsafe) {
    return unsafe
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
};