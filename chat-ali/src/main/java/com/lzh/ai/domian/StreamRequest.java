package com.lzh.ai.domian;

import java.io.Serializable;

public class StreamRequest implements Serializable {
    private String type;
    private String userQuery;

    // Getter 和 Setter 方法

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserQuery() {
        return userQuery;
    }

    public void setUserQuery(String userQuery) {
        this.userQuery = userQuery;
    }
}
