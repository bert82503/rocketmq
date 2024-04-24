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
package org.apache.rocketmq.client.consumer.rebalance;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.rocketmq.common.message.MessageQueue;

/**
 * Computer room Hashing queue algorithm, such as Alipay logic room
 * 机房哈希队列算法，如支付宝逻辑机房
 */
public class AllocateMessageQueueByMachineRoom extends AbstractAllocateMessageQueueStrategy {
    /**
     * 消费者的机房列表
     */
    private Set<String> consumerIdcs;

    @Override
    public List<MessageQueue> allocate(String consumerGroup, String currentCID, List<MessageQueue> mqAll,
        List<String> cidAll) {

        List<MessageQueue> result = new ArrayList<>();
        if (!check(consumerGroup, currentCID, mqAll, cidAll)) {
            return result;
        }
        int currentIndex = cidAll.indexOf(currentCID);
        if (currentIndex < 0) {
            return result;
        }
        List<MessageQueue> premqAll = new ArrayList<>();
        for (MessageQueue mq : mqAll) {
            String[] temp = mq.getBrokerName().split("@");
            if (temp.length == 2 && consumerIdcs.contains(temp[0])) {
                premqAll.add(mq);
            }
        }

        int mod = premqAll.size() / cidAll.size();
        int rem = premqAll.size() % cidAll.size();
        int startIndex = mod * currentIndex;
        int endIndex = startIndex + mod;
        for (int i = startIndex; i < endIndex; i++) {
            result.add(premqAll.get(i));
        }
        if (rem > currentIndex) {
            result.add(premqAll.get(currentIndex + mod * cidAll.size()));
        }
        return result;
    }

    @Override
    public String getName() {
        return "MACHINE_ROOM";
    }

    public Set<String> getConsumeridcs() {
        return consumerIdcs;
    }

    public void setConsumeridcs(Set<String> consumerIdcs) {
        this.consumerIdcs = consumerIdcs;
    }
}
