package com.zjzcn.test;

import kafka.admin.AdminClient;
import kafka.admin.AdminUtils;
import kafka.admin.TopicCommand;
import kafka.common.TopicAndPartition;
import kafka.coordinator.group.GroupOverview;
import kafka.utils.ZkUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import scala.collection.JavaConversions;
import scala.collection.Seq;

import java.util.*;


public class KafkaAdminClient {

    private String defaultBootstrapServers;

    private ZkUtils zkUtils;
    private AdminClient adminClient;
    private TopicCommand topicCommand;

    public KafkaAdminClient(String zkConnect, String defaultBootstrapServers) {
        this.defaultBootstrapServers = defaultBootstrapServers;
        zkUtils = ZkUtils.apply(zkConnect, 30000, 30000, false);

        Properties config = new Properties();
        config.setProperty(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, defaultBootstrapServers);
        adminClient = AdminClient.create(config);
    }

    public int getController() {
        return zkUtils.getController();
    }

    public List<String> getAllTopics() {
        Seq<String> topics = zkUtils.getAllTopics();
        return JavaConversions.seqAsJavaList(topics);
    }

    public void createTopic(String topic, int partitions, int replicas) {
        TopicCommand.TopicCommandOptions opt = new TopicCommand.TopicCommandOptions(null);
        topicCommand.createTopic(zkUtils, opt);
    }

    public Map<String, Properties> getTopicMetadata(String topic) {
        scala.collection.Map<String, Properties> configs = AdminUtils.fetchAllTopicConfigs(zkUtils);
        return JavaConversions.mapAsJavaMap(configs);
    }

    public Collection<Node> getAllBrokerNodes() {
        scala.collection.immutable.List<Node> brokers = adminClient.findAllBrokers();
        return JavaConversions.asJavaCollection(brokers);
    }

    public Set<TopicAndPartition> getAllPartitions() {
        scala.collection.Set<TopicAndPartition> partitions = zkUtils.getAllPartitions();
        return JavaConversions.setAsJavaSet(partitions);
    }

    public String getBootstrapServers() {
        StringBuilder sb = new StringBuilder();
        for(Node node : getAllBrokerNodes()) {
            sb.append(node.host()).append(":").append(node.port()).append(",");
        }
        if(sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.length() == 0 ? defaultBootstrapServers : sb.toString();
    }

    public List<String> getAllGroups() {
        List<GroupOverview> overviews = JavaConversions.seqAsJavaList(adminClient.listAllConsumerGroupsFlattened());
        List<String> groups = new ArrayList<>();
        for(GroupOverview overview : overviews) {
            groups.add(overview.groupId());
        }
        return groups;
    }

    public AdminClient.ConsumerGroupSummary getGroupPartitions(String group) {
        AdminClient.ConsumerGroupSummary consumerGroupSummary = adminClient.describeConsumerGroup(group, 10000);
        return consumerGroupSummary;
    }

    public Map<TopicPartition, Object> getOffsets(String group) {
        scala.collection.immutable.Map<TopicPartition, Object> offsets = adminClient.listGroupOffsets(group);
        return JavaConversions.mapAsJavaMap(offsets);
    }

    public void close() {
        if(adminClient != null) {
            adminClient.close();
        }
        if(zkUtils != null) {
            zkUtils.close();
        }
    }

    private KafkaConsumer<String, String> createConsumer(String group) {
        Properties config = new Properties();
        config.setProperty(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());
        config.put(ConsumerConfig.GROUP_ID_CONFIG, group);
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        config.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(config);
        return consumer;
    }

    private long getOffset(KafkaConsumer<String, String> consumer, TopicPartition topicPartition) {
        OffsetAndMetadata offset = consumer.committed(topicPartition);
        long currentOffset = offset.offset();
        return currentOffset;
    }

    private long getEndOffset(KafkaConsumer<String, String> consumer, TopicPartition topicPartition) {
        consumer.assign(Arrays.asList(topicPartition));
        consumer.seekToEnd(Arrays.asList(topicPartition));
        long logEndOffset = consumer.position(topicPartition);
        return logEndOffset;
    }

    public static void main(String[] args) {
        KafkaAdminClient client = new KafkaAdminClient("localhost:2181", "localhost:9092");
        System.out.println(client.getBootstrapServers());
        System.out.println(client.getController());
        System.out.println(client.getAllTopics());
        System.out.println(client.getAllGroups());
        System.out.println(client.getGroupPartitions("console-consumer-68749"));
        System.out.println(client.getTopicMetadata("test"));
        System.out.println(client.getAllTopics());
        System.out.println(client.getOffsets("console-consumer-68749"));
    }
}