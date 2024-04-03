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

import java.util.HashSet;

/**
 * 消息系统保留的属性Key集合
 */
public class MessageConst {
    /**
     * 索引Key列表，消息的索引键
     * 消息的业务标识
     */
    public static final String PROPERTY_KEYS = "KEYS";
    /**
     * 过滤标签Tag，消息的过滤标签
     * 消息标签，方便服务器过滤使用。目前只支持每个消息设置一个
     */
    public static final String PROPERTY_TAGS = "TAGS";
    /**
     * 表示消息是否在服务器落盘后才返回应答。
     */
    public static final String PROPERTY_WAIT_STORE_MSG_OK = "WAIT";
    /**
     * 定时时间
     * 定时场景下，消息触发延时投递的毫秒级时间戳。
     * 【定时/延时消息】
     */
    public static final String PROPERTY_DELAY_TIME_LEVEL = "DELAY";
    /**
     * 重试主题
     */
    public static final String PROPERTY_RETRY_TOPIC = "RETRY_TOPIC";
    /**
     * 真实主题
     */
    public static final String PROPERTY_REAL_TOPIC = "REAL_TOPIC";
    /**
     * 真实队列id
     */
    public static final String PROPERTY_REAL_QUEUE_ID = "REAL_QID";
    /**
     * 事务消息
     */
    public static final String PROPERTY_TRANSACTION_PREPARED = "TRAN_MSG";
    /**
     * 生产者分组
     */
    public static final String PROPERTY_PRODUCER_GROUP = "PGROUP";
    /**
     * 最小消息位点
     */
    public static final String PROPERTY_MIN_OFFSET = "MIN_OFFSET";
    /**
     * 最大消息位点
     */
    public static final String PROPERTY_MAX_OFFSET = "MAX_OFFSET";
    /**
     * 买家id
     */
    public static final String PROPERTY_BUYER_ID = "BUYER_ID";
    /**
     * 原始消息id
     */
    public static final String PROPERTY_ORIGIN_MESSAGE_ID = "ORIGIN_MESSAGE_ID";
    /**
     * 传输标志
     */
    public static final String PROPERTY_TRANSFER_FLAG = "TRANSFER_FLAG";
    /**
     * 更正标志
     */
    public static final String PROPERTY_CORRECTION_FLAG = "CORRECTION_FLAG";
    public static final String PROPERTY_MQ2_FLAG = "MQ2_FLAG";
    /**
     * 重新消费时间
     */
    public static final String PROPERTY_RECONSUME_TIME = "RECONSUME_TIME";
    /**
     * 消息实例所处地域
     */
    public static final String PROPERTY_MSG_REGION = "MSG_REGION";
    /**
     * 消息轨迹开关
     */
    public static final String PROPERTY_TRACE_SWITCH = "TRACE_ON";
    /**
     * 消息ID：消息的唯一标识，集群内每条消息的ID全局唯一
     */
    public static final String PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX = "UNIQ_KEY";
    /**
     * 拓展的唯一信息
     */
    public static final String PROPERTY_EXTEND_UNIQ_INFO = "EXTEND_UNIQ_INFO";
    /**
     * 最大重新消费次数
     */
    public static final String PROPERTY_MAX_RECONSUME_TIMES = "MAX_RECONSUME_TIMES";
    /**
     * 消费开始时间
     */
    public static final String PROPERTY_CONSUME_START_TIMESTAMP = "CONSUME_START_TIME";
    public static final String PROPERTY_INNER_NUM = "INNER_NUM";
    public static final String PROPERTY_INNER_BASE = "INNER_BASE";
    public static final String DUP_INFO = "DUP_INFO";
    public static final String PROPERTY_CHECK_IMMUNITY_TIME_IN_SECONDS = "CHECK_IMMUNITY_TIME_IN_SECONDS";
    /**
     * 可重入队列消息位点
     */
    public static final String PROPERTY_TRANSACTION_PREPARED_QUEUE_OFFSET = "TRAN_PREPARED_QUEUE_OFFSET";
    public static final String PROPERTY_TRANSACTION_ID = "__transactionId__";
    /**
     * 事务检查次数
     */
    public static final String PROPERTY_TRANSACTION_CHECK_TIMES = "TRANSACTION_CHECK_TIMES";
    /**
     * 实例id
     */
    public static final String PROPERTY_INSTANCE_ID = "INSTANCE_ID";
    /**
     * 关联id
     */
    public static final String PROPERTY_CORRELATION_ID = "CORRELATION_ID";
    /**
     * 回复给客户端
     */
    public static final String PROPERTY_MESSAGE_REPLY_TO_CLIENT = "REPLY_TO_CLIENT";
    /**
     * 生存时间
     */
    public static final String PROPERTY_MESSAGE_TTL = "TTL";
    /**
     * 回复消息到达时间
     */
    public static final String PROPERTY_REPLY_MESSAGE_ARRIVE_TIME = "ARRIVE_TIME";
    /**
     * 推送回复时间
     */
    public static final String PROPERTY_PUSH_REPLY_TIME = "PUSH_REPLY_TIME";
    /**
     * 集群
     */
    public static final String PROPERTY_CLUSTER = "CLUSTER";
    /**
     * 消息类型
     */
    public static final String PROPERTY_MESSAGE_TYPE = "MSG_TYPE";
    public static final String PROPERTY_POP_CK = "POP_CK";
    public static final String PROPERTY_POP_CK_OFFSET = "POP_CK_OFFSET";
    public static final String PROPERTY_FIRST_POP_TIME = "1ST_POP_TIME";
    /**
     * 分片
     */
    public static final String PROPERTY_SHARDING_KEY = "__SHARDINGKEY";
    public static final String PROPERTY_FORWARD_QUEUE_ID = "PROPERTY_FORWARD_QUEUE_ID";
    public static final String PROPERTY_REDIRECT = "REDIRECT";
    public static final String PROPERTY_INNER_MULTI_DISPATCH = "INNER_MULTI_DISPATCH";
    /**
     * 多个消息队列的消息位点
     */
    public static final String PROPERTY_INNER_MULTI_QUEUE_OFFSET = "INNER_MULTI_QUEUE_OFFSET";
    /**
     * 消息轨迹上下文
     */
    public static final String PROPERTY_TRACE_CONTEXT = "TRACE_CONTEXT";
    /**
     * 消息延时级别-秒
     * 【定时/延时消息】
     */
    public static final String PROPERTY_TIMER_DELAY_SEC = "TIMER_DELAY_SEC";
    /**
     * 消息投递级别-毫秒
     * 【定时/延时消息】
     */
    public static final String PROPERTY_TIMER_DELIVER_MS = "TIMER_DELIVER_MS";
    /**
     * 出生主机
     */
    public static final String PROPERTY_BORN_HOST = "__BORNHOST";
    /**
     * 出生时间
     */
    public static final String PROPERTY_BORN_TIMESTAMP = "BORN_TIMESTAMP";

    /**
     * property which name starts with "__RMQ.TRANSIENT." is called transient one that will not stored in broker disks.
     * 短暂的属性名
     */
    public static final String PROPERTY_TRANSIENT_PREFIX = "__RMQ.TRANSIENT.";

    /**
     * the transient property key of topicSysFlag (set by client when pulling messages)
     * 主题系统标识
     */
    public static final String PROPERTY_TRANSIENT_TOPIC_CONFIG = PROPERTY_TRANSIENT_PREFIX + "TOPIC_SYS_FLAG";

    /**
     * the transient property key of groupSysFlag (set by client when pulling messages)
     * 分组系统标识
     */
    public static final String PROPERTY_TRANSIENT_GROUP_CONFIG = PROPERTY_TRANSIENT_PREFIX + "GROUP_SYS_FLAG";

    /**
     * 消息索引分隔符
     */
    public static final String KEY_SEPARATOR = " ";

    /**
     * 系统保留的属性key集合
     */
    public static final HashSet<String> STRING_HASH_SET = new HashSet<>(64);

    /**
     * 定时器进入队列时间
     */
    public static final String PROPERTY_TIMER_ENQUEUE_MS = "TIMER_ENQUEUE_MS";
    /**
     * 定时器出去队列时间
     */
    public static final String PROPERTY_TIMER_DEQUEUE_MS = "TIMER_DEQUEUE_MS";
    /**
     * 定时器轮询次数
     */
    public static final String PROPERTY_TIMER_ROLL_TIMES = "TIMER_ROLL_TIMES";
    public static final String PROPERTY_TIMER_OUT_MS = "TIMER_OUT_MS";
    public static final String PROPERTY_TIMER_DEL_UNIQKEY = "TIMER_DEL_UNIQKEY";
    /**
     * 定时器延时级别
     */
    public static final String PROPERTY_TIMER_DELAY_LEVEL = "TIMER_DELAY_LEVEL";
    /**
     * 定时器延时毫秒数
     */
    public static final String PROPERTY_TIMER_DELAY_MS = "TIMER_DELAY_MS";

    /**
     * properties for DLQ
     */
    public static final String PROPERTY_DLQ_ORIGIN_TOPIC = "DLQ_ORIGIN_TOPIC";
    public static final String PROPERTY_DLQ_ORIGIN_MESSAGE_ID = "DLQ_ORIGIN_MESSAGE_ID";

    static {
        STRING_HASH_SET.add(PROPERTY_TRACE_SWITCH);
        STRING_HASH_SET.add(PROPERTY_MSG_REGION);
        STRING_HASH_SET.add(PROPERTY_KEYS);
        STRING_HASH_SET.add(PROPERTY_TAGS);
        STRING_HASH_SET.add(PROPERTY_WAIT_STORE_MSG_OK);
        STRING_HASH_SET.add(PROPERTY_DELAY_TIME_LEVEL);
        STRING_HASH_SET.add(PROPERTY_RETRY_TOPIC);
        STRING_HASH_SET.add(PROPERTY_REAL_TOPIC);
        STRING_HASH_SET.add(PROPERTY_REAL_QUEUE_ID);
        STRING_HASH_SET.add(PROPERTY_TRANSACTION_PREPARED);
        STRING_HASH_SET.add(PROPERTY_PRODUCER_GROUP);
        STRING_HASH_SET.add(PROPERTY_MIN_OFFSET);
        STRING_HASH_SET.add(PROPERTY_MAX_OFFSET);
        STRING_HASH_SET.add(PROPERTY_BUYER_ID);
        STRING_HASH_SET.add(PROPERTY_ORIGIN_MESSAGE_ID);
        STRING_HASH_SET.add(PROPERTY_TRANSFER_FLAG);
        STRING_HASH_SET.add(PROPERTY_CORRECTION_FLAG);
        STRING_HASH_SET.add(PROPERTY_MQ2_FLAG);
        STRING_HASH_SET.add(PROPERTY_RECONSUME_TIME);
        STRING_HASH_SET.add(PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX);
        STRING_HASH_SET.add(PROPERTY_MAX_RECONSUME_TIMES);
        STRING_HASH_SET.add(PROPERTY_CONSUME_START_TIMESTAMP);
        STRING_HASH_SET.add(PROPERTY_POP_CK);
        STRING_HASH_SET.add(PROPERTY_POP_CK_OFFSET);
        STRING_HASH_SET.add(PROPERTY_FIRST_POP_TIME);
        STRING_HASH_SET.add(PROPERTY_TRANSACTION_PREPARED_QUEUE_OFFSET);
        STRING_HASH_SET.add(DUP_INFO);
        STRING_HASH_SET.add(PROPERTY_EXTEND_UNIQ_INFO);
        STRING_HASH_SET.add(PROPERTY_INSTANCE_ID);
        STRING_HASH_SET.add(PROPERTY_CORRELATION_ID);
        STRING_HASH_SET.add(PROPERTY_MESSAGE_REPLY_TO_CLIENT);
        STRING_HASH_SET.add(PROPERTY_MESSAGE_TTL);
        STRING_HASH_SET.add(PROPERTY_REPLY_MESSAGE_ARRIVE_TIME);
        STRING_HASH_SET.add(PROPERTY_PUSH_REPLY_TIME);
        STRING_HASH_SET.add(PROPERTY_CLUSTER);
        STRING_HASH_SET.add(PROPERTY_MESSAGE_TYPE);
        STRING_HASH_SET.add(PROPERTY_INNER_MULTI_QUEUE_OFFSET);
        STRING_HASH_SET.add(PROPERTY_TIMER_DELAY_MS);
        STRING_HASH_SET.add(PROPERTY_TIMER_DELAY_SEC);
        STRING_HASH_SET.add(PROPERTY_TIMER_DELIVER_MS);
        STRING_HASH_SET.add(PROPERTY_TIMER_ENQUEUE_MS);
        STRING_HASH_SET.add(PROPERTY_TIMER_DEQUEUE_MS);
        STRING_HASH_SET.add(PROPERTY_TIMER_ROLL_TIMES);
        STRING_HASH_SET.add(PROPERTY_TIMER_OUT_MS);
        STRING_HASH_SET.add(PROPERTY_TIMER_DEL_UNIQKEY);
        STRING_HASH_SET.add(PROPERTY_TIMER_DELAY_LEVEL);
        STRING_HASH_SET.add(PROPERTY_BORN_HOST);
        STRING_HASH_SET.add(PROPERTY_BORN_TIMESTAMP);
        STRING_HASH_SET.add(PROPERTY_DLQ_ORIGIN_TOPIC);
        STRING_HASH_SET.add(PROPERTY_DLQ_ORIGIN_MESSAGE_ID);
    }

}
