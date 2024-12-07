package com.lzh.ai.strategyFactory;

import com.lzh.ai.strategy.TextProcessingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TextProcessingStrategyFactory {

    private final Map<String, TextProcessingStrategy> strategies;

    @Autowired
    public TextProcessingStrategyFactory(Map<String, TextProcessingStrategy> strategies) {
        this.strategies = strategies;
    }

    public TextProcessingStrategy getStrategy(String type) {
        TextProcessingStrategy strategy = strategies.get(type.toLowerCase());
        if (strategy == null) {
            throw new IllegalArgumentException("无效的处理类型: " + type);
        }
        return strategy;
    }
}
