/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fish.payara.patroneventos.eventsourcing.checking.messagelistener;

import java.util.logging.Logger;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;

/**
 *
 * @author carlospecam
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "clientId", propertyValue
            = "checkingAcctFundTransferListener"),
    @ActivationConfigProperty(propertyName = "groupIdConfig", propertyValue
            = "fundTransfer"),
    @ActivationConfigProperty(propertyName = "topics", propertyValue
            = "checkingacct-topic"),
    @ActivationConfigProperty(propertyName = "bootstrapServersConfig",
            propertyValue = "localhost:9092"),
    @ActivationConfigProperty(propertyName = "autoCommitInterval",
            propertyValue = "100"),
    @ActivationConfigProperty(propertyName = "retryBackoff",
            propertyValue = "1000"),
    @ActivationConfigProperty(propertyName = "keyDeserializer",
            propertyValue
            = "org.apache.kafka.common.serialization.StringDeserializer"),
    @ActivationConfigProperty(propertyName = "valueDeserializer",
            propertyValue
            = "org.apache.kafka.common.serialization.StringDeserializer"),
    @ActivationConfigProperty(propertyName = "pollInterval", propertyValue
            = "1000"),})
public class siniestroListener implements KafkaListener {

    private static final Logger LOGGER
            = Logger.getLogger(siniestroListener.class.getName());
    @Resource(lookup = "java:comp/env/KafkaConnectionFactory")
    private KafkaConnectionFactory kafkaConnectionFactory;
    @Inject
    private checkingAllSiniestros checkSiniestro;

    @OnRecord(topics = {"checkingacct-topic"})
    public void siniestroOccurs(ConsumerRecord consumerRecord) {
        String siniestroString = (String) consumerRecord.value();
        siniestroDTO siniestrodto
                = FundTransferDTOUtil.jsonToFundTransferDTO(siniestroString);
        if (fundTransferDTO.getSourceAcctType().equals(AccountType.CHECKING)) {
            LOGGER.log(Level.INFO, String.format(
                    "Withdrawing %.2f currency units from checking",
                    fundTransferDTO.getAmt()));
            if (checkingAcctMgr.withdrawFunds(fundTransferDTO)) {
                try (KafkaConnection kafkaConnection
                        = kafkaConnectionFactory.createConnection()) {
                    kafkaConnection.send(new ProducerRecord("savingsaccttopic",
                            siniestroString));
                } catch (Exception ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE,
                            null, ex);
                }
            } else {
                LOGGER.log(Level.WARNING,
                        "There was a problem withdrawing funds, aborting transfer.");
            }
        } else if (fundTransferDTO.getDestAcctType().equals(
                AccountType.CHECKING)) {
            LOGGER.log(Level.INFO, String.format(
                    "Depositing %.2f currency units to checking",
                    fundTransferDTO.getAmt()));
            checkingAcctMgr.depositFunds(fundTransferDTO);
        }
    }
}
}
