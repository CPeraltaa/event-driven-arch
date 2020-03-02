/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fish.payara.patroneventos.controller;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author carlospecam
 */
@Named
@ViewScoped
public class siniestroController implements Serializable {
 @Resource(lookup = "java:module/env/KafkaConnectionFactory")
 private KafkaConnectionFactory kafkaConnectionFactory;
 @Inject
 private FacesContext facesContext;
 private static final Logger LOGGER =
 Logger.getLogger(siniestroController.class.getName());
 
 public void makeaRequest() {
 LOGGER.log(Level.INFO, String.format("{0}.makeaRequest() invoked",
 this.getClass().getClass()));
 siniestroDTO siniestro = new siniestroDTO();
 siniestro.setDate(03/01/2020);
 siniestro.setVehiculo("P648GNF");
 siniestro.setDescripcion("Accidente en redondel");
 siniestro.setDamage("Primario, frontal");
 siniestro.setTerceroNombre("Fernando Rodriguez");
 siniestro.setTerceroVechiculo("P375CDR");
 sendCheckingMessage(siniestro);
 facesContext.addMessage("msgs",
 new FacesMessage(FacesMessage.SEVERITY_INFO,
 "Alerta enviada satisfactoriamente successfully",
 "Mensaje de alerta enviado satisfactoriamente"));
 }
 public void simulateError() {
 LOGGER.log(Level.INFO,
 String.format("{0}.simulateError() invoked",
 this.getClass().getClass()));
 siniestroDTO siniestro = new siniestroDTO();
 siniestro.setDate(03/01/2020);
 siniestro.setVehiculo("P648GNR"); //placa invalida debe mostrar error
 siniestro.setDescripcion("Accidente en redondel");
 siniestro.setDamage("Primario, frontal");
 siniestro.setTerceroNombre("Fernando Rodriguez");
 siniestro.setTerceroVechiculo("P375CDR");
 sendCheckingMessage(siniestro);
 facesContext.addMessage("msgs",
 new FacesMessage(FacesMessage.SEVERITY_INFO,
 "Alerta con error enviada satisfactoriamente successfully",
 "Mensaje de alerta con error enviado satisfactoriamente"));
 }
 
private void sendCheckingMessage(siniestroDTO siniestro) {
 String siniestroString;
 siniestroString =
 siniestroUtils.siniestroDTOtoJSON(siniestro);
 try (KafkaConnection kafkaConnection =
 kafkaConnectionFactory.createConnection()) {
 kafkaConnection.send(new ProducerRecord("checkingacct-topic",siniestroString));
 } catch (Exception ex) {
 Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
 }
 }
}