package finalProject.API.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.DatagramAcceptor;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class MultiThreadedUDPServer {
    private static final MultiThreadedUDPServer instance = new MultiThreadedUDPServer();
    private KafkaProducer<String, String> producer;
    public static MultiThreadedUDPServer getInstance() {
        return instance;
    }
    private MultiThreadedUDPServer() {
        // Kafka Producer 초기화
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        this.producer = new KafkaProducer<>(props);
    }
    public void startServer() {
        DatagramAcceptor acceptor = new NioDatagramAcceptor();
        acceptor.setHandler(new UDPHandler(producer));
        DatagramSessionConfig dcfg = acceptor.getSessionConfig();
        dcfg.setReuseAddress(true);
        try {
            acceptor.bind(new InetSocketAddress(22101));
            System.out.println("MultiThreaded UDP Echo Server is listening on port 22101");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static class UDPHandler extends IoHandlerAdapter {
        private final ExecutorService threadPool = Executors.newCachedThreadPool();
        private KafkaProducer<String, String> producer;
        public UDPHandler(KafkaProducer<String, String> producer) {
            this.producer = producer;
        }
        @Override
        public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
            cause.printStackTrace();
        }
        @Override
        public void messageReceived(IoSession session, Object message) throws Exception {
            if (message instanceof IoBuffer) {
                IoBuffer buffer = (IoBuffer) message;
                threadPool.execute(new ClientHandler(session, buffer,producer));
            }
        }
    }
    private static class ClientHandler implements Runnable {
        private IoSession session;
        private IoBuffer buffer;
        private StockA3 stockA3;
        private long startTime;
        private long endTime;
        private KafkaProducer<String, String> producer;
        public ClientHandler(IoSession session, IoBuffer message, KafkaProducer<String, String> producer) {
            this.session = session;
            this.buffer = message.duplicate(); // IoBuffer 복제
            this.stockA3 = new StockA3(); // StockA3 객체 초기화
            this.producer = producer;
        }
        public void run() {
            try {
                long threadCnt = Thread.activeCount();
                long threadId = Thread.currentThread().getId();
                if (buffer.remaining() < 195) {
                    String bufferContent = buffer.getString(buffer.remaining(), StandardCharsets.UTF_8.newDecoder());
                    System.out.println("T_id = " + threadId + " : " + bufferContent);
                    return;
                }
                // 받은 메세지를 스레드가 코드별로 분류해서 처리
                StockA3 transaction = stockA3.parseTransaction(buffer);
                // 여기서부터는 transaction 객체를 이용하여 원하는 작업 수행
                String logMessage = "timestamp=" + transaction.get거래시간() + ":" + "stockCode=" + transaction.get종목코드() + ":" +
                        "dealPrice=" + transaction.get체결가격() + ":" +
                        "dealVolume=" + transaction.get거래량() + ":" +
                        "totalDealVolume=" + transaction.get누적거래량();
                //System.out.println(logMessage);
                producer.send(new ProducerRecord<>("stock-input", Long.toString(threadId), logMessage));  // Kafka에 메시지 보내기
                producer.send(new ProducerRecord<>("stock", Long.toString(threadId), logMessage));  // Kafka에 메시지 보내기
                // DB에  insert하기
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}