package com.yuzhouwan.bigdata.kafka.util;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.yuzhouwan.bigdata.kafka.util.pc.AvroEvent;
import com.yuzhouwan.bigdata.kafka.util.pc.AvroEventFactory;
import com.yuzhouwan.bigdata.kafka.util.pc.AvroEventProducer;
import com.yuzhouwan.bigdata.kafka.util.pc.AvroEventWorkHandler;
import com.yuzhouwan.common.util.DecimalUtils;

import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Copyright @ 2017 yuzhouwan.com
 * All right reserved.
 * Function：Kafka Utils Test
 *
 * @author Benedict Jin
 * @since 2016/11/25
 */
public class KafkaUtilsTest {

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {

        Executor executor = Executors.newCachedThreadPool();
        AvroEventFactory factory = new AvroEventFactory();
        int bufferSize = 1024 * 1024 * 16;
        Disruptor<AvroEvent> disruptor = new Disruptor<>(factory,
                bufferSize, executor,
                ProducerType.MULTI,
                new BlockingWaitStrategy());
        disruptor.handleEventsWithWorkerPool(
                new AvroEventWorkHandler(KafkaConnPoolUtils.getPool().iterator().next(), "topic", 1)/*,...*/);
        disruptor.start();

        RingBuffer<AvroEvent> ringBuffer = disruptor.getRingBuffer();
        AvroEventProducer producer = new AvroEventProducer(ringBuffer);
        AvroEventProducer producer2 = new AvroEventProducer(ringBuffer);
        AvroEventProducer producer3 = new AvroEventProducer(ringBuffer);
        ByteBuffer bb = ByteBuffer.allocate(8);
        long start = System.currentTimeMillis();
        long index;
        for (long l = 0; l < 100_0000; l++) {
            bb.putLong(0, l);
            index = l % 3;
            if (index == 0) producer.product(getByteBuffer(bb));
            else if (index == 1) producer2.product(getByteBuffer(bb));
            else producer3.product(getByteBuffer(bb));
        }
        System.out.println(String.format("Time: %dms", System.currentTimeMillis() - start));        // Time: 98ms
        disruptor.shutdown();
        System.exit(0);
    }

    private static byte[] getByteBuffer(ByteBuffer bb) {
        return DecimalUtils.byteBuffer2byteArray(bb);
    }
}