const express = require('express');
const http = require('http');
const cors = require('cors');
const { Server } = require('socket.io');

const PORT = 3001;

const app = express();

app.use(cors());

const server = http.createServer(app);
const io = new Server(server, {
    cors: {
        origin: 'http://localhost:3000',
        methods: ['GET', 'POST'],
    },
})

io.on('connection', (socket) => {
    console.log(`User with ID ${socket.id} connected.`);

    socket.on('join_room', (roomId) => {
        socket.join(roomId);

        console.log(`User with ID ${socket.id} joined the room with ID ${roomId}.`);
    });

    socket.on('send_message', (messageWithAdditionalInfo) => {
        socket.to(messageWithAdditionalInfo.roomId).emit('receive_message', messageWithAdditionalInfo)

        console.log(`User with ID ${socket.id} sent a message to the room with ID ${messageWithAdditionalInfo.roomId}.`);
    })

    socket.on('disconnect', () => {
        console.log(`User with ID ${socket.id} disconnected.`);
    })
})

server.listen(PORT, () => {
    console.log(`Server running on port ${PORT}!`);
})
