// pimp Date to contains a getWeek function.
// code stolen from http://javascript.about.com/library/blweekyear.htm
Date.prototype.getWeek = function() {
	var onejan = new Date(this.getFullYear(),0,1);
	return Math.ceil((((this - onejan) / 86400000) + onejan.getDay()+1)/7);
}

function sameDate(d1, d2) {
	return d1.getFullYear() == d2.getFullYear()
	    && d1.getMonth() == d2.getMonth()
		&& d1.getDate() == d2.getDate();
}

function sameWeek(d1, d2) {
	return d1.getFullYear() == d2.getFullYear()
	    && d1.getWeek() == d2.getWeek();
}

function olderDate(d1, d2) {
	return d1.getFullYear() < d2.getFullYear()
	    || d1.getFullYear() == d2.getFullYear() && d1.getMonth() < d2.getMonth()
		|| d1.getFullYear() == d2.getFullYear() && d1.getMonth() == d2.getMonth() && d1.getDate() < d2.getDate();
}

function getDate(menuDiv) {
	var dateStr = menuDiv.getElementsByTagName("h1")[0].innerHTML.split(" ")[1];

	var dateArray = dateStr.split("-");

	return new Date(dateArray[0], dateArray[1]-1, dateArray[2]);
}

function hidden(menuDiv) {
	var today = new Date();

	return olderDate(getDate(menuDiv), today);
}

function hideStuff() {
	var hiddenDivs = [];

	var today = new Date();

	var divs = document.getElementsByTagName("div");
	for (var i = 0; i < divs.length; i++) {
		var menuDiv = divs[i];
		if (hidden(menuDiv)) {
			menuDiv.style.display = "none";
			hiddenDivs.push(menuDiv);
		}
	}

	if (hiddenDivs.length > 0) {
		var showStuffDiv = document.createElement("div");
		var showStuffLink = document.createElement("a");
		showStuffLink.href = "javascript:void(0)";
		var linkText = "Visa " + hiddenDivs.length + " äldre ";
		if (hiddenDivs.length == 1) {
			linkText += " dagsmeny";
		}
		else {
			linkText += " dagsmenyer";
		}
		showStuffLink.innerHTML = linkText;
		showStuffLink.onclick = function() {
			for (var i = 0; i < hiddenDivs.length; i++) {
				var menuDiv = hiddenDivs[i];
				menuDiv.style.display = "inline";
			}

			body.removeChild(showStuffDiv);
		};

		showStuffDiv.appendChild(showStuffLink);

		var body = document.getElementsByTagName("body")[0];
		// every time this code is reached body must have 1 or more children, making this code safe
		body.insertBefore(showStuffDiv, body.childNodes[0]);
	}
}

