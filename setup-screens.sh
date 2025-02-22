#!/bin/bash

# Install screen if not already installed
if ! command -v screen &> /dev/null; then
    echo "Installing screen..."
    if command -v apt &> /dev/null; then
        sudo apt update && sudo apt install screen -y
    elif command -v yum &> /dev/null; then
        sudo yum install screen -y
    else
        echo "Could not install screen. Please install manually."
        exit 1
    fi
fi

# Create a new screen session for React.js
screen -dmS react bash -c '
    echo "Starting React.js application..."
    cd /var/www/ewf-fe  # Replace with your React app path
    npm install
    npm start
'
screen -dmS springboot bash -c '
    echo "Starting Spring Boot application..."
    cd /home
    java -jar *.jar    # This will run the first .jar file it finds
'

# Display running screen sessions
echo "Created screen sessions. Use the following commands to manage them:"
echo "- List all sessions: screen -ls"
echo "- Attach to React session: screen -r react"
echo "- Attach to Spring Boot session: screen -r springboot"
echo "- Detach from a session: Press Ctrl+A then D"
echo "- Kill a session: screen -X -S [session-name] quit"