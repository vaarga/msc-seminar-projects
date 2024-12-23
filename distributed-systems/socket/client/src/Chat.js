import React, { useState, useEffect } from 'react';
import ScrollToBottom from 'react-scroll-to-bottom';

const Chat = ({
    socket,
    username,
    roomId,
}) => {
    const [message, setMessage] = useState('');
    const [messageList, setMessageList] = useState([]);

    const sendMessage = async () => {
        if (message !== '') {
            const currentDate = new Date(Date.now());
            const messageWithAdditionalInfo = {
                roomId,
                author: username,
                message,
                time: `${currentDate.getHours()}:${String(currentDate.getMinutes()).padStart(2, '0')}`,
            }

            await socket.emit('send_message', messageWithAdditionalInfo);

            setMessageList((prevMessageList) => [
                ...prevMessageList,
                messageWithAdditionalInfo,
            ])

            setMessage('');
        }
    };

    useEffect(() => {
        socket.on('receive_message', (messageWithAdditionalInfo) => {
            setMessageList((prevMessageList) => [
                ...prevMessageList,
                messageWithAdditionalInfo,
            ])
        });

        return () => socket.removeListener('receive_message');
    }, [socket]);

    return (
        <div className='chat-window'>
            <div className='chat-header'>
                <p>Room with ID: {roomId}</p>
            </div>
            <div className='chat-body'>
                <ScrollToBottom className='message-container'>
                    {
                        messageList.map((messageWithAdditionalInfo, index) => {
                            return (
                                <div
                                    className={`message ${username === messageWithAdditionalInfo.author ? 'you' : 'other'}`}
                                    key={index}
                                >
                                    <div>
                                        <div className='message-content'>
                                            <p>{messageWithAdditionalInfo.message}</p>
                                        </div>
                                        <div className='message-meta'>
                                            <p>At {messageWithAdditionalInfo.time} by&nbsp;</p>
                                            <p className='author'>{messageWithAdditionalInfo.author}</p>
                                        </div>
                                    </div>
                                </div>
                            )
                        })
                    }
                </ScrollToBottom>
            </div>
            <div className='chat-footer'>
                <input
                    type='text'
                    placeholder='Write your message here'
                    onChange={(event) => { setMessage(event.target.value) }}
                    onKeyPress={(event) => { event.key === 'Enter'  && sendMessage()}}
                    value={message}
                />
                <button onClick={sendMessage}>&#9658;</button>
            </div>
        </div>
    );
};

export default Chat;
