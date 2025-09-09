package com.itu.socialcom.demo.whatsapp.service;

/**
 * Service for sending WhatsApp messages using the Meta Graph API.
 */
public interface WhatsAppService {

    /**
     * Generates or retrieves a valid access token for WhatsApp API.
     * The token is automatically renewed if expired.
     * 
     * @return The valid access token
     */
    String getAccessToken();

    /**
     * Sends a WhatsApp message to the specified phone number.
     * 
     * @param phoneNumber The recipient's phone number (with country code)
     * @param message The message to send
     * @return true if the message was sent successfully, false otherwise
     */
    boolean sendMessage(String phoneNumber, String message);

    /**
     * Sends a WhatsApp template message to the specified phone number.
     * 
     * @param phoneNumber The recipient's phone number (with country code)
     * @param templateName The name of the template to use
     * @param parameters The parameters to fill in the template
     * @return true if the message was sent successfully, false otherwise
     */
    boolean sendTemplateMessage(String phoneNumber, String templateName, Object... parameters);

    /**
     * Sends a WhatsApp "hello_world" template message to the specified phone number.
     * This is a convenience method that implements the exact format from the example curl command.
     * 
     * @param phoneNumber The recipient's phone number (with country code)
     * @return true if the message was sent successfully, false otherwise
     */
    boolean sendHelloWorldTemplate(String phoneNumber);
}
