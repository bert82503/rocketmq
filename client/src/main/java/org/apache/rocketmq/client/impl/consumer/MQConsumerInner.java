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
package org.apache.rocketmq.client.impl.consumer;

import java.util.Set;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.protocol.body.ConsumerRunningInfo;
import org.apache.rocketmq.remoting.protocol.heartbeat.ConsumeType;
import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.remoting.protocol.heartbeat.SubscriptionData;

/**
 * Consumer inner interface
 * 消费者
 */
public interface MQConsumerInner {
    /**
     * @return 消费者分组名称
     */
    String groupName();

    /**
     * @return 消息模型
     */
    MessageModel messageModel();

    /**
     * @return 消费类型
     */
    ConsumeType consumeType();

    /**
     * @return 消费从哪里开始
     */
    ConsumeFromWhere consumeFromWhere();

    /**
     * @return 订阅关系数据
     */
    Set<SubscriptionData> subscriptions();

    /**
     * 进行消息重新平衡
     */
    void doRebalance();

    /**
     * 持久化消费者位点
     */
    void persistConsumerOffset();

    /**
     * 更新消息主题的订阅信息
     * @param topic 消息主题
     * @param info  消息队列的列表
     */
    void updateTopicSubscribeInfo(final String topic, final Set<MessageQueue> info);

    boolean isSubscribeTopicNeedUpdate(final String topic);

    boolean isUnitMode();

    /**
     * @return 消费者运行信息
     */
    ConsumerRunningInfo consumerRunningInfo();
}
