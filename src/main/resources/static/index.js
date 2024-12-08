function searchGames(method) {
    const keyword = document.getElementById('keyword').value;

    if (method === 'GET') {
        // Redirect to results.html with keyword as URL parameter
        window.location.href = `results.html?keyword=${encodeURIComponent(keyword)}`;
    }
}

function ranking(method) {
    const genre = document.getElementById('genre').value;
    const type = document.getElementById('type').value;
    const url = `/api/games/ranking?genre=${encodeURIComponent(genre)}&type=${encodeURIComponent(type)}`;

    if (method === 'GET') {
        fetch(url)
            .then(response => response.json())
            .then(data => {console.log(data); displayRanking(data)} )
            .catch(error => console.error('Error with GET request:', error));
    }
}

function displayRanking(data) {
    const resultsDiv = document.getElementById('results');
    resultsDiv.innerHTML = ''; // Clear previous results

    if (data.length === 0) {
        resultsDiv.innerHTML = '<p>No results found.</p>';
        return;
    }

    data.forEach(game => {
        const gameDiv = document.createElement('div');
        gameDiv.className = 'ranking';
        gameDiv.innerHTML = `
            <h3>${game.game_name}</h3>
            <p>Genre: ${game.genre}</p>
            <p>Type: ${game.rank_type}</p>
            <p>Rank: ${game.rank}</p>
        `;
        resultsDiv.appendChild(gameDiv);
    });
}


function sentiment() {
    const review = document.getElementById('review').value;
    const recommendation = document.getElementById('recommendation').value;
    const url = `/api/games/sentiment?review=${encodeURIComponent(review)}&recommendation=${encodeURIComponent(recommendation)}`;
    fetch(url)
        .then(response => response.json())
        .then(data => displaySentiment(data))
        .catch(error => console.error('Error with GET request:', error));

}

function displaySentiment(data) {
    const outputDiv = document.getElementById('output');
    outputDiv.innerHTML = ''; // Clear previous results

    if (data.length === 0) {
        outputDiv.innerHTML = '<p>No results found.</p>';
        return;
    }

//    outputDiv.textContent = data.recommendation;


}
