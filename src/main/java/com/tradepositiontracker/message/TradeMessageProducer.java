package com.tradepositiontracker.message;
import com.tradepositiontracker.dto.TradeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor

public class TradeMessageProducer {
  private final JmsTemplate jmsTemplate;
  private static final String TRADE_QUEUE= "trade.update.queue";
  public void sendTradeUpdate(TradeResponse tradeResponse){
    jmsTemplate.convertAndSend(TRADE_QUEUE, tradeResponse);
    System.out.println("Sent Trade Update to ActiveMQ | Trade: " + tradeResponse.getTradeReference() + " | Status: " + tradeResponse.getStatus());
    }
}
