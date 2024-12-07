import React, { useState } from 'react';
import '../assets/StreamGenerator.css'; // 引入 CSS 样式文件

const StreamGenerator = () => {
    const [query, setQuery] = useState(''); // 存储用户输入的查询
    const [result, setResult] = useState(''); // 存储生成的内容
    const [type, setType] = useState('translation'); // 存储选择的处理类型
    const [isLoading, setIsLoading] = useState(false); // 存储加载状态

    // 处理用户输入
    const handleQueryChange = (e) => {
        setQuery(e.target.value);
    };

    // 处理类型选择
    const handleTypeChange = (e) => {
        setType(e.target.value);
    };

    // 开始流式生成内容
    const startStreaming = async () => {
        if (!type) {
            alert('请选择处理类型');
            return;
        }

        setResult(''); // 清空之前的结果
        setIsLoading(true); // 开启加载状态
        let buffer = ''; // 用于暂存当前的流内容

        try {
            const response = await fetch(`ttt/api/stream/ai?type=${type}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(query)
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const reader = response.body.getReader();
            const decoder = new TextDecoder();

            // 读取流数据并逐步更新结果
            const readStream = async ({ done, value }) => {
                if (done) {
                    setIsLoading(false); // 关闭加载状态
                    return; // 完成读取
                }
                buffer = decoder.decode(value, { stream: true });

                // 更新结果（每次获取新内容时追加）
                setResult((prevResult) => prevResult + buffer);

                // 继续读取流
                reader.read().then(readStream);
            };

            // 启动读取流
            reader.read().then(readStream);

        } catch (error) {
            console.error('Error:', error);
            setResult('生成内容时出错，请稍后再试。');
            setIsLoading(false); // 关闭加载状态
        }
    };

    return (
        <div className="chat-container">
            <div className="chat-box">
                <div className="messages">
                    <div className="user-message">
                        <span className="user-label">你:</span>
                        <p>{query}</p>
                    </div>
                    <div className="bot-message">
                        <span className="bot-label">AI:</span>
                        <p>{result}</p>
                    </div>
                    {isLoading && (
                        <div className="loading-indicator">
                            <div className="spinner"></div>
                            <span>AI 正在生成内容...</span>
                        </div>
                    )}
                </div>
                <div className="input-container">
                    <select value={type} onChange={handleTypeChange} className="type-select">
                        <option value="translation">中英文翻译</option>
                        <option value="summarization">文本总结</option>
                        <option value="rewriting">文本重写</option>
                        <option value="mindmap">生成思维导图</option>
                        <option value="fix_format">智能格式排版</option>
                        <option value="correction">文本纠错</option>
                        <option value="continuation">文本续写</option>
                    </select>
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
