package com.sofm.recommend.infrastructure.kafka.consumer;

import com.sofm.recommend.common.code.UgcType;
import com.sofm.recommend.common.dto.MessageDto;
import com.sofm.recommend.common.utils.JSONUtils;
import com.sofm.recommend.domain.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class KafkaMessageHandler implements MessageListener<String, String> {

    private static final List<String> needToStream = List.of(UgcType.CLICK.getCode(), UgcType.LIKE.getCode(), UgcType.SHARE.getCode(), UgcType.COMMENT.getCode());
    private static final Pattern pattern = Pattern.compile("UserId::(\\d+)\\s+(\\w+|\\w+_\\w+)\\s+ItemId::(\\d+(?:,\\d+)*)\\s+TimeStamp::(\\d+)(?:\\s+RId::([\\w-]+))?");


    private final KafkaMessageForwarder kafkaMessageForwarder;
    private final UserService userService;

    public KafkaMessageHandler(KafkaMessageForwarder kafkaMessageForwarder, UserService userService) {
        this.kafkaMessageForwarder = kafkaMessageForwarder;
        this.userService = userService;
    }

    @Override
    public void onMessage(ConsumerRecord<String, String> record) {
        log.info("Received message: {}", record.value());
        MessageDto msg = parseMessage(record.value());
        if (needToStream.contains(msg.getType())) {
            if (!msg.getItemsId().isEmpty()) {
                kafkaMessageForwarder.forwardMessage("ugc-stream", msg.getItemsId().get(0), JSONUtils.toJson(msg));
            }
        }
        userService.processMsg(msg);
    }


    public MessageDto parseMessage(String message) {
        Matcher matcher = pattern.matcher(message);
        if (matcher.matches()) {
            MessageDto dto = new MessageDto();
            dto.setUserId(Integer.parseInt(matcher.group(1)));     // 提取 UserId
            dto.setType(matcher.group(2));                         // 提取类型
            String items = matcher.group(3);
            List<String> itemIds = Arrays.asList(items.split(","));
            dto.setItemsId(itemIds);
            dto.setTimestamp(Long.parseLong(matcher.group(4)));
            return dto;
        } else {
            throw new IllegalArgumentException("Message :" + message + " format is invalid");
        }
    }
}
