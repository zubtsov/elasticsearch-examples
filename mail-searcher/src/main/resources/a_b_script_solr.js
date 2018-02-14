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
    var table = document.createElement("TABLE");
    var tableHeader = table.createTHead();
    tableHeader.insertRow().innerHTML = "ID";

    var docs = JSON.parse(jsonString).response.docs;

    for (i = 0; i < docs.length; i++) {
        var row = table.insertRow();
        row.insertCell().innerHTML = docs[i].id;
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