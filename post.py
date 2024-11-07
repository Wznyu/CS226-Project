import sys
import requests
import json

# Define the base URL for the API
url = "http://localhost:8080/api/games/"

# Check if enough arguments are provided
if len(sys.argv) < 2:
    print("Please provide either a keyword or both genre and type as arguments.")
    sys.exit(1)

# Case 1: Only one argument (keyword) provided
if len(sys.argv) == 2:
    keyword = sys.argv[1]
    endpoint = "search"
    data = {"keyword": keyword}

# Case 2: Two arguments (genre and type) provided
elif len(sys.argv) == 3:
    genre = sys.argv[1]
    type = sys.argv[2]
    endpoint = "ranking"
    data = {"genre": genre, "type": type}

# Construct the full URL with endpoint
full_url = url + endpoint

# Make the POST request with JSON headers
response = requests.post(full_url, json=data)

# Check the status of the response
if response.status_code == 200:
    # Pretty-print the JSON response to the terminal
    print(json.dumps(response.json(), indent=4))
else:
    print(f"Request failed with status code {response.status_code}: {response.reason}")
