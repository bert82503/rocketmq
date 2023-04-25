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

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.compression.Compressor;
import org.apache.rocketmq.common.compression.CompressorFactory;
import org.apache.rocketmq.common.sysflag.MessageSysFlag;

/**
 * 消息/扩展消息/批处理消息编解码
 * 消息协议
 */
public class MessageDecoder {
//    public final static int MSG_ID_LENGTH = 8 + 8;

    public final static Charset CHARSET_UTF8 = StandardCharsets.UTF_8;
    /**
     * 消息魔法编码的位置
     */
    public final static int MESSAGE_MAGIC_CODE_POSITION = 4;
    public final static int MESSAGE_FLAG_POSITION = 16;
    public final static int MESSAGE_PHYSIC_OFFSET_POSITION = 28;
    /**
     * 存储时间戳的位置
     */
    public final static int MESSAGE_STORE_TIMESTAMP_POSITION = 56;

    // 消息魔法编码
    // Set message magic code v2 if topic length > 127
    public final static int MESSAGE_MAGIC_CODE = -626843481;
    public final static int MESSAGE_MAGIC_CODE_V2 = -626843477;

    // End of file empty MAGIC CODE cbd43194
    public final static int BLANK_MAGIC_CODE = -875286124;
    /**
     * 名称-值分隔符
     */
    public static final char NAME_VALUE_SEPARATOR = 1;
    /**
     * 属性分隔符
     */
    public static final char PROPERTY_SEPARATOR = 2;
    /**
     * 物理位置
     */
    public static final int PHY_POS_POSITION = 4 + 4 + 4 + 4 + 4 + 8;
    /**
     * 消息位点位置
     */
    public static final int QUEUE_OFFSET_POSITION = 4 + 4 + 4 + 4 + 4;
    /**
     * 系统标志位置
     */
    public static final int SYSFLAG_POSITION = 4 + 4 + 4 + 4 + 4 + 8 + 8;
//    public static final int BODY_SIZE_POSITION = 4 // 1 TOTALSIZE
//        + 4 // 2 MAGICCODE
//        + 4 // 3 BODYCRC
//        + 4 // 4 QUEUEID
//        + 4 // 5 FLAG
//        + 8 // 6 QUEUEOFFSET
//        + 8 // 7 PHYSICALOFFSET
//        + 4 // 8 SYSFLAG
//        + 8 // 9 BORNTIMESTAMP
//        + 8 // 10 BORNHOST
//        + 8 // 11 STORETIMESTAMP
//        + 8 // 12 STOREHOSTADDRESS
//        + 4 // 13 RECONSUMETIMES
//        + 8; // 14 Prepared Transaction Offset

    // 消息ID

    public static String createMessageId(final ByteBuffer input, final ByteBuffer addr, final long offset) {
        input.flip();
        int msgIDLength = addr.limit() == 8 ? 16 : 28;
        input.limit(msgIDLength);

        input.put(addr);
        input.putLong(offset);

        return UtilAll.bytes2string(input.array());
    }

    public static String createMessageId(SocketAddress socketAddress, long transactionIdHashCode) {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
        int msgIDLength = inetSocketAddress.getAddress() instanceof Inet4Address ? 16 : 28;
        ByteBuffer byteBuffer = ByteBuffer.allocate(msgIDLength);
        // 消息ID：套接字原生ip地址+端口+事务id散列码
        byteBuffer.put(inetSocketAddress.getAddress().getAddress());
        byteBuffer.putInt(inetSocketAddress.getPort());
        byteBuffer.putLong(transactionIdHashCode);
        byteBuffer.flip();
        return UtilAll.bytes2string(byteBuffer.array());
    }

    public static MessageId decodeMessageId(final String msgId) throws UnknownHostException {
        byte[] bytes = UtilAll.string2bytes(msgId);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);

        // address(ip+port)
        byte[] ip = new byte[msgId.length() == 32 ? 4 : 16];
        byteBuffer.get(ip);
        int port = byteBuffer.getInt();
        SocketAddress address = new InetSocketAddress(InetAddress.getByAddress(ip), port);

        // offset-消息位点
        long offset = byteBuffer.getLong();

        return new MessageId(address, offset);
    }

    // 消息属性

    /**
     * Just decode properties from msg buffer.
     * 从消息的字节缓冲区解码消息属性
     *
     * @param byteBuffer msg commit log buffer.
     */
    public static Map<String, String> decodeProperties(ByteBuffer byteBuffer) {
        int sysFlag = byteBuffer.getInt(SYSFLAG_POSITION);
        int magicCode = byteBuffer.getInt(MESSAGE_MAGIC_CODE_POSITION);
        // 消息版本
        MessageVersion version = MessageVersion.valueOfMagicCode(magicCode);

        int bornHostLength = (sysFlag & MessageSysFlag.BORNHOST_V6_FLAG) == 0 ? 8 : 20;
        int storeHostAddressLength = (sysFlag & MessageSysFlag.STOREHOSTADDRESS_V6_FLAG) == 0 ? 8 : 20;
        // 消息体位置
        int bodySizePosition = 4 // 1 TOTALSIZE
                + 4 // 2 MAGICCODE
                + 4 // 3 BODYCRC
                + 4 // 4 QUEUEID
                + 4 // 5 FLAG
                + 8 // 6 QUEUEOFFSET
                + 8 // 7 PHYSICALOFFSET
                + 4 // 8 SYSFLAG
                + 8 // 9 BORNTIMESTAMP
                + bornHostLength // 10 BORNHOST
                + 8 // 11 STORETIMESTAMP
                + storeHostAddressLength // 12 STOREHOSTADDRESS
                + 4 // 13 RECONSUMETIMES
                + 8; // 14 Prepared Transaction Offset

        // 主题
        int topicLengthPosition = bodySizePosition + 4 + byteBuffer.getInt(bodySizePosition);
        byteBuffer.position(topicLengthPosition);
        int topicLengthSize = version.getTopicLengthSize();
        int topicLength = version.getTopicLength(byteBuffer);

        // 属性位置
        int propertiesPosition = topicLengthPosition + topicLengthSize + topicLength;
        short propertiesLength = byteBuffer.getShort(propertiesPosition);
        byteBuffer.position(propertiesPosition + 2);

        if (propertiesLength > 0) {
            byte[] properties = new byte[propertiesLength];
            byteBuffer.get(properties);
            String propertiesString = new String(properties, CHARSET_UTF8);
            return string2messageProperties(propertiesString);
        }
        return null;
    }

    // 扩展消息
    // 从消息的字节缓冲区解码扩展消息

    public static MessageExt decode(ByteBuffer byteBuffer) {
        return decode(byteBuffer, true, true, false);
    }

    public static MessageExt clientDecode(ByteBuffer byteBuffer, final boolean readBody) {
        return decode(byteBuffer, readBody, true, true);
    }

    public static MessageExt decode(ByteBuffer byteBuffer, final boolean readBody) {
        return decode(byteBuffer, readBody, true, false);
    }

    // 编码拓展消息

    public static byte[] encode(MessageExt messageExt, boolean needCompress) throws Exception {
        // 消息体
        byte[] body = messageExt.getBody();
        // 主题
        byte[] topics = messageExt.getTopic().getBytes(CHARSET_UTF8);
        byte topicLen = (byte) topics.length;
        // 消息属性
        String properties = messageProperties2String(messageExt.getProperties());
        byte[] propertiesBytes = properties.getBytes(CHARSET_UTF8);
        short propertiesLength = (short) propertiesBytes.length;
        // 系统标志
        int sysFlag = messageExt.getSysFlag();
        // 出生主机
        int bornHostLength = (sysFlag & MessageSysFlag.BORNHOST_V6_FLAG) == 0 ? 8 : 20;
        // 存储主机地址
        int storeHostAddressLength = (sysFlag & MessageSysFlag.STOREHOSTADDRESS_V6_FLAG) == 0 ? 8 : 20;
        // 新的消息体
        byte[] newBody = messageExt.getBody();
        if (needCompress && (sysFlag & MessageSysFlag.COMPRESSED_FLAG) == MessageSysFlag.COMPRESSED_FLAG) {
            // 压缩消息体
            Compressor compressor = CompressorFactory.getCompressor(MessageSysFlag.getCompressionType(sysFlag));
            newBody = compressor.compress(body, 5);
        }
        int bodyLength = newBody.length;
        // 消息的存储大小
        int storeSize = messageExt.getStoreSize();
        // 存储字节缓冲区
        ByteBuffer byteBuffer;
        if (storeSize > 0) {
            byteBuffer = ByteBuffer.allocate(storeSize);
        } else {
            storeSize = 4 // 1 TOTALSIZE
                    + 4 // 2 MAGICCODE
                    + 4 // 3 BODYCRC
                    + 4 // 4 QUEUEID
                    + 4 // 5 FLAG
                    + 8 // 6 QUEUEOFFSET
                    + 8 // 7 PHYSICALOFFSET
                    + 4 // 8 SYSFLAG
                    + 8 // 9 BORNTIMESTAMP
                    + bornHostLength // 10 BORNHOST
                    + 8 // 11 STORETIMESTAMP
                    + storeHostAddressLength // 12 STOREHOSTADDRESS
                    + 4 // 13 RECONSUMETIMES
                    + 8 // 14 Prepared Transaction Offset
                    + 4 + bodyLength // 14 BODY
                    + 1 + topicLen // 15 TOPIC
                    + 2 + propertiesLength // 16 propertiesLength
                    + 0;
            byteBuffer = ByteBuffer.allocate(storeSize);
        }
        // 1 TOTALSIZE
        // 存储大小
        byteBuffer.putInt(storeSize);

        // 2 MAGICCODE
        // 魔法编码
        byteBuffer.putInt(MESSAGE_MAGIC_CODE);

        // 3 BODYCRC
        // 消息体的循环冗余码校验
        int bodyCRC = messageExt.getBodyCRC();
        byteBuffer.putInt(bodyCRC);

        // 4 QUEUEID
        // 队列ID
        int queueId = messageExt.getQueueId();
        byteBuffer.putInt(queueId);

        // 5 FLAG
        int flag = messageExt.getFlag();
        byteBuffer.putInt(flag);

        // 6 QUEUEOFFSET
        // 消息位点
        long queueOffset = messageExt.getQueueOffset();
        byteBuffer.putLong(queueOffset);

        // 7 PHYSICALOFFSET
        // 物理存储位点
        long physicOffset = messageExt.getCommitLogOffset();
        byteBuffer.putLong(physicOffset);

        // 8 SYSFLAG
        byteBuffer.putInt(sysFlag);

        // 9 BORNTIMESTAMP
        long bornTimeStamp = messageExt.getBornTimestamp();
        byteBuffer.putLong(bornTimeStamp);

        // 10 BORNHOST
        // 出生主机：ip+port
        InetSocketAddress bornHost = (InetSocketAddress) messageExt.getBornHost();
        byteBuffer.put(bornHost.getAddress().getAddress());
        byteBuffer.putInt(bornHost.getPort());

        // 11 STORETIMESTAMP
        long storeTimestamp = messageExt.getStoreTimestamp();
        byteBuffer.putLong(storeTimestamp);

        // 12 STOREHOST
        // 服务器存储主机：ip+port
        InetSocketAddress serverHost = (InetSocketAddress) messageExt.getStoreHost();
        byteBuffer.put(serverHost.getAddress().getAddress());
        byteBuffer.putInt(serverHost.getPort());

        // 13 RECONSUMETIMES
        // 消费次数
        int reconsumeTimes = messageExt.getReconsumeTimes();
        byteBuffer.putInt(reconsumeTimes);

        // 14 Prepared Transaction Offset
        // 事务位点
        long preparedTransactionOffset = messageExt.getPreparedTransactionOffset();
        byteBuffer.putLong(preparedTransactionOffset);

        // 15 BODY
        // 消息体
        byteBuffer.putInt(bodyLength);
        byteBuffer.put(newBody);

        // 16 TOPIC
        // 主题
        byteBuffer.put(topicLen);
        byteBuffer.put(topics);

        // 17 properties
        // 消息属性
        byteBuffer.putShort(propertiesLength);
        byteBuffer.put(propertiesBytes);

        return byteBuffer.array();
    }

    /**
     * Encode without store timestamp and store host, skip blank msg.
     *
     * @param messageExt   msg
     * @param needCompress need compress or not
     * @return byte array
     * @throws IOException when compress failed
     */
    public static byte[] encodeUniquely(MessageExt messageExt, boolean needCompress) throws IOException {
        // 消息体
        byte[] body = messageExt.getBody();
        // 主题
        byte[] topics = messageExt.getTopic().getBytes(CHARSET_UTF8);
        byte topicLen = (byte) topics.length;
        // 消息属性
        String properties = messageProperties2String(messageExt.getProperties());
        byte[] propertiesBytes = properties.getBytes(CHARSET_UTF8);
        short propertiesLength = (short) propertiesBytes.length;
        int sysFlag = messageExt.getSysFlag();
        int bornHostLength = (sysFlag & MessageSysFlag.BORNHOST_V6_FLAG) == 0 ? 8 : 20;
        // 消息体
        byte[] newBody = messageExt.getBody();
        if (needCompress && (sysFlag & MessageSysFlag.COMPRESSED_FLAG) == MessageSysFlag.COMPRESSED_FLAG) {
            newBody = UtilAll.compress(body, 5);
        }
        int bodyLength = newBody.length;
        // 存储大小
        int storeSize = messageExt.getStoreSize();
        // 消息的字节缓冲区
        ByteBuffer byteBuffer;
        if (storeSize > 0) {
            byteBuffer = ByteBuffer.allocate(storeSize - 8); // except size for store timestamp
        } else {
            storeSize = 4 +  // 1 TOTALSIZE
                    4 +  // 2 MAGICCODE
                    4 +  // 3 BODYCRC
                    4 +  // 4 QUEUEID
                    4 +  // 5 FLAG
                    8 +  // 6 QUEUEOFFSET
                    8 +  // 7 PHYSICALOFFSET
                    4 +  // 8 SYSFLAG
                    8 +  // 9 BORNTIMESTAMP
                    bornHostLength + // 10 BORNHOST
                    4 +  // 11 RECONSUMETIMES
                    8 +  // 12 Prepared Transaction Offset
                    4 + bodyLength +  // 13 BODY
                    1 + topicLen +  // 14 TOPIC
                    2 + propertiesLength // 15 propertiesLength
            ;
            byteBuffer = ByteBuffer.allocate(storeSize);
        }

        // 1 TOTALSIZE
        // 存储大小
        byteBuffer.putInt(storeSize);

        // 2 MAGICCODE
        // 魔法编码
        byteBuffer.putInt(MESSAGE_MAGIC_CODE);

        // 3 BODYCRC
        // 消息体的循环冗余码校验
        int bodyCRC = messageExt.getBodyCRC();
        byteBuffer.putInt(bodyCRC);

        // 4 QUEUEID
        // 队列ID
        int queueId = messageExt.getQueueId();
        byteBuffer.putInt(queueId);

        // 5 FLAG
        // 业务标志
        int flag = messageExt.getFlag();
        byteBuffer.putInt(flag);

        // 6 QUEUEOFFSET
        // 消息位点
        long queueOffset = messageExt.getQueueOffset();
        byteBuffer.putLong(queueOffset);

        // 7 PHYSICALOFFSET
        // 物理存储位点
        long physicOffset = messageExt.getCommitLogOffset();
        byteBuffer.putLong(physicOffset);

        // 8 SYSFLAG
        // 系统标志
        byteBuffer.putInt(sysFlag);

        // 9 BORNTIMESTAMP
        // 出生时间戳
        long bornTimeStamp = messageExt.getBornTimestamp();
        byteBuffer.putLong(bornTimeStamp);

        // 10 BORNHOST
        // 出生主机：ip+port
        InetSocketAddress bornHost = (InetSocketAddress) messageExt.getBornHost();
        byteBuffer.put(bornHost.getAddress().getAddress());
        byteBuffer.putInt(bornHost.getPort());

        // 11 RECONSUMETIMES
        // 消费次数
        int reconsumeTimes = messageExt.getReconsumeTimes();
        byteBuffer.putInt(reconsumeTimes);

        // 12 Prepared Transaction Offset
        // 事务点位
        long preparedTransactionOffset = messageExt.getPreparedTransactionOffset();
        byteBuffer.putLong(preparedTransactionOffset);

        // 13 BODY
        // 消息体
        byteBuffer.putInt(bodyLength);
        byteBuffer.put(newBody);

        // 14 TOPIC
        // 主题
        byteBuffer.put(topicLen);
        byteBuffer.put(topics);

        // 15 properties
        // 消息属性
        byteBuffer.putShort(propertiesLength);
        byteBuffer.put(propertiesBytes);

        return byteBuffer.array();
    }

    // 从消息的字节缓冲区解码扩展消息

    public static MessageExt decode(
            ByteBuffer byteBuffer, final boolean readBody, final boolean deCompressBody) {
        return decode(byteBuffer, readBody, deCompressBody, false);
    }

    public static MessageExt decode(
            ByteBuffer byteBuffer, final boolean readBody, final boolean deCompressBody, final boolean isClient) {
        return decode(byteBuffer, readBody, deCompressBody, isClient,
                false, false);
    }

    public static MessageExt decode(
            ByteBuffer byteBuffer, final boolean readBody, final boolean deCompressBody, final boolean isClient,
            final boolean isSetPropertiesString) {
        return decode(byteBuffer, readBody, deCompressBody, isClient,
                isSetPropertiesString, false);
    }

    public static MessageExt decode(
            ByteBuffer byteBuffer, final boolean readBody, final boolean deCompressBody, final boolean isClient,
            final boolean isSetPropertiesString, final boolean checkCRC) {
        try {
            MessageExt msgExt;
            if (isClient) {
                msgExt = new MessageClientExt();
            } else {
                msgExt = new MessageExt();
            }

            // 1 TOTALSIZE
            // 存储大小
            int storeSize = byteBuffer.getInt();
            msgExt.setStoreSize(storeSize);

            // 2 MAGICCODE
            // 魔法编码
            int magicCode = byteBuffer.getInt();
            MessageVersion version = MessageVersion.valueOfMagicCode(magicCode);

            // 3 BODYCRC
            // 消息体的循环冗余码校验
            int bodyCRC = byteBuffer.getInt();
            msgExt.setBodyCRC(bodyCRC);

            // 4 QUEUEID
            // 队列ID
            int queueId = byteBuffer.getInt();
            msgExt.setQueueId(queueId);

            // 5 FLAG
            // 业务标志
            int flag = byteBuffer.getInt();
            msgExt.setFlag(flag);

            // 6 QUEUEOFFSET
            // 消息位点
            long queueOffset = byteBuffer.getLong();
            msgExt.setQueueOffset(queueOffset);

            // 7 PHYSICALOFFSET
            // 物理存储位点
            long physicOffset = byteBuffer.getLong();
            msgExt.setCommitLogOffset(physicOffset);

            // 8 SYSFLAG
            // 系统标志
            int sysFlag = byteBuffer.getInt();
            msgExt.setSysFlag(sysFlag);

            // 9 BORNTIMESTAMP
            // 出生时间戳
            long bornTimeStamp = byteBuffer.getLong();
            msgExt.setBornTimestamp(bornTimeStamp);

            // 10 BORNHOST
            // 出生主机
            int bornHostIPLength = (sysFlag & MessageSysFlag.BORNHOST_V6_FLAG) == 0 ? 4 : 16;
            byte[] bornHost = new byte[bornHostIPLength];
            byteBuffer.get(bornHost, 0, bornHostIPLength);
            int port = byteBuffer.getInt();
            msgExt.setBornHost(new InetSocketAddress(InetAddress.getByAddress(bornHost), port));

            // 11 STORETIMESTAMP
            // 存储时间戳
            long storeTimestamp = byteBuffer.getLong();
            msgExt.setStoreTimestamp(storeTimestamp);

            // 12 STOREHOST
            // 存储主机
            int storeHostIPLength = (sysFlag & MessageSysFlag.STOREHOSTADDRESS_V6_FLAG) == 0 ? 4 : 16;
            byte[] storeHost = new byte[storeHostIPLength];
            byteBuffer.get(storeHost, 0, storeHostIPLength);
            port = byteBuffer.getInt();
            msgExt.setStoreHost(new InetSocketAddress(InetAddress.getByAddress(storeHost), port));

            // 13 RECONSUMETIMES
            // 消费次数
            int reconsumeTimes = byteBuffer.getInt();
            msgExt.setReconsumeTimes(reconsumeTimes);

            // 14 Prepared Transaction Offset
            // 事务点位
            long preparedTransactionOffset = byteBuffer.getLong();
            msgExt.setPreparedTransactionOffset(preparedTransactionOffset);

            // 15 BODY
            // 消息体
            int bodyLen = byteBuffer.getInt();
            if (bodyLen > 0) {
                if (readBody) {
                    byte[] body = new byte[bodyLen];
                    byteBuffer.get(body);

                    if (checkCRC) {
                        //crc body
                        int crc = UtilAll.crc32(body, 0, bodyLen);
                        if (crc != bodyCRC) {
                            throw new Exception("Msg crc is error!");
                        }
                    }

                    // decompress body
                    if (deCompressBody && (sysFlag & MessageSysFlag.COMPRESSED_FLAG) == MessageSysFlag.COMPRESSED_FLAG) {
                        // 解压缩消息体
                        Compressor compressor = CompressorFactory.getCompressor(MessageSysFlag.getCompressionType(sysFlag));
                        body = compressor.decompress(body);
                    }

                    msgExt.setBody(body);
                } else {
                    // 跳过消息体
                    byteBuffer.position(byteBuffer.position() + bodyLen);
                }
            }

            // 16 TOPIC
            // 主题
            int topicLen = version.getTopicLength(byteBuffer);
            byte[] topic = new byte[topicLen];
            byteBuffer.get(topic);
            msgExt.setTopic(new String(topic, CHARSET_UTF8));

            // 17 properties
            // 消息属性
            short propertiesLength = byteBuffer.getShort();
            if (propertiesLength > 0) {
                byte[] properties = new byte[propertiesLength];
                byteBuffer.get(properties);
                String propertiesString = new String(properties, CHARSET_UTF8);
                Map<String, String> map = string2messageProperties(propertiesString);
                if (isSetPropertiesString) {
                    map.put("propertiesString", propertiesString);
                }
                msgExt.setProperties(map);
            }

            // 消息ID
            int msgIDLength = storeHostIPLength + 4 + 8;
            ByteBuffer byteBufferMsgId = ByteBuffer.allocate(msgIDLength);
            String msgId = createMessageId(byteBufferMsgId, msgExt.getStoreHostBytes(), msgExt.getCommitLogOffset());
            msgExt.setMsgId(msgId);

            if (isClient) {
                ((MessageClientExt) msgExt).setOffsetMsgId(msgId);
            }

            return msgExt;
        } catch (Exception e) {
            byteBuffer.position(byteBuffer.limit());
        }

        return null;
    }

    // 解码批处理消息

    public static List<MessageExt> decodes(ByteBuffer byteBuffer) {
        return decodes(byteBuffer, true);
    }

    public static List<MessageExt> decodesBatch(
            ByteBuffer byteBuffer,
            final boolean readBody, final boolean decompressBody, final boolean isClient) {
        List<MessageExt> msgExtList = new ArrayList<>(10);
        while (byteBuffer.hasRemaining()) {
            MessageExt msgExt = decode(byteBuffer, readBody, decompressBody, isClient);
            if (null != msgExt) {
                msgExtList.add(msgExt);
            } else {
                break;
            }
        }
        return msgExtList;
    }

    public static List<MessageExt> decodes(ByteBuffer byteBuffer, final boolean readBody) {
        List<MessageExt> msgExtList = new ArrayList<>(10);
        while (byteBuffer.hasRemaining()) {
            MessageExt msgExt = clientDecode(byteBuffer, readBody);
            if (null != msgExt) {
                msgExtList.add(msgExt);
            } else {
                break;
            }
        }
        return msgExtList;
    }

    // 消息属性<=>字符串

    public static String messageProperties2String(Map<String, String> properties) {
        if (properties == null) {
            return "";
        }
        int len = 0;
        for (final Map.Entry<String, String> entry : properties.entrySet()) {
            final String name = entry.getKey();
            final String value = entry.getValue();
            if (value == null) {
                continue;
            }
            if (name != null) {
                len += name.length();
            }
            len += value.length();
            len += 2; // separator
        }
        // 消息属性的字符串
        StringBuilder sb = new StringBuilder(len);
        for (final Map.Entry<String, String> entry : properties.entrySet()) {
            final String name = entry.getKey();
            final String value = entry.getValue();

            if (value == null) {
                continue;
            }
            sb.append(name);
            sb.append(NAME_VALUE_SEPARATOR);
            sb.append(value);
            sb.append(PROPERTY_SEPARATOR);
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public static Map<String, String> string2messageProperties(final String properties) {
        Map<String, String> map = new HashMap<>(128);
        if (properties != null) {
            int len = properties.length();
            int index = 0;
            while (index < len) {
                int newIndex = properties.indexOf(PROPERTY_SEPARATOR, index);
                if (newIndex < 0) {
                    newIndex = len;
                }
                if (newIndex - index >= 3) {
                    int kvSepIndex = properties.indexOf(NAME_VALUE_SEPARATOR, index);
                    if (kvSepIndex > index && kvSepIndex < newIndex - 1) {
                        String k = properties.substring(index, kvSepIndex);
                        String v = properties.substring(kvSepIndex + 1, newIndex);
                        map.put(k, v);
                    }
                }
                index = newIndex + 1;
            }
        }

        return map;
    }

    // 消息
    // 编码消息

    public static byte[] encodeMessage(Message message) {
        //only need flag, body, properties
        // 消息体
        byte[] body = message.getBody();
        int bodyLength = body.length;
        // 消息属性
        String properties = messageProperties2String(message.getProperties());
        byte[] propertiesBytes = properties.getBytes(CHARSET_UTF8);
        //note properties length must not more than Short.MAX
        short propertiesLength = (short) propertiesBytes.length;
//        int sysFlag = message.getFlag();
        // 存储大小
        int storeSize = 4 // 1 TOTALSIZE
                + 4 // 2 MAGICCOD
                + 4 // 3 BODYCRC
                + 4 // 4 FLAG
                + 4 + bodyLength // 4 BODY
                + 2 + propertiesLength;
        ByteBuffer byteBuffer = ByteBuffer.allocate(storeSize);
        // 1 TOTALSIZE
        // 存储大小
        byteBuffer.putInt(storeSize);

        // 2 MAGICCODE
        // 魔法编码
        byteBuffer.putInt(0);

        // 3 BODYCRC
        // 消息体的循环冗余码校验
        byteBuffer.putInt(0);

        // 4 FLAG
        // 业务标志
        int flag = message.getFlag();
        byteBuffer.putInt(flag);

        // 5 BODY
        // 消息体
        byteBuffer.putInt(bodyLength);
        byteBuffer.put(body);

        // 6 properties
        // 消息属性
        byteBuffer.putShort(propertiesLength);
        byteBuffer.put(propertiesBytes);

        return byteBuffer.array();
    }

    // 解码消息

    public static Message decodeMessage(ByteBuffer byteBuffer) {
        Message message = new Message();

        // 1 TOTALSIZE
        // 存储大小
        byteBuffer.getInt();

        // 2 MAGICCODE
        // 魔法编码
        byteBuffer.getInt();

        // 3 BODYCRC
        // 消息体的循环冗余码校验
        byteBuffer.getInt();

        // 4 FLAG
        // 业务标志
        int flag = byteBuffer.getInt();
        message.setFlag(flag);

        // 5 BODY
        // 消息体
        int bodyLen = byteBuffer.getInt();
        byte[] body = new byte[bodyLen];
        byteBuffer.get(body);
        message.setBody(body);

        // 6 properties
        // 消息属性
        short propertiesLength = byteBuffer.getShort();
        byte[] properties = new byte[propertiesLength];
        byteBuffer.get(properties);
        String propertiesString = new String(properties, CHARSET_UTF8);
        Map<String, String> map = string2messageProperties(propertiesString);
        message.setProperties(map);

        return message;
    }

    // 批处理消息

    public static byte[] encodeMessages(List<Message> messages) {
        //TO DO refactor, accumulate in one buffer, avoid copies
        List<byte[]> encodedMessages = new ArrayList<>(messages.size());
        int allSize = 0;
        for (Message message : messages) {
            byte[] bytes = encodeMessage(message);
            encodedMessages.add(bytes);
            allSize += bytes.length;
        }
        byte[] allBytes = new byte[allSize];
        int pos = 0;
        for (byte[] bytes : encodedMessages) {
            System.arraycopy(bytes, 0, allBytes, pos, bytes.length);
            pos += bytes.length;
        }
        return allBytes;
    }

    public static List<Message> decodeMessages(ByteBuffer byteBuffer) {
        //TO DO add a callback for processing, avoid creating lists
        List<Message> messageList = new ArrayList<>();
        while (byteBuffer.hasRemaining()) {
            Message message = decodeMessage(byteBuffer);
            messageList.add(message);
        }
        return messageList;
    }

    public static void decodeMessage(MessageExt messageExt, List<MessageExt> list) {
        List<Message> messages = decodeMessages(ByteBuffer.wrap(messageExt.getBody()));
        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            MessageClientExt messageClientExt = new MessageClientExt();
            // 主题
            messageClientExt.setTopic(messageExt.getTopic());
            // 消息位点
            messageClientExt.setQueueOffset(messageExt.getQueueOffset() + i);
            // 队列ID
            messageClientExt.setQueueId(messageExt.getQueueId());
            // 业务标志
            messageClientExt.setFlag(message.getFlag());
            // 消息属性
            MessageAccessor.setProperties(messageClientExt, message.getProperties());
            // 消息体
            messageClientExt.setBody(message.getBody());
            // 出生主机
            messageClientExt.setBornHost(messageExt.getBornHost());
            // 出生时间戳
            messageClientExt.setBornTimestamp(messageExt.getBornTimestamp());
            // 存储主机
            messageClientExt.setStoreHost(messageExt.getStoreHost());
            // 存储时间戳
            messageClientExt.setStoreTimestamp(messageExt.getStoreTimestamp());
            // 系统标志
            messageClientExt.setSysFlag(messageExt.getSysFlag());
            // 提交日志位点
            messageClientExt.setCommitLogOffset(messageExt.getCommitLogOffset());
            // 表示消息是否在服务器落盘后才返回应答
            messageClientExt.setWaitStoreMsgOK(messageExt.isWaitStoreMsgOK());
            list.add(messageClientExt);
        }
    }

    /**
     * 统计消息数量。
     *
     * @param buffer 消息的字节缓冲区
     */
    public static int countInnerMsgNum(ByteBuffer buffer) {
        int count = 0;
        while (buffer.hasRemaining()) {
            count++;
            int currPos = buffer.position();
            int size = buffer.getInt();
            buffer.position(currPos + size);
        }
        return count;
    }
}
