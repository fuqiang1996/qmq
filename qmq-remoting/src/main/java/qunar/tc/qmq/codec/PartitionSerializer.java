package qunar.tc.qmq.codec;

import com.google.common.collect.Range;
import io.netty.buffer.ByteBuf;
import qunar.tc.qmq.meta.Partition;
import qunar.tc.qmq.utils.PayloadHolderUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author zhenwei.liu
 * @since 2019-08-28
 */
public class PartitionSerializer extends ObjectSerializer<Partition> {

    private ParameterizedType rangeType = Types.newParameterizedType(null, Range.class, new Type[]{
            Integer.class
    });

    @Override
    void doSerialize(Partition partition, ByteBuf buf, short version) {
        PayloadHolderUtils.writeString(partition.getSubject(), buf);
        PayloadHolderUtils.writeString(partition.getPartitionName(), buf);
        buf.writeInt(partition.getPartitionId());
        Serializer<Range> rangeSerializer = Serializers.getSerializer(Range.class);
        rangeSerializer.serialize(partition.getLogicalPartition(), buf, version);
        PayloadHolderUtils.writeString(partition.getBrokerGroup(), buf);
        PayloadHolderUtils.writeString(partition.getStatus().name(), buf);
    }

    @Override
    Partition doDeserialize(ByteBuf buf, Type type, short version) {
        String subject = PayloadHolderUtils.readString(buf);
        String partitionName = PayloadHolderUtils.readString(buf);
        int partitionId = buf.readInt();
        Serializer<Range> rangeSerializer = Serializers.getSerializer(Range.class);
        Range logicalRange = rangeSerializer.deserialize(buf, rangeType, version);
        String brokerGroup = PayloadHolderUtils.readString(buf);
        Partition.Status status = Partition.Status.valueOf(PayloadHolderUtils.readString(buf));
        return new Partition(
                subject,
                partitionName,
                partitionId,
                logicalRange,
                brokerGroup,
                status
        );
    }
}