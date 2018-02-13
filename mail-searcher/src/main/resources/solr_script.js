function loadDocs() {
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (this.readyState == 4 && this.status == 200) {
      var response = JSON.parse(this.responseText);

      var table = document.createElement("TABLE");
      var header = table.createTHead();
      var row = header.insertRow(0);
      row.insertCell().innerHTML="<b>Order</b>";
      row.insertCell().innerHTML="<b>Folder</b>";
      row.insertCell().innerHTML="<b>From</b>";
      row.insertCell().innerHTML="<b>Subject</b>";
      row.insertCell().innerHTML="<b>Sent date</b>";
      row.insertCell().innerHTML="<b>Received date</b>";
      row.insertCell().innerHTML="<b>Recipients</b>";
      row.insertCell().innerHTML="<b>Reply to</b>";

      var hit;
      for (i = 0; i < response.response.docs.length; i++) {
        hit = response.response.docs[i];

        var row = table.insertRow();
        var numberCell = row.insertCell();
        var folderCell = row.insertCell();
        var fromCell = row.insertCell();
        var subjectCell = row.insertCell();
        var sentDateCell = row.insertCell();
        var receivedDateCell = row.insertCell();
        var recipientsCell = row.insertCell();
        var replyToCell = row.insertCell();

        numberCell.innerHTML=i;

        folderCell.innerHTML=hit.folder;
        fromCell.innerHTML=escapeHtml(hit.from.toString());
        subjectCell.innerHTML=hit.subject;
        sentDateCell.innerHTML=hit.sent_date;
        receivedDateCell.innerHTML=hit.received_date;
        recipientsCell.innerHTML=escapeHtml(hit.recipients.toString());
        replyToCell.innerHTML=escapeHtml(hit.reply_to.toString());
      }

      document.getElementById("results").innerHTML="";
      document.getElementById("results").append(table);
    }
  };
  function escapeHtml(unsafe) {
    return unsafe
         .replace(/&/g, "&amp;")
         .replace(/</g, "&lt;")
         .replace(/>/g, "&gt;")
         .replace(/"/g, "&quot;")
         .replace(/'/g, "&#039;");
  };
  xhttp.open("GET", document.getElementById("url").value, true);
  xhttp.send();
}