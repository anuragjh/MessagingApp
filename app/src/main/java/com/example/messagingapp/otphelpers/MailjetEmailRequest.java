package com.example.messagingapp.otphelpers;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MailjetEmailRequest {

    @SerializedName("Messages")
    private List<Message> messages;

    public MailjetEmailRequest(List<Message> messages) {
        this.messages = messages;
    }

    // Getter
    public List<Message> getMessages() {
        return messages;
    }

    public static class Message {
        @SerializedName("From")
        private Email from;

        @SerializedName("To")
        private List<Email> to;

        @SerializedName("Subject")
        private String subject;

        @SerializedName("HtmlPart") // Changed from TextPart to HtmlPart
        private String htmlPart;

        public Message(Email from, List<Email> to, String subject, String htmlPart) {
            this.from = from;
            this.to = to;
            this.subject = subject;
            this.htmlPart = htmlPart;
        }

        // Getters
        public Email getFrom() {
            return from;
        }

        public List<Email> getTo() {
            return to;
        }

        public String getSubject() {
            return subject;
        }

        public String getHtmlPart() {
            return htmlPart;
        }
    }

    public static class Email {
        @SerializedName("Email")
        private String email;

        public Email(String email) {
            this.email = email;
        }

        // Getter
        public String getEmail() {
            return email;
        }
    }


    }
