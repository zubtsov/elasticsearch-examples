function loadDocs() {
    loadTable("url1", "results1");
    loadTable("url2", "results2");
}
function loadTable(urlString, tagId) {
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (this.readyState == 4 && this.status == 200) {
        document.getElementById(tagId).innerHTML="";
        document.getElementById(tagId).append(jsonToTable(this.responseText));
    }
  };
  xhttp.open("GET", document.getElementById(urlString).value, true);
  xhttp.send();
}
function jsonToTable(jsonString) {
    var docs = JSON.parse(jsonString).response.docs;

    var table = document.createElement("TABLE");
    var tableHeader = table.createTHead().insertRow();
    for (var property in docs[0]) {
        if (docs[0].hasOwnProperty(property)) {
            tableHeader.insertCell().innerHTML = property.toString();
        }
    }

    for (i = 0; i < docs.length; i++) {
        var row = table.insertRow();
        for (var property in docs[i]) {
            if (docs[i].hasOwnProperty(property)) {
                row.insertCell().innerHTML = escapeHtml(docs[i][property.toString()].toString());
            }
        }
    }

    return table;
}
function escapeHtml(unsafe) {
    return unsafe
     .replace(/&/g, "&amp;")
     .replace(/</g, "&lt;")
     .replace(/>/g, "&gt;")
     .replace(/"/g, "&quot;")
     .replace(/'/g, "&#039;");
};