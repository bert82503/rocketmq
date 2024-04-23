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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.rocketmq.common.MixAll;

/**
 * 批处理消息
 */
public class MessageBatch extends Message implements Iterable<Message> {

    private static final long serialVersionUID = 621335151046335557L;
    /**
     * 消息列表
     */
    private final List<Message> messages;

    private MessageBatch(List<Message> messages) {
        this.messages = messages;
    }

    public byte[] encode() {
        // 编码
        return MessageDecoder.encodeMessages(messages);
    }

    @Override
    public Iterator<Message> iterator() {
        return messages.iterator();
    }

    public static MessageBatch generateFromList(Collection<? extends Message> messages) {
        assert messages != null;
        assert messages.size() > 0;
        List<Message> messageList = new ArrayList<>(messages.size());
        Message first = null;
        for (Message message : messages) {
            if (message.getDelayTimeLevel() > 0) {
                // 定时/延时消息
                throw new UnsupportedOperationException("TimeDelayLevel is not supported for batching");
            }
            if (message.getTopic().startsWith(MixAll.RETRY_GROUP_TOPIC_PREFIX)) {
                // 重试分组消息主题
                throw new UnsupportedOperationException("Retry Group is not supported for batching");
            }
            if (first == null) {
                first = message;
            } else {
                if (!first.getTopic().equals(message.getTopic())) {
                    // 消息主题需要一样
                    throw new UnsupportedOperationException("The topic of the messages in one batch should be the same");
                }
                if (first.isWaitStoreMsgOK() != message.isWaitStoreMsgOK()) {
                    throw new UnsupportedOperationException("The waitStoreMsgOK of the messages in one batch should the same");
                }
            }
            messageList.add(message);
        }
        MessageBatch messageBatch = new MessageBatch(messageList);

        // 消息主题，同一个
        messageBatch.setTopic(first.getTopic());
        // 表示消息是否在服务器落盘后才返回应答
        messageBatch.setWaitStoreMsgOK(first.isWaitStoreMsgOK());
        return messageBatch;
    }

}
