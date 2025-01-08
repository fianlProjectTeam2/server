package finalProject.API.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Properties;

public class KafkaWebSocketServer {

    public void startKafkaConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");  // Kafka 서버 주소
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "websocket-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("stock_output"));  // 읽고자 하는 Kafka 토픽명
        // Kafka 메시지 수신 및 WebSocket 전송 쓰레드 실행
        new Thread(() -> {
            try {
                while (true) {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                    for (ConsumerRecord<String, String> record : records) {
                        System.out.printf("Received message: key = %s, value = %s, topic = %s, partition = %d, offset = %d\n",
                                record.key(), record.value(), record.topic(), record.partition(), record.offset());
                        // Kafka 메시지를 WebSocket 클라이언트들에게 브로드캐스트
                        // 거래시간 / 종목코드 / 체결가격 / 거래량 / 누적 거래량
                        // 데이터 파싱
                        String[] pairs = record.value().split(":");
                        JSONObject stockData = new JSONObject();
                        for (String pair : pairs) {
                            String[] keyValue = pair.split("=");
                            if (keyValue.length == 2) {
                                String key = keyValue[0].trim();
                                String value = keyValue[1].trim();
                                // 필요에 따라 변환
                                switch (key) {
                                    case "거래시간":
                                        stockData.put("timestamp", value);
                                        break;
                                    case "종목코드":
                                        stockData.put("symbol", value);
                                        break;
                                    case "체결가격":
                                        stockData.put("price", Double.parseDouble(value));
                                        break;
                                    case "거래량":
                                        stockData.put("volume", Integer.parseInt(value));
                                        break;
                                    case "누적거래량":
                                        stockData.put("cumulativeVolume", Long.parseLong(value));
                                        break;
                                    default:
                                        System.out.println("Unknown key: " + key);
                                }
                            }
                        }
                        if(stockData.getInt("volume") > 1000){
                            System.out.println("stockData = " + stockData.toString());
                        }
                        // Kafka 메시지를 WebSocket 클라이언트들에게 브로드캐스트
                        broadcastMessage(stockData.toString());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                consumer.close();
            }
        }).start();
    }
}