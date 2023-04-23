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

package org.apache.rocketmq.common.message;

/**
 * 消息类型
 */
public enum MessageType {
    /**
     * 普通消息，消息本身无特殊语义，消息之间也没有任何关联。
     */
    Normal_Msg("Normal"),
    /**
     * 顺序消息，通过消息分组标记一组特定消息的先后顺序，可以保证消息的投递顺序严格按照消息发送时的顺序。
     */
    Order_Msg("Order"),
    /**
     * 事务消息，支持分布式事务消息，支持应用数据库更新和消息调用的事务一致性保障。
     */
    Trans_Msg_Half("Trans"),
    Trans_msg_Commit("TransCommit"),
    /**
     * 定时/延时消息，通过指定延时时间控制消息生产后不要立即投递，而是在延时间隔后才对消费者可见。
     */
    Delay_Msg("Delay");

    private final String shortName;

    MessageType(String shortName) {
        this.shortName = shortName;
    }

    public String getShortName() {
        return shortName;
    }

    public static MessageType getByShortName(String shortName) {
        for (MessageType msgType : MessageType.values()) {
            if (msgType.getShortName().equals(shortName)) {
                return msgType;
            }
        }
        return Normal_Msg;
    }
}
