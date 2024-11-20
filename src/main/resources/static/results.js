function displayResults(data) {
    const resultsDiv = document.getElementById('results');
    resultsDiv.innerHTML = ''; // Clear previous results

    if (data.length === 0) {
        resultsDiv.innerHTML = '<p>No results found.</p>';
        return;
    }

    data.forEach(game => {
        const gameDiv = document.createElement('div');
        gameDiv.className = 'result';
        gameDiv.innerHTML = `
            <h3>${game.name}</h3>
            <div class="result-content">
                <p>Short Description: ${game.short_description}</p>
                <p>Release Date: ${game.release_date}</p>
                <p>Reviews: ${game.number_of_english_reviews}</p>
                <p>Genre: ${game.genres}</p>
                <p>Developer: ${game.developer}</p>
                <p>Publisher: ${game.publisher}</p>
                <p>Total Review: ${game.number_of_reviews_from_purchased_people}</p>
                <p>Overall Rating: ${game.overall_player_rating}</p>
                <p>URL: <a href="${game.link}" target="_blank">${game.link}</a></p>
            </div>
            <img src="${game.imgSrc}" alt="${game.name}">
        `;
        resultsDiv.appendChild(gameDiv);
    });
}

const savedResults = localStorage.getItem('searchResults');
if (savedResults) {
    displayResults(JSON.parse(savedResults));
    localStorage.removeItem('searchResults');
} else {
    const urlParams = new URLSearchParams(window.location.search);
    const keyword = urlParams.get('keyword');

    if (keyword) {
        // Fetch search results from the server
        fetch(`/api/games/search?keyword=${encodeURIComponent(keyword)}`)
            .then(response => response.json())
            .then(data => displayResults(data))
            .catch(error => console.error('Error fetching search results:', error));
    }
}
