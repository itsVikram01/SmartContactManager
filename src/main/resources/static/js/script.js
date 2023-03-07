console.log("This is script file");

function openNav() {
	document.getElementById("mysidebar").style.width = "250px";
	document.getElementById("mycontent").style.marginLeft = "250px";
}

function closeNav() {
	document.getElementById("mysidebar").style.width = "0";
	document.getElementById("mycontent").style.marginLeft = "0";
};


// search contact js

const search = () => {
	// console.log("searching...")
	let query = $("#search-input").val();

	if (query == '') {
		$(".search-result").hide();
	} else {
		// search
		console.log(query);

		// sending request to server
		let url = `http://localhost:8080/search/${query}`;

		fetch(url)
			.then((Response) => {
				return Response.json();
			})
			.then((data) => {
				// data
				//console.log(data);
				let text = `<div class='list-group>'`;

				data.forEach((contact) => {
					text += `<a href='/user/${contact.cId}/contact' class='list-group-item list-group-item-action'> ${contact.name} </a>`
				});
				text += `</div>`

				$(".search-result").html(text);
				$(".search-result").show();
			})
		$(".search-result").show();
	}
}


