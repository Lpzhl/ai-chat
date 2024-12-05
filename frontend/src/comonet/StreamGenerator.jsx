import React, { useState } from 'react';
import '../assets/StreamGenerator.css'; // 引入 CSS 样式文件

const StreamGenerator = () => {
    const [query, setQuery] = useState(''); // 存储用户输入的查询
    const [result, setResult] = useState(''); // 存储生成的内容

    // 处理用户输入
    const handleQueryChange = (e) => {
        setQuery(e.target.value);
    };

    // 开始流式生成内容
    const startStreaming = async () => {
        setResult(''); // 清空之前的结果
        let buffer = ''; // 用于暂存当前的流内容

        try {
            const response = await fetch('http://localhost:8888/api/stream/generate', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ query }) // 这里假设 query 是你要发送的内容
            });

            const reader = response.body.getReader();
            const decoder = new TextDecoder();

            // 读取流数据并逐步更新结果
            const readStream = async ({ done, value }) => {
                if (done) return; // 完成读取
                buffer = ''; // 清空 buffer，以便下次只拼接新内容
                buffer += decoder.decode(value, { stream: true });

                // 更新结果（每次获取新内容时追加）
                setResult((prevResult) => prevResult + buffer);

                // 继续读取流
                reader.read().then(readStream);
            };

            // 启动读取流
            reader.read().then(readStream);

        } catch (error) {
            console.error('Error:', error);
        }
    };

    return (
        <div className="chat-container">
            <div className="chat-box">
                <div className="messages">
                    <div className="user-message">
                        <span>你:</span>
                        <p>{query}</p>
                    </div>
                    <div className="bot-message">
                        <span>AI:</span>
                        <p>{result}</p>
                    </div>
                </div>
                <div className="input-container">
                    <input
                        type="text"
                        id="query"
                        placeholder="请输入查询内容"
                        value={query}
                        onChange={handleQueryChange}
                        className="input-field"
                    />
                    <button onClick={startStreaming} className="generate-btn">开始生成</button>
                </div>
            </div>
        </div>
    );
};

export default StreamGenerator;
