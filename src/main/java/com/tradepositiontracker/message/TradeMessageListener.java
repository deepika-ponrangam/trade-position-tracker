package com.tradepositiontracker.message;
import com.tradepositiontracker.dto.TradeResponse;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class TradeMessageListener {
    @JmsListener(destination = "trade.update.queue")
    public void receiveTradeUpdate(TradeResponse tradeResponse){
        System.out.println("Received Trade Update from ActiveMQ for Trade: " + tradeResponse.getTradeReference());
        System.out.println("Trade Status: " + tradeResponse.getStatus());
        System.out.println("Net Amount:"+ tradeResponse.getPrimaryAmount()+""+tradeResponse.getPrimaryCurrency());
    }
}
