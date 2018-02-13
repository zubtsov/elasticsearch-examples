function loadDocs() {
    loadDocs1();
    loadDocs2();
}
function loadDocs2() {
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (this.readyState == 4 && this.status == 200) {
        document.getElementById("results2").innerHTML="";
     document.getElementById("results2").append(this.responseText);
    }
  };
  xhttp.open("GET", document.getElementById("url2").value, true);
  xhttp.send();
}
function loadDocs1() {
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (this.readyState == 4 && this.status == 200) {
        document.getElementById("results1").innerHTML="";
     document.getElementById("results1").append(this.responseText);
    }
  };
  xhttp.open("GET", document.getElementById("url1").value, true);
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