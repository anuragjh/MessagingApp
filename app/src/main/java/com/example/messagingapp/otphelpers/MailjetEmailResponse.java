package com.example.messagingapp.otphelpers;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MailjetEmailResponse {

    @SerializedName("Messages")
    private List<MessageStatus> messages;

    public List<MessageStatus> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageStatus> messages) {
        this.messages = messages;
    }

    public static class MessageStatus {
        @SerializedName("To")
        private List<RecipientStatus> to;

        @SerializedName("Status")
        private String status;

        @SerializedName("CustomID")
        private String customId;

        public List<RecipientStatus> getTo() {
            return to;
        }

        public void setTo(List<RecipientStatus> to) {
            this.to = to;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getCustomId() {
            return customId;
        }

        public void setCustomId(String customId) {
            this.customId = customId;
        }

        public static class RecipientStatus {
            @SerializedName("Email")
            private String email;

            @SerializedName("MessageUUID")
            private String messageUuid;

            @SerializedName("Status")
            private String status;

            public String getEmail() {
                return email;
            }

            public void setEmail(String email) {
                this.email = email;
            }

            public String getMessageUuid() {
                return messageUuid;
            }

            public void setMessageUuid(String messageUuid) {
                this.messageUuid = messageUuid;
            }

            public String getStatus() {
                return status;
            }

            public void setStatus(String status) {
                this.status = status;
            }
        }
    }
}
