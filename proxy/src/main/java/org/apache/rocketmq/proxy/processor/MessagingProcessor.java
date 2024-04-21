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
package org.apache.rocketmq.proxy.processor;

import io.netty.channel.Channel;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.apache.rocketmq.broker.client.ClientChannelInfo;
import org.apache.rocketmq.broker.client.ConsumerGroupInfo;
import org.apache.rocketmq.broker.client.ConsumerIdsChangeListener;
import org.apache.rocketmq.broker.client.ProducerChangeListener;
import org.apache.rocketmq.client.consumer.AckResult;
import org.apache.rocketmq.client.consumer.PopResult;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.consumer.ReceiptHandle;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.proxy.common.Address;
import org.apache.rocketmq.proxy.common.ProxyContext;
import org.apache.rocketmq.proxy.common.StartAndShutdown;
import org.apache.rocketmq.proxy.service.metadata.MetadataService;
import org.apache.rocketmq.proxy.service.relay.ProxyRelayService;
import org.apache.rocketmq.proxy.service.route.ProxyTopicRouteData;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;
import org.apache.rocketmq.remoting.protocol.heartbeat.ConsumeType;
import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.remoting.protocol.heartbeat.SubscriptionData;
import org.apache.rocketmq.remoting.protocol.subscription.SubscriptionGroupConfig;

/**
 * 消息处理器
 */
public interface MessagingProcessor extends StartAndShutdown {

    long DEFAULT_TIMEOUT_MILLS = Duration.ofSeconds(2).toMillis();

    // 订阅关系分组配置
    SubscriptionGroupConfig getSubscriptionGroupConfig(
        ProxyContext ctx,
        String consumerGroupName
    );

    // 代理主题的路由数据
    ProxyTopicRouteData getTopicRouteDataForProxy(
        ProxyContext ctx,
        List<Address> requestHostAndPortList,
        String topicName
    ) throws Exception;

    // 发送消息
    default CompletableFuture<List<SendResult>> sendMessage(
        ProxyContext ctx,
        QueueSelector queueSelector,
        String producerGroup,
        int sysFlag,
        List<Message> msg
    ) {
        // 发送消息
        return sendMessage(ctx, queueSelector, producerGroup, sysFlag, msg, DEFAULT_TIMEOUT_MILLS);
    }

    CompletableFuture<List<SendResult>> sendMessage(
        ProxyContext ctx,
        QueueSelector queueSelector,
        String producerGroup,
        int sysFlag,
        List<Message> msg,
        long timeoutMillis
    );

    // 将消息转发到死信队列
    default CompletableFuture<RemotingCommand> forwardMessageToDeadLetterQueue(
        ProxyContext ctx,
        ReceiptHandle handle,
        String messageId,
        String groupName,
        String topicName
    ) {
        // 将消息转发到死信队列
        return forwardMessageToDeadLetterQueue(ctx, handle, messageId, groupName, topicName, DEFAULT_TIMEOUT_MILLS);
    }

    CompletableFuture<RemotingCommand> forwardMessageToDeadLetterQueue(
        ProxyContext ctx,
        ReceiptHandle handle,
        String messageId,
        String groupName,
        String topicName,
        long timeoutMillis
    );

    // 结束消息事务
    default CompletableFuture<Void> endTransaction(
        ProxyContext ctx,
        String transactionId,
        String messageId,
        String producerGroup,
        TransactionStatus transactionStatus,
        boolean fromTransactionCheck
    ) {
        // 结束消息事务
        return endTransaction(ctx, transactionId, messageId, producerGroup, transactionStatus, fromTransactionCheck, DEFAULT_TIMEOUT_MILLS);
    }

    CompletableFuture<Void> endTransaction(
        ProxyContext ctx,
        String transactionId,
        String messageId,
        String producerGroup,
        TransactionStatus transactionStatus,
        boolean fromTransactionCheck,
        long timeoutMillis
    );

    // 弹出消息
    CompletableFuture<PopResult> popMessage(
        ProxyContext ctx,
        QueueSelector queueSelector,
        String consumerGroup,
        String topic,
        int maxMsgNums,
        long invisibleTime,
        long pollTime,
        int initMode,
        SubscriptionData subscriptionData,
        boolean fifo,
        PopMessageResultFilter popMessageResultFilter,
        long timeoutMillis
    );

    // ack消息
    default CompletableFuture<AckResult> ackMessage(
        ProxyContext ctx,
        ReceiptHandle handle,
        String messageId,
        String consumerGroup,
        String topic
    ) {
        // ack消息
        return ackMessage(ctx, handle, messageId, consumerGroup, topic, DEFAULT_TIMEOUT_MILLS);
    }

    CompletableFuture<AckResult> ackMessage(
        ProxyContext ctx,
        ReceiptHandle handle,
        String messageId,
        String consumerGroup,
        String topic,
        long timeoutMillis
    );

    // 更改不可见时间
    default CompletableFuture<AckResult> changeInvisibleTime(
        ProxyContext ctx,
        ReceiptHandle handle,
        String messageId,
        String groupName,
        String topicName,
        long invisibleTime
    ) {
        // 更改不可见时间
        return changeInvisibleTime(ctx, handle, messageId, groupName, topicName, invisibleTime, DEFAULT_TIMEOUT_MILLS);
    }

    CompletableFuture<AckResult> changeInvisibleTime(
        ProxyContext ctx,
        ReceiptHandle handle,
        String messageId,
        String groupName,
        String topicName,
        long invisibleTime,
        long timeoutMillis
    );

    // 拉取消息
    CompletableFuture<PullResult> pullMessage(
        ProxyContext ctx,
        MessageQueue messageQueue,
        String consumerGroup,
        long queueOffset,
        int maxMsgNums,
        int sysFlag,
        long commitOffset,
        long suspendTimeoutMillis,
        SubscriptionData subscriptionData,
        long timeoutMillis
    );

    // 更新消费者的偏移位点
    CompletableFuture<Void> updateConsumerOffset(
        ProxyContext ctx,
        MessageQueue messageQueue,
        String consumerGroup,
        long commitOffset,
        long timeoutMillis
    );

    // 查询消费者的偏移位点
    CompletableFuture<Long> queryConsumerOffset(
        ProxyContext ctx,
        MessageQueue messageQueue,
        String consumerGroup,
        long timeoutMillis
    );

    // 加锁批量消息
    CompletableFuture<Set<MessageQueue>> lockBatchMQ(
        ProxyContext ctx,
        Set<MessageQueue> mqSet,
        String consumerGroup,
        String clientId,
        long timeoutMillis
    );

    // 释放批量消息加锁
    CompletableFuture<Void> unlockBatchMQ(
        ProxyContext ctx,
        Set<MessageQueue> mqSet,
        String consumerGroup,
        String clientId,
        long timeoutMillis
    );

    // 获取最大偏移位点
    CompletableFuture<Long> getMaxOffset(
        ProxyContext ctx,
        MessageQueue messageQueue,
        long timeoutMillis
    );

    // 获取最小偏移位点
    CompletableFuture<Long> getMinOffset(
        ProxyContext ctx,
        MessageQueue messageQueue,
        long timeoutMillis
    );

    // 请求
    CompletableFuture<RemotingCommand> request(ProxyContext ctx, String brokerName, RemotingCommand request,
        long timeoutMillis);

    // 只请求一次
    CompletableFuture<Void> requestOneway(ProxyContext ctx, String brokerName, RemotingCommand request,
        long timeoutMillis);

    // 注册消息生产者
    void registerProducer(
        ProxyContext ctx,
        String producerGroup,
        ClientChannelInfo clientChannelInfo
    );

    // 取消注册消息生产者
    void unRegisterProducer(
        ProxyContext ctx,
        String producerGroup,
        ClientChannelInfo clientChannelInfo
    );

    Channel findProducerChannel(
        ProxyContext ctx,
        String producerGroup,
        String clientId
    );

    // 注册消息生产者监听器
    void registerProducerListener(
        ProducerChangeListener producerChangeListener
    );

    // 注册消息消费者
    void registerConsumer(
        ProxyContext ctx,
        String consumerGroup,
        ClientChannelInfo clientChannelInfo,
        ConsumeType consumeType,
        MessageModel messageModel,
        ConsumeFromWhere consumeFromWhere,
        Set<SubscriptionData> subList,
        boolean updateSubscription
    );

    ClientChannelInfo findConsumerChannel(
        ProxyContext ctx,
        String consumerGroup,
        Channel channel
    );

    // 取消注册消息消费者
    void unRegisterConsumer(
        ProxyContext ctx,
        String consumerGroup,
        ClientChannelInfo clientChannelInfo
    );

    // 注册消息消费者监听器
    void registerConsumerListener(
        ConsumerIdsChangeListener consumerIdsChangeListener
    );

    // 处理消息管道关闭事件
    void doChannelCloseEvent(String remoteAddr, Channel channel);

    // 消费者分组信息
    ConsumerGroupInfo getConsumerGroupInfo(String consumerGroup);

    void addTransactionSubscription(
        ProxyContext ctx,
        String producerGroup,
        String topic
    );

    ProxyRelayService getProxyRelayService();

    // 元数据服务
    MetadataService getMetadataService();
}
