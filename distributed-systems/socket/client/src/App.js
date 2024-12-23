import { useState } from 'react';
import io from 'socket.io-client';
import Chat from './Chat';
import './App.css';

const socket = io.connect('http://localhost:3001');

const App = () => {
    const [username, setUsername] = useState('');
    const [roomId, setRoomId] = useState('');
    const [showChat, setShowChat] = useState(false);

    const joinRoom = () => {
        if (username !== '' && roomId !== '') {
            socket.emit('join_room', roomId);

            setShowChat(true);
        }
    }

    return (
        <div className='App'>
            {
                !showChat ? (
                    <div className="joinChatContainer">
                        <h3>Join a room</h3>
                        <input
                            type='text'
                            placeholder='Name'
                            onChange={(event) => { setUsername(event.target.value) }}
                        />
                        <input
                            type='text'
                            placeholder='Room ID'
                            onChange={(event) => { setRoomId(event.target.value) }}
                        />
                        <button onClick={joinRoom}>Join the room</button>
                    </div>
                ) : (
                    <Chat
                        socket={socket}
                        username={username}
                        roomId={roomId}
                    />
                )
            }
        </div>
    );
}

export default App;
