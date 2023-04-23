/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.common.attribute;

import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import org.apache.rocketmq.common.message.MessageConst;

/**
 * 主题所支持的消息类型
 */
public enum TopicMessageType {
    /**
     * 未指定
     */
    UNSPECIFIED("UNSPECIFIED"),
    /**
     * 普通消息，消息本身无特殊语义，消息之间也没有任何关联。
     */
    NORMAL("NORMAL"),
    /**
     * 顺序消息，通过消息分组标记一组特定消息的先后顺序，可以保证消息的投递顺序严格按照消息发送时的顺序。
     */
    FIFO("FIFO"),
    /**
     * 定时/延时消息，通过指定延时时间控制消息生产后不要立即投递，而是在延时间隔后才对消费者可见。
     */
    DELAY("DELAY"),
    /**
     * 事务消息，支持分布式事务消息，支持应用数据库更新和消息调用的事务一致性保障。
     */
    TRANSACTION("TRANSACTION");

    private final String value;

    TopicMessageType(String value) {
        this.value = value;
    }

    public static Set<String> topicMessageTypeSet() {
        return Sets.newHashSet(UNSPECIFIED.value, NORMAL.value, FIFO.value, DELAY.value, TRANSACTION.value);
    }

    public String getValue() {
        return value;
    }

    public static TopicMessageType parseFromMessageProperty(Map<String, String> messageProperty) {
        String isTrans = messageProperty.get(MessageConst.PROPERTY_TRANSACTION_PREPARED);
        String isTransValue = "true";
        if (isTransValue.equals(isTrans)) {
            return TopicMessageType.TRANSACTION;
        } else if (messageProperty.get(MessageConst.PROPERTY_DELAY_TIME_LEVEL) != null
            || messageProperty.get(MessageConst.PROPERTY_TIMER_DELIVER_MS) != null
            || messageProperty.get(MessageConst.PROPERTY_TIMER_DELAY_SEC) != null) {
            return TopicMessageType.DELAY;
        } else if (messageProperty.get(MessageConst.PROPERTY_SHARDING_KEY) != null) {
            return TopicMessageType.FIFO;
        }
        return TopicMessageType.NORMAL;
    }

    public String getMetricsValue() {
        return value.toLowerCase();
    }
}
